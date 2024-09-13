package net.p3pp3rf1y.sophisticatedstorage.client.init;

import net.minecraft.client.gui.screens.MenuScreens;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.LimitedBarrelScreen;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.LimitedBarrelSettingsScreen;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageScreen;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageSettingsScreen;

import static net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks.LIMITED_BARREL_CONTAINER_TYPE;
import static net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks.LIMITED_BARREL_SETTINGS_CONTAINER_TYPE;
import static net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks.SETTINGS_CONTAINER_TYPE;
import static net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks.STORAGE_CONTAINER_TYPE;

public class ModBlocksClient {
	public static void registerScreens() {
		MenuScreens.register(STORAGE_CONTAINER_TYPE, StorageScreen::constructScreen);
		MenuScreens.register(SETTINGS_CONTAINER_TYPE, StorageSettingsScreen::constructScreen);
		MenuScreens.register(LIMITED_BARREL_CONTAINER_TYPE, LimitedBarrelScreen::new);
		MenuScreens.register(LIMITED_BARREL_SETTINGS_CONTAINER_TYPE, LimitedBarrelSettingsScreen::new);
	}
}
