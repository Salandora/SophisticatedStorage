package net.p3pp3rf1y.sophisticatedstorage.init;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.dispenser.ShulkerBoxDispenseBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.p3pp3rf1y.sophisticatedcore.util.BlockItemBase;
import net.p3pp3rf1y.sophisticatedstorage.Config;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.*;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.LimitedBarrelContainerMenu;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.LimitedBarrelSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedstorage.crafting.*;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.ChestBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.ShulkerBoxItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class ModBlocks {
	public final static Map<ResourceLocation, Block> BLOCKS = new LinkedHashMap<>(); // Must be up here!
	public final static Map<ResourceLocation, Item> ITEMS = new LinkedHashMap<>(); // Must be up here!

	private static final String LIMITED_BARREL_NAME = "limited_barrel";

	private ModBlocks() {
	}

	public static final TagKey<Item> BASE_TIER_WOODEN_STORAGE_TAG = TagKey.create(Registries.ITEM, SophisticatedStorage.getRL("base_tier_wooden_storage"));

	public static Collection<Block> getBlocksByPredicate(BiPredicate<ResourceLocation, Block> matches) {
		List<Block> blocks = new ArrayList<>();
		for (var pair : BLOCKS.entrySet()) {
			if (matches.test(pair.getKey(), pair.getValue())) {
				blocks.add(pair.getValue());
			}
		}
		return blocks;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// BLOCKS & ITEMS
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static final String BARREL_REG_NAME = "barrel";
	public static final BarrelBlock BARREL = register(BARREL_REG_NAME, () -> new BarrelBlock(Config.SERVER.woodBarrel.inventorySlotCount, Config.SERVER.woodBarrel.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock COPPER_BARREL = register("copper_barrel", () -> new BarrelBlock(Config.SERVER.copperBarrel.inventorySlotCount, Config.SERVER.copperBarrel.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock IRON_BARREL = register("iron_barrel", () -> new BarrelBlock(Config.SERVER.ironBarrel.inventorySlotCount, Config.SERVER.ironBarrel.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock GOLD_BARREL = register("gold_barrel", () -> new BarrelBlock(Config.SERVER.goldBarrel.inventorySlotCount, Config.SERVER.goldBarrel.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock DIAMOND_BARREL = register("diamond_barrel", () -> new BarrelBlock(Config.SERVER.diamondBarrel.inventorySlotCount, Config.SERVER.diamondBarrel.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock NETHERITE_BARREL = register("netherite_barrel", () -> new BarrelBlock(Config.SERVER.netheriteBarrel.inventorySlotCount, Config.SERVER.netheriteBarrel.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F, 1200).sound(SoundType.WOOD)));
	public static final BlockItem BARREL_ITEM = registerItem(BARREL_REG_NAME, () -> new BarrelBlockItem(BARREL));
	public static final BlockItem COPPER_BARREL_ITEM = registerItem("copper_barrel", () -> new BarrelBlockItem(COPPER_BARREL));
	public static final BlockItem IRON_BARREL_ITEM = registerItem("iron_barrel", () -> new BarrelBlockItem(IRON_BARREL));
	public static final BlockItem GOLD_BARREL_ITEM = registerItem("gold_barrel", () -> new BarrelBlockItem(GOLD_BARREL));
	public static final BlockItem DIAMOND_BARREL_ITEM = registerItem("diamond_barrel", () -> new BarrelBlockItem(DIAMOND_BARREL));
	public static final BlockItem NETHERITE_BARREL_ITEM = registerItem("netherite_barrel", () -> new BarrelBlockItem(NETHERITE_BARREL, new Properties().fireResistant()));

	public static final Block[] BARRELS = new Block[] {
			BARREL, COPPER_BARREL, IRON_BARREL, GOLD_BARREL, DIAMOND_BARREL, NETHERITE_BARREL
	};
	public static final BlockItem[] BARREL_ITEMS = new BlockItem[] {
			BARREL_ITEM, COPPER_BARREL_ITEM, IRON_BARREL_ITEM, GOLD_BARREL_ITEM, DIAMOND_BARREL_ITEM, NETHERITE_BARREL_ITEM
	};

	private static final String LIMITED_BARREL_REG_NAME = LIMITED_BARREL_NAME;
	public static final BarrelBlock LIMITED_BARREL_1 = register("limited_barrel_1", () -> new LimitedBarrelBlock(1, Config.SERVER.limitedBarrel1.baseSlotLimitMultiplier, Config.SERVER.limitedBarrel1.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_COPPER_BARREL_1 = register("limited_copper_barrel_1", () -> new LimitedBarrelBlock(1, Config.SERVER.copperLimitedBarrel1.baseSlotLimitMultiplier, Config.SERVER.copperLimitedBarrel1.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_IRON_BARREL_1 = register("limited_iron_barrel_1", () -> new LimitedBarrelBlock(1, Config.SERVER.ironLimitedBarrel1.baseSlotLimitMultiplier, Config.SERVER.ironLimitedBarrel1.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_GOLD_BARREL_1 = register("limited_gold_barrel_1", () -> new LimitedBarrelBlock(1, Config.SERVER.goldLimitedBarrel1.baseSlotLimitMultiplier, Config.SERVER.goldLimitedBarrel1.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_DIAMOND_BARREL_1 = register("limited_diamond_barrel_1", () -> new LimitedBarrelBlock(1, Config.SERVER.diamondLimitedBarrel1.baseSlotLimitMultiplier, Config.SERVER.diamondLimitedBarrel1.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_NETHERITE_BARREL_1 = register("limited_netherite_barrel_1", () -> new LimitedBarrelBlock(1, Config.SERVER.netheriteLimitedBarrel1.baseSlotLimitMultiplier, Config.SERVER.netheriteLimitedBarrel1.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F, 1200).sound(SoundType.WOOD)));
	public static final BlockItem LIMITED_BARREL_1_ITEM = registerItem("limited_barrel_1", () -> new BarrelBlockItem(LIMITED_BARREL_1));
	public static final BlockItem LIMITED_IRON_BARREL_1_ITEM = registerItem("limited_iron_barrel_1", () -> new BarrelBlockItem(LIMITED_IRON_BARREL_1));
	public static final BlockItem LIMITED_COPPER_BARREL_1_ITEM = registerItem("limited_copper_barrel_1", () -> new BarrelBlockItem(LIMITED_COPPER_BARREL_1));
	public static final BlockItem LIMITED_GOLD_BARREL_1_ITEM = registerItem("limited_gold_barrel_1", () -> new BarrelBlockItem(LIMITED_GOLD_BARREL_1));
	public static final BlockItem LIMITED_DIAMOND_BARREL_1_ITEM = registerItem("limited_diamond_barrel_1", () -> new BarrelBlockItem(LIMITED_DIAMOND_BARREL_1));
	public static final BlockItem LIMITED_NETHERITE_BARREL_1_ITEM = registerItem("limited_netherite_barrel_1", () -> new BarrelBlockItem(LIMITED_NETHERITE_BARREL_1, new Properties().fireResistant()));

	public static final BarrelBlock LIMITED_BARREL_2 = register("limited_barrel_2", () -> new LimitedBarrelBlock(2, Config.SERVER.limitedBarrel2.baseSlotLimitMultiplier, Config.SERVER.limitedBarrel2.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_COPPER_BARREL_2 = register("limited_copper_barrel_2", () -> new LimitedBarrelBlock(2, Config.SERVER.copperLimitedBarrel2.baseSlotLimitMultiplier, Config.SERVER.copperLimitedBarrel2.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_IRON_BARREL_2 = register("limited_iron_barrel_2", () -> new LimitedBarrelBlock(2, Config.SERVER.ironLimitedBarrel2.baseSlotLimitMultiplier, Config.SERVER.ironLimitedBarrel2.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_GOLD_BARREL_2 = register("limited_gold_barrel_2", () -> new LimitedBarrelBlock(2, Config.SERVER.goldLimitedBarrel2.baseSlotLimitMultiplier, Config.SERVER.goldLimitedBarrel2.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_DIAMOND_BARREL_2 = register("limited_diamond_barrel_2", () -> new LimitedBarrelBlock(2, Config.SERVER.diamondLimitedBarrel2.baseSlotLimitMultiplier, Config.SERVER.diamondLimitedBarrel2.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_NETHERITE_BARREL_2 = register("limited_netherite_barrel_2", () -> new LimitedBarrelBlock(2, Config.SERVER.netheriteLimitedBarrel2.baseSlotLimitMultiplier, Config.SERVER.netheriteLimitedBarrel2.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F, 1200).sound(SoundType.WOOD)));
	public static final BlockItem LIMITED_BARREL_2_ITEM = registerItem("limited_barrel_2", () -> new BarrelBlockItem(LIMITED_BARREL_2));
	public static final BlockItem LIMITED_COPPER_BARREL_2_ITEM = registerItem("limited_copper_barrel_2", () -> new BarrelBlockItem(LIMITED_COPPER_BARREL_2));
	public static final BlockItem LIMITED_IRON_BARREL_2_ITEM = registerItem("limited_iron_barrel_2", () -> new BarrelBlockItem(LIMITED_IRON_BARREL_2));
	public static final BlockItem LIMITED_GOLD_BARREL_2_ITEM = registerItem("limited_gold_barrel_2", () -> new BarrelBlockItem(LIMITED_GOLD_BARREL_2));
	public static final BlockItem LIMITED_DIAMOND_BARREL_2_ITEM = registerItem("limited_diamond_barrel_2", () -> new BarrelBlockItem(LIMITED_DIAMOND_BARREL_2));
	public static final BlockItem LIMITED_NETHERITE_BARREL_2_ITEM = registerItem("limited_netherite_barrel_2", () -> new BarrelBlockItem(LIMITED_NETHERITE_BARREL_2, new Properties().fireResistant()));

	public static final BarrelBlock LIMITED_BARREL_3 = register("limited_barrel_3", () -> new LimitedBarrelBlock(3, Config.SERVER.limitedBarrel3.baseSlotLimitMultiplier, Config.SERVER.limitedBarrel3.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_COPPER_BARREL_3 = register("limited_copper_barrel_3", () -> new LimitedBarrelBlock(3, Config.SERVER.copperLimitedBarrel3.baseSlotLimitMultiplier, Config.SERVER.copperLimitedBarrel3.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_IRON_BARREL_3 = register("limited_iron_barrel_3", () -> new LimitedBarrelBlock(3, Config.SERVER.ironLimitedBarrel3.baseSlotLimitMultiplier, Config.SERVER.ironLimitedBarrel3.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_GOLD_BARREL_3 = register("limited_gold_barrel_3", () -> new LimitedBarrelBlock(3, Config.SERVER.goldLimitedBarrel3.baseSlotLimitMultiplier, Config.SERVER.goldLimitedBarrel3.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_DIAMOND_BARREL_3 = register("limited_diamond_barrel_3", () -> new LimitedBarrelBlock(3, Config.SERVER.diamondLimitedBarrel3.baseSlotLimitMultiplier, Config.SERVER.diamondLimitedBarrel3.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_NETHERITE_BARREL_3 = register("limited_netherite_barrel_3", () -> new LimitedBarrelBlock(3, Config.SERVER.netheriteLimitedBarrel3.baseSlotLimitMultiplier, Config.SERVER.netheriteLimitedBarrel3.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F, 1200).sound(SoundType.WOOD)));
	public static final BlockItem LIMITED_BARREL_3_ITEM = registerItem("limited_barrel_3", () -> new BarrelBlockItem(LIMITED_BARREL_3));
	public static final BlockItem LIMITED_COPPER_BARREL_3_ITEM = registerItem("limited_copper_barrel_3", () -> new BarrelBlockItem(LIMITED_COPPER_BARREL_3));
	public static final BlockItem LIMITED_IRON_BARREL_3_ITEM = registerItem("limited_iron_barrel_3", () -> new BarrelBlockItem(LIMITED_IRON_BARREL_3));
	public static final BlockItem LIMITED_GOLD_BARREL_3_ITEM = registerItem("limited_gold_barrel_3", () -> new BarrelBlockItem(LIMITED_GOLD_BARREL_3));
	public static final BlockItem LIMITED_DIAMOND_BARREL_3_ITEM = registerItem("limited_diamond_barrel_3", () -> new BarrelBlockItem(LIMITED_DIAMOND_BARREL_3));
	public static final BlockItem LIMITED_NETHERITE_BARREL_3_ITEM = registerItem("limited_netherite_barrel_3", () -> new BarrelBlockItem(LIMITED_NETHERITE_BARREL_3, new Properties().fireResistant()));

	public static final BarrelBlock LIMITED_BARREL_4 = register("limited_barrel_4", () -> new LimitedBarrelBlock(4, Config.SERVER.limitedBarrel4.baseSlotLimitMultiplier, Config.SERVER.limitedBarrel4.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_COPPER_BARREL_4 = register("limited_copper_barrel_4", () -> new LimitedBarrelBlock(4, Config.SERVER.copperLimitedBarrel4.baseSlotLimitMultiplier, Config.SERVER.copperLimitedBarrel4.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_IRON_BARREL_4 = register("limited_iron_barrel_4", () -> new LimitedBarrelBlock(4, Config.SERVER.ironLimitedBarrel4.baseSlotLimitMultiplier, Config.SERVER.ironLimitedBarrel4.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_GOLD_BARREL_4 = register("limited_gold_barrel_4", () -> new LimitedBarrelBlock(4, Config.SERVER.goldLimitedBarrel4.baseSlotLimitMultiplier, Config.SERVER.goldLimitedBarrel4.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_DIAMOND_BARREL_4 = register("limited_diamond_barrel_4", () -> new LimitedBarrelBlock(4, Config.SERVER.diamondLimitedBarrel4.baseSlotLimitMultiplier, Config.SERVER.diamondLimitedBarrel4.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final BarrelBlock LIMITED_NETHERITE_BARREL_4 = register("limited_netherite_barrel_4", () -> new LimitedBarrelBlock(4, Config.SERVER.netheriteLimitedBarrel4.baseSlotLimitMultiplier, Config.SERVER.netheriteLimitedBarrel4.upgradeSlotCount,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F, 1200).sound(SoundType.WOOD)));
	public static final BlockItem LIMITED_BARREL_4_ITEM = registerItem("limited_barrel_4", () -> new BarrelBlockItem(LIMITED_BARREL_4));
	public static final BlockItem LIMITED_COPPER_BARREL_4_ITEM = registerItem("limited_copper_barrel_4", () -> new BarrelBlockItem(LIMITED_COPPER_BARREL_4));
	public static final BlockItem LIMITED_IRON_BARREL_4_ITEM = registerItem("limited_iron_barrel_4", () -> new BarrelBlockItem(LIMITED_IRON_BARREL_4));
	public static final BlockItem LIMITED_GOLD_BARREL_4_ITEM = registerItem("limited_gold_barrel_4", () -> new BarrelBlockItem(LIMITED_GOLD_BARREL_4));
	public static final BlockItem LIMITED_DIAMOND_BARREL_4_ITEM = registerItem("limited_diamond_barrel_4", () -> new BarrelBlockItem(LIMITED_DIAMOND_BARREL_4));
	public static final BlockItem LIMITED_NETHERITE_BARREL_4_ITEM = registerItem("limited_netherite_barrel_4", () -> new BarrelBlockItem(LIMITED_NETHERITE_BARREL_4, new Properties().fireResistant()));

	public static final Block[] LIMITED_BARRELS = new Block[] {
			LIMITED_BARREL_1, LIMITED_COPPER_BARREL_1, LIMITED_IRON_BARREL_1, LIMITED_GOLD_BARREL_1, LIMITED_DIAMOND_BARREL_1, LIMITED_NETHERITE_BARREL_1,
			LIMITED_BARREL_2, LIMITED_COPPER_BARREL_2, LIMITED_IRON_BARREL_2, LIMITED_GOLD_BARREL_2, LIMITED_DIAMOND_BARREL_2, LIMITED_NETHERITE_BARREL_2,
			LIMITED_BARREL_3, LIMITED_COPPER_BARREL_3, LIMITED_IRON_BARREL_3, LIMITED_GOLD_BARREL_3, LIMITED_DIAMOND_BARREL_3, LIMITED_NETHERITE_BARREL_3,
			LIMITED_BARREL_4, LIMITED_COPPER_BARREL_4, LIMITED_IRON_BARREL_4, LIMITED_GOLD_BARREL_4, LIMITED_DIAMOND_BARREL_4, LIMITED_NETHERITE_BARREL_4
	};
	public static final BlockItem[] LIMITED_BARREL_ITEMS = new BlockItem[] {
			LIMITED_BARREL_1_ITEM, LIMITED_COPPER_BARREL_1_ITEM, LIMITED_IRON_BARREL_1_ITEM, LIMITED_GOLD_BARREL_1_ITEM, LIMITED_DIAMOND_BARREL_1_ITEM, LIMITED_NETHERITE_BARREL_1_ITEM,
			LIMITED_BARREL_2_ITEM, LIMITED_COPPER_BARREL_2_ITEM, LIMITED_IRON_BARREL_2_ITEM, LIMITED_GOLD_BARREL_2_ITEM, LIMITED_DIAMOND_BARREL_2_ITEM, LIMITED_NETHERITE_BARREL_2_ITEM,
			LIMITED_BARREL_3_ITEM, LIMITED_COPPER_BARREL_3_ITEM, LIMITED_IRON_BARREL_3_ITEM, LIMITED_GOLD_BARREL_3_ITEM, LIMITED_DIAMOND_BARREL_3_ITEM, LIMITED_NETHERITE_BARREL_3_ITEM,
			LIMITED_BARREL_4_ITEM, LIMITED_COPPER_BARREL_4_ITEM, LIMITED_IRON_BARREL_4_ITEM, LIMITED_GOLD_BARREL_4_ITEM, LIMITED_DIAMOND_BARREL_4_ITEM, LIMITED_NETHERITE_BARREL_4_ITEM
	};

	public static final Block[] ALL_BARRELS = ArrayUtils.addAll(BARRELS, LIMITED_BARRELS);
	public static final BlockItem[] ALL_BARREL_ITEMS = ArrayUtils.addAll(BARREL_ITEMS, LIMITED_BARREL_ITEMS);

	private static final String CHEST_REG_NAME = "chest";
	public static final ChestBlock CHEST = register(CHEST_REG_NAME, () -> new ChestBlock(Config.SERVER.woodChest.inventorySlotCount, Config.SERVER.woodChest.upgradeSlotCount));
	public static final ChestBlock COPPER_CHEST = register("copper_chest", () -> new ChestBlock(Config.SERVER.copperChest.inventorySlotCount, Config.SERVER.copperChest.upgradeSlotCount));
	public static final ChestBlock IRON_CHEST = register("iron_chest", () -> new ChestBlock(Config.SERVER.ironChest.inventorySlotCount, Config.SERVER.ironChest.upgradeSlotCount));
	public static final ChestBlock GOLD_CHEST = register("gold_chest", () -> new ChestBlock(Config.SERVER.goldChest.inventorySlotCount, Config.SERVER.goldChest.upgradeSlotCount));
	public static final ChestBlock DIAMOND_CHEST = register("diamond_chest", () -> new ChestBlock(Config.SERVER.diamondChest.inventorySlotCount, Config.SERVER.diamondChest.upgradeSlotCount));
	public static final ChestBlock NETHERITE_CHEST = register("netherite_chest", () -> new ChestBlock(Config.SERVER.netheriteChest.inventorySlotCount, Config.SERVER.netheriteChest.upgradeSlotCount, 1200));
	public static final BlockItem CHEST_ITEM = registerItem(CHEST_REG_NAME, () -> new ChestBlockItem(CHEST));
	public static final BlockItem COPPER_CHEST_ITEM = registerItem("copper_chest", () -> new ChestBlockItem(COPPER_CHEST));
	public static final BlockItem IRON_CHEST_ITEM = registerItem("iron_chest", () -> new ChestBlockItem(IRON_CHEST));
	public static final BlockItem GOLD_CHEST_ITEM = registerItem("gold_chest", () -> new ChestBlockItem(GOLD_CHEST));
	public static final BlockItem DIAMOND_CHEST_ITEM = registerItem("diamond_chest", () -> new ChestBlockItem(DIAMOND_CHEST));
	public static final BlockItem NETHERITE_CHEST_ITEM = registerItem("netherite_chest", () -> new ChestBlockItem(NETHERITE_CHEST, new Properties().fireResistant()));

	public static final Block[] CHESTS = new Block[] {
			CHEST, COPPER_CHEST, IRON_CHEST, GOLD_CHEST, DIAMOND_CHEST, NETHERITE_CHEST
	};
	public static final BlockItem[] CHEST_ITEMS = new BlockItem[] {
			CHEST_ITEM, COPPER_CHEST_ITEM, IRON_CHEST_ITEM, GOLD_CHEST_ITEM, DIAMOND_CHEST_ITEM, NETHERITE_CHEST_ITEM
	};

	public static final Block[] ALL_WOODSTORAGES = ArrayUtils.addAll(ALL_BARRELS, CHESTS);
	public static final BlockItem[] ALL_WOODSTORAGE_ITEMS = ArrayUtils.addAll(ALL_BARREL_ITEMS, CHEST_ITEMS);

	private static final String SHULKER_BOX_REG_NAME = "shulker_box";
	public static final ShulkerBoxBlock SHULKER_BOX = register(SHULKER_BOX_REG_NAME, () -> new ShulkerBoxBlock(Config.SERVER.shulkerBox.inventorySlotCount, Config.SERVER.shulkerBox.upgradeSlotCount));
	public static final ShulkerBoxBlock COPPER_SHULKER_BOX = register("copper_shulker_box", () -> new ShulkerBoxBlock(Config.SERVER.copperShulkerBox.inventorySlotCount, Config.SERVER.copperShulkerBox.upgradeSlotCount));
	public static final ShulkerBoxBlock IRON_SHULKER_BOX = register("iron_shulker_box", () -> new ShulkerBoxBlock(Config.SERVER.ironShulkerBox.inventorySlotCount, Config.SERVER.ironShulkerBox.upgradeSlotCount));
	public static final ShulkerBoxBlock GOLD_SHULKER_BOX = register("gold_shulker_box", () -> new ShulkerBoxBlock(Config.SERVER.goldShulkerBox.inventorySlotCount, Config.SERVER.goldShulkerBox.upgradeSlotCount));
	public static final ShulkerBoxBlock DIAMOND_SHULKER_BOX = register("diamond_shulker_box", () -> new ShulkerBoxBlock(Config.SERVER.diamondShulkerBox.inventorySlotCount, Config.SERVER.diamondShulkerBox.upgradeSlotCount));
	public static final ShulkerBoxBlock NETHERITE_SHULKER_BOX = register("netherite_shulker_box", () -> new ShulkerBoxBlock(Config.SERVER.netheriteShulkerBox.inventorySlotCount, Config.SERVER.netheriteShulkerBox.upgradeSlotCount, 1200));
	public static final BlockItem SHULKER_BOX_ITEM = registerItem(SHULKER_BOX_REG_NAME, () -> new ShulkerBoxItem(SHULKER_BOX));
	public static final BlockItem COPPER_SHULKER_BOX_ITEM = registerItem("copper_shulker_box", () -> new ShulkerBoxItem(COPPER_SHULKER_BOX));
	public static final BlockItem IRON_SHULKER_BOX_ITEM = registerItem("iron_shulker_box", () -> new ShulkerBoxItem(IRON_SHULKER_BOX));
	public static final BlockItem GOLD_SHULKER_BOX_ITEM = registerItem("gold_shulker_box", () -> new ShulkerBoxItem(GOLD_SHULKER_BOX));
	public static final BlockItem DIAMOND_SHULKER_BOX_ITEM = registerItem("diamond_shulker_box", () -> new ShulkerBoxItem(DIAMOND_SHULKER_BOX));
	public static final BlockItem NETHERITE_SHULKER_BOX_ITEM = registerItem("netherite_shulker_box", () -> new ShulkerBoxItem(NETHERITE_SHULKER_BOX, new Properties().stacksTo(1).fireResistant()));

	public static final Block[] SHULKER_BOXES = new Block[] {
			SHULKER_BOX, COPPER_SHULKER_BOX, IRON_SHULKER_BOX, GOLD_SHULKER_BOX, DIAMOND_SHULKER_BOX, NETHERITE_SHULKER_BOX
	};
	public static final BlockItem[] SHULKER_BOX_ITEMS = new BlockItem[] {
			SHULKER_BOX_ITEM, COPPER_SHULKER_BOX_ITEM, IRON_SHULKER_BOX_ITEM, GOLD_SHULKER_BOX_ITEM, DIAMOND_SHULKER_BOX_ITEM, NETHERITE_SHULKER_BOX_ITEM
	};

	public static final Block[] ALL_STORAGECONTAINERS = ArrayUtils.addAll(ModBlocks.ALL_WOODSTORAGES, ModBlocks.SHULKER_BOXES);
	public static final BlockItem[] ALL_STORAGECONTAINER_ITEMS = ArrayUtils.addAll(ModBlocks.ALL_WOODSTORAGE_ITEMS, ModBlocks.SHULKER_BOX_ITEMS);


	private static final String CONTROLLER_REG_NAME = "controller";
	public static final ControllerBlock CONTROLLER = register(CONTROLLER_REG_NAME, ControllerBlock::new);
	private static final String STORAGE_LINK_REG_NAME = "storage_link";
	public static final StorageLinkBlock STORAGE_LINK = register(STORAGE_LINK_REG_NAME, StorageLinkBlock::new);
	public static final BlockItemBase CONTROLLER_ITEM = registerItem(CONTROLLER_REG_NAME, () -> new BlockItemBase(CONTROLLER, new Properties()));
	public static final BlockItemBase STORAGE_LINK_ITEM = registerItem(STORAGE_LINK_REG_NAME, () -> new BlockItemBase(STORAGE_LINK, new Properties()));
	public static final String STORAGE_IO_REG_NAME = "storage_io";
	public static final StorageIOBlock STORAGE_IO = register(STORAGE_IO_REG_NAME, StorageIOBlock::new);
	public static final String STORAGE_INPUT_REG_NAME = "storage_input";
	public static final StorageIOBlock STORAGE_INPUT = register(STORAGE_INPUT_REG_NAME, () -> new StorageIOBlock() {
		@Override
		public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
			return new StorageInputBlockEntity(pos, state);
		}
	});
	public static final String STORAGE_OUTPUT_REG_NAME = "storage_output";
	public static final StorageIOBlock STORAGE_OUTPUT = register(STORAGE_OUTPUT_REG_NAME, () -> new StorageIOBlock() {
		@Override
		public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
			return new StorageOutputBlockEntity(pos, state);
		}
	});

	public static final BlockItem STORAGE_IO_ITEM = registerItem(STORAGE_IO_REG_NAME, () -> new BlockItemBase(STORAGE_IO, new Item.Properties()));
	public static final BlockItem STORAGE_INPUT_ITEM = registerItem(STORAGE_INPUT_REG_NAME, () -> new BlockItemBase(STORAGE_INPUT, new Item.Properties()));
	public static final BlockItem STORAGE_OUTPUT_ITEM = registerItem(STORAGE_OUTPUT_REG_NAME, () -> new BlockItemBase(STORAGE_OUTPUT, new Item.Properties()));


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// BLOCK_ENTITY_TYPES
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("ConstantConditions") //no datafixer type needed
	public static final BlockEntityType<BarrelBlockEntity> BARREL_BLOCK_ENTITY_TYPE = registerEntityType(BARREL_REG_NAME, () ->
			BlockEntityType.Builder.of(BarrelBlockEntity::new, BARRELS)
					.build(null));

	@SuppressWarnings("ConstantConditions") //no datafixer type needed
	public static final BlockEntityType<LimitedBarrelBlockEntity> LIMITED_BARREL_BLOCK_ENTITY_TYPE = registerEntityType(LIMITED_BARREL_REG_NAME, () ->
			BlockEntityType.Builder.of(LimitedBarrelBlockEntity::new, LIMITED_BARRELS)
					.build(null));

	@SuppressWarnings("ConstantConditions") //no datafixer type needed
	public static final BlockEntityType<ChestBlockEntity> CHEST_BLOCK_ENTITY_TYPE = registerEntityType(CHEST_REG_NAME, () ->
			BlockEntityType.Builder.of(ChestBlockEntity::new, CHESTS)
					.build(null));

	@SuppressWarnings("ConstantConditions") //no datafixer type needed
	public static final BlockEntityType<ShulkerBoxBlockEntity> SHULKER_BOX_BLOCK_ENTITY_TYPE = registerEntityType(SHULKER_BOX_REG_NAME, () ->
			BlockEntityType.Builder.of(ShulkerBoxBlockEntity::new, SHULKER_BOXES)
					.build(null));

	@SuppressWarnings("ConstantConditions") //no datafixer type needed
	public static final BlockEntityType<ControllerBlockEntity> CONTROLLER_BLOCK_ENTITY_TYPE = registerEntityType(CONTROLLER_REG_NAME, () ->
			BlockEntityType.Builder.of(ControllerBlockEntity::new, CONTROLLER)
					.build(null));

	@SuppressWarnings("ConstantConditions") //no datafixer type needed
	public static final BlockEntityType<StorageLinkBlockEntity> STORAGE_LINK_BLOCK_ENTITY_TYPE = registerEntityType(STORAGE_LINK_REG_NAME, () ->
			BlockEntityType.Builder.of(StorageLinkBlockEntity::new, STORAGE_LINK)
					.build(null));

	@SuppressWarnings("ConstantConditions") //no datafixer type needed
	public static final BlockEntityType<StorageIOBlockEntity> STORAGE_IO_BLOCK_ENTITY_TYPE = registerEntityType(STORAGE_IO_REG_NAME, () ->
			BlockEntityType.Builder.of(StorageIOBlockEntity::new, STORAGE_IO)
					.build(null));

	@SuppressWarnings("ConstantConditions") //no datafixer type needed
	public static final BlockEntityType<StorageInputBlockEntity> STORAGE_INPUT_BLOCK_ENTITY_TYPE = registerEntityType(STORAGE_INPUT_REG_NAME, () ->
			BlockEntityType.Builder.of(StorageInputBlockEntity::new, STORAGE_INPUT)
					.build(null));

	@SuppressWarnings("ConstantConditions") //no datafixer type needed
	public static final BlockEntityType<StorageOutputBlockEntity> STORAGE_OUTPUT_BLOCK_ENTITY_TYPE = registerEntityType(STORAGE_OUTPUT_REG_NAME, () ->
			BlockEntityType.Builder.of(StorageOutputBlockEntity::new, STORAGE_OUTPUT)
					.build(null));

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// MENU_TYPES
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final MenuType<StorageContainerMenu> STORAGE_CONTAINER_TYPE = registerMenuType("storage",
			() -> new ExtendedScreenHandlerType<>(StorageContainerMenu::fromBuffer));

	public static final MenuType<LimitedBarrelContainerMenu> LIMITED_BARREL_CONTAINER_TYPE = registerMenuType(LIMITED_BARREL_NAME,
			() -> new ExtendedScreenHandlerType<>(LimitedBarrelContainerMenu::fromBuffer));

	public static final MenuType<StorageSettingsContainerMenu> SETTINGS_CONTAINER_TYPE = registerMenuType("settings",
			() -> new ExtendedScreenHandlerType<>(StorageSettingsContainerMenu::fromBuffer));

	public static final MenuType<LimitedBarrelSettingsContainerMenu> LIMITED_BARREL_SETTINGS_CONTAINER_TYPE = registerMenuType("limited_barrel_settings",
			() -> new ExtendedScreenHandlerType<>(LimitedBarrelSettingsContainerMenu::fromBuffer));


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// RECIPE_SERIALIZERS
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final SimpleCraftingRecipeSerializer<?> STORAGE_DYE_RECIPE_SERIALIZER = registerRecipeSerializer("storage_dye", () -> new SimpleCraftingRecipeSerializer<>(StorageDyeRecipe::new));
	public static final RecipeSerializer<?> STORAGE_TIER_UPGRADE_RECIPE_SERIALIZER = registerRecipeSerializer("storage_tier_upgrade", StorageTierUpgradeRecipe.Serializer::new);
	public static final RecipeSerializer<?> STORAGE_TIER_UPGRADE_SHAPELESS_RECIPE_SERIALIZER = registerRecipeSerializer("storage_tier_upgrade_shapeless", StorageTierUpgradeShapelessRecipe.Serializer::new);
	public static final RecipeSerializer<?> SHULKER_BOX_FROM_CHEST_RECIPE_SERIALIZER = registerRecipeSerializer("shulker_box_from_chest", ShulkerBoxFromChestRecipe.Serializer::new);
	public static final SimpleCraftingRecipeSerializer<?> FLAT_TOP_BARREL_TOGGLE_RECIPE_SERIALIZER = registerRecipeSerializer("flat_top_barrel_toggle", () -> new SimpleCraftingRecipeSerializer<>(FlatTopBarrelToggleRecipe::new));
	public static final SimpleCraftingRecipeSerializer<?> BARREL_MATERIAL_RECIPE_SERIALIZER = registerRecipeSerializer("barrel_material", () -> new SimpleCraftingRecipeSerializer<>(BarrelMaterialRecipe::new));

	// Register
	public static <T extends Block> T register(String id, Supplier<T> supplier) {
		T block = supplier.get();
		BLOCKS.put(SophisticatedStorage.getRL(id), block);
		return Registry.register(BuiltInRegistries.BLOCK, SophisticatedStorage.getRL(id), block);
	}
	public static <T extends Item> T registerItem(String id, Supplier<T> supplier) {
		T item = supplier.get();
		ITEMS.put(SophisticatedStorage.getRL(id), item);
		return Registry.register(BuiltInRegistries.ITEM, SophisticatedStorage.getRL(id), item);
	}
	public static <T extends MenuType<?>> T registerMenuType(String id, Supplier<T> supplier) {
		return Registry.register(BuiltInRegistries.MENU, SophisticatedStorage.getRL(id), supplier.get());
	}
	public static <T extends BlockEntityType<?>> T registerEntityType(String id, Supplier<T> supplier) {
		return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SophisticatedStorage.getRL(id), supplier.get());
	}
	public static <T extends RecipeSerializer<?>> T registerRecipeSerializer(String id, Supplier<T> supplier) {
		return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, SophisticatedStorage.getRL(id), supplier.get());
	}

	public static void register() {
		registerDispenseBehavior();
		registerCauldronInteractions();
		registerRecipeSerializers();
		registerCapabilities();
	}

	private static void registerCapabilities() {
		ItemStorage.SIDED.registerForBlockEntity(ControllerBlockEntity::getExternalItemHandler, CONTROLLER_BLOCK_ENTITY_TYPE);
		ItemStorage.SIDED.registerForBlockEntity(ChestBlockEntity::getExternalItemHandler, CHEST_BLOCK_ENTITY_TYPE);
		ItemStorage.SIDED.registerForBlockEntity(ShulkerBoxBlockEntity::getExternalItemHandler, SHULKER_BOX_BLOCK_ENTITY_TYPE);
		ItemStorage.SIDED.registerForBlockEntity(LimitedBarrelBlockEntity::getExternalItemHandler, LIMITED_BARREL_BLOCK_ENTITY_TYPE);
		ItemStorage.SIDED.registerForBlockEntity(BarrelBlockEntity::getExternalItemHandler, BARREL_BLOCK_ENTITY_TYPE);
		ItemStorage.SIDED.registerForBlockEntity(StorageIOBlockEntity::getExternalItemHandler, STORAGE_IO_BLOCK_ENTITY_TYPE);
		ItemStorage.SIDED.registerForBlockEntity(StorageInputBlockEntity::getExternalItemHandler, STORAGE_INPUT_BLOCK_ENTITY_TYPE);
		ItemStorage.SIDED.registerForBlockEntity(StorageOutputBlockEntity::getExternalItemHandler, STORAGE_OUTPUT_BLOCK_ENTITY_TYPE);
	}

	private static void registerRecipeSerializers() {
		CustomIngredientSerializer.register(BaseTierWoodenStorageIngredient.Serializer.INSTANCE);
	}

	private static void registerDispenseBehavior() {
		DispenserBlock.registerBehavior(SHULKER_BOX_ITEM, new ShulkerBoxDispenseBehavior());
	}

	private static void registerCauldronInteractions() {
		for (BlockItem item : ALL_BARREL_ITEMS) {
			CauldronInteraction.WATER.map().put(item, BarrelCauldronInteraction.INSTANCE);
		}

		for (BlockItem item : CHEST_ITEMS) {
			CauldronInteraction.WATER.map().put(item, WoodStorageCauldronInteraction.INSTANCE);
		}

		for (BlockItem item : SHULKER_BOX_ITEMS) {
			CauldronInteraction.WATER.map().put(item, StorageCauldronInteraction.INSTANCE);
		}
	}

	@SuppressWarnings("java:S6548") //singleton is correct here
	public static class BarrelCauldronInteraction extends WoodStorageCauldronInteraction {
		private static final BarrelCauldronInteraction INSTANCE = new BarrelCauldronInteraction();

		@Override
		protected void removePaint(ItemStack stack) {
			super.removePaint(stack);
			BarrelBlockItem.removeMaterials(stack);
		}
	}

	@SuppressWarnings("java:S6548") //singleton is correct here
	public static class WoodStorageCauldronInteraction extends StorageCauldronInteraction {
		private static final WoodStorageCauldronInteraction INSTANCE = new WoodStorageCauldronInteraction();

		@Override
		protected void removePaint(ItemStack stack) {
			super.removePaint(stack);
			if (WoodStorageBlockItem.getWoodType(stack).isEmpty()) {
				WoodStorageBlockItem.setWoodType(stack, WoodType.ACACIA);
			}
		}

		@Override
		protected boolean canRemovePaint(ItemStack stack) {
			return super.canRemovePaint(stack) && !WoodStorageBlockItem.isPacked(stack);
		}
	}

	@SuppressWarnings("java:S6548") //singleton is correct here
	public static class StorageCauldronInteraction implements CauldronInteraction {
		private static final StorageCauldronInteraction INSTANCE = new StorageCauldronInteraction();

		@Override
		public InteractionResult interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
			if (canRemovePaint(stack)) {

				if (!level.isClientSide()) {
					removePaint(stack);
				}
				return InteractionResult.sidedSuccess(level.isClientSide);
			}
			return InteractionResult.PASS;
		}

		protected boolean canRemovePaint(ItemStack stack) {
			return stack.getItem() instanceof ITintableBlockItem;
		}

		protected void removePaint(ItemStack stack) {
			if (stack.getItem() instanceof ITintableBlockItem tintableBlockItem) {
				tintableBlockItem.removeMainColor(stack);
				tintableBlockItem.removeAccentColor(stack);
			}
		}
	}
}
