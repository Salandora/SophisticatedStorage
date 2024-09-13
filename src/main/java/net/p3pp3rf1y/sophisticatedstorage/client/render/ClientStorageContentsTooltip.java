package net.p3pp3rf1y.sophisticatedstorage.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.client.render.ClientStorageContentsTooltipBase;
import net.p3pp3rf1y.sophisticatedcore.network.PacketHelper;
import net.p3pp3rf1y.sophisticatedstorage.item.StackStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageContentsTooltip;
import net.p3pp3rf1y.sophisticatedstorage.network.RequestStorageContentsPacket;

import java.util.UUID;

public class ClientStorageContentsTooltip extends ClientStorageContentsTooltipBase {
	private final ItemStack storageItem;

	@SuppressWarnings("unused")
	//parameter needs to be there so that addListener logic would know which event this method listens to
	public static void onWorldLoad(Minecraft client, ClientLevel world) {
		refreshContents();
		lastRequestTime = 0;
	}

	@Override
	public void renderImage(Font font, int leftX, int topY, GuiGraphics guiGraphics) {
		renderTooltip(StackStorageWrapper.fromData(storageItem), font, leftX, topY, guiGraphics);
	}

	public ClientStorageContentsTooltip(StorageContentsTooltip tooltip) {
		storageItem = tooltip.getStorageItem();
	}

	@Override
	protected void sendInventorySyncRequest(UUID uuid) {
		PacketHelper.sendToServer(new RequestStorageContentsPacket(uuid));
	}
}
