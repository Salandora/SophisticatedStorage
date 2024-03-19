package net.p3pp3rf1y.sophisticatedstorage.client.render;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import io.github.fabricators_of_create.porting_lib.model.geometry.IGeometryBakingContext;
import io.github.fabricators_of_create.porting_lib.model.geometry.IGeometryLoader;
import io.github.fabricators_of_create.porting_lib.model.geometry.IUnbakedGeometry;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.WoodStorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.block.WoodStorageBlockEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;

public class ChestDynamicModel implements IUnbakedGeometry<ChestDynamicModel> {
	private static final String BLOCK_BREAK_FOLDER = "block/break/";
	private static final Map<String, ResourceLocation> WOOD_BREAK_TEXTURES = new HashMap<>();
	public static final ResourceLocation TINTABLE_BREAK_TEXTURE = SophisticatedStorage.getRL(BLOCK_BREAK_FOLDER + "tintable_chest");

	static {
		WoodStorageBlockBase.CUSTOM_TEXTURE_WOOD_TYPES.keySet().forEach(woodType -> WOOD_BREAK_TEXTURES.put(woodType.name(), SophisticatedStorage.getRL(BLOCK_BREAK_FOLDER + woodType.name() + "_chest")));
	}

	public static Collection<ResourceLocation> getWoodBreakTextures() {
		return WOOD_BREAK_TEXTURES.values();
	}

	@Override
	public BakedModel bake(IGeometryBakingContext context, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
		return new ChestBakedModel();
	}

	@Override
	public Collection<Material> getMaterials(IGeometryBakingContext context, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		return Collections.emptySet();
	}

	private static class ChestBakedModel implements BakedModel, IDataModel {
		@Override
		public ItemTransforms getTransforms() {
			return ItemTransforms.NO_TRANSFORMS;
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
			return Collections.emptyList();
		}

		@Override
		public boolean useAmbientOcclusion() {
			return true;
		}

		@Override
		public boolean isGui3d() {
			return true;
		}

		@Override
		public boolean usesBlockLight() {
			return true;
		}

		@Override
		public boolean isCustomRenderer() {
			return true;
		}

		@SuppressWarnings("java:S1874")
		@Override
		public TextureAtlasSprite getParticleIcon() {
			BakedModel model = Minecraft.getInstance().getModelManager().getModel(BlockModelShaper.stateToModelLocation(Blocks.OAK_PLANKS.defaultBlockState()));
			//noinspection deprecation
			return model.getParticleIcon();
		}

		@Override
		public TextureAtlasSprite getParticleIcon(BlockState state, BlockAndTintGetter blockView, BlockPos pos) {
			Object attachment = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
			if (attachment instanceof WoodStorageBlockEntity.ModelData data) {
				ResourceLocation texture = TINTABLE_BREAK_TEXTURE;
				if (Boolean.FALSE.equals(data.hasMainColor()) && data.woodName() != null && WOOD_BREAK_TEXTURES.containsKey(data.woodName())) {
					texture = WOOD_BREAK_TEXTURES.get(data.woodName());
				}
				return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
			}

			return getParticleIcon();
		}

		@Override
		public ItemOverrides getOverrides() {
			return new ItemOverrides() {};
		}

	}

	public static final class Loader implements IGeometryLoader<ChestDynamicModel> {
		public static final Loader INSTANCE = new Loader();

		@Override
		public ChestDynamicModel read(JsonObject modelContents, JsonDeserializationContext deserializationContext) {
			return new ChestDynamicModel();
		}
	}
}
