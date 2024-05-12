package net.p3pp3rf1y.sophisticatedstorage.upgrades.compression;

import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Pair;

import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.p3pp3rf1y.sophisticatedcore.inventory.IInventoryPartHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryPartitioner;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.util.RecipeHelper;
import net.p3pp3rf1y.sophisticatedstorage.Config;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;

import static net.p3pp3rf1y.sophisticatedcore.util.MathHelper.intMaxCappedAddition;
import static net.p3pp3rf1y.sophisticatedcore.util.MathHelper.intMaxCappedMultiply;

public class CompressionInventoryPart implements IInventoryPartHandler {
	public static final String NAME = "compression";
	public static final Pair<ResourceLocation, ResourceLocation> EMPTY_COMPRESSION_SLOT = new Pair<>(InventoryMenu.BLOCK_ATLAS, SophisticatedStorage.getRL("item/empty_compression_slot"));
	private final InventoryHandler parent;
	private final InventoryPartitioner.SlotRange slotRange;
	private final Supplier<MemorySettingsCategory> getMemorySettings;
	@SuppressWarnings("FieldCanBeLocal") //need field instead of local variable because it's wrapped in WeakReference in RecipeHelper
	private final Runnable recipeChangeListener = () -> calculateStacks(false);

	private Map<Integer, SlotDefinition> slotDefinitions = new HashMap<>();
	private final Map<Integer, ItemStack> calculatedStacks = new HashMap<>();

	public CompressionInventoryPart(InventoryHandler parent, InventoryPartitioner.SlotRange slotRange, Supplier<MemorySettingsCategory> getMemorySettings) {
		this.parent = parent;
		this.slotRange = slotRange;
		this.getMemorySettings = getMemorySettings;

		RecipeHelper.addRecipeChangeListener(recipeChangeListener);
	}

	@Override
	public void onInit() {
		calculateStacks(true);
	}

	private void calculateStacks(boolean initial) {
		clearCollections();
		Map<Integer, ItemStack> existingStacks = getExistingStacks();

		if (existingStacks.isEmpty()) {
			return;
		}

		int lastNonEmptySlot = getLastNonEmptySlot(existingStacks);
		setSlotDefinitions(getSlotDefinitions(existingStacks.get(lastNonEmptySlot).getItem(), lastNonEmptySlot, existingStacks), initial);

		compactInternalSlots();
		updateCalculatedStacks();
	}

	private void setSlotDefinitions(Map<Integer, SlotDefinition> definitions, boolean initial) {
		slotDefinitions = definitions;
		if (initial) {
			parent.initFilterItems();
		} else {
			parent.onFilterItemsChanged();
			slotDefinitions.forEach((slot, definition) -> parent.triggerOnChangeListeners(slot));
		}
	}

	private Integer getLastNonEmptySlot(Map<Integer, ItemStack> existingStacks) {
		for (int slot = slotRange.firstSlot() + slotRange.numberOfSlots() - 1; slot >= slotRange.firstSlot(); slot--) {
			if (existingStacks.containsKey(slot)) {
				return slot;
			}
		}

		return -1;
	}

	private Map<Integer, SlotDefinition> getSlotDefinitions(Item firstItem, int lastSlot, Map<Integer, ItemStack> existingStacks) {
		Map<Integer, SlotDefinition> ret = new HashMap<>();
		addPreviousItems(ret, lastSlot, firstItem);

		Item prevItem = firstItem;
		for (int slot = lastSlot; slot >= slotRange.firstSlot(); slot--) {
			if (existingStacks.containsKey(slot) && existingStacks.get(slot).getItem() != prevItem) {
				ret.clear(); //clearing any compressible definition added before as the compression should no longer compress if there are incompatible items present
				break;
			} else {
				Optional<RecipeHelper.CompactingShape> compressionShape = getCompressionShape(prevItem);
				if (compressionShape.isPresent()) {
					RecipeHelper.CompactingShape shape = compressionShape.get();
					ret.put(slot, new SlotDefinition(prevItem, shape.getNumberOfIngredients(), true));
					prevItem = RecipeHelper.getCompactingResult(prevItem, shape).getResult().getItem();
				} else {
					ret.put(slot, new SlotDefinition(prevItem, 1, true));
					break;
				}
			}
		}

		updateSlotLimits(ret);
		updateInaccessibleAndCompressible(ret, existingStacks);

		return ret;
	}

