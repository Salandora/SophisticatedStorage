package net.p3pp3rf1y.sophisticatedstorage.util.model;

import net.minecraft.resources.ResourceLocation;
import net.p3pp3rf1y.sophisticatedcore.util.model.ModelProperty;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelMaterial;

import java.util.Map;

public class ModelProperties extends net.p3pp3rf1y.sophisticatedcore.util.model.ModelProperties {
	public static final ModelProperty<Map<BarrelMaterial, ResourceLocation>> MATERIALS = new ModelProperty<>();
}
