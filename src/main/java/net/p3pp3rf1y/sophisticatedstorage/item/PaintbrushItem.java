package net.p3pp3rf1y.sophisticatedstorage.item;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.util.*;
import net.p3pp3rf1y.sophisticatedstorage.block.*;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.util.DecorationHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class PaintbrushItem extends ItemBase {
	public static final Codec<Map<ResourceLocation, Integer>> REMAINING_PARTS_CODEC =
			Codec.unboundedMap(ResourceLocation.CODEC, ExtraCodecs.POSITIVE_INT);

	public static final StreamCodec<FriendlyByteBuf, Map<ResourceLocation, Integer>> REMAINING_PARTS_STREAM_CODEC =
			StreamCodec.of((buf, map) -> buf.writeMap(map, ResourceLocation.STREAM_CODEC, ByteBufCodecs.INT),
					buf -> buf.readMap(ResourceLocation.STREAM_CODEC, ByteBufCodecs.INT));

	public PaintbrushItem() {
		super(new Properties().stacksTo(1));
	}

	public static void setBarrelMaterials(ItemStack paintbrush, Map<BarrelMaterial, ResourceLocation> materials) {
		paintbrush.sophisticatedLibrary_set(ModDataComponents.BARREL_MATERIALS, Map.copyOf(materials));
		resetMainColor(paintbrush);
		resetAccentColor(paintbrush);
	}

	public static Optional<ItemRequirements> getItemRequirements(ItemStack paintbrush, Player player, Level level, BlockPos lookingAtPos) {
		Map<BarrelMaterial, ResourceLocation> materialsToApply = new HashMap<>(getBarrelMaterials(paintbrush));
		BlockEntity be = level.getBlockEntity(lookingAtPos);
		if (be == null) {
			return Optional.empty();
		}

		if (!materialsToApply.isEmpty()) {
			return getMaterialItemRequirements(paintbrush, player, be, materialsToApply);
		} else {
			return getDyeItemRequirements(paintbrush, player, be);
		}
	}

	private static Optional<ItemRequirements> getMaterialItemRequirements(ItemStack paintbrush, Player player, BlockEntity be, Map<BarrelMaterial, ResourceLocation> materialsToApply) {
		Map<ResourceLocation, Integer> allPartsNeeded = new HashMap<>();
		if (be instanceof IMaterialHolder materialHolder) {
			allPartsNeeded = getMaterialHolderPartsNeeded(materialsToApply, materialHolder);
		} else if (be instanceof ControllerBlockEntity controllerBe) {
			for (BlockPos storagePosition : controllerBe.getStoragePositions()) {
				addStorageMaterialPartsNeeded(materialsToApply, controllerBe, storagePosition, allPartsNeeded);
			}
		}

		if (allPartsNeeded.isEmpty()) {
			return Optional.empty();
		}
		return getItemRequirements(paintbrush, player, allPartsNeeded);
	}

	public static Optional<ItemRequirements> getItemRequirements(ItemStack paintbrush, Player player, Map<ResourceLocation, Integer> allPartsNeeded) {
		Map<ResourceLocation, Integer> remainingParts = getRemainingParts(paintbrush);
		DecorationHelper.ConsumptionResult result = DecorationHelper.fabric_consumeMaterialPartsNeeded(allPartsNeeded, remainingParts, InventoryHelper.getItemHandlersFromPlayerIncludingContainers(player), true);

		List<ItemStack> itemsPresent = new ArrayList<>();
		List<ItemStack> itemsMissing = new ArrayList<>();

		for (Map.Entry<ResourceLocation, Integer> entry : allPartsNeeded.entrySet()) {
			ResourceLocation part = entry.getKey();
			int count = Math.ceilDiv(entry.getValue() - remainingParts.getOrDefault(part, 0), DecorationHelper.BLOCK_TOTAL_PARTS);
			int missing = Math.ceilDiv(result.missingParts().getOrDefault(part, 0), DecorationHelper.BLOCK_TOTAL_PARTS);
			int present = count - missing;
			BuiltInRegistries.ITEM.getOptional(part).ifPresent(item -> {
				if (missing > 0) {
					itemsMissing.add(new ItemStack(item, missing));
				}
				if (present > 0) {
					itemsPresent.add(new ItemStack(item, present));
				}
			});
		}

		return Optional.of(new ItemRequirements(itemsPresent, itemsMissing));
	}

	private static void addStorageMaterialPartsNeeded(Map<BarrelMaterial, ResourceLocation> materialsToApply, ControllerBlockEntity controllerBe, BlockPos storagePosition, Map<ResourceLocation, Integer> allPartsNeeded) {
		WorldHelper.getBlockEntity(controllerBe.getLevel(), storagePosition, IMaterialHolder.class).ifPresent(materialHolder -> {
			Map<ResourceLocation, Integer> storagePartsNeeded = getMaterialHolderPartsNeeded(materialsToApply, materialHolder);
			storagePartsNeeded.forEach((part, count) -> allPartsNeeded.merge(part, count, Integer::sum));
		});
	}

	private static Optional<ItemRequirements> getDyeItemRequirements(ItemStack paintbrush, Player player, BlockEntity be) {
		int mainColorToSet = getMainColor(paintbrush);
		int accentColorToSet = getAccentColor(paintbrush);

		Map<TagKey<Item>, Integer> allPartsNeeded = new HashMap<>();
		if (be instanceof StorageBlockEntity storageBe) {
			allPartsNeeded = getStorageDyePartsNeeded(mainColorToSet, accentColorToSet, storageBe.getStorageWrapper());
		} else if (be instanceof ControllerBlockEntity controllerBe) {
			for (BlockPos storagePosition : controllerBe.getStoragePositions()) {
				addStorageDyePartsNeeded(mainColorToSet, accentColorToSet, controllerBe, storagePosition, allPartsNeeded);
			}
		}

		if (allPartsNeeded.isEmpty()) {
			return Optional.empty();
		}
		return getDyeItemRequirements(paintbrush, player, allPartsNeeded);
	}

	public static @NotNull Optional<ItemRequirements> getDyeItemRequirements(ItemStack paintbrush, Player player, Map<TagKey<Item>, Integer> allPartsNeeded) {
		Map<ResourceLocation, Integer> remainingParts = getRemainingParts(paintbrush);
		DecorationHelper.ConsumptionResult result = DecorationHelper.fabric_consumeDyePartsNeeded(allPartsNeeded, InventoryHelper.getItemHandlersFromPlayerIncludingContainers(player), remainingParts, true);

		return compileDyeItemRequirements(allPartsNeeded, remainingParts, result);
	}

	private static void addStorageDyePartsNeeded(int mainColorToSet, int accentColorToSet, ControllerBlockEntity controllerBe, BlockPos storagePosition, Map<TagKey<Item>, Integer> allPartsNeeded) {
		WorldHelper.getBlockEntity(controllerBe.getLevel(), storagePosition, StorageBlockEntity.class).ifPresent(storageBe -> {
			Map<TagKey<Item>, Integer> storagePartsNeeded = getStorageDyePartsNeeded(mainColorToSet, accentColorToSet, storageBe.getStorageWrapper());
			storagePartsNeeded.forEach((part, count) -> allPartsNeeded.merge(part, count, Integer::sum));
		});
	}

	public static Map<TagKey<Item>, Integer> getStorageDyePartsNeeded(int mainColorToSet, int accentColorToSet, ITintable tintable) {
		return DecorationHelper.getDyePartsNeeded(mainColorToSet, accentColorToSet, tintable.getMainColor(), tintable.getAccentColor());
	}



	private static @NotNull Optional<ItemRequirements> compileDyeItemRequirements(Map<TagKey<Item>, Integer> allPartsNeeded, Map<ResourceLocation, Integer> remainingParts, DecorationHelper.ConsumptionResult result) {
		List<ItemStack> itemsPresent = new ArrayList<>();
		List<ItemStack> itemsMissing = new ArrayList<>();

		for (Map.Entry<TagKey<Item>, Integer> entry : allPartsNeeded.entrySet()) {
			TagKey<Item> part = entry.getKey();
			int count = Math.ceilDiv(entry.getValue() - remainingParts.getOrDefault(part.location(), 0), DecorationHelper.BLOCK_TOTAL_PARTS);
			int missing = Math.ceilDiv(result.missingParts().getOrDefault(part.location(), 0), DecorationHelper.BLOCK_TOTAL_PARTS);
			int present = count - missing;

			Item dyeItem;
			if (part == ConventionalItemTags.RED_DYES) {
				dyeItem = Items.RED_DYE;
			} else if (part == ConventionalItemTags.GREEN_DYES) {
				dyeItem = Items.GREEN_DYE;
			} else if (part == ConventionalItemTags.BLUE_DYES) {
				dyeItem = Items.BLUE_DYE;
			} else {
				continue;
			}

			if (missing > 0) {
				itemsMissing.add(new ItemStack(dyeItem, missing));
			}
			if (present > 0) {
				itemsPresent.add(new ItemStack(dyeItem, present));
			}
		}

		return Optional.of(new ItemRequirements(itemsPresent, itemsMissing));
	}

	public static Map<ResourceLocation, Integer> getMaterialHolderPartsNeeded(Map<BarrelMaterial, ResourceLocation> materialsToApply, IMaterialHolder materialHolder) {
		Map<BarrelMaterial, ResourceLocation> originalMaterials = new HashMap<>(materialHolder.getMaterials());
		BarrelBlockItem.uncompactMaterials(originalMaterials);
		return DecorationHelper.getMaterialPartsNeeded(originalMaterials, materialsToApply);
	}

	@Override
	public InteractionResult sophisticatedLibrary_onItemUseFirst(ItemStack paintbrush, UseOnContext context) {
		if (!hasMainColor(paintbrush) && !hasAccentColor(paintbrush) && !hasBarrelMaterials(paintbrush)) {
			return InteractionResult.PASS;
		}

		Level level = context.getLevel();
		BlockEntity be = level.getBlockEntity(context.getClickedPos());
		if (be instanceof StorageBlockEntity storageBe) {
			if (!level.isClientSide()) {
				paintStorage(context.getPlayer(), paintbrush, storageBe, 1);
			}
			return InteractionResult.SUCCESS;
		} else if (be instanceof ControllerBlockEntity controllerBe) {
			if (!level.isClientSide()) {
				paintConnectedStorages(context.getPlayer(), level, paintbrush, controllerBe);
			}
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	private void paintConnectedStorages(@Nullable Player player, Level level, ItemStack paintbrush, ControllerBlockEntity controllerBe) {
		if (player == null) {
			return;
		}

		for (BlockPos pos : controllerBe.getStoragePositions()) {
			WorldHelper.getBlockEntity(level, pos, StorageBlockEntity.class).ifPresent(storageBe -> paintStorage(player, paintbrush, storageBe, 0.6f));
		}
	}

	private static void paintStorage(@Nullable Player player, ItemStack paintbrush, StorageBlockEntity storageBe, float soundVolume) {
		if (player == null) {
			return;
		}
		ITintable tintable = storageBe.getStorageWrapper();
		IMaterialHolder materialHolder = storageBe instanceof IMaterialHolder ? (IMaterialHolder) storageBe : null;

		if (storageBe instanceof ChestBlockEntity chestBe) {
			tintable = chestBe.getMainStorageWrapper();
			storageBe = chestBe.getMainChestBlockEntity();
		}

		BlockState state = storageBe.getBlockState();
		Direction effectOffsetDirection = state.getBlock() instanceof StorageBlockBase storageBlock ? storageBlock.getFacing(state) : Direction.UP;
		if (paint(player, paintbrush, soundVolume, materialHolder, tintable, Vec3.atCenterOf(storageBe.getBlockPos()), effectOffsetDirection, state.getSoundType(/*player.level(), storageBe.getBlockPos(), null*/).getPlaceSound())) {
			WorldHelper.notifyBlockUpdate(storageBe);
		}
	}

	public static boolean paint(@NotNull Player player, ItemStack paintbrush, @Nullable IMaterialHolder materialHolder, ITintable tintable, Vec3 successEffectPos, Direction effectOffsetDirection, SoundEvent placeSound) {
		return paint(player, paintbrush, 1f, materialHolder, tintable, successEffectPos, effectOffsetDirection, placeSound);
	}

	public static boolean paint(@NotNull Player player, ItemStack paintbrush, float soundVolume, @Nullable IMaterialHolder materialHolder, ITintable tintable, Vec3 successEffectPos, Direction effectOffsetDirection, SoundEvent placeSound) {
		if (hasBarrelMaterials(paintbrush)) {
			if (materialHolder == null || !materialHolder.canHoldMaterials()) {
				return false;
			}

			if (applyMaterials(player, paintbrush, materialHolder, tintable)) {
				playSoundAndParticles(player.level(), successEffectPos, soundVolume, placeSound, effectOffsetDirection);
				return true;
			}
		} else {
			if (setColors(player, paintbrush, tintable, materialHolder)) {
				playSoundAndParticles(player.level(), successEffectPos, soundVolume, placeSound, effectOffsetDirection);
				return true;
			}
		}
		return false;
	}

	public static boolean setColors(Player player, ItemStack paintbrush, ITintable tintable, @Nullable IMaterialHolder materialHolder) {
		Map<ResourceLocation, Integer> remainingParts = new HashMap<>(getRemainingParts(paintbrush));
		List<Storage<ItemVariant>> itemHandlers = InventoryHelper.getItemHandlersFromPlayerIncludingContainers(player);
		int mainColorToSet = getMainColor(paintbrush);
		int accentColorToSet = getAccentColor(paintbrush);

		int originalMainColor = tintable.getMainColor();
		int originalAccentColor = tintable.getAccentColor();

		if (originalMainColor == mainColorToSet && originalAccentColor == accentColorToSet) {
			return false;
		}

		if (!DecorationHelper.fabric_consumeDyes(mainColorToSet, accentColorToSet, remainingParts, itemHandlers, originalMainColor, originalAccentColor, true)) {
			return false;
		}

		tintable.setColors(mainColorToSet, accentColorToSet);

		if (materialHolder != null) {
			materialHolder.setMaterials(Collections.emptyMap());
		}

		DecorationHelper.fabric_consumeDyes(mainColorToSet, accentColorToSet, remainingParts, itemHandlers, originalMainColor, originalAccentColor, false);
		setRemainingParts(paintbrush, remainingParts);
		return true;
	}

	private static boolean applyMaterials(Player player, ItemStack paintbrush, IMaterialHolder materialHolder, ITintable tintable) {
		List<Storage<ItemVariant>> itemHandlers = InventoryHelper.getItemHandlersFromPlayerIncludingContainers(player);
		Map<ResourceLocation, Integer> remainingParts = new HashMap<>(getRemainingParts(paintbrush));

		Map<BarrelMaterial, ResourceLocation> originalMaterials = new HashMap<>(materialHolder.getMaterials());
		Map<BarrelMaterial, ResourceLocation> materialsToApply = new HashMap<>(getBarrelMaterials(paintbrush));
		if (originalMaterials.equals(materialsToApply)) {
			return false;
		}

		BarrelBlockItem.uncompactMaterials(originalMaterials);

		if (!DecorationHelper.fabric_consumeMaterials(remainingParts, itemHandlers, originalMaterials, materialsToApply, true)) {
			return false;
		}

		DecorationHelper.fabric_consumeMaterials(remainingParts, itemHandlers, originalMaterials, materialsToApply, false);
		setRemainingParts(paintbrush, remainingParts);

		tintable.setColors(-1, -1);

		BarrelBlockItem.compactMaterials(materialsToApply);
		materialHolder.setMaterials(materialsToApply);
		return true;
	}

	private static void playSoundAndParticles(Level level, Vec3 pos, float soundVolume, SoundEvent placeSound, Direction effectOffsetDirection) {
		level.playSound(null, pos.x(), pos.y(), pos.z(), placeSound, SoundSource.BLOCKS, soundVolume, 1);

		if (level instanceof ServerLevel serverLevel) {
			double x = pos.x() + 0.5D + effectOffsetDirection.getStepX() * 0.6D;
			double y = pos.y() + 0.5D + effectOffsetDirection.getStepY() * 0.6D;
			double z = pos.z() + 0.5D + effectOffsetDirection.getStepZ() * 0.6D;
			double xOffset;
			double yOffset;
			double zOffset;
			if (effectOffsetDirection.getAxis().isVertical()) {
				xOffset = 0.4D;
				yOffset = 0.1D;
				zOffset = 0.4D;
			} else {
				xOffset = 0.1D + effectOffsetDirection.getStepZ() * 0.3D;
				yOffset = 0.4D;
				zOffset = 0.1D + effectOffsetDirection.getStepX() * 0.3D;
			}

			serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 4, xOffset, yOffset, zOffset, 1f);
		}
	}

	private static void resetBarrelMaterials(ItemStack paintbrush) {
		paintbrush.sophisticatedLibrary_remove(ModDataComponents.BARREL_MATERIALS);
	}

	public static void resetMainColor(ItemStack paintbrush) {
		paintbrush.sophisticatedLibrary_remove(ModCoreDataComponents.MAIN_COLOR);
	}

	public static void resetAccentColor(ItemStack paintbrush) {
		paintbrush.sophisticatedLibrary_remove(ModCoreDataComponents.ACCENT_COLOR);
	}

	public static void setMainColor(ItemStack paintbrush, int mainColor) {
		paintbrush.sophisticatedLibrary_set(ModCoreDataComponents.MAIN_COLOR, mainColor);
		resetBarrelMaterials(paintbrush);
	}

	public static void setAccentColor(ItemStack paintbrush, int secondaryColor) {
		paintbrush.sophisticatedLibrary_set(ModCoreDataComponents.ACCENT_COLOR, secondaryColor);
		resetBarrelMaterials(paintbrush);
	}

	public static void setRemainingParts(ItemStack paintbrush, Map<ResourceLocation, Integer> remainingParts) {
		paintbrush.sophisticatedLibrary_set(ModDataComponents.REMAINING_PARTS, remainingParts);
	}

	public static Map<ResourceLocation, Integer> getRemainingParts(ItemStack paintbrush) {
		return paintbrush.sophisticatedLibrary_getOrDefault(ModDataComponents.REMAINING_PARTS, Collections.emptyMap());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
		super.appendHoverText(stack, context, tooltip, tooltipFlag);

		tooltip.addAll(StorageTranslationHelper.INSTANCE.getTranslatedLines(StorageTranslationHelper.INSTANCE.translItemTooltip(stack.getItem()), null, ChatFormatting.DARK_GRAY));

		if (hasBarrelMaterials(stack)) {
			tooltip.add(Component.translatable(StorageTranslationHelper.INSTANCE.translItemTooltip("paintbrush") + ".materials").withStyle(ChatFormatting.GRAY));
			Map<BarrelMaterial, ResourceLocation> barrelMaterials = getBarrelMaterials(stack);
			barrelMaterials.forEach((barrelMaterial, blockName) -> {
				BuiltInRegistries.BLOCK.getOptional(blockName).ifPresent(block -> {
					tooltip.add(
							Component.translatable(StorageTranslationHelper.INSTANCE.translItemTooltip("paintbrush") + ".material",
									Component.translatable(StorageTranslationHelper.INSTANCE.translGui("barrel_part." + barrelMaterial.getSerializedName())),
									block.getName().withStyle(ChatFormatting.DARK_AQUA)
							).withStyle(ChatFormatting.GRAY)
					);
				});
			});
		}

		if (hasMainColor(stack)) {
			int mainColor = getMainColor(stack);
			tooltip.add(Component.translatable(StorageTranslationHelper.INSTANCE.translItemTooltip("paintbrush") + ".main_color",
							Component.literal(ColorHelper.getHexColor(mainColor)).withStyle(Style.EMPTY.withColor(mainColor))
					).withStyle(ChatFormatting.GRAY)
			);
		}

		if (hasAccentColor(stack)) {
			int accentColor = getAccentColor(stack);
			tooltip.add(Component.translatable(StorageTranslationHelper.INSTANCE.translItemTooltip("paintbrush") + ".accent_color",
							Component.literal(ColorHelper.getHexColor(accentColor)).withStyle(Style.EMPTY.withColor(accentColor))
					).withStyle(ChatFormatting.GRAY)
			);
		}
	}

	private static boolean hasMainColor(ItemStack paintbrush) {
		return paintbrush.sophisticatedLibrary_has(ModCoreDataComponents.MAIN_COLOR);
	}

	private static boolean hasAccentColor(ItemStack paintbrush) {
		return paintbrush.sophisticatedLibrary_has(ModCoreDataComponents.ACCENT_COLOR);
	}

	private static boolean hasBarrelMaterials(ItemStack paintbrush) {
		return paintbrush.sophisticatedLibrary_has(ModDataComponents.BARREL_MATERIALS);
	}

	public static int getMainColor(ItemStack paintbrush) {
		return paintbrush.sophisticatedLibrary_getOrDefault(ModCoreDataComponents.MAIN_COLOR, -1);
	}

	public static int getAccentColor(ItemStack paintbrush) {
		return paintbrush.sophisticatedLibrary_getOrDefault(ModCoreDataComponents.ACCENT_COLOR, -1);
	}

	public static Map<BarrelMaterial, ResourceLocation> getBarrelMaterials(ItemStack paintbrush) {
		return paintbrush.sophisticatedLibrary_getOrDefault(ModDataComponents.BARREL_MATERIALS, Collections.emptyMap());
	}

	public record ItemRequirements(List<ItemStack> itemsPresent, List<ItemStack> itemsMissing) {}
}
