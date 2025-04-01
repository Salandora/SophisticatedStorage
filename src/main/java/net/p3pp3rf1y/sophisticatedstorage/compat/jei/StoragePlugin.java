package net.p3pp3rf1y.sophisticatedstorage.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.*;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SettingsScreen;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.ClientRecipeHelper;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.CraftingContainerRecipeTransferHandlerBase;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.SettingsGhostIngredientHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.StorageGhostIngredientHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.subtypes.PropertyBasedSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageScreen;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageSettingsScreen;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorage.compat.jei.subtypes.BarrelSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorage.compat.jei.subtypes.ChestSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorage.compat.jei.subtypes.ShulkerBoxSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorage.crafting.ShulkerBoxFromVanillaShapelessRecipe;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@JeiPlugin
public class StoragePlugin implements IModPlugin {
	private static Consumer<IRecipeCatalystRegistration> additionalCatalystRegistrar = registration -> {
	};
	private final PropertyBasedSubtypeInterpreter chestSubtypeInterpreter = new ChestSubtypeInterpreter();
	private final PropertyBasedSubtypeInterpreter barrelSubtypeInterpreter = new BarrelSubtypeInterpreter();
	private final PropertyBasedSubtypeInterpreter shulkerBoxSubtypeInterpreter = new ShulkerBoxSubtypeInterpreter();

	private Map<BlockItem, PropertyBasedSubtypeInterpreter> getSubtypeIntepreters() {
		return new HashMap<>(){{
			put(ModBlocks.BARREL_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.COPPER_BARREL_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.IRON_BARREL_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.GOLD_BARREL_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.DIAMOND_BARREL_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.NETHERITE_BARREL_ITEM.get(), barrelSubtypeInterpreter);

			put(ModBlocks.CHEST_ITEM.get(), chestSubtypeInterpreter);
			put(ModBlocks.COPPER_CHEST_ITEM.get(), chestSubtypeInterpreter);
			put(ModBlocks.IRON_CHEST_ITEM.get(), chestSubtypeInterpreter);
			put(ModBlocks.GOLD_CHEST_ITEM.get(), chestSubtypeInterpreter);
			put(ModBlocks.DIAMOND_CHEST_ITEM.get(), chestSubtypeInterpreter);
			put(ModBlocks.NETHERITE_CHEST_ITEM.get(), chestSubtypeInterpreter);

			put(ModBlocks.LIMITED_BARREL_1_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_BARREL_2_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_BARREL_3_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_BARREL_4_ITEM.get(), barrelSubtypeInterpreter);

			put(ModBlocks.LIMITED_COPPER_BARREL_1_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_COPPER_BARREL_2_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_COPPER_BARREL_3_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_COPPER_BARREL_4_ITEM.get(), barrelSubtypeInterpreter);

			put(ModBlocks.LIMITED_IRON_BARREL_1_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_IRON_BARREL_2_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_IRON_BARREL_3_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_IRON_BARREL_4_ITEM.get(), barrelSubtypeInterpreter);

			put(ModBlocks.LIMITED_GOLD_BARREL_1_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_GOLD_BARREL_2_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_GOLD_BARREL_3_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_GOLD_BARREL_4_ITEM.get(), barrelSubtypeInterpreter);

			put(ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM.get(), barrelSubtypeInterpreter);

			put(ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_NETHERITE_BARREL_2_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_NETHERITE_BARREL_3_ITEM.get(), barrelSubtypeInterpreter);
			put(ModBlocks.LIMITED_NETHERITE_BARREL_4_ITEM.get(), barrelSubtypeInterpreter);

			put(ModBlocks.SHULKER_BOX_ITEM.get(), shulkerBoxSubtypeInterpreter);
			put(ModBlocks.COPPER_SHULKER_BOX_ITEM.get(), shulkerBoxSubtypeInterpreter);
			put(ModBlocks.IRON_SHULKER_BOX_ITEM.get(), shulkerBoxSubtypeInterpreter);
			put(ModBlocks.GOLD_SHULKER_BOX_ITEM.get(), shulkerBoxSubtypeInterpreter);
			put(ModBlocks.DIAMOND_SHULKER_BOX_ITEM.get(), shulkerBoxSubtypeInterpreter);
			put(ModBlocks.NETHERITE_SHULKER_BOX_ITEM.get(), shulkerBoxSubtypeInterpreter);
		}};
	}

	private Optional<PropertyBasedSubtypeInterpreter> getSubtypeInterpreter(Map<BlockItem, PropertyBasedSubtypeInterpreter> subtypeInterpreters, ItemStack stack) {
		if (!(stack.getItem() instanceof BlockItem blockItem)) {
			return Optional.empty();
		}

		return Optional.ofNullable(subtypeInterpreters.get(blockItem));
	}

	public static void setAdditionalCatalystRegistrar(Consumer<IRecipeCatalystRegistration> additionalCatalystRegistrar) {
		StoragePlugin.additionalCatalystRegistrar = additionalCatalystRegistrar;
	}

	@Override
	public ResourceLocation getPluginUid() {
		return ResourceLocation.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "default");
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		getSubtypeIntepreters().forEach(registration::registerSubtypeInterpreter);
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
		Map<BlockItem, PropertyBasedSubtypeInterpreter> subtypeInterpreters = getSubtypeIntepreters();
		registration.addRecipes(RecipeTypes.CRAFTING, DyeRecipesMaker.getRecipes(stack -> getSubtypeInterpreter(subtypeInterpreters, stack)));
		registration.addRecipes(RecipeTypes.CRAFTING, TierUpgradeRecipesMaker.getShapedCraftingRecipes(stack -> getSubtypeInterpreter(subtypeInterpreters, stack)));
		registration.addRecipes(RecipeTypes.CRAFTING, TierUpgradeRecipesMaker.getShapelessCraftingRecipes(stack -> getSubtypeInterpreter(subtypeInterpreters, stack)));
		registration.addRecipes(RecipeTypes.CRAFTING, ShulkerBoxFromChestRecipesMaker.getRecipes(stack -> getSubtypeInterpreter(subtypeInterpreters, stack)));
		registration.addRecipes(RecipeTypes.CRAFTING, ClientRecipeHelper.transformAllRecipesOfType(RecipeType.CRAFTING, ShulkerBoxFromVanillaShapelessRecipe.class, ClientRecipeHelper::copyShapelessRecipe));
		registration.addRecipes(RecipeTypes.CRAFTING, FlatBarrelRecipesMaker.getRecipes());
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(ModItems.CRAFTING_UPGRADE.get()), RecipeTypes.CRAFTING);
		registration.addRecipeCatalyst(new ItemStack(ModItems.STONECUTTER_UPGRADE.get()), RecipeTypes.STONECUTTING);
		additionalCatalystRegistrar.accept(registration);
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		IRecipeTransferHandlerHelper handlerHelper = registration.getTransferHelper();
		IStackHelper stackHelper = registration.getJeiHelpers().getStackHelper();
		registration.addRecipeTransferHandler(new CraftingContainerRecipeTransferHandlerBase<StorageContainerMenu, RecipeHolder<CraftingRecipe>>(handlerHelper, stackHelper) {
			@Override
			public Class<StorageContainerMenu> getContainerClass() {
				return StorageContainerMenu.class;
			}

			@Override
			public mezz.jei.api.recipe.RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
				return RecipeTypes.CRAFTING;
			}
		}, RecipeTypes.CRAFTING);
	}

}
