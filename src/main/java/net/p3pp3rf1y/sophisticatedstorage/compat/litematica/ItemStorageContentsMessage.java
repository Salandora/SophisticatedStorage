package net.p3pp3rf1y.sophisticatedstorage.compat.litematica;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedcore.compat.litematica.LitematicaHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;

import java.util.UUID;

public class ItemStorageContentsMessage implements FabricPacket {
	public static final PacketType<ItemStorageContentsMessage> TYPE = PacketType.create(new ResourceLocation(SophisticatedBackpacks.MOD_ID, "litematica_item_storage_contents"), ItemStorageContentsMessage::new);

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
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(this.storageUuid);
		buffer.writeNbt(this.storageContents);
	}

	public void handle(LocalPlayer player, PacketSender responseSender) {
		if (this.storageContents == null) {
			return;
		}

		ItemContentsStorage.get().setStorageContents(this.storageUuid, this.storageContents);
		LitematicaHelper.incrementReceived(1);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
