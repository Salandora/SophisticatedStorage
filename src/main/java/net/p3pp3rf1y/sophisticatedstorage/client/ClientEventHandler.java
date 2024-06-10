package net.p3pp3rf1y.sophisticatedstorage.client;

import com.mojang.blaze3d.platform.InputConstants;

import io.github.fabricators_of_create.porting_lib.event.client.RegisterGeometryLoadersCallback;
import io.github.fabricators_of_create.porting_lib.model.geometry.IGeometryLoader;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedcore.event.client.ClientLifecycleEvent;
import net.p3pp3rf1y.sophisticatedcore.event.client.ClientRawInputEvent;
import net.p3pp3rf1y.sophisticatedcore.util.SimpleIdentifiablePrepareableReloadListener;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.LimitedBarrelBlock;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageScreen;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.ToolInfoOverlay;
import net.p3pp3rf1y.sophisticatedstorage.client.init.ModBlockColors;
import net.p3pp3rf1y.sophisticatedstorage.client.init.ModItemColors;
import net.p3pp3rf1y.sophisticatedstorage.client.init.ModParticles;
import net.p3pp3rf1y.sophisticatedstorage.client.render.BarrelBakedModelBase;
import net.p3pp3rf1y.sophisticatedstorage.client.render.BarrelDynamicModel;
import net.p3pp3rf1y.sophisticatedstorage.client.render.BarrelDynamicModelBase;
import net.p3pp3rf1y.sophisticatedstorage.client.render.BarrelRenderer;
import net.p3pp3rf1y.sophisticatedstorage.client.render.ChestDynamicModel;
import net.p3pp3rf1y.sophisticatedstorage.client.render.ChestItemRenderer;
import net.p3pp3rf1y.sophisticatedstorage.client.render.ChestRenderer;
import net.p3pp3rf1y.sophisticatedstorage.client.render.ClientStorageContentsTooltip;
import net.p3pp3rf1y.sophisticatedstorage.client.render.ControllerRenderer;
import net.p3pp3rf1y.sophisticatedstorage.client.render.LimitedBarrelDynamicModel;
import net.p3pp3rf1y.sophisticatedstorage.client.render.LimitedBarrelRenderer;
import net.p3pp3rf1y.sophisticatedstorage.client.render.LockRenderer;
import net.p3pp3rf1y.sophisticatedstorage.client.render.ShulkerBoxDynamicModel;
import net.p3pp3rf1y.sophisticatedstorage.client.render.ShulkerBoxItemRenderer;
import net.p3pp3rf1y.sophisticatedstorage.client.render.ShulkerBoxRenderer;
import net.p3pp3rf1y.sophisticatedstorage.client.render.SimpleCompositeModel;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageContentsTooltip;
import net.p3pp3rf1y.sophisticatedstorage.network.ScrolledToolMessage;
import net.p3pp3rf1y.sophisticatedstorage.network.StoragePacketHandler;
import net.p3pp3rf1y.sophisticatedstorage.upgrades.compression.CompressionInventoryPart;
import net.p3pp3rf1y.sophisticatedstorage.upgrades.hopper.HopperUpgradeContainer;

