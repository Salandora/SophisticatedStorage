package net.p3pp3rf1y.sophisticatedstorage.item;

import io.github.fabricators_of_create.porting_lib.util.EnvExecutor;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TranslationHelper;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedstorage.Config;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.ShulkerBoxBlock;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.common.CapabilityStorageWrapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;

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
		if (flagIn == TooltipFlag.Default.ADVANCED) {
			CapabilityStorageWrapper.get(stack).flatMap(IStorageWrapper::getContentsUuid)
					.ifPresent(uuid -> tooltip.add(Component.literal("UUID: " + uuid).withStyle(ChatFormatting.DARK_GRAY)));
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
		AtomicReference<TooltipComponent> ret = new AtomicReference<>(null);
		EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> {
			Minecraft mc = Minecraft.getInstance();
			if (Screen.hasShiftDown() || (mc.player != null && !mc.player.containerMenu.getCarried().isEmpty())) {
				ret.set(new StorageContentsTooltip(stack));
			}
		});
		return Optional.ofNullable(ret.get());
	}

	@Override
	public boolean canFitInsideContainerItems() {
		return false;
	}

	@Override
	public void onDestroyed(ItemEntity itemEntity) {
		Level level = itemEntity.getLevel();
		if (level.isClientSide) {
			return;
		}
		ItemStack itemstack = itemEntity.getItem();
		CapabilityStorageWrapper.get(itemstack).ifPresent(storageWrapper -> {
			InventoryHelper.dropItems(storageWrapper.getInventoryHandler(), level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ());
			InventoryHelper.dropItems(storageWrapper.getUpgradeHandler(), level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ());
		});
	}

	@Override
	public Optional<TooltipComponent> getInventoryTooltip(ItemStack stack) {
		return Optional.of(new StorageContentsTooltip(stack));
	}

	@Override
	public ItemStack stash(ItemStack storageStack, ItemStack stack) {
		return CapabilityStorageWrapper.get(storageStack).map(wrapper -> {
			if (wrapper.getContentsUuid().isEmpty()) {
				wrapper.setContentsUuid(UUID.randomUUID());
			}
			try (Transaction ctx = Transaction.openOuter()) {
				long inserted = wrapper.getInventoryForUpgradeProcessing().insert(ItemVariant.of(stack), stack.getCount(), ctx);
				ctx.commit();
				return stack.copyWithCount(stack.getCount() - (int) inserted);
			}
		}).orElse(stack);
	}

	@Override
	public StashResult getItemStashable(ItemStack storageStack, ItemStack stack) {
		return CapabilityStorageWrapper.get(storageStack).map(wrapper -> {
			if (StorageUtil.simulateInsert(wrapper.getInventoryForUpgradeProcessing(), ItemVariant.of(stack), stack.getCount(), null) == 0) {
				return StashResult.NO_SPACE;
			}
			if (wrapper.getInventoryHandler().getSlotTracker().getItems().contains(stack.getItem()) || wrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class).matchesFilter(stack)) {
				return StashResult.MATCH_AND_SPACE;
			}

			return StashResult.SPACE;
		}).orElse(StashResult.NO_SPACE);
	}

	public void setNumberOfInventorySlots(ItemStack shulkerBoxStack, int numberOfInventorySlots) {
		NBTHelper.putInt(shulkerBoxStack.getOrCreateTag(), "numberOfInventorySlots", numberOfInventorySlots);
	}

	public int getNumberOfInventorySlots(ItemStack shulkerBoxStack) {
		int defaultNumberOfInventorySlots = CapabilityStorageWrapper.get(shulkerBoxStack).map(StorageWrapper::getDefaultNumberOfInventorySlots).orElse(1);
		return NBTHelper.getInt(shulkerBoxStack, "numberOfInventorySlots").map(inventorySlots -> Math.max(inventorySlots, defaultNumberOfInventorySlots)).orElse(defaultNumberOfInventorySlots);
	}

	public int getNumberOfUpgradeSlots(ItemStack shulkerBoxStack) {
		int defaultNumberOfUpgradeSlots = CapabilityStorageWrapper.get(shulkerBoxStack).map(StorageWrapper::getDefaultNumberOfUpgradeSlots).orElse(1);
		return NBTHelper.getInt(shulkerBoxStack, "numberOfUpgradeSlots").map(numberOfUpgradeSlots -> Math.max(numberOfUpgradeSlots, defaultNumberOfUpgradeSlots)).orElse(defaultNumberOfUpgradeSlots);
	}

	public void setNumberOfUpgradeSlots(ItemStack shulkerBoxStack, int numberOfUpgradeSlots) {
		NBTHelper.putInt(shulkerBoxStack.getOrCreateTag(), "numberOfUpgradeSlots", numberOfUpgradeSlots);
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack storageStack, Slot slot, ClickAction action, Player player) {
		if (storageStack.getCount() > 1 || !slot.mayPickup(player) || slot.getItem().isEmpty() || action != ClickAction.SECONDARY) {
			return super.overrideStackedOnOther(storageStack, slot, action, player);
		}

		ItemStack stackToStash = slot.getItem();
		ItemStack stashResult = stash(storageStack, stackToStash);
		if (stashResult.getCount() != stackToStash.getCount()) {
			slot.set(stashResult);
			slot.onTake(player, stashResult);
			return true;
		}

		return super.overrideStackedOnOther(storageStack, slot, action, player);
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack storageStack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess carriedAccess) {
		if (storageStack.getCount() > 1 || !slot.mayPlace(storageStack) || action != ClickAction.SECONDARY) {
			return super.overrideOtherStackedOnMe(storageStack, otherStack, slot, action, player, carriedAccess);
		}

		ItemStack result = stash(storageStack, otherStack);
		if (result.getCount() != otherStack.getCount()) {
			carriedAccess.set(result);
			slot.set(storageStack);
			return true;
		}

		return super.overrideOtherStackedOnMe(storageStack, otherStack, slot, action, player, carriedAccess);
	}

	public static StorageWrapper initWrapper(ItemStack stack) {
		UUID uuid = NBTHelper.getUniqueId(stack, "uuid").orElse(null);
		StorageWrapper storageWrapper = new StackStorageWrapper(stack) {
			@Override
			protected boolean isAllowedInStorage(ItemStack stack) {
				Block block = Block.byItem(stack.getItem());
				return !(block instanceof ShulkerBoxBlock) && !(block instanceof net.minecraft.world.level.block.ShulkerBoxBlock) && !Config.SERVER.shulkerBoxDisallowedItems.isItemDisallowed(stack.getItem());
			}
		};
		if (uuid != null) {
			CompoundTag compoundtag = ItemContentsStorage.get().getOrCreateStorageContents(uuid).getCompound(StorageBlockEntity.STORAGE_WRAPPER_TAG);
			storageWrapper.load(compoundtag);
			storageWrapper.setContentsUuid(uuid); //setting here because client side the uuid isn't in contentsnbt before this data is synced from server and it would create a new one otherwise
		}
		return storageWrapper;
	}
}
