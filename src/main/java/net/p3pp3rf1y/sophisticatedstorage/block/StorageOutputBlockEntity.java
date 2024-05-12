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
import net.p3pp3rf1y.porting_lib.base.util.LazyOptional;
import net.p3pp3rf1y.sophisticatedcore.inventory.IItemHandlerSimpleInserter;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;

import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

public class StorageOutputBlockEntity extends StorageIOBlockEntity {
	public StorageOutputBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.STORAGE_OUTPUT_BLOCK_ENTITY_TYPE, pos, state);
	}

	@Override
	protected <T> LazyOptional<T> getControllerCapability(BlockApiLookup<T, Direction> cap, @Nullable Direction opt, ControllerBlockEntity c) {
		if (cap == ItemStorage.SIDED) {
				return c.getCapability(ItemStorage.SIDED, null) //passing null side to not get the cache failed handler
						.map(itemHandler -> LazyOptional.of(() -> itemHandler instanceof IItemHandlerSimpleInserter simpleInserter ? new OutputOnlyItemHandlerWrapper(simpleInserter) : itemHandler))
						.orElseGet(LazyOptional::empty).cast();
		}

		return super.getControllerCapability(cap, opt, c);
	}

	private static class OutputOnlyItemHandlerWrapper implements SlottedStackStorage {
		private final IItemHandlerSimpleInserter itemHandler;

		public OutputOnlyItemHandlerWrapper(IItemHandlerSimpleInserter itemHandler) {
			this.itemHandler = itemHandler;
		}

		@Override
		public int getSlotCount() {
			return itemHandler.getSlotCount();
		}

		@Override
		public SingleSlotStorage<ItemVariant> getSlot(int slot) {
			return itemHandler.getSlot(slot);
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
}
