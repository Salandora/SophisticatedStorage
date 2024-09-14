package net.p3pp3rf1y.sophisticatedstorage.common.gui;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;

public class LimitedBarrelSettingsContainerMenu extends StorageSettingsContainerMenu {
	protected LimitedBarrelSettingsContainerMenu(int windowId, Player player, BlockPos pos) {
		super(ModBlocks.LIMITED_BARREL_SETTINGS_CONTAINER_TYPE, windowId, player, pos);
	}

	public static LimitedBarrelSettingsContainerMenu fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		return new LimitedBarrelSettingsContainerMenu(windowId, playerInventory.player, buffer.readBlockPos());
	}
}
