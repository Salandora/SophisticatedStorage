package net.p3pp3rf1y.sophisticatedstorage.compat.jade;

import net.p3pp3rf1y.sophisticatedstorage.block.WoodStorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@SuppressWarnings("unused") //used by Jade's reflection
@WailaPlugin
public class StorageJadePlugin implements IWailaPlugin {
	@Override
	public void registerClient(IWailaClientRegistration registration) {
		ModBlocks.BLOCKS.values().forEach(block -> {
			if (block instanceof WoodStorageBlockBase) {
				registration.usePickedResult(block);
			}
		});
	}
}
