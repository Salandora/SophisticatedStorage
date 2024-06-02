package net.p3pp3rf1y.sophisticatedstorage.compat.mkb;

import committee.nova.mkb.api.IKeyBinding;

import net.minecraft.client.KeyMapping;
import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;

import static net.p3pp3rf1y.sophisticatedstorage.client.ClientEventHandler.SORT_KEYBIND;

public class ModernKeyBindingCompat implements ICompat {
	@Override
	public void setup() {
		if (!IKeyBinding.class.isAssignableFrom(KeyMapping.class)) {
			return;
		}

		((IKeyBinding) SORT_KEYBIND).setKeyConflictContext(StorageGuiKeyConflictContext.INSTANCE);
	}
}
