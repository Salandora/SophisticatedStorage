package net.p3pp3rf1y.sophisticatedstorage.settings;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderInfo;
import net.p3pp3rf1y.sophisticatedcore.settings.ISettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsHandler;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.main.MainSettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class StorageSettingsHandler extends SettingsHandler {
	public static final String SOPHISTICATED_STORAGE_SETTINGS_PLAYER_TAG = "sophisticatedStorageSettings";

	static {
		ServerPlayerEvents.COPY_FROM.register(StorageSettingsHandler::onPlayerClone);
	}

	private static void onPlayerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
		CompoundTag oldData = oldPlayer.getSophisticatedCustomData();
		CompoundTag newData = newPlayer.getSophisticatedCustomData();

		if (oldData.contains(SOPHISTICATED_STORAGE_SETTINGS_PLAYER_TAG)) {
			//noinspection ConstantConditions
			newData.put(SOPHISTICATED_STORAGE_SETTINGS_PLAYER_TAG, oldData.get(SOPHISTICATED_STORAGE_SETTINGS_PLAYER_TAG));
		}
	}

	protected StorageSettingsHandler(CompoundTag contentsNbt, Runnable markContentsDirty, Supplier<InventoryHandler> inventoryHandlerSupplier, Supplier<RenderInfo> renderInfoSupplier) {
		super(contentsNbt, markContentsDirty, inventoryHandlerSupplier, renderInfoSupplier);
	}

	protected abstract int getNumberOfDisplayItems();

	@Override
	protected CompoundTag getSettingsNbtFromContentsNbt(CompoundTag contentsNbt) {
		return contentsNbt;
	}

	@Override
	protected void addItemDisplayCategory(Supplier<InventoryHandler> inventoryHandlerSupplier, Supplier<RenderInfo> renderInfoSupplier, CompoundTag settingsNbt) {
		addSettingsCategory(settingsNbt, ItemDisplaySettingsCategory.NAME, markContentsDirty, (categoryNbt, saveNbt) ->
				new ItemDisplaySettingsCategory(inventoryHandlerSupplier, renderInfoSupplier, categoryNbt, saveNbt, getNumberOfDisplayItems(), () -> getTypeCategory(MemorySettingsCategory.class)));
	}

	@Override
	public String getGlobalSettingsCategoryName() {
		return MainSettingsCategory.NAME;
	}

	@Override
	public ISettingsCategory<?> instantiateGlobalSettingsCategory(CompoundTag categoryNbt, Consumer<CompoundTag> saveNbt) {
		return new MainSettingsCategory<>(categoryNbt, saveNbt, SOPHISTICATED_STORAGE_SETTINGS_PLAYER_TAG);
	}

	@Override
	protected void saveCategoryNbt(CompoundTag settingsNbt, String categoryName, CompoundTag tag) {
		contentsNbt.put(categoryName, tag);
	}

	@Override
	public void reloadFrom(CompoundTag contentsNbt) {
		this.contentsNbt = contentsNbt;
		super.reloadFrom(contentsNbt);
	}
}
