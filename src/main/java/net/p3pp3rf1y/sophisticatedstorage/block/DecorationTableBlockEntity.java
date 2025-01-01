package net.p3pp3rf1y.sophisticatedstorage.block;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedcore.util.WorldHelper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.PaintbrushItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.util.DecorationHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class DecorationTableBlockEntity extends BlockEntity {
	public static final int TOP_INNER_TRIM_SLOT = 0;
	public static final int TOP_TRIM_SLOT = 1;
	public static final int SIDE_TRIM_SLOT = 2;
	public static final int BOTTOM_TRIM_SLOT = 3;
	public static final int TOP_CORE_SLOT = 4;
	public static final int SIDE_CORE_SLOT = 5;
	public static final int BOTTOM_CORE_SLOT = 6;
	public static final int RED_DYE_SLOT = 0;
	public static final int GREEN_DYE_SLOT = 1;
	public static final int BLUE_DYE_SLOT = 2;
	public static final Set<Item> STORAGES_WIHOUT_TOP_INNER_TRIM = Set.of(ModBlocks.BARREL_ITEM.get(), ModBlocks.COPPER_BARREL_ITEM.get(), ModBlocks.IRON_BARREL_ITEM.get(), ModBlocks.GOLD_BARREL_ITEM.get(), ModBlocks.DIAMOND_BARREL_ITEM.get(), ModBlocks.NETHERITE_BARREL_ITEM.get(),
			ModBlocks.LIMITED_BARREL_1_ITEM.get(), ModBlocks.LIMITED_COPPER_BARREL_1_ITEM.get(), ModBlocks.LIMITED_IRON_BARREL_1_ITEM.get(), ModBlocks.LIMITED_GOLD_BARREL_1_ITEM.get(), ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM.get(), ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM.get());

	private final Map<ResourceLocation, Integer> remainingParts = new HashMap<>();

	private final ItemStackHandler decorativeBlocks = new ItemStackHandler(7) {
		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			updateResultAndSetChanged();
		}

		@Override
		public boolean isItemValid(int slot, ItemVariant resource, int count) {
			return resource.getItem() instanceof BlockItem && !(resource.getItem() instanceof StorageBlockItem);
		}
	};

	private final ItemStackHandler dyes = new ItemStackHandler(3) {
		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			updateResultAndSetChanged();
		}

		@Override
		public boolean isItemValid(int slot, ItemVariant resource, int count) {
			return switch (slot) {
				case RED_DYE_SLOT -> resource.toStack().is(ConventionalItemTags.RED_DYES);
				case GREEN_DYE_SLOT -> resource.toStack().is(ConventionalItemTags.GREEN_DYES);
				case BLUE_DYE_SLOT -> resource.toStack().is(ConventionalItemTags.BLUE_DYES);
				default -> false;
			};
		}
	};

	private ItemStack result = ItemStack.EMPTY;

	private final Map<Integer, Boolean> slotMaterialInheritance = new HashMap<>();
	private int accentColor = -1;
	private int mainColor = -1;

	private final Set<ResourceLocation> missingDyes = new HashSet<>();

	public void updateResultAndSetChanged() {
		updateResult();
		setChanged();
	}

	private final ItemStackHandler storageBlock = new ItemStackHandler(1) {
		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			updateResultAndSetChanged();
		}

		@Override
		public boolean isItemValid(int slot, ItemVariant resource, int count) {
			return resource.getItem() instanceof StorageBlockItem || resource.getItem() instanceof PaintbrushItem;
		}
	};

	private void updateResult() {
		missingDyes.clear();
		result = ItemStack.EMPTY;

		ItemStack storage = storageBlock.getStackInSlot(0);
		if (storage.isEmpty()) {
			return;
		}

		if (storage.getItem() instanceof PaintbrushItem) {
			updatePaintbrushResult(storage);
			return;
		}

		DecorationResult decorationResult = decorateStack(storage);
		result = decorationResult.result();
		missingDyes.addAll(decorationResult.missingDyes());
	}

	public boolean hasMaterials() {
		return !InventoryHelper.isEmpty(decorativeBlocks);
	}

	public record DecorationResult(ItemStack result, Set<ResourceLocation> missingDyes) {
		public static final DecorationResult EMPTY = new DecorationResult(ItemStack.EMPTY, Collections.emptySet());
	}
	public DecorationResult decorateStack(ItemStack storage) {
		ItemStack result;
		if ((InventoryHelper.isEmpty(decorativeBlocks) || !(storage.getItem() instanceof BarrelBlockItem) || isTintedStorage(storage)) && (colorsTransparentOrSameAs(storage) || !BarrelBlockItem.getMaterials(storage).isEmpty())) {
			return DecorationResult.EMPTY;
		}
		if (!(storage.getItem() instanceof BarrelBlockItem) || InventoryHelper.isEmpty(decorativeBlocks) || isTintedStorage(storage)) {
			result = storage.copy();
			result.setCount(1);

			if (result.getItem() instanceof BlockItem blockItem && blockItem instanceof ITintableBlockItem tintableBlockItem) {
				if (mainColor != -1) {
					tintableBlockItem.setMainColor(result, mainColor);
				}
				if (accentColor != -1) {
					tintableBlockItem.setAccentColor(result, accentColor);
				}
			}
			Set<ResourceLocation> missingDyes = calculateMissingDyes(storage);
			return new DecorationResult(result, missingDyes);
		}

		if (InventoryHelper.isEmpty(decorativeBlocks)) {
			return DecorationResult.EMPTY;
		}

		Map<BarrelMaterial, ResourceLocation> materials = new EnumMap<>(BarrelMaterial.class);
		materials.putAll(BarrelBlockItem.getMaterials(storage));
		BarrelBlockItem.uncompactMaterials(materials);

		setMaterialsFromDecorativeBlocks(materials, !STORAGES_WIHOUT_TOP_INNER_TRIM.contains(storage.getItem()));
		BarrelBlockItem.compactMaterials(materials);

		if (allMaterialsMatch(materials, BarrelBlockItem.getMaterials(storage))) {
			return DecorationResult.EMPTY;
		}

		result = storage.copy();
		result.setCount(1);

		BarrelBlockItem.removeCoveredTints(result, materials);
		BarrelBlockItem.setMaterials(result, materials);

		return new DecorationResult(result, Collections.emptySet());
	}

	private void updatePaintbrushResult(ItemStack paintbrush) {
		if (!InventoryHelper.isEmpty(decorativeBlocks)) {
			Map<BarrelMaterial, ResourceLocation> materials = new EnumMap<>(BarrelMaterial.class);
			setMaterialsFromDecorativeBlocks(materials, true);
			BarrelBlockItem.compactMaterials(materials);

			if (allMaterialsMatch(materials, BarrelBlockItem.getMaterials(paintbrush))) {
				return;
			}

			result = paintbrush.copy();
			result.setCount(1);
			PaintbrushItem.setBarrelMaterials(result, materials);
		} else {
			if ((mainColor == -1 && accentColor == -1) || (mainColor == PaintbrushItem.getMainColor(paintbrush) && accentColor == PaintbrushItem.getAccentColor(paintbrush))) {
				return;
			}

			result = paintbrush.copy();
			result.setCount(1);
			if (mainColor != -1) {
				PaintbrushItem.setMainColor(result, mainColor);
			}
			if (accentColor != -1) {
				PaintbrushItem.setAccentColor(result, accentColor);
			}
		}
	}

	private boolean isTintedStorage(ItemStack storage) {
		return StorageBlockItem.getMainColorFromStack(storage).isPresent() || StorageBlockItem.getAccentColorFromStack(storage).isPresent();
	}

	private boolean allMaterialsMatch(Map<BarrelMaterial, ResourceLocation> newMaterials, Map<BarrelMaterial, ResourceLocation> currentMaterials) {
		if (newMaterials.size() != currentMaterials.size()) {
			return false;
		}

		for (Map.Entry<BarrelMaterial, ResourceLocation> entry : newMaterials.entrySet()) {
			if (!entry.getValue().equals(currentMaterials.get(entry.getKey()))) {
				return false;
			}
		}

		return true;
	}

	private Set<ResourceLocation> calculateMissingDyes(ItemStack storage) {
		Set<ResourceLocation> missingDyes = new HashSet<>();
		if (!dyes.getStackInSlot(RED_DYE_SLOT).isEmpty() && !dyes.getStackInSlot(GREEN_DYE_SLOT).isEmpty() && !dyes.getStackInSlot(BLUE_DYE_SLOT).isEmpty()) {
			return missingDyes;
		}

		Map<ResourceLocation, Integer> partsNeeded =
				DecorationHelper.getDyePartsNeeded(mainColor, accentColor,
								StorageBlockItem.getMainColorFromStack(storage).orElse(-1), StorageBlockItem.getAccentColorFromStack(storage).orElse(-1))
						.entrySet().stream().map(entry -> Map.entry(entry.getKey().location(), entry.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		for (Map.Entry<ResourceLocation, Integer> entry : partsNeeded.entrySet()) {
			if (entry.getKey().equals(ConventionalItemTags.RED_DYES.location()) && dyes.getStackInSlot(RED_DYE_SLOT).isEmpty()) {
				missingDyes.add(entry.getKey());
			} else if (entry.getKey().equals(ConventionalItemTags.GREEN_DYES.location()) && dyes.getStackInSlot(GREEN_DYE_SLOT).isEmpty()) {
				missingDyes.add(entry.getKey());
			} else if (entry.getKey().equals(ConventionalItemTags.BLUE_DYES.location()) && dyes.getStackInSlot(BLUE_DYE_SLOT).isEmpty()) {
				missingDyes.add(entry.getKey());
			}
		}
		return missingDyes;
	}

	public Set<ResourceLocation> getMissingDyes() {
		return missingDyes;
	}

	private boolean colorsTransparentOrSameAs(ItemStack storage) {
		return (mainColor == -1 || mainColor == StorageBlockItem.getMainColorFromStack(storage).orElse(-1)) && (accentColor == -1 || accentColor == StorageBlockItem.getAccentColorFromStack(storage).orElse(-1));
	}

	private void setMaterialsFromDecorativeBlocks(Map<BarrelMaterial, ResourceLocation> materials, boolean supportsInnerTrim) {
		ResourceLocation topInnerTrimMaterialLocation = setMaterialFromBlock(TOP_INNER_TRIM_SLOT, null, materials, BarrelMaterial.TOP_INNER_TRIM, supportsInnerTrim);
		ResourceLocation topTrimMaterialLocation = setMaterialFromBlock(TOP_TRIM_SLOT, topInnerTrimMaterialLocation, materials, BarrelMaterial.TOP_TRIM, true);
		ResourceLocation sideTrimMaterialLocation = setMaterialFromBlock(SIDE_TRIM_SLOT, topTrimMaterialLocation, materials, BarrelMaterial.SIDE_TRIM, true);
		setMaterialFromBlock(BOTTOM_TRIM_SLOT, sideTrimMaterialLocation, materials, BarrelMaterial.BOTTOM_TRIM, true);
		ResourceLocation topMaterialLocation = setMaterialFromBlock(TOP_CORE_SLOT, topTrimMaterialLocation, materials, BarrelMaterial.TOP, true);
		ResourceLocation sideMaterialLocation = setMaterialFromBlock(SIDE_CORE_SLOT, topMaterialLocation, materials, BarrelMaterial.SIDE, true);
		setMaterialFromBlock(BOTTOM_CORE_SLOT, sideMaterialLocation, materials, BarrelMaterial.BOTTOM, true);
	}

	@Nullable
	private ResourceLocation setMaterialFromBlock(int slotIndex, @Nullable ResourceLocation defaultMaterialLocation, Map<BarrelMaterial, ResourceLocation> materials, BarrelMaterial material, boolean addToMaterials) {
		ItemStack decorativeBlock = decorativeBlocks.getStackInSlot(slotIndex);
		ResourceLocation materialLocation = DecorationHelper.getMaterialLocation(decorativeBlock).orElse(isSlotMaterialInherited(slotIndex) ? defaultMaterialLocation : null);
		if (materialLocation != null) {
			if (addToMaterials) {
				materials.put(material, materialLocation);
			}
			return materialLocation;
		}
		return null;
	}

	public DecorationTableBlockEntity(BlockPos pos, BlockState blockState) {
		super(ModBlocks.DECORATION_TABLE_BLOCK_ENTITY_TYPE.get(), pos, blockState);
	}

	public ItemStackHandler getDecorativeBlocks() {
		return decorativeBlocks;
	}

	public ItemStackHandler getDyes() {
		return dyes;
	}

	public ItemStackHandler getStorageBlock() {
		return storageBlock;
	}

	public ItemStack getResult() {
		return result;
	}

	public ItemStack extractResult(int count) {
		ItemStack result = getResult();
		if (result.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack extracted = result.copy();
		extracted.setCount(count);
		if (count >= result.getCount()) {
			this.result = ItemStack.EMPTY;
		} else {
			result.shrink(count);
		}
		setChanged();
		return extracted;
	}

	public boolean isSlotMaterialInherited(int slot) {
		return slotMaterialInheritance.getOrDefault(slot, true);
	}

	public ItemStack getInheritedItem(int childSlot) {
		while (isSlotMaterialInherited(childSlot)) {
			int parentSlot = getSlotInheritedFrom(childSlot);
			if (parentSlot == -1) {
				return ItemStack.EMPTY;
			} else if (!decorativeBlocks.getStackInSlot(parentSlot).isEmpty()) {
				return decorativeBlocks.getStackInSlot(parentSlot);
			}
			childSlot = parentSlot;
		}
		return ItemStack.EMPTY;
	}

	public int getSlotInheritedFrom(int slot) {
		return switch (slot) {
			case TOP_TRIM_SLOT -> TOP_INNER_TRIM_SLOT;
			case SIDE_TRIM_SLOT -> TOP_TRIM_SLOT;
			case BOTTOM_TRIM_SLOT -> SIDE_TRIM_SLOT;
			case TOP_CORE_SLOT -> TOP_TRIM_SLOT;
			case SIDE_CORE_SLOT -> TOP_CORE_SLOT;
			case BOTTOM_CORE_SLOT -> SIDE_CORE_SLOT;
			default -> -1;
		};
	}

	public void setSlotMaterialInheritance(int slot, boolean value) {
		if (value) {
			slotMaterialInheritance.remove(slot);
		} else {
			slotMaterialInheritance.put(slot, false);
		}
		updateResultAndSetChanged();
	}

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		saveData(tag, registries);
		return tag;
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		decorativeBlocks.deserializeNBT(registries, tag.getCompound("decorativeBlocks"));
		dyes.deserializeNBT(registries, tag.getCompound("dyes"));
		storageBlock.deserializeNBT(registries, tag.getCompound("storageBlock"));
		result = ItemStack.parse(registries, tag.getCompound("result")).orElse(ItemStack.EMPTY);
		slotMaterialInheritance.clear();
		ListTag inheritance = tag.getList("slotMaterialInheritance", Tag.TAG_COMPOUND);
		for (int i = 0; i < inheritance.size(); i++) {
			CompoundTag slotTag = inheritance.getCompound(i);
			slotMaterialInheritance.put(slotTag.getInt("slot"), slotTag.getBoolean("value"));
		}
		remainingParts.clear();
		ListTag remainingPartsTag = tag.getList("remainingParts", Tag.TAG_COMPOUND);
		for (int i = 0; i < remainingPartsTag.size(); i++) {
			CompoundTag partTag = remainingPartsTag.getCompound(i);
			ResourceLocation key = ResourceLocation.tryParse(partTag.getString("key"));
			if (key == null) {
				continue;
			}
			remainingParts.put(key, partTag.getInt("value"));
		}

		mainColor = tag.getInt("mainColor");
		accentColor = tag.getInt("accentColor");
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		saveData(tag, registries);
	}

	private void saveData(CompoundTag tag, HolderLookup.Provider registries) {
		tag.put("decorativeBlocks", decorativeBlocks.serializeNBT(registries));
		tag.put("dyes", dyes.serializeNBT(registries));
		tag.put("storageBlock", storageBlock.serializeNBT(registries));
		if (!result.isEmpty()) {
			tag.put("result", result.save(registries));
		}
		ListTag inheritance = new ListTag();
		slotMaterialInheritance.forEach((slot, value) -> {
			CompoundTag slotTag = new CompoundTag();
			slotTag.putInt("slot", slot);
			slotTag.putBoolean("value", value);
			inheritance.add(slotTag);
		});
		tag.put("slotMaterialInheritance", inheritance);
		ListTag remainingPartsTag = new ListTag();
		remainingParts.forEach((key, value) -> {
			CompoundTag partTag = new CompoundTag();
			partTag.putString("key", key.toString());
			partTag.putInt("value", value);
			remainingPartsTag.add(partTag);
		});
		tag.put("remainingParts", remainingPartsTag);

		tag.putInt("mainColor", mainColor);
		tag.putInt("accentColor", accentColor);
	}

	public void consumeIngredientsOnCraft() {
		if (getStorageBlock().getStackInSlot(0).getItem() instanceof PaintbrushItem) {
			return; //paintbrush consumes ingredients by itself on application to storage block
		}
		ItemStack storageStack = storageBlock.getStackInSlot(0);
		if (InventoryHelper.isEmpty(decorativeBlocks)) {
			DecorationHelper.consumeDyes(mainColor, accentColor, this.remainingParts, List.of(dyes), StorageBlockItem.getMainColorFromStack(storageStack).orElse(-1), StorageBlockItem.getAccentColorFromStack(storageStack).orElse(-1), false);
		} else {
			Map<BarrelMaterial, ResourceLocation> originalMaterials = BarrelBlockItem.getUncompactedMaterials(storageStack);
			DecorationHelper.consumeMaterials(this.remainingParts, List.of(decorativeBlocks), originalMaterials, getMaterialsToApply(storageStack), false);
		}

		setChanged();
		WorldHelper.notifyBlockUpdate(this);
	}

	public Map<ResourceLocation, Integer> getPartsNeeded() {
		Map<ResourceLocation, Integer> partsNeeded = new HashMap<>();
		ItemStack storageStack = storageBlock.getStackInSlot(0);
		if (InventoryHelper.isEmpty(decorativeBlocks) || !(storageStack.getItem() instanceof BarrelBlockItem)) {
			DecorationHelper.getDyePartsNeeded(mainColor, accentColor, StorageBlockItem.getMainColorFromStack(storageStack).orElse(-1), StorageBlockItem.getAccentColorFromStack(storageStack).orElse(-1))
					.forEach((tag, parts) -> partsNeeded.put(tag.location(), parts));
		} else {
			partsNeeded.putAll(DecorationHelper.getMaterialPartsNeeded(BarrelBlockItem.getUncompactedMaterials(storageStack), getMaterialsToApply(storageStack)));
		}

		return partsNeeded;
	}

	private Map<BarrelMaterial, ResourceLocation> getMaterialsToApply(ItemStack storageStack) {
		Map<BarrelMaterial, ResourceLocation> materialsToApply = new EnumMap<>(BarrelMaterial.class);
		setMaterialsFromDecorativeBlocks(materialsToApply, !STORAGES_WIHOUT_TOP_INNER_TRIM.contains(storageStack.getItem()));
		return materialsToApply;
	}

	public int getMainColor() {
		return mainColor;
	}

	public void setMainColor(int mainColor) {
		this.mainColor = mainColor;
		updateResultAndSetChanged();
	}

	public int getAccentColor() {
		return accentColor;
	}

	public void setAccentColor(int accentColor) {
		this.accentColor = accentColor;
		updateResultAndSetChanged();
	}

	public Map<ResourceLocation, Integer> getPartsStored() {
		return remainingParts;
	}

	public void dropContents() {
		InventoryHelper.dropItems(decorativeBlocks, level, worldPosition);
		InventoryHelper.dropItems(dyes, level, worldPosition);
		InventoryHelper.dropItems(storageBlock, level, worldPosition);
	}
}
