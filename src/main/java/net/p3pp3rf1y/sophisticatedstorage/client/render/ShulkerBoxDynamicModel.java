package net.p3pp3rf1y.sophisticatedstorage.client.render;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import io.github.fabricators_of_create.porting_lib.models.geometry.IGeometryLoader;
import io.github.fabricators_of_create.porting_lib.models.geometry.IUnbakedGeometry;
import net.p3pp3rf1y.sophisticatedcore.client.render.CustomParticleIcon;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.ShulkerBoxBlockEntity;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ShulkerBoxDynamicModel implements IUnbakedGeometry<ShulkerBoxDynamicModel> {
	private static final String BLOCK_BREAK_FOLDER = "block/break/";
	public static final ResourceLocation TINTABLE_BREAK_TEXTURE = SophisticatedStorage.getRL(BLOCK_BREAK_FOLDER + "tintable_shulker_box");
	public static final ResourceLocation MAIN_BREAK_TEXTURE = SophisticatedStorage.getRL(BLOCK_BREAK_FOLDER + "shulker_box");

	@Override
	public BakedModel bake(BlockModel context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation, boolean isGui3d) {
		return new ShulkerBoxBakedModel();
	}

	private static class ShulkerBoxBakedModel implements BakedModel, CustomParticleIcon {
		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
			return Collections.emptyList();
		}

		@Override
		public ItemTransforms getTransforms() {
			return ItemTransforms.NO_TRANSFORMS;
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

		@Override
		public TextureAtlasSprite getParticleIcon() {
			BakedModel model = Minecraft.getInstance().getModelManager().getModel(BlockModelShaper.stateToModelLocation(Blocks.OAK_PLANKS.defaultBlockState()));
			return model.getParticleIcon();
		}

		@Override
		public TextureAtlasSprite getParticleIcon(BlockState state, BlockAndTintGetter blockView, BlockPos pos) {
			Object data = blockView.getBlockEntityRenderData(pos);
			if (data instanceof ShulkerBoxBlockEntity.ModelData sbd) {
				ResourceLocation texture = TINTABLE_BREAK_TEXTURE;
				if (Boolean.FALSE.equals(sbd.hasMainColor())) {
					texture = MAIN_BREAK_TEXTURE;
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

	public static final class Loader implements IGeometryLoader<ShulkerBoxDynamicModel> {
		public static final Loader INSTANCE = new Loader();

		@Override
		public ShulkerBoxDynamicModel read(JsonObject modelContents, JsonDeserializationContext deserializationContext) {
			return new ShulkerBoxDynamicModel();
		}
	}
}
