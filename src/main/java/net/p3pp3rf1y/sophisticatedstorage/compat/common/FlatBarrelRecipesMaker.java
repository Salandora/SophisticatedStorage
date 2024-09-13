package net.p3pp3rf1y.sophisticatedstorage.compat.common;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;

import java.util.ArrayList;
import java.util.List;

public class FlatBarrelRecipesMaker {
	private FlatBarrelRecipesMaker() {
	}

	public static List<RecipeHolder<CraftingRecipe>> getRecipes() {
		List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();

		ItemStack barrel = WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.BARREL), WoodType.ACACIA);
		ItemStack flatBarrel = barrel.copy();
		BarrelBlockItem.toggleFlatTop(flatBarrel);

		recipes.add(new RecipeHolder<>(SophisticatedStorage.getRL("flatten_barrel"), new ShapelessRecipe("", CraftingBookCategory.MISC, flatBarrel, NonNullList.of(Ingredient.EMPTY, Ingredient.of(barrel)))));
		recipes.add(new RecipeHolder<>(SophisticatedStorage.getRL("unflatten_barrel"), new ShapelessRecipe("", CraftingBookCategory.MISC, barrel, NonNullList.of(Ingredient.EMPTY, Ingredient.of(flatBarrel)))));

		return recipes;
	}
}
