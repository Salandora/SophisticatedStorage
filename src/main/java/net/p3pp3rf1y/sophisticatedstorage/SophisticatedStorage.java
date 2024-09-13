package net.p3pp3rf1y.sophisticatedstorage;

import fuzs.forgeconfigapiport.api.config.v3.ForgeConfigRegistry;
import net.neoforged.fml.config.ModConfig;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import net.p3pp3rf1y.sophisticatedstorage.common.CommonEventHandler;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModCompat;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.init.ModPackets;
import net.p3pp3rf1y.sophisticatedstorage.init.ModParticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SophisticatedStorage implements ModInitializer {
	public static final String MOD_ID = "sophisticatedstorage";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private final CommonEventHandler commonEventHandler = new CommonEventHandler();

	@SuppressWarnings("java:S1118") //needs to be public for mod to work
	public SophisticatedStorage() {
	}

	@Override
	public void onInitialize() {
		ForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.SERVER, Config.SERVER_SPEC);
		ForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
		ForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, Config.COMMON_SPEC);
		Config.SERVER.initListeners();
		commonEventHandler.registerHandlers();
		ModCompat.register();
		ModBlocks.register();
		ModItems.register();
		ModPackets.registerPackets();
		ModParticles.registerParticles();
	}

	public static ResourceLocation getRL(String regName) {
		return new ResourceLocation(getRegistryName(regName));
	}

	public static String getRegistryName(String regName) {
		return MOD_ID + ":" + regName;
	}
}
