package net.p3pp3rf1y.sophisticatedstorage.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.world.item.Item;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks.BASE_TIER_WOODEN_STORAGE_TAG;

public class ItemTagProvider extends FabricTagProvider.ItemTagProvider {
	public ItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider arg) {
		getOrCreateTagBuilder(BASE_TIER_WOODEN_STORAGE_TAG).add(ModBlocks.BARREL_ITEM, ModBlocks.CHEST_ITEM);

		FabricTagBuilder allStorageTag = getOrCreateTagBuilder(ModBlocks.ALL_STORAGE_TAG);
		BuiltInRegistries.ITEM.stream().filter(item -> item instanceof StorageBlockItem).forEach(allStorageTag::add);
	}
}
