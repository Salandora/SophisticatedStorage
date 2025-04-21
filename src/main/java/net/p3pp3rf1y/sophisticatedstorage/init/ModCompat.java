package net.p3pp3rf1y.sophisticatedstorage.init;

import net.p3pp3rf1y.sophisticatedcore.compat.CompatInfo;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatModIds;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatRegistry;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.compat.chipped.ChippedCompat;
import net.p3pp3rf1y.sophisticatedstorage.compat.litematica.LitematicaCompat;
import net.p3pp3rf1y.sophisticatedstorage.compat.sb.SBCompat;
/*import net.p3pp3rf1y.sophisticatedstorage.compat.mkb.ModernKeyBindingCompat;
import net.p3pp3rf1y.sophisticatedstorage.compat.sodium.SodiumCompat;*/

public class ModCompat {
	private static final String SB_MOD_ID = "sophisticatedbackpacks";
	private ModCompat() {
	}

	//public static final String SODIUM = "sodium";
	public static final String MKB = "mkb";

	public static void register() {
		//CompatRegistry.registerCompat(new CompatInfo(CompatModIds.QUARK, null), () -> new QuarkCompat());
		CompatRegistry.getRegistry(SophisticatedStorage.MOD_ID).registerCompat(new CompatInfo(CompatModIds.CHIPPED, null), () -> new ChippedCompat());
		CompatRegistry.getRegistry(SophisticatedStorage.MOD_ID).registerCompat(new CompatInfo(CompatModIds.LITEMATICA, null), () -> new LitematicaCompat());
		/*CompatRegistry.registerCompat(new CompatInfo(SODIUM, CompatRegistry.fromSpec(">=0.4.9 <0.5")), () -> new SodiumCompat());
		CompatRegistry.registerCompat(new CompatInfo(MKB, null), () -> new ModernKeyBindingCompat());*/
		CompatRegistry.getRegistry(SophisticatedStorage.MOD_ID).registerCompat(new CompatInfo(SB_MOD_ID, null), () -> new SBCompat());
	}
}
