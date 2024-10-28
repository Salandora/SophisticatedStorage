package net.p3pp3rf1y.sophisticatedstorage.upgrades.hopper;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.p3pp3rf1y.porting_lib.base.util.LazyOptional;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ContentsFilterLogic;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogic;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ITickableUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.block.VerticalFacing;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.BlockSide;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.upgrades.INeighborChangeListenerUpgrade;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class HopperUpgradeWrapper extends UpgradeWrapperBase<HopperUpgradeWrapper, HopperUpgradeItem>
		implements ITickableUpgrade, INeighborChangeListenerUpgrade {

	private Set<Direction> pullDirections = new LinkedHashSet<>();
	private Set<Direction> pushDirections = new LinkedHashSet<>();
	private boolean directionsInitialized = false;

	private final Map<Direction, ItemHandlerHolder> handlerCache = new EnumMap<>(Direction.class);

	private final ContentsFilterLogic inputFilterLogic;
	private final TargetContentsFilterLogic outputFilterLogic;
	private long coolDownTime = 0;

	protected HopperUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
		super(storageWrapper, upgrade, upgradeSaveHandler);
		inputFilterLogic = new ContentsFilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getInputFilterSlotCount(), storageWrapper::getInventoryHandler,
				storageWrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class), "inputFilter");
		outputFilterLogic = new TargetContentsFilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getOutputFilterSlotCount(), storageWrapper::getInventoryHandler,
				storageWrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class), "outputFilter");

		deserialize();
	}

	@Override
	public void tick(@Nullable Entity entity, Level level, BlockPos pos) {
		initDirections(level, pos);

		if (coolDownTime > level.getGameTime()) {
			return;
		}

		for (Direction pushDirection : pushDirections) {
			boolean done = false;
			for (Storage<ItemVariant> itemHandler : getItemHandlers(level, pos, pushDirection, entity == null)) {
				if (pushItems(itemHandler)) {
					done = true;
					break;
				}
			}
			if (!done) {
				for (WorldlyContainer worldlyContainer : getWorldlyContainers(level, pos, pushDirection)) {
					if (pushItemsToContainer(worldlyContainer, pushDirection.getOpposite())) {
						break;
					}
				}
			}

			if (!done) {
				getEntityContainer(level, pos, pushDirection, entity).ifPresent(container -> {
					pushItemsToContainer(container, pushDirection.getOpposite());
				});
			}
		}

		for (Direction pullDirection : pullDirections) {
			boolean done = false;
			for (Storage<ItemVariant> itemHandler : getItemHandlers(level, pos, pullDirection, entity == null)) {
				if (pullItems(itemHandler)) {
					done = true;
					break;
				}
			}

			if (!done) {
				for (WorldlyContainer worldlyContainer : getWorldlyContainers(level, pos, pullDirection)) {
					if (pullItemsFromContainer(worldlyContainer, pullDirection.getOpposite())) {
						done = true;
						break;
					}
				}
			}

			if (!done) {
				getEntityContainer(level, pos, pullDirection, entity).ifPresent(container -> {
					pullItemsFromContainer(container, pullDirection.getOpposite());
				});
			}
		}

		coolDownTime = level.getGameTime() + upgradeItem.getTransferSpeedTicks();
	}

	private Optional<Container> getEntityContainer(Level level, BlockPos pos, Direction direction, @Nullable Entity entity) {
		BlockState storageState = level.getBlockState(pos);
		List<BlockPos> offsetPositions = entity == null && storageState.getBlock() instanceof StorageBlockBase storageBlock ? storageBlock.getNeighborPos(storageState, pos, direction) : List.of(pos.relative(direction));

		List<Entity> entities = new ArrayList<>();
		for (BlockPos offsetPosition : offsetPositions) {
			entities.addAll(level.getEntities((Entity)null, new AABB(offsetPosition), e -> e != entity && EntitySelector.CONTAINER_ENTITY_SELECTOR.test(e)));
		}
		if (!entities.isEmpty()) {
			Collections.shuffle(entities);
			return Optional.of((Container) entities.get(0));
		}
		return Optional.empty();
	}

	private boolean pushItemsToContainer(Container worldlyContainer, Direction face) {
		ITrackedContentsItemHandler fromHandler = storageWrapper.getInventoryForUpgradeProcessing();

		outputFilterLogic.setInventory(Storage.empty());
		for (StorageView<ItemVariant> view : fromHandler.nonEmptyViews()) {
			if (!view.isResourceBlank() && outputFilterLogic.matchesFilter(view.getResource().toStack((int) view.getAmount()))) {
				long extracted = StorageUtil.simulateExtract(fromHandler, view.getResource(), Math.min(worldlyContainer.getMaxStackSize(), upgradeItem.getMaxTransferStackSize()), null);
				if (extracted > 0 && pushStackToContainer(worldlyContainer, face, extracted, view)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean pushStackToContainer(Container container, Direction face, long extracted, StorageView<ItemVariant> view) {
		ItemStack extractedStack = view.getResource().toStack((int) extracted);
		for (int containerSlot = 0; containerSlot < container.getContainerSize(); containerSlot++) {
			if (!(container instanceof WorldlyContainer worldlyContainer) || worldlyContainer.canPlaceItemThroughFace(containerSlot, extractedStack, face)) {
				ItemStack existingStack = container.getItem(containerSlot);
				if (existingStack.isEmpty()) {
					container.setItem(containerSlot, extractedStack);
					try (Transaction ctx = Transaction.openOuter()) {
						view.extract(view.getResource(), extracted, ctx);
						ctx.commit();
					}
					return true;
				} else if (ItemHandlerHelper.canItemStacksStack(existingStack, extractedStack)) {
					int maxStackSize = Math.min(container.getMaxStackSize(), existingStack.getMaxStackSize());
					int remainder = maxStackSize - existingStack.getCount();
					if (remainder > 0) {
						int countToExtract = (int) Math.min(extracted, remainder);
						existingStack.grow(countToExtract);
						container.setItem(containerSlot, existingStack);
						try (Transaction ctx = Transaction.openOuter()) {
							view.extract(view.getResource(), countToExtract, ctx);
							ctx.commit();
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean pullItemsFromContainer(Container container, Direction face) {
		ITrackedContentsItemHandler toHandler = storageWrapper.getInventoryForUpgradeProcessing();
		for (int containerSlot = 0; containerSlot < container.getContainerSize(); containerSlot++) {
			ItemStack stackToInsert = container.getItem(containerSlot).copy();
			if (stackToInsert.getCount() > upgradeItem.getMaxTransferStackSize()) {
				stackToInsert.setCount(upgradeItem.getMaxTransferStackSize());
			}
			if (!stackToInsert.isEmpty()
					&& (!(container instanceof WorldlyContainer worldlyContainer) || worldlyContainer.canTakeItemThroughFace(containerSlot, stackToInsert, face))
					&& inputFilterLogic.matchesFilter(stackToInsert)) {
				ItemVariant resource = ItemVariant.of(stackToInsert);
				long maxAmount = stackToInsert.getCount();
				try (Transaction ctx = Transaction.openOuter()) {
					maxAmount -= toHandler.insert(resource, maxAmount, ctx);
					ctx.commit();
				}

				if (maxAmount > 0) {
					container.setItem(containerSlot, resource.toStack((int) maxAmount));
					return true;
				}
			}
		}

		return false;
	}

	private void initDirections(Level level, BlockPos pos) {
		if (upgrade.hasTag() && (upgrade.getItem() != ModItems.HOPPER_UPGRADE || directionsInitialized)) {
			return;
		}
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() instanceof StorageBlockBase storageBlock) {
			Direction horizontalDirection = storageBlock.getHorizontalDirection(state);
			VerticalFacing verticalFacing = storageBlock.getVerticalFacing(state);
			pullDirections.clear();
			pushDirections.clear();
			initDirections(BlockSide.BOTTOM.toDirection(horizontalDirection, verticalFacing), BlockSide.TOP.toDirection(horizontalDirection, verticalFacing));
			directionsInitialized = true;
		} else {
			initDirections(Direction.DOWN, Direction.UP);
		}
	}

	private List<WorldlyContainer> getWorldlyContainers(Level level, BlockPos pos, Direction direction) {
		BlockState storageState = level.getBlockState(pos);
		List<BlockPos> offsetPositions = storageState.getBlock() instanceof StorageBlockBase storageBlock ? storageBlock.getNeighborPos(storageState, pos, direction) : List.of(pos.relative(direction));
		List<WorldlyContainer> worldlyContainers = new ArrayList<>();
		offsetPositions.forEach(offsetPos -> {
			BlockState state = level.getBlockState(offsetPos);
			if (state.getBlock() instanceof WorldlyContainerHolder worldlyContainerHolder) {
				worldlyContainers.add(worldlyContainerHolder.getContainer(state, level, offsetPos));
			}
		});
		return worldlyContainers;
	}

	private boolean pullItems(Storage<ItemVariant> fromHandler) {
		if (moveItems(fromHandler, storageWrapper.getInventoryForUpgradeProcessing(), inputFilterLogic)) {
			return true;
		}
		return false;
	}

	private boolean pushItems(Storage<ItemVariant> toHandler) {
		outputFilterLogic.setInventory(toHandler);
		if (moveItems(storageWrapper.getInventoryForUpgradeProcessing(), toHandler, outputFilterLogic)) {
			return true;
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

		BlockState storageState = level.getBlockState(pos);
		List<BlockPos> offsetPositions = storageState.getBlock() instanceof StorageBlockBase storageBlock ? storageBlock.getNeighborPos(storageState, pos, direction) : List.of(pos.relative(direction));

		List<BlockApiCache<Storage<ItemVariant>, Direction>> caches = new ArrayList<>();

		AtomicBoolean refreshOnEveryNeighborChange = new AtomicBoolean(false);
		offsetPositions.forEach(offsetPos -> {
			offsetPos = level.getBlockEntity(offsetPos, ModBlocks.STORAGE_INPUT_BLOCK_ENTITY_TYPE)
					.flatMap(storageInputBlockEntity -> {
						refreshOnEveryNeighborChange.set(true);
						return storageInputBlockEntity.getControllerPos();
					}).orElse(offsetPos);

			caches.add(BlockApiCache.create(ItemStorage.SIDED, serverLevel, offsetPos));
		});
		handlerCache.put(direction, new ItemHandlerHolder(caches, refreshOnEveryNeighborChange.get()));
	}

	private List<Storage<ItemVariant>> getItemHandlers(Level level, BlockPos pos, Direction direction, boolean useCache) {
		if (useCache) {
			if (!handlerCache.containsKey(direction)) {
				updateCacheOnSide(level, pos, direction);
			}
		}

		return handlerCache.containsKey(direction) ? handlerCache.get(direction).handlers().stream().map(handlerCache -> handlerCache.find(direction.getOpposite())).filter(Objects::nonNull).toList() : Collections.emptyList();
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
		NBTHelper.putList(upgrade.getOrCreateTag(), "pullDirections", pullDirections, d -> StringTag.valueOf(d.getSerializedName()));
		save();
	}

	private void serializePushDirections() {
		NBTHelper.putList(upgrade.getOrCreateTag(), "pushDirections", pushDirections, d -> StringTag.valueOf(d.getSerializedName()));
		save();
	}

	public void deserialize() {
		pullDirections.clear();
		pushDirections.clear();
		if (upgrade.hasTag()) {
			pullDirections = NBTHelper.getCollection(upgrade.getOrCreateTag(), "pullDirections", Tag.TAG_STRING, t -> Optional.ofNullable(Direction.byName(t.getAsString())), HashSet::new).orElseGet(HashSet::new);
			pushDirections = NBTHelper.getCollection(upgrade.getOrCreateTag(), "pushDirections", Tag.TAG_STRING, t -> Optional.ofNullable(Direction.byName(t.getAsString())), HashSet::new).orElseGet(HashSet::new);
		}
	}

	public void initDirections(Direction pushDirection, Direction pullDirection) {
		setPushingTo(pushDirection, true);
		setPullingFrom(pullDirection, true);
	}

	private record ItemHandlerHolder(List<BlockApiCache<Storage<ItemVariant>, Direction>> handlers, boolean refreshOnEveryNeighborChange) {
	}
}
