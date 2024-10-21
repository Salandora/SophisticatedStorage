package net.p3pp3rf1y.sophisticatedstorage.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.p3pp3rf1y.sophisticatedstorage.network.*;

public class ModPayloads {
	private ModPayloads() {
	}

	public static void registerPackets() {
		registerC2S(OpenStorageInventoryPayload.TYPE, OpenStorageInventoryPayload.STREAM_CODEC, OpenStorageInventoryPayload::handlePayload);
		registerC2S(RequestStorageContentsPayload.TYPE, RequestStorageContentsPayload.STREAM_CODEC, RequestStorageContentsPayload::handlePayload);
		registerC2S(ScrolledToolPayload.TYPE, ScrolledToolPayload.STREAM_CODEC, ScrolledToolPayload::handlePayload);
		registerC2S(RequestPlayerSettingsPayload.TYPE, RequestPlayerSettingsPayload.STREAM_CODEC, (payload, context) -> RequestPlayerSettingsPayload.handlePayload(context));

		PayloadTypeRegistry.playS2C().register(StorageContentsPayload.TYPE, StorageContentsPayload.STREAM_CODEC);
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			ClientPlayNetworking.registerGlobalReceiver(StorageContentsPayload.TYPE, StorageContentsPayload::handlePayload);
		}
	}

	public static <T extends CustomPacketPayload> void registerC2S(CustomPacketPayload.Type<T> id, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, ServerPlayNetworking.PlayPayloadHandler<T> handler) {
		PayloadTypeRegistry.playC2S().register(id, codec);
		ServerPlayNetworking.registerGlobalReceiver(id, handler);
	}
}
