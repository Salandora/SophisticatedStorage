package net.p3pp3rf1y.sophisticatedstorage.item;

import com.github.salandora.sophisticatedfabriclib.transfer.api.v1.wrapper.fabric.FabricItemHandlerWrapper;
import com.github.salandora.sophisticatedfabriclib.util.Capabilities;
import com.github.salandora.sophisticatedfabriclib.util.LazyOptional;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageIOBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;


public class CapabilityStorageWrapper {
	@SuppressWarnings("unchecked")
	public static final ItemApiLookup<LazyOptional<StackStorageWrapper>, Void> STORAGE_WRAPPER_CAPABILITY = ItemApiLookup.get(SophisticatedStorage.getRL("storage_wrapper"), (Class<LazyOptional<StackStorageWrapper>>) (Class<?>) LazyOptional.class, Void.class);

	public static ItemApiLookup<LazyOptional<StackStorageWrapper>, Void> getCapabilityInstance() {
		return STORAGE_WRAPPER_CAPABILITY;
	}

	public static void register() {
		Capabilities.ItemHandler.SIDED.registerForBlockEntities((be, dir) -> ((StorageBlockEntity) be).getCapability(Capabilities.ItemHandler.SIDED, dir).orElse(null),
				ModBlocks.BARREL_BLOCK_ENTITY_TYPE, ModBlocks.LIMITED_BARREL_BLOCK_ENTITY_TYPE, ModBlocks.SHULKER_BOX_BLOCK_ENTITY_TYPE, ModBlocks.CHEST_BLOCK_ENTITY_TYPE);
		Capabilities.ItemHandler.SIDED.registerForBlockEntities((be, dir) -> ((StorageIOBlockEntity) be).getCapability(Capabilities.ItemHandler.SIDED, dir).orElse(null),
				ModBlocks.STORAGE_IO_BLOCK_ENTITY_TYPE, ModBlocks.STORAGE_INPUT_BLOCK_ENTITY_TYPE, ModBlocks.STORAGE_OUTPUT_BLOCK_ENTITY_TYPE);

		ItemStorage.SIDED.registerForBlockEntities((be, dir) -> FabricItemHandlerWrapper.of(((StorageBlockEntity) be).getCapability(Capabilities.ItemHandler.SIDED, dir).orElse(null)),
				ModBlocks.BARREL_BLOCK_ENTITY_TYPE, ModBlocks.LIMITED_BARREL_BLOCK_ENTITY_TYPE, ModBlocks.SHULKER_BOX_BLOCK_ENTITY_TYPE, ModBlocks.CHEST_BLOCK_ENTITY_TYPE);
		ItemStorage.SIDED.registerForBlockEntities((be, dir) -> FabricItemHandlerWrapper.of(((StorageIOBlockEntity) be).getCapability(Capabilities.ItemHandler.SIDED, dir).orElse(null)),
				ModBlocks.STORAGE_IO_BLOCK_ENTITY_TYPE, ModBlocks.STORAGE_INPUT_BLOCK_ENTITY_TYPE, ModBlocks.STORAGE_OUTPUT_BLOCK_ENTITY_TYPE);

		STORAGE_WRAPPER_CAPABILITY.registerForItems(WoodStorageBlockItem.initCapabilities(), ModBlocks.ALL_WOODSTORAGE_ITEMS);
		STORAGE_WRAPPER_CAPABILITY.registerForItems(ShulkerBoxItem.initCapabilities(), ModBlocks.SHULKER_BOX_ITEMS);
	}
}
