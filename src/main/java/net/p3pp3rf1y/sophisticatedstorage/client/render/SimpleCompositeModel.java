package net.p3pp3rf1y.sophisticatedstorage.client.render;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Transformation;

import io.github.fabricators_of_create.porting_lib.models.geometry.IGeometryLoader;
import io.github.fabricators_of_create.porting_lib.models.geometry.IUnbakedGeometry;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedstorage.mixin.client.accessor.BlockModelAccessor;
import net.p3pp3rf1y.sophisticatedstorage.mixin.client.accessor.SimpleBakedModelBuilderAccessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

public class SimpleCompositeModel implements IUnbakedGeometry<SimpleCompositeModel> {

	private static final String PARTICLE_MATERIAL = "particle";
	private final ImmutableMap<String, BlockModel> children;

	private SimpleCompositeModel(ImmutableMap<String, BlockModel> children) {
		this.children = children;
	}

	@Override
	public BakedModel bake(BlockModel context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation, boolean isGui3d) {
		Material particleLocation = context.getMaterial(PARTICLE_MATERIAL);
		TextureAtlasSprite particle = spriteGetter.apply(particleLocation);

		var rootTransform = context.getRootTransform();
		if (!rootTransform.equals(Transformation.identity())) {
			ModelState finalModelState = modelState;
			modelState = new ModelState() {
				@Override
				public Transformation getRotation() {
					return finalModelState.getRotation().compose(rootTransform);
				}

				@Override
				public boolean isUvLocked() {
					return finalModelState.isUvLocked();
				}
			};
		}

		var bakedPartsBuilder = ImmutableMap.<String, BakedModel>builder();
		for (var entry : children.entrySet()) {
			var name = entry.getKey();
			if (!context.isComponentVisible(name, true)) {
				continue;
			}
			var model = entry.getValue();
			bakedPartsBuilder.put(name, model.bake(baker, model, spriteGetter, modelState, modelLocation, true));
		}
		var bakedParts = bakedPartsBuilder.build();

		var itemPassesBuilder = ImmutableList.<BakedModel>builder();

		return new Baked(isGui3d, context.getGuiLight().lightLikeBlock(), context.hasAmbientOcclusion(), particle, context.getTransforms(), overrides, bakedParts, itemPassesBuilder.build());
	}

	@SuppressWarnings("java:S5803") //need to access textureMap here to get textures
	public Map<String, Either<Material, String>> getTextures() {
		HashMap<String, Either<Material, String>> textures = new HashMap<>();
		children.values().forEach(childModel -> {
			((BlockModelAccessor) childModel).getTextureMap().forEach(textures::putIfAbsent);
			if (childModel.getCustomGeometry() instanceof SimpleCompositeModel compositeModel) {
				compositeModel.getTextures().forEach(textures::putIfAbsent);
			} else if (childModel.parent != null) {
				((BlockModelAccessor) childModel.parent).getTextureMap().forEach(textures::putIfAbsent);
			}
		});

		return textures;
	}

