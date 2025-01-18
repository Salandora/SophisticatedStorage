package net.p3pp3rf1y.sophisticatedstorage.compat.jei.subtypes;

import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;

import java.util.StringJoiner;

public class BarrelSubtypeInterpreter extends PropertyBasedSubtypeInterpreter {
	public BarrelSubtypeInterpreter() {
		addOptionalProperty(WoodStorageBlockItem::getWoodType);
		addOptionalProperty(StorageBlockItem::getMainColorFromComponentHolder);
		addOptionalProperty(StorageBlockItem::getAccentColorFromComponentHolder);
		addProperty(BarrelBlockItem::isFlatTop);
	}

	@Override
	public String getLegacyStringSubtypeInfo(ItemStack itemStack, UidContext context) {
		StringJoiner result = new StringJoiner(",");
		WoodStorageBlockItem.getWoodType(itemStack).ifPresent(woodName -> result.add("woodName:" + woodName));
		StorageBlockItem.getMainColorFromComponentHolder(itemStack).ifPresent(mainColor -> result.add("mainColor:" + mainColor));
		StorageBlockItem.getAccentColorFromComponentHolder(itemStack).ifPresent(accentColor -> result.add("accentColor:" + accentColor));
		result.add("flatTop:" + BarrelBlockItem.isFlatTop(itemStack));
		return "{" + result + "}";
	}
}
