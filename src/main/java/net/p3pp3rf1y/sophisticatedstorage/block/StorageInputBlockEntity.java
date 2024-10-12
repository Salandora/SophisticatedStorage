package net.p3pp3rf1y.sophisticatedstorage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StorageInputBlockEntity extends StorageIOBlockEntity {
	@Nullable
	private Storage<ItemVariant> itemHandler;

	public StorageInputBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.STORAGE_INPUT_BLOCK_ENTITY_TYPE, pos, state);
	}

	@Nullable
	@Override
	public Storage<ItemVariant> getExternalItemHandler(@Nullable Direction side) {
		if (getControllerPos().isEmpty()) {
			return null;
		}

		if (itemHandler == null) {
			itemHandler = super.getExternalItemHandler(side);
			if (itemHandler instanceof SlottedStackStorage simpleInserter) {
				itemHandler = new SingleSlotInputItemHandlerWrapper(simpleInserter);
			}
		}

		return itemHandler;
	}

	@Override
	protected void invalidateItemHandlerCache() {
		super.invalidateItemHandlerCache();
		itemHandler = null;
	}

	private static class SingleSlotInputItemHandlerWrapper implements SlottedStackStorage {
		private final SlottedStackStorage itemHandler;

		public SingleSlotInputItemHandlerWrapper(SlottedStackStorage itemHandler) {
			this.itemHandler = itemHandler;
		}

		@Override
		public int getSlotCount() {
			return 1;
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
