package net.p3pp3rf1y.sophisticatedstorage.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.util.BlockItemBase;
import net.p3pp3rf1y.sophisticatedstorage.block.ITintableBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModDataComponents;

import java.util.Optional;

import static net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity.STORAGE_WRAPPER_TAG;

public class StorageBlockItem extends BlockItemBase implements ITintableBlockItem {

    public StorageBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

	public static Optional<CompoundTag> getEntityWrapperTagFromStack(ItemStack barrelStack) {
        CustomData customData = barrelStack.get(DataComponents.BLOCK_ENTITY_DATA);
		if (customData == null) {
            return Optional.empty();
        }
        return Optional.of(customData.copyTag().getCompound(STORAGE_WRAPPER_TAG));
	}

	public static Optional<Integer> getMainColorFromStack(ItemStack storageStack) {
		return getEntityWrapperTagFromStack(storageStack).map(tag -> tag.getInt(StorageWrapper.MAIN_COLOR_TAG)).or(() -> Optional.ofNullable(storageStack.get(ModCoreDataComponents.MAIN_COLOR)));
	}
	public static Optional<Integer> getAccentColorFromStack(ItemStack storageStack) {
		return getEntityWrapperTagFromStack(storageStack).map(tag -> tag.getInt(StorageWrapper.ACCENT_COLOR_TAG)).or(() -> Optional.ofNullable(storageStack.get(ModCoreDataComponents.ACCENT_COLOR)));
	}

    @Override
    public void setMainColor(ItemStack storageStack, int mainColor) {
        storageStack.set(ModCoreDataComponents.MAIN_COLOR, mainColor);
    }

    @Override
    public Optional<Integer> getMainColor(ItemStack storageStack) {
        return StorageBlockItem.getMainColorFromStack(storageStack);
    }

    @Override
    public void setAccentColor(ItemStack storageStack, int accentColor) {
        storageStack.set(ModCoreDataComponents.ACCENT_COLOR, accentColor);
    }

    @Override
    public void removeMainColor(ItemStack stack) {
        stack.remove(ModCoreDataComponents.MAIN_COLOR);
    }

    @Override
    public void removeAccentColor(ItemStack stack) {
        stack.remove(ModCoreDataComponents.ACCENT_COLOR);
    }

    @Override
    public Optional<Integer> getAccentColor(ItemStack stack) {
        return StorageBlockItem.getAccentColorFromStack(stack);
    }

    public static boolean showsTier(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.SHOWS_TIER, true);
    }

    public static void setShowsTier(ItemStack stack, boolean showsTier) {
        if (showsTier) {
            stack.remove(ModDataComponents.SHOWS_TIER);
        } else {
            stack.set(ModDataComponents.SHOWS_TIER, false);
        }
    }
}
