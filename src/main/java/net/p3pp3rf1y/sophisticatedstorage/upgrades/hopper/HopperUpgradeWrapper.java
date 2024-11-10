package net.p3pp3rf1y.sophisticatedstorage.upgrades.hopper;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ContentsFilterLogic;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogic;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ITickableUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.block.VerticalFacing;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.BlockSide;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.upgrades.INeighborChangeListenerUpgrade;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class HopperUpgradeWrapper extends UpgradeWrapperBase<HopperUpgradeWrapper, HopperUpgradeItem>
		implements ITickableUpgrade, INeighborChangeListenerUpgrade {

	private final Set<Direction> pullDirections = new LinkedHashSet<>();
	private final Set<Direction> pushDirections = new LinkedHashSet<>();
	private final Map<Direction, ItemHandlerHolder> handlerCache = new EnumMap<>(Direction.class);

	private final ContentsFilterLogic inputFilterLogic;
	private final TargetContentsFilterLogic outputFilterLogic;
	private long coolDownTime = 0;

	protected HopperUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
		super(storageWrapper, upgrade, upgradeSaveHandler);
		inputFilterLogic = new ContentsFilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getInputFilterSlotCount(), storageWrapper::getInventoryHandler,
				storageWrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class), ModCoreDataComponents.INPUT_FILTER_ATTRIBUTES);
		outputFilterLogic = new TargetContentsFilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getOutputFilterSlotCount(), storageWrapper::getInventoryHandler,
				storageWrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class), ModDataComponents.OUTPUT_FILTER_ATTRIBUTES);

		deserialize();
	}

	@Override
	public void tick(@Nullable LivingEntity entity, Level level, BlockPos pos) {
		initDirections(level, pos);

		if (coolDownTime > level.getGameTime()) {
			return;
		}

		for (Direction pushDirection : pushDirections) {
			if (runOnItemHandlers(level, pos, pushDirection, this::pushItems)) {
				break;
			}
		}

		for (Direction pullDirection : pullDirections) {
			if (runOnItemHandlers(level, pos, pullDirection, this::pullItems)) {
				break;
			}
		}

		coolDownTime = level.getGameTime() + upgradeItem.getTransferSpeedTicks();
	}

	private void initDirections(Level level, BlockPos pos) {
		if (upgrade.has(ModDataComponents.PUSH_DIRECTIONS) || upgrade.has(ModDataComponents.PULL_DIRECTIONS)) {
			return;
		}
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() instanceof StorageBlockBase storageBlock) {
			Direction horizontalDirection = storageBlock.getHorizontalDirection(state);
			VerticalFacing verticalFacing = storageBlock.getVerticalFacing(state);
			pullDirections.clear();
			pushDirections.clear();
			initDirections(BlockSide.BOTTOM.toDirection(horizontalDirection, verticalFacing), BlockSide.TOP.toDirection(horizontalDirection, verticalFacing));
		}
	}

	private boolean pullItems(List<Storage<ItemVariant>> fromHandlers) {
		for (Storage<ItemVariant> fromHandler : fromHandlers) {
			if (moveItems(fromHandler, storageWrapper.getInventoryForUpgradeProcessing(), inputFilterLogic)) {
				return true;
			}
		}
		return false;
	}

	private boolean pushItems(List<Storage<ItemVariant>> toHandlers) {
		for (Storage<ItemVariant> toHandler : toHandlers) {
			outputFilterLogic.setInventory(toHandler);
			if (moveItems(storageWrapper.getInventoryForUpgradeProcessing(), toHandler, outputFilterLogic)) {
				return true;
			}
		}
		return false;
	}

	private boolean moveItems(Storage<ItemVariant> fromHandler, Storage<ItemVariant> toHandler, FilterLogic filterLogic) {
		for (StorageView<ItemVariant> view : fromHandler.nonEmptyViews()) {
			ItemVariant resource = view.getResource();
			ItemStack slotStack = resource.toStack((int) view.getAmount());
			if (!slotStack.isEmpty() && filterLogic.matchesFilter(slotStack)) {
				long maxExtracted = StorageUtil.simulateExtract(view, resource, upgradeItem.getMaxTransferStackSize(), null);

				try (Transaction transferTransaction = Transaction.openOuter()) {
					long accepted = toHandler.insert(resource, maxExtracted, transferTransaction);
					if (fromHandler.extract(resource, accepted, transferTransaction) == accepted) {
						transferTransaction.commit();
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void onNeighborChange(Level level, BlockPos pos, Direction direction) {
		if (!level.isClientSide() && (pushDirections.contains(direction) || pullDirections.contains(direction))
				&& needsCacheUpdate(level, pos, direction)) {
			updateCacheOnSide(level, pos, direction);
		}
	}

	private boolean needsCacheUpdate(Level level, BlockPos pos, Direction direction) {
		ItemHandlerHolder holder = handlerCache.get(direction);
		if (holder == null || holder.handlers().isEmpty()) {
			return !level.getBlockState(pos).isAir();
		} else if (holder.refreshOnEveryNeighborChange()) {
			return true;
		}

		for (BlockApiCache<Storage<ItemVariant>, Direction> handler : holder.handlers()) {
			if (handler.find(direction.getOpposite()) == null) {
				return true;
			}
		}

		return false;
	}

	public void updateCacheOnSide(Level level, BlockPos pos, Direction direction) {
		if (!level.isLoaded(pos) || !level.isLoaded(pos.relative(direction)) || !(level instanceof ServerLevel serverLevel)) {
			handlerCache.remove(direction);
			return;
		}

		//WeakReference<HopperUpgradeWrapper> existRef = new WeakReference<>(this);

		BlockState storageState = level.getBlockState(pos);
		List<BlockPos> offsetPositions = storageState.getBlock() instanceof StorageBlockBase storageBlock ? storageBlock.getNeighborPos(storageState, pos, direction) : List.of(pos.relative(direction));

		List<BlockApiCache<Storage<ItemVariant>, Direction>> caches = new ArrayList<>();

		AtomicBoolean refreshOnEveryNeighborChange = new AtomicBoolean(false);
		offsetPositions.forEach(offsetPos -> {
			offsetPos = level.getBlockEntity(offsetPos, ModBlocks.STORAGE_INPUT_BLOCK_ENTITY_TYPE.get())
					.flatMap(storageInputBlockEntity -> {
								refreshOnEveryNeighborChange.set(true);
								return storageInputBlockEntity.getControllerPos();
							}
					).orElse(offsetPos);

			caches.add(BlockApiCache.create(ItemStorage.SIDED, serverLevel, offsetPos));
			// caches.add(BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, serverLevel, offsetPos, direction.getOpposite(), () -> existRef.get() != null, () -> updateCacheOnSide(level, pos, direction)));
		});
		handlerCache.put(direction, new ItemHandlerHolder(caches, refreshOnEveryNeighborChange.get()));
	}

	private boolean runOnItemHandlers(Level level, BlockPos pos, Direction direction, Predicate<List<Storage<ItemVariant>>> run) {
		if (!handlerCache.containsKey(direction)) {
			updateCacheOnSide(level, pos, direction);
		}
		if (handlerCache.get(direction) == null) {
			return false;
		}

		List<Storage<ItemVariant>> handler = handlerCache.get(direction).handlers().stream().map(handlerCache -> handlerCache.find(direction.getOpposite())).filter(Objects::nonNull).toList();

		return run.test(handler);
	}

	public ContentsFilterLogic getInputFilterLogic() {
		return inputFilterLogic;
	}

	public ContentsFilterLogic getOutputFilterLogic() {
		return outputFilterLogic;
	}

	public boolean isPullingFrom(Direction direction) {
		return pullDirections.contains(direction);
	}

	public boolean isPushingTo(Direction direction) {
		return pushDirections.contains(direction);
	}

	public void setPullingFrom(Direction direction, boolean shouldPull) {
		if (shouldPull) {
			pullDirections.add(direction);
		} else {
			pullDirections.remove(direction);
		}
		serializePullDirections();
	}

	public void setPushingTo(Direction direction, boolean isPushing) {
		if (isPushing) {
			pushDirections.add(direction);
		} else {
			pushDirections.remove(direction);
		}
		serializePushDirections();
	}

	private void serializePullDirections() {
		upgrade.set(ModDataComponents.PULL_DIRECTIONS, Set.copyOf(pullDirections));
		save();
	}

	private void serializePushDirections() {
		upgrade.set(ModDataComponents.PUSH_DIRECTIONS, Set.copyOf(pushDirections));
		save();
	}

	public void deserialize() {
		pullDirections.clear();
		pushDirections.clear();
		Set<Direction> directions = upgrade.get(ModDataComponents.PULL_DIRECTIONS);
		if (directions != null) {
			pullDirections.addAll(directions);
		}
		directions = upgrade.get(ModDataComponents.PUSH_DIRECTIONS);
		if (directions != null) {
			pushDirections.addAll(directions);
		}
	}

	public void initDirections(Direction pushDirection, Direction pullDirection) {
		setPushingTo(pushDirection, true);
		setPullingFrom(pullDirection, true);
	}

	private record ItemHandlerHolder(List<BlockApiCache<Storage<ItemVariant>, Direction>> handlers,
									 boolean refreshOnEveryNeighborChange) {
	}
}
