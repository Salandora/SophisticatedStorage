package net.p3pp3rf1y.sophisticatedstorage.block;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedcore.api.Tags;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedcore.util.WorldHelper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;

import javax.annotation.Nullable;
import java.util.*;

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

	public static final int BLOCK_TOTAL_PARTS = 24;
	private static final int MAIN_COLOR_PARTS = 18;
	private static final int ACCENT_COLOR_PARTS = 6;
	private static final Map<Integer, Integer> DECORATIVE_SLOT_PARTS_NEEDED = Map.of(
			TOP_INNER_TRIM_SLOT, 1,
			TOP_TRIM_SLOT, 1,
			SIDE_TRIM_SLOT, 4,
			BOTTOM_TRIM_SLOT, 1,
			TOP_CORE_SLOT, 3,
			SIDE_CORE_SLOT, 12,
			BOTTOM_CORE_SLOT, 3
	);

	private static final Set<Item> STORAGES_WIHOUT_TOP_INNER_TRIM = Set.of(ModBlocks.BARREL_ITEM, ModBlocks.COPPER_BARREL_ITEM, ModBlocks.IRON_BARREL_ITEM, ModBlocks.GOLD_BARREL_ITEM, ModBlocks.DIAMOND_BARREL_ITEM, ModBlocks.NETHERITE_BARREL_ITEM,
			ModBlocks.LIMITED_BARREL_1_ITEM, ModBlocks.LIMITED_COPPER_BARREL_1_ITEM, ModBlocks.LIMITED_IRON_BARREL_1_ITEM, ModBlocks.LIMITED_GOLD_BARREL_1_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM);

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
			return resource.getItem() instanceof StorageBlockItem;
		}
	};

	private ItemStack result = ItemStack.EMPTY;

	private final Map<Integer, Boolean> slotMaterialInheritance = new HashMap<>();
	private int accentColor = -1;
	private int mainColor = -1;

	private final Set<ResourceLocation> missingDyes = new HashSet<>();

	private void updateResult() {
		missingDyes.clear();

		ItemStack storage = storageBlock.getStackInSlot(0);
		if (storage.isEmpty() || (
				(InventoryHelper.isEmpty(decorativeBlocks)
						|| !(storage.getItem() instanceof BarrelBlockItem)
						|| isTintedStorage(storage)
				) && colorsTransparentOrSameAs(storage))) {
			result = ItemStack.EMPTY;
			return;
		}
		if (!(storage.getItem() instanceof BarrelBlockItem) || InventoryHelper.isEmpty(decorativeBlocks) || isTintedStorage(storage)) {
			result = storage.copy();
			result.setCount(1);

			if (result.getItem() instanceof BlockItem blockItem && blockItem instanceof ITintableBlockItem tintableBlockItem) {
				if (mainColor != -1) {
					tintableBlockItem.setMainColor(result, mainColor & 0x00FFFFFF);
				}
				if (accentColor != -1) {
					tintableBlockItem.setAccentColor(result, accentColor & 0x00FFFFFF);
				}
			}
			calculateMissingDyes(storage);
			return;
		}

		if (InventoryHelper.isEmpty(decorativeBlocks)) {
			result = ItemStack.EMPTY;
			return;
		}

		Map<BarrelMaterial, ResourceLocation> materials = new EnumMap<>(BarrelMaterial.class);
		materials.putAll(BarrelBlockItem.getMaterials(storage));
		BarrelBlockItem.uncompactMaterials(materials);

		setMaterialsFromDecorativeBlocks(materials, !STORAGES_WIHOUT_TOP_INNER_TRIM.contains(storage.getItem()));
		BarrelBlockItem.compactMaterials(materials);

		if (allMaterialsMatch(materials, BarrelBlockItem.getMaterials(storage))) {
			result = ItemStack.EMPTY;
			return;
		}

		result = storage.copy();
		result.setCount(1);

		BarrelBlockItem.removeCoveredTints(result, materials);
		BarrelBlockItem.setMaterials(result, materials);
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

	private void calculateMissingDyes(ItemStack storage) {
		if (!dyes.getStackInSlot(RED_DYE_SLOT).isEmpty() && !dyes.getStackInSlot(GREEN_DYE_SLOT).isEmpty() && !dyes.getStackInSlot(BLUE_DYE_SLOT).isEmpty()) {
			return;
		}

		Map<ResourceLocation, Integer> partsNeeded = new HashMap<>();
		addDyePartsNeeded(storage, partsNeeded);

		for (Map.Entry<ResourceLocation, Integer> entry : partsNeeded.entrySet()) {
			if (entry.getKey().equals(ConventionalItemTags.RED_DYES.location()) && dyes.getStackInSlot(RED_DYE_SLOT).isEmpty()) {
				missingDyes.add(entry.getKey());
			} else if (entry.getKey().equals(ConventionalItemTags.GREEN_DYES.location()) && dyes.getStackInSlot(GREEN_DYE_SLOT).isEmpty()) {
				missingDyes.add(entry.getKey());
			} else if (entry.getKey().equals(ConventionalItemTags.BLUE_DYES.location()) && dyes.getStackInSlot(BLUE_DYE_SLOT).isEmpty()) {
				missingDyes.add(entry.getKey());
			}
		}
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
		ResourceLocation materialLocation = getMaterialLocation(decorativeBlock).orElse(isSlotMaterialInherited(slotIndex) ? defaultMaterialLocation : null);
		if (materialLocation != null) {
			if (addToMaterials) {
				materials.put(material, materialLocation);
			}
			return materialLocation;
		}
		return null;
	}

	private Optional<ResourceLocation> getMaterialLocation(ItemStack stack) {
		if (stack.getItem() instanceof BlockItem blockItem) {
			return Optional.of(BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()));
		}
		return Optional.empty();
	}

	public DecorationTableBlockEntity(BlockPos pos, BlockState blockState) {
		super(ModBlocks.DECORATION_TABLE_BLOCK_ENTITY_TYPE, pos, blockState);
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
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		saveData(tag);
		return tag;
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		decorativeBlocks.deserializeNBT(tag.getCompound("decorativeBlocks"));
		dyes.deserializeNBT(tag.getCompound("dyes"));
		storageBlock.deserializeNBT(tag.getCompound("storageBlock"));
		result = tag.contains("result") ? ItemStack.of(tag.getCompound("result")) : ItemStack.EMPTY;
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
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		saveData(tag);
	}

	private void saveData(CompoundTag tag) {
		tag.put("decorativeBlocks", decorativeBlocks.serializeNBT());
		tag.put("dyes", dyes.serializeNBT());
		tag.put("storageBlock", storageBlock.serializeNBT());
		if (!result.isEmpty()) {
			tag.put("result", result.save(new CompoundTag()));
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
		if (InventoryHelper.isEmpty(decorativeBlocks)) {
			consumeDyes();
		} else {
			consumeMaterials();
		}

		setChanged();
		WorldHelper.notifyBlockUpdate(this);
	}

	private void consumeDyes() {
		ItemStack storageStack = storageBlock.getStackInSlot(0);
		Map<ResourceLocation, Integer> partsNeeded = new HashMap<>();
		Map<ResourceLocation, Integer> firstSlotWithMaterial = new HashMap<>();
		firstSlotWithMaterial.put(ConventionalItemTags.RED_DYES.location(), RED_DYE_SLOT);
		firstSlotWithMaterial.put(ConventionalItemTags.GREEN_DYES.location(), GREEN_DYE_SLOT);
		firstSlotWithMaterial.put(ConventionalItemTags.BLUE_DYES.location(), BLUE_DYE_SLOT);

		addDyePartsNeeded(storageStack, partsNeeded);

		if (partsNeeded.isEmpty()) {
			return;
		}

		consumePartsNeeded(partsNeeded, firstSlotWithMaterial, dyes);
	}

	private void addDyePartsNeeded(ItemStack storageStack, Map<ResourceLocation, Integer> partsNeeded) {
		if (mainColor != -1 && mainColor != StorageBlockItem.getMainColorFromStack(storageStack).orElse(-1)) {
			int[] rgbPartsNeeded = calculateRGBPartsNeeded(mainColor, MAIN_COLOR_PARTS);
			addPartsNeededIfAny(rgbPartsNeeded, partsNeeded);
		}
		if (accentColor != -1 && accentColor != StorageBlockItem.getAccentColorFromStack(storageStack).orElse(-1)) {
			int[] rgbPartsNeeded = calculateRGBPartsNeeded(accentColor, ACCENT_COLOR_PARTS);
			addPartsNeededIfAny(rgbPartsNeeded, partsNeeded);
		}
	}

	private static void addPartsNeededIfAny(int[] rgbPartsNeeded, Map<ResourceLocation, Integer> partsNeeded) {
		addPartsNeededIfAny(rgbPartsNeeded[0], partsNeeded, ConventionalItemTags.RED_DYES.location());
		addPartsNeededIfAny(rgbPartsNeeded[1], partsNeeded, ConventionalItemTags.GREEN_DYES.location());
		addPartsNeededIfAny(rgbPartsNeeded[2], partsNeeded, ConventionalItemTags.BLUE_DYES.location());
	}

	private static void addPartsNeededIfAny(int parts, Map<ResourceLocation, Integer> partsNeeded, ResourceLocation dyeName) {
		if (parts != 0) {
			partsNeeded.compute(dyeName, (location, partsTotal) -> partsTotal == null ? parts : partsTotal + parts);
		}
	}

	private int[] calculateRGBPartsNeeded(int color, int totalParts) {
		float[] ratios = new float[3];
		ratios[0] = FastColor.ARGB32.red(color) / 255f;
		ratios[1] = FastColor.ARGB32.green(color) / 255f;
		ratios[2] = FastColor.ARGB32.blue(color) / 255f;

		float totalRaios = ratios[0] + ratios[1] + ratios[2];
		ratios[0] /= totalRaios;
		ratios[1] /= totalRaios;
		ratios[2] /= totalRaios;

		int n = ratios.length;
		int[] result = new int[n];
		double[] remainders = new double[n];

		double[] scaled = new double[n];
		for (int i = 0; i < n; i++) {
			scaled[i] = ratios[i] * totalParts;
			result[i] = (int) scaled[i];
			remainders[i] = scaled[i] - result[i];
		}

		int remaining = totalParts - Arrays.stream(result).sum();

		Integer[] indices = new Integer[n];
		for (int i = 0; i < n; i++) indices[i] = i;

		Arrays.sort(indices, Comparator.comparingDouble(i -> -remainders[i]));

		for (int i = 0; i < remaining; i++) {
			result[indices[i % n]]++;
		}

		return result;
	}

	private void consumeMaterials() {
		Map<ResourceLocation, Integer> partsNeeded = new HashMap<>();
		Map<ResourceLocation, Integer> firstSlotWithMaterial = new HashMap<>();
		addMaterialPartsNeeded(partsNeeded, firstSlotWithMaterial);
		consumePartsNeeded(partsNeeded, firstSlotWithMaterial, decorativeBlocks);
	}

	private void addMaterialPartsNeeded(Map<ResourceLocation, Integer> partsNeeded, Map<ResourceLocation, Integer> firstSlotWithMaterial) {
		ResourceLocation topInnerTrimMaterialLocation = addMaterialCostForSlotAndGetMaterial(TOP_INNER_TRIM_SLOT, null, partsNeeded, firstSlotWithMaterial);
		ResourceLocation topTrimMaterialLocation = addMaterialCostForSlotAndGetMaterial(TOP_TRIM_SLOT, topInnerTrimMaterialLocation, partsNeeded, firstSlotWithMaterial);
		ResourceLocation sideTrimMaterialLocation = addMaterialCostForSlotAndGetMaterial(SIDE_TRIM_SLOT, topTrimMaterialLocation, partsNeeded, firstSlotWithMaterial);
		addMaterialCostForSlotAndGetMaterial(BOTTOM_TRIM_SLOT, sideTrimMaterialLocation, partsNeeded, firstSlotWithMaterial);
		ResourceLocation topMaterialLocation = addMaterialCostForSlotAndGetMaterial(TOP_CORE_SLOT, topTrimMaterialLocation, partsNeeded, firstSlotWithMaterial);
		ResourceLocation sideMaterialLocation = addMaterialCostForSlotAndGetMaterial(SIDE_CORE_SLOT, topMaterialLocation, partsNeeded, firstSlotWithMaterial);
		addMaterialCostForSlotAndGetMaterial(BOTTOM_CORE_SLOT, sideMaterialLocation, partsNeeded, firstSlotWithMaterial);
	}

	public Map<ResourceLocation, Integer> getPartsNeeded() {
		Map<ResourceLocation, Integer> partsNeeded = new HashMap<>();
		ItemStack storageStack = storageBlock.getStackInSlot(0);
		if (InventoryHelper.isEmpty(decorativeBlocks) || !(storageStack.getItem() instanceof BarrelBlockItem)) {
			addDyePartsNeeded(storageStack, partsNeeded);
		} else {
			addMaterialPartsNeeded(partsNeeded, new HashMap<>());
		}

		return partsNeeded;
	}

	private void consumePartsNeeded(Map<ResourceLocation, Integer> partsNeeded, Map<ResourceLocation, Integer> firstSlotWithMaterial, ItemStackHandler resources) {
		partsNeeded.forEach((material, parts) -> {
			int remainingParts = this.remainingParts.getOrDefault(material, 0);
			if (remainingParts >= parts) {
				if (remainingParts == parts) {
					this.remainingParts.remove(material);
				} else {
					this.remainingParts.put(material, remainingParts - parts);
				}
			} else {
				if (firstSlotWithMaterial.get(material) == null) {
					return;
				}
				int slotWithMaterial = firstSlotWithMaterial.get(material);
				ItemStack stack = resources.getStackInSlot(slotWithMaterial);
				stack.shrink(1);
				resources.setStackInSlot(slotWithMaterial, stack);
				this.remainingParts.put(material, remainingParts + BLOCK_TOTAL_PARTS - parts);
			}
		});
	}

	@Nullable
	private ResourceLocation addMaterialCostForSlotAndGetMaterial(int slotIndex, @Nullable ResourceLocation defaultMaterialLocation, Map<ResourceLocation, Integer> partsNeeded, Map<ResourceLocation, Integer> firstSlotWithMaterial) {
		boolean hasNoCost = slotIndex == TOP_TRIM_SLOT && defaultMaterialLocation != null;

		ItemStack decorativeBlock = decorativeBlocks.getStackInSlot(slotIndex);
		ResourceLocation materialLocation = getMaterialLocation(decorativeBlock).orElse(isSlotMaterialInherited(slotIndex) ? defaultMaterialLocation : null);
		if (hasNoCost) {
			return materialLocation;
		}

		if (materialLocation != null) {
			int parts = DECORATIVE_SLOT_PARTS_NEEDED.get(slotIndex);
			partsNeeded.compute(materialLocation, (key, value) -> value == null ? parts : value + parts);
			firstSlotWithMaterial.putIfAbsent(materialLocation, slotIndex);
		}
		return materialLocation;
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
