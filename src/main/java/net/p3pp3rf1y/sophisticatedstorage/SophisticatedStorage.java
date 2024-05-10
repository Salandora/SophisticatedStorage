package net.p3pp3rf1y.sophisticatedstorage;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import net.p3pp3rf1y.sophisticatedstorage.common.CapabilityStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.common.CommonEventHandler;
import net.p3pp3rf1y.sophisticatedstorage.compat.litematica.LitematicaCompat;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModCompat;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.init.ModParticles;
import net.p3pp3rf1y.sophisticatedstorage.network.StoragePacketHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SophisticatedStorage implements ModInitializer {
	public static final String ID = "sophisticatedstorage";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	private final CommonEventHandler commonEventHandler = new CommonEventHandler();

	@SuppressWarnings("java:S1118") //needs to be public for mod to work
	public SophisticatedStorage() {
	}

	@Override
	public void onInitialize() {
		Config.register();

		commonEventHandler.registerHandlers();

		ModBlocks.register();
		ModItems.register();

		CapabilityStorageWrapper.register();

		ModParticles.registerParticles();

		ModCompat.initCompats();
		LitematicaCompat.alwaysInit();

		StoragePacketHandler.init();
		ModCompat.compatsSetup();
	}

	public static ResourceLocation getRL(String regName) {
		return new ResourceLocation(getRegistryName(regName));
	}

	public static String getRegistryName(String regName) {
		return ID + ":" + regName;
	}
}
