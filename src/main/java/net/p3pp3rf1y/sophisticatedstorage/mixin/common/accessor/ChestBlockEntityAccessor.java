package net.p3pp3rf1y.sophisticatedstorage.mixin.common.accessor;

import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChestBlockEntity.class)
public interface ChestBlockEntityAccessor {
	@Accessor
	ContainerOpenersCounter getOpenersCounter();
}
