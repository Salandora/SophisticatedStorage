package net.p3pp3rf1y.sophisticatedstorage.compat.jei.subtypes;

import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;

import java.util.StringJoiner;

public class ShulkerBoxSubtypeInterpreter extends PropertyBasedSubtypeInterpreter {
	public ShulkerBoxSubtypeInterpreter() {
		addOptionalProperty(StorageBlockItem::getMainColorFromComponentHolder);
		addOptionalProperty(StorageBlockItem::getAccentColorFromComponentHolder);
	}

	@Override
	public String getLegacyStringSubtypeInfo(ItemStack itemStack, UidContext context) {
		StringJoiner result = new StringJoiner(",");
		StorageBlockItem.getMainColorFromComponentHolder(itemStack).ifPresent(mainColor -> result.add("mainColor:" + mainColor));
		StorageBlockItem.getAccentColorFromComponentHolder(itemStack).ifPresent(accentColor -> result.add("accentColor:" + accentColor));
		return "{" + result + "}";
	}
}
