package net.p3pp3rf1y.sophisticatedstorage.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.p3pp3rf1y.sophisticatedcore.crafting.IWrapperRecipe;
import net.p3pp3rf1y.sophisticatedcore.crafting.RecipeWrapperSerializer;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.CapabilityStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.mixin.common.accessor.ShapelessRecipeAccessor;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class ShulkerBoxFromVanillaShapelessRecipe extends ShapelessRecipe implements IWrapperRecipe<ShapelessRecipe> {
	public static final Set<ResourceLocation> REGISTERED_RECIPES = new LinkedHashSet<>();
	private final ShapelessRecipe compose;

	public ShulkerBoxFromVanillaShapelessRecipe(ShapelessRecipe compose) {
		super(compose.getId(), compose.getGroup(), compose.category(), ((ShapelessRecipeAccessor) compose).getResult(), compose.getIngredients());
		this.compose = compose;
		REGISTERED_RECIPES.add(compose.getId());
	}

	@Override
	public boolean matches(CraftingContainer inventory, Level level) {
		return super.matches(inventory, level) && getVanillaShulkerBox(inventory).map(storage -> !(storage.getItem() instanceof WoodStorageBlockItem) || !WoodStorageBlockItem.isPacked(storage)).orElse(false);
	}

	@Override
	public ShapelessRecipe getCompose() {
		return compose;
	}

	@Override
	public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
		ItemStack upgradedStorage = super.assemble(input, registries);
		getVanillaShulkerBox(input).ifPresent(vanillaShulkerBox -> {
			NonNullList<ItemStack> itemStacks = getStoredItems(vanillaShulkerBox);
			upgradedStorage.sophisticatedLibrary_getLazyCapability(CapabilityStorageWrapper.getCapabilityInstance()).ifPresent(wrapper -> {
				for (ItemStack stack : itemStacks) {
					if (!stack.isEmpty()) {
						wrapper.getInventoryHandler().insertItem(stack, false);
					}
				};
			});
		});
		return upgradedStorage;
	}

	public static NonNullList<ItemStack> getStoredItems(ItemStack stackIn) {
		CompoundTag nbt = stackIn.getTag();
		if (nbt != null && nbt.contains("BlockEntityTag", 10)) {
			CompoundTag tagBlockEntity = nbt.getCompound("BlockEntityTag");
			if (tagBlockEntity.contains("Items", 9)) {
				NonNullList<ItemStack> items = NonNullList.create();
				ListTag tagList = tagBlockEntity.getList("Items", 10);
				int count = tagList.size();

				for(int i = 0; i < count; ++i) {
					ItemStack stack = ItemStack.of(tagList.getCompound(i));
					if (!stack.isEmpty()) {
						items.add(stack);
					}
				}

				return items;
			}
		}

		return NonNullList.create();
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	private Optional<ItemStack> getVanillaShulkerBox(CraftingContainer input) {
		for (int slot = 0; slot < input.getContainerSize(); slot++) {
			ItemStack slotStack = input.getItem(slot);
			if (slotStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
				return Optional.of(slotStack);
			}
		}

		return Optional.empty();
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModBlocks.SHULKER_BOX_FROM_VANILLA_SHAPELESS_RECIPE_SERIALIZER;
	}

	public static class Serializer extends RecipeWrapperSerializer<ShapelessRecipe, ShulkerBoxFromVanillaShapelessRecipe> {
		public Serializer() {
			super(ShulkerBoxFromVanillaShapelessRecipe::new, RecipeSerializer.SHAPELESS_RECIPE);
		}
	}
}
