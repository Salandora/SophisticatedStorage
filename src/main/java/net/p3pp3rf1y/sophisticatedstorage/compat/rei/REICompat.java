package net.p3pp3rf1y.sophisticatedstorage.compat.rei;

import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleMenuInfoProvider;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.compat.rei.ReiGridMenuInfo;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;

public class REICompat implements REIServerPlugin {
    @Override
    public double getPriority() {
        return 0D;
    }

    @Override
    public void registerItemComparators(ItemComparatorRegistry registry) {
        EntryComparator<Tag> nbtHasher = EntryComparator.nbt();
		EntryComparator<ItemStack> woodStorageNbtInterpreter = (context, stack) -> {
            CompoundTag tag = new CompoundTag();
			WoodStorageBlockItem.getWoodType(stack).ifPresent(woodName -> tag.putString("woodName", woodName.name()));
			StorageBlockItem.getMainColorFromStack(stack).ifPresent(mainColor -> tag.putInt("mainColor", mainColor));
			StorageBlockItem.getAccentColorFromStack(stack).ifPresent(accentColor -> tag.putInt("accentColor", accentColor));
            return nbtHasher.hash(context, tag);
        };

		EntryComparator<ItemStack> barrelNbtInterpreter = (context, stack) -> {
			CompoundTag tag = new CompoundTag();
			WoodStorageBlockItem.getWoodType(stack).ifPresent(woodName -> tag.putString("woodName", woodName.name()));
			StorageBlockItem.getMainColorFromStack(stack).ifPresent(mainColor -> tag.putInt("mainColor", mainColor));
			StorageBlockItem.getAccentColorFromStack(stack).ifPresent(accentColor -> tag.putInt("accentColor", accentColor));
			tag.putBoolean("flatTop", BarrelBlockItem.isFlatTop(stack));
			return nbtHasher.hash(context, tag);
		};

		registry.register(barrelNbtInterpreter, ModBlocks.ALL_BARREL_ITEMS);
		registry.register(woodStorageNbtInterpreter, ModBlocks.CHEST_ITEMS);

		EntryComparator<ItemStack> shulkerBoxNbtInterpreter = (context, stack) -> {
			CompoundTag tag = new CompoundTag();
			StorageBlockItem.getMainColorFromStack(stack).ifPresent(mainColor -> tag.putInt("mainColor", mainColor));
			StorageBlockItem.getAccentColorFromStack(stack).ifPresent(accentColor -> tag.putInt("accentColor", accentColor));
			return nbtHasher.hash(context, tag);
		};

		registry.register(shulkerBoxNbtInterpreter, ModBlocks.SHULKER_BOX_ITEMS);
    }

    @Override
    public void registerMenuInfo(MenuInfoRegistry registry) {
        registry.register(BuiltinPlugin.CRAFTING, StorageContainerMenu.class, SimpleMenuInfoProvider.of(ReiGridMenuInfo::new));
		//registry.register(BuiltinPlugin.STONE_CUTTING, StorageScreen.class, SimpleMenuInfoProvider.of(ReiGridMenuInfo::new));
    }
}
