package net.p3pp3rf1y.sophisticatedstorage.client.render;

import io.github.fabricators_of_create.porting_lib.models.ElementsModel;
import io.github.fabricators_of_create.porting_lib.models.IModelBuilder;
import io.github.fabricators_of_create.porting_lib.models.MeshBakedModel;
import io.github.fabricators_of_create.porting_lib.models.geometry.IGeometryBakingContext;
import io.github.fabricators_of_create.porting_lib.render_types.RenderTypeGroup;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;

import java.util.List;
import java.util.function.Function;

public class SSElementsModel extends ElementsModel {
	public SSElementsModel(List<BlockElement> elements) {
		super(elements);
	}

	@Override
	public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
		TextureAtlasSprite particle = spriteGetter.apply(context.getMaterial("particle"));

		var renderTypeHint = context.getRenderTypeHint();
		var renderTypes = renderTypeHint != null ? context.getRenderType(renderTypeHint) : RenderTypeGroup.EMPTY;
		IModelBuilder<?> builder = new Simple(context.useAmbientOcclusion(), context.useBlockLight(), context.isGui3d(),
				context.getTransforms(), overrides, particle, renderTypes);

		addQuads(context, builder, baker, spriteGetter, modelState);

		return builder.build();
	}


	static class Simple implements IModelBuilder<Simple> {
		private final MeshBuilder builder;
		private final boolean hasAmbientOcclusion, usesBlockLight, isGui3d;
		private final ItemTransforms transforms;
		private final ItemOverrides overrides;
		private final TextureAtlasSprite particle;
		private final RenderTypeGroup renderTypes;
		private final RenderMaterial material;

		private Simple(boolean hasAmbientOcclusion, boolean usesBlockLight, boolean isGui3d,
				ItemTransforms transforms, ItemOverrides overrides, TextureAtlasSprite particle,
				RenderTypeGroup renderTypes) {
			this.builder = RendererAccess.INSTANCE.getRenderer().meshBuilder();//new SimpleBakedModel.Builder(hasAmbientOcclusion, usesBlockLight, isGui3d, transforms, overrides).particle(particle);
			this.hasAmbientOcclusion = hasAmbientOcclusion;
			this.usesBlockLight = usesBlockLight;
			this.isGui3d = isGui3d;
			this.transforms = transforms;
			this.overrides = overrides;
			this.particle = particle;
			this.renderTypes = renderTypes;
			this.material = RendererAccess.INSTANCE.getRenderer().materialFinder().blendMode(BlendMode.fromRenderLayer(renderTypes.block())).find();
		}

		@Override
		public SSElementsModel.Simple addCulledFace(Direction facing, BakedQuad quad) {
			builder.getEmitter().fromVanilla(quad, material, facing).emit();
			return this;
		}

		@Override
		public SSElementsModel.Simple addUnculledFace(BakedQuad quad) {
			builder.getEmitter().fromVanilla(quad, material, null).emit();
			return this;
		}

		@Override
		public SSElementsModel.Simple addFace(QuadView quad) {
			builder.getEmitter().copyFrom(quad).emit();
			return this;
		}

		@Deprecated
		@Override
		public BakedModel build() {
			return new MeshBakedModel(builder.build(), hasAmbientOcclusion, usesBlockLight, isGui3d, particle, transforms, overrides);
		}
	}
}
