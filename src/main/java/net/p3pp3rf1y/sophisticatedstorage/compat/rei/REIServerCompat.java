package net.p3pp3rf1y.sophisticatedstorage.compat.rei;

import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.compat.rei.subtypes.PropertyBasedSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.compat.rei.subtypes.BarrelSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorage.compat.rei.subtypes.ChestSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorage.compat.rei.subtypes.ShulkerBoxSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;

import java.util.function.Supplier;

public class REIServerCompat implements REIServerPlugin {
	private final PropertyBasedSubtypeInterpreter chestSubtypeInterpreter = new ChestSubtypeInterpreter();
	private final PropertyBasedSubtypeInterpreter barrelSubtypeInterpreter = new BarrelSubtypeInterpreter();
	private final PropertyBasedSubtypeInterpreter shulkerBoxSubtypeInterpreter = new ShulkerBoxSubtypeInterpreter();

    @Override
    public double getPriority() {
        return 0D;
    }

    @Override
    public void registerItemComparators(ItemComparatorRegistry registry) {
		registry.register(barrelSubtypeInterpreter, ModBlocks.ALL_BARREL_ITEMS.stream().map(Supplier::get).toArray(BlockItem[]::new));
		registry.register(chestSubtypeInterpreter, ModBlocks.CHEST_ITEMS.stream().map(Supplier::get).toArray(BlockItem[]::new));
		registry.register(shulkerBoxSubtypeInterpreter, ModBlocks.SHULKER_BOX_ITEMS.stream().map(Supplier::get).toArray(BlockItem[]::new));
    }
}
