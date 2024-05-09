package net.p3pp3rf1y.sophisticatedstorage.init;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.fabricmc.loader.impl.util.version.VersionPredicateParser;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatModIds;
import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.compat.litematica.LitematicaCompat;
import net.p3pp3rf1y.sophisticatedstorage.compat.sodium.SodiumCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class ModCompat {
	private ModCompat() {}

	 private static final String SODIUM_MOD_ID = "sodium";

	private static final Map<CompatInfo, Supplier<Callable<ICompat>>> compatFactories = new HashMap<>();

	private static final Map<CompatInfo, ICompat> loadedCompats = new HashMap<>();

	static {
		// compatFactories.put(new CompatInfo(CompatModIds.QUARK, null), () -> QuarkCompat::new);
		// compatFactories.put(new CompatInfo(CompatModIds.CHIPPED, null), () -> ChippedCompat::new);
		compatFactories.put(new CompatInfo(CompatModIds.LITEMATICA, null), () -> LitematicaCompat::new);
		try {
			compatFactories.put(new CompatInfo(SODIUM_MOD_ID, VersionPredicateParser.parse(">=0.4.9 <0.5")), () -> SodiumCompat::new);
		}
		catch (VersionParsingException e) {
			throw new RuntimeException(e);
		}
	}

	public static void compatsSetup() {
		loadedCompats.values().forEach(ICompat::setup);
	}

	public static void initCompats() {
		for (Map.Entry<CompatInfo, Supplier<Callable<ICompat>>> entry : compatFactories.entrySet()) {
			if (entry.getKey().isLoaded()) {
				try {
					loadedCompats.put(entry.getKey(), entry.getValue().get().call());
				}
				catch (Exception e) {
					SophisticatedStorage.LOGGER.error("Error instantiating compatibility ", e);
				}
			}
		}

		loadedCompats.values().forEach(ICompat::init);
	}

	record CompatInfo(String modId, @Nullable VersionPredicate supportedVersionRange){
		public boolean isLoaded() {
			return FabricLoader.getInstance().getModContainer(modId())
					.map(container -> supportedVersionRange() == null || supportedVersionRange().test(container.getMetadata().getVersion()))
					.orElse(false);
		}
	}
}
