package net.p3pp3rf1y.sophisticatedstorage.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
		if (flagIn == TooltipFlag.ADVANCED) {
			StackStorageWrapper.fromData(stack).getContentsUuid().ifPresent(uuid -> tooltip.add(Component.literal("UUID: " + uuid).withStyle(ChatFormatting.DARK_GRAY)));
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
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			return Optional.ofNullable(StorageItemClient.getTooltipImage(stack));
		}
		return Optional.empty();
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
		ItemStack stack = itemEntity.getItem();
		StackStorageWrapper storageWrapper = StackStorageWrapper.fromData(stack);
		InventoryHelper.dropItems(storageWrapper.getInventoryHandler(), level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ());
		InventoryHelper.dropItems(storageWrapper.getUpgradeHandler(), level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ());
	}

	@Override
	public Optional<TooltipComponent> getInventoryTooltip(ItemStack stack) {
		return Optional.of(new StorageContentsTooltip(stack));
	}

	public ItemStack stash(ItemStack storageStack, ItemStack stack, @Nullable Transaction ctx) {
		StackStorageWrapper wrapper = StackStorageWrapper.fromData(storageStack);
		if (wrapper.getContentsUuid().isEmpty()) {
			wrapper.setContentsUuid(UUID.randomUUID());
		}
		try (Transaction inner = Transaction.openNested(ctx)) {
			long inserted = wrapper.getInventoryForUpgradeProcessing().insert(ItemVariant.of(stack), stack.getCount(), inner);
			inner.commit();
			return stack.copyWithCount(stack.getCount() - (int) inserted);
		}
	}

	@Override
	public StashResult getItemStashable(ItemStack storageStack, ItemStack stack) {
		StackStorageWrapper wrapper = StackStorageWrapper.fromData(storageStack);

		if (StorageUtil.simulateInsert(wrapper.getInventoryForUpgradeProcessing(), ItemVariant.of(stack), stack.getCount(), null) == 0) {
			return StashResult.NO_SPACE;
		}
		if (wrapper.getInventoryHandler().getSlotTracker().getItems().contains(stack.getItem()) || wrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class).matchesFilter(stack)) {
			return StashResult.MATCH_AND_SPACE;
		}

		return StashResult.SPACE;
	}

	public void setNumberOfInventorySlots(ItemStack shulkerBoxStack, int numberOfInventorySlots) {
		NBTHelper.putInt(shulkerBoxStack.getOrCreateTag(), "numberOfInventorySlots", numberOfInventorySlots);
	}

	public int getNumberOfInventorySlots(ItemStack shulkerBoxStack) {
		int defaultNumberOfInventorySlots = StackStorageWrapper.fromData(shulkerBoxStack).getDefaultNumberOfInventorySlots();
		return NBTHelper.getInt(shulkerBoxStack, "numberOfInventorySlots").map(inventorySlots -> Math.max(inventorySlots, defaultNumberOfInventorySlots)).orElse(defaultNumberOfInventorySlots);
	}

	public int getNumberOfUpgradeSlots(ItemStack shulkerBoxStack) {
		int defaultNumberOfUpgradeSlots = StackStorageWrapper.fromData(shulkerBoxStack).getDefaultNumberOfUpgradeSlots();
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
		ItemStack stashResult;
		try (Transaction simulate = Transaction.openOuter()) {
			stashResult = stash(storageStack, stackToStash, simulate);
		}

		if (stashResult.getCount() < stackToStash.getCount()) {
			int countToTake = stackToStash.getCount() - stashResult.getCount();
			ItemStack takeResult = slot.safeTake(countToTake, countToTake, player);
			stash(storageStack, takeResult, null);
			return true;
		}

		return super.overrideStackedOnOther(storageStack, slot, action, player);
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack storageStack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess carriedAccess) {
		if (storageStack.getCount() > 1 || !slot.mayPlace(storageStack) || action != ClickAction.SECONDARY) {
			return super.overrideOtherStackedOnMe(storageStack, otherStack, slot, action, player, carriedAccess);
		}

		ItemStack result = stash(storageStack, otherStack, null);
		if (result.getCount() != otherStack.getCount()) {
			carriedAccess.set(result);
			slot.set(storageStack);
			return true;
		}

		return super.overrideOtherStackedOnMe(storageStack, otherStack, slot, action, player, carriedAccess);
	}

}
