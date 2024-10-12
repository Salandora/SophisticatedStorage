package net.p3pp3rf1y.sophisticatedstorage;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.p3pp3rf1y.sophisticatedstorage.init.*;

public class SophisticatedStoragePreLaunch implements PreLaunchEntrypoint {
	@Override
	public void onPreLaunch() {
		ModCompat.register();
	}
}
