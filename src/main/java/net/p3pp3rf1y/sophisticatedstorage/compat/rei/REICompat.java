package net.p3pp3rf1y.sophisticatedstorage.compat.rei;

import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleMenuInfoProvider;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.compat.rei.ReiGridMenuInfo;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;

import java.util.function.Supplier;

public class REICompat implements REIServerPlugin {
    @Override
    public double getPriority() {
        return 0D;
    }

    @Override
    public void registerItemComparators(ItemComparatorRegistry registry) {
        EntryComparator<DataComponentMap> componentHasher = EntryComparator.component();
		EntryComparator<ItemStack> woodStorageNbtInterpreter = (context, stack) -> {
			var builder = DataComponentMap.builder();
			WoodStorageBlockItem.getWoodType(stack).ifPresent(woodType -> builder.set(ModDataComponents.WOOD_TYPE.get(), woodType));
			StorageBlockItem.getMainColorFromComponentHolder(stack).ifPresent(mainColor -> builder.set(ModCoreDataComponents.MAIN_COLOR.get(), mainColor));
			StorageBlockItem.getAccentColorFromComponentHolder(stack).ifPresent(accentColor -> builder.set(ModCoreDataComponents.ACCENT_COLOR.get(), accentColor));
            return componentHasher.hash(context, new PatchedDataComponentMap(builder.build()));
        };

		EntryComparator<ItemStack> barrelNbtInterpreter = (context, stack) -> {
			var builder = DataComponentMap.builder();
			WoodStorageBlockItem.getWoodType(stack).ifPresent(woodType -> builder.set(ModDataComponents.WOOD_TYPE.get(), woodType));
			StorageBlockItem.getMainColorFromComponentHolder(stack).ifPresent(mainColor -> builder.set(ModCoreDataComponents.MAIN_COLOR.get(), mainColor));
			StorageBlockItem.getAccentColorFromComponentHolder(stack).ifPresent(accentColor -> builder.set(ModCoreDataComponents.ACCENT_COLOR.get(), accentColor));
			builder.set(ModDataComponents.FLAT_TOP.get(), BarrelBlockItem.isFlatTop(stack));
			return componentHasher.hash(context, new PatchedDataComponentMap(builder.build()));
		};

		registry.register(barrelNbtInterpreter, ModBlocks.ALL_BARREL_ITEMS.stream().map(Supplier::get).toArray(BlockItem[]::new));
		registry.register(woodStorageNbtInterpreter, ModBlocks.CHEST_ITEMS.stream().map(Supplier::get).toArray(BlockItem[]::new));

		EntryComparator<ItemStack> shulkerBoxNbtInterpreter = (context, stack) -> {
			var builder = DataComponentMap.builder();
			StorageBlockItem.getMainColorFromComponentHolder(stack).ifPresent(mainColor -> builder.set(ModCoreDataComponents.MAIN_COLOR.get(), mainColor));
			StorageBlockItem.getAccentColorFromComponentHolder(stack).ifPresent(accentColor -> builder.set(ModCoreDataComponents.ACCENT_COLOR.get(), accentColor));
			return componentHasher.hash(context, new PatchedDataComponentMap(builder.build()));
		};

		registry.register(shulkerBoxNbtInterpreter, ModBlocks.SHULKER_BOX_ITEMS.stream().map(Supplier::get).toArray(BlockItem[]::new));
    }

    @Override
    public void registerMenuInfo(MenuInfoRegistry registry) {
        registry.register(BuiltinPlugin.CRAFTING, StorageContainerMenu.class, SimpleMenuInfoProvider.of(ReiGridMenuInfo::crafting));
		// TODO: Fix this
		//registry.register(BuiltinPlugin.STONE_CUTTING, StorageScreen.class, SimpleMenuInfoProvider.of(ReiGridMenuInfo::stonecutting));
		//registry.register(BuiltinPlugin.CRAFTING, StorageContainerMenu.class, SimpleMenuInfoProvider.of(ReiGridMenuInfo::smithing));
    }
}
