package net.p3pp3rf1y.sophisticatedstorage.block;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.porting_lib.base.util.LazyOptional;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public abstract class WoodStorageBlockEntity extends StorageBlockEntity {
	private static final String PACKED_TAG = "packed";
	@Nullable
	private WoodType woodType = null;

	private boolean packed = false;

	protected WoodStorageBlockEntity(BlockPos pos, BlockState state, BlockEntityType<? extends StorageBlockEntity> blockEntityType) {
		super(pos, state, blockEntityType);
	}

	@Override
	protected void saveSynchronizedData(CompoundTag tag) {
		super.saveSynchronizedData(tag);
		if (woodType != null) {
			tag.putString("woodType", woodType.name());
		}
		tag.putBoolean(PACKED_TAG, packed);
	}

	public CompoundTag getStorageContentsTag() {
		CompoundTag contents = saveWithoutMetadata();
		contents.putBoolean(PACKED_TAG, false);
		return contents;
	}

	@Override
	public void loadSynchronizedData(CompoundTag tag) {
		super.loadSynchronizedData(tag);
		woodType = NBTHelper.getString(tag, "woodType").flatMap(woodTypeName -> WoodType.values().filter(wt -> wt.name().equals(woodTypeName)).findFirst())
				.orElse(getStorageWrapper().hasMainColor() && getStorageWrapper().hasAccentColor() ? null : WoodType.ACACIA);
		packed = tag.getBoolean(PACKED_TAG);
	}

	public Optional<WoodType> getWoodType() {
		return Optional.ofNullable(woodType);
	}

	public void setWoodType(WoodType woodType) {
		this.woodType = woodType;
		setChanged();
	}

	@Override
	public Component getDisplayName() {
		if (displayName != null) {
			return displayName;
		}
		return makeWoodStorageDescriptionId(getWoodType().orElse(null));
	}

	private Component makeWoodStorageDescriptionId(@Nullable WoodType wt) {
		String id = Util.makeDescriptionId("block", Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock())));
		return WoodStorageBlockItem.getDisplayName(id, wt);
	}

	public boolean isPacked() {
		return packed;
	}

	public void setPacked(boolean packed) {
		this.packed = packed;
	}

	@Override
	public boolean shouldDropContents() {
		return !isPacked();
	}

	@Nonnull
	@Override
	public <T, C> LazyOptional<T> getCapability(BlockApiLookup<T, C> cap, @Nullable C opt) {
		if (isPacked() && cap == ItemStorage.SIDED) {
			return LazyOptional.empty();
		}

		return super.getCapability(cap, opt);
	}

	@Override
	public boolean canConnectStorages() {
		return !packed && super.canConnectStorages();
	}

	@Override
	public boolean canBeConnected() {
		return !packed && super.canBeConnected();
	}

	@Override
	public boolean canBeLinked() {
		return !packed;
	}

	@Override
	protected boolean canRefreshUpgrades() {
		return super.canRefreshUpgrades() && !packed;
	}

	@Override
	public @Nullable Object getRenderAttachmentData() {
		return new ModelData(this);
	}

	public record ModelData(Boolean hasMainColor, @Nullable String woodName) {
		public ModelData(WoodStorageBlockEntity tile) {
			this(tile.getStorageWrapper().hasMainColor(), tile.getWoodType().map(WoodType::name).orElse(null));
		}
	}
}
