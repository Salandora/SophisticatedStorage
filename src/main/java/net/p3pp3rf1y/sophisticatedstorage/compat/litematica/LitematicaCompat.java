package net.p3pp3rf1y.sophisticatedstorage.compat.litematica;

import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;
import net.p3pp3rf1y.sophisticatedcore.compat.litematica.LitematicaCompat.LitematicaWrapper;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.item.StackStorageWrapper;

import static net.p3pp3rf1y.sophisticatedcore.compat.litematica.LitematicaCompat.LITEMATICA_CAPABILITY;
import static net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks.ALL_STORAGECONTAINER_ITEMS;

public class LitematicaCompat implements ICompat {
	@Override
	public void setup() {
		LITEMATICA_CAPABILITY.registerForItems((stack, context) -> new LitematicaWrapper(StackStorageWrapper.fromData(stack), (uuid) -> new ItemStorageContentsMessage(uuid, ItemContentsStorage.get().getOrCreateStorageContents(uuid))), ALL_STORAGECONTAINER_ITEMS);
	}
}
