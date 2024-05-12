package net.p3pp3rf1y.sophisticatedstorage.crafting;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.util.BlockItemBase;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;

import java.util.List;
import org.jetbrains.annotations.Nullable;

public class BaseTierWoodenStorageIngredient implements CustomIngredient {
	public static final BaseTierWoodenStorageIngredient INSTANCE = new BaseTierWoodenStorageIngredient();

	private BaseTierWoodenStorageIngredient() {
		super();
	}

	@Override
	public boolean test(@Nullable ItemStack stack) {
		return stack != null && stack.is(ModBlocks.BASE_TIER_WOODEN_STORAGE_TAG);
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		List<ItemStack> itemStacks = Lists.newArrayList();
		if (ModBlocks.CHEST_ITEM instanceof BlockItemBase itemBase) {
			itemBase.addCreativeTabItems(itemStacks::add);
		}
		if (ModBlocks.BARREL_ITEM instanceof BlockItemBase itemBase) {
			itemBase.addCreativeTabItems(itemStacks::add);
		}

		return itemStacks;
	}

	@Override
	public boolean requiresTesting() {
		return false;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
	}

	public static class Serializer implements CustomIngredientSerializer<BaseTierWoodenStorageIngredient> {
		public static Serializer INSTANCE = new Serializer();

		@Override
		public ResourceLocation getIdentifier() {
			return SophisticatedStorage.getRL("base_tier_wooden_storage");
		}

		@Override
		public BaseTierWoodenStorageIngredient read(JsonObject json) {
			return new BaseTierWoodenStorageIngredient();
		}

		@Override
		public BaseTierWoodenStorageIngredient read(FriendlyByteBuf buf) {
			return new BaseTierWoodenStorageIngredient();
		}

		@Override
		public void write(JsonObject json, BaseTierWoodenStorageIngredient ingredient) {
			//noop
		}

		@Override
		public void write(FriendlyByteBuf buf, BaseTierWoodenStorageIngredient ingredient) {
			//noop
		}
	}
}
