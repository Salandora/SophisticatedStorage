package net.p3pp3rf1y.sophisticatedstorage.block;

import com.github.salandora.sophisticatedfabriclib.transfer.api.v1.IItemHandler;
import com.github.salandora.sophisticatedfabriclib.util.Capabilities;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedcore.inventory.IItemHandlerSimpleInserter;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class StorageInputBlockEntity extends StorageIOBlockEntity {
	@Nullable
	private IItemHandler itemHandler;

	public StorageInputBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.STORAGE_INPUT_BLOCK_ENTITY_TYPE, pos, state);
	}

	@Nullable
	@Override
	protected <T> Direction getAdjustedCapabilitySide(BlockApiLookup<T, Direction> cap, @Nullable Direction side) {
		if (cap == Capabilities.ItemHandler.SIDED) {
			return null; //passing null side to not get the cache failed handler from controller
		}

		return super.getAdjustedCapabilitySide(cap, side);
	}

	@Override
	protected <T> T wrapCapability(BlockApiLookup<T, Direction> cap, T capability) {
		if (cap == Capabilities.ItemHandler.SIDED) {
			if (capability instanceof IItemHandlerSimpleInserter itemHandler) {
				return (T) new SingleSlotInputItemHandlerWrapper(itemHandler);
			}
		}

		return super.wrapCapability(cap, capability);
	}

	private static class SingleSlotInputItemHandlerWrapper implements IItemHandler {
		private final IItemHandlerSimpleInserter itemHandler;

		public SingleSlotInputItemHandlerWrapper(IItemHandlerSimpleInserter itemHandler) {
			this.itemHandler = itemHandler;
		}

		@Override
		public int getSlotCount() {
			return Math.min(itemHandler.getSlotCount(), 1);
		}

		@Override
		public @NotNull ItemStack getStackInSlot(int slot) {
			return ItemStack.EMPTY;
		}

		// Fabric: Added for internal use to reset the content when a Transaction was cancelled
		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			itemHandler.setStackInSlot(slot, stack);
		}

		@Override
		public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
			return itemHandler.insertItem(stack, simulate);
		}

		@Override
		public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot) {
			return 64;
		}

		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack) {
			return true;
		}
	}
}