	private void updateSlotLimits(Map<Integer, SlotDefinition> definitions) {
		int lastMultiplier = 1;
		int totalLimit = 0;
		for (int slot = slotRange.firstSlot(); slot < slotRange.firstSlot() + slotRange.numberOfSlots(); slot++) {
			if (definitions.containsKey(slot) && definitions.get(slot).isAccessible()) {
				if (slot != slotRange.firstSlot()) {
					lastMultiplier = intMaxCappedMultiply(lastMultiplier, definitions.get(slot).prevSlotMultiplier);
				}
				totalLimit = intMaxCappedAddition(totalLimit, intMaxCappedMultiply(lastMultiplier, parent.getBaseStackLimit(ItemVariant.of(definitions.get(slot).item))));

				definitions.get(slot).setSlotLimit(totalLimit);
			}
		}
	}

	private void updateCalculatedStacks() {
		int totalCalculated = 0;
		boolean prevFull = false;
		for (int slot = slotRange.firstSlot(); slot < slotRange.firstSlot() + slotRange.numberOfSlots(); slot++) {
			SlotDefinition slotDefinition = slotDefinitions.get(slot);
			if (!slotDefinition.isAccessible()) {
				continue;
			}
			if (!slotDefinition.isCompressible()) {
				calculatedStacks.put(slot, parent.getSlotStack(slot).copy());
				continue;
			}
			int internalCount = parent.getSlotStack(slot).getCount();
			totalCalculated = Integer.MAX_VALUE / slotDefinition.prevSlotMultiplier() < totalCalculated ? Integer.MAX_VALUE : totalCalculated * slotDefinition.prevSlotMultiplier();
			totalCalculated = Integer.MAX_VALUE - internalCount < totalCalculated ? Integer.MAX_VALUE : totalCalculated + internalCount;

			ItemStack calculatedStack = new ItemStack(slotDefinition.item(), totalCalculated);

			int internalLimit = parent.getBaseStackLimit(ItemVariant.of(slotDefinition.item()));
			int maxStackSize = calculatedStack.getMaxStackSize();
			if (Integer.MAX_VALUE - totalCalculated < maxStackSize) {
				calculatedStack.setCount(Integer.MAX_VALUE - (prevFull ? Math.min(maxStackSize, internalLimit - internalCount) : maxStackSize));
			}
			calculatedStacks.put(slot, calculatedStack);

			prevFull = internalLimit <= internalCount;
		}
	}

	private void compactInternalSlots() {
		Map<Integer, Integer> toUpdate = new HashMap<>();

		for (int slot = slotRange.firstSlot() + 1; slot < slotRange.firstSlot() + slotRange.numberOfSlots(); slot++) {
			ItemStack slotStack = parent.getSlotStack(slot);
			int multiplier = getPrevSlotMultiplier(slot);
			if (slotStack.isEmpty() || multiplier < 2) {
				continue;
			}
			int prevSlot = slot - 1;
			ItemStack prevStack = parent.getSlotStack(prevSlot);
			int stackLimit = parent.getBaseStackLimit(ItemVariant.of(prevStack));
			int prevStackCount = toUpdate.containsKey(prevSlot) ? toUpdate.get(prevSlot) : prevStack.getCount();
			int availableSpace = stackLimit - prevStackCount;
			int countToInsert = Math.min(availableSpace, slotStack.getCount() / multiplier);
			if (countToInsert > 0) {
				toUpdate.put(prevSlot, prevStackCount + countToInsert);
				toUpdate.put(slot, slotStack.getCount() - countToInsert * multiplier);
			}
		}

		updateInternalStacksWithCounts(toUpdate);
	}

