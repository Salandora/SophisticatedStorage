package net.p3pp3rf1y.sophisticatedstorage.compat.litematica;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedcore.compat.litematica.LitematicaHelper;
import net.p3pp3rf1y.sophisticatedcore.util.StreamCodecHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;

import java.util.UUID;

public record LitematicaItemStorageContentsPayload(UUID storageUuid, CompoundTag storageContents) implements CustomPacketPayload {
	public static final Type<LitematicaItemStorageContentsPayload> TYPE = new Type<>(SophisticatedBackpacks.getRL("litematica_item_storage_contents"));
	public static final StreamCodec<ByteBuf, LitematicaItemStorageContentsPayload> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC,
			LitematicaItemStorageContentsPayload::storageUuid,
			StreamCodecHelper.ofNullable(ByteBufCodecs.COMPOUND_TAG),
			LitematicaItemStorageContentsPayload::storageContents,
			LitematicaItemStorageContentsPayload::new);

	public static void handlePayload(LitematicaItemStorageContentsPayload payload, ClientPlayNetworking.Context context) {
		if (payload.storageContents == null) {
			return;
		}

		ItemContentsStorage.get().setStorageContents(payload.storageUuid, payload.storageContents);
		LitematicaHelper.incrementReceived(1);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
