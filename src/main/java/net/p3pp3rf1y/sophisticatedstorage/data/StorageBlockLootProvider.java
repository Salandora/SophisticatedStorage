package net.p3pp3rf1y.sophisticatedstorage.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;


public class StorageBlockLootProvider extends FabricBlockLootTableProvider {
	public StorageBlockLootProvider(FabricDataGenerator output) {
		super(output);
	}

	@Override
	protected void generateBlockLootTables() {
		add(ModBlocks.BARREL, getStorage(ModBlocks.BARREL_ITEM));
		add(ModBlocks.COPPER_BARREL, getStorage(ModBlocks.COPPER_BARREL_ITEM));
		add(ModBlocks.IRON_BARREL, getStorage(ModBlocks.IRON_BARREL_ITEM));
		add(ModBlocks.GOLD_BARREL, getStorage(ModBlocks.GOLD_BARREL_ITEM));
		add(ModBlocks.DIAMOND_BARREL, getStorage(ModBlocks.DIAMOND_BARREL_ITEM));
		add(ModBlocks.NETHERITE_BARREL, getStorage(ModBlocks.NETHERITE_BARREL_ITEM));
		add(ModBlocks.LIMITED_BARREL_1, getStorage(ModBlocks.LIMITED_BARREL_1_ITEM));
		add(ModBlocks.LIMITED_COPPER_BARREL_1, getStorage(ModBlocks.LIMITED_COPPER_BARREL_1_ITEM));
		add(ModBlocks.LIMITED_IRON_BARREL_1, getStorage(ModBlocks.LIMITED_IRON_BARREL_1_ITEM));
		add(ModBlocks.LIMITED_GOLD_BARREL_1, getStorage(ModBlocks.LIMITED_GOLD_BARREL_1_ITEM));
		add(ModBlocks.LIMITED_DIAMOND_BARREL_1, getStorage(ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM));
		add(ModBlocks.LIMITED_NETHERITE_BARREL_1, getStorage(ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM));
		add(ModBlocks.LIMITED_BARREL_2, getStorage(ModBlocks.LIMITED_BARREL_2_ITEM));
		add(ModBlocks.LIMITED_COPPER_BARREL_2, getStorage(ModBlocks.LIMITED_COPPER_BARREL_2_ITEM));
		add(ModBlocks.LIMITED_IRON_BARREL_2, getStorage(ModBlocks.LIMITED_IRON_BARREL_2_ITEM));
		add(ModBlocks.LIMITED_GOLD_BARREL_2, getStorage(ModBlocks.LIMITED_GOLD_BARREL_2_ITEM));
		add(ModBlocks.LIMITED_DIAMOND_BARREL_2, getStorage(ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM));
		add(ModBlocks.LIMITED_NETHERITE_BARREL_2, getStorage(ModBlocks.LIMITED_NETHERITE_BARREL_2_ITEM));
		add(ModBlocks.LIMITED_BARREL_3, getStorage(ModBlocks.LIMITED_BARREL_3_ITEM));
		add(ModBlocks.LIMITED_COPPER_BARREL_3, getStorage(ModBlocks.LIMITED_COPPER_BARREL_3_ITEM));
		add(ModBlocks.LIMITED_IRON_BARREL_3, getStorage(ModBlocks.LIMITED_IRON_BARREL_3_ITEM));
		add(ModBlocks.LIMITED_GOLD_BARREL_3, getStorage(ModBlocks.LIMITED_GOLD_BARREL_3_ITEM));
		add(ModBlocks.LIMITED_DIAMOND_BARREL_3, getStorage(ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM));
		add(ModBlocks.LIMITED_NETHERITE_BARREL_3, getStorage(ModBlocks.LIMITED_NETHERITE_BARREL_3_ITEM));
		add(ModBlocks.LIMITED_BARREL_4, getStorage(ModBlocks.LIMITED_BARREL_4_ITEM));
		add(ModBlocks.LIMITED_COPPER_BARREL_4, getStorage(ModBlocks.LIMITED_COPPER_BARREL_4_ITEM));
		add(ModBlocks.LIMITED_IRON_BARREL_4, getStorage(ModBlocks.LIMITED_IRON_BARREL_4_ITEM));
		add(ModBlocks.LIMITED_GOLD_BARREL_4, getStorage(ModBlocks.LIMITED_GOLD_BARREL_4_ITEM));
		add(ModBlocks.LIMITED_DIAMOND_BARREL_4, getStorage(ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM));
		add(ModBlocks.LIMITED_NETHERITE_BARREL_4, getStorage(ModBlocks.LIMITED_NETHERITE_BARREL_4_ITEM));
		add(ModBlocks.CHEST, getStorage(ModBlocks.CHEST_ITEM));
		add(ModBlocks.COPPER_CHEST, getStorage(ModBlocks.COPPER_CHEST_ITEM));
		add(ModBlocks.IRON_CHEST, getStorage(ModBlocks.IRON_CHEST_ITEM));
		add(ModBlocks.GOLD_CHEST, getStorage(ModBlocks.GOLD_CHEST_ITEM));
		add(ModBlocks.DIAMOND_CHEST, getStorage(ModBlocks.DIAMOND_CHEST_ITEM));
		add(ModBlocks.NETHERITE_CHEST, getStorage(ModBlocks.NETHERITE_CHEST_ITEM));
		add(ModBlocks.SHULKER_BOX, getStorage(ModBlocks.SHULKER_BOX_ITEM));
		add(ModBlocks.COPPER_SHULKER_BOX, getStorage(ModBlocks.COPPER_SHULKER_BOX_ITEM));
		add(ModBlocks.IRON_SHULKER_BOX, getStorage(ModBlocks.IRON_SHULKER_BOX_ITEM));
		add(ModBlocks.GOLD_SHULKER_BOX, getStorage(ModBlocks.GOLD_SHULKER_BOX_ITEM));
		add(ModBlocks.DIAMOND_SHULKER_BOX, getStorage(ModBlocks.DIAMOND_SHULKER_BOX_ITEM));
		add(ModBlocks.NETHERITE_SHULKER_BOX, getStorage(ModBlocks.NETHERITE_SHULKER_BOX_ITEM));

		add(ModBlocks.CONTROLLER, createSingleItemTable(ModBlocks.CONTROLLER_ITEM));
		add(ModBlocks.STORAGE_LINK, createSingleItemTable(ModBlocks.STORAGE_LINK_ITEM));
	}

	@Override
	public String getName() {
		return "SophisticatedStorage block loot tables";
	}

	private static LootTable.Builder getStorage(Item storageItem) {
		LootPool.Builder pool = LootPool.lootPool().name("main").setRolls(ConstantValue.exactly(1))
				.add(LootItem.lootTableItem(storageItem))
				.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
				.apply(CopyStorageDataFunction.builder());
		return LootTable.lootTable().withPool(pool);
	}

	public static LootTable.Builder createSingleItemTable(ItemLike pItem) {
		LootPool.Builder pool = applyExplosionCondition(pItem, LootPool.lootPool().name("main").setRolls(ConstantValue.exactly(1.0F))
				.add(LootItem.lootTableItem(pItem))
		);
		return LootTable.lootTable().withPool(pool);
	}
}
