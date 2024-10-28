package net.p3pp3rf1y.sophisticatedstorage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedcore.util.model.ModelData;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedcore.util.WorldHelper;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static net.p3pp3rf1y.sophisticatedstorage.util.model.ModelProperties.*;

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
		this(pos, state, ModBlocks.BARREL_BLOCK_ENTITY_TYPE.get());
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
	public void loadSynchronizedData(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadSynchronizedData(tag, registries);
		materials = NBTHelper.getMap(tag, MATERIALS_TAG, BarrelMaterial::fromName, (bm, t) -> Optional.of(ResourceLocation.parse(t.getAsString()))).orElse(Map.of());
	}

	public void setMaterials(Map<BarrelMaterial, ResourceLocation> materials) {
		this.materials = materials;
		setChanged();
	}

	public Map<BarrelMaterial, ResourceLocation> getMaterials() {
		return materials;
	}

	@Override
	public @Nullable Object getRenderData() {
		ModelData.Builder builder = ModelData.builder();
		boolean hasMainColor = this.getStorageWrapper().hasMainColor();
		builder.with(HAS_MAIN_COLOR, hasMainColor);
		boolean hasAccentColor = this.getStorageWrapper().hasAccentColor();
		builder.with(HAS_ACCENT_COLOR, hasAccentColor);
		if (!this.hasFullyDynamicRenderer()) {
			builder.with(DISPLAY_ITEMS, this.getStorageWrapper().getRenderInfo().getItemDisplayRenderInfo().getDisplayItems());
			builder.with(INACCESSIBLE_SLOTS, this.getStorageWrapper().getRenderInfo().getItemDisplayRenderInfo().getInaccessibleSlots());
		}
		builder.with(IS_PACKED, this.isPacked());
		builder.with(SHOWS_LOCK, this.isLocked() && this.shouldShowLock());
		builder.with(SHOWS_TIER, this.shouldShowTier());
		Optional<WoodType> woodType = this.getWoodType();
		if (woodType.isPresent() || !(hasMainColor && hasAccentColor)) {
			builder.with(WOOD_NAME, woodType.orElse(WoodType.ACACIA).name());
		}

		Map<BarrelMaterial, ResourceLocation> materials = this.getMaterials();
		if (!materials.isEmpty()) {
			builder.with(MATERIALS, materials);
		}
		return builder.build();
	}
}
