package net.p3pp3rf1y.sophisticatedstorage.block;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedcore.inventory.IItemHandlerSimpleInserter;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class StorageInputBlockEntity extends StorageIOBlockEntity {

	public StorageInputBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.STORAGE_INPUT_BLOCK_ENTITY_TYPE, pos, state);
	}

	@Nullable
	@Override
	protected <T> Direction getAdjustedCapabilitySide(BlockApiLookup<T, Direction> cap, @Nullable Direction side) {
		if (cap == ItemStorage.SIDED) {
			return null; //passing null side to not get the cache failed handler from controller
		}

		return super.getAdjustedCapabilitySide(cap, side);
	}

	@Override
	protected <T> T wrapCapability(BlockApiLookup<T, Direction> cap, T capability) {
		if (cap == ItemStorage.SIDED) {
			if (capability instanceof IItemHandlerSimpleInserter itemHandler) {
				return (T) new SingleSlotInputItemHandlerWrapper(itemHandler);
			}
		}

		return super.wrapCapability(cap, capability);
	}

	private static class SingleSlotInputItemHandlerWrapper implements SlottedStackStorage {
		private final SlottedStackStorage itemHandler;

		public SingleSlotInputItemHandlerWrapper(SlottedStackStorage itemHandler) {
			this.itemHandler = itemHandler;
		}

		@Override
		public int getSlotCount() {
			return Math.min(itemHandler.getSlotCount(), 1);
		}

		@Override
		public SingleSlotStorage<ItemVariant> getSlot(int slot) {
			return new SingleSlotInputSlotWrapper(itemHandler.getSlot(slot));
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			itemHandler.setStackInSlot(slot, stack);
		}

		@Override
		public @NotNull ItemStack getStackInSlot(int slot) {
			return ItemStack.EMPTY;
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext ctx) {
			return itemHandler.insert(resource, maxAmount, ctx);
		}

		@Override
		public long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext ctx) {
			return itemHandler.insertSlot(slot, resource, maxAmount, ctx);
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext ctx) {
			return 0;
		}

		@Override
		public long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext ctx) {
			return 0;
		}

		@Override
		public int getSlotLimit(int slot) {
			return 64;
		}

		@Override
		public boolean isItemValid(int slot, ItemVariant resource, int count) {
			return true;
		}
	}

	private static class SingleSlotInputSlotWrapper implements SingleSlotStorage<ItemVariant> {
		private final SingleSlotStorage<ItemVariant> backingSlot;
		public SingleSlotInputSlotWrapper(SingleSlotStorage<ItemVariant> backingSlot) {
			this.backingSlot = backingSlot;
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			return backingSlot.insert(resource, maxAmount, transaction);
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			return 0;
		}

		@Override
		public boolean isResourceBlank() {
			return backingSlot.isResourceBlank();
		}

		@Override
		public ItemVariant getResource() {
			return backingSlot.getResource();
		}

		@Override
		public long getAmount() {
			return backingSlot.getAmount();
		}

		@Override
		public long getCapacity() {
			return backingSlot.getSlotCount();
		}
	}
}
