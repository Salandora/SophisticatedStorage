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
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.porting_lib.base.util.LazyOptional;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ContentsFilterLogic;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogic;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ITickableUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedcore.util.WorldHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageInputBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.VerticalFacing;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.BlockSide;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.upgrades.INeighborChangeListenerUpgrade;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class HopperUpgradeWrapper extends UpgradeWrapperBase<HopperUpgradeWrapper, HopperUpgradeItem>
		implements ITickableUpgrade, INeighborChangeListenerUpgrade {

	private Set<Direction> pullDirections = new LinkedHashSet<>();
	private Set<Direction> pushDirections = new LinkedHashSet<>();
	private boolean directionsInitialized = false;

	private final Map<Direction, ItemHandlerHolder> handlerCache = new EnumMap<>(Direction.class);
	//private final Map<Direction, BlockApiCache<Storage<ItemVariant>, Direction>> handlerCache = new EnumMap<>(Direction.class);

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

	// TODO: Necessary?
	/*private boolean pushItemsToContainer(WorldlyContainer worldlyContainer, Direction face) {
		ITrackedContentsItemHandler fromHandler = storageWrapper.getInventoryForUpgradeProcessing();

		outputFilterLogic.setInventory(EmptyHandler.INSTANCE);
		for (int slot = 0; slot < fromHandler.getSlotCount(); slot++) {
			ItemStack slotStack = fromHandler.getStackInSlot(slot);
			if (!slotStack.isEmpty() && outputFilterLogic.matchesFilter(slotStack)) {
				try (Transaction extractionSimulation = Transaction.openOuter())
				fromHandler.extractSlot()
				ItemStack extractedStack = StorageUtil.simulateExtract(fromHandler, ) fromHandler.extractItem(slot, Math.min(worldlyContainer.getMaxStackSize(), upgradeItem.getMaxTransferStackSize()), true);
				if (!extractedStack.isEmpty() && pushStackToContainer(worldlyContainer, face, extractedStack, fromHandler, slot)) {
					return true;
				}
			}
		}

		return false;
	}
	private boolean pushItemsToContainer(WorldlyContainer worldlyContainer, Direction face) {
		ITrackedContentsItemHandler fromHandler = storageWrapper.getInventoryForUpgradeProcessing();

		outputFilterLogic.setInventory(EmptyHandler.INSTANCE);
		for (int slot = 0; slot < fromHandler.getSlots(); slot++) {
			ItemStack slotStack = fromHandler.getStackInSlot(slot);
			if (!slotStack.isEmpty() && outputFilterLogic.matchesFilter(slotStack)) {
				ItemStack extractedStack = fromHandler.extractItem(slot, Math.min(worldlyContainer.getMaxStackSize(), upgradeItem.getMaxTransferStackSize()), true);
				if (!extractedStack.isEmpty() && pushStackToContainer(worldlyContainer, face, extractedStack, fromHandler, slot)) {
					return true;
				}
			}
		}

		return false;
	}


	// TODO: Necessary?
	private boolean pushStackToContainer(WorldlyContainer worldlyContainer, Direction face, ItemStack extractedStack, ITrackedContentsItemHandler fromHandler, int slotToExtractFrom) {
		for (int containerSlot = 0; containerSlot < worldlyContainer.getContainerSize(); containerSlot++) {
			if (worldlyContainer.canPlaceItemThroughFace(containerSlot, extractedStack, face)) {
				ItemStack existingStack = worldlyContainer.getItem(containerSlot);
				if (existingStack.isEmpty()) {
					worldlyContainer.setItem(containerSlot, extractedStack);
					fromHandler.extractItem(slotToExtractFrom, extractedStack.getCount(), false);
					return true;
				} else if (ItemHandlerHelper.canItemStacksStack(existingStack, extractedStack)) {
					int maxStackSize = Math.min(worldlyContainer.getMaxStackSize(), existingStack.getMaxStackSize());
					int remainder = maxStackSize - existingStack.getCount();
					if (remainder > 0) {
						int countToExtract = Math.min(extractedStack.getCount(), remainder);
						existingStack.grow(countToExtract);
						worldlyContainer.setItem(containerSlot, existingStack);
						fromHandler.extractItem(slotToExtractFrom, countToExtract, false);
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean pullItemsFromContainer(WorldlyContainer worldlyContainer, Direction face) {
		ITrackedContentsItemHandler toHandler = storageWrapper.getInventoryForUpgradeProcessing();
		for (int containerSlot = 0; containerSlot < worldlyContainer.getContainerSize(); containerSlot++) {
			ItemStack existingStack = worldlyContainer.getItem(containerSlot);
			if (!existingStack.isEmpty() && worldlyContainer.canTakeItemThroughFace(containerSlot, existingStack, face) && inputFilterLogic.matchesFilter(existingStack)) {
				ItemVariant resource = ItemVariant.of(existingStack);
				long maxAmount = existingStack.getCount();
				try (Transaction nested = Transaction.openNested(null)) {
					maxAmount -= toHandler.insert(resource, maxAmount, nested);
					nested.commit();
				}

				if (maxAmount > 0) {
					worldlyContainer.setItem(containerSlot, resource.toStack((int) maxAmount));
					return true;
				}
			}
		}

		return false;
	}
	private boolean pullItemsFromContainer(WorldlyContainer worldlyContainer, Direction face) {
		ITrackedContentsItemHandler toHandler = storageWrapper.getInventoryForUpgradeProcessing();
		for (int containerSlot = 0; containerSlot < worldlyContainer.getContainerSize(); containerSlot++) {
			ItemStack existingStack = worldlyContainer.getItem(containerSlot);
			if (!existingStack.isEmpty() && worldlyContainer.canTakeItemThroughFace(containerSlot, existingStack, face) && inputFilterLogic.matchesFilter(existingStack)) {
				ItemStack remainingStack = InventoryHelper.insertIntoInventory(existingStack, toHandler, false);

				if (remainingStack.getCount() < existingStack.getCount()) {
					worldlyContainer.setItem(containerSlot, remainingStack);
					return true;
				}
			}
		}

		return false;
	}*/

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
