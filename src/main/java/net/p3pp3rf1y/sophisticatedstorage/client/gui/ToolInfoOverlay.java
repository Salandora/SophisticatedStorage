package net.p3pp3rf1y.sophisticatedstorage.client.gui;

import com.mojang.blaze3d.platform.Window;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageToolItem;

public class ToolInfoOverlay {
	public static final HudRenderCallback HUD_TOOL_INFO = (guiGraphics, tickDelta) -> {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		InventoryHelper.getItemFromEitherHand(player, ModItems.STORAGE_TOOL).ifPresent(storageTool -> {
			Minecraft mc = Minecraft.getInstance();
			Window window = mc.getWindow();
			int screenWidth = window.getGuiScaledWidth();
			int screenHeight = window.getGuiScaledHeight();
			Font font = mc.font;

			Component overlayMessage = StorageToolItem.getOverlayMessage(storageTool);
			int i = font.width(overlayMessage);
			int x = (screenWidth - i) / 2;
			int y = screenHeight - 75;
			guiGraphics.drawString(font, overlayMessage, x + 1, y, DyeColor.WHITE.getTextColor());
		});
	};
}