	private void updateInaccessibleAndCompressible(Map<Integer, SlotDefinition> definitions, Map<Integer, ItemStack> existingStacks) {
		for (int slot = slotRange.firstSlot(); slot < slotRange.firstSlot() + slotRange.numberOfSlots(); slot++) {
			definitions.computeIfAbsent(slot, s -> {
				if (existingStacks.containsKey(s)) {
					return new SlotDefinition(existingStacks.get(s).getItem(), 1, true);
				}
				return SlotDefinition.inaccesible();
			});
			if (!definitions.get(slot).isAccessible()) {
				continue;
			}
			boolean uncompressibledFromNext = definitions.containsKey(slot - 1) && definitions.get(slot - 1).isAccessible() && definitions.get(slot).prevSlotMultiplier() > 1;
			boolean compressibleFromPrevious = definitions.containsKey(slot + 1) && definitions.get(slot + 1).isAccessible() && definitions.get(slot + 1).prevSlotMultiplier() > 1;
			definitions.get(slot).setCompressible(uncompressibledFromNext || compressibleFromPrevious);
		}
	}

	private void clearCollections() {
		slotDefinitions.clear();
		calculatedStacks.clear();
		parent.onFilterItemsChanged();
	}

	private Optional<RecipeHelper.CompactingShape> getCompressionShape(Item item) {
		Set<RecipeHelper.CompactingShape> compactingShapes = RecipeHelper.getItemCompactingShapes(item);

		if (compactingShapes.contains(RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE)) {
			return Optional.of(RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE);
		} else if (compactingShapes.contains(RecipeHelper.CompactingShape.TWO_BY_TWO_UNCRAFTABLE)) {
			return Optional.of(RecipeHelper.CompactingShape.TWO_BY_TWO_UNCRAFTABLE);
		} else if (compactingShapes.contains(RecipeHelper.CompactingShape.THREE_BY_THREE)) {
			Item compressedItem = RecipeHelper.getCompactingResult(item, RecipeHelper.CompactingShape.THREE_BY_THREE).getResult().getItem();
			return getDecompressionResultFromConfig(compressedItem).isPresent() ? Optional.of(RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE) : Optional.empty();
		} else if (compactingShapes.contains(RecipeHelper.CompactingShape.TWO_BY_TWO)) {
			Item compressedItem = RecipeHelper.getCompactingResult(item, RecipeHelper.CompactingShape.TWO_BY_TWO).getResult().getItem();
			return getDecompressionResultFromConfig(compressedItem).isPresent() ? Optional.of(RecipeHelper.CompactingShape.TWO_BY_TWO_UNCRAFTABLE) : Optional.empty();
		}
		return Optional.empty();
	}


	private void addPreviousItems(Map<Integer, SlotDefinition> slotDefinitions, int firstFilledSlot, Item firstFilledItem) {
		Item currentItem = firstFilledItem;
		for (int slot = firstFilledSlot + 1; slot < slotRange.firstSlot() + slotRange.numberOfSlots(); slot++) {
			RecipeHelper.UncompactingResult uncompactingResult = RecipeHelper.getUncompactingResult(currentItem);
			if (uncompactingResult.getCompactUsingShape() == RecipeHelper.CompactingShape.NONE) {
				Optional<RecipeHelper.UncompactingResult> decompressionResult = getDecompressionResultFromConfig(currentItem);
				if (decompressionResult.isEmpty()) {
					break;
				}
				uncompactingResult = decompressionResult.get();
			}
			slotDefinitions.put(slot, new SlotDefinition(uncompactingResult.getResult(), uncompactingResult.getCompactUsingShape() == RecipeHelper.CompactingShape.TWO_BY_TWO_UNCRAFTABLE ? 4 : 9, true));
			currentItem = uncompactingResult.getResult();
		}
	}

	public Optional<RecipeHelper.UncompactingResult> getDecompressionResultFromConfig(Item currentItem) {
		return Config.SERVER.compressionUpgrade.getDecompressionResult(currentItem);
	}

