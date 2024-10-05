package net.p3pp3rf1y.sophisticatedstorage.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.p3pp3rf1y.sophisticatedcore.network.PacketHelper;
import net.p3pp3rf1y.sophisticatedcore.network.SyncPlayerSettingsPacket;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsManager;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.settings.StorageSettingsHandler;

public class RequestPlayerSettingsPacket implements FabricPacket {
	public static final PacketType<RequestPlayerSettingsPacket> TYPE = PacketType.create(new ResourceLocation(SophisticatedStorage.MOD_ID, "request_player_settings"), RequestPlayerSettingsPacket::new);

	public RequestPlayerSettingsPacket() {
	}

	public RequestPlayerSettingsPacket(FriendlyByteBuf buffer) {
	}

	public void handle(ServerPlayer player, PacketSender responseSender) {
		String playerTagName = StorageSettingsHandler.SOPHISTICATED_STORAGE_SETTINGS_PLAYER_TAG;
		PacketHelper.sendToPlayer(new SyncPlayerSettingsPacket(playerTagName, SettingsManager.getPlayerSettingsTag(player, playerTagName)), (ServerPlayer) player);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		//noop
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
