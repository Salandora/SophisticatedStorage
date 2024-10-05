package net.p3pp3rf1y.sophisticatedstorage.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.p3pp3rf1y.sophisticatedcore.util.MenuProviderHelper;
import net.p3pp3rf1y.sophisticatedcore.util.WorldHelper;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.LimitedBarrelBlock;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.LimitedBarrelContainerMenu;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageContainerMenu;

public class OpenStorageInventoryPacket implements FabricPacket {
	public static final PacketType<OpenStorageInventoryPacket> TYPE = PacketType.create(new ResourceLocation(SophisticatedStorage.MOD_ID, "open_storage_inventory"), OpenStorageInventoryPacket::new);
	private final BlockPos pos;

	public OpenStorageInventoryPacket(BlockPos pos) {
		this.pos = pos;
	}

	public OpenStorageInventoryPacket(FriendlyByteBuf buffer) {
		this(buffer.readBlockPos());
	}

	public void handle(ServerPlayer player, PacketSender responseSender) {
		player.openMenu(
				MenuProviderHelper.createMenuProvider(
						(w, ctx, pl) -> instantiateContainerMenu(w, pl),
						buffer -> buffer.writeBlockPos(pos),
						WorldHelper.getBlockEntity(player.level(), pos, StorageBlockEntity.class).map(StorageBlockEntity::getDisplayName).orElse(Component.empty())
				)
		);
	}

	private StorageContainerMenu instantiateContainerMenu(int windowId, Player player) {
		if (player.level().getBlockState(pos).getBlock() instanceof LimitedBarrelBlock) {
			return new LimitedBarrelContainerMenu(windowId, player, pos);
		} else {
			return new StorageContainerMenu(windowId, player, pos);
		}
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
