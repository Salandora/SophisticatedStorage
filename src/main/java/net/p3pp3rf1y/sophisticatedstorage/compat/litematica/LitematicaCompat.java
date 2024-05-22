package net.p3pp3rf1y.sophisticatedstorage.compat.litematica;

import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;
import net.p3pp3rf1y.sophisticatedcore.compat.litematica.network.LitematicaPacketHandler;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.common.CapabilityStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;

import static net.p3pp3rf1y.sophisticatedcore.compat.litematica.LitematicaCompat.LITEMATICA_CAPABILITY;

public class LitematicaCompat implements ICompat {
	public static void alwaysInit() {
		LitematicaPacketHandler.registerS2CMessage(ItemStorageContentsMessage.class, ItemStorageContentsMessage::new);


		LITEMATICA_CAPABILITY.registerForItems((stack, context) ->
						CapabilityStorageWrapper.get(stack).map(wrapper -> new net.p3pp3rf1y.sophisticatedcore.compat.litematica.LitematicaCompat.LitematicaWrapper(wrapper, (uuid) -> new ItemStorageContentsMessage(uuid, ItemContentsStorage.get().getOrCreateStorageContents(uuid))))
								.orElse(null),
				ModBlocks.ALL_STORAGECONTAINER_ITEMS);
	}

	@Override
	public void setup() {
	}
}
