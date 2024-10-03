package net.p3pp3rf1y.sophisticatedstorage.common;

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.porting_lib.base.util.LazyOptional;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageIOBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.ShulkerBoxItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;


public class CapabilityStorageWrapper {
	public static final ItemApiLookup<LazyOptional<StorageWrapper>, Void> STORAGE_WRAPPER_CAPABILITY = ItemApiLookup.get(SophisticatedStorage.getRL("storage_wrapper"), (Class<LazyOptional<StorageWrapper>>) (Class<?>) LazyOptional.class, Void.class);

	public static LazyOptional<StorageWrapper> get(ItemStack provider) {
		LazyOptional<StorageWrapper> wrapper = STORAGE_WRAPPER_CAPABILITY.find(provider, null);
		if (wrapper != null) {
			return wrapper;
		}
		return LazyOptional.empty();
	}

	public static void register() {
		ItemStorage.SIDED.registerForBlockEntities((be, dir) -> ((StorageBlockEntity) be).getCapability(ItemStorage.SIDED, dir).getValueUnsafer(),
				ModBlocks.BARREL_BLOCK_ENTITY_TYPE, ModBlocks.LIMITED_BARREL_BLOCK_ENTITY_TYPE, ModBlocks.SHULKER_BOX_BLOCK_ENTITY_TYPE, ModBlocks.CHEST_BLOCK_ENTITY_TYPE);

		ItemStorage.SIDED.registerForBlockEntities((be, dir) -> ((StorageIOBlockEntity) be).getCapability(ItemStorage.SIDED, dir).getValueUnsafer(),
				ModBlocks.STORAGE_IO_BLOCK_ENTITY_TYPE, ModBlocks.STORAGE_INPUT_BLOCK_ENTITY_TYPE, ModBlocks.STORAGE_OUTPUT_BLOCK_ENTITY_TYPE);

		STORAGE_WRAPPER_CAPABILITY.registerForItems(WoodStorageBlockItem.initCapabilities(), ModBlocks.ALL_WOODSTORAGE_ITEMS);
		STORAGE_WRAPPER_CAPABILITY.registerForItems(ShulkerBoxItem.initCapabilities(), ModBlocks.SHULKER_BOX_ITEMS);
	}
}
