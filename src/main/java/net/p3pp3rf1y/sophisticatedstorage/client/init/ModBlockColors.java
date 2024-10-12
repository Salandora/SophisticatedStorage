package net.p3pp3rf1y.sophisticatedstorage.client.init;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderInfo;
import net.p3pp3rf1y.sophisticatedcore.util.WorldHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.LimitedBarrelBlock;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.mixin.client.accessor.MinecraftAccessor;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.List;

public class ModBlockColors {
	private ModBlockColors() {}

	public static void registerBlockColorHandlers() {
		ColorProviderRegistry.BLOCK.register(ModBlockColors::getBarrelTintColor, ModBlocks.ALL_BARRELS);
		ColorProviderRegistry.BLOCK.register(ModBlockColors::getChestShulkerBoxColor, ArrayUtils.addAll(ModBlocks.CHESTS, ModBlocks.SHULKER_BOXES));
	}

	private static int getBarrelTintColor(BlockState state, @Nullable BlockAndTintGetter blockDisplayReader, @Nullable BlockPos pos, int tintIndex) {
		if (tintIndex < 0 || pos == null) {
			return -1;
		}
		return WorldHelper.getBlockEntity(blockDisplayReader, pos, StorageBlockEntity.class)
				.map(be -> {
					if (tintIndex == 1000) {
						return be.getStorageWrapper().getMainColor();
					} else if (tintIndex == 1001) {
						return be.getStorageWrapper().getAccentColor();
					} else {
						RenderInfo.ItemDisplayRenderInfo itemDisplayRenderInfo = be.getStorageWrapper().getRenderInfo().getItemDisplayRenderInfo();
						int displayItemIndex = (tintIndex > 1000 ? tintIndex - 1000 : tintIndex) / 10 - 1;
						List<RenderInfo.DisplayItem> displayItems = itemDisplayRenderInfo.getDisplayItems();
						if (displayItemIndex >= 0) {
							int tintOffset = (displayItemIndex + 1) * 10;
							ItemStack stack = getDisplayItemWithIndex(displayItemIndex, displayItems, state.getBlock() instanceof LimitedBarrelBlock);
							if (stack.isEmpty()) {
								return -1;
							}
							return ((MinecraftAccessor) Minecraft.getInstance()).getItemColors().getColor(stack, tintIndex - tintOffset);
						}
					}
					return -1;
				})
				.orElse(-1);
	}

	private static ItemStack getDisplayItemWithIndex(int displayItemIndex, List<RenderInfo.DisplayItem> displayItems, boolean isLimitedBarrel) {
		if (isLimitedBarrel) {
			for (RenderInfo.DisplayItem displayItem : displayItems) {
				if (displayItem.getSlotIndex() == displayItemIndex) {
					return displayItem.getItem();
				}
			}
		}
		return displayItems.size() > displayItemIndex ? displayItems.get(displayItemIndex).getItem() : ItemStack.EMPTY;
	}

	private static int getChestShulkerBoxColor(BlockState state, @Nullable BlockAndTintGetter blockDisplayReader, @Nullable BlockPos pos, int tintIndex) {
		if (tintIndex < 0 || pos == null) {
			return -1;
		}
		return WorldHelper.getBlockEntity(blockDisplayReader, pos, StorageBlockEntity.class)
				.map(be -> {
					if (tintIndex == 0) { //this is only needed for particle texture handling so no need to handle anything other than just the main color
						return be.getStorageWrapper().getMainColor();
					}
					return -1;
				})
				.orElse(-1);
	}
}
