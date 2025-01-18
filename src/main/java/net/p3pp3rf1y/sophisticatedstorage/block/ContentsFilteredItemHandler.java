package net.p3pp3rf1y.sophisticatedstorage.block;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.inventory.ISlotTracker;
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ContentsFilteredItemHandler implements ITrackedContentsItemHandler {

	private final Supplier<ITrackedContentsItemHandler> itemHandlerGetter;
	private final Supplier<ISlotTracker> slotTrackerGetter;
	private final Supplier<MemorySettingsCategory> memorySettingsGetter;

	public ContentsFilteredItemHandler(Supplier<ITrackedContentsItemHandler> itemHandlerGetter, Supplier<ISlotTracker> slotTrackerGetter, Supplier<MemorySettingsCategory> memorySettingsGetter) {
		this.itemHandlerGetter = itemHandlerGetter;
		this.slotTrackerGetter = slotTrackerGetter;
		this.memorySettingsGetter = memorySettingsGetter;
	}

	@Override
	public int getSlotCount() {
		return itemHandlerGetter.get().getSlotCount();
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return itemHandlerGetter.get().getStackInSlot(slot);
	}

	@Override
	public SingleSlotStorage<ItemVariant> getSlot(int slot) {
		return itemHandlerGetter.get().getSlot(slot);
	}
	@Override
	public long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext ctx) {
		if (matchesContents(resource.toStack((int) maxAmount))) {
			return itemHandlerGetter.get().insertSlot(slot, resource, maxAmount, ctx);
		}
		return 0;
	}

	@Override
	public long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext ctx) {
		return itemHandlerGetter.get().extractSlot(slot, resource, maxAmount, ctx);
	}

	@Override
	public int getSlotLimit(int slot) {
		return itemHandlerGetter.get().getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, ItemVariant resource, int count) {
		return matchesContents(resource.toStack(count)) && itemHandlerGetter.get().isItemValid(slot, resource, count);
	}

	private boolean matchesContents(ItemStack stack) {
		return slotTrackerGetter.get().getItems().contains(stack.getItem()) || memorySettingsGetter.get().matchesFilter(stack);
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext ctx) {
		if (matchesContents(resource.toStack((int) maxAmount))) {
			return itemHandlerGetter.get().insert(resource, maxAmount, ctx);
		}
		return 0;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext ctx) {
		return itemHandlerGetter.get().extract(resource, maxAmount, ctx);
	}

	@Override
	public Set<ItemStackKey> getTrackedStacks() {
		return itemHandlerGetter.get().getTrackedStacks();
	}

	@Override
	public void registerTrackingListeners(Consumer<ItemStackKey> onAddStackKey, Consumer<ItemStackKey> onRemoveStackKey, Runnable onAddFirstEmptySlot, Runnable onRemoveLastEmptySlot) {
		itemHandlerGetter.get().registerTrackingListeners(onAddStackKey, onRemoveStackKey, onAddFirstEmptySlot, onRemoveLastEmptySlot);
	}

	@Override
	public void unregisterStackKeyListeners() {
		itemHandlerGetter.get().unregisterStackKeyListeners();
	}

	@Override
	public boolean hasEmptySlots() {
		return itemHandlerGetter.get().hasEmptySlots();
	}

	@Override
	public int getInternalSlotLimit(int slot) {
		return itemHandlerGetter.get().getInternalSlotLimit(slot);
	}

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		itemHandlerGetter.get().setStackInSlot(slot, stack);
	}
}