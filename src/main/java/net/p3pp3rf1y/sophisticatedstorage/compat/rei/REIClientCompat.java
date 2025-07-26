package net.p3pp3rf1y.sophisticatedstorage.compat.rei;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.compat.rei.REISettingsGhostIngredientHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.rei.REIStorageGhostIngredientHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.rei.SophisticatedTransferHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.rei.subtypes.PropertyBasedSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageScreen;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageSettingsScreen;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorage.compat.jei.FlatBarrelRecipesMaker;
import net.p3pp3rf1y.sophisticatedstorage.compat.rei.subtypes.BarrelSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorage.compat.rei.subtypes.ChestSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorage.compat.rei.subtypes.ShulkerBoxSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;

import java.util.*;
import java.util.function.Consumer;

public class REIClientCompat implements REIClientPlugin {
	private static Consumer<CategoryRegistry> additionalCategories = registration -> {};
	public static void setAdditionalCategories(Consumer<CategoryRegistry> additionalCategories) {
		REIClientCompat.additionalCategories = additionalCategories;
	}
	private final PropertyBasedSubtypeInterpreter chestSubtypeInterpreter = new ChestSubtypeInterpreter();
	private final PropertyBasedSubtypeInterpreter barrelSubtypeInterpreter = new BarrelSubtypeInterpreter();
	private final PropertyBasedSubtypeInterpreter shulkerBoxSubtypeInterpreter = new ShulkerBoxSubtypeInterpreter();

	private Map<BlockItem, PropertyBasedSubtypeInterpreter> getSubtypeIntepreters() {
		return new HashMap<>(){{
			put(ModBlocks.BARREL_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.COPPER_BARREL_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.IRON_BARREL_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.GOLD_BARREL_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.DIAMOND_BARREL_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.NETHERITE_BARREL_ITEM.get(), barrelSubtypeInterpreter);

			put(ModBlocks.CHEST_ITEM.get(), chestSubtypeInterpreter);
			put(ModBlocks.COPPER_CHEST_ITEM.get(), chestSubtypeInterpreter);
			put(ModBlocks.IRON_CHEST_ITEM.get(), chestSubtypeInterpreter);
			put(ModBlocks.GOLD_CHEST_ITEM.get(), chestSubtypeInterpreter);
			put(ModBlocks.DIAMOND_CHEST_ITEM.get(), chestSubtypeInterpreter);
			put(ModBlocks.NETHERITE_CHEST_ITEM.get(), chestSubtypeInterpreter);

			put(ModBlocks.LIMITED_BARREL_1_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_BARREL_2_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_BARREL_3_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_BARREL_4_ITEM.get(), barrelSubtypeInterpreter);

			put(ModBlocks.LIMITED_COPPER_BARREL_1_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_COPPER_BARREL_2_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_COPPER_BARREL_3_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_COPPER_BARREL_4_ITEM.get(), barrelSubtypeInterpreter);

			put(ModBlocks.LIMITED_IRON_BARREL_1_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_IRON_BARREL_2_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_IRON_BARREL_3_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_IRON_BARREL_4_ITEM.get(), barrelSubtypeInterpreter);

			put(ModBlocks.LIMITED_GOLD_BARREL_1_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_GOLD_BARREL_2_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_GOLD_BARREL_3_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_GOLD_BARREL_4_ITEM.get(), barrelSubtypeInterpreter);

			put(ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM.get(), barrelSubtypeInterpreter);

			put(ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_NETHERITE_BARREL_2_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_NETHERITE_BARREL_3_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_NETHERITE_BARREL_4_ITEM.get(), barrelSubtypeInterpreter);

			put(ModBlocks.SHULKER_BOX_ITEM.get(), shulkerBoxSubtypeInterpreter);
			put(ModBlocks.COPPER_SHULKER_BOX_ITEM.get(), shulkerBoxSubtypeInterpreter);
			put(ModBlocks.IRON_SHULKER_BOX_ITEM.get(), shulkerBoxSubtypeInterpreter);
			put(ModBlocks.GOLD_SHULKER_BOX_ITEM.get(), shulkerBoxSubtypeInterpreter);
			put(ModBlocks.DIAMOND_SHULKER_BOX_ITEM.get(), shulkerBoxSubtypeInterpreter);
			put(ModBlocks.NETHERITE_SHULKER_BOX_ITEM.get(), shulkerBoxSubtypeInterpreter);
		}};
	}

