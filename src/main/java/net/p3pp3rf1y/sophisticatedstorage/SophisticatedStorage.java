package net.p3pp3rf1y.sophisticatedstorage;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.api.ModInitializer;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatRegistry;
import net.p3pp3rf1y.sophisticatedstorage.common.CommonEventHandler;
import net.p3pp3rf1y.sophisticatedstorage.init.*;
import net.neoforged.fml.config.ModConfig;

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
		NeoForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.SERVER, Config.SERVER_SPEC);
		NeoForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
		NeoForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, Config.COMMON_SPEC);
		Config.SERVER.initListeners();
		commonEventHandler.registerHandlers();
		ModCompat.register();
		CompatRegistry.getRegistry(MOD_ID).initCompats();
		ModBlocks.registerHandlers();
		ModItems.registerHandlers();
		ModPayloads.registerPackets();
		SophisticatedStorage.setup();
		ModParticles.registerParticles();

		CompatRegistry.getRegistry(MOD_ID).setupCompats();
	}

	private static void setup() {
		ModBlocks.registerDispenseBehavior();
		ModBlocks.registerCauldronInteractions();
	}

	public static ResourceLocation getRL(String regName) {
		return ResourceLocation.parse(getRegistryName(regName));
	}

	public static String getRegistryName(String regName) {
		return MOD_ID + ":" + regName;
	}
}
