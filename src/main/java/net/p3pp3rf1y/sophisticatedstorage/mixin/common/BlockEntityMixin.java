package net.p3pp3rf1y.sophisticatedstorage.mixin.common;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.extensions.CapabilityHelper;

@Mixin(BlockEntity.class)
public class BlockEntityMixin implements CapabilityHelper {
}
