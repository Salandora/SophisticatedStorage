package net.p3pp3rf1y.sophisticatedstorage.client.init;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;

public class ModItemColors {
	private ModItemColors() {}

	public static void registerItemColorHandlers() {
		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
					if (tintIndex < 1000 || tintIndex > 1001) {
						return -1;
					}
					if (tintIndex == 1000) {
						return StorageBlockItem.getMainColorFromStack(stack).orElse(-1);
					} else {
						return StorageBlockItem.getAccentColorFromStack(stack).orElse(-1);
					}
				},
				ModBlocks.BARREL_ITEM, ModBlocks.COPPER_BARREL_ITEM, ModBlocks.IRON_BARREL_ITEM, ModBlocks.GOLD_BARREL_ITEM, ModBlocks.DIAMOND_BARREL_ITEM, ModBlocks.NETHERITE_BARREL_ITEM,
				ModBlocks.LIMITED_BARREL_1_ITEM, ModBlocks.LIMITED_COPPER_BARREL_1_ITEM, ModBlocks.LIMITED_IRON_BARREL_1_ITEM, ModBlocks.LIMITED_GOLD_BARREL_1_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM,
				ModBlocks.LIMITED_BARREL_2_ITEM, ModBlocks.LIMITED_COPPER_BARREL_2_ITEM, ModBlocks.LIMITED_IRON_BARREL_2_ITEM, ModBlocks.LIMITED_GOLD_BARREL_2_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_2_ITEM,
				ModBlocks.LIMITED_BARREL_3_ITEM, ModBlocks.LIMITED_COPPER_BARREL_3_ITEM, ModBlocks.LIMITED_IRON_BARREL_3_ITEM, ModBlocks.LIMITED_GOLD_BARREL_3_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_3_ITEM,
				ModBlocks.LIMITED_BARREL_4_ITEM, ModBlocks.LIMITED_COPPER_BARREL_4_ITEM, ModBlocks.LIMITED_IRON_BARREL_4_ITEM, ModBlocks.LIMITED_GOLD_BARREL_4_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_4_ITEM
		);
	}
}
