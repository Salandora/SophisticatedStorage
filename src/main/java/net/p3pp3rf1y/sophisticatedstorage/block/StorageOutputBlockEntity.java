package net.p3pp3rf1y.sophisticatedstorage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;

import javax.annotation.Nullable;

public class StorageOutputBlockEntity extends StorageIOBlockEntity {
	@Nullable
	private Storage<ItemVariant> itemHandler;

	public StorageOutputBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.STORAGE_OUTPUT_BLOCK_ENTITY_TYPE, pos, state);
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
				//itemHandler = new OutputOnlyItemHandlerWrapper(simpleInserter);
				itemHandler = FilteringStorage.extractOnlyOf(simpleInserter);
			}
		}

		return itemHandler;
	}

	@Override
	protected void invalidateItemHandlerCache() {
		super.invalidateItemHandlerCache();
		itemHandler = null;
	}

	/*private static class OutputOnlyItemHandlerWrapper implements SlottedStackStorage {
		private final SlottedStackStorage itemHandler;

		public OutputOnlyItemHandlerWrapper(SlottedStackStorage itemHandler) {
			this.itemHandler = itemHandler;
		}

		@Override
		public int getSlotCount() {
			return itemHandler.getSlotCount();
		}

		@Override
		public SingleSlotStorage<ItemVariant> getSlot(int slot) {
			return new SingleSlotOutputSlotWrapper(itemHandler.getSlot(slot));
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
		}

		@Override
		public @NotNull ItemStack getStackInSlot(int slot) {
			return itemHandler.getStackInSlot(slot);
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext ctx) {
			return 0;
		}

		@Override
		public long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext ctx) {
			return 0;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext ctx) {
			return itemHandler.extract(resource, maxAmount, ctx);
		}

		@Override
		public long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext ctx) {
			return itemHandler.extractSlot(slot, resource, maxAmount, ctx);
		}

		@Override
		public int getSlotLimit(int slot) {
			return itemHandler.getSlotLimit(slot);
		}

		@Override
		public boolean isItemValid(int slot, ItemVariant resource, int count) {
			return false;
		}
	}

	private static class SingleSlotOutputSlotWrapper implements SingleSlotStorage<ItemVariant> {
		private final SingleSlotStorage<ItemVariant> backingSlot;
		public SingleSlotOutputSlotWrapper(SingleSlotStorage<ItemVariant> backingSlot) {
			this.backingSlot = backingSlot;
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			return 0;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			return backingSlot.extract(resource, maxAmount, transaction);
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
	}*/
}
