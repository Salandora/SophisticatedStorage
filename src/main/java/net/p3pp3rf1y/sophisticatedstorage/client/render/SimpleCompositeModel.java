package net.p3pp3rf1y.sophisticatedstorage.client.render;

import com.github.salandora.sophisticatedlibrary.model.api.v1.loading.IGeometryBakingContext;
import com.github.salandora.sophisticatedlibrary.model.api.v1.loading.IGeometryLoader;
import com.github.salandora.sophisticatedlibrary.model.api.v1.loading.IUnbakedGeometry;
import com.github.salandora.sophisticatedlibrary.model.api.v1.models.BlockModelWrapper;
import com.github.salandora.sophisticatedlibrary.model.api.v1.util.ModelData;
import com.github.salandora.sophisticatedlibrary.model.api.v1.util.SimpleModelState;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Transformation;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleCompositeModel implements IUnbakedGeometry {
	private static final String PARTICLE_MATERIAL = "particle";
	private final ImmutableMap<String, BlockModel> children;

	private SimpleCompositeModel(ImmutableMap<String, BlockModel> children) {
		this.children = children;
	}

	@Override
	public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
		Material particleLocation = context.getMaterial(PARTICLE_MATERIAL);
		TextureAtlasSprite particle = spriteGetter.apply(particleLocation);

		var rootTransform = context.getRootTransform();
		if (!rootTransform.equals(Transformation.identity())) {
			modelState = new SimpleModelState(modelState.getRotation().compose(rootTransform), modelState.isUvLocked());
		}

		var bakedPartsBuilder = ImmutableMap.<String, BakedModel>builder();
		for (var entry : children.entrySet()) {
			var name = entry.getKey();
			// TODO: Component Visible
			/*if (!context.isComponentVisible(name, true)) {
				continue;
			}*/
			var model = entry.getValue();
 			bakedPartsBuilder.put(name, model.bake(baker, model, spriteGetter, modelState, true));
		}
		var bakedParts = bakedPartsBuilder.build();

		return new Baked(context.isGui3d(), context.useBlockLight(), context.useAmbientOcclusion(), particle, context.getTransforms(), overrides, bakedParts);
	}

	@SuppressWarnings("java:S5803") //need to access textureMap here to get textures
	public Map<String, Either<Material, String>> getTextures() {
		HashMap<String, Either<Material, String>> textures = new HashMap<>();
		children.values().forEach(childModel -> {
			childModel.textureMap.forEach(textures::putIfAbsent);
			if (childModel instanceof BlockModelWrapper wrapper && wrapper.getWrapper() instanceof SimpleCompositeModel compositeModel) {
				compositeModel.getTextures().forEach(textures::putIfAbsent);
			} else if (childModel.parent != null) {
				childModel.parent.textureMap.forEach(textures::putIfAbsent);
			}
		});

		return textures;
	}

	@Override
	public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
		children.values().forEach(childModel -> childModel.resolveParents(modelGetter));
	}

	@SuppressWarnings("java:S1874") //need to get elements from the model so actually need to call getElements here
	public List<BlockElement> getElements() {
		List<BlockElement> elements = new ArrayList<>();

		children.forEach((name, model) -> {
			elements.addAll(model.getElements());
			if (model instanceof BlockModelWrapper wrapper && wrapper.getWrapper() instanceof SimpleCompositeModel compositeModel) {
				elements.addAll(compositeModel.getElements());
			}
		});

		return elements;
	}

	/*@Override
	public Set<String> getConfigurableComponentNames() {
		return children.keySet();
	}*/

	public static class Baked implements BakedModel, FabricBakedModel {
		private final boolean isAmbientOcclusion;
		private final boolean isGui3d;
		private final boolean isSideLit;
		private final TextureAtlasSprite particle;
		private final ItemOverrides overrides;
		private final ItemTransforms transforms;
		private final ImmutableMap<String, BakedModel> children;

		public Baked(boolean isGui3d, boolean isSideLit, boolean isAmbientOcclusion, TextureAtlasSprite particle, ItemTransforms transforms, ItemOverrides overrides, ImmutableMap<String, BakedModel> children) {
			this.children = children;
			this.isAmbientOcclusion = isAmbientOcclusion;
			this.isGui3d = isGui3d;
			this.isSideLit = isSideLit;
			this.particle = particle;
			this.overrides = overrides;
			this.transforms = transforms;
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

		@NotNull
		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
			List<BakedQuad> quadLists = new ArrayList<>();
			for (Map.Entry<String, BakedModel> entry : children.entrySet()) {
				quadLists.addAll(entry.getValue().getQuads(state, side, rand));
			}
			return Collections.unmodifiableList(quadLists);
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

		@SuppressWarnings({"java:S1874", "deprecation"})
		// need to override getTransforms not just call the non deprecated version here
		@Override
		public ItemTransforms getTransforms() {
			return transforms;
		}
	}

	@SuppressWarnings("java:S6548") // singleton implementation is good here
	public static final class Loader implements IGeometryLoader<SimpleCompositeModel> {
		public static final Loader INSTANCE = new Loader();

		private Loader() {
		}

		@Override
		public SimpleCompositeModel read(JsonObject jsonObject) {
			ImmutableMap.Builder<String, BlockModel> childrenBuilder = ImmutableMap.builder();
			readChildren(jsonObject, childrenBuilder);

			var children = childrenBuilder.build();
			if (children.isEmpty()) {
				throw new JsonParseException("Composite model requires a \"parts\" element with at least one element.");
			}

			return new SimpleCompositeModel(children);
		}

		private void readChildren(JsonObject jsonObject, ImmutableMap.Builder<String, BlockModel> children) {
			if (!jsonObject.has("parts")) {
				return;
			}
			var childrenJsonObject = jsonObject.getAsJsonObject("parts");
			for (Map.Entry<String, JsonElement> entry : childrenJsonObject.entrySet()) {
				// TODO: Can we do this better?
				children.put(entry.getKey(), new BlockModelWrapper(BlockModel.fromString(entry.getValue().toString())));
			}
		}
	}
}
