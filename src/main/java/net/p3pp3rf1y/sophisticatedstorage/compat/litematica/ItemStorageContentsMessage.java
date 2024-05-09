package net.p3pp3rf1y.sophisticatedstorage.compat.litematica;

import com.google.common.collect.Maps;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.compat.litematica.LitematicaCompat;
import net.p3pp3rf1y.sophisticatedcore.network.SimplePacketBase;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.common.CapabilityStorageWrapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemStorageContentsMessage extends SimplePacketBase {
	public static ItemStorageContentsMessage create(List<UUID> uuids) {
		Map<UUID, CompoundTag> contents = Maps.toMap(uuids, ItemContentsStorage.get()::getOrCreateStorageContents);
		return new ItemStorageContentsMessage(contents);
	}

	private final Map<UUID, CompoundTag> storageContents;

	public ItemStorageContentsMessage(Map<UUID, CompoundTag> storageContents) {
		this.storageContents = storageContents;
	}
	public ItemStorageContentsMessage(FriendlyByteBuf buffer) {
		this(buffer.readMap(FriendlyByteBuf::readUUID, FriendlyByteBuf::readNbt));
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeMap(this.storageContents, FriendlyByteBuf::writeUUID, FriendlyByteBuf::writeNbt);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			Player player = context.getClientPlayer();
			if (player == null || this.storageContents == null) {
				return;
			}

			this.storageContents.forEach(ItemContentsStorage.get()::setStorageContents);
			CapabilityStorageWrapper.invalidateCache();
			if (LitematicaCompat.getTask() != null) {
				LitematicaCompat.getTask().incrementReceived(this.storageContents.size());
			}
		});
		return true;
	}
}