	private Map<Integer, ItemStack> getExistingStacks() {
		Map<Integer, ItemStack> existingStacks = new LinkedHashMap<>();
		for (int slot = slotRange.firstSlot(); slot < slotRange.firstSlot() + slotRange.numberOfSlots(); slot++) {
			ItemStack slotStack = parent.getSlotStack(slot);
			if (!slotStack.isEmpty()) {
				existingStacks.put(slot, slotStack);
			}
		}

		if (existingStacks.isEmpty()) {
			MemorySettingsCategory memorySettings = getMemorySettings.get();
			for (int slot = slotRange.firstSlot(); slot < slotRange.firstSlot() + slotRange.numberOfSlots(); slot++) {
				int finalSlot = slot;
				memorySettings.getSlotFilterStack(slot, true).ifPresent(stack -> existingStacks.put(finalSlot, stack));
			}
		}

		return existingStacks;
	}

	@Override
	public int getSlotLimit(int slot) {
		return slotDefinitions.containsKey(slot) ? slotDefinitions.get(slot).slotLimit() : parent.getBaseSlotLimit();
	}

	@Override
	public int getStackLimit(int slot, ItemVariant resource) {
		if (!slotDefinitions.containsKey(slot)) {
			return parent.getBaseStackLimit(resource);
		}

		SlotDefinition slotDefinition = slotDefinitions.get(slot);
		return getStackLimit(slotDefinition);
	}

	private int getStackLimit(SlotDefinition slotDefinition) {
		if (!slotDefinition.isAccessible()) {
			return 0;
		}

		return slotDefinition.slotLimit();
	}

	@Override
	public long extractItem(int slot, ItemVariant resource, long amount, TransactionContext ctx) {
		return extractItem(slot, resource, amount, ctx, ItemStack::getMaxStackSize);
	}

	private long extractItem(int slot, ItemVariant resource, long amount, TransactionContext ctx, ToIntFunction<ItemStack> getLimit) {
		if (!slotDefinitions.containsKey(slot) || !slotDefinitions.get(slot).isAccessible()) {
			return 0;
		}
		int toExtract = Math.min(calculatedStacks.get(slot).getCount(), (int) amount);

		if (toExtract > 0) {
			SlotDefinition slotDefinition = slotDefinitions.get(slot);
			ItemStack slotStack = parent.getSlotStack(slot);
			toExtract = Math.min(toExtract, getLimit.applyAsInt(slotStack));
			//ItemStack result = slotDefinition.isCompressible() ? new ItemStack(slotDefinition.item(), toExtract) : ItemHandlerHelper.copyStackWithSize(slotStack, toExtract);

			int finalToExtract = toExtract;
			TransactionCallback.onSuccess(ctx, () -> {
				if (slotDefinition.isCompressible()) {
					extractFromCalculated(slot, finalToExtract);
					extractFromInternal(slot, finalToExtract);
				} else {
					slotStack.shrink(finalToExtract);
					parent.setSlotStack(slot, slotStack);
					calculatedStacks.put(slot, slotStack.copy());
				}
				removeDefinitionsIfEmpty(slot);
			});

			return toExtract;
		}

		return 0;
	}

	private void removeDefinitionsIfEmpty(int slotTriggeringChange) {
		for (int slot = slotRange.firstSlot(); slot < slotRange.firstSlot() + slotRange.numberOfSlots(); slot++) {
			if (!parent.getSlotStack(slot).isEmpty() || getMemorySettings.get().getSlotFilterStack(slot, false).isPresent()) {
				return;
			}
		}

		clearCollections();
		parent.triggerOnChangeListeners(slotTriggeringChange);
	}

