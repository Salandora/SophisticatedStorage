package net.p3pp3rf1y.sophisticatedstorage.client.render;

import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.p3pp3rf1y.sophisticatedcore.api.client.model.loading.IGeometryBakingContext;
import net.p3pp3rf1y.sophisticatedcore.api.client.model.loading.IUnbakedGeometry;
import net.p3pp3rf1y.sophisticatedcore.client.model.MeshBakedModel;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ElementsModel implements IUnbakedGeometry {
	private static final FaceBakery FACE_BAKERY = new FaceBakery();

	private final List<BlockElement> elements;

	public ElementsModel(List<BlockElement> elements) {
		this.elements = elements;
	}

	@Override
	public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
		TextureAtlasSprite particle = spriteGetter.apply(context.getMaterial("particle"));

		Simple builder = new Simple(context.useAmbientOcclusion(), context.useBlockLight(), context.isGui3d(), context.getTransforms(), overrides, particle);

		for (BlockElement element : elements) {
			for (Map.Entry<Direction, BlockElementFace> entry : element.faces.entrySet()) {
				BlockElementFace face = entry.getValue();
				BakedQuad quad = bakeFace(element, face, spriteGetter.apply(context.getMaterial(face.texture())), entry.getKey(), modelState);

				if (face.cullForDirection() == null) {
					builder.addUnculledFace(quad);
				} else {
					builder.addCulledFace(Direction.rotate(modelState.getRotation().getMatrix(), face.cullForDirection()), quad);
				}
			}
		}

		return builder.build();
	}

	private static BakedQuad bakeFace(BlockElement element, BlockElementFace face, TextureAtlasSprite sprite, Direction facing, ModelState state) {
		return FACE_BAKERY.bakeQuad(element.from, element.to, face, sprite, facing, state, element.rotation, element.shade);
	}

	static class Simple {
		private final MeshBuilder builder;
		private final boolean hasAmbientOcclusion, usesBlockLight, isGui3d;
		private final ItemTransforms transforms;
		private final ItemOverrides overrides;
		private final TextureAtlasSprite particle;
		private final RenderMaterial material;

		private Simple(boolean hasAmbientOcclusion, boolean usesBlockLight, boolean isGui3d,
				ItemTransforms transforms, ItemOverrides overrides, TextureAtlasSprite particle) {
			this.builder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
			this.hasAmbientOcclusion = hasAmbientOcclusion;
			this.usesBlockLight = usesBlockLight;
			this.isGui3d = isGui3d;
			this.transforms = transforms;
			this.overrides = overrides;
			this.particle = particle;
			this.material = RendererAccess.INSTANCE.getRenderer().materialFinder().blendMode(BlendMode.DEFAULT).find();
		}

		public ElementsModel.Simple addCulledFace(Direction facing, BakedQuad quad) {
			builder.getEmitter().fromVanilla(quad, material, facing).emit();
			return this;
		}

		public ElementsModel.Simple addUnculledFace(BakedQuad quad) {
			builder.getEmitter().fromVanilla(quad, material, null).emit();
			return this;
		}

		@Deprecated
		public BakedModel build() {
			return new MeshBakedModel(builder.build(), hasAmbientOcclusion, usesBlockLight, isGui3d, particle, transforms, overrides);
		}
	}
}