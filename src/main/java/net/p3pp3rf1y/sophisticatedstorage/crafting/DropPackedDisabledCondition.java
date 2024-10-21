package net.p3pp3rf1y.sophisticatedstorage.crafting;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.core.HolderLookup;
import net.p3pp3rf1y.sophisticatedstorage.Config;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import org.jetbrains.annotations.Nullable;

public class DropPackedDisabledCondition implements ResourceCondition {
	private static final DropPackedDisabledCondition INSTANCE = new DropPackedDisabledCondition();
	public static final MapCodec<DropPackedDisabledCondition> CODEC = MapCodec.unit(INSTANCE).stable();

	@Override
	public boolean test(HolderLookup.@Nullable Provider registryLookup) {
		return Boolean.FALSE.equals(Config.COMMON.dropPacked.get());
	}

	@Override
	public ResourceConditionType<DropPackedDisabledCondition> getType() {
		return ModItems.DROP_PACKED_DISABLED_CONDITION;
	}
}
