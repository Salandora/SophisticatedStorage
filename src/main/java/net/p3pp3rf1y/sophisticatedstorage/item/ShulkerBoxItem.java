package net.p3pp3rf1y.sophisticatedstorage.item;

import com.github.salandora.sophisticatedfabriclib.util.LazyOptional;
import com.google.common.collect.MapMaker;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.p3pp3rf1y.sophisticatedcore.api.IStashStorageItem;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TranslationHelper;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ShulkerBoxItem extends StorageBlockItem implements IStashStorageItem {
	public ShulkerBoxItem(Block block) {
		this(block, new Properties().stacksTo(1));
	}

	public ShulkerBoxItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		if (flagIn == TooltipFlag.ADVANCED) {
			stack.sophisticatedLibrary_getLazyCapability(CapabilityStorageWrapper.getCapabilityInstance())
					.ifPresent(w -> w.getContentsUuid().ifPresent(uuid -> tooltip.add(Component.literal("UUID: " + uuid).withStyle(ChatFormatting.DARK_GRAY))));
		}
		if (!Screen.hasShiftDown()) {
			tooltip.add(Component.translatable(
					TranslationHelper.INSTANCE.translItemTooltip("storage") + ".press_for_contents",
					Component.translatable(TranslationHelper.INSTANCE.translItemTooltip("storage") + ".shift").withStyle(ChatFormatting.AQUA)
			).withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		TooltipComponent ret = null;
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			Minecraft mc = Minecraft.getInstance();
			if (Screen.hasShiftDown() || (mc.player != null && !mc.player.containerMenu.getCarried().isEmpty())) {
				ret = new StorageContentsTooltip(stack);
			}
		}
		return Optional.ofNullable(ret);
	}

	@Override
	public boolean canFitInsideContainerItems() {
		return false;
	}

	@Override
	public void onDestroyed(ItemEntity itemEntity) {
		Level level = itemEntity.level();
		if (level.isClientSide) {
			return;
		}
		ItemStack itemstack = itemEntity.getItem();
		itemstack.sophisticatedLibrary_getLazyCapability(CapabilityStorageWrapper.getCapabilityInstance()).ifPresent(storageWrapper -> {
			InventoryHelper.dropItems(storageWrapper.getInventoryHandler(), level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ());
			InventoryHelper.dropItems(storageWrapper.getUpgradeHandler(), level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ());
		});
	}

	public static ItemApiLookup.ItemApiProvider<LazyOptional<StackStorageWrapper>, Void> initCapabilities() {
		return new ItemApiLookup.ItemApiProvider<>() {
			final Map<ItemStack, StackStorageWrapper> wrapperMap = new MapMaker().weakKeys().weakValues().makeMap();

			@Override
			public LazyOptional<StackStorageWrapper> find(ItemStack stack, Void context) {
				if (stack.getCount() == 1) {
					return LazyOptional.of(() -> wrapperMap.computeIfAbsent(stack, this::initWrapper)).cast();
				}
				return LazyOptional.empty();
			}

			private StackStorageWrapper initWrapper(ItemStack stack) {
				UUID uuid = getContentsUuid(stack).orElse(null);
				StackStorageWrapper storageWrapper = new StackStorageWrapper(stack);
				if (uuid != null) {
					CompoundTag compoundtag = ItemContentsStorage.get().getOrCreateStorageContents(uuid).getCompound(StorageBlockEntity.STORAGE_WRAPPER_TAG);
					storageWrapper.load(compoundtag);
					storageWrapper.setContentsUuid(uuid); //setting here because client side the uuid isn't in contentsnbt before this data is synced from server and it would create a new one otherwise
				}
				return storageWrapper;
			}
		};
	}

	@Override
	public Optional<TooltipComponent> getInventoryTooltip(ItemStack stack) {
		return Optional.of(new StorageContentsTooltip(stack));
	}

	public ItemStack stash(ItemStack storageStack, ItemStack stack, boolean simulate) {
		return storageStack.sophisticatedLibrary_getLazyCapability(CapabilityStorageWrapper.getCapabilityInstance()).map(wrapper -> {
			if (wrapper.getContentsUuid().isEmpty()) {
				wrapper.setContentsUuid(UUID.randomUUID());
			}
			return wrapper.getInventoryForUpgradeProcessing().insertItem(stack, simulate);
		}).orElse(stack);
	}

	@Override
	public StashResult getItemStashable(ItemStack storageStack, ItemStack stack) {
		return storageStack.sophisticatedLibrary_getLazyCapability(CapabilityStorageWrapper.getCapabilityInstance()).map(wrapper -> {
			if (wrapper.getInventoryForUpgradeProcessing().insertItem(stack, true).getCount() == stack.getCount()) {
				return StashResult.NO_SPACE;
			}
			if (wrapper.getInventoryHandler().getSlotTracker().getItems().contains(stack.getItem()) || wrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class).matchesFilter(stack)) {
				return StashResult.MATCH_AND_SPACE;
			}

			return StashResult.SPACE;
		}).orElse(StashResult.NO_SPACE);
	}

	public int getNumberOfInventorySlotsOrDefault(ItemStack shulkerBoxStack) {
		int defaultNumberOfInventorySlots = shulkerBoxStack.sophisticatedLibrary_getLazyCapability(CapabilityStorageWrapper.getCapabilityInstance()).map(StorageWrapper::getDefaultNumberOfInventorySlots).orElse(1);
		return NBTHelper.getInt(shulkerBoxStack, "numberOfInventorySlots").map(inventorySlots -> Math.max(inventorySlots, defaultNumberOfInventorySlots)).orElse(defaultNumberOfInventorySlots);
	}

	public int getNumberOfUpgradeSlotsOrDefault(ItemStack shulkerBoxStack) {
		int defaultNumberOfUpgradeSlots = shulkerBoxStack.sophisticatedLibrary_getLazyCapability(CapabilityStorageWrapper.getCapabilityInstance()).map(StorageWrapper::getDefaultNumberOfUpgradeSlots).orElse(1);
		return NBTHelper.getInt(shulkerBoxStack, "numberOfUpgradeSlots").map(numberOfUpgradeSlots -> Math.max(numberOfUpgradeSlots, defaultNumberOfUpgradeSlots)).orElse(defaultNumberOfUpgradeSlots);
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack storageStack, Slot slot, ClickAction action, Player player) {
		if (hasCreativeScreenContainerOpen(player) || storageStack.getCount() > 1 || !slot.mayPickup(player) || slot.getItem().isEmpty() || action != ClickAction.SECONDARY) {
			return super.overrideStackedOnOther(storageStack, slot, action, player);
		}

		ItemStack stackToStash = slot.getItem();
		ItemStack stashResult = stash(storageStack, stackToStash, true);
		if (stashResult.getCount() < stackToStash.getCount()) {
			int countToTake = stackToStash.getCount() - stashResult.getCount();
			ItemStack takeResult = slot.safeTake(countToTake, countToTake, player);
			stash(storageStack, takeResult, false);
			return true;
		}

		return super.overrideStackedOnOther(storageStack, slot, action, player);
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack storageStack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess carriedAccess) {
		if (hasCreativeScreenContainerOpen(player) || storageStack.getCount() > 1 || !slot.mayPlace(storageStack) || action != ClickAction.SECONDARY) {
			return super.overrideOtherStackedOnMe(storageStack, otherStack, slot, action, player, carriedAccess);
		}

		ItemStack result = stash(storageStack, otherStack, false);
		if (result.getCount() != otherStack.getCount()) {
			carriedAccess.set(result);
			slot.set(storageStack);
			return true;
		}

		return super.overrideOtherStackedOnMe(storageStack, otherStack, slot, action, player, carriedAccess);
	}

	private boolean hasCreativeScreenContainerOpen(Player player) {
		return player.level().isClientSide() && player.containerMenu instanceof CreativeModeInventoryScreen.ItemPickerMenu;
	}
}
