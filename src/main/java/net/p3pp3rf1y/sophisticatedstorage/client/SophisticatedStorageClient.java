package net.p3pp3rf1y.sophisticatedstorage.client;

import net.fabricmc.api.ClientModInitializer;
import net.p3pp3rf1y.sophisticatedstorage.client.init.ModBlocksClient;
import net.p3pp3rf1y.sophisticatedstorage.client.init.ModItemsClient;
import net.p3pp3rf1y.sophisticatedstorage.compat.litematica.LitematicaPackets;
import net.p3pp3rf1y.sophisticatedstorage.init.ModPackets;

public class SophisticatedStorageClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientEventHandler.registerHandlers();

		ModBlocksClient.registerScreens();
		ModItemsClient.registerScreens();

		ModPackets.registerClientPackets();
		LitematicaPackets.registerClientPackets();
    }
}
