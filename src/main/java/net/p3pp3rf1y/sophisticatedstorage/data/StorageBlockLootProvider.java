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
		add(ModBlocks.BARREL, dropStorageWithContents(ModBlocks.BARREL_ITEM));
		add(ModBlocks.IRON_BARREL, dropStorageWithContents(ModBlocks.IRON_BARREL_ITEM));
		add(ModBlocks.GOLD_BARREL, dropStorageWithContents(ModBlocks.GOLD_BARREL_ITEM));
		add(ModBlocks.DIAMOND_BARREL, dropStorageWithContents(ModBlocks.DIAMOND_BARREL_ITEM));
		add(ModBlocks.NETHERITE_BARREL, dropStorageWithContents(ModBlocks.NETHERITE_BARREL_ITEM));
		add(ModBlocks.LIMITED_BARREL_1, dropStorageWithContents(ModBlocks.LIMITED_BARREL_1_ITEM));
		add(ModBlocks.LIMITED_IRON_BARREL_1, dropStorageWithContents(ModBlocks.LIMITED_IRON_BARREL_1_ITEM));
		add(ModBlocks.LIMITED_GOLD_BARREL_1, dropStorageWithContents(ModBlocks.LIMITED_GOLD_BARREL_1_ITEM));
		add(ModBlocks.LIMITED_DIAMOND_BARREL_1, dropStorageWithContents(ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM));
		add(ModBlocks.LIMITED_NETHERITE_BARREL_1, dropStorageWithContents(ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM));
		add(ModBlocks.LIMITED_BARREL_2, dropStorageWithContents(ModBlocks.LIMITED_BARREL_2_ITEM));
		add(ModBlocks.LIMITED_IRON_BARREL_2, dropStorageWithContents(ModBlocks.LIMITED_IRON_BARREL_2_ITEM));
		add(ModBlocks.LIMITED_GOLD_BARREL_2, dropStorageWithContents(ModBlocks.LIMITED_GOLD_BARREL_2_ITEM));
		add(ModBlocks.LIMITED_DIAMOND_BARREL_2, dropStorageWithContents(ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM));
		add(ModBlocks.LIMITED_NETHERITE_BARREL_2, dropStorageWithContents(ModBlocks.LIMITED_NETHERITE_BARREL_2_ITEM));
		add(ModBlocks.LIMITED_BARREL_3, dropStorageWithContents(ModBlocks.LIMITED_BARREL_3_ITEM));
		add(ModBlocks.LIMITED_IRON_BARREL_3, dropStorageWithContents(ModBlocks.LIMITED_IRON_BARREL_3_ITEM));
		add(ModBlocks.LIMITED_GOLD_BARREL_3, dropStorageWithContents(ModBlocks.LIMITED_GOLD_BARREL_3_ITEM));
		add(ModBlocks.LIMITED_DIAMOND_BARREL_3, dropStorageWithContents(ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM));
		add(ModBlocks.LIMITED_NETHERITE_BARREL_3, dropStorageWithContents(ModBlocks.LIMITED_NETHERITE_BARREL_3_ITEM));
		add(ModBlocks.LIMITED_BARREL_4, dropStorageWithContents(ModBlocks.LIMITED_BARREL_4_ITEM));
		add(ModBlocks.LIMITED_IRON_BARREL_4, dropStorageWithContents(ModBlocks.LIMITED_IRON_BARREL_4_ITEM));
		add(ModBlocks.LIMITED_GOLD_BARREL_4, dropStorageWithContents(ModBlocks.LIMITED_GOLD_BARREL_4_ITEM));
		add(ModBlocks.LIMITED_DIAMOND_BARREL_4, dropStorageWithContents(ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM));
		add(ModBlocks.LIMITED_NETHERITE_BARREL_4, dropStorageWithContents(ModBlocks.LIMITED_NETHERITE_BARREL_4_ITEM));
		add(ModBlocks.CHEST, dropStorageWithContents(ModBlocks.CHEST_ITEM));
		add(ModBlocks.IRON_CHEST, dropStorageWithContents(ModBlocks.IRON_CHEST_ITEM));
		add(ModBlocks.GOLD_CHEST, dropStorageWithContents(ModBlocks.GOLD_CHEST_ITEM));
		add(ModBlocks.DIAMOND_CHEST, dropStorageWithContents(ModBlocks.DIAMOND_CHEST_ITEM));
		add(ModBlocks.NETHERITE_CHEST, dropStorageWithContents(ModBlocks.NETHERITE_CHEST_ITEM));
		add(ModBlocks.SHULKER_BOX, dropStorageWithContents(ModBlocks.SHULKER_BOX_ITEM));
		add(ModBlocks.IRON_SHULKER_BOX, dropStorageWithContents(ModBlocks.IRON_SHULKER_BOX_ITEM));
		add(ModBlocks.GOLD_SHULKER_BOX, dropStorageWithContents(ModBlocks.GOLD_SHULKER_BOX_ITEM));
		add(ModBlocks.DIAMOND_SHULKER_BOX, dropStorageWithContents(ModBlocks.DIAMOND_SHULKER_BOX_ITEM));
		add(ModBlocks.NETHERITE_SHULKER_BOX, dropStorageWithContents(ModBlocks.NETHERITE_SHULKER_BOX_ITEM));

		add(ModBlocks.CONTROLLER, dropBlock(ModBlocks.CONTROLLER_ITEM));
		add(ModBlocks.STORAGE_LINK, dropBlock(ModBlocks.STORAGE_LINK_ITEM));
	}

	private static LootTable.Builder dropStorageWithContents(Item storageItem) {
		LootPool.Builder pool = LootPool.lootPool().name("main").setRolls(ConstantValue.exactly(1))
				.add(LootItem.lootTableItem(storageItem))
				.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
				.apply(CopyStorageDataFunction.builder());
		return LootTable.lootTable().withPool(pool);
	}

	public LootTable.Builder dropBlock(ItemLike pItem) {
		LootPool.Builder pool = applyExplosionCondition(pItem, LootPool.lootPool().name("main").setRolls(ConstantValue.exactly(1.0F))
				.add(LootItem.lootTableItem(pItem))
		);
		return LootTable.lootTable().withPool(pool);
	}
}
