package net.p3pp3rf1y.sophisticatedstorage.compat.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SettingsScreen;
import net.p3pp3rf1y.sophisticatedcore.compat.emi.EmiGridMenuInfo;
import net.p3pp3rf1y.sophisticatedcore.compat.emi.EmiSettingsGhostDragDropHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.emi.EmiStorageGhostDragDropHandler;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageScreen;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageSettingsScreen;
import net.p3pp3rf1y.sophisticatedstorage.compat.common.ControllerRecipesMaker;
import net.p3pp3rf1y.sophisticatedstorage.compat.common.DyeRecipesMaker;
import net.p3pp3rf1y.sophisticatedstorage.compat.common.FlatBarrelRecipesMaker;
import net.p3pp3rf1y.sophisticatedstorage.compat.common.ShulkerBoxFromChestRecipesMaker;
import net.p3pp3rf1y.sophisticatedstorage.compat.common.TierUpgradeRecipesMaker;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;

import java.util.Collection;
import java.util.function.Consumer;

public class EmiCompat implements EmiPlugin {
	private static Consumer<EmiRegistry> additionalCategories = registration -> {};
	public static void setAdditionalCategories(Consumer<EmiRegistry> additionalCategories) {
		EmiCompat.additionalCategories = additionalCategories;
	}

	@Override
    public void register(EmiRegistry registry) {
        registry.addExclusionArea(StorageScreen.class, (screen, consumer) -> {
            screen.getUpgradeSlotsRectangle().ifPresent(r -> consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
            screen.getUpgradeSettingsControl().getTabRectangles().forEach(r -> consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
            screen.getSortButtonsRectangle().ifPresent(r -> consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
        });
        registry.addExclusionArea(StorageSettingsScreen.class, (screen, consumer) -> {
            //noinspection ConstantValue
            if (screen == null || screen.getSettingsTabControl() == null) { // Due to how Emi collects the exclusion area this can be null
                return;
            }
            screen.getSettingsTabControl().getTabRectangles().forEach(r -> consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
        });

        registry.addDragDropHandler(StorageScreen.class, new EmiStorageGhostDragDropHandler<>());
        registry.addDragDropHandler(SettingsScreen.class, new EmiSettingsGhostDragDropHandler<>());

        registerCraftingRecipes(registry, DyeRecipesMaker.getRecipes());
		registerCraftingRecipes(registry, TierUpgradeRecipesMaker.getShapedCraftingRecipes());
		registerCraftingRecipes(registry, TierUpgradeRecipesMaker.getShapelessCraftingRecipes());
		registerCraftingRecipes(registry, ControllerRecipesMaker.getRecipes());
		registerCraftingRecipes(registry, ShulkerBoxFromChestRecipesMaker.getRecipes());
		registerCraftingRecipes(registry, FlatBarrelRecipesMaker.getRecipes());

		Comparison woodStorageNbtInterpreter = Comparison.compareData(emiStack -> {
			CompoundTag tag = new CompoundTag();
			ItemStack stack = emiStack.getItemStack();
			WoodStorageBlockItem.getWoodType(stack).ifPresent(woodName -> tag.putString("woodName", woodName.name()));
			StorageBlockItem.getMainColorFromStack(stack).ifPresent(mainColor -> tag.putInt("mainColor", mainColor));
			StorageBlockItem.getAccentColorFromStack(stack).ifPresent(accentColor -> tag.putInt("accentColor", accentColor));
			return tag;
		});

		Comparison barrelNbtInterpreter = Comparison.compareData(emiStack -> {
			CompoundTag tag = new CompoundTag();
			ItemStack stack = emiStack.getItemStack();
			WoodStorageBlockItem.getWoodType(stack).ifPresent(woodName -> tag.putString("woodName", woodName.name()));
			StorageBlockItem.getMainColorFromStack(stack).ifPresent(mainColor -> tag.putInt("mainColor", mainColor));
			StorageBlockItem.getAccentColorFromStack(stack).ifPresent(accentColor -> tag.putInt("accentColor", accentColor));
			tag.putBoolean("flatTop", BarrelBlockItem.isFlatTop(stack));
			return tag;
		});

		registry.setDefaultComparison(ModBlocks.BARREL_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.IRON_BARREL_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.GOLD_BARREL_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.DIAMOND_BARREL_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.NETHERITE_BARREL_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.CHEST_ITEM, woodStorageNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.IRON_CHEST_ITEM, woodStorageNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.GOLD_CHEST_ITEM, woodStorageNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.DIAMOND_CHEST_ITEM, woodStorageNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.NETHERITE_CHEST_ITEM, woodStorageNbtInterpreter);

		registry.setDefaultComparison(ModBlocks.LIMITED_BARREL_1_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.LIMITED_BARREL_2_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.LIMITED_BARREL_3_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.LIMITED_BARREL_4_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.LIMITED_IRON_BARREL_1_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.LIMITED_IRON_BARREL_2_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.LIMITED_IRON_BARREL_3_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.LIMITED_IRON_BARREL_4_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.LIMITED_GOLD_BARREL_1_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.LIMITED_GOLD_BARREL_2_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.LIMITED_GOLD_BARREL_3_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.LIMITED_GOLD_BARREL_4_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.LIMITED_NETHERITE_BARREL_2_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.LIMITED_NETHERITE_BARREL_3_ITEM, barrelNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.LIMITED_NETHERITE_BARREL_4_ITEM, barrelNbtInterpreter);

		Comparison shulkerBoxNbtInterpreter = Comparison.compareData(emiStack -> {
			CompoundTag tag = new CompoundTag();
			ItemStack stack = emiStack.getItemStack();
			StorageBlockItem.getMainColorFromStack(stack).ifPresent(mainColor -> tag.putInt("mainColor", mainColor));
			StorageBlockItem.getAccentColorFromStack(stack).ifPresent(accentColor -> tag.putInt("accentColor", accentColor));
			return tag;
		});
		registry.setDefaultComparison(ModBlocks.SHULKER_BOX_ITEM, shulkerBoxNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.IRON_SHULKER_BOX_ITEM, shulkerBoxNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.GOLD_SHULKER_BOX_ITEM, shulkerBoxNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.DIAMOND_SHULKER_BOX_ITEM, shulkerBoxNbtInterpreter);
		registry.setDefaultComparison(ModBlocks.NETHERITE_SHULKER_BOX_ITEM, shulkerBoxNbtInterpreter);

		registry.addRecipeHandler(ModBlocks.STORAGE_CONTAINER_TYPE, new EmiGridMenuInfo<>());

		registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(ModItems.CRAFTING_UPGRADE));
		registry.addWorkstation(VanillaEmiRecipeCategories.STONECUTTING, EmiStack.of(ModItems.STONECUTTER_UPGRADE));
		additionalCategories.accept(registry);
    }

    private static void registerCraftingRecipes(EmiRegistry registry, Collection<CraftingRecipe> recipes) {
        recipes.forEach(r -> registry.addRecipe(
            new EmiCraftingRecipe(
                r.getIngredients().stream().map(EmiIngredient::of).toList(),
                EmiStack.of(r.getResultItem()),
                r.getId())
            )
        );
    }
}
