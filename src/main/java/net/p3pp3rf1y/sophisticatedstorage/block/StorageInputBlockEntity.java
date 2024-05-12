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

public class StorageInputBlockEntity extends StorageIOBlockEntity {
	public StorageInputBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.STORAGE_INPUT_BLOCK_ENTITY_TYPE, pos, state);
	}

	@Override
	protected <T> LazyOptional<T> getControllerCapability(BlockApiLookup<T, Direction> cap, @Nullable Direction opt, ControllerBlockEntity c) {
		if (cap == ItemStorage.SIDED) {
			return c.getCapability(ItemStorage.SIDED, null) //passing null side to not get the cache failed handler
					.map(itemHandler -> LazyOptional.of(() -> itemHandler instanceof IItemHandlerSimpleInserter simpleInserter ? new SingleSlotInputItemHandlerWrapper(simpleInserter) : itemHandler))
					.orElseGet(LazyOptional::empty).cast();
		}

		return super.getControllerCapability(cap, opt, c);
	}

	private static class SingleSlotInputItemHandlerWrapper implements SlottedStackStorage {
		private final IItemHandlerSimpleInserter itemHandler;

		public SingleSlotInputItemHandlerWrapper(IItemHandlerSimpleInserter itemHandler) {
			this.itemHandler = itemHandler;
		}

		@Override
		public int getSlotCount() {
			return 1;
		}

		@Override
		public SingleSlotStorage<ItemVariant> getSlot(int slot) {
			return null;
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
}
