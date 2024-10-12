package net.p3pp3rf1y.sophisticatedstorage.crafting;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.p3pp3rf1y.sophisticatedcore.util.BlockItemBase;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public class BaseTierWoodenStorageIngredient implements CustomIngredient {
	public static final BaseTierWoodenStorageIngredient INSTANCE = new BaseTierWoodenStorageIngredient();
	public static final Codec<BaseTierWoodenStorageIngredient> CODEC = MapCodec.unit(INSTANCE).stable().codec();

	private BaseTierWoodenStorageIngredient() {
		super();
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
	public boolean test(@Nullable ItemStack stack) {
		return stack != null && stack.is(ModBlocks.BASE_TIER_WOODEN_STORAGE_TAG);
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
		public Codec<BaseTierWoodenStorageIngredient> getCodec(boolean allowEmpty) {
			return CODEC;
		}

		@Override
		public BaseTierWoodenStorageIngredient read(FriendlyByteBuf buf) {
			return new BaseTierWoodenStorageIngredient();
		}

		@Override
		public void write(FriendlyByteBuf buf, BaseTierWoodenStorageIngredient ingredient) {
			//noop
		}
	}
}
