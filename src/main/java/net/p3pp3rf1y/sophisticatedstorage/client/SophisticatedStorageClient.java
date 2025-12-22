package net.p3pp3rf1y.sophisticatedstorage.client;

import net.fabricmc.api.ClientModInitializer;
import net.p3pp3rf1y.sophisticatedstorage.client.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.client.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.network.StoragePacketHandler;

public class SophisticatedStorageClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientEventHandler.registerHandlers();

		ModBlocks.registerScreens();
		ModItems.registerScreens();

		StoragePacketHandler.INSTANCE.initClientListener();
    }
}
