package net.p3pp3rf1y.sophisticatedstorage.compat.litematica;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;

public class LitematicaPayloads {
	@Environment(EnvType.CLIENT)
	public static void registerClientPackets() {
		PayloadTypeRegistry.playS2C().register(LitematicaItemStorageContentsPayload.TYPE, LitematicaItemStorageContentsPayload.STREAM_CODEC);


		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			ClientPlayNetworking.registerGlobalReceiver(LitematicaItemStorageContentsPayload.TYPE, LitematicaItemStorageContentsPayload::handlePayload);
		}
	}
}
