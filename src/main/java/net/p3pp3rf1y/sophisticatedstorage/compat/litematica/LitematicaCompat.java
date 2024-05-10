package net.p3pp3rf1y.sophisticatedstorage.compat.litematica;

import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundTag;
import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;
import net.p3pp3rf1y.sophisticatedcore.compat.litematica.network.LitematicaPacketHandler;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.p3pp3rf1y.sophisticatedcore.compat.litematica.LitematicaCompat.REQUEST_CONTENTS_CAPABILITY;

public class LitematicaCompat implements ICompat {
	public static void alwaysInit() {
		// Register this on the SophisticatedCore channel
		LitematicaPacketHandler.registerS2CMessage(ItemStorageContentsMessage.class, ItemStorageContentsMessage::new);

		REQUEST_CONTENTS_CAPABILITY.registerForItems((stack, context) -> LitematicaCompat::createItemStorageContentsMessage,
				ModBlocks.BARREL_ITEM, ModBlocks.IRON_BARREL_ITEM, ModBlocks.GOLD_BARREL_ITEM, ModBlocks.DIAMOND_BARREL_ITEM, ModBlocks.NETHERITE_BARREL_ITEM,
				ModBlocks.LIMITED_BARREL_1_ITEM, ModBlocks.LIMITED_IRON_BARREL_1_ITEM, ModBlocks.LIMITED_GOLD_BARREL_1_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM,
				ModBlocks.LIMITED_BARREL_2_ITEM, ModBlocks.LIMITED_IRON_BARREL_2_ITEM, ModBlocks.LIMITED_GOLD_BARREL_2_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_2_ITEM,
				ModBlocks.LIMITED_BARREL_3_ITEM, ModBlocks.LIMITED_IRON_BARREL_3_ITEM, ModBlocks.LIMITED_GOLD_BARREL_3_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_3_ITEM,
				ModBlocks.LIMITED_BARREL_4_ITEM, ModBlocks.LIMITED_IRON_BARREL_4_ITEM, ModBlocks.LIMITED_GOLD_BARREL_4_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_4_ITEM,
				ModBlocks.CHEST_ITEM, ModBlocks.IRON_CHEST_ITEM, ModBlocks.GOLD_CHEST_ITEM, ModBlocks.DIAMOND_CHEST_ITEM, ModBlocks.NETHERITE_CHEST_ITEM,
				ModBlocks.SHULKER_BOX_ITEM, ModBlocks.IRON_SHULKER_BOX_ITEM, ModBlocks.GOLD_SHULKER_BOX_ITEM, ModBlocks.DIAMOND_SHULKER_BOX_ITEM, ModBlocks.NETHERITE_SHULKER_BOX_ITEM);
	}

	public static ItemStorageContentsMessage createItemStorageContentsMessage(List<UUID> uuids) {
		Map<UUID, CompoundTag> contents = Maps.toMap(uuids, ItemContentsStorage.get()::getOrCreateStorageContents);
		return new ItemStorageContentsMessage(contents);
	}

	@Override
	public void setup() {
	}
}
