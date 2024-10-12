package net.p3pp3rf1y.sophisticatedstorage.compat.mkb;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;
import committee.nova.mkb.api.IKeyBinding;

import static net.p3pp3rf1y.sophisticatedstorage.client.ClientEventHandler.SORT_KEYBIND;
import static net.p3pp3rf1y.sophisticatedstorage.client.ClientEventHandler.tryCallSort;

public class ModernKeyBindingCompat implements ICompat {
	@Override
	public void setup() {
		if (!IKeyBinding.class.isAssignableFrom(KeyMapping.class)) {
			return;
		}

		((IKeyBinding) SORT_KEYBIND).setKeyConflictContext(StorageGuiKeyConflictContext.INSTANCE);

		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			ScreenKeyboardEvents.allowKeyPress(screen).register(ModernKeyBindingCompat::handleGuiKeyPress);
			ScreenMouseEvents.allowMouseClick(screen).register(ModernKeyBindingCompat::handleGuiMouseKeyPress);
		});
	}

	public static boolean handleGuiKeyPress(Screen screen, int keycode, int scancode, int modifiers) {
		InputConstants.Key key = InputConstants.getKey(keycode, scancode);
		return !((IKeyBinding) SORT_KEYBIND).isActiveAndMatches(key) || !tryCallSort(screen);
	}

	public static boolean handleGuiMouseKeyPress(Screen screen, double mouseX, double mouseY, int button) {
		InputConstants.Key key = InputConstants.Type.MOUSE.getOrCreate(button);
		return !((IKeyBinding) SORT_KEYBIND).isActiveAndMatches(key) || !tryCallSort(screen);
	}
}
