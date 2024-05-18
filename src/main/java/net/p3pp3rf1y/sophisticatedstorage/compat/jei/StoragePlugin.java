package net.p3pp3rf1y.sophisticatedstorage.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SettingsScreen;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.CraftingContainerRecipeTransferHandlerBase;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.SettingsGhostIngredientHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.StorageGhostIngredientHandler;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageScreen;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageSettingsScreen;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorage.compat.common.DyeRecipesMaker;
import net.p3pp3rf1y.sophisticatedstorage.compat.common.FlatBarrelRecipesMaker;
import net.p3pp3rf1y.sophisticatedstorage.compat.common.ShulkerBoxFromChestRecipesMaker;
import net.p3pp3rf1y.sophisticatedstorage.compat.common.TierUpgradeRecipesMaker;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@JeiPlugin
public class StoragePlugin implements IModPlugin {
	private static Consumer<IRecipeCatalystRegistration> additionalCatalystRegistrar = registration -> {};
	public static void setAdditionalCatalystRegistrar(Consumer<IRecipeCatalystRegistration> additionalCatalystRegistrar) {
		StoragePlugin.additionalCatalystRegistrar = additionalCatalystRegistrar;
	}

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(SophisticatedStorage.ID, "default");
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		IIngredientSubtypeInterpreter<ItemStack> woodStorageNbtInterpreter = (itemStack, context) -> {
			StringJoiner result = new StringJoiner(",");
			WoodStorageBlockItem.getWoodType(itemStack).ifPresent(woodName -> result.add("woodName:" + woodName));
			StorageBlockItem.getMainColorFromStack(itemStack).ifPresent(mainColor -> result.add("mainColor:" + mainColor));
			StorageBlockItem.getAccentColorFromStack(itemStack).ifPresent(accentColor -> result.add("accentColor:" + accentColor));
			return "{" + result + "}";
		};
		IIngredientSubtypeInterpreter<ItemStack> barrelNbtInterpreter = (itemStack, context) -> {
			StringJoiner result = new StringJoiner(",");
			WoodStorageBlockItem.getWoodType(itemStack).ifPresent(woodName -> result.add("woodName:" + woodName));
			StorageBlockItem.getMainColorFromStack(itemStack).ifPresent(mainColor -> result.add("mainColor:" + mainColor));
			StorageBlockItem.getAccentColorFromStack(itemStack).ifPresent(accentColor -> result.add("accentColor:" + accentColor));
			result.add("flatTop:" + BarrelBlockItem.isFlatTop(itemStack));
			return "{" + result + "}";
		};

		for (BlockItem item : ModBlocks.ALL_BARREL_ITEMS) {
			registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, barrelNbtInterpreter);
		}
		for (BlockItem item : ModBlocks.CHEST_ITEMS) {
			registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, woodStorageNbtInterpreter);
		}

		IIngredientSubtypeInterpreter<ItemStack> shulkerBoxNbtInterpreter = (itemStack, context) -> {
			StringJoiner result = new StringJoiner(",");
			StorageBlockItem.getMainColorFromStack(itemStack).ifPresent(mainColor -> result.add("mainColor:" + mainColor));
			StorageBlockItem.getAccentColorFromStack(itemStack).ifPresent(accentColor -> result.add("accentColor:" + accentColor));
			return "{" + result + "}";
		};
		for (BlockItem item : ModBlocks.SHULKER_BOX_ITEMS) {
			registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, shulkerBoxNbtInterpreter);
		}
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGuiContainerHandler(StorageScreen.class, new IGuiContainerHandler<>() {
			@Override
			public List<Rect2i> getGuiExtraAreas(StorageScreen gui) {
				List<Rect2i> ret = new ArrayList<>();
				gui.getUpgradeSlotsRectangle().ifPresent(ret::add);
				ret.addAll(gui.getUpgradeSettingsControl().getTabRectangles());
				gui.getSortButtonsRectangle().ifPresent(ret::add);
				return ret;
			}
		});

		registration.addGuiContainerHandler(StorageSettingsScreen.class, new IGuiContainerHandler<>() {
			@Override
			public List<Rect2i> getGuiExtraAreas(StorageSettingsScreen gui) {
				return new ArrayList<>(gui.getSettingsTabControl().getTabRectangles());
			}
		});

		registration.addGhostIngredientHandler(StorageScreen.class, new StorageGhostIngredientHandler<>());
		registration.addGhostIngredientHandler(SettingsScreen.class, new SettingsGhostIngredientHandler<>());
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		registration.addRecipes(RecipeTypes.CRAFTING, DyeRecipesMaker.getRecipes());
		registration.addRecipes(RecipeTypes.CRAFTING, TierUpgradeRecipesMaker.getShapedCraftingRecipes());
		registration.addRecipes(RecipeTypes.CRAFTING, TierUpgradeRecipesMaker.getShapelessCraftingRecipes());
		registration.addRecipes(RecipeTypes.CRAFTING, ShulkerBoxFromChestRecipesMaker.getRecipes());
		registration.addRecipes(RecipeTypes.CRAFTING, FlatBarrelRecipesMaker.getRecipes());
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(ModItems.CRAFTING_UPGRADE), RecipeTypes.CRAFTING);
		registration.addRecipeCatalyst(new ItemStack(ModItems.STONECUTTER_UPGRADE), RecipeTypes.STONECUTTING);
		additionalCatalystRegistrar.accept(registration);
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		IRecipeTransferHandlerHelper handlerHelper = registration.getTransferHelper();
		IStackHelper stackHelper = registration.getJeiHelpers().getStackHelper();
		registration.addRecipeTransferHandler(new CraftingContainerRecipeTransferHandlerBase<StorageContainerMenu>(handlerHelper, stackHelper) {
			@Override
			public Class<StorageContainerMenu> getContainerClass() {
				return StorageContainerMenu.class;
			}
		}, RecipeTypes.CRAFTING);
	}

}