	private void extractFromInternal(int slotToStartFrom, int amountToExtract) {
		Map<Integer, Integer> toUpdate = new HashMap<>();
		int decompressedAmountToInsert = 0;
		int totalMultiplier = 1;
		while (amountToExtract > 0) {
			ItemStack slotStack = parent.getSlotStack(slotToStartFrom);
			if (totalMultiplier == 1) {
				int toRemove = Math.min(amountToExtract, slotStack.getCount());
				toUpdate.put(slotToStartFrom, slotStack.getCount() - toRemove);
				amountToExtract -= toRemove;
			} else {
				int ceiledAmount = (int) Math.ceil((double) amountToExtract / totalMultiplier);
				int toRemove = Math.min(ceiledAmount, slotStack.getCount());
				toUpdate.put(slotToStartFrom, slotStack.getCount() - toRemove);

				int totalToRemove = toRemove * totalMultiplier;
				if (totalToRemove > amountToExtract) {
					decompressedAmountToInsert = totalToRemove - amountToExtract;
					break;
				}
				amountToExtract -= totalToRemove;
			}

			totalMultiplier *= getPrevSlotMultiplier(slotToStartFrom);
			slotToStartFrom--;
		}

		while (decompressedAmountToInsert > 0) {
			slotToStartFrom++;
			totalMultiplier /= getPrevSlotMultiplier(slotToStartFrom);

			int toInsert = decompressedAmountToInsert / totalMultiplier;
			if (toInsert > 0) {
				toUpdate.put(slotToStartFrom, toUpdate.getOrDefault(slotToStartFrom, 0) + toInsert);
				decompressedAmountToInsert -= toInsert * totalMultiplier;
			}
		}

		updateInternalStacksWithCounts(toUpdate);
	}

	private int getPrevSlotMultiplier(int slot) {
		return slotDefinitions.get(slot).prevSlotMultiplier;
	}

	private void updateInternalStacksWithCounts(Map<Integer, Integer> toUpdate) {
		toUpdate.forEach((s, count) -> {
			ItemStack slotStack = parent.getSlotStack(s);
			if (slotStack.getCount() != count) {
				if (count == 0) {
					parent.setSlotStack(s, ItemStack.EMPTY);
				} else if (slotStack.isEmpty()) {
					parent.setSlotStack(s, new ItemStack(slotDefinitions.get(s).item(), count));
				} else {
					slotStack.setCount(count);
					parent.setSlotStack(s, slotStack);
				}
			}
		});
	}

	private void extractFromCalculated(int slot, int extractCount) {
		extractFromCalculatedThisAndPreviousStacks(extractCount, slot);
		extractFromCalculatedThisAndStacksAfter(extractCount, slot + 1);
	}

	private void extractFromCalculatedThisAndPreviousStacks(int extractCount, int slotCalculated) {
		int countBeforeChange = -1;
		int multiplier = 1;
		while (extractCount != 0 && calculatedStacks.containsKey(slotCalculated)) {
			ItemStack calculatedStack = calculatedStacks.get(slotCalculated);

			if (countBeforeChange > 0 && countBeforeChange / multiplier > calculatedStack.getCount()) {
				extractCount = calculatedStack.getCount() - (countBeforeChange - extractCount * multiplier) / multiplier;
				if (extractCount <= 0) {
					break;
				}
			}

			countBeforeChange = calculatedStack.getCount();
			int toSet = getCountChangeLeavingSpaceBeforeMaxInt(countBeforeChange - extractCount, slotCalculated, calculatedStack);
			calculatedStack.setCount(toSet);

			calculatedStacks.put(slotCalculated, calculatedStack);

			multiplier = getPrevSlotMultiplier(slotCalculated);
			extractCount = countBeforeChange / multiplier - calculatedStack.getCount() / multiplier;

			slotCalculated--;
		}
	}

	private int getCountChangeLeavingSpaceBeforeMaxInt(int countCalculated, int slotCalculated, ItemStack calculatedStack) {
		int toSet = countCalculated;
		int prevSlot = slotCalculated - 1;
		SlotDefinition prevSlotDefinition = slotDefinitions.get(prevSlot);
		boolean hasPrevious = prevSlotDefinition != null && prevSlotDefinition.isAccessible();
		if (countCalculated > 0 && Integer.MAX_VALUE - countCalculated < calculatedStack.getMaxStackSize() && hasPrevious) {
			boolean prevSlotFull = getSlotLimit(prevSlot) == calculatedStacks.get(prevSlot).getCount();
			int buffer = prevSlotFull ? getStackLimit(slotCalculated, ItemVariant.of(calculatedStack)) - countCalculated : calculatedStack.getMaxStackSize();
			toSet = Integer.MAX_VALUE - buffer;
		}
		return toSet;
	}

