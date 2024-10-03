package net.p3pp3rf1y.sophisticatedstorage.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerRegistry;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerType;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ContentsFilteredUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeCountLimitConfig;
import net.p3pp3rf1y.sophisticatedcore.upgrades.battery.BatteryUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.battery.BatteryUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.compacting.CompactingUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.compacting.CompactingUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.compacting.CompactingUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.cooking.AutoBlastingUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.cooking.AutoCookingUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.cooking.AutoCookingUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.cooking.AutoSmeltingUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.cooking.AutoSmokingUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.cooking.BlastingUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.cooking.CookingUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.cooking.CookingUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.cooking.SmeltingUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.cooking.SmokingUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.feeding.FeedingUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.feeding.FeedingUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.feeding.FeedingUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.filter.FilterUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.filter.FilterUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.JukeboxUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.JukeboxUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.magnet.MagnetUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.magnet.MagnetUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.magnet.MagnetUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.pickup.PickupUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.pickup.PickupUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.pump.PumpUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.pump.PumpUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.pump.PumpUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stonecutter.StonecutterUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stonecutter.StonecutterUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stonecutter.StonecutterUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.tank.TankUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.tank.TankUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.voiding.VoidUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.voiding.VoidUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.voiding.VoidUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.xppump.XpPumpUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.xppump.XpPumpUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.xppump.XpPumpUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.util.BlockItemBase;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;
import net.p3pp3rf1y.sophisticatedstorage.Config;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorage.crafting.DropPackedDisabledCondition;
import net.p3pp3rf1y.sophisticatedstorage.data.CopyStorageDataFunction;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageTierUpgradeItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageToolItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.upgrades.compression.CompressionUpgradeItem;
import net.p3pp3rf1y.sophisticatedstorage.upgrades.hopper.HopperUpgradeContainer;
import net.p3pp3rf1y.sophisticatedstorage.upgrades.hopper.HopperUpgradeItem;
import net.p3pp3rf1y.sophisticatedstorage.upgrades.hopper.HopperUpgradeWrapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class ModItems {
	private ModItems() {
	}

	final static Map<ResourceLocation, Item> ITEMS = new LinkedHashMap<>(); // Must be up here!

	public static final ResourceLocation STORAGE_UPGRADE_TAG_NAME = new ResourceLocation(SophisticatedStorage.MOD_ID, "upgrade");

	public static final TagKey<Item> STORAGE_UPGRADE_TAG = TagKey.create(Registries.ITEM, STORAGE_UPGRADE_TAG_NAME);

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// ITEMS
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final PickupUpgradeItem PICKUP_UPGRADE = register("pickup_upgrade",
			() -> new PickupUpgradeItem(Config.SERVER.pickupUpgrade.filterSlots::get, Config.SERVER.maxUpgradesPerStorage));
	public static final PickupUpgradeItem ADVANCED_PICKUP_UPGRADE = register("advanced_pickup_upgrade",
			() -> new PickupUpgradeItem(Config.SERVER.advancedPickupUpgrade.filterSlots::get, Config.SERVER.maxUpgradesPerStorage));
	public static final FilterUpgradeItem FILTER_UPGRADE = register("filter_upgrade",
			() -> new FilterUpgradeItem(Config.SERVER.filterUpgrade.filterSlots::get, Config.SERVER.maxUpgradesPerStorage));
	public static final FilterUpgradeItem ADVANCED_FILTER_UPGRADE = register("advanced_filter_upgrade",
			() -> new FilterUpgradeItem(Config.SERVER.advancedFilterUpgrade.filterSlots::get, Config.SERVER.maxUpgradesPerStorage));
	public static final MagnetUpgradeItem MAGNET_UPGRADE = register("magnet_upgrade",
			() -> new MagnetUpgradeItem(Config.SERVER.magnetUpgrade.magnetRange::get, Config.SERVER.magnetUpgrade.filterSlots::get, Config.SERVER.maxUpgradesPerStorage));
	public static final MagnetUpgradeItem ADVANCED_MAGNET_UPGRADE = register("advanced_magnet_upgrade",
			() -> new MagnetUpgradeItem(Config.SERVER.advancedMagnetUpgrade.magnetRange::get, Config.SERVER.advancedMagnetUpgrade.filterSlots::get, Config.SERVER.maxUpgradesPerStorage));
	public static final FeedingUpgradeItem FEEDING_UPGRADE = register("feeding_upgrade",
			() -> new FeedingUpgradeItem(Config.SERVER.feedingUpgrade.filterSlots::get, Config.SERVER.maxUpgradesPerStorage));
	public static final FeedingUpgradeItem ADVANCED_FEEDING_UPGRADE = register("advanced_feeding_upgrade",
			() -> new FeedingUpgradeItem(Config.SERVER.advancedFeedingUpgrade.filterSlots::get, Config.SERVER.maxUpgradesPerStorage));
	public static final CompactingUpgradeItem COMPACTING_UPGRADE = register("compacting_upgrade",
			() -> new StorageCompactingUpgradeItem(false, Config.SERVER.compactingUpgrade.filterSlots::get, Config.SERVER.maxUpgradesPerStorage));
	public static final CompactingUpgradeItem ADVANCED_COMPACTING_UPGRADE = register("advanced_compacting_upgrade",
			() -> new StorageCompactingUpgradeItem(true, Config.SERVER.advancedCompactingUpgrade.filterSlots::get, Config.SERVER.maxUpgradesPerStorage));
	public static final VoidUpgradeItem VOID_UPGRADE = register("void_upgrade",
			() -> new VoidUpgradeItem(Config.SERVER.voidUpgrade, Config.SERVER.maxUpgradesPerStorage));
	public static final VoidUpgradeItem ADVANCED_VOID_UPGRADE = register("advanced_void_upgrade",
			() -> new VoidUpgradeItem(Config.SERVER.advancedVoidUpgrade, Config.SERVER.maxUpgradesPerStorage));
	public static final SmeltingUpgradeItem SMELTING_UPGRADE = register("smelting_upgrade",
			() -> new SmeltingUpgradeItem(Config.SERVER.smeltingUpgrade, Config.SERVER.maxUpgradesPerStorage));
	public static final AutoSmeltingUpgradeItem AUTO_SMELTING_UPGRADE = register("auto_smelting_upgrade",
			() -> new AutoSmeltingUpgradeItem(Config.SERVER.autoSmeltingUpgrade, Config.SERVER.maxUpgradesPerStorage));
	public static final SmokingUpgradeItem SMOKING_UPGRADE = register("smoking_upgrade",
			() -> new SmokingUpgradeItem(Config.SERVER.smokingUpgrade, Config.SERVER.maxUpgradesPerStorage));
	public static final AutoSmokingUpgradeItem AUTO_SMOKING_UPGRADE = register("auto_smoking_upgrade",
			() -> new AutoSmokingUpgradeItem(Config.SERVER.autoSmokingUpgrade, Config.SERVER.maxUpgradesPerStorage));
	public static final BlastingUpgradeItem BLASTING_UPGRADE = register("blasting_upgrade",
			() -> new BlastingUpgradeItem(Config.SERVER.blastingUpgrade, Config.SERVER.maxUpgradesPerStorage));
	public static final AutoBlastingUpgradeItem AUTO_BLASTING_UPGRADE = register("auto_blasting_upgrade",
			() -> new AutoBlastingUpgradeItem(Config.SERVER.autoBlastingUpgrade, Config.SERVER.maxUpgradesPerStorage));
	public static final CraftingUpgradeItem CRAFTING_UPGRADE = register("crafting_upgrade", () -> new CraftingUpgradeItem(Config.SERVER.maxUpgradesPerStorage));
	public static final StonecutterUpgradeItem STONECUTTER_UPGRADE = register("stonecutter_upgrade", () -> new StonecutterUpgradeItem(Config.SERVER.maxUpgradesPerStorage));
	public static final StackUpgradeItem STACK_UPGRADE_TIER_1 = register("stack_upgrade_tier_1", () ->
			new StackUpgradeItem(2, Config.SERVER.maxUpgradesPerStorage));
	public static final StackUpgradeItem STACK_UPGRADE_TIER_1_PLUS = register("stack_upgrade_tier_1_plus", () ->
			new StackUpgradeItem(3, Config.SERVER.maxUpgradesPerStorage));
	public static final StackUpgradeItem STACK_UPGRADE_TIER_2 = register("stack_upgrade_tier_2", () ->
			new StackUpgradeItem(4, Config.SERVER.maxUpgradesPerStorage));
	public static final StackUpgradeItem STACK_UPGRADE_TIER_3 = register("stack_upgrade_tier_3", () ->
			new StackUpgradeItem(8, Config.SERVER.maxUpgradesPerStorage));
	public static final StackUpgradeItem STACK_UPGRADE_TIER_4 = register("stack_upgrade_tier_4", () ->
			new StackUpgradeItem(16, Config.SERVER.maxUpgradesPerStorage));
	public static final StackUpgradeItem STACK_UPGRADE_TIER_5 = register("stack_upgrade_tier_5", () ->
			new StackUpgradeItem(32, Config.SERVER.maxUpgradesPerStorage));
	public static final String JUKEBOX_UPGRADE_NAME = "jukebox_upgrade";
	public static final JukeboxUpgradeItem JUKEBOX_UPGRADE = register(JUKEBOX_UPGRADE_NAME, () -> new JukeboxUpgradeItem(Config.SERVER.maxUpgradesPerStorage));
	public static final PumpUpgradeItem PUMP_UPGRADE = register("pump_upgrade", () -> new PumpUpgradeItem(false, false, Config.SERVER.pumpUpgrade, Config.SERVER.maxUpgradesPerStorage));
	public static final PumpUpgradeItem ADVANCED_PUMP_UPGRADE = register("advanced_pump_upgrade", () -> new PumpUpgradeItem(true, true, Config.SERVER.pumpUpgrade, Config.SERVER.maxUpgradesPerStorage));
	public static final XpPumpUpgradeItem XP_PUMP_UPGRADE = register("xp_pump_upgrade", () -> new XpPumpUpgradeItem(Config.SERVER.xpPumpUpgrade, Config.SERVER.maxUpgradesPerStorage));
	public static final CompressionUpgradeItem COMPRESSION_UPGRADE = register("compression_upgrade", CompressionUpgradeItem::new);
	public static final HopperUpgradeItem HOPPER_UPGRADE = register("hopper_upgrade", () ->
			new HopperUpgradeItem(Config.SERVER.hopperUpgrade.inputFilterSlots::get, Config.SERVER.hopperUpgrade.outputFilterSlots::get, Config.SERVER.hopperUpgrade.transferSpeedTicks::get, Config.SERVER.hopperUpgrade.maxTransferStackSize::get));
	public static final HopperUpgradeItem ADVANCED_HOPPER_UPGRADE = register("advanced_hopper_upgrade", () ->
			new HopperUpgradeItem(Config.SERVER.advancedHopperUpgrade.inputFilterSlots::get, Config.SERVER.advancedHopperUpgrade.outputFilterSlots::get, Config.SERVER.advancedHopperUpgrade.transferSpeedTicks::get, Config.SERVER.advancedHopperUpgrade.maxTransferStackSize::get));
	public static final StorageTierUpgradeItem BASIC_TIER_UPGRADE = register("basic_tier_upgrade", () -> new StorageTierUpgradeItem(StorageTierUpgradeItem.TierUpgrade.BASIC, true));
	public static final StorageTierUpgradeItem BASIC_TO_COPPER_TIER_UPGRADE = register("basic_to_copper_tier_upgrade", () -> new StorageTierUpgradeItem(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_COPPER));
	public static final StorageTierUpgradeItem BASIC_TO_IRON_TIER_UPGRADE = register("basic_to_iron_tier_upgrade", () -> new StorageTierUpgradeItem(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_IRON));
	public static final StorageTierUpgradeItem BASIC_TO_GOLD_TIER_UPGRADE = register("basic_to_gold_tier_upgrade", () -> new StorageTierUpgradeItem(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_GOLD));
	public static final StorageTierUpgradeItem BASIC_TO_DIAMOND_TIER_UPGRADE = register("basic_to_diamond_tier_upgrade", () -> new StorageTierUpgradeItem(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_DIAMOND));
	public static final StorageTierUpgradeItem BASIC_TO_NETHERITE_TIER_UPGRADE = register("basic_to_netherite_tier_upgrade", () -> new StorageTierUpgradeItem(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_NETHERITE));
	public static final StorageTierUpgradeItem COPPER_TO_IRON_TIER_UPGRADE = register("copper_to_iron_tier_upgrade", () -> new StorageTierUpgradeItem(StorageTierUpgradeItem.TierUpgrade.COPPER_TO_IRON));
	public static final StorageTierUpgradeItem COPPER_TO_GOLD_TIER_UPGRADE = register("copper_to_gold_tier_upgrade", () -> new StorageTierUpgradeItem(StorageTierUpgradeItem.TierUpgrade.COPPER_TO_GOLD));
	public static final StorageTierUpgradeItem COPPER_TO_DIAMOND_TIER_UPGRADE = register("copper_to_diamond_tier_upgrade", () -> new StorageTierUpgradeItem(StorageTierUpgradeItem.TierUpgrade.COPPER_TO_DIAMOND));
	public static final StorageTierUpgradeItem COPPER_TO_NETHERITE_TIER_UPGRADE = register("copper_to_netherite_tier_upgrade", () -> new StorageTierUpgradeItem(StorageTierUpgradeItem.TierUpgrade.COPPER_TO_NETHERITE));
	public static final StorageTierUpgradeItem IRON_TO_GOLD_TIER_UPGRADE = register("iron_to_gold_tier_upgrade", () -> new StorageTierUpgradeItem(StorageTierUpgradeItem.TierUpgrade.IRON_TO_GOLD));
	public static final StorageTierUpgradeItem IRON_TO_DIAMOND_TIER_UPGRADE = register("iron_to_diamond_tier_upgrade", () -> new StorageTierUpgradeItem(StorageTierUpgradeItem.TierUpgrade.IRON_TO_DIAMOND));
	public static final StorageTierUpgradeItem IRON_TO_NETHERITE_TIER_UPGRADE = register("iron_to_netherite_tier_upgrade", () -> new StorageTierUpgradeItem(StorageTierUpgradeItem.TierUpgrade.IRON_TO_NETHERITE));
	public static final StorageTierUpgradeItem GOLD_TO_DIAMOND_TIER_UPGRADE = register("gold_to_diamond_tier_upgrade", () -> new StorageTierUpgradeItem(StorageTierUpgradeItem.TierUpgrade.GOLD_TO_DIAMOND));
	public static final StorageTierUpgradeItem GOLD_TO_NETHERITE_TIER_UPGRADE = register("gold_to_netherite_tier_upgrade", () -> new StorageTierUpgradeItem(StorageTierUpgradeItem.TierUpgrade.GOLD_TO_NETHERITE));
	public static final StorageTierUpgradeItem DIAMOND_TO_NETHERITE_TIER_UPGRADE = register("diamond_to_netherite_tier_upgrade", () -> new StorageTierUpgradeItem(StorageTierUpgradeItem.TierUpgrade.DIAMOND_TO_NETHERITE));

	public static final ItemBase UPGRADE_BASE = register("upgrade_base", () -> new ItemBase(new Item.Properties().stacksTo(16)));

	public static final String PACKING_TAPE_NAME = "packing_tape";
	public static final ItemBase PACKING_TAPE = register(PACKING_TAPE_NAME, () -> new ItemBase(new Item.Properties().stacksTo(1).durability(8)) {
		@Override
		public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
			super.appendHoverText(stack, level, tooltip, isAdvanced);
			if (Boolean.TRUE.equals(Config.COMMON.dropPacked.get())) {
				tooltip.add(Component.translatable(StorageTranslationHelper.INSTANCE.translItemTooltip(PACKING_TAPE_NAME) + ".disabled").withStyle(ChatFormatting.RED));
			} else {
				tooltip.add(Component.translatable(StorageTranslationHelper.INSTANCE.translItemTooltip(PACKING_TAPE_NAME),
								Component.literal(String.valueOf(stack.getMaxDamage() - stack.getDamageValue())).withStyle(ChatFormatting.GREEN)
						).withStyle(ChatFormatting.DARK_GRAY)
				);
			}
		}

		@Override
		public void addCreativeTabItems(Consumer<ItemStack> itemConsumer) {
			if (!Boolean.TRUE.equals(Config.COMMON.dropPacked.get())) {
				super.addCreativeTabItems(itemConsumer);
			}
		}
	});
	public static final ItemBase STORAGE_TOOL = register("storage_tool", StorageToolItem::new);
	public static final ItemBase DEBUG_TOOL = register("debug_tool", () -> new ItemBase(new Item.Properties().stacksTo(1)));
	public static final Item INACCESSIBLE_SLOT = register("inaccessible_slot", () -> new Item(new Item.Properties().stacksTo(1)));
	public static final LootItemFunctionType COPY_STORAGE_DATA = registerLootFunction("copy_storage_data", () -> new LootItemFunctionType(new CopyStorageDataFunction.Serializer()));

	@SuppressWarnings("unused")
	public static final CreativeModeTab CREATIVE_TAB = FabricItemGroup.builder()
			.icon(() -> WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.GOLD_BARREL_ITEM), WoodType.SPRUCE))
			.title(Component.translatable("itemGroup.sophisticatedstorage"))
			.displayItems((featureFlags, output) -> {
				ModBlocks.ITEMS.values().stream().filter(i -> i instanceof BlockItemBase).forEach(i -> ((BlockItemBase) i).addCreativeTabItems(output::accept));
				ITEMS.values().stream().filter(i -> i instanceof ItemBase).forEach(i -> ((ItemBase) i).addCreativeTabItems(output::accept));
			})
			.build();


	private static void registerRecipeCondition() {
		ResourceConditions.register(DropPackedDisabledCondition.NAME, DropPackedDisabledCondition::test);
	}

	public static final UpgradeContainerType<PickupUpgradeWrapper, ContentsFilteredUpgradeContainer<PickupUpgradeWrapper>> PICKUP_BASIC_TYPE = new UpgradeContainerType<>(ContentsFilteredUpgradeContainer::new);
	public static final UpgradeContainerType<PickupUpgradeWrapper, ContentsFilteredUpgradeContainer<PickupUpgradeWrapper>> PICKUP_ADVANCED_TYPE = new UpgradeContainerType<>(ContentsFilteredUpgradeContainer::new);
	public static final UpgradeContainerType<MagnetUpgradeWrapper, MagnetUpgradeContainer> MAGNET_BASIC_TYPE = new UpgradeContainerType<>(MagnetUpgradeContainer::new);
	public static final UpgradeContainerType<MagnetUpgradeWrapper, MagnetUpgradeContainer> MAGNET_ADVANCED_TYPE = new UpgradeContainerType<>(MagnetUpgradeContainer::new);
	public static final UpgradeContainerType<FeedingUpgradeWrapper, FeedingUpgradeContainer> FEEDING_TYPE = new UpgradeContainerType<>(FeedingUpgradeContainer::new);
	public static final UpgradeContainerType<FeedingUpgradeWrapper, FeedingUpgradeContainer> ADVANCED_FEEDING_TYPE = new UpgradeContainerType<>(FeedingUpgradeContainer::new);
	public static final UpgradeContainerType<CompactingUpgradeWrapper, CompactingUpgradeContainer> COMPACTING_TYPE = new UpgradeContainerType<>(CompactingUpgradeContainer::new);
	public static final UpgradeContainerType<CompactingUpgradeWrapper, CompactingUpgradeContainer> ADVANCED_COMPACTING_TYPE = new UpgradeContainerType<>(CompactingUpgradeContainer::new);
	public static final UpgradeContainerType<VoidUpgradeWrapper, VoidUpgradeContainer> VOID_TYPE = new UpgradeContainerType<>(VoidUpgradeContainer::new);
	public static final UpgradeContainerType<VoidUpgradeWrapper, VoidUpgradeContainer> ADVANCED_VOID_TYPE = new UpgradeContainerType<>(VoidUpgradeContainer::new);
	public static final UpgradeContainerType<CookingUpgradeWrapper.SmeltingUpgradeWrapper, CookingUpgradeContainer<SmeltingRecipe, CookingUpgradeWrapper.SmeltingUpgradeWrapper>> SMELTING_TYPE = new UpgradeContainerType<>(CookingUpgradeContainer::new);
	public static final UpgradeContainerType<AutoCookingUpgradeWrapper.AutoSmeltingUpgradeWrapper, AutoCookingUpgradeContainer<SmeltingRecipe, AutoCookingUpgradeWrapper.AutoSmeltingUpgradeWrapper>> AUTO_SMELTING_TYPE = new UpgradeContainerType<>(AutoCookingUpgradeContainer::new);
	public static final UpgradeContainerType<CookingUpgradeWrapper.SmokingUpgradeWrapper, CookingUpgradeContainer<SmokingRecipe, CookingUpgradeWrapper.SmokingUpgradeWrapper>> SMOKING_TYPE = new UpgradeContainerType<>(CookingUpgradeContainer::new);
	public static final UpgradeContainerType<AutoCookingUpgradeWrapper.AutoSmokingUpgradeWrapper, AutoCookingUpgradeContainer<SmokingRecipe, AutoCookingUpgradeWrapper.AutoSmokingUpgradeWrapper>> AUTO_SMOKING_TYPE = new UpgradeContainerType<>(AutoCookingUpgradeContainer::new);
	public static final UpgradeContainerType<CookingUpgradeWrapper.BlastingUpgradeWrapper, CookingUpgradeContainer<BlastingRecipe, CookingUpgradeWrapper.BlastingUpgradeWrapper>> BLASTING_TYPE = new UpgradeContainerType<>(CookingUpgradeContainer::new);
	public static final UpgradeContainerType<AutoCookingUpgradeWrapper.AutoBlastingUpgradeWrapper, AutoCookingUpgradeContainer<BlastingRecipe, AutoCookingUpgradeWrapper.AutoBlastingUpgradeWrapper>> AUTO_BLASTING_TYPE = new UpgradeContainerType<>(AutoCookingUpgradeContainer::new);
	public static final UpgradeContainerType<CraftingUpgradeWrapper, CraftingUpgradeContainer> CRAFTING_TYPE = new UpgradeContainerType<>(CraftingUpgradeContainer::new);
	public static final UpgradeContainerType<StonecutterUpgradeWrapper, StonecutterUpgradeContainer> STONECUTTER_TYPE = new UpgradeContainerType<>(StonecutterUpgradeContainer::new);
	public static final UpgradeContainerType<JukeboxUpgradeItem.Wrapper, JukeboxUpgradeContainer> JUKEBOX_TYPE = new UpgradeContainerType<>(JukeboxUpgradeContainer::new);
	public static final UpgradeContainerType<TankUpgradeWrapper, TankUpgradeContainer> TANK_TYPE = new UpgradeContainerType<>(TankUpgradeContainer::new);
	public static final UpgradeContainerType<BatteryUpgradeWrapper, BatteryUpgradeContainer> BATTERY_TYPE = new UpgradeContainerType<>(BatteryUpgradeContainer::new);
	public static final UpgradeContainerType<PumpUpgradeWrapper, PumpUpgradeContainer> PUMP_TYPE = new UpgradeContainerType<>(PumpUpgradeContainer::new);
	public static final UpgradeContainerType<PumpUpgradeWrapper, PumpUpgradeContainer> ADVANCED_PUMP_TYPE = new UpgradeContainerType<>(PumpUpgradeContainer::new);
	public static final UpgradeContainerType<XpPumpUpgradeWrapper, XpPumpUpgradeContainer> XP_PUMP_TYPE = new UpgradeContainerType<>(XpPumpUpgradeContainer::new);
	public static final UpgradeContainerType<HopperUpgradeWrapper, HopperUpgradeContainer> HOPPER_TYPE = new UpgradeContainerType<>(HopperUpgradeContainer::new);
	public static final UpgradeContainerType<HopperUpgradeWrapper, HopperUpgradeContainer> ADVANCED_HOPPER_TYPE = new UpgradeContainerType<>(HopperUpgradeContainer::new);

	public static void registerContainers() {
		UpgradeContainerRegistry.register(PICKUP_UPGRADE, PICKUP_BASIC_TYPE);
		UpgradeContainerRegistry.register(ADVANCED_PICKUP_UPGRADE, PICKUP_ADVANCED_TYPE);
		UpgradeContainerRegistry.register(FILTER_UPGRADE, FilterUpgradeContainer.BASIC_TYPE);
		UpgradeContainerRegistry.register(ADVANCED_FILTER_UPGRADE, FilterUpgradeContainer.ADVANCED_TYPE);
		UpgradeContainerRegistry.register(MAGNET_UPGRADE, MAGNET_BASIC_TYPE);
		UpgradeContainerRegistry.register(ADVANCED_MAGNET_UPGRADE, MAGNET_ADVANCED_TYPE);
		UpgradeContainerRegistry.register(FEEDING_UPGRADE, FEEDING_TYPE);
		UpgradeContainerRegistry.register(ADVANCED_FEEDING_UPGRADE, ADVANCED_FEEDING_TYPE);
		UpgradeContainerRegistry.register(COMPACTING_UPGRADE, COMPACTING_TYPE);
		UpgradeContainerRegistry.register(ADVANCED_COMPACTING_UPGRADE, ADVANCED_COMPACTING_TYPE);
		UpgradeContainerRegistry.register(VOID_UPGRADE, VOID_TYPE);
		UpgradeContainerRegistry.register(ADVANCED_VOID_UPGRADE, ADVANCED_VOID_TYPE);
		UpgradeContainerRegistry.register(SMELTING_UPGRADE, SMELTING_TYPE);
		UpgradeContainerRegistry.register(AUTO_SMELTING_UPGRADE, AUTO_SMELTING_TYPE);
		UpgradeContainerRegistry.register(SMOKING_UPGRADE, SMOKING_TYPE);
		UpgradeContainerRegistry.register(AUTO_SMOKING_UPGRADE, AUTO_SMOKING_TYPE);
		UpgradeContainerRegistry.register(BLASTING_UPGRADE, BLASTING_TYPE);
		UpgradeContainerRegistry.register(AUTO_BLASTING_UPGRADE, AUTO_BLASTING_TYPE);
		UpgradeContainerRegistry.register(CRAFTING_UPGRADE, CRAFTING_TYPE);
		UpgradeContainerRegistry.register(STONECUTTER_UPGRADE, STONECUTTER_TYPE);
		UpgradeContainerRegistry.register(JUKEBOX_UPGRADE, JUKEBOX_TYPE);
		UpgradeContainerRegistry.register(PUMP_UPGRADE, PUMP_TYPE);
		UpgradeContainerRegistry.register(ADVANCED_PUMP_UPGRADE, ADVANCED_PUMP_TYPE);
		UpgradeContainerRegistry.register(XP_PUMP_UPGRADE, XP_PUMP_TYPE);
		UpgradeContainerRegistry.register(HOPPER_UPGRADE, HOPPER_TYPE);
		UpgradeContainerRegistry.register(ADVANCED_HOPPER_UPGRADE, ADVANCED_HOPPER_TYPE);
	}

	private static class StorageCompactingUpgradeItem extends CompactingUpgradeItem {
		public static final List<UpgradeConflictDefinition> UPGRADE_CONFLICT_DEFINITIONS = List.of(new UpgradeConflictDefinition(CompressionUpgradeItem.class::isInstance, 0, StorageTranslationHelper.INSTANCE.translError("add.compression_exists")));

		public StorageCompactingUpgradeItem(boolean shouldCompactThreeByThree, IntSupplier filterSlotCount, IUpgradeCountLimitConfig upgradeCountLimitConfig) {
			super(shouldCompactThreeByThree, filterSlotCount, upgradeCountLimitConfig);
		}

		@Override
		public List<UpgradeConflictDefinition> getUpgradeConflicts() {
			return UPGRADE_CONFLICT_DEFINITIONS;
		}
	}

	public static <T extends Item> T register(String id, Supplier<T> supplier) {
		T item = supplier.get();
		ITEMS.put(SophisticatedStorage.getRL(id), item);
		return Registry.register(BuiltInRegistries.ITEM, SophisticatedStorage.getRL(id), item);
	}
	public static <T extends LootItemFunctionType> T registerLootFunction(String id, Supplier<T> supplier) {
		return Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, SophisticatedStorage.getRL(id), supplier.get());
	}

	public static void register() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, SophisticatedStorage.getRL("item_group"), CREATIVE_TAB);

		registerContainers();
		registerRecipeCondition();
	}
}
