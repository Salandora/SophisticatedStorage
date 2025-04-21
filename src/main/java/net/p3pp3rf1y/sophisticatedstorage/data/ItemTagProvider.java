package net.p3pp3rf1y.sophisticatedstorage.data;

import net.minecraft.core.HolderLookup;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks.BASE_TIER_WOODEN_STORAGE_TAG;

public class ItemTagProvider extends FabricTagProvider.ItemTagProvider {
	public ItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		getOrCreateTagBuilder(BASE_TIER_WOODEN_STORAGE_TAG).add(ModBlocks.BARREL_ITEM.get(), ModBlocks.CHEST_ITEM.get());

		FabricTagBuilder allStorageTag = getOrCreateTagBuilder(ModBlocks.ALL_STORAGE_TAG);
		BuiltInRegistries.ITEM.stream().filter(item -> item instanceof StorageBlockItem).forEach(allStorageTag::add);

		FabricTagBuilder upgradeTag = getOrCreateTagBuilder(ModItems.STORAGE_UPGRADE_TAG);
		BuiltInRegistries.ITEM.entrySet().stream()
				.filter(entry -> entry.getKey().location().getNamespace().equals(SophisticatedStorage.MOD_ID) && entry.getValue() instanceof UpgradeItemBase)
				.map(Map.Entry::getValue).forEach(item -> {
					ResourceLocation location = BuiltInRegistries.ITEM.getKey(item);
					if (location.getPath().contains("/")) {
						upgradeTag.addOptional(location);
					} else {
						upgradeTag.add(item);
					}
				});
	}
}
