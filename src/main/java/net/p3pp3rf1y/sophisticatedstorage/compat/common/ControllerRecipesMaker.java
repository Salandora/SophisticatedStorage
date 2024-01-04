package net.p3pp3rf1y.sophisticatedstorage.compat.common;

import com.google.common.base.Function;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.p3pp3rf1y.sophisticatedcore.compat.common.ClientRecipeHelper;
import net.p3pp3rf1y.sophisticatedcore.util.BlockItemBase;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ControllerRecipesMaker {
	private ControllerRecipesMaker() {}

	public static List<CraftingRecipe> getRecipes() {
		return ClientRecipeHelper.getRecipeByKey(BuiltInRegistries.ITEM.getKey(ModBlocks.CONTROLLER_ITEM)).map((Function<Recipe<?>, List<CraftingRecipe>>) r -> {
			if (!(r instanceof ShapedRecipe originalRecipe)) {
				return new ArrayList<>();
			}

			NonNullList<Ingredient> ingredients = r.getIngredients();
			NonNullList<Ingredient> ingredientsCopy = NonNullList.createWithCapacity(ingredients.size());
			int i = 0;
			for (Ingredient ingredient : ingredients) {
				ingredientsCopy.add(i, getExpandedIngredient(ingredient));
				i++;
			}
			return Collections.singletonList(new ShapedRecipe(originalRecipe.getId(), "", CraftingBookCategory.MISC, originalRecipe.getWidth(), originalRecipe.getHeight(), ingredientsCopy, ClientRecipeHelper.getResultItem(originalRecipe)));
		}).orElse(Collections.emptyList());
	}

	private static Ingredient getExpandedIngredient(Ingredient ingredient) {
		ItemStack[] ingredientItems = ingredient.getItems();

		NonNullList<ItemStack> storages = NonNullList.create();
		for (ItemStack ingredientItem : ingredientItems) {
			Item item = ingredientItem.getItem();
			if (item instanceof BlockItemBase itemBase && (item == ModBlocks.BARREL_ITEM || item == ModBlocks.CHEST_ITEM)) {
				itemBase.addCreativeTabItems(storages::add);
			}
		}
		if (!storages.isEmpty()) {
			return Ingredient.of(storages.toArray(new ItemStack[0]));
		} else {
			return ingredient;
		}
	}
}