	@Override
	public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, BlockModel context) {
		children.values().forEach(childModel -> childModel.resolveParents(modelGetter));
	}

	@SuppressWarnings("java:S1874") //need to get elements from the model so actually need to call getElements here
	public List<BlockElement> getElements() {
		List<BlockElement> elements = new ArrayList<>();

		children.forEach((name, model) -> {
			elements.addAll(model.getElements());
			if (model.getCustomGeometry() instanceof SimpleCompositeModel compositeModel) {
				elements.addAll(compositeModel.getElements());
			}
		});

		return elements;
	}

	@Override
	public Set<String> getConfigurableComponentNames() {
		return children.keySet();
	}

	public static class Baked implements BakedModel, FabricBakedModel {
		private final boolean isAmbientOcclusion;
		private final boolean isGui3d;
		private final boolean isSideLit;
		private final TextureAtlasSprite particle;
		private final ItemOverrides overrides;
		private final ItemTransforms transforms;
		private final ImmutableMap<String, BakedModel> children;

		public Baked(boolean isGui3d, boolean isSideLit, boolean isAmbientOcclusion, TextureAtlasSprite particle, ItemTransforms transforms, ItemOverrides overrides, ImmutableMap<String, BakedModel> children, ImmutableList<BakedModel> itemPasses) {
			this.children = children;
			this.isAmbientOcclusion = isAmbientOcclusion;
			this.isGui3d = isGui3d;
			this.isSideLit = isSideLit;
			this.particle = particle;
			this.overrides = overrides;
			this.transforms = transforms;
		}

		@NotNull
		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
			List<BakedQuad> quadLists = new ArrayList<>();
			for (Map.Entry<String, BakedModel> entry : children.entrySet()) {
				quadLists.addAll(entry.getValue().getQuads(state, side, rand));
			}
			return quadLists;
		}

		@Override
		public boolean isVanillaAdapter() {
			return false;
		}

		@Override
		public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
			for (Map.Entry<String, BakedModel> entry : children.entrySet()) {
				entry.getValue().emitBlockQuads(blockView, state, pos, randomSupplier, context);
			}
		}

		@Override
		public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
			for (Map.Entry<String, BakedModel> entry : children.entrySet()) {
				entry.getValue().emitItemQuads(stack, randomSupplier, context);
			}
		}

		@Override
		public boolean useAmbientOcclusion() {
			return isAmbientOcclusion;
		}

		@Override
		public boolean isGui3d() {
			return isGui3d;
		}

		@Override
		public boolean usesBlockLight() {
			return isSideLit;
		}

		@Override
		public boolean isCustomRenderer() {
			return false;
		}

		@Override
		public TextureAtlasSprite getParticleIcon() {
			return particle;
		}

		@Override
		public ItemOverrides getOverrides() {
			return overrides;
		}

		@SuppressWarnings({"java:S1874"})
		// need to override getTransforms not just call the non deprecated version here
		@Override
		public ItemTransforms getTransforms() {
			return transforms;
		}

		public static Builder builder(BlockModel owner, boolean isGui3d, TextureAtlasSprite particle, ItemOverrides overrides, ItemTransforms cameraTransforms) {
			return builder(owner.hasAmbientOcclusion(), isGui3d, owner.getGuiLight().lightLikeBlock(), particle, overrides, cameraTransforms);
		}

		public static Builder builder(boolean isAmbientOcclusion, boolean isGui3d, boolean isSideLit, TextureAtlasSprite particle, ItemOverrides overrides, ItemTransforms cameraTransforms) {
			return new Builder(isAmbientOcclusion, isGui3d, isSideLit, particle, overrides, cameraTransforms);
		}

		public static class Builder {
			private final boolean isAmbientOcclusion;
			private final boolean isGui3d;
			private final boolean isSideLit;
			private final List<BakedModel> children = new ArrayList<>();
			private final List<BakedQuad> quads = new ArrayList<>();
			private final ItemOverrides overrides;
			private final ItemTransforms transforms;
			private TextureAtlasSprite particle;

			private Builder(boolean isAmbientOcclusion, boolean isGui3d, boolean isSideLit, TextureAtlasSprite particle, ItemOverrides overrides, ItemTransforms transforms) {
				this.isAmbientOcclusion = isAmbientOcclusion;
				this.isGui3d = isGui3d;
				this.isSideLit = isSideLit;
				this.particle = particle;
				this.overrides = overrides;
				this.transforms = transforms;
			}

			public void addLayer(BakedModel model) {
				flushQuads();
				children.add(model);
			}

			private void addLayer(List<BakedQuad> quads) {
				var modelBuilder = SimpleBakedModelBuilderAccessor.create(isAmbientOcclusion, isSideLit, isGui3d, transforms, overrides).particle(particle);
				quads.forEach(modelBuilder::addUnculledFace);
				children.add(modelBuilder.build());
			}

			private void flushQuads() {
				if (!quads.isEmpty()) {
					addLayer(quads);
					quads.clear();
				}
			}

			public Builder setParticle(TextureAtlasSprite particleSprite) {
				this.particle = particleSprite;
				return this;
			}

			public Builder addQuads(BakedQuad... quadsToAdd) {
				flushQuads();
				Collections.addAll(quads, quadsToAdd);
				return this;
			}

			public Builder addQuads(Collection<BakedQuad> quadsToAdd) {
				flushQuads();
				quads.addAll(quadsToAdd);
				return this;
			}

			public BakedModel build() {
				if (!quads.isEmpty()) {
					addLayer(quads);
				}

				var childrenBuilder = ImmutableMap.<String, BakedModel>builder();
				var itemPassesBuilder = ImmutableList.<BakedModel>builder();
				int i = 0;
				for (var model : this.children) {
					childrenBuilder.put("model_" + (i++), model);
					itemPassesBuilder.add(model);
				}
				return new SimpleCompositeModel.Baked(isGui3d, isSideLit, isAmbientOcclusion, particle, transforms, overrides, childrenBuilder.build(), itemPassesBuilder.build());
			}
		}
	}

	@SuppressWarnings("java:S6548") // singleton implementation is good here
	public static final class Loader implements IGeometryLoader<SimpleCompositeModel> {
		public static final Loader INSTANCE = new Loader();

		private Loader() {
		}

		@Override
		public SimpleCompositeModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {

			ImmutableMap.Builder<String, BlockModel> childrenBuilder = ImmutableMap.builder();
			readChildren(jsonObject, deserializationContext, childrenBuilder);

			var children = childrenBuilder.build();
			if (children.isEmpty()) {
				throw new JsonParseException("Composite model requires a \"parts\" element with at least one element.");
			}

			return new SimpleCompositeModel(children);
		}

		private void readChildren(JsonObject jsonObject, JsonDeserializationContext deserializationContext, ImmutableMap.Builder<String, BlockModel> children) {
			if (!jsonObject.has("parts")) {
				return;
			}
			var childrenJsonObject = jsonObject.getAsJsonObject("parts");
			for (Map.Entry<String, JsonElement> entry : childrenJsonObject.entrySet()) {
				children.put(entry.getKey(), deserializationContext.deserialize(entry.getValue(), BlockModel.class));
			}
		}
	}
}
