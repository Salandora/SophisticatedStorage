package net.p3pp3rf1y.sophisticatedstorage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderInfo;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedcore.util.WorldHelper;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

public class BarrelBlockEntity extends WoodStorageBlockEntity {
	private static final String MATERIALS_TAG = "materials";
	public static final String STORAGE_TYPE = "barrel";
	private Map<BarrelMaterial, ResourceLocation> materials = new EnumMap<>(BarrelMaterial.class);
	private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
		protected void onOpen(Level level, BlockPos pos, BlockState state) {
			playSound(state, SoundEvents.BARREL_OPEN);
			updateOpenBlockState(state, true);
		}

		protected void onClose(Level level, BlockPos pos, BlockState state) {
			playSound(state, SoundEvents.BARREL_CLOSE);
			updateOpenBlockState(state, false);
		}

		protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int previousOpenerCount, int newOpenerCount) {
			//noop
		}

		protected boolean isOwnContainer(Player player) {
			if (player.containerMenu instanceof StorageContainerMenu storageContainerMenu) {
				return storageContainerMenu.getStorageBlockEntity() == BarrelBlockEntity.this;
			} else {
				return false;
			}
		}
	};

	private IDynamicRenderTracker dynamicRenderTracker = IDynamicRenderTracker.NOOP;

	@Override
	protected ContainerOpenersCounter getOpenersCounter() {
		return openersCounter;
	}

	@Override
	protected String getStorageType() {
		return STORAGE_TYPE;
	}

	protected BarrelBlockEntity(BlockPos pos, BlockState state, BlockEntityType<? extends BarrelBlockEntity> blockEntityType) {
		super(pos, state, blockEntityType);
		getStorageWrapper().getRenderInfo().setChangeListener(ri -> {
			dynamicRenderTracker.onRenderInfoUpdated(ri);
			WorldHelper.notifyBlockUpdate(this);
		});
	}

	public BarrelBlockEntity(BlockPos pos, BlockState state) {
		this(pos, state, ModBlocks.BARREL_BLOCK_ENTITY_TYPE);
	}

	void updateOpenBlockState(BlockState state, boolean open) {
		if (level == null) {
			return;
		}
		level.setBlock(getBlockPos(), state.setValue(BarrelBlock.OPEN, open), 3);
	}

	@Override
	public void setLevel(Level level) {
		super.setLevel(level);
		if (level.isClientSide) {
			dynamicRenderTracker = new DynamicRenderTracker(this);
		}
	}

	public boolean hasDynamicRenderer() {
		return dynamicRenderTracker.isDynamicRenderer();
	}

	public boolean hasFullyDynamicRenderer() {
		return dynamicRenderTracker.isFullyDynamicRenderer();
	}

	@Override
	public void toggleLock() {
		setUpdateBlockRender();
		super.toggleLock();
	}

	@Override
	protected void saveSynchronizedData(CompoundTag tag) {
		super.saveSynchronizedData(tag);
		NBTHelper.putMap(tag, MATERIALS_TAG, materials, BarrelMaterial::getSerializedName, resourceLocation -> StringTag.valueOf(resourceLocation.toString()));
	}

	@Override
	public void loadSynchronizedData(CompoundTag tag) {
		super.loadSynchronizedData(tag);
		materials = NBTHelper.getMap(tag, MATERIALS_TAG, BarrelMaterial::fromName, (bm, t) -> Optional.of(new ResourceLocation(t.getAsString()))).orElse(Map.of());
	}

	public void setMaterials(Map<BarrelMaterial, ResourceLocation> materials) {
		this.materials = materials;
		setChanged();
	}

	public Map<BarrelMaterial, ResourceLocation> getMaterials() {
		return materials;
	}

	@Override
	public @Nullable Object getRenderAttachmentData() {
		return new ModelData(this);
	}

	public static class ModelData {
		public static ModelData EMPTY = new ModelData(null);
		private final Boolean hasMainColor;
		private final Boolean hasAccentColor;
		private String woodName;
		private final Boolean isPacked;
		private final Boolean showsLock;
		private final Boolean showsTier;
		private List<RenderInfo.DisplayItem> displayItems;
		private List<Integer> inaccessibleSlots;
		private final Map<BarrelMaterial, ResourceLocation> materials;

		public ModelData(@Nullable BarrelBlockEntity tile) {
			if (tile != null) {
				StorageWrapper wrapper = tile.getStorageWrapper();
				this.hasMainColor = wrapper.hasMainColor();
				this.hasAccentColor = wrapper.hasAccentColor();

				Optional<WoodType> woodType = tile.getWoodType();
				if (woodType.isPresent() || !(hasMainColor && hasAccentColor)) {
					this.woodName = woodType.orElse(WoodType.ACACIA).name();
				}

				if (!tile.hasFullyDynamicRenderer()) {
					this.displayItems = wrapper.getRenderInfo().getItemDisplayRenderInfo().getDisplayItems();
					this.inaccessibleSlots = wrapper.getRenderInfo().getItemDisplayRenderInfo().getInaccessibleSlots();
				}

				this.isPacked = tile.isPacked();
				this.showsLock = tile.isLocked() && tile.shouldShowLock();
				this.showsTier = tile.shouldShowTier();
				this.materials = tile.getMaterials();
			} else {
				this.hasMainColor = false;
				this.hasAccentColor = false;

				this.woodName = null;

				this.displayItems = null;
				this.inaccessibleSlots = null;

				this.isPacked = false;
				this.showsLock = false;
				this.showsTier = false;
				this.materials = null;
			}
		}

		// Getters for fields (you can generate these automatically in most IDEs)
		public Boolean hasMainColor() {
			return hasMainColor;
		}

		public Boolean hasAccentColor() {
			return hasAccentColor;
		}

		@Nullable
		public String woodName() {
			return woodName;
		}

		public Boolean isPacked() {
			return isPacked;
		}

		public Boolean showsLock() {
			return showsLock;
		}

		public Boolean showsTier() {
			return showsTier;
		}

		@Nullable
		public List<RenderInfo.DisplayItem> getDisplayItems() {
			return displayItems;
		}

		@Nullable
		public List<Integer> getInaccessibleSlots() {
			return inaccessibleSlots;
		}

		@Nullable
		public Map<BarrelMaterial, ResourceLocation> getMaterials() {
			return materials;
		}
	}
}
