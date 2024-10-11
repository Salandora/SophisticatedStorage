package net.p3pp3rf1y.sophisticatedstorage.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelBlock;

import javax.annotation.Nullable;


@Environment(EnvType.CLIENT)
public class CustomTintTerrainParticle extends TerrainParticle {
	public CustomTintTerrainParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, BlockState state, BlockPos pos) {
		super(level, x, y, z, xSpeed, ySpeed, zSpeed, state);

		int color;
		if (state.getBlock() instanceof BarrelBlock) {
			color = Minecraft.getInstance().getBlockColors().getColor(state, level, pos, 1000);
		} else {
			color = Minecraft.getInstance().getBlockColors().getColor(state, level, pos, 0);
		}
		rCol *= (color >> 16 & 255) / 255.0F;
		gCol *= (color >> 8 & 255) / 255.0F;
		bCol *= (color & 255) / 255.0F;
	}

	@Environment(EnvType.CLIENT)
	public static class Factory implements ParticleProvider<CustomTintTerrainParticleData> {
		@Nullable
		@Override
		public Particle createParticle(CustomTintTerrainParticleData type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			BlockPos pos = type.getPos();
			BlockState state = type.getState();
			CustomTintTerrainParticle particle = new CustomTintTerrainParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, state, pos);
			particle.sophisticatedCore$updateSprite(state, pos);
			return particle;
		}
	}
}
