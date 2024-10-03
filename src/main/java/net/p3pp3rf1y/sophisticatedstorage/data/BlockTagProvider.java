package net.p3pp3rf1y.sophisticatedstorage.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.tags.BlockTags;
import net.p3pp3rf1y.sophisticatedcore.api.Tags;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;

public class BlockTagProvider extends FabricTagProvider.BlockTagProvider {

	public BlockTagProvider(FabricDataGenerator output) {
		super(output);
	}

	@Override
	protected void generateTags() {
		getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.ALL_WOODSTORAGES);
		getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_PICKAXE)
				.add(ModBlocks.SHULKER_BOXES)
				.add(ModBlocks.CONTROLLER, ModBlocks.STORAGE_LINK
		);
		getOrCreateTagBuilder(BlockTags.GUARDED_BY_PIGLINS).add(ModBlocks.ALL_STORAGECONTAINERS);

		getOrCreateTagBuilder(ConventionalBlockTags.CHESTS).add(ModBlocks.CHESTS);
		getOrCreateTagBuilder(Tags.Blocks.WOODEN_CHESTS).add(ModBlocks.CHESTS);
		getOrCreateTagBuilder(Tags.Blocks.BARRELS).add(ModBlocks.BARRELS);
		getOrCreateTagBuilder(Tags.Blocks.WOODEN_BARRELS).add(ModBlocks.BARRELS);
	}
}
