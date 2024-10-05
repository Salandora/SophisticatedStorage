package net.p3pp3rf1y.sophisticatedstorage.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.p3pp3rf1y.sophisticatedcore.network.PacketHelper;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;

import java.util.UUID;

public class RequestStorageContentsPacket implements FabricPacket {
	public static final PacketType<RequestStorageContentsPacket> TYPE = PacketType.create(new ResourceLocation(SophisticatedStorage.MOD_ID, "request_storage_contents"), RequestStorageContentsPacket::new);
	private final UUID storageUuid;

	public RequestStorageContentsPacket(UUID storageUuid) {
		this.storageUuid = storageUuid;
	}

	public RequestStorageContentsPacket(FriendlyByteBuf buffer) {
		this(buffer.readUUID());
	}

	public void handle(ServerPlayer player, PacketSender responseSender) {
		PacketHelper.sendToPlayer(new StorageContentsPacket(storageUuid, ItemContentsStorage.get().getOrCreateStorageContents(storageUuid)), player);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(storageUuid);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
