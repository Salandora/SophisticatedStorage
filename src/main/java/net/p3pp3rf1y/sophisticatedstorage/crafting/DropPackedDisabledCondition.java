package net.p3pp3rf1y.sophisticatedstorage.crafting;

import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.minecraft.resources.ResourceLocation;
import net.p3pp3rf1y.sophisticatedstorage.Config;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;

public class DropPackedDisabledCondition implements ConditionJsonProvider {
	public static final ResourceLocation ID = SophisticatedStorage.getRL("drop_packed_disabled");

	@Override
	public ResourceLocation getConditionId() {
		return ID;
	}

	@Override
	public void writeParameters(JsonObject object) {
		// noop
	}

	public static boolean test(JsonObject context) {
		return Boolean.FALSE.equals(Config.COMMON.dropPacked.get());
	}
}