	private void extractFromCalculatedThisAndStacksAfter(int extractCount, int slot) {
		while (slot < slotRange.firstSlot() + slotRange.numberOfSlots() && slotDefinitions.get(slot).isAccessible()) {
			ItemStack calculatedStack = calculatedStacks.get(slot);
			int multiplier = getPrevSlotMultiplier(slot);
			extractCount *= multiplier;
			int countSet = calculatedStack.getCount() - extractCount;
			countSet = getCountChangeLeavingSpaceBeforeMaxInt(countSet, slot, calculatedStack);
			calculatedStack.setCount(countSet);
			calculatedStacks.put(slot, calculatedStack);
			slot++;
		}
	}

	@Override
	public long insertItem(int slot, ItemVariant resource, long maxAmount, @Nullable TransactionContext ctx, Function4<Integer, ItemVariant, Long, TransactionContext, Long> insertSuper) {
		return insertItem(slot, resource, maxAmount, ctx);
	}

	private long insertItem(int slot, ItemVariant resource, long maxAmount, @Nullable TransactionContext ctx) {
		if (canNotBeInserted(slot, resource)) {
			return 0;
		}

		Map<Integer, SlotDefinition> definitions = slotDefinitions;
		if (definitions.isEmpty()) {
			definitions = getSlotDefinitions(resource.getItem(), slot, Map.of());
		}

		int limit = getStackLimit(definitions.get(slot));

		int currentCalculatedCount = calculatedStacks.containsKey(slot) ? calculatedStacks.get(slot).getCount() : 0;
		long inserted = Math.min(Math.max(parent.getBaseStackLimit(resource) - parent.getSlotStack(slot).getCount(), limit - currentCalculatedCount), maxAmount);

		if (inserted == 0) {
			return 0;
		}

		Map<Integer, SlotDefinition> finalDefinitions = definitions;
		try (Transaction nested = Transaction.openNested(ctx)) {
			TransactionCallback.onSuccess(nested, () -> {
				if (!slotDefinitions.containsKey(slot)) {
					setSlotDefinitions(finalDefinitions, false);
					compactInternalSlots();
					updateCalculatedStacks();
				}

				if (slotDefinitions.get(slot).isCompressible()) {
					insertIntoInternalAndCalculated(slot, inserted);
				} else if (inserted > 0) {
					calculatedStacks.compute(slot, (s, st) -> {
						if (st == null || st.isEmpty()) {
							return resource.toStack((int) inserted);
						}
						st.grow((int) inserted);
						return st;
					});
					ItemStack slotStack = parent.getSlotStack(slot);
					if (slotStack.isEmpty()) {
						parent.setSlotStack(slot, resource.toStack((int) inserted));
					} else {
						slotStack.grow((int) inserted);
						parent.setSlotStack(slot, slotStack);
					}
				}
			});
			nested.commit();
		}

		return inserted;
	}

	private boolean canNotBeInserted(int slot, ItemVariant resource) {
		if (resource.isBlank()) {
			return true;
		}

		if (!slotDefinitions.containsKey(slot)) {
			return false;
		}

		SlotDefinition slotDefinition = slotDefinitions.get(slot);
		return !slotDefinition.isAccessible() || slotDefinition.item() != resource.getItem();
	}

