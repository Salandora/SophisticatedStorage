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
				ModBlocks.ALL_BARREL_ITEMS);
	}
}