import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public class ClientEventHandler {
	private ClientEventHandler() {}

	private static final String KEYBIND_SOPHISTICATEDSTORAGE_CATEGORY = "keybind.sophisticatedstorage.category";
	private static final int MIDDLE_BUTTON = 2;
	public static final KeyMapping SORT_KEYBIND = new KeyMapping(StorageTranslationHelper.INSTANCE.translKeybind("sort"),
			InputConstants.Type.MOUSE, MIDDLE_BUTTON, KEYBIND_SOPHISTICATEDSTORAGE_CATEGORY); // StorageGuiKeyConflictContext.INSTANCE

	private static final ResourceLocation CHEST_RL = new ResourceLocation(SophisticatedStorage.ID, "chest");
	public static final ModelLayerLocation CHEST_LAYER = new ModelLayerLocation(CHEST_RL, "main");

	public static void registerHandlers() {
		RegisterGeometryLoadersCallback.EVENT.register(ClientEventHandler::onModelRegistry);

		ClientSpriteRegistryCallback.event(InventoryMenu.BLOCK_ATLAS).register(ClientEventHandler::stitchTextures);
		ClientSpriteRegistryCallback.event(Sheets.CHEST_SHEET).register(ClientEventHandler::stitchChestTextures);
		ClientSpriteRegistryCallback.event(Sheets.SHULKER_SHEET).register(ClientEventHandler::stitchShulkerBoxTextures);

		ClientEventHandler.registerLayer();
		ClientEventHandler.registerTooltipComponent();
		ClientEventHandler.registerOverlay();
		ClientEventHandler.registerEntityRenderers();
		ClientEventHandler.registerItemRenderers();
		ClientEventHandler.registerKeyMappings();
		ClientEventHandler.registerStorageLayerLoader();
		ClientEventHandler.onRegisterReloadListeners();

		ModelLoadingRegistry.INSTANCE.registerModelProvider(ClientEventHandler::onRegisterAdditionalModels);

		ModParticles.registerProviders();
		ModItemColors.registerItemColorHandlers();
		ModBlockColors.registerBlockColorHandlers();

		ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(ClientStorageContentsTooltip::onWorldLoad);

		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			ScreenKeyboardEvents.allowKeyPress(screen).register(ClientEventHandler::handleGuiKeyPress);
			ScreenMouseEvents.allowMouseClick(screen).register(ClientEventHandler::handleGuiMouseKeyPress);
		});

		AttackBlockCallback.EVENT.register(ClientEventHandler::onLimitedBarrelClicked);
		ClientRawInputEvent.MOUSE_SCROLLED.register(ClientEventHandler::onMouseScrolled);
	}

	private static void onRegisterAdditionalModels(ResourceManager manager, Consumer<ResourceLocation> out) {
		addBarrelPartModelsToBake(manager, out);
	}

	private static void addBarrelPartModelsToBake(ResourceManager manager, Consumer<ResourceLocation> out) {
		Map<ResourceLocation, Resource> models = manager.listResources("models/block/barrel_part", fileName -> fileName.getPath().endsWith(".json"));
		models.forEach((modelName, resource) -> {
			if (modelName.getNamespace().equals(SophisticatedStorage.MOD_ID)) {
				out.accept(new ResourceLocation(modelName.getNamespace(), modelName.getPath().substring("models/".length()).replace(".json", "")));
			}
		});
	}

	private static InteractionResult onMouseScrolled(Minecraft mc, double delta) {
		if (mc.screen != null) {
			return InteractionResult.PASS;
		}

		LocalPlayer player = mc.player;
		if (player == null || !player.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}
		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() != ModItems.STORAGE_TOOL) {
			return InteractionResult.PASS;
		}
		StoragePacketHandler.sendToServer(new ScrolledToolMessage(delta > 0));
		return InteractionResult.SUCCESS;
	}

	private static InteractionResult onLimitedBarrelClicked(Player player, Level level, InteractionHand hand, BlockPos pos, Direction direction) {
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof LimitedBarrelBlock limitedBarrel)) {
			return InteractionResult.PASS;
		}
		if (limitedBarrel.isLookingAtFront(player, pos, state)) {
			if (player.isCreative()) {
				return InteractionResult.SUCCESS;
			} else {
				if (player.getDestroySpeed(state) < 2) {
					Minecraft.getInstance().gameMode.destroyDelay = 5;
					// Necessary cause fabrics version does stop the attack from happening
					// and there is no full equivalent to forges event
					state.getBlock().attack(state, level, pos, player);
					return InteractionResult.SUCCESS;
				}
			}
		}

		return InteractionResult.PASS;
	}

	public static boolean handleGuiKeyPress(Screen screen, int key, int scancode, int modifiers) {
		return !SORT_KEYBIND.isDown() || !tryCallSort(screen);
	}

	public static boolean handleGuiMouseKeyPress(Screen screen, double mouseX, double mouseY, int button) {
		return !SORT_KEYBIND.matchesMouse(button) || !tryCallSort(screen);
	}

	private static void registerStorageLayerLoader() {
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(StorageTextureManager.INSTANCE);
	}

	private static boolean tryCallSort(Screen gui) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && mc.player.containerMenu instanceof StorageContainerMenu container && gui instanceof StorageScreen screen) {
			MouseHandler mh = mc.mouseHandler;
			double mouseX = mh.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
			double mouseY = mh.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
			Slot selectedSlot = screen.findSlot(mouseX, mouseY);
			if (selectedSlot != null && container.isNotPlayersInventorySlot(selectedSlot.index)) {
				container.sort();
				return true;
			}
		}
		return false;
	}

	private static void onModelRegistry(Map<ResourceLocation, IGeometryLoader<?>> loaders) {
		loaders.put(SophisticatedStorage.getRL("barrel"), BarrelDynamicModel.Loader.INSTANCE);
		loaders.put(SophisticatedStorage.getRL("limited_barrel"), LimitedBarrelDynamicModel.Loader.INSTANCE);
		loaders.put(SophisticatedStorage.getRL("chest"), ChestDynamicModel.Loader.INSTANCE);
		loaders.put(SophisticatedStorage.getRL("shulker_box"), ShulkerBoxDynamicModel.Loader.INSTANCE);
		loaders.put(SophisticatedStorage.getRL("simple_composite"), SimpleCompositeModel.Loader.INSTANCE);
	}

	private static void onRegisterReloadListeners() {
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleIdentifiablePrepareableReloadListener<>(SophisticatedStorage.getRL("main")) {
			@Override
			protected void apply(Object object, ResourceManager resourceManager, ProfilerFiller profiler) {
				BarrelDynamicModelBase.invalidateCache();
				BarrelBakedModelBase.invalidateCache();
			}
		});
	}

	public static void registerLayer() {
		EntityModelLayerRegistry.registerModelLayer(CHEST_LAYER, () -> ChestRenderer.createSingleBodyLayer(true));
	}

	private static void registerKeyMappings() {
		KeyBindingHelper.registerKeyBinding(SORT_KEYBIND);
	}

	private static void registerTooltipComponent() {
		TooltipComponentCallback.EVENT.register(ClientEventHandler::registerTooltipComponent);
	}
	@Nullable
	private static ClientTooltipComponent registerTooltipComponent(TooltipComponent data) {
		if (data instanceof StorageContentsTooltip storageContentsTooltip) {
			return new ClientStorageContentsTooltip(storageContentsTooltip);
		}
		return null;
	}

	private static void registerOverlay() {
		HudRenderCallback.EVENT.register(ToolInfoOverlay.HUD_TOOL_INFO);
	}

	private static void stitchTextures(TextureAtlas atlas, ClientSpriteRegistryCallback.Registry registry) {
		stitchBlockAtlasTextures(atlas, registry);
		registry.register(LockRenderer.LOCK_TEXTURE.texture());
		registry.register(LimitedBarrelRenderer.FILL_INDICATORS_TEXTURE.texture());
		registry.register(CompressionInventoryPart.EMPTY_COMPRESSION_SLOT.getSecond());
		registry.register(HopperUpgradeContainer.EMPTY_INPUT_FILTER_SLOT_BACKGROUND.getSecond());
		registry.register(HopperUpgradeContainer.EMPTY_OUTPUT_FILTER_SLOT_BACKGROUND.getSecond());
	}

	private static void stitchShulkerBoxTextures(TextureAtlas atlas, ClientSpriteRegistryCallback.Registry registry) {
		registry.register(ShulkerBoxRenderer.BASE_TIER_MATERIAL.texture());
		registry.register(ShulkerBoxRenderer.COPPER_TIER_MATERIAL.texture());
		registry.register(ShulkerBoxRenderer.IRON_TIER_MATERIAL.texture());
		registry.register(ShulkerBoxRenderer.GOLD_TIER_MATERIAL.texture());
		registry.register(ShulkerBoxRenderer.DIAMOND_TIER_MATERIAL.texture());
		registry.register(ShulkerBoxRenderer.NETHERITE_TIER_MATERIAL.texture());
		registry.register(ShulkerBoxRenderer.TINTABLE_MAIN_MATERIAL.texture());
		registry.register(ShulkerBoxRenderer.TINTABLE_ACCENT_MATERIAL.texture());
		registry.register(ShulkerBoxRenderer.NO_TINT_MATERIAL.texture());
	}

	private static void stitchChestTextures(TextureAtlas atlas, ClientSpriteRegistryCallback.Registry registry) {
		StorageTextureManager.INSTANCE.getUniqueChestMaterials().forEach(mat -> registry.register(mat.texture()));
	}

	private static void stitchBlockAtlasTextures(TextureAtlas atlas, ClientSpriteRegistryCallback.Registry registry) {
		ChestDynamicModel.getWoodBreakTextures().forEach(registry::register);
		registry.register(ChestDynamicModel.TINTABLE_BREAK_TEXTURE);
		registry.register(ShulkerBoxDynamicModel.TINTABLE_BREAK_TEXTURE);
		registry.register(ShulkerBoxDynamicModel.MAIN_BREAK_TEXTURE);
	}

	private static void registerEntityRenderers() {
		BlockEntityRenderers.register(ModBlocks.BARREL_BLOCK_ENTITY_TYPE, context -> new BarrelRenderer<>());
		BlockEntityRenderers.register(ModBlocks.LIMITED_BARREL_BLOCK_ENTITY_TYPE, context -> new LimitedBarrelRenderer());
		BlockEntityRenderers.register(ModBlocks.CHEST_BLOCK_ENTITY_TYPE, ChestRenderer::new);
		BlockEntityRenderers.register(ModBlocks.SHULKER_BOX_BLOCK_ENTITY_TYPE, ShulkerBoxRenderer::new);
		BlockEntityRenderers.register(ModBlocks.CONTROLLER_BLOCK_ENTITY_TYPE, context -> new ControllerRenderer());

		BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), ModBlocks.getBlocksByPredicate((id, block) -> id.getPath().contains("barrel")).toArray(new Block[0]));
	}

	private static void registerItemRenderers() {
		BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.CHEST_ITEM, ChestItemRenderer::render);
		BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.COPPER_CHEST_ITEM, ChestItemRenderer::render);
		BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.IRON_CHEST_ITEM, ChestItemRenderer::render);
		BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.GOLD_CHEST_ITEM, ChestItemRenderer::render);
		BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.DIAMOND_CHEST_ITEM, ChestItemRenderer::render);
		BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.NETHERITE_CHEST_ITEM, ChestItemRenderer::render);

		BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.SHULKER_BOX_ITEM, ShulkerBoxItemRenderer::render);
		BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.COPPER_SHULKER_BOX_ITEM, ShulkerBoxItemRenderer::render);
		BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.IRON_SHULKER_BOX_ITEM, ShulkerBoxItemRenderer::render);
		BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.GOLD_SHULKER_BOX_ITEM, ShulkerBoxItemRenderer::render);
		BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.DIAMOND_SHULKER_BOX_ITEM, ShulkerBoxItemRenderer::render);
		BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.NETHERITE_SHULKER_BOX_ITEM, ShulkerBoxItemRenderer::render);
	}
}
