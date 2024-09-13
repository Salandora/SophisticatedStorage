package net.p3pp3rf1y.sophisticatedstorage.network;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.p3pp3rf1y.sophisticatedcore.client.render.ClientStorageContentsTooltipBase;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;

import java.util.UUID;
import javax.annotation.Nullable;

public class StorageContentsPacket implements FabricPacket {
	public static final PacketType<StorageContentsPacket> TYPE = PacketType.create(new ResourceLocation(SophisticatedStorage.MOD_ID, "storage_contents"), StorageContentsPacket::new);
	private final UUID shulkerBoxUuid;
	@Nullable
	private final CompoundTag contents;

	public StorageContentsPacket(UUID shulkerBoxUuid, @Nullable CompoundTag contents) {
		this.shulkerBoxUuid = shulkerBoxUuid;
		this.contents = contents;
	}

	public StorageContentsPacket(FriendlyByteBuf buffer) {
		this(buffer.readUUID(), buffer.readNbt());
	}

	public void handle(LocalPlayer player, PacketSender responseSender) {
		if (contents == null) {
			return;
		}

		ItemContentsStorage.get().setStorageContents(shulkerBoxUuid, contents);
		ClientStorageContentsTooltipBase.refreshContents();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(shulkerBoxUuid);
		buffer.writeNbt(contents);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
