package net.p3pp3rf1y.sophisticatedstorage.client.render;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.mixin.client.accessor.BlockModelAccessor;
import net.p3pp3rf1y.sophisticatedstorage.mixin.client.accessor.SimpleBakedModelBuilderAccessor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class CompositeElementsModel extends BlockModel {
	private final List<BlockElement> elements;

	public CompositeElementsModel(@Nullable ResourceLocation parentLocation, Map<String, Either<Material, String>> textureMap) {
		super(parentLocation, Collections.emptyList(), textureMap, true, null, ItemTransforms.NO_TRANSFORMS, Collections.emptyList());
		elements = new ArrayList<>();
	}

	@Override
	public BakedModel bake(ModelBaker modelBaker, BlockModel owner, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ResourceLocation modelLocation, boolean guiLight3d) {
		var particleSprite = spriteGetter.apply(getMaterial("particle"));
		if (getRootModel() == ModelBakery.BLOCK_ENTITY_MARKER) {
			return new BuiltInModel(getTransforms(), getOverrides(modelBaker, owner, spriteGetter), particleSprite, getGuiLight().lightLikeBlock());
		}

		ItemOverrides overrides = getOverrides(modelBaker, owner, spriteGetter);
		ItemTransforms transforms = this.getTransforms();
		var modelBuilder = SimpleCompositeModel.Baked.builder(this.hasAmbientOcclusion(), false, this.getGuiLight().lightLikeBlock(), particleSprite, overrides, transforms);
		for (BlockElement element : getElements()) {
			element.faces.forEach((side, face) -> {
				var sprite = spriteGetter.apply(this.getMaterial(face.texture));
				var simpleModelBuilder = SimpleBakedModelBuilderAccessor.create(this.hasAmbientOcclusion(), this.getGuiLight().lightLikeBlock(), false, transforms, overrides).particle(sprite);
				simpleModelBuilder.addUnculledFace(BlockModel.FACE_BAKERY.bakeQuad(element.from, element.to, face, sprite, side, modelState, element.rotation, element.shade, modelLocation));
				modelBuilder.addLayer(simpleModelBuilder.build());
			});
		}
		return modelBuilder.build();
	}

	@SuppressWarnings("java:S1874") //overriding getElements here
	@Override
	public List<BlockElement> getElements() {
		return elements;
	}

	@Override
	public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter) {
		super.resolveParents(modelGetter);

		copyElementsFromAllIncludedModels();
		copyTexturesFromAllIncludedModels();
	}

	@SuppressWarnings("java:S1874") //need to call getElements even though deprecated
	private void copyElementsFromAllIncludedModels() {
		if (parent != null) {
			elements.addAll(parent.getElements());
			if (parent.getCustomGeometry() instanceof SimpleCompositeModel simpleCompositeModel) {
				elements.addAll(simpleCompositeModel.getElements());
			}
		}
	}

	@SuppressWarnings("java:S5803") //need to call textureMap here even though only visible for testing
	private void copyTexturesFromAllIncludedModels() {
		if (parent != null) {
			((BlockModelAccessor) parent).getTextureMap().forEach(((BlockModelAccessor) this).getTextureMap()::putIfAbsent);
			if (parent.getCustomGeometry() instanceof SimpleCompositeModel simpleCompositeModel) {
				simpleCompositeModel.getTextures().forEach(((BlockModelAccessor) this).getTextureMap()::putIfAbsent);
			}
		}
	}

	@Override
	public Material getMaterial(String textureName) {
		if (textureName.charAt(0) == '#') {
			textureName = textureName.substring(1);
		}

		List<String> visitedTextureReferences = Lists.newArrayList();
		while (true) {
			Either<Material, String> either = findTexture(textureName);
			Optional<Material> optional = either.left();
			if (optional.isPresent()) {
				return optional.get();
			}

			textureName = either.right().orElse("");

			if (visitedTextureReferences.contains(textureName)) {
				SophisticatedStorage.LOGGER.warn("Unable to resolve texture due to reference chain {}->{} in {}", Joiner.on("->").join(visitedTextureReferences), textureName, name);
				return new Material(InventoryMenu.BLOCK_ATLAS, MissingTextureAtlasSprite.getLocation());
			}

			visitedTextureReferences.add(textureName);
		}
	}

	private Either<Material, String> findTexture(String textureName) {
		for (BlockModel blockmodel = this; blockmodel != null; blockmodel = blockmodel.parent) {
			Either<Material, String> either = ((BlockModelAccessor) blockmodel).getTextureMap().get(textureName);
			if (either != null) {
				return either;
			}
		}

		return Either.left(new Material(InventoryMenu.BLOCK_ATLAS, MissingTextureAtlasSprite.getLocation()));
	}
}
