package net.p3pp3rf1y.sophisticatedstorage.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.p3pp3rf1y.sophisticatedcore.api.Tags;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;

import java.util.concurrent.CompletableFuture;

public class BlockTagProvider extends FabricTagProvider.BlockTagProvider {

	public BlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider arg) {
		getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.ALL_WOODSTORAGES);
		getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_PICKAXE)
				.add(ModBlocks.SHULKER_BOXES)
				.add(ModBlocks.CONTROLLER, ModBlocks.STORAGE_LINK, ModBlocks.STORAGE_IO, ModBlocks.STORAGE_INPUT, ModBlocks.STORAGE_OUTPUT
		);
		getOrCreateTagBuilder(BlockTags.GUARDED_BY_PIGLINS).add(ModBlocks.ALL_STORAGECONTAINERS);

		getOrCreateTagBuilder(ConventionalBlockTags.CHESTS).add(ModBlocks.CHESTS);
		getOrCreateTagBuilder(Tags.Blocks.WOODEN_CHESTS).add(ModBlocks.CHESTS);
		getOrCreateTagBuilder(Tags.Blocks.BARRELS).add(ModBlocks.BARRELS);
		getOrCreateTagBuilder(ConventionalBlockTags.WOODEN_BARRELS).add(ModBlocks.BARRELS);
	}
}
