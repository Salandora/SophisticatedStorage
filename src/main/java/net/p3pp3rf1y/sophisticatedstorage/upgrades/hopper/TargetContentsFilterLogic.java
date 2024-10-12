package net.p3pp3rf1y.sophisticatedstorage.upgrades.hopper;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ContentsFilterLogic;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TargetContentsFilterLogic extends ContentsFilterLogic {
	private Set<ItemStackKey> inventoryFilterStacks = new HashSet<>();
	private final LoadingCache<Storage<ItemVariant>, Set<ItemStackKey>> inventoryCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build(new CacheLoader<>() {
		@Override
		public Set<ItemStackKey> load(Storage<ItemVariant> inventory) {
			return InventoryHelper.getUniqueStacks(inventory);
		}
	});

	public TargetContentsFilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount, Supplier<InventoryHandler> getInventoryHandler, MemorySettingsCategory memorySettings, String parentTagKey) {
		super(upgrade, saveHandler, filterSlotCount, getInventoryHandler, memorySettings, parentTagKey);
	}

	public void setInventory(Storage<ItemVariant> inventory) {
		inventoryFilterStacks = inventoryCache.getUnchecked(inventory);
	}

	@Override
	public boolean matchesFilter(ItemStack stack) {
		if (!shouldFilterByStorage()) {
			return super.matchesFilter(stack);
		}

		for (ItemStackKey filterStack : inventoryFilterStacks) {
			if (stackMatchesFilter(stack, filterStack.getStack())) {
				return true;
			}
		}
		return false;
	}
}
