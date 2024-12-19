package net.p3pp3rf1y.sophisticatedstorage.crafting;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.util.BlockItemBase;
import net.p3pp3rf1y.sophisticatedcore.util.StreamCodecHelper;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BaseTierWoodenStorageIngredient implements CustomIngredient {
	public static final BaseTierWoodenStorageIngredient INSTANCE = new BaseTierWoodenStorageIngredient();
	public static final CustomIngredientSerializer<BaseTierWoodenStorageIngredient> SERIALIZER = new Serializer();

	public BaseTierWoodenStorageIngredient() {
	}

	@Override
	public boolean test(@Nullable ItemStack stack) {
		return stack != null && stack.is(ModBlocks.BASE_TIER_WOODEN_STORAGE_TAG);
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		List<ItemStack> itemStacks = Lists.newArrayList();
		if (ModBlocks.CHEST_ITEM.get() instanceof BlockItemBase itemBase) {
			itemBase.addCreativeTabItems(itemStacks::add);
		}
		if (ModBlocks.BARREL_ITEM.get() instanceof BlockItemBase itemBase) {
			itemBase.addCreativeTabItems(itemStacks::add);
		}

		return itemStacks;
	}

	@Override
	public boolean requiresTesting() {
		return true;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	public static class Serializer implements CustomIngredientSerializer<BaseTierWoodenStorageIngredient> {
		public static final MapCodec<BaseTierWoodenStorageIngredient> CODEC = MapCodec.unit(BaseTierWoodenStorageIngredient::new);
		public static final StreamCodec<RegistryFriendlyByteBuf, BaseTierWoodenStorageIngredient> PACKET_CODEC = StreamCodecHelper.singleton(BaseTierWoodenStorageIngredient::new);

		@Override
		public ResourceLocation getIdentifier() {
			return SophisticatedStorage.getRL("base_tier_wooden_storage");
		}

		@Override
		public MapCodec<BaseTierWoodenStorageIngredient> getCodec(boolean allowEmpty) {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, BaseTierWoodenStorageIngredient> getPacketCodec() {
			return PACKET_CODEC;
		}
	}
}
