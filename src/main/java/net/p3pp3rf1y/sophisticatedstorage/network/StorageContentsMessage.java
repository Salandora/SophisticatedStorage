package net.p3pp3rf1y.sophisticatedstorage.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.client.render.ClientStorageContentsTooltip;
import net.p3pp3rf1y.sophisticatedcore.network.SimplePacketBase;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;

import java.util.UUID;
import javax.annotation.Nullable;

public class StorageContentsMessage extends SimplePacketBase {
	private final UUID shulkerBoxUuid;
	@Nullable
	private final CompoundTag contents;

	public StorageContentsMessage(UUID shulkerBoxUuid, @Nullable CompoundTag contents) {
		this.shulkerBoxUuid = shulkerBoxUuid;
		this.contents = contents;
	}

	public StorageContentsMessage(FriendlyByteBuf buffer) {
		this(buffer.readUUID(), buffer.readNbt());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(shulkerBoxUuid);
		buffer.writeNbt(contents);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			Player player = context.getClientPlayer();
			if (player == null || contents == null) {
				return;
			}

			ItemContentsStorage.get().setStorageContents(shulkerBoxUuid, contents);
			ClientStorageContentsTooltip.refreshContents();
		});
		return true;
	}

}
