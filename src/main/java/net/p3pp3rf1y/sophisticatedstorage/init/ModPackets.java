package net.p3pp3rf1y.sophisticatedstorage.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.p3pp3rf1y.sophisticatedstorage.network.*;

public class ModPackets {
	private ModPackets() {
	}

	public static void registerPackets() {
		ServerPlayNetworking.registerGlobalReceiver(OpenStorageInventoryPacket.TYPE, OpenStorageInventoryPacket::handle);
		ServerPlayNetworking.registerGlobalReceiver(RequestStorageContentsPacket.TYPE, RequestStorageContentsPacket::handle);
		ServerPlayNetworking.registerGlobalReceiver(ScrolledToolPacket.TYPE, ScrolledToolPacket::handle);
		ServerPlayNetworking.registerGlobalReceiver(RequestPlayerSettingsPacket.TYPE, RequestPlayerSettingsPacket::handle);
	}

	@Environment(EnvType.CLIENT)
	public static void registerClientPackets() {
		ClientPlayNetworking.registerGlobalReceiver(StorageContentsPacket.TYPE, StorageContentsPacket::handle);
	}
}
