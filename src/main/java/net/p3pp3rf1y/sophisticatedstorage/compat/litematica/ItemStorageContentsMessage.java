package net.p3pp3rf1y.sophisticatedstorage.compat.litematica;

import me.pepperbell.simplenetworking.S2CPacket;
import me.pepperbell.simplenetworking.SimpleChannel;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.p3pp3rf1y.sophisticatedcore.compat.litematica.LitematicaHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.common.CapabilityStorageWrapper;

import java.util.UUID;

public class ItemStorageContentsMessage implements S2CPacket {
	private final UUID storageUuid;
	private final CompoundTag storageContents;

	public ItemStorageContentsMessage(UUID storageUuid, CompoundTag storageContents) {
		this.storageUuid = storageUuid;
		this.storageContents = storageContents;
	}
	public ItemStorageContentsMessage(FriendlyByteBuf buffer) {
		this(buffer.readUUID(), buffer.readNbt());
	}

	@Override
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeUUID(this.storageUuid);
		buffer.writeNbt(this.storageContents);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handle(Minecraft client, ClientPacketListener listener, PacketSender responseSender, SimpleChannel channel) {
		client.execute(() -> {
			if (client.player == null || this.storageContents == null) {
				return;
			}

			ItemContentsStorage.get().setStorageContents(this.storageUuid, this.storageContents);
			CapabilityStorageWrapper.invalidateCache(this.storageUuid);
			LitematicaHelper.incrementReceived(1);
		});
	}
}