	private void insertIntoInternalAndCalculated(int slotToStartFrom, long amountToInsert) {
		Map<Integer, Integer> toUpdate = new LinkedHashMap<>();
		Map<Integer, Integer> calculatedAdditions = new LinkedHashMap<>();
		int totalMultiplier = 1;
		int slot = slotToStartFrom;

		long amountToSet = amountToInsert + parent.getSlotStack(slot).getCount();

		while (amountToSet / ((long) totalMultiplier * getPrevSlotMultiplier(slot)) > 0 && slotDefinitions.containsKey(slot - 1) && slotDefinitions.get(slot - 1).isAccessible()) {
			totalMultiplier *= getPrevSlotMultiplier(slot);
			slot--;
			amountToSet += (long) parent.getSlotStack(slot).getCount() * totalMultiplier;
		}

		long calculatedAddition = 0;
		while (slot <= slotToStartFrom) {
			calculatedAddition *= getPrevSlotMultiplier(slot);
			ItemStack slotStack = parent.getSlotStack(slot);
			int toSet = (int) Math.min(amountToSet / totalMultiplier, parent.getBaseStackLimit(ItemVariant.of(slotStack)));
			calculatedAddition += (toSet - slotStack.getCount());
			calculatedAdditions.put(slot, (int) Math.min(calculatedAddition, Integer.MAX_VALUE));
			if (toSet > 0) {
				toUpdate.put(slot, toSet);
				amountToSet -= (long) toSet * totalMultiplier;
			} else {
				toUpdate.put(slot, 0);
			}

			if (amountToSet != 0) {
				totalMultiplier /= getPrevSlotMultiplier(slot + 1);
			}
			slot++;
		}

		//finish calculation of calculated addition to the follow up slots even though they are not getting their internal stack changed
		while (slot < slotRange.firstSlot() + slotRange.numberOfSlots()) {
			if (!slotDefinitions.containsKey(slot)) {
				break;
			}

			calculatedAddition *= getPrevSlotMultiplier(slot);
			calculatedAdditions.put(slot, (int) Math.min(calculatedAddition, Integer.MAX_VALUE));

			slot++;
		}

		updateInternalStacksWithCounts(toUpdate);

		calculatedAdditions.forEach(this::addToCalculatedStack);
	}

	private void addToCalculatedStack(int slot, int countToAdd) {
		if (!calculatedStacks.containsKey(slot) || calculatedStacks.get(slot).isEmpty()) {
			SlotDefinition slotDefinition = slotDefinitions.get(slot);
			calculatedStacks.put(slot, new ItemStack(slotDefinition.item(), countToAdd));
			return;
		}
		ItemStack currentCalculated = calculatedStacks.get(slot);

		int totalCalculated = Integer.MAX_VALUE - countToAdd < currentCalculated.getCount() ? Integer.MAX_VALUE : currentCalculated.getCount() + countToAdd;

		int previousSlot = slot - 1;
		if (totalCalculated != Integer.MAX_VALUE || !slotDefinitions.containsKey(previousSlot)) {
			currentCalculated.setCount(totalCalculated);
			return;
		}

		ItemStack previousInternalStack = parent.getSlotStack(previousSlot);
		boolean isPreviousFull = previousInternalStack.getCount() >= parent.getBaseStackLimit(ItemVariant.of(previousInternalStack));

		int internalLimit = parent.getBaseStackLimit(ItemVariant.of(currentCalculated));
		int internalCount = parent.getSlotStack(slot).getCount();

		int maxStackSize = previousInternalStack.getMaxStackSize();
		int spaceBeforeMaxInt = isPreviousFull ? Math.min(maxStackSize, internalLimit - internalCount) : maxStackSize;
		currentCalculated.setCount(Integer.MAX_VALUE - spaceBeforeMaxInt);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack, BiConsumer<Integer, ItemStack> setStackInSlotSuper) {
		int currentCount = calculatedStacks.containsKey(slot) ? calculatedStacks.get(slot).getCount() : 0;
		try (Transaction ctx = Transaction.openOuter()) {
			if (currentCount < stack.getCount()) {
				insertItem(slot, ItemVariant.of(stack), stack.getCount() - currentCount, ctx);
			} else if (currentCount > stack.getCount()) {
				extractItem(slot, ItemVariant.of(stack), currentCount - stack.getCount(), ctx, s -> Integer.MAX_VALUE);
			}
			ctx.commit();
		}
	}

