package net.p3pp3rf1y.sophisticatedstorage.compat.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.registry.EmiTags;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.p3pp3rf1y.sophisticatedcore.compat.common.ClientRecipeHelper;
import net.p3pp3rf1y.sophisticatedcore.compat.emi.EmiGridMenuInfo;
import net.p3pp3rf1y.sophisticatedcore.compat.emi.EmiSettingsGhostDragDropHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.emi.EmiStorageGhostDragDropHandler;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageScreen;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageSettingsScreen;
import net.p3pp3rf1y.sophisticatedstorage.compat.common.DyeRecipesMaker;
import net.p3pp3rf1y.sophisticatedstorage.compat.common.FlatBarrelRecipesMaker;
import net.p3pp3rf1y.sophisticatedstorage.compat.common.ShulkerBoxFromChestRecipesMaker;
import net.p3pp3rf1y.sophisticatedstorage.compat.common.TierUpgradeRecipesMaker;
import net.p3pp3rf1y.sophisticatedstorage.crafting.BaseTierWoodenStorageIngredient;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;

import java.util.*;
import java.util.function.Consumer;

public class EmiCompat implements EmiPlugin {
	public static Event<WorkstationCallback> WORKSTATIONS = EventFactory.createArrayBacked(WorkstationCallback.class, (listeners) -> (consumer) -> {
		for (WorkstationCallback listener : listeners) {
			listener.additionalWorkstations(consumer);
		}
	});

	public record WorkstationEntry(ResourceLocation id, Block icon, Item workstation) {}

	public interface WorkstationCallback {
		void additionalWorkstations(Consumer<WorkstationEntry> consumer);
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
        registry.addDragDropHandler(StorageSettingsScreen.class, new EmiSettingsGhostDragDropHandler<>());

		for (BlockItem item : ModBlocks.WOODEN_STORAGE_INGREDIENT_ITEMS) {
			ClientRecipeHelper.getRecipeByKey(BuiltInRegistries.ITEM.getKey(item)).ifPresent(r -> registerRecipes(registry, Collections.singletonList(r)));
		}

        registerRecipes(registry, DyeRecipesMaker.getRecipes());
		registerRecipes(registry, TierUpgradeRecipesMaker.getShapedCraftingRecipes());
		registerRecipes(registry, TierUpgradeRecipesMaker.getShapelessCraftingRecipes());
		registerRecipes(registry, ShulkerBoxFromChestRecipesMaker.getRecipes());
		registerRecipes(registry, FlatBarrelRecipesMaker.getRecipes());

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

		for (BlockItem item : ModBlocks.ALL_BARREL_ITEMS) {
			registry.setDefaultComparison(item, barrelNbtInterpreter);
		}
		for (BlockItem item : ModBlocks.CHEST_ITEMS) {
			registry.setDefaultComparison(item, woodStorageNbtInterpreter);
		}

		Comparison shulkerBoxNbtInterpreter = Comparison.compareData(emiStack -> {
			CompoundTag tag = new CompoundTag();
			ItemStack stack = emiStack.getItemStack();
			StorageBlockItem.getMainColorFromStack(stack).ifPresent(mainColor -> tag.putInt("mainColor", mainColor));
			StorageBlockItem.getAccentColorFromStack(stack).ifPresent(accentColor -> tag.putInt("accentColor", accentColor));
			return tag;
		});
		for (BlockItem item : ModBlocks.SHULKER_BOX_ITEMS) {
			registry.setDefaultComparison(item, shulkerBoxNbtInterpreter);
		}

		registry.addRecipeHandler(ModBlocks.STORAGE_CONTAINER_TYPE, EmiGridMenuInfo.crafting());
		registry.addRecipeHandler(ModBlocks.STORAGE_CONTAINER_TYPE, EmiGridMenuInfo.stonecutting());
		registry.addRecipeHandler(ModBlocks.STORAGE_CONTAINER_TYPE, EmiGridMenuInfo.smithing());

		registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(ModItems.CRAFTING_UPGRADE));
		registry.addWorkstation(VanillaEmiRecipeCategories.STONECUTTING, EmiStack.of(ModItems.STONECUTTER_UPGRADE));

		List<WorkstationEntry> entries = new ArrayList<>();
		WORKSTATIONS.invoker().additionalWorkstations(entries::add);
		for (WorkstationEntry entry : entries) {
			registry.addWorkstation(new EmiRecipeCategory(entry.id, EmiStack.of(entry.icon)), EmiStack.of(entry.workstation));
		}
    }

    private static void registerRecipes(EmiRegistry registry, Collection<? extends Recipe<?>> recipes) {
        recipes.forEach(r -> {
			NonNullList<Ingredient> ingredients = r.getIngredients();
			NonNullList<EmiIngredient> ingredientsCopy = NonNullList.createWithCapacity(ingredients.size());
			int i = 0;
			for (Ingredient ingredient : ingredients) {
				if (ingredient.getCustomIngredient() instanceof BaseTierWoodenStorageIngredient) {
					ItemStack[] stacks = ingredient.getItems();
					int amount = 1;
					if (stacks.length != 0) {
						amount = stacks[0].getCount();
						for (int j = 1; j < stacks.length; j++) {
							if (stacks[j].getCount() != amount) {
								amount = 1;
								break;
							}
						}
					}

					ingredientsCopy.add(i, EmiTags.getIngredient(Item.class, Arrays.stream(ingredient.getItems()).map(stack -> EmiStack.of(stack).comparison(Comparison.compareNbt())).toList(), amount));
				} else {
					ingredientsCopy.add(i, EmiIngredient.of(ingredient));
				}
				i++;
			}

			registry.addRecipe(new EmiCraftingRecipe(ingredientsCopy, EmiStack.of(r.getResultItem(null)), r.getId()));
		});
    }
}
