package net.p3pp3rf1y.sophisticatedstorage.block;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface IMaterialHolder {
	void setMaterials(Map<BarrelMaterial, ResourceLocation> materials);

	Map<BarrelMaterial, ResourceLocation> getMaterials();

	boolean canHoldMaterials();
}
