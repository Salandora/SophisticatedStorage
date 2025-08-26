package net.p3pp3rf1y.sophisticatedstorage.client.render;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.p3pp3rf1y.sophisticatedstorage.mixin.client.accessor.SimpleBakedModelBuilderAccessor;

import java.util.List;
import java.util.function.Function;

public record ElementsModel(List<BlockElement> elements) {
	public BakedModel bake(BlockModel context, ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
		TextureAtlasSprite particle = spriteGetter.apply(context.getMaterial("particle"));
		SimpleBakedModel.Builder modelBuilder = SimpleBakedModelBuilderAccessor.create(
				context.hasAmbientOcclusion(),
				context.getGuiLight().lightLikeBlock(),
				true,
				context.getTransforms(),
				overrides
		).particle(particle);

		for (BlockElement element : elements) {
			element.faces.forEach((direction, face) -> {
				var sprite = spriteGetter.apply(context.getMaterial(face.texture));
				var quad = BlockModel.bakeFace(element, face, sprite, direction, modelState, modelLocation);
				//noinspection ConstantValue - this can be null but is not marked as such
				if (face.cullForDirection == null) {
					modelBuilder.addUnculledFace(quad);
				} else {
					modelBuilder.addCulledFace(modelState.getRotation().rotateTransform(face.cullForDirection), quad);
				}
			});
		}
		return modelBuilder.build();
	}
}
