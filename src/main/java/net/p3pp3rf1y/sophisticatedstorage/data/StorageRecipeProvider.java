package net.p3pp3rf1y.sophisticatedstorage.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.DefaultResourceConditions;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedcore.api.Tags;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatModIds;
import net.p3pp3rf1y.sophisticatedcore.compat.chipped.BlockTransformationUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.crafting.ShapeBasedRecipeBuilder;
import net.p3pp3rf1y.sophisticatedcore.crafting.ShapelessBasedRecipeBuilder;
import net.p3pp3rf1y.sophisticatedcore.init.ModRecipes;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.util.RegistryHelper;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.WoodStorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.compat.chipped.ChippedCompat;
import net.p3pp3rf1y.sophisticatedstorage.crafting.BaseTierWoodenStorageIngredient;
import net.p3pp3rf1y.sophisticatedstorage.crafting.DropPackedDisabledCondition;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;

import java.util.function.Consumer;

public class StorageRecipeProvider extends FabricRecipeProvider {
	private static final String HAS_UPGRADE_BASE_CRITERION_NAME = "has_upgrade_base";
	private static final String HAS_REDSTONE_TORCH_CRITERION_NAME = "has_redstone_torch";
	private static final String HAS_SMELTING_UPGRADE_CRITERION_NAME = "has_smelting_upgrade";
	public static final String HAS_BASE_TIER_WOODEN_STORAGE_CRITERION_NAME = "has_base_tier_wooden_storage";
	private static final String PLANK_SUFFIX = "_plank";

	public StorageRecipeProvider(FabricDataOutput output) {
		super(output);
	}

	@Override
	public void buildRecipes(Consumer<FinishedRecipe> consumer) {
		SpecialRecipeBuilder.special(ModBlocks.STORAGE_DYE_RECIPE_SERIALIZER).save(consumer, SophisticatedStorage.getRegistryName("storage_dye"));
		SpecialRecipeBuilder.special(ModBlocks.FLAT_TOP_BARREL_TOGGLE_RECIPE_SERIALIZER).save(consumer, SophisticatedStorage.getRegistryName("flat_top_barrel_toggle"));
		SpecialRecipeBuilder.special(ModBlocks.BARREL_MATERIAL_RECIPE_SERIALIZER).save(consumer, SophisticatedStorage.getRegistryName("barrel_material"));

		addBarrelRecipes(consumer);
		addLimitedBarrelRecipes(consumer);
		addChestRecipes(consumer);
		addShulkerBoxRecipes(consumer);
		addControllerRelatedRecipes(consumer);
		addUpgradeRecipes(consumer);
		addBackpackUpgradeConversionRecipes(consumer);
		addTierUpgradeItemRecipes(consumer);

		ShapelessBasedRecipeBuilder.shapeless(ModItems.PACKING_TAPE)
				.requires(Items.SLIME_BALL)
				.requires(Items.PAPER)
				.unlockedBy("has_slime", has(Items.SLIME_BALL))
				.condition(new DropPackedDisabledCondition())
				.save(consumer);
	}

	private void addBackpackUpgradeConversionRecipes(Consumer<FinishedRecipe> consumer) {
		addStorageStackUpgradeFromBackpackStackUpgradeRecipe(consumer, ModItems.STACK_UPGRADE_TIER_1_PLUS, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.STACK_UPGRADE_STARTER_TIER);
		addStorageStackUpgradeFromBackpackStackUpgradeRecipe(consumer, ModItems.STACK_UPGRADE_TIER_2, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.STACK_UPGRADE_TIER_1);
		addStorageStackUpgradeFromBackpackStackUpgradeRecipe(consumer, ModItems.STACK_UPGRADE_TIER_3, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.STACK_UPGRADE_TIER_2);
		addStorageStackUpgradeFromBackpackStackUpgradeRecipe(consumer, ModItems.STACK_UPGRADE_TIER_4, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.STACK_UPGRADE_TIER_3);
		addStorageStackUpgradeFromBackpackStackUpgradeRecipe(consumer, ModItems.STACK_UPGRADE_TIER_5, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.STACK_UPGRADE_TIER_4);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.PICKUP_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.PICKUP_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.ADVANCED_PICKUP_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_PICKUP_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.MAGNET_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.MAGNET_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.ADVANCED_MAGNET_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_MAGNET_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.FILTER_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.FILTER_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.ADVANCED_FILTER_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_FILTER_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.CRAFTING_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.CRAFTING_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.FEEDING_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.FEEDING_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.ADVANCED_FEEDING_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_FEEDING_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.COMPACTING_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.COMPACTING_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.ADVANCED_COMPACTING_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_COMPACTING_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.VOID_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.VOID_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.ADVANCED_VOID_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_VOID_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.SMELTING_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.SMELTING_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.AUTO_SMELTING_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.AUTO_SMELTING_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.SMOKING_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.SMOKING_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.AUTO_SMOKING_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.AUTO_SMOKING_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.BLASTING_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.BLASTING_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.AUTO_BLASTING_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.AUTO_BLASTING_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.STONECUTTER_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.STONECUTTER_UPGRADE);
		addStorageUpgradeFromBackpackUpgradeRecipe(consumer, ModItems.JUKEBOX_UPGRADE, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.JUKEBOX_UPGRADE);

		addBackpackStackUpgradeFromStorageStackUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.STACK_UPGRADE_STARTER_TIER, ModItems.STACK_UPGRADE_TIER_1_PLUS);
		addBackpackStackUpgradeFromStorageStackUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.STACK_UPGRADE_TIER_1, ModItems.STACK_UPGRADE_TIER_2);
		addBackpackStackUpgradeFromStorageStackUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.STACK_UPGRADE_TIER_2, ModItems.STACK_UPGRADE_TIER_3);
		addBackpackStackUpgradeFromStorageStackUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.STACK_UPGRADE_TIER_3, ModItems.STACK_UPGRADE_TIER_4);
		addBackpackStackUpgradeFromStorageStackUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.STACK_UPGRADE_TIER_4, ModItems.STACK_UPGRADE_TIER_5);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.PICKUP_UPGRADE, ModItems.PICKUP_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_PICKUP_UPGRADE, ModItems.ADVANCED_PICKUP_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.MAGNET_UPGRADE, ModItems.MAGNET_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_MAGNET_UPGRADE, ModItems.ADVANCED_MAGNET_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.FILTER_UPGRADE, ModItems.FILTER_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_FILTER_UPGRADE, ModItems.ADVANCED_FILTER_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.CRAFTING_UPGRADE, ModItems.CRAFTING_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.FEEDING_UPGRADE, ModItems.FEEDING_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_FEEDING_UPGRADE, ModItems.ADVANCED_FEEDING_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.COMPACTING_UPGRADE, ModItems.COMPACTING_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_COMPACTING_UPGRADE, ModItems.ADVANCED_COMPACTING_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.VOID_UPGRADE, ModItems.VOID_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_VOID_UPGRADE, ModItems.ADVANCED_VOID_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.SMELTING_UPGRADE, ModItems.SMELTING_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.AUTO_SMELTING_UPGRADE, ModItems.AUTO_SMELTING_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.SMOKING_UPGRADE, ModItems.SMOKING_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.AUTO_SMOKING_UPGRADE, ModItems.AUTO_SMOKING_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.BLASTING_UPGRADE, ModItems.BLASTING_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.AUTO_BLASTING_UPGRADE, ModItems.AUTO_BLASTING_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.STONECUTTER_UPGRADE, ModItems.STONECUTTER_UPGRADE);
		addBackpackUpgradeFromStorageUpgradeRecipe(consumer, net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.JUKEBOX_UPGRADE, ModItems.JUKEBOX_UPGRADE);

	}

	private void addBackpackStackUpgradeFromStorageStackUpgradeRecipe(Consumer<FinishedRecipe> consumer, StackUpgradeItem backpackStackUpgrade, StackUpgradeItem storageStackUpgrade) {
		ShapeBasedRecipeBuilder.shaped(backpackStackUpgrade)
				.pattern("TST")
				.pattern("SLS")
				.pattern("T T")
				.define('T', Items.STRING)
				.define('L', Items.LEATHER)
				.define('S', storageStackUpgrade)
				.unlockedBy("has_storage_stack_upgrade", has(storageStackUpgrade))
				.condition(DefaultResourceConditions.allModsLoaded(SophisticatedBackpacks.MOD_ID))
				.save(consumer, SophisticatedStorage.getRL("backpack_" + RegistryHelper.getItemKey(backpackStackUpgrade).getPath() + "_from_storage_" + RegistryHelper.getItemKey(storageStackUpgrade).getPath()));
	}

	private void addBackpackUpgradeFromStorageUpgradeRecipe(Consumer<FinishedRecipe> consumer, UpgradeItemBase<?> backpackUpgrade, UpgradeItemBase<?> storageUpgrade) {
		ShapeBasedRecipeBuilder.shaped(backpackUpgrade)
				.pattern("TUT")
				.pattern(" L ")
				.pattern("T T")
				.define('T', Items.STRING)
				.define('L', Items.LEATHER)
				.define('U', storageUpgrade)
				.unlockedBy("has_storage_upgrade", has(storageUpgrade))
				.condition(DefaultResourceConditions.allModsLoaded(SophisticatedBackpacks.MOD_ID))
				.save(consumer, SophisticatedStorage.getRL("backpack_" + RegistryHelper.getItemKey(backpackUpgrade).getPath() + "_from_storage_" + RegistryHelper.getItemKey(storageUpgrade).getPath()));
	}

	private void addStorageUpgradeFromBackpackUpgradeRecipe(Consumer<FinishedRecipe> consumer, UpgradeItemBase<?> storageUpgrade, UpgradeItemBase<?> backpackUpgrade) {
		ShapeBasedRecipeBuilder.shaped(storageUpgrade)
				.pattern("PUP")
				.pattern(" P ")
				.pattern("P P")
				.define('P', ItemTags.PLANKS)
				.define('U', backpackUpgrade)
				.unlockedBy("has_backpack_upgrade", has(backpackUpgrade))
				.condition(DefaultResourceConditions.allModsLoaded(SophisticatedBackpacks.MOD_ID))
				.save(consumer, SophisticatedStorage.getRL("storage_" + RegistryHelper.getItemKey(storageUpgrade).getPath() + "_from_backpack_" + RegistryHelper.getItemKey(backpackUpgrade).getPath()));
	}

	private static void addStorageStackUpgradeFromBackpackStackUpgradeRecipe(Consumer<FinishedRecipe> consumer, StackUpgradeItem storageStackUpgrade, StackUpgradeItem backpackStackUpgrade) {
		ShapeBasedRecipeBuilder.shaped(storageStackUpgrade, 3)
				.pattern("PSP")
				.pattern(" P ")
				.pattern("P P")
				.define('P', ItemTags.PLANKS)
				.define('S', backpackStackUpgrade)
				.unlockedBy("has_backpack_stack_upgrade", has(backpackStackUpgrade))
				.condition(DefaultResourceConditions.allModsLoaded(SophisticatedBackpacks.MOD_ID))
				.save(consumer, SophisticatedStorage.getRL("storage_" + RegistryHelper.getItemKey(storageStackUpgrade).getPath() + "_from_backpack_" + RegistryHelper.getItemKey(backpackStackUpgrade).getPath()));
	}

	private void addLimitedBarrelRecipes(Consumer<FinishedRecipe> consumer) {
		WoodStorageBlockBase.CUSTOM_TEXTURE_WOOD_TYPES.forEach((woodType, blockFamily) -> {
			limitedWoodBarrel1Recipe(consumer, woodType, blockFamily.getBaseBlock(), blockFamily.get(BlockFamily.Variant.SLAB));
			limitedWoodBarrel2Recipe(consumer, woodType, blockFamily.getBaseBlock(), blockFamily.get(BlockFamily.Variant.SLAB));
			limitedWoodBarrel3Recipe(consumer, woodType, blockFamily.getBaseBlock(), blockFamily.get(BlockFamily.Variant.SLAB));
			limitedWoodBarrel4Recipe(consumer, woodType, blockFamily.getBaseBlock(), blockFamily.get(BlockFamily.Variant.SLAB));
		});

		addStorageTierUpgradeRecipes(consumer, ModBlocks.LIMITED_BARREL_1_ITEM, ModBlocks.LIMITED_COPPER_BARREL_1_ITEM, ModBlocks.LIMITED_IRON_BARREL_1_ITEM, ModBlocks.LIMITED_GOLD_BARREL_1_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM);
		addStorageTierUpgradeRecipes(consumer, ModBlocks.LIMITED_BARREL_2_ITEM, ModBlocks.LIMITED_COPPER_BARREL_2_ITEM, ModBlocks.LIMITED_IRON_BARREL_2_ITEM, ModBlocks.LIMITED_GOLD_BARREL_2_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_2_ITEM);
		addStorageTierUpgradeRecipes(consumer, ModBlocks.LIMITED_BARREL_3_ITEM, ModBlocks.LIMITED_COPPER_BARREL_3_ITEM, ModBlocks.LIMITED_IRON_BARREL_3_ITEM, ModBlocks.LIMITED_GOLD_BARREL_3_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_3_ITEM);
		addStorageTierUpgradeRecipes(consumer, ModBlocks.LIMITED_BARREL_4_ITEM, ModBlocks.LIMITED_COPPER_BARREL_4_ITEM, ModBlocks.LIMITED_IRON_BARREL_4_ITEM, ModBlocks.LIMITED_GOLD_BARREL_4_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_4_ITEM);
	}

	private void addStorageTierUpgradeRecipes(Consumer<FinishedRecipe> consumer, BlockItem baseTierItem, BlockItem copperTierItem, BlockItem ironTierItem, BlockItem goldTierItem, BlockItem diamondTierItem, BlockItem netheriteTierItem) {
		ShapeBasedRecipeBuilder.shaped(copperTierItem, ModBlocks.STORAGE_TIER_UPGRADE_RECIPE_SERIALIZER)
				.pattern("CCC")
				.pattern("CSC")
				.pattern("CCC")
				.define('C', ConventionalItemTags.COPPER_INGOTS)
				.define('S', baseTierItem)
				.unlockedBy("has_" + RegistryHelper.getItemKey(baseTierItem).getPath(), has(baseTierItem))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ironTierItem, ModBlocks.STORAGE_TIER_UPGRADE_RECIPE_SERIALIZER)
				.pattern(" I ")
				.pattern("ISI")
				.pattern(" I ")
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.define('S', copperTierItem)
				.unlockedBy("has_" + RegistryHelper.getItemKey(copperTierItem).getPath(), has(copperTierItem))
				.save(consumer, SophisticatedStorage.getRL(RegistryHelper.getItemKey(ironTierItem).getPath() + "_from_" + RegistryHelper.getItemKey(copperTierItem).getPath()));

		ShapeBasedRecipeBuilder.shaped(ironTierItem, ModBlocks.STORAGE_TIER_UPGRADE_RECIPE_SERIALIZER)
				.pattern("III")
				.pattern("ISI")
				.pattern("III")
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.define('S', baseTierItem)
				.unlockedBy("has_" + RegistryHelper.getItemKey(baseTierItem).getPath(), has(baseTierItem))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(goldTierItem, ModBlocks.STORAGE_TIER_UPGRADE_RECIPE_SERIALIZER)
				.pattern("GGG")
				.pattern("GSG")
				.pattern("GGG")
				.define('G', ConventionalItemTags.GOLD_INGOTS)
				.define('S', ironTierItem)
				.unlockedBy("has_" + RegistryHelper.getItemKey(ironTierItem).getPath(), has(ironTierItem))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(diamondTierItem, ModBlocks.STORAGE_TIER_UPGRADE_RECIPE_SERIALIZER)
				.pattern("DDD")
				.pattern("DSD")
				.pattern("DDD")
				.define('D', ConventionalItemTags.DIAMONDS)
				.define('S', goldTierItem)
				.unlockedBy("has_" + RegistryHelper.getItemKey(goldTierItem).getPath(), has(goldTierItem))
				.save(consumer);

		ShapelessBasedRecipeBuilder.shapeless(netheriteTierItem, ModBlocks.STORAGE_TIER_UPGRADE_SHAPELESS_RECIPE_SERIALIZER)
				.requires(Ingredient.of(diamondTierItem))
				.requires(ConventionalItemTags.NETHERITE_INGOTS)
				.unlockedBy("has_" + RegistryHelper.getItemKey(diamondTierItem).getPath(), has(diamondTierItem))
				.save(consumer, RegistryHelper.getItemKey(netheriteTierItem));
	}

	private void addControllerRelatedRecipes(Consumer<FinishedRecipe> consumer) {
		ShapeBasedRecipeBuilder.shaped(ModBlocks.CONTROLLER_ITEM)
				.pattern("SCS")
				.pattern("PBP")
				.pattern("SCS")
				.define('S', Tags.Items.STONES)
				.define('C', Items.COMPARATOR)
				.define('P', ItemTags.PLANKS)
				.define('B', BaseTierWoodenStorageIngredient.INSTANCE.toVanilla())
				.unlockedBy(HAS_BASE_TIER_WOODEN_STORAGE_CRITERION_NAME, has(ModBlocks.BASE_TIER_WOODEN_STORAGE_TAG))
				.save(consumer);

		ShapelessBasedRecipeBuilder.shapeless(ModBlocks.STORAGE_LINK_ITEM, 3)
				.requires(ModBlocks.CONTROLLER_ITEM)
				.requires(Items.ENDER_PEARL)
				.unlockedBy("has_controller", has(ModBlocks.CONTROLLER_ITEM))
				.save(consumer, SophisticatedStorage.getRL("storage_link_from_controller"));

		ShapeBasedRecipeBuilder.shaped(ModBlocks.STORAGE_LINK_ITEM)
				.pattern("EP")
				.pattern("RS")
				.define('E', Items.ENDER_PEARL)
				.define('P', ItemTags.PLANKS)
				.define('R', Items.REPEATER)
				.define('S', Tags.Items.STONES)
				.unlockedBy("has_repeater", has(Items.REPEATER))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.STORAGE_TOOL)
				.pattern(" EI")
				.pattern(" SR")
				.pattern("S  ")
				.define('E', Items.ENDER_PEARL)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.define('S', Items.STICK)
				.define('R', Items.REDSTONE_TORCH)
				.unlockedBy(HAS_REDSTONE_TORCH_CRITERION_NAME, has(Items.REDSTONE_TORCH))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModBlocks.STORAGE_IO_ITEM)
				.pattern("SPS")
				.pattern("RBG")
				.pattern("SPS")
				.define('S', Tags.Items.STONES)
				.define('P', ItemTags.PLANKS)
				.define('R', Items.REPEATER)
				.define('G', ConventionalItemTags.GOLD_INGOTS)
				.define('B',  BaseTierWoodenStorageIngredient.INSTANCE.toVanilla())
				.unlockedBy(HAS_BASE_TIER_WOODEN_STORAGE_CRITERION_NAME, has(ModBlocks.BASE_TIER_WOODEN_STORAGE_TAG))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModBlocks.STORAGE_OUTPUT_ITEM)
				.pattern("SGS")
				.pattern("PBP")
				.pattern("SRS")
				.define('S', Tags.Items.STONES)
				.define('P', ItemTags.PLANKS)
				.define('R', Items.REPEATER)
				.define('G', ConventionalItemTags.GOLD_INGOTS)
				.define('B',  BaseTierWoodenStorageIngredient.INSTANCE.toVanilla())
				.unlockedBy(HAS_BASE_TIER_WOODEN_STORAGE_CRITERION_NAME, has(ModBlocks.BASE_TIER_WOODEN_STORAGE_TAG))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModBlocks.STORAGE_INPUT_ITEM)
				.pattern("SRS")
				.pattern("PBP")
				.pattern("SGS")
				.define('S', Tags.Items.STONES)
				.define('P', ItemTags.PLANKS)
				.define('R', Items.REPEATER)
				.define('G', ConventionalItemTags.GOLD_INGOTS)
				.define('B',  BaseTierWoodenStorageIngredient.INSTANCE.toVanilla())
				.unlockedBy(HAS_BASE_TIER_WOODEN_STORAGE_CRITERION_NAME, has(ModBlocks.BASE_TIER_WOODEN_STORAGE_TAG))
				.save(consumer);

		ShapelessBasedRecipeBuilder.shapeless(ModBlocks.STORAGE_INPUT_ITEM)
				.requires(ModBlocks.STORAGE_IO_ITEM)
				.unlockedBy("has_storage_io", has(ModBlocks.STORAGE_IO_ITEM))
				.save(consumer, "storage_input_from_io");

		ShapelessBasedRecipeBuilder.shapeless(ModBlocks.STORAGE_OUTPUT_ITEM)
				.requires(ModBlocks.STORAGE_INPUT_ITEM)
				.unlockedBy("has_storage_input", has(ModBlocks.STORAGE_INPUT_ITEM))
				.save(consumer, "storage_output_from_input");

		ShapelessBasedRecipeBuilder.shapeless(ModBlocks.STORAGE_IO_ITEM)
				.requires(ModBlocks.STORAGE_OUTPUT_ITEM)
				.unlockedBy("has_storage_output", has(ModBlocks.STORAGE_OUTPUT_ITEM))
				.save(consumer, "storage_io_from_output");
	}

	private void addShulkerBoxRecipes(Consumer<FinishedRecipe> consumer) {
		ShapeBasedRecipeBuilder.shaped(ModBlocks.SHULKER_BOX_ITEM)
				.pattern(" S")
				.pattern("RC")
				.pattern(" S")
				.define('R', Items.REDSTONE_TORCH)
				.define('S', Items.SHULKER_SHELL)
				.define('C', ConventionalItemTags.CHESTS)
				.unlockedBy("has_shulker_shell", has(Items.SHULKER_SHELL))
				.save(consumer);

		ShapelessBasedRecipeBuilder.shapeless(ModBlocks.SHULKER_BOX_ITEM)
				.requires(Items.SHULKER_BOX).requires(Items.REDSTONE_TORCH)
				.save(consumer, "shulker_box_from_vanilla_shulker_box");

		tintedShulkerBoxRecipe(consumer, Blocks.BLACK_SHULKER_BOX, DyeColor.BLACK);
		tintedShulkerBoxRecipe(consumer, Blocks.BLUE_SHULKER_BOX, DyeColor.BLUE);
		tintedShulkerBoxRecipe(consumer, Blocks.BROWN_SHULKER_BOX, DyeColor.BROWN);
		tintedShulkerBoxRecipe(consumer, Blocks.CYAN_SHULKER_BOX, DyeColor.CYAN);
		tintedShulkerBoxRecipe(consumer, Blocks.GRAY_SHULKER_BOX, DyeColor.GRAY);
		tintedShulkerBoxRecipe(consumer, Blocks.GREEN_SHULKER_BOX, DyeColor.GREEN);
		tintedShulkerBoxRecipe(consumer, Blocks.LIGHT_BLUE_SHULKER_BOX, DyeColor.LIGHT_BLUE);
		tintedShulkerBoxRecipe(consumer, Blocks.LIGHT_GRAY_SHULKER_BOX, DyeColor.LIGHT_GRAY);
		tintedShulkerBoxRecipe(consumer, Blocks.LIME_SHULKER_BOX, DyeColor.LIME);
		tintedShulkerBoxRecipe(consumer, Blocks.MAGENTA_SHULKER_BOX, DyeColor.MAGENTA);
		tintedShulkerBoxRecipe(consumer, Blocks.ORANGE_SHULKER_BOX, DyeColor.ORANGE);
		tintedShulkerBoxRecipe(consumer, Blocks.PINK_SHULKER_BOX, DyeColor.PINK);
		tintedShulkerBoxRecipe(consumer, Blocks.PURPLE_SHULKER_BOX, DyeColor.PURPLE);
		tintedShulkerBoxRecipe(consumer, Blocks.RED_SHULKER_BOX, DyeColor.RED);
		tintedShulkerBoxRecipe(consumer, Blocks.WHITE_SHULKER_BOX, DyeColor.WHITE);
		tintedShulkerBoxRecipe(consumer, Blocks.YELLOW_SHULKER_BOX, DyeColor.YELLOW);

		ShapeBasedRecipeBuilder.shaped(ModBlocks.SHULKER_BOX_ITEM, ModBlocks.SHULKER_BOX_FROM_CHEST_RECIPE_SERIALIZER)
				.pattern("S")
				.pattern("C")
				.pattern("S")
				.define('C', ModBlocks.CHEST_ITEM)
				.define('S', Items.SHULKER_SHELL)
				.unlockedBy("has_chest", has(ModBlocks.CHEST_ITEM))
				.save(consumer, SophisticatedStorage.getRL("shulker_from_chest"));

		addStorageTierUpgradeRecipes(consumer, ModBlocks.SHULKER_BOX_ITEM, ModBlocks.COPPER_SHULKER_BOX_ITEM, ModBlocks.IRON_SHULKER_BOX_ITEM, ModBlocks.GOLD_SHULKER_BOX_ITEM, ModBlocks.DIAMOND_SHULKER_BOX_ITEM, ModBlocks.NETHERITE_SHULKER_BOX_ITEM);

		ShapeBasedRecipeBuilder.shaped(ModBlocks.COPPER_SHULKER_BOX_ITEM, ModBlocks.SHULKER_BOX_FROM_CHEST_RECIPE_SERIALIZER)
				.pattern("S")
				.pattern("C")
				.pattern("S")
				.define('C', ModBlocks.COPPER_CHEST_ITEM)
				.define('S', Items.SHULKER_SHELL)
				.unlockedBy("has_copper_chest", has(ModBlocks.COPPER_CHEST_ITEM))
				.save(consumer, SophisticatedStorage.getRL("copper_shulker_from_copper_chest"));


		ShapeBasedRecipeBuilder.shaped(ModBlocks.IRON_SHULKER_BOX_ITEM, ModBlocks.SHULKER_BOX_FROM_CHEST_RECIPE_SERIALIZER)
				.pattern("S")
				.pattern("C")
				.pattern("S")
				.define('C', ModBlocks.IRON_CHEST_ITEM)
				.define('S', Items.SHULKER_SHELL)
				.unlockedBy("has_iron_chest", has(ModBlocks.IRON_CHEST_ITEM))
				.save(consumer, SophisticatedStorage.getRL("iron_shulker_from_iron_chest"));

		ShapeBasedRecipeBuilder.shaped(ModBlocks.GOLD_SHULKER_BOX_ITEM, ModBlocks.SHULKER_BOX_FROM_CHEST_RECIPE_SERIALIZER)
				.pattern("S")
				.pattern("C")
				.pattern("S")
				.define('C', ModBlocks.GOLD_CHEST_ITEM)
				.define('S', Items.SHULKER_SHELL)
				.unlockedBy("has_gold_chest", has(ModBlocks.GOLD_CHEST_ITEM))
				.save(consumer, SophisticatedStorage.getRL("gold_shulker_from_gold_chest"));

		ShapeBasedRecipeBuilder.shaped(ModBlocks.DIAMOND_SHULKER_BOX_ITEM, ModBlocks.SHULKER_BOX_FROM_CHEST_RECIPE_SERIALIZER)
				.pattern("S")
				.pattern("C")
				.pattern("S")
				.define('C', ModBlocks.DIAMOND_CHEST_ITEM)
				.define('S', Items.SHULKER_SHELL)
				.unlockedBy("has_diamond_chest", has(ModBlocks.DIAMOND_CHEST_ITEM))
				.save(consumer, SophisticatedStorage.getRL("diamond_shulker_from_diamond_chest"));

		ShapeBasedRecipeBuilder.shaped(ModBlocks.NETHERITE_SHULKER_BOX_ITEM, ModBlocks.SHULKER_BOX_FROM_CHEST_RECIPE_SERIALIZER)
				.pattern("S")
				.pattern("C")
				.pattern("S")
				.define('C', ModBlocks.NETHERITE_CHEST_ITEM)
				.define('S', Items.SHULKER_SHELL)
				.unlockedBy("has_netherite_chest", has(ModBlocks.NETHERITE_CHEST_ITEM))
				.save(consumer, SophisticatedStorage.getRL("netherite_shulker_from_netherite_chest"));
	}

	private void addTierUpgradeItemRecipes(Consumer<FinishedRecipe> consumer) {
		ShapeBasedRecipeBuilder.shaped(ModItems.BASIC_TIER_UPGRADE)
				.pattern(" S ")
				.pattern("SRS")
				.pattern(" S ")
				.define('R', Items.REDSTONE_TORCH)
				.define('S', Items.STICK)
				.unlockedBy(HAS_REDSTONE_TORCH_CRITERION_NAME, has(Items.REDSTONE_TORCH))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.BASIC_TO_COPPER_TIER_UPGRADE)
				.pattern("CCC")
				.pattern("CRC")
				.pattern("CCC")
				.define('R', Items.REDSTONE_TORCH)
				.define('C', ConventionalItemTags.COPPER_INGOTS)
				.unlockedBy(HAS_REDSTONE_TORCH_CRITERION_NAME, has(Items.REDSTONE_TORCH))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.BASIC_TO_IRON_TIER_UPGRADE)
				.pattern("III")
				.pattern("IRI")
				.pattern("III")
				.define('R', Items.REDSTONE_TORCH)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.unlockedBy(HAS_REDSTONE_TORCH_CRITERION_NAME, has(Items.REDSTONE_TORCH))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.BASIC_TO_IRON_TIER_UPGRADE)
				.pattern(" I ")
				.pattern("IRI")
				.pattern(" I ")
				.define('R', ModItems.BASIC_TO_COPPER_TIER_UPGRADE)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.unlockedBy(HAS_REDSTONE_TORCH_CRITERION_NAME, has(Items.REDSTONE_TORCH))
				.save(consumer, SophisticatedStorage.getRL("basic_to_iron_tier_from_basic_to_copper_tier"));

		ShapeBasedRecipeBuilder.shaped(ModItems.BASIC_TO_GOLD_TIER_UPGRADE)
				.pattern("GGG")
				.pattern("GTG")
				.pattern("GGG")
				.define('T', ModItems.BASIC_TO_IRON_TIER_UPGRADE)
				.define('G', ConventionalItemTags.GOLD_INGOTS)
				.unlockedBy("has_basic_to_iron_tier_upgrade", has(ModItems.BASIC_TO_IRON_TIER_UPGRADE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.BASIC_TO_DIAMOND_TIER_UPGRADE)
				.pattern("DDD")
				.pattern("DTD")
				.pattern("DDD")
				.define('T', ModItems.BASIC_TO_GOLD_TIER_UPGRADE)
				.define('D', ConventionalItemTags.DIAMONDS)
				.unlockedBy("has_basic_to_gold_tier_upgrade", has(ModItems.BASIC_TO_GOLD_TIER_UPGRADE))
				.save(consumer);

		ShapelessBasedRecipeBuilder.shapeless(ModItems.BASIC_TO_NETHERITE_TIER_UPGRADE)
				.requires(ModItems.BASIC_TO_DIAMOND_TIER_UPGRADE)
				.requires(ConventionalItemTags.NETHERITE_INGOTS)
				.unlockedBy("has_basic_to_diamond_tier_upgrade", has(ModItems.BASIC_TO_DIAMOND_TIER_UPGRADE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.COPPER_TO_IRON_TIER_UPGRADE)
				.pattern(" I ")
				.pattern("IRI")
				.pattern(" I ")
				.define('R', Items.REDSTONE_TORCH)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.unlockedBy(HAS_REDSTONE_TORCH_CRITERION_NAME, has(Items.REDSTONE_TORCH))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.COPPER_TO_GOLD_TIER_UPGRADE)
				.pattern("GGG")
				.pattern("GTG")
				.pattern("GGG")
				.define('T', ModItems.COPPER_TO_IRON_TIER_UPGRADE)
				.define('G', ConventionalItemTags.GOLD_INGOTS)
				.unlockedBy("has_copper_to_iron_tier_upgrade", has(ModItems.COPPER_TO_IRON_TIER_UPGRADE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.COPPER_TO_DIAMOND_TIER_UPGRADE)
				.pattern("DDD")
				.pattern("DTD")
				.pattern("DDD")
				.define('T', ModItems.COPPER_TO_GOLD_TIER_UPGRADE)
				.define('D', ConventionalItemTags.DIAMONDS)
				.unlockedBy("has_copper_to_gold_tier_upgrade", has(ModItems.COPPER_TO_GOLD_TIER_UPGRADE))
				.save(consumer);

		ShapelessBasedRecipeBuilder.shapeless(ModItems.COPPER_TO_NETHERITE_TIER_UPGRADE)
				.requires(ModItems.COPPER_TO_DIAMOND_TIER_UPGRADE)
				.requires(ConventionalItemTags.NETHERITE_INGOTS)
				.unlockedBy("has_copper_to_diamond_tier_upgrade", has(ModItems.COPPER_TO_DIAMOND_TIER_UPGRADE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.IRON_TO_GOLD_TIER_UPGRADE)
				.pattern("GGG")
				.pattern("GRG")
				.pattern("GGG")
				.define('R', Items.REDSTONE_TORCH)
				.define('G', ConventionalItemTags.GOLD_INGOTS)
				.unlockedBy(HAS_REDSTONE_TORCH_CRITERION_NAME, has(Items.REDSTONE_TORCH))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.IRON_TO_DIAMOND_TIER_UPGRADE)
				.pattern("DDD")
				.pattern("DTD")
				.pattern("DDD")
				.define('T', ModItems.IRON_TO_GOLD_TIER_UPGRADE)
				.define('D', ConventionalItemTags.DIAMONDS)
				.unlockedBy("has_iron_to_gold_tier_upgrade", has(ModItems.IRON_TO_GOLD_TIER_UPGRADE))
				.save(consumer);

		ShapelessBasedRecipeBuilder.shapeless(ModItems.IRON_TO_NETHERITE_TIER_UPGRADE)
				.requires(ModItems.IRON_TO_DIAMOND_TIER_UPGRADE)
				.requires(ConventionalItemTags.NETHERITE_INGOTS)
				.unlockedBy("has_iron_to_diamond_tier_upgrade", has(ModItems.IRON_TO_DIAMOND_TIER_UPGRADE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.GOLD_TO_DIAMOND_TIER_UPGRADE)
				.pattern("DDD")
				.pattern("DRD")
				.pattern("DDD")
				.define('R', Items.REDSTONE_TORCH)
				.define('D', ConventionalItemTags.DIAMONDS)
				.unlockedBy(HAS_REDSTONE_TORCH_CRITERION_NAME, has(Items.REDSTONE_TORCH))
				.save(consumer);

		ShapelessBasedRecipeBuilder.shapeless(ModItems.GOLD_TO_NETHERITE_TIER_UPGRADE)
				.requires(ModItems.GOLD_TO_DIAMOND_TIER_UPGRADE)
				.requires(ConventionalItemTags.NETHERITE_INGOTS)
				.unlockedBy("has_gold_to_diamond_tier_upgrade", has(ModItems.GOLD_TO_DIAMOND_TIER_UPGRADE))
				.save(consumer);

		ShapelessBasedRecipeBuilder.shapeless(ModItems.DIAMOND_TO_NETHERITE_TIER_UPGRADE)
				.requires(Items.REDSTONE_TORCH)
				.requires(ConventionalItemTags.NETHERITE_INGOTS)
				.unlockedBy(HAS_REDSTONE_TORCH_CRITERION_NAME, has(Items.REDSTONE_TORCH))
				.save(consumer);
	}

	private void addUpgradeRecipes(Consumer<FinishedRecipe> consumer) {
		ShapeBasedRecipeBuilder.shaped(ModItems.UPGRADE_BASE)
				.pattern("PIP")
				.pattern("IPI")
				.pattern("PIP")
				.define('P', ItemTags.PLANKS)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.unlockedBy("has_iron_ingot", has(ConventionalItemTags.IRON_INGOTS))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.PICKUP_UPGRADE)
				.pattern(" P ")
				.pattern("LBL")
				.pattern("RRR")
				.define('B', ModItems.UPGRADE_BASE)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('L', ItemTags.PLANKS)
				.define('P', Blocks.STICKY_PISTON)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.UPGRADE_BASE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.ADVANCED_PICKUP_UPGRADE, ModRecipes.UPGRADE_NEXT_TIER_SERIALIZER)
				.pattern(" D ")
				.pattern("GPG")
				.pattern("RRR")
				.define('D', ConventionalItemTags.DIAMONDS)
				.define('G', ConventionalItemTags.GOLD_INGOTS)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('P', ModItems.PICKUP_UPGRADE)
				.unlockedBy("has_pickup_upgrade", has(ModItems.PICKUP_UPGRADE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.FILTER_UPGRADE)
				.pattern("RSR")
				.pattern("SBS")
				.pattern("RSR")
				.define('B', ModItems.UPGRADE_BASE)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('S', Items.STRING)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.UPGRADE_BASE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.ADVANCED_FILTER_UPGRADE, ModRecipes.UPGRADE_NEXT_TIER_SERIALIZER)
				.pattern("GPG")
				.pattern("RRR")
				.define('G', ConventionalItemTags.GOLD_INGOTS)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('P', ModItems.FILTER_UPGRADE)
				.unlockedBy("has_filter_upgrade", has(ModItems.FILTER_UPGRADE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.MAGNET_UPGRADE, ModRecipes.UPGRADE_NEXT_TIER_SERIALIZER)
				.pattern("EIE")
				.pattern("IPI")
				.pattern("R L")
				.define('E', Items.ENDER_PEARL)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('L', ConventionalItemTags.LAPIS)
				.define('P', ModItems.PICKUP_UPGRADE)
				.unlockedBy("has_pickup_upgrade", has(ModItems.PICKUP_UPGRADE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.ADVANCED_MAGNET_UPGRADE, ModRecipes.UPGRADE_NEXT_TIER_SERIALIZER)
				.pattern("EIE")
				.pattern("IPI")
				.pattern("R L")
				.define('E', Items.ENDER_PEARL)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('L', ConventionalItemTags.LAPIS)
				.define('P', ModItems.ADVANCED_PICKUP_UPGRADE)
				.unlockedBy("has_advanced_pickup_upgrade", has(ModItems.ADVANCED_PICKUP_UPGRADE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.ADVANCED_MAGNET_UPGRADE, ModRecipes.UPGRADE_NEXT_TIER_SERIALIZER)
				.pattern(" D ")
				.pattern("GMG")
				.pattern("RRR")
				.define('D', ConventionalItemTags.DIAMONDS)
				.define('G', ConventionalItemTags.GOLD_INGOTS)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('M', ModItems.MAGNET_UPGRADE)
				.unlockedBy("has_magnet_upgrade", has(ModItems.MAGNET_UPGRADE))
				.save(consumer, SophisticatedStorage.getRL("advanced_magnet_upgrade_from_basic"));

		ShapeBasedRecipeBuilder.shaped(ModItems.FEEDING_UPGRADE)
				.pattern(" C ")
				.pattern("ABM")
				.pattern(" E ")
				.define('B', ModItems.UPGRADE_BASE)
				.define('C', Items.GOLDEN_CARROT)
				.define('A', Items.GOLDEN_APPLE)
				.define('M', Items.GLISTERING_MELON_SLICE)
				.define('E', Items.ENDER_PEARL)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.UPGRADE_BASE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.COMPACTING_UPGRADE)
				.pattern("IPI")
				.pattern("PBP")
				.pattern("RPR")
				.define('B', ModItems.UPGRADE_BASE)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.define('P', Items.PISTON)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.UPGRADE_BASE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.ADVANCED_COMPACTING_UPGRADE, ModRecipes.UPGRADE_NEXT_TIER_SERIALIZER)
				.pattern(" D ")
				.pattern("GCG")
				.pattern("RRR")
				.define('D', ConventionalItemTags.DIAMONDS)
				.define('G', ConventionalItemTags.GOLD_INGOTS)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('C', ModItems.COMPACTING_UPGRADE)
				.unlockedBy("has_compacting_upgrade", has(ModItems.COMPACTING_UPGRADE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.VOID_UPGRADE)
				.pattern(" E ")
				.pattern("OBO")
				.pattern("ROR")
				.define('B', ModItems.UPGRADE_BASE)
				.define('E', Items.ENDER_PEARL)
				.define('O', Items.OBSIDIAN)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.UPGRADE_BASE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.ADVANCED_VOID_UPGRADE, ModRecipes.UPGRADE_NEXT_TIER_SERIALIZER)
				.pattern(" D ")
				.pattern("GVG")
				.pattern("RRR")
				.define('D', ConventionalItemTags.DIAMONDS)
				.define('G', ConventionalItemTags.GOLD_INGOTS)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('V', ModItems.VOID_UPGRADE)
				.unlockedBy("has_void_upgrade", has(ModItems.VOID_UPGRADE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.SMELTING_UPGRADE)
				.pattern("RIR")
				.pattern("IBI")
				.pattern("RFR")
				.define('B', ModItems.UPGRADE_BASE)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.define('F', Items.FURNACE)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.UPGRADE_BASE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.AUTO_SMELTING_UPGRADE, ModRecipes.UPGRADE_NEXT_TIER_SERIALIZER)
				.pattern("DHD")
				.pattern("RSH")
				.pattern("GHG")
				.define('D', ConventionalItemTags.DIAMONDS)
				.define('G', ConventionalItemTags.GOLD_INGOTS)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('H', Items.HOPPER)
				.define('S', ModItems.SMELTING_UPGRADE)
				.unlockedBy(HAS_SMELTING_UPGRADE_CRITERION_NAME, has(ModItems.SMELTING_UPGRADE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.CRAFTING_UPGRADE)
				.pattern(" T ")
				.pattern("IBI")
				.pattern(" C ")
				.define('B', ModItems.UPGRADE_BASE)
				.define('C', ConventionalItemTags.CHESTS)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.define('T', Items.CRAFTING_TABLE)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.UPGRADE_BASE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.STONECUTTER_UPGRADE)
				.pattern(" S ")
				.pattern("IBI")
				.pattern(" R ")
				.define('B', ModItems.UPGRADE_BASE)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.define('S', Items.STONECUTTER)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.UPGRADE_BASE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.STACK_UPGRADE_TIER_1)
				.pattern("LLL")
				.pattern("LBL")
				.pattern("LLL")
				.define('B', ModItems.UPGRADE_BASE)
				.define('L', ItemTags.LOGS)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.UPGRADE_BASE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.STACK_UPGRADE_TIER_1_PLUS)
				.pattern("CCC")
				.pattern("CSC")
				.pattern("BCB")
				.define('S', ModItems.STACK_UPGRADE_TIER_1)
				.define('C', ConventionalItemTags.COPPER_INGOTS)
				.define('B', Tags.Items.STORAGE_BLOCKS_COPPER)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.STACK_UPGRADE_TIER_1))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.STACK_UPGRADE_TIER_2)
				.pattern(" I ")
				.pattern("ISI")
				.pattern(" B ")
				.define('S', ModItems.STACK_UPGRADE_TIER_1_PLUS)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.define('B', Tags.Items.STORAGE_BLOCKS_IRON)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.STACK_UPGRADE_TIER_1_PLUS))
				.save(consumer, SophisticatedStorage.getRL("stack_upgrade_tier_2_from_tier_1_plus"));

		ShapeBasedRecipeBuilder.shaped(ModItems.STACK_UPGRADE_TIER_2)
				.pattern("III")
				.pattern("ISI")
				.pattern("BIB")
				.define('S', ModItems.STACK_UPGRADE_TIER_1)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.define('B', Tags.Items.STORAGE_BLOCKS_IRON)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.STACK_UPGRADE_TIER_1))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.STACK_UPGRADE_TIER_3)
				.pattern("GGG")
				.pattern("GSG")
				.pattern("BGB")
				.define('S', ModItems.STACK_UPGRADE_TIER_2)
				.define('G', ConventionalItemTags.GOLD_INGOTS)
				.define('B', Tags.Items.STORAGE_BLOCKS_GOLD)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.STACK_UPGRADE_TIER_2))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.STACK_UPGRADE_TIER_4)
				.pattern("DDD")
				.pattern("DSD")
				.pattern("BDB")
				.define('S', ModItems.STACK_UPGRADE_TIER_3)
				.define('D', ConventionalItemTags.DIAMONDS)
				.define('B', Tags.Items.STORAGE_BLOCKS_DIAMOND)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.STACK_UPGRADE_TIER_3))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.STACK_UPGRADE_TIER_5)
				.pattern("NNN")
				.pattern("NSN")
				.pattern("BNB")
				.define('S', ModItems.STACK_UPGRADE_TIER_4)
				.define('N', ConventionalItemTags.NETHERITE_INGOTS)
				.define('B', Tags.Items.STORAGE_BLOCKS_NETHERITE)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.STACK_UPGRADE_TIER_4))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.JUKEBOX_UPGRADE)
				.pattern(" J ")
				.pattern("IBI")
				.pattern(" R ")
				.define('B', ModItems.UPGRADE_BASE)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.define('J', Items.JUKEBOX)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.UPGRADE_BASE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.ADVANCED_FEEDING_UPGRADE, ModRecipes.UPGRADE_NEXT_TIER_SERIALIZER)
				.pattern(" D ")
				.pattern("GVG")
				.pattern("RRR")
				.define('D', ConventionalItemTags.DIAMONDS)
				.define('G', ConventionalItemTags.GOLD_INGOTS)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('V', ModItems.FEEDING_UPGRADE)
				.unlockedBy("has_feeding_upgrade", has(ModItems.FEEDING_UPGRADE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.SMOKING_UPGRADE)
				.pattern("RIR")
				.pattern("IBI")
				.pattern("RSR")
				.define('B', ModItems.UPGRADE_BASE)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.define('S', Items.SMOKER)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.UPGRADE_BASE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.SMOKING_UPGRADE)
				.pattern(" L ")
				.pattern("LSL")
				.pattern(" L ")
				.define('S', ModItems.SMELTING_UPGRADE)
				.define('L', ItemTags.LOGS)
				.unlockedBy(HAS_SMELTING_UPGRADE_CRITERION_NAME, has(ModItems.SMELTING_UPGRADE))
				.save(consumer, SophisticatedStorage.getRL("smoking_upgrade_from_smelting_upgrade"));

		ShapeBasedRecipeBuilder.shaped(ModItems.AUTO_SMOKING_UPGRADE, ModRecipes.UPGRADE_NEXT_TIER_SERIALIZER)
				.pattern("DHD")
				.pattern("RSH")
				.pattern("GHG")
				.define('D', ConventionalItemTags.DIAMONDS)
				.define('G', ConventionalItemTags.GOLD_INGOTS)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('H', Items.HOPPER)
				.define('S', ModItems.SMOKING_UPGRADE)
				.unlockedBy("has_smoking_upgrade", has(ModItems.SMOKING_UPGRADE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.AUTO_SMOKING_UPGRADE)
				.pattern(" L ")
				.pattern("LSL")
				.pattern(" L ")
				.define('S', ModItems.AUTO_SMELTING_UPGRADE)
				.define('L', ItemTags.LOGS)
				.unlockedBy("has_auto_smelting_upgrade", has(ModItems.AUTO_SMELTING_UPGRADE))
				.save(consumer, SophisticatedStorage.getRL("auto_smoking_upgrade_from_auto_smelting_upgrade"));

		ShapeBasedRecipeBuilder.shaped(ModItems.BLASTING_UPGRADE)
				.pattern("RIR")
				.pattern("IBI")
				.pattern("RFR")
				.define('B', ModItems.UPGRADE_BASE)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.define('F', Items.BLAST_FURNACE)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.UPGRADE_BASE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.BLASTING_UPGRADE)
				.pattern("III")
				.pattern("ISI")
				.pattern("TTT")
				.define('S', ModItems.SMELTING_UPGRADE)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.define('T', Items.SMOOTH_STONE)
				.unlockedBy(HAS_SMELTING_UPGRADE_CRITERION_NAME, has(ModItems.SMELTING_UPGRADE))
				.save(consumer, SophisticatedStorage.getRL("blasting_upgrade_from_smelting_upgrade"));

		ShapeBasedRecipeBuilder.shaped(ModItems.AUTO_BLASTING_UPGRADE, ModRecipes.UPGRADE_NEXT_TIER_SERIALIZER)
				.pattern("DHD")
				.pattern("RSH")
				.pattern("GHG")
				.define('D', ConventionalItemTags.DIAMONDS)
				.define('G', ConventionalItemTags.GOLD_INGOTS)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('H', Items.HOPPER)
				.define('S', ModItems.BLASTING_UPGRADE)
				.unlockedBy("has_blasting_upgrade", has(ModItems.BLASTING_UPGRADE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.AUTO_BLASTING_UPGRADE)
				.pattern("III")
				.pattern("ISI")
				.pattern("TTT")
				.define('S', ModItems.AUTO_SMELTING_UPGRADE)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.define('T', Items.SMOOTH_STONE)
				.unlockedBy("has_auto_smelting_upgrade", has(ModItems.AUTO_SMELTING_UPGRADE))
				.save(consumer, SophisticatedStorage.getRL("auto_blasting_upgrade_from_auto_smelting_upgrade"));

		ShapeBasedRecipeBuilder.shaped(ModItems.COMPRESSION_UPGRADE)
				.pattern(" I ")
				.pattern("PBP")
				.pattern("RIR")
				.define('B', ModItems.UPGRADE_BASE)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.define('P', Items.PISTON)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.UPGRADE_BASE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.HOPPER_UPGRADE)
				.pattern(" H ")
				.pattern("IBI")
				.pattern("RRR")
				.define('B', ModItems.UPGRADE_BASE)
				.define('H', Items.HOPPER)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.UPGRADE_BASE))
				.save(consumer);

		ShapeBasedRecipeBuilder.shaped(ModItems.ADVANCED_HOPPER_UPGRADE, ModRecipes.UPGRADE_NEXT_TIER_SERIALIZER)
				.pattern(" D ")
				.pattern("GHG")
				.pattern("ROR")
				.define('D', ConventionalItemTags.DIAMONDS)
				.define('G', ConventionalItemTags.GOLD_INGOTS)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('O', Items.DROPPER)
				.define('H', ModItems.HOPPER_UPGRADE)
				.unlockedBy("has_feeding_upgrade", has(ModItems.HOPPER_UPGRADE))
				.save(consumer);

		addChippedUpgradeRecipes(consumer);
	}

	private static void addChippedUpgradeRecipes(Consumer<FinishedRecipe> consumer) {
		addChippedUpgradeRecipe(consumer, ChippedCompat.BOTANIST_WORKBENCH_UPGRADE, earth.terrarium.chipped.common.registry.ModBlocks.BOTANIST_WORKBENCH.get(), net.p3pp3rf1y.sophisticatedbackpacks.compat.chipped.ChippedCompat.BOTANIST_WORKBENCH_UPGRADE);
		addChippedUpgradeRecipe(consumer, ChippedCompat.GLASSBLOWER_UPGRADE, earth.terrarium.chipped.common.registry.ModBlocks.GLASSBLOWER.get(), net.p3pp3rf1y.sophisticatedbackpacks.compat.chipped.ChippedCompat.GLASSBLOWER_UPGRADE);
		addChippedUpgradeRecipe(consumer, ChippedCompat.CARPENTERS_TABLE_UPGRADE, earth.terrarium.chipped.common.registry.ModBlocks.CARPENTERS_TABLE.get(), net.p3pp3rf1y.sophisticatedbackpacks.compat.chipped.ChippedCompat.CARPENTERS_TABLE_UPGRADE);
		addChippedUpgradeRecipe(consumer, ChippedCompat.LOOM_TABLE_UPGRADE, earth.terrarium.chipped.common.registry.ModBlocks.LOOM_TABLE.get(), net.p3pp3rf1y.sophisticatedbackpacks.compat.chipped.ChippedCompat.LOOM_TABLE_UPGRADE);
		addChippedUpgradeRecipe(consumer, ChippedCompat.MASON_TABLE_UPGRADE, earth.terrarium.chipped.common.registry.ModBlocks.MASON_TABLE.get(), net.p3pp3rf1y.sophisticatedbackpacks.compat.chipped.ChippedCompat.MASON_TABLE_UPGRADE);
		addChippedUpgradeRecipe(consumer, ChippedCompat.ALCHEMY_BENCH_UPGRADE, earth.terrarium.chipped.common.registry.ModBlocks.ALCHEMY_BENCH.get(), net.p3pp3rf1y.sophisticatedbackpacks.compat.chipped.ChippedCompat.ALCHEMY_BENCH_UPGRADE);
		addChippedUpgradeRecipe(consumer, ChippedCompat.TINKERING_TABLE_UPGRADE, earth.terrarium.chipped.common.registry.ModBlocks.TINKERING_TABLE.get(), net.p3pp3rf1y.sophisticatedbackpacks.compat.chipped.ChippedCompat.TINKERING_TABLE_UPGRADE);
	}

	private static void addChippedUpgradeRecipe(Consumer<FinishedRecipe> consumer, BlockTransformationUpgradeItem upgrade, Block workbench, BlockTransformationUpgradeItem backpackUpgrade) {
		ShapeBasedRecipeBuilder.shaped(upgrade)
				.pattern(" W ")
				.pattern("IBI")
				.pattern(" R ")
				.define('B', ModItems.UPGRADE_BASE)
				.define('R', ConventionalItemTags.REDSTONE_DUSTS)
				.define('I', ConventionalItemTags.IRON_INGOTS)
				.define('W', workbench)
				.unlockedBy(HAS_UPGRADE_BASE_CRITERION_NAME, has(ModItems.UPGRADE_BASE))
				.condition(DefaultResourceConditions.allModsLoaded(CompatModIds.CHIPPED))
				.save(consumer);

		//storage from backpack upgrade
		ShapeBasedRecipeBuilder.shaped(upgrade)
				.pattern("PUP")
				.pattern(" P ")
				.pattern("P P")
				.define('P', ItemTags.PLANKS)
				.define('U', backpackUpgrade)
				.unlockedBy("has_backpack_upgrade", has(backpackUpgrade))
				.condition(DefaultResourceConditions.allModsLoaded(CompatModIds.CHIPPED, SophisticatedBackpacks.MOD_ID))
				.save(consumer, new ResourceLocation(SophisticatedStorage.MOD_ID, "storage_" + getChippedItemPath(upgrade) + "_from_backpack_" + getChippedItemPath(backpackUpgrade)));

		//backpack from storage upgrade
		ShapeBasedRecipeBuilder.shaped(backpackUpgrade)
				.pattern("TUT")
				.pattern(" L ")
				.pattern("T T")
				.define('T', Items.STRING)
				.define('L', Items.LEATHER)
				.define('U', upgrade)
				.unlockedBy("has_storage_upgrade", has(upgrade))
				.condition(DefaultResourceConditions.allModsLoaded(CompatModIds.CHIPPED, SophisticatedBackpacks.MOD_ID))
				.save(consumer, new ResourceLocation(SophisticatedStorage.MOD_ID, "backpack_" + getChippedItemPath(backpackUpgrade) + "_from_storage_" + getChippedItemPath(upgrade)));
	}

	private static String getChippedItemPath(BlockTransformationUpgradeItem upgrade) {
		return RegistryHelper.getItemKey(upgrade).getPath().replace('/', '_');
	}

	private void addChestRecipes(Consumer<FinishedRecipe> consumer) {
		WoodStorageBlockBase.CUSTOM_TEXTURE_WOOD_TYPES.forEach((woodType, blockFamily) -> woodChestRecipe(consumer, woodType, blockFamily.getBaseBlock()));

		ShapelessBasedRecipeBuilder.shapeless(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.CHEST_ITEM), WoodType.OAK))
				.requires(Blocks.CHEST)
				.requires(Blocks.REDSTONE_TORCH)
				.unlockedBy("has_vanilla_chest", has(Blocks.CHEST))
				.save(consumer, SophisticatedStorage.getRL("oak_chest_from_vanilla_chest"));

		addStorageTierUpgradeRecipes(consumer, ModBlocks.CHEST_ITEM, ModBlocks.COPPER_CHEST_ITEM, ModBlocks.IRON_CHEST_ITEM, ModBlocks.GOLD_CHEST_ITEM, ModBlocks.DIAMOND_CHEST_ITEM, ModBlocks.NETHERITE_CHEST_ITEM);

