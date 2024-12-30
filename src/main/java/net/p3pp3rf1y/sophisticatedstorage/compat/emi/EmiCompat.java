package net.p3pp3rf1y.sophisticatedstorage.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
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
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SettingsScreen;
import net.p3pp3rf1y.sophisticatedcore.compat.emi.EmiGridMenuInfo;
import net.p3pp3rf1y.sophisticatedcore.compat.emi.EmiSettingsGhostDragDropHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.emi.EmiStorageGhostDragDropHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.ClientRecipeHelper;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageScreen;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageSettingsScreen;
import net.p3pp3rf1y.sophisticatedstorage.crafting.BaseTierWoodenStorageIngredient;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@EmiEntrypoint
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
        registry.addDragDropHandler(SettingsScreen.class, new EmiSettingsGhostDragDropHandler<>());

		for (Supplier<BlockItem> item : ModBlocks.WOODEN_STORAGE_INGREDIENT_ITEMS) {
			ClientRecipeHelper.getCraftingRecipeByKey(RecipeType.CRAFTING, BuiltInRegistries.ITEM.getKey(item.get())).ifPresent(r -> registerRecipes(registry, List.of(r)));
		}

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

		for (Supplier<BlockItem> item : ModBlocks.ALL_BARREL_ITEMS) {
			registry.setDefaultComparison(item.get(), barrelNbtInterpreter);
		}
		for (Supplier<BlockItem> item : ModBlocks.CHEST_ITEMS) {
			registry.setDefaultComparison(item.get(), woodStorageNbtInterpreter);
		}

		Comparison shulkerBoxNbtInterpreter = Comparison.compareData(emiStack -> {
			CompoundTag tag = new CompoundTag();
			ItemStack stack = emiStack.getItemStack();
			StorageBlockItem.getMainColorFromStack(stack).ifPresent(mainColor -> tag.putInt("mainColor", mainColor));
			StorageBlockItem.getAccentColorFromStack(stack).ifPresent(accentColor -> tag.putInt("accentColor", accentColor));
			return tag;
		});
		for (Supplier<BlockItem> item : ModBlocks.SHULKER_BOX_ITEMS) {
			registry.setDefaultComparison(item, shulkerBoxNbtInterpreter);
		}

		registry.addRecipeHandler(ModBlocks.STORAGE_CONTAINER_TYPE.get(), new EmiGridMenuInfo<>());

		registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(ModItems.CRAFTING_UPGRADE.get()));
		registry.addWorkstation(VanillaEmiRecipeCategories.STONECUTTING, EmiStack.of(ModItems.STONECUTTER_UPGRADE.get()));

		List<WorkstationEntry> entries = new ArrayList<>();
		WORKSTATIONS.invoker().additionalWorkstations(entries::add);
		for (WorkstationEntry entry : entries) {
			registry.addWorkstation(new EmiRecipeCategory(entry.id, EmiStack.of(entry.icon)), EmiStack.of(entry.workstation));
		}
    }

    private static void registerRecipes(EmiRegistry registry, List<RecipeHolder<CraftingRecipe>> recipes) {
        recipes.forEach(holder -> {
			Recipe<?> r = holder.value();
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

					ingredientsCopy.add(
							i,
							EmiTags.getIngredient(
									Item.class,
									Arrays.stream(ingredient.getItems())
											.map(stack -> EmiStack.of(stack).comparison(Comparison.compareComponents())).toList(),
									amount
							)
					);
				} else {
					ingredientsCopy.add(i, EmiIngredient.of(ingredient));
				}
				i++;
			}

			registry.removeRecipes(holder.id());
			registry.addRecipe(new EmiCraftingRecipe(
							ingredientsCopy,
							EmiStack.of(r.getResultItem(null)),
							ResourceLocation.fromNamespaceAndPath(holder.id().getNamespace(), "/" + holder.id().getPath())
					)
			);
		});
    }
}
