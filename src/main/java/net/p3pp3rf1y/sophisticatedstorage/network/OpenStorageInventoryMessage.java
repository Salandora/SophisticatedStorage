package net.p3pp3rf1y.sophisticatedstorage.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SophisticatedMenuProvider;
import net.p3pp3rf1y.sophisticatedcore.network.SimplePacketBase;
import net.p3pp3rf1y.sophisticatedcore.util.WorldHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.LimitedBarrelBlock;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.LimitedBarrelContainerMenu;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageContainerMenu;

public class OpenStorageInventoryMessage extends SimplePacketBase {
	private final BlockPos pos;

	public OpenStorageInventoryMessage(BlockPos pos) {this.pos = pos;}

	public OpenStorageInventoryMessage(FriendlyByteBuf buffer) {
		this(buffer.readBlockPos());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null) {
				return;
			}

			player.sophisticatedCore_openMenu(
					new SophisticatedMenuProvider(
							(w, p, pl) -> instantiateContainerMenu(w, pl, pos),
							WorldHelper.getBlockEntity(player.level(), pos, StorageBlockEntity.class).map(StorageBlockEntity::getDisplayName).orElse(Component.empty()),
							false
					),
					pos
			);
		});
		return true;
	}

	private static StorageContainerMenu instantiateContainerMenu(int windowId, Player player, BlockPos pos) {
		if (player.level().getBlockState(pos).getBlock() instanceof LimitedBarrelBlock) {
			return new LimitedBarrelContainerMenu(windowId, player, pos);
		} else {
			return new StorageContainerMenu(windowId, player, pos);
		}
	}
}
