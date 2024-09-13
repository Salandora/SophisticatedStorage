package net.p3pp3rf1y.sophisticatedstorage.init;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedstorage.client.particle.CustomTintTerrainParticleData;

public class ModParticles {
	private ModParticles() {
	}

	public static final CustomTintTerrainParticleData TERRAIN_PARTICLE = register("terrain_particle", new CustomTintTerrainParticleData());

	public static <T extends ParticleType<?>> T register(String id, T value) {
		return Registry.register(BuiltInRegistries.PARTICLE_TYPE, SophisticatedCore.getRL(id), value);
	}

	public static void registerParticles() {
	}

}