	@Override
	public boolean isItemValid(int slot, ItemVariant resource, int count) {
		if (!slotDefinitions.containsKey(slot)) {
			return true;
		}

		SlotDefinition slotDefinition = slotDefinitions.get(slot);
		return slotDefinition.isAccessible() && slotDefinition.item() == resource.getItem();
	}

	@Override
	public ItemVariant getVariantInSlot(int slot, IntFunction<ItemVariant> getVariantInSlotSuper) {
		return slotDefinitions.containsKey(slot) && slotDefinitions.get(slot).isAccessible() && calculatedStacks.containsKey(slot) ? ItemVariant.of(calculatedStacks.get(slot)) : ItemVariant.blank();
	}

	@Override
	public ItemStack getStackInSlot(int slot, IntFunction<ItemStack> getStackInSlotSuper) {
		return slotDefinitions.containsKey(slot) && slotDefinitions.get(slot).isAccessible() && calculatedStacks.containsKey(slot) ? calculatedStacks.get(slot) : ItemStack.EMPTY;
	}

	@Override
	public boolean isSlotAccessible(int slot) {
		return !slotDefinitions.containsKey(slot) || slotDefinitions.get(slot).isAccessible();
	}

	@Override
	public int getSlots() {
		return slotRange.numberOfSlots();
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Nullable
	@Override
	public Pair<ResourceLocation, ResourceLocation> getNoItemIcon(int slot) {
		return EMPTY_COMPRESSION_SLOT;
	}

	@Override
	public Item getFilterItem(int slot) {
		return slotDefinitions.containsKey(slot) ? slotDefinitions.get(slot).item() : Items.AIR;
	}

	@Override
	public void onSlotLimitChange() {
		updateSlotLimits(slotDefinitions);
	}

	@Override
	public Set<Integer> getNoSortSlots() {
		return IntStream.rangeClosed(slotRange.firstSlot(), slotRange.firstSlot() + slotRange.numberOfSlots() - 1).boxed().collect(Collectors.toSet());
	}

	@Override
	public void onSlotFilterChanged(int slot) {
		calculateStacks(false);
	}

	@Override
	public boolean isFilterItem(Item item) {
		for (SlotDefinition slotDefinition : slotDefinitions.values()) {
			if (slotDefinition.item() == item) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Map<Item, Set<Integer>> getFilterItems() {
		Map<Item, Set<Integer>> filterItems = new HashMap<>();
		for (Map.Entry<Integer, SlotDefinition> entry : slotDefinitions.entrySet()) {
			SlotDefinition slotDefinition = entry.getValue();
			if (slotDefinition.isAccessible()) {
				filterItems.computeIfAbsent(slotDefinition.item(), k -> new HashSet<>()).add(entry.getKey());
			}
		}
		return filterItems;
	}

	private static final class SlotDefinition {
		private final Item item;
		private final int prevSlotMultiplier;
		private int slotLimit;
		private final boolean isAccessible;

		private boolean isCompressible = false;

		private SlotDefinition(Item item, int prevSlotMultiplier, int slotLimit, boolean isAccessible) {
			this.item = item;
			this.prevSlotMultiplier = prevSlotMultiplier;
			this.slotLimit = slotLimit;
			this.isAccessible = isAccessible;
		}

		public static SlotDefinition inaccesible() {
			return new SlotDefinition(Items.AIR, 0, 0, false);
		}

		public SlotDefinition(Item item, int prevSlotMultiplier, boolean isAccessible) {
			this(item, prevSlotMultiplier, -1, isAccessible);
		}

		public void setSlotLimit(int slotLimit) {
			this.slotLimit = slotLimit;
		}

		public void setCompressible(boolean compressible) {
			isCompressible = compressible;
		}

		public Item item() {return item;}

		public int prevSlotMultiplier() {return prevSlotMultiplier;}

		public int slotLimit() {return slotLimit;}

		public boolean isAccessible() {return isAccessible;}

		public boolean isCompressible() {
			return isCompressible;
		}
	}
}
