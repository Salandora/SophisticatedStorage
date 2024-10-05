package net.p3pp3rf1y.sophisticatedstorage.mixin.common.accessor;

import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShulkerBoxBlockEntity.class)
public interface ShulkerBoxBlockEntityAccessor {
	@Accessor
	int getOpenCount();
}
