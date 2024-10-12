package net.p3pp3rf1y.sophisticatedstorage.client.gui;

import com.google.common.collect.ImmutableMap;
import net.p3pp3rf1y.sophisticatedcore.client.gui.Tab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsContainerBase;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsTab;
import net.p3pp3rf1y.sophisticatedcore.settings.StorageSettingsTabControlBase;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsTab;
import net.p3pp3rf1y.sophisticatedcore.settings.main.MainSettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsTab;
import net.p3pp3rf1y.sophisticatedcore.settings.nosort.NoSortSettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.nosort.NoSortSettingsTab;

import java.util.Map;

public class StorageSettingsTabControl extends StorageSettingsTabControlBase {
	private static final Map<String, ISettingsTabFactory<?, ?>> SETTINGS_TAB_FACTORIES;

	static {
		ImmutableMap.Builder<String, ISettingsTabFactory<?, ?>> builder = new ImmutableMap.Builder<>();
		addFactory(builder, MainSettingsCategory.NAME, StorageMainSettingsTab::new);
		addFactory(builder, NoSortSettingsCategory.NAME, NoSortSettingsTab::new);
		addFactory(builder, MemorySettingsCategory.NAME, MemorySettingsTab::new);
		addFactory(builder, ItemDisplaySettingsCategory.NAME, ItemDisplaySettingsTab::new);
		SETTINGS_TAB_FACTORIES = builder.build();
	}

	protected StorageSettingsTabControl(StorageSettingsScreen screen, Position position) {
		super(screen, position);
	}

	@Override
	protected Tab instantiateReturnBackTab() {
		return new BackToStorageTab(new Position(x, getTopY()), screen.getMenu().getBlockPosition());
	}

	@Override
	protected <C extends SettingsContainerBase<?>, T extends SettingsTab<C>> ISettingsTabFactory<C, T> getSettingsTabFactory(String name) {
		//noinspection unchecked
		return (ISettingsTabFactory<C, T>) SETTINGS_TAB_FACTORIES.get(name);
	}
}