//		addQuarkChestRecipes(consumer);
	}

	/*private void addQuarkChestRecipes(Consumer<FinishedRecipe> consumer) {
		addQuarkChestRecipe(consumer, "oak_chest", WoodType.OAK);
		addQuarkChestRecipe(consumer, "acacia_chest", WoodType.ACACIA);
		addQuarkChestRecipe(consumer, "birch_chest", WoodType.BIRCH);
		addQuarkChestRecipe(consumer, "crimson_chest", WoodType.CRIMSON);
		addQuarkChestRecipe(consumer, "dark_oak_chest", WoodType.DARK_OAK);
		addQuarkChestRecipe(consumer, "jungle_chest", WoodType.JUNGLE);
		addQuarkChestRecipe(consumer, "mangrove_chest", WoodType.MANGROVE);
		addQuarkChestRecipe(consumer, "spruce_chest", WoodType.SPRUCE);
		addQuarkChestRecipe(consumer, "warped_chest", WoodType.WARPED);
		addQuarkChestRecipe(consumer, "bamboo_chest", WoodType.BAMBOO);
		addQuarkChestRecipe(consumer, "cherry_chest", WoodType.CHERRY);
	}

	private void addQuarkChestRecipe(Consumer<FinishedRecipe> consumer, String name, WoodType woodType) {
		String chestRegistryName = "quark:" + name;
		Block chestBlock = getBlock(chestRegistryName);
		ShapelessBasedRecipeBuilder.shapeless(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.CHEST_ITEM), woodType))
				.requires(chestBlock)
				.requires(Blocks.REDSTONE_TORCH)
				.condition(new ItemExistsCondition(chestRegistryName))
				.save(consumer, SophisticatedStorage.getRL(woodType.name() + "_chest_from_quark_" + name));
	}

	private Block getBlock(String registryName) {
		//noinspection ConstantConditions - could only fail in dev environment and crashing is preferred here to fix issues early
		return BuiltInRegistries.BLOCK.get(new ResourceLocation(registryName));
	}*/

	private void addBarrelRecipes(Consumer<FinishedRecipe> consumer) {
		WoodStorageBlockBase.CUSTOM_TEXTURE_WOOD_TYPES.forEach((woodType, blockFamily) -> woodBarrelRecipe(consumer, woodType, blockFamily.getBaseBlock(), blockFamily.get(BlockFamily.Variant.SLAB)));

		ShapelessBasedRecipeBuilder.shapeless(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.BARREL_ITEM), WoodType.SPRUCE))
				.requires(Blocks.BARREL)
				.requires(Blocks.REDSTONE_TORCH)
				.unlockedBy("has_vanilla_barrel", has(Blocks.BARREL))
				.save(consumer, SophisticatedStorage.getRL("spruce_barrel_from_vanilla_barrel"));

		addStorageTierUpgradeRecipes(consumer, ModBlocks.BARREL_ITEM, ModBlocks.COPPER_BARREL_ITEM, ModBlocks.IRON_BARREL_ITEM, ModBlocks.GOLD_BARREL_ITEM, ModBlocks.DIAMOND_BARREL_ITEM, ModBlocks.NETHERITE_BARREL_ITEM);
	}

	private void woodBarrelRecipe(Consumer<FinishedRecipe> consumer, WoodType woodType, Block planks, Block slab) {
		ShapeBasedRecipeBuilder.shaped(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.BARREL_ITEM), woodType))
				.pattern("PSP")
				.pattern("PRP")
				.pattern("PSP")
				.define('P', planks)
				.define('S', slab)
				.define('R', Blocks.REDSTONE_TORCH)
				.unlockedBy("has_" + woodType.name() + PLANK_SUFFIX, has(planks))
				.save(consumer, SophisticatedStorage.getRL(woodType.name() + "_barrel"));
	}

	private void limitedWoodBarrelRecipe(Consumer<FinishedRecipe> consumer, WoodType woodType, Block planks, Block slab, Consumer<ShapeBasedRecipeBuilder> addPattern, BlockItem item) {
		ShapeBasedRecipeBuilder builder = ShapeBasedRecipeBuilder.shaped(WoodStorageBlockItem.setWoodType(new ItemStack(item), woodType))
				.define('P', planks)
				.define('S', slab)
				.define('R', Blocks.REDSTONE_TORCH)
				.unlockedBy("has_" + woodType.name() + PLANK_SUFFIX, has(planks));
		addPattern.accept(builder);
		builder.save(consumer, SophisticatedStorage.getRL(woodType.name() + "_" + RegistryHelper.getItemKey(item).getPath()));
	}

	private void limitedWoodBarrel1Recipe(Consumer<FinishedRecipe> consumer, WoodType woodType, Block planks, Block slab) {
		limitedWoodBarrelRecipe(consumer, woodType, planks, slab, builder ->
						builder.pattern("PSP")
								.pattern("PRP")
								.pattern("PPP")
				, ModBlocks.LIMITED_BARREL_1_ITEM);
	}

	private void limitedWoodBarrel2Recipe(Consumer<FinishedRecipe> consumer, WoodType woodType, Block planks, Block slab) {
		limitedWoodBarrelRecipe(consumer, woodType, planks, slab, builder ->
						builder.pattern("PPP")
								.pattern("SRS")
								.pattern("PPP")
				, ModBlocks.LIMITED_BARREL_2_ITEM);
	}

	private void limitedWoodBarrel3Recipe(Consumer<FinishedRecipe> consumer, WoodType woodType, Block planks, Block slab) {
		limitedWoodBarrelRecipe(consumer, woodType, planks, slab, builder ->
						builder.pattern("PSP")
								.pattern("PRP")
								.pattern("SPS")
				, ModBlocks.LIMITED_BARREL_3_ITEM);
	}

	private void limitedWoodBarrel4Recipe(Consumer<FinishedRecipe> consumer, WoodType woodType, Block planks, Block slab) {
		limitedWoodBarrelRecipe(consumer, woodType, planks, slab, builder ->
						builder.pattern("SPS")
								.pattern("PRP")
								.pattern("SPS")
				, ModBlocks.LIMITED_BARREL_4_ITEM);
	}

	private void woodChestRecipe(Consumer<FinishedRecipe> consumer, WoodType woodType, Block planks) {
		ShapeBasedRecipeBuilder.shaped(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.CHEST_ITEM), woodType))
				.pattern("PPP")
				.pattern("PRP")
				.pattern("PPP")
				.define('P', planks)
				.define('R', Blocks.REDSTONE_TORCH)
				.unlockedBy("has_" + woodType.name() + PLANK_SUFFIX, has(planks))
				.save(consumer, SophisticatedStorage.getRL(woodType.name() + "_chest"));
	}

	private void tintedShulkerBoxRecipe(Consumer<FinishedRecipe> consumer, Block vanillaShulkerBox, DyeColor dyeColor) {
		//noinspection ConstantConditions
		String vanillaShulkerBoxName = BuiltInRegistries.BLOCK.getKey(vanillaShulkerBox).getPath();
		ShapelessBasedRecipeBuilder.shapeless(ModBlocks.SHULKER_BOX.getTintedStack(dyeColor)).requires(vanillaShulkerBox).requires(Items.REDSTONE_TORCH)
				.unlockedBy("has_" + vanillaShulkerBoxName, has(vanillaShulkerBox))
				.save(consumer, SophisticatedStorage.getRL(vanillaShulkerBoxName + "_to_sophisticated"));
	}
}
