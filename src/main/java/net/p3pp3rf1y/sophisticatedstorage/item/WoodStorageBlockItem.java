package net.p3pp3rf1y.sophisticatedstorage.item;

import com.google.common.collect.MapMaker;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.porting_lib.base.util.LazyOptional;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TranslationHelper;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.common.CapabilityStorageWrapper;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

public class WoodStorageBlockItem extends StorageBlockItem {
	public static final String WOOD_TYPE_TAG = "woodType";
	public static final String PACKED_TAG = "packed";

	public WoodStorageBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	public static void setPacked(ItemStack storageStack, boolean packed) {
		storageStack.getOrCreateTag().putBoolean(PACKED_TAG, packed);
	}

	public static boolean isPacked(ItemStack storageStack) {
		return NBTHelper.getBoolean(storageStack, PACKED_TAG).orElse(false);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		if (isPacked(stack)) {
			if (flagIn == TooltipFlag.ADVANCED) {
				CapabilityStorageWrapper.get(stack).flatMap(IStorageWrapper::getContentsUuid)
						.ifPresent(uuid -> tooltip.add(Component.literal("UUID: " + uuid).withStyle(ChatFormatting.DARK_GRAY)));
			}
			if (!Screen.hasShiftDown()) {
				tooltip.add(Component.translatable(
						TranslationHelper.INSTANCE.translItemTooltip("storage") + ".press_for_contents",
						Component.translatable(TranslationHelper.INSTANCE.translItemTooltip("storage") + ".shift").withStyle(ChatFormatting.AQUA)
				).withStyle(ChatFormatting.GRAY));
			}
		}
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		if (!isPacked(stack)) {
			return Optional.empty();
		}

		TooltipComponent ret = null;
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			Minecraft mc = Minecraft.getInstance();
			if (Screen.hasShiftDown() || (mc.player != null && !mc.player.containerMenu.getCarried().isEmpty())) {
				ret = new StorageContentsTooltip(stack);
			}
		}
		return Optional.ofNullable(ret);
	}

	@Override
	public void setMainColor(ItemStack storageStack, int mainColor) {
		if (StorageBlockItem.getAccentColorFromStack(storageStack).isPresent()) {
			removeWoodType(storageStack);
		}
		super.setMainColor(storageStack, mainColor);
	}

	@Override
	public void setAccentColor(ItemStack storageStack, int accentColor) {
		if (StorageBlockItem.getMainColorFromStack(storageStack).isPresent()) {
			removeWoodType(storageStack);
		}
		super.setAccentColor(storageStack, accentColor);
	}

	private void removeWoodType(ItemStack storageStack) {
		storageStack.getOrCreateTag().remove(WoodStorageBlockItem.WOOD_TYPE_TAG);
	}

	public static Optional<WoodType> getWoodType(ItemStack storageStack) {
		return NBTHelper.getString(storageStack, WOOD_TYPE_TAG)
				.flatMap(woodType -> WoodType.values().filter(wt -> wt.name().equals(woodType)).findFirst());
	}

	public static ItemApiLookup.ItemApiProvider<LazyOptional<StorageWrapper>, Void> initCapabilities() {
		return new ItemApiLookup.ItemApiProvider<>() {
			final Map<ItemStack, StorageWrapper> wrapperMap = new MapMaker().weakKeys().weakValues().makeMap();

			@Override
			public LazyOptional<StorageWrapper> find(ItemStack stack, Void context) {
				if (stack.getCount() == 1) {
					return LazyOptional.of(() -> wrapperMap.computeIfAbsent(stack, this::initWrapper)).cast();
				}

				return LazyOptional.empty();
			}

			private StorageWrapper initWrapper(ItemStack stack) {
				UUID uuid = NBTHelper.getUniqueId(stack, "uuid").orElse(null);
				StorageWrapper storageWrapper = new StackStorageWrapper(stack) {
					@Override
					public String getStorageType() {
						return "wood_storage"; //isn't really relevant because wooden storage can't have its gui open when in item form
					}

					@Override
					public Component getDisplayName() {
						return Component.empty(); //isn't really relevant because wooden storage can't have its gui open when in item form
					}

					@Override
					protected boolean isAllowedInStorage(ItemStack stack) {
						return false;
					}
				};
				if (uuid != null) {
					CompoundTag compoundtag = ItemContentsStorage.get().getOrCreateStorageContents(uuid).getCompound(StorageBlockEntity.STORAGE_WRAPPER_TAG);
					storageWrapper.load(compoundtag);
					storageWrapper.setContentsUuid(uuid); //setting here because client side the uuid isn't in contentsnbt before this data is synced from server and it would create a new one otherwise
				}
				return storageWrapper;
			}
		};
	}

	public static ItemStack setWoodType(ItemStack storageStack, WoodType woodType) {
		storageStack.getOrCreateTag().putString(WOOD_TYPE_TAG, woodType.name());
		return storageStack;
	}

	@Override
	public Component getName(ItemStack stack) {
		return getDisplayName(getDescriptionId(), getWoodType(stack).orElse(null));
	}

	public static Component getDisplayName(String descriptionId, @Nullable WoodType woodType) {
		if (woodType == null) {
			return Component.translatable(descriptionId, "", "");
		}
		return Component.translatable(descriptionId, Component.translatable("wood_name.sophisticatedstorage." + woodType.name().toLowerCase(Locale.ROOT)), " ");
	}

	public static void setNumberOfInventorySlots(ItemStack storageStack, int numberOfInventorySlots) {
		NBTHelper.putInt(storageStack.getOrCreateTag(), "numberOfInventorySlots", numberOfInventorySlots);
	}

	public static  void setNumberOfUpgradeSlots(ItemStack storageStack, int numberOfUpgradeSlots) {
		NBTHelper.putInt(storageStack.getOrCreateTag(), "numberOfUpgradeSlots", numberOfUpgradeSlots);
	}
}
