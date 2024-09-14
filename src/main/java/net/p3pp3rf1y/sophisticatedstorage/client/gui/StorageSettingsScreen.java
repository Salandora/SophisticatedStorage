package net.p3pp3rf1y.sophisticatedstorage.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SettingsScreen;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.network.PacketHelper;
import net.p3pp3rf1y.sophisticatedcore.settings.StorageSettingsTabControlBase;
import net.p3pp3rf1y.sophisticatedstorage.network.OpenStorageInventoryPacket;

public class StorageSettingsScreen extends SettingsScreen {
	public StorageSettingsScreen(SettingsContainerMenu<?> screenContainer, Inventory inv, Component title) {
		super(screenContainer, inv, title);
	}

	@Override
	protected StorageSettingsTabControlBase initializeTabControl() {
		return new StorageSettingsTabControl(this, new Position(leftPos + imageWidth, topPos + 4));
	}

	@Override
	protected void sendStorageInventoryScreenOpenMessage() {
		PacketHelper.sendToServer(new OpenStorageInventoryPacket(menu.getBlockPosition()));
	}

	public static StorageSettingsScreen constructScreen(SettingsContainerMenu<?> screenContainer, Inventory inventory, Component title) {
		return new StorageSettingsScreen(screenContainer, inventory, title);
	}
}
