package net.p3pp3rf1y.sophisticatedstorage.common;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.common.CapabilityWrapper;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.ShulkerBoxItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import static net.p3pp3rf1y.sophisticatedcore.common.CapabilityWrapper.STORAGE_WRAPPER_CAPABILITY;

public class CapabilityStorageWrapper {
	private static final Map<ItemStack, StorageWrapper> cachedStorageWrappers = new WeakHashMap<>();

	public static Optional<StorageWrapper> get(ItemStack provider) {
		return CapabilityWrapper.get(provider, StorageWrapper.class);
	}

	private CapabilityStorageWrapper() {}

	public static void register() {
		ItemStorage.SIDED.registerForBlockEntities((be, dir) -> ((StorageBlockEntity) be).getCapability(ItemStorage.SIDED, dir), ModBlocks.BARREL_BLOCK_ENTITY_TYPE, ModBlocks.LIMITED_BARREL_BLOCK_ENTITY_TYPE, ModBlocks.SHULKER_BOX_BLOCK_ENTITY_TYPE, ModBlocks.CHEST_BLOCK_ENTITY_TYPE);

		STORAGE_WRAPPER_CAPABILITY.registerForItems((stack, c) -> {
					if (stack.getCount() == 1) {
						return cachedStorageWrappers.computeIfAbsent(stack, WoodStorageBlockItem::initWrapper);
					}
					return null;
				},
				ModBlocks.BARREL_ITEM, ModBlocks.IRON_BARREL_ITEM, ModBlocks.GOLD_BARREL_ITEM, ModBlocks.DIAMOND_BARREL_ITEM, ModBlocks.NETHERITE_BARREL_ITEM,
				ModBlocks.LIMITED_BARREL_1_ITEM, ModBlocks.LIMITED_IRON_BARREL_1_ITEM, ModBlocks.LIMITED_GOLD_BARREL_1_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM,
				ModBlocks.LIMITED_BARREL_2_ITEM, ModBlocks.LIMITED_IRON_BARREL_2_ITEM, ModBlocks.LIMITED_GOLD_BARREL_2_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_2_ITEM,
				ModBlocks.LIMITED_BARREL_3_ITEM, ModBlocks.LIMITED_IRON_BARREL_3_ITEM, ModBlocks.LIMITED_GOLD_BARREL_3_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_3_ITEM,
				ModBlocks.LIMITED_BARREL_4_ITEM, ModBlocks.LIMITED_IRON_BARREL_4_ITEM, ModBlocks.LIMITED_GOLD_BARREL_4_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_4_ITEM,
				ModBlocks.CHEST_ITEM, ModBlocks.IRON_CHEST_ITEM, ModBlocks.GOLD_CHEST_ITEM, ModBlocks.DIAMOND_CHEST_ITEM, ModBlocks.NETHERITE_CHEST_ITEM
		);

		STORAGE_WRAPPER_CAPABILITY.registerForItems((stack, c) -> {
					if (stack.getCount() == 1) {
						return cachedStorageWrappers.computeIfAbsent(stack, ShulkerBoxItem::initWrapper);
					}
					return null;
				},
				ModBlocks.SHULKER_BOX_ITEM, ModBlocks.IRON_SHULKER_BOX_ITEM, ModBlocks.GOLD_SHULKER_BOX_ITEM, ModBlocks.DIAMOND_SHULKER_BOX_ITEM, ModBlocks.NETHERITE_SHULKER_BOX_ITEM);
	}
}
