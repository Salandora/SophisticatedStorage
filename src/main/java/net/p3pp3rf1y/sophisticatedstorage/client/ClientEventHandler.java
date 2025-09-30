package net.p3pp3rf1y.sophisticatedstorage.client;

import com.github.salandora.sophisticatedlibrary.event.api.client.ClientLifecycleEvents;
import com.github.salandora.sophisticatedlibrary.event.api.client.ClientRawInputEvent;
import com.github.salandora.sophisticatedlibrary.model.loading.IGeometryLoader;
import com.github.salandora.sophisticatedlibrary.model.loading.RegisterGeometryLoadersCallback;
import com.github.salandora.sophisticatedlibrary.network.PacketDistributor;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.util.SimpleIdentifiablePrepareableReloadListener;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelBlock;
import net.p3pp3rf1y.sophisticatedstorage.block.LimitedBarrelBlock;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.PaintbrushOverlay;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageScreen;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.ToolInfoOverlay;
import net.p3pp3rf1y.sophisticatedstorage.client.init.ModBlockColors;
import net.p3pp3rf1y.sophisticatedstorage.client.init.ModItemColors;
import net.p3pp3rf1y.sophisticatedstorage.client.init.ModParticles;
import net.p3pp3rf1y.sophisticatedstorage.client.render.*;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModCompat;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.ChestBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.PaintbrushItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageContentsTooltip;
import net.p3pp3rf1y.sophisticatedstorage.network.RequestPlayerSettingsPayload;
import net.p3pp3rf1y.sophisticatedstorage.network.ScrolledToolPayload;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ClientEventHandler {
	private ClientEventHandler() {
	}

	private static final String KEYBIND_SOPHISTICATEDSTORAGE_CATEGORY = "keybind.sophisticatedstorage.category";
	private static final int MIDDLE_BUTTON = 2;
	public static final KeyMapping SORT_KEYBIND = new KeyMapping(StorageTranslationHelper.INSTANCE.translKeybind("sort"),
			InputConstants.Type.MOUSE, MIDDLE_BUTTON, KEYBIND_SOPHISTICATEDSTORAGE_CATEGORY); // StorageGuiKeyConflictContext.INSTANCE

	private static Set<Predicate<StorageScreenBase<?>>> SORT_SCREEN_MATCHERS = new HashSet<>();
	static {
		SORT_SCREEN_MATCHERS.add(screen -> screen instanceof StorageScreen);
	}

	public static void addSortScreenMatcher(Predicate<StorageScreenBase<?>> matcher) {
		SORT_SCREEN_MATCHERS.add(matcher);
	}

	// TODO: Sort screen matchers
	/*@SuppressWarnings("java:S6548") //singleton is intended here
	private static class StorageGuiKeyConflictContext implements IKeyConflictContext {
		public static final StorageGuiKeyConflictContext INSTANCE = new StorageGuiKeyConflictContext();

		@Override
		public boolean isActive() {
			return GUI.isActive() && Minecraft.getInstance().screen instanceof StorageScreenBase<?> storageScreen
					&& SORT_SCREEN_MATCHERS.stream().anyMatch(matcher -> matcher.test(storageScreen));
		}

		@Override
		public boolean conflicts(IKeyConflictContext other) {
			return this == other;
		}
	}*/

	private static final ResourceLocation CHEST_RL = ResourceLocation.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "chest");
	private static final ResourceLocation CHEST_LEFT_RL = ResourceLocation.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "chest_left");
	private static final ResourceLocation CHEST_RIGHT_RL = ResourceLocation.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "chest_right");
	public static final ModelLayerLocation CHEST_LAYER = new ModelLayerLocation(CHEST_RL, "main");
	public static final ModelLayerLocation CHEST_LEFT_LAYER = new ModelLayerLocation(CHEST_LEFT_RL, "main");
	public static final ModelLayerLocation CHEST_RIGHT_LAYER = new ModelLayerLocation(CHEST_RIGHT_RL, "main");

	public static void registerHandlers() {
		RegisterGeometryLoadersCallback.register(ClientEventHandler::onModelRegistry);
		ClientEventHandler.registerLayer();
		ClientEventHandler.registerTooltipComponent();
		ClientEventHandler.registerOverlay();
		ClientEventHandler.registerEntityRenderers();
		ModParticles.registerProviders();
		ClientEventHandler.registerKeyMappings();
		ModItemColors.registerItemColorHandlers();
		ModBlockColors.registerBlockColorHandlers();
		ClientEventHandler.registerStorageLayerLoader();
		PreparableModelLoadingPlugin.register(((resourceManager, executor) -> CompletableFuture.completedFuture(resourceManager)), (resourceManager, context) -> onRegisterAdditionalModels(resourceManager, context::addModels));
		ClientEventHandler.onRegisterReloadListeners();
		ClientEventHandler.registerStorageClientExtensions();

		ClientLifecycleEvents.CLIENT_LEVEL_LOAD.register(ClientStorageContentsTooltip::onWorldLoad);
		if (!FabricLoader.getInstance().isModLoaded(ModCompat.MKB)) {
			ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
				ScreenKeyboardEvents.allowKeyPress(screen).register(ClientEventHandler::handleGuiKeyPress);
				ScreenMouseEvents.allowMouseClick(screen).register(ClientEventHandler::handleGuiMouseKeyPress);
			});
		}
		AttackBlockCallback.EVENT.register(ClientEventHandler::onLimitedBarrelClicked);
		ClientRawInputEvent.MOUSE_SCROLLED.register(ClientEventHandler::onMouseScrolled);
		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(ClientEventHandler::onRenderHighlight);
		ClientPlayConnectionEvents.JOIN.register(ClientEventHandler::onPlayerLoggingIn);
	}

	private static void onPlayerLoggingIn(ClientPacketListener clientPacketListener, PacketSender packetSender, Minecraft minecraft) {
		PacketDistributor.sendToServer(new RequestPlayerSettingsPayload());
	}

	@SuppressWarnings("SameReturnValue")
	private static boolean onRenderHighlight(WorldRenderContext context, @Nullable HitResult hitResult) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.screen != null || !(hitResult instanceof BlockHitResult blockHitResult)) {
			return true;
		}

		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() instanceof ChestBlockItem && ChestBlockItem.isDoubleChest(stack)) {
			BlockPos otherPos = blockHitResult.getBlockPos().relative(player.getDirection().getClockWise());
			Level level = player.level();
			BlockState blockState = level.getBlockState(otherPos);
			if (!blockState.isAir() && level.getWorldBorder().isWithinBounds(otherPos)) {
				VertexConsumer vertexConsumer = context.consumers().getBuffer(RenderType.lines());
				Vec3 cameraPos = context.camera().getPosition();
				LevelRenderer.renderShape(context.matrixStack(), vertexConsumer, blockState.getShape(level, otherPos, CollisionContext.of(context.camera().getEntity())),
						otherPos.getX() - cameraPos.x, otherPos.getY() - cameraPos.y, otherPos.getZ() - cameraPos.z, 0.0F, 0.0F, 0.0F, 0.4F);
			}
		}

		if (stack.getItem() instanceof PaintbrushItem) {
			Level level = player.level();
			BlockPos pos = blockHitResult.getBlockPos();
			BlockState blockState = level.getBlockState(pos);

			if (blockState.getBlock() instanceof StorageBlockBase || blockState.getBlock() == ModBlocks.CONTROLLER.get()) {
				AtomicBoolean canceled = new AtomicBoolean(false);
				PaintbrushOverlay.getItemRequirementsFor(stack, player, level, pos).ifPresent(itemRequirements -> {
					float red = !itemRequirements.itemsMissing().isEmpty() ? 1 : 0;
					float green = itemRequirements.itemsMissing().isEmpty() ? 1 : 0;
					VertexConsumer vertexConsumer = context.consumers().getBuffer(RenderType.lines());
					Vec3 cameraPos = context.camera().getPosition();
					PoseStack poseStack = context.matrixStack();
					LevelRenderer.renderShape(poseStack, vertexConsumer, blockState.getShape(level, pos, CollisionContext.of(context.camera().getEntity())),
							pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z, red, green, 0.0F, 1);
					canceled.set(true);
				});
				// If the item requirements are missing, we want to render the block outline
				return !canceled.get();
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
				out.accept(ResourceLocation.fromNamespaceAndPath(modelName.getNamespace(), modelName.getPath().substring("models/".length()).replace(".json", "")));
			}
		});
	}

	private static InteractionResult onMouseScrolled(Minecraft mc, double deltaX, double deltaY) {
		if (mc.screen != null) {
			return InteractionResult.PASS;
		}
		LocalPlayer player = mc.player;
		if (player == null || !player.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}
		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() != ModItems.STORAGE_TOOL.get()) {
			return InteractionResult.PASS;
		}
		PacketDistributor.sendToServer(new ScrolledToolPayload(deltaX > 0));
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
					// Necessary cause fabrics version does stop the attack from happening
					// and there is no full equivalent to forges event
					state.getBlock().attack(state, level, pos, player);
					Minecraft.getInstance().gameMode.destroyDelay = 5;
				}
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	public static boolean handleGuiKeyPress(Screen screen, int key, int scancode, int modifiers) {
		return !SORT_KEYBIND.matches(key, scancode) || !tryCallSort(screen);
	}

	private static void registerStorageLayerLoader() {
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(StorageTextureManager.INSTANCE);
	}

	public static boolean handleGuiMouseKeyPress(Screen screen, double mouseX, double mouseY, int button) {
		return !SORT_KEYBIND.matchesMouse(button) || !tryCallSort(screen);
	}

	public static boolean tryCallSort(Screen gui) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && mc.player.containerMenu instanceof StorageContainerMenuBase<?> container && gui instanceof StorageScreenBase<?> screen) {
			MouseHandler mh = mc.mouseHandler;
			double mouseX = mh.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
			double mouseY = mh.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
			Slot selectedSlot = screen.findSlot(mouseX, mouseY);
			if (selectedSlot == null || container.isNotPlayersInventorySlot(selectedSlot.index)) {
				container.sort();
				return true;
			}
		}
		return false;
	}

	private static void onModelRegistry(Map<ResourceLocation, IGeometryLoader<?>> loaders) {
		loaders.put(ResourceLocation.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "barrel"), BarrelDynamicModel.Loader.INSTANCE);
		loaders.put(ResourceLocation.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "limited_barrel"), LimitedBarrelDynamicModel.Loader.INSTANCE);
		loaders.put(ResourceLocation.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "chest"), ChestDynamicModel.Loader.INSTANCE);
		loaders.put(ResourceLocation.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "shulker_box"), ShulkerBoxDynamicModel.Loader.INSTANCE);
		loaders.put(ResourceLocation.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "simple_composite"), SimpleCompositeModel.Loader.INSTANCE);
	}

	private static void onRegisterReloadListeners() {
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleIdentifiablePrepareableReloadListener<>(SophisticatedStorage.getRL("main")) {
			@Override
			protected Object prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
				BarrelDynamicModelBase.invalidateCache();
				BarrelBakedModelBase.invalidateCache();
				return super.prepare(resourceManager, profiler);
			}

			@Override
			protected void apply(Object object, ResourceManager resourceManager, ProfilerFiller profiler) {
			}
		});
	}

	public static void registerLayer() {
		EntityModelLayerRegistry.registerModelLayer(CHEST_LAYER, () -> ChestRenderer.createSingleBodyLayer(true));
		EntityModelLayerRegistry.registerModelLayer(CHEST_LEFT_LAYER, ChestRenderer::createDoubleBodyLeftLayer);
		EntityModelLayerRegistry.registerModelLayer(CHEST_RIGHT_LAYER, ChestRenderer::createDoubleBodyRightLayer);
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
//		event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "paintbrush_info"), PaintbrushOverlay.HUD_PAINTBRUSH_INFO);
//		event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(SophisticatedStorage.MOD_ID, "storage_tool_info"), ToolInfoOverlay.HUD_TOOL_INFO);

		HudRenderCallback.EVENT.register(PaintbrushOverlay.HUD_PAINTBRUSH_INFO);
		HudRenderCallback.EVENT.register(ToolInfoOverlay.HUD_TOOL_INFO);
	}

	private static void registerEntityRenderers() {
		BlockEntityRenderers.register(ModBlocks.BARREL_BLOCK_ENTITY_TYPE.get(), context -> new BarrelRenderer<>());
		BlockEntityRenderers.register(ModBlocks.LIMITED_BARREL_BLOCK_ENTITY_TYPE.get(), context -> new LimitedBarrelRenderer());
		BlockEntityRenderers.register(ModBlocks.CHEST_BLOCK_ENTITY_TYPE.get(), ChestRenderer::new);
		BlockEntityRenderers.register(ModBlocks.SHULKER_BOX_BLOCK_ENTITY_TYPE.get(), ShulkerBoxRenderer::new);
		BlockEntityRenderers.register(ModBlocks.CONTROLLER_BLOCK_ENTITY_TYPE.get(), context -> new ControllerRenderer());
		BlockEntityRenderers.register(ModBlocks.DECORATION_TABLE_BLOCK_ENTITY_TYPE.get(), DecorationTableRenderer::new);

		BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), ModBlocks.ALL_BARRELS.stream().map(Supplier::get).toArray(BarrelBlock[]::new));
	}

	private static void registerStorageClientExtensions() {
		/*registerBarrelClientExtensions(event,
				ModBlocks.BARREL.get(), ModBlocks.COPPER_BARREL.get(), ModBlocks.IRON_BARREL.get(), ModBlocks.GOLD_BARREL.get(), ModBlocks.DIAMOND_BARREL.get(), ModBlocks.NETHERITE_BARREL.get(),
				ModBlocks.LIMITED_BARREL_1.get(), ModBlocks.LIMITED_COPPER_BARREL_1.get(), ModBlocks.LIMITED_IRON_BARREL_1.get(), ModBlocks.LIMITED_GOLD_BARREL_1.get(), ModBlocks.LIMITED_DIAMOND_BARREL_1.get(), ModBlocks.LIMITED_NETHERITE_BARREL_1.get(),
				ModBlocks.LIMITED_BARREL_2.get(), ModBlocks.LIMITED_COPPER_BARREL_2.get(), ModBlocks.LIMITED_IRON_BARREL_2.get(), ModBlocks.LIMITED_GOLD_BARREL_2.get(), ModBlocks.LIMITED_DIAMOND_BARREL_2.get(), ModBlocks.LIMITED_NETHERITE_BARREL_2.get(),
				ModBlocks.LIMITED_BARREL_3.get(), ModBlocks.LIMITED_COPPER_BARREL_3.get(), ModBlocks.LIMITED_IRON_BARREL_3.get(), ModBlocks.LIMITED_GOLD_BARREL_3.get(), ModBlocks.LIMITED_DIAMOND_BARREL_3.get(), ModBlocks.LIMITED_NETHERITE_BARREL_3.get(),
				ModBlocks.LIMITED_BARREL_4.get(), ModBlocks.LIMITED_COPPER_BARREL_4.get(), ModBlocks.LIMITED_IRON_BARREL_4.get(), ModBlocks.LIMITED_GOLD_BARREL_4.get(), ModBlocks.LIMITED_DIAMOND_BARREL_4.get(), ModBlocks.LIMITED_NETHERITE_BARREL_4.get()
		);*/
		for (Supplier<BlockItem> item : ModBlocks.CHEST_ITEMS) {
			BuiltinItemRendererRegistry.INSTANCE.register(item.get(), ChestItemRenderer::render);
		}

		for (Supplier<BlockItem> item : ModBlocks.SHULKER_BOX_ITEMS) {
			BuiltinItemRendererRegistry.INSTANCE.register(item.get(), ShulkerBoxItemRenderer::render);
		}
	}

	/*private static void registerBarrelClientExtensions(RegisterClientExtensionsEvent event, BarrelBlock... barrelBlocks) {
		for (int i = 0; i < barrelBlocks.length; i++) {
			event.registerBlock(new BarrelBlockClientExtensions(barrelBlocks[i]), barrelBlocks[i]);
		}
	}*/
}
