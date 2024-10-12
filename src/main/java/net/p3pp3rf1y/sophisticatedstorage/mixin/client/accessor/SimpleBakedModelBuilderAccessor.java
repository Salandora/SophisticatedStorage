package net.p3pp3rf1y.sophisticatedstorage.mixin.client.accessor;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.SimpleBakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SimpleBakedModel.Builder.class)
public interface SimpleBakedModelBuilderAccessor {
	@Invoker("<init>")
	static SimpleBakedModel.Builder create(boolean hasAmbientOcclusion, boolean usesBlockLight, boolean isGui3d, ItemTransforms transforms, ItemOverrides overrides) {
		return null;
	}
}
