package net.p3pp3rf1y.sophisticatedstorage.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mkb.api.IKeyBinding;
import committee.nova.mkb.api.IKeyConflictContext;

import io.github.fabricators_of_create.porting_lib.models.geometry.IGeometryLoader;
import io.github.fabricators_of_create.porting_lib.models.geometry.RegisterGeometryLoadersCallback;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
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
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
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
import net.p3pp3rf1y.sophisticatedstorage.client.render.ShulkerBoxDynamicModel;
import net.p3pp3rf1y.sophisticatedstorage.client.render.ShulkerBoxItemRenderer;
import net.p3pp3rf1y.sophisticatedstorage.client.render.ShulkerBoxRenderer;
import net.p3pp3rf1y.sophisticatedstorage.client.render.SimpleCompositeModel;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.ChestBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageContentsTooltip;
import net.p3pp3rf1y.sophisticatedstorage.mixin.client.accessor.LevelRendererAccessor;
import net.p3pp3rf1y.sophisticatedstorage.mixin.client.accessor.MultiPlayerGameModeAccessor;
import net.p3pp3rf1y.sophisticatedstorage.network.ScrolledToolMessage;
import net.p3pp3rf1y.sophisticatedstorage.network.StoragePacketHandler;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import static committee.nova.mkb.keybinding.KeyConflictContext.GUI;

public class ClientEventHandler {
	private ClientEventHandler() {}

	private static final String KEYBIND_SOPHISTICATEDSTORAGE_CATEGORY = "keybind.sophisticatedstorage.category";
	private static final int MIDDLE_BUTTON = 2;
	public static final KeyMapping SORT_KEYBIND = new KeyMapping(StorageTranslationHelper.INSTANCE.translKeybind("sort"),
			InputConstants.Type.MOUSE, MIDDLE_BUTTON, KEYBIND_SOPHISTICATEDSTORAGE_CATEGORY); // StorageGuiKeyConflictContext.INSTANCE

	@SuppressWarnings("java:S6548") //singleton is intended here
	private static class StorageGuiKeyConflictContext implements IKeyConflictContext {
		public static final StorageGuiKeyConflictContext INSTANCE = new StorageGuiKeyConflictContext();

		@Override
		public boolean isActive() {
			return GUI.isActive() && Minecraft.getInstance().screen instanceof StorageScreen;
		}

		@Override
		public boolean conflicts(IKeyConflictContext other) {
			return this == other;
		}
	}

	private static final ResourceLocation CHEST_RL = new ResourceLocation(SophisticatedStorage.MOD_ID, "chest");
	private static final ResourceLocation CHEST_LEFT_RL = new ResourceLocation(SophisticatedStorage.MOD_ID, "chest_left");
	private static final ResourceLocation CHEST_RIGHT_RL = new ResourceLocation(SophisticatedStorage.MOD_ID, "chest_right");
	public static final ModelLayerLocation CHEST_LAYER = new ModelLayerLocation(CHEST_RL, "main");
	public static final ModelLayerLocation CHEST_LEFT_LAYER = new ModelLayerLocation(CHEST_LEFT_RL, "main");
	public static final ModelLayerLocation CHEST_RIGHT_LAYER = new ModelLayerLocation(CHEST_RIGHT_RL, "main");

	public static void registerHandlers() {
		RegisterGeometryLoadersCallback.EVENT.register(ClientEventHandler::onModelRegistry);

		ClientEventHandler.registerLayer();
		ClientEventHandler.registerTooltipComponent();
		ClientEventHandler.registerOverlay();
		ClientEventHandler.registerEntityRenderers();
		ClientEventHandler.registerItemRenderers();
		ClientEventHandler.registerKeyMappings();
		ClientEventHandler.registerStorageLayerLoader();
		ClientEventHandler.onRegisterReloadListeners();

		PreparableModelLoadingPlugin.register(((resourceManager, executor) -> CompletableFuture.completedFuture(resourceManager)), (resourceManager, context) -> onRegisterAdditionalModels(resourceManager, context::addModels));

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

		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(ClientEventHandler::onRenderHighlight);
	}

	@SuppressWarnings("SameReturnValue")
	private static boolean onRenderHighlight(WorldRenderContext context, @Nullable HitResult hitResult) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return true;
		}

		if (player.getMainHandItem().getItem() instanceof ChestBlockItem && ChestBlockItem.isDoubleChest(player.getMainHandItem())) {
			BlockHitResult blockHitResult = (BlockHitResult) hitResult;
			BlockPos otherPos = blockHitResult.getBlockPos().relative(player.getDirection().getClockWise());
			Level level = player.level();
			BlockState blockState = level.getBlockState(otherPos);
			if (!blockState.isAir() && level.getWorldBorder().isWithinBounds(otherPos)) {
				VertexConsumer vertexConsumer = context.consumers().getBuffer(RenderType.lines());
				Vec3 cameraPos = context.camera().getPosition();
				LevelRendererAccessor.renderShape(context.matrixStack(), vertexConsumer, blockState.getShape(level, otherPos, CollisionContext.of(context.camera().getEntity())),
						otherPos.getX() - cameraPos.x, otherPos.getY() - cameraPos.y, otherPos.getZ() - cameraPos.z, 0.0F, 0.0F, 0.0F, 0.4F);
			}
		}

		return true;
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
					((MultiPlayerGameModeAccessor) Minecraft.getInstance().gameMode).setDestroyDelay(5);
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
		return !((IKeyBinding) SORT_KEYBIND).isActiveAndMatches(InputConstants.getKey(key, scancode)) || !tryCallSort(screen);
	}

	private static void registerStorageLayerLoader() {
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(StorageTextureManager.INSTANCE);
	}

	public static boolean handleGuiMouseKeyPress(Screen screen, double mouseX, double mouseY, int button) {
		InputConstants.Key input = InputConstants.Type.MOUSE.getOrCreate(button);
		return !((IKeyBinding) SORT_KEYBIND).isActiveAndMatches(input) || !tryCallSort(screen);
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
		EntityModelLayerRegistry.registerModelLayer(CHEST_LEFT_LAYER, ChestRenderer::createDoubleBodyLeftLayer);
		EntityModelLayerRegistry.registerModelLayer(CHEST_RIGHT_LAYER, ChestRenderer::createDoubleBodyRightLayer);
	}

	private static void registerKeyMappings() {
		((IKeyBinding)SORT_KEYBIND).setKeyConflictContext(StorageGuiKeyConflictContext.INSTANCE);

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
