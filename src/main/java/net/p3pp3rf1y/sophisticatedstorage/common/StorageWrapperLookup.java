package net.p3pp3rf1y.sophisticatedstorage.common;

import com.google.common.collect.MapMaker;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.StackStorageWrapper;

import java.util.Map;

public class StorageWrapperLookup {
	public static final ItemApiLookup<StackStorageWrapper, Void> STORAGE_WRAPPER_CAPABILITY = ItemApiLookup.get(SophisticatedStorage.getRL("storage_wrapper"), StackStorageWrapper.class, Void.class);

	public static StackStorageWrapper getOrCreate(ItemStack provider) {
		return STORAGE_WRAPPER_CAPABILITY.find(provider, null);
	}

	static {
		ItemApiLookup.ItemApiProvider<StackStorageWrapper, Void> provider = new ItemApiLookup.ItemApiProvider<>() {
			final Map<ItemStack, StackStorageWrapper> wrapperMap = new MapMaker().weakKeys().weakValues().makeMap();

			@Override
			public StackStorageWrapper find(ItemStack stack, Void context) {
				return wrapperMap.computeIfAbsent(stack, ignored -> new StackStorageWrapper());
			}
		};

		STORAGE_WRAPPER_CAPABILITY.registerForItems(provider, ModBlocks.ALL_WOODSTORAGE_ITEMS);
		STORAGE_WRAPPER_CAPABILITY.registerForItems(provider, ModBlocks.SHULKER_BOX_ITEMS);
	}
}
