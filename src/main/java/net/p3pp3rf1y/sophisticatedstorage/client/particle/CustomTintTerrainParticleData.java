package net.p3pp3rf1y.sophisticatedstorage.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedstorage.init.ModParticles;

import java.util.Objects;

public class CustomTintTerrainParticleData extends ParticleType<CustomTintTerrainParticleData> implements ParticleOptions {
	private final BlockState state;
	private final BlockPos pos;

	public CustomTintTerrainParticleData() {
		this(Blocks.AIR.defaultBlockState(), BlockPos.ZERO);
	}

	public CustomTintTerrainParticleData(BlockState state, BlockPos pos) {
		super(false, DESERIALIZER);
		this.state = state;
		this.pos = pos;
	}

	@Override
	public CustomTintTerrainParticleData getType() {
		return ModParticles.TERRAIN_PARTICLE;
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf buffer) {
		buffer.writeVarInt(Block.getId(state));
		buffer.writeBlockPos(pos);
	}

	@Override
	public String writeToString() {
		return BuiltInRegistries.PARTICLE_TYPE.getKey(getType()) + "|" + BlockStateParser.serialize(state) + "|" + pos.toShortString();
	}

	@SuppressWarnings("deprecation")
	public static final Deserializer<CustomTintTerrainParticleData> DESERIALIZER = new Deserializer<>() {
		@Override
		public CustomTintTerrainParticleData fromCommand(ParticleType<CustomTintTerrainParticleData> particleType, StringReader reader)
				throws CommandSyntaxException {
			reader.expect('|');
			BlockState blockState = Objects.requireNonNull(BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), reader, false).blockState());
			reader.expect('|');
			return new CustomTintTerrainParticleData(blockState, fromString(reader.readUnquotedString()));
		}

		private BlockPos fromString(String value) {
			String[] split = value.split(",");
			return BlockPos.containing(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
		}

		@Override
		public CustomTintTerrainParticleData fromNetwork(ParticleType<CustomTintTerrainParticleData> particleType, FriendlyByteBuf buffer) {
			return new CustomTintTerrainParticleData(Objects.requireNonNull(Block.stateById(buffer.readVarInt())), buffer.readBlockPos());
		}
	};

	private final Codec<CustomTintTerrainParticleData> codec = RecordCodecBuilder.create(
			particleDataInstance -> particleDataInstance.group(
					BlockState.CODEC.fieldOf("state").forGetter(data -> data.state),
					BlockPos.CODEC.fieldOf("pos").forGetter(data -> data.pos)
			).apply(particleDataInstance, CustomTintTerrainParticleData::new));

	@Override
	public Codec<CustomTintTerrainParticleData> codec() {
		return codec;
	}

	public BlockState getState() {
		return state;
	}

	public BlockPos getPos() {
		return pos;
	}
}
