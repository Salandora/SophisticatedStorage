package net.p3pp3rf1y.sophisticatedstorage.compat.litematica;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class LitematicaPackets {
	@Environment(EnvType.CLIENT)
	public static void registerClientPackets() {
		ClientPlayNetworking.registerGlobalReceiver(ItemStorageContentsMessage.TYPE, ItemStorageContentsMessage::handle);
	}
}
