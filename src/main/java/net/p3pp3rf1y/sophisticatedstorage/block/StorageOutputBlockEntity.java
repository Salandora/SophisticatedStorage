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

public class StorageOutputBlockEntity extends StorageIOBlockEntity {
	public StorageOutputBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.STORAGE_OUTPUT_BLOCK_ENTITY_TYPE, pos, state);
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
				return (T) new OutputOnlyItemHandlerWrapper(itemHandler);
			}
		}

		return super.wrapCapability(cap, capability);
	}

	private static class OutputOnlyItemHandlerWrapper implements IItemHandler {
		private final IItemHandlerSimpleInserter itemHandler;

		public OutputOnlyItemHandlerWrapper(IItemHandlerSimpleInserter itemHandler) {
			this.itemHandler = itemHandler;
		}

		@Override
		public int getSlotCount() {
			return itemHandler.getSlotCount();
		}

		@Override
		public @NotNull ItemStack getStackInSlot(int slot) {
			return itemHandler.getStackInSlot(slot);
		}

		// Fabric: Added for internal use to reset the content when a Transaction was cancelled
		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			itemHandler.setStackInSlot(slot, stack);
		}

		@Override
		public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
			return stack;
		}

		@Override
		public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
			return itemHandler.extractItem(slot, amount, simulate);
		}

		@Override
		public int getSlotLimit(int slot) {
			return itemHandler.getSlotLimit(slot);
		}

		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack) {
			return false;
		}
	}
}
