package net.p3pp3rf1y.sophisticatedstorage.compat.mkb;

import committee.nova.mkb.api.IKeyConflictContext;

import net.minecraft.client.Minecraft;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageScreen;

import static committee.nova.mkb.keybinding.KeyConflictContext.GUI;

public class StorageGuiKeyConflictContext implements IKeyConflictContext {
	public static final StorageGuiKeyConflictContext INSTANCE = new StorageGuiKeyConflictContext();

	@Override
	public boolean isActive() {
		return GUI.isActive() && Minecraft.getInstance().screen instanceof StorageScreen;
	}

	@Override
	public boolean conflicts(IKeyConflictContext other) {
		return this == other;
	}
}