	private Optional<PropertyBasedSubtypeInterpreter> getSubtypeInterpreter(Map<BlockItem, PropertyBasedSubtypeInterpreter> subtypeInterpreters, ItemStack stack) {
		if (!(stack.getItem() instanceof BlockItem blockItem)) {
			return Optional.empty();
		}

		return Optional.ofNullable(subtypeInterpreters.get(blockItem));
	}

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(StorageScreen.class, screen -> {
            List<Rect2i> ret = new ArrayList<>();
            screen.getUpgradeSlotsRectangle().ifPresent(ret::add);
            ret.addAll(screen.getUpgradeSettingsControl().getTabRectangles());
            screen.getSortButtonsRectangle().ifPresent(ret::add);
            return ret.stream().map(r -> new Rectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight())).toList();
        });

        zones.register(StorageSettingsScreen.class, screen -> screen.getSettingsTabControl().getTabRectangles().stream().map(r -> new Rectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight())).toList());
    }

	@Override
	public void registerTransferHandlers(TransferHandlerRegistry registry) {
		registry.register(SophisticatedTransferHandler.crafting(StorageContainerMenu.class));
	}

	@Override
	public void registerCategories(CategoryRegistry registry) {
		registry.addWorkstations(BuiltinPlugin.CRAFTING, EntryStacks.of(ModItems.CRAFTING_UPGRADE.get()));
		registry.addWorkstations(BuiltinPlugin.STONE_CUTTING, EntryStacks.of(ModItems.STONECUTTER_UPGRADE.get()));
		additionalCategories.accept(registry);
	}

	@Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerDraggableStackVisitor(new REIStorageGhostIngredientHandler<>() {
            @Override
            public <R extends Screen> boolean isHandingScreen(R screen) {
                return screen instanceof StorageScreen;
            }
        });
		registry.registerDraggableStackVisitor(new REISettingsGhostIngredientHandler<>());
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
		Map<BlockItem, PropertyBasedSubtypeInterpreter> subtypeInterpreters = getSubtypeIntepreters();
        registerRecipes(registry, DyeRecipesMaker.getRecipes(stack -> getSubtypeInterpreter(subtypeInterpreters, stack)), BuiltinPlugin.CRAFTING);
		registerRecipes(registry, TierUpgradeRecipesMaker.getShapedCraftingRecipes(stack -> getSubtypeInterpreter(subtypeInterpreters, stack)), BuiltinPlugin.CRAFTING);
		registerRecipes(registry, TierUpgradeRecipesMaker.getShapelessCraftingRecipes(stack -> getSubtypeInterpreter(subtypeInterpreters, stack)), BuiltinPlugin.CRAFTING);
		registerRecipes(registry, ShulkerBoxFromChestRecipesMaker.getRecipes(stack -> getSubtypeInterpreter(subtypeInterpreters, stack)), BuiltinPlugin.CRAFTING);
		registerRecipes(registry, FlatBarrelRecipesMaker.getRecipes(), BuiltinPlugin.CRAFTING);
    }

    public static void registerRecipes(DisplayRegistry registry, Collection<?> recipes, CategoryIdentifier<?> identifier) {
        recipes.forEach(recipe -> {
            Collection<Display> displays = registry.tryFillDisplay(recipe);
            for (Display display : displays) {
                if (Objects.equals(display.getCategoryIdentifier(), identifier)) {
                    registry.add(display, recipe);
                }
            }
        });
    }
}
