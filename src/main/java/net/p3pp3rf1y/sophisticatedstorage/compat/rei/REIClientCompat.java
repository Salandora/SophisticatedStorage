package net.p3pp3rf1y.sophisticatedstorage.compat.rei;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.p3pp3rf1y.sophisticatedcore.compat.rei.SettingsGhostIngredientHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.rei.StorageGhostIngredientHandler;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageScreen;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageSettingsScreen;
import net.p3pp3rf1y.sophisticatedstorage.compat.common.DyeRecipesMaker;
import net.p3pp3rf1y.sophisticatedstorage.compat.common.FlatBarrelRecipesMaker;
import net.p3pp3rf1y.sophisticatedstorage.compat.common.ShulkerBoxFromChestRecipesMaker;
import net.p3pp3rf1y.sophisticatedstorage.compat.common.TierUpgradeRecipesMaker;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class REIClientCompat implements REIClientPlugin {
	private static Consumer<CategoryRegistry> additionalCategories = registration -> {};
	public static void setAdditionalCategories(Consumer<CategoryRegistry> additionalCategories) {
		REIClientCompat.additionalCategories = additionalCategories;
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
	public void registerCategories(CategoryRegistry registry) {
		registry.addWorkstations(BuiltinPlugin.CRAFTING, EntryStacks.of(ModItems.CRAFTING_UPGRADE));
		registry.addWorkstations(BuiltinPlugin.STONE_CUTTING, EntryStacks.of(ModItems.STONECUTTER_UPGRADE));
		additionalCategories.accept(registry);
	}

	@Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerDraggableStackVisitor(new StorageGhostIngredientHandler<>() {
            @Override
            public <R extends Screen> boolean isHandingScreen(R screen) {
                return screen instanceof StorageScreen;
            }
        });
		registry.registerDraggableStackVisitor(new SettingsGhostIngredientHandler<>());
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registerRecipes(registry, DyeRecipesMaker.getRecipes(), BuiltinPlugin.CRAFTING);
		registerRecipes(registry, TierUpgradeRecipesMaker.getShapedCraftingRecipes(), BuiltinPlugin.CRAFTING);
		registerRecipes(registry, TierUpgradeRecipesMaker.getShapelessCraftingRecipes(), BuiltinPlugin.CRAFTING);
		registerRecipes(registry, ShulkerBoxFromChestRecipesMaker.getRecipes(), BuiltinPlugin.CRAFTING);
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
