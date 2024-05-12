package net.p3pp3rf1y.sophisticatedstorage.block;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.porting_lib.base.util.LazyOptional;
import net.p3pp3rf1y.sophisticatedcore.controller.ControllerBlockEntityBase;
import net.p3pp3rf1y.sophisticatedcore.controller.IControllerBoundable;
import net.p3pp3rf1y.sophisticatedcore.controller.ILinkable;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedcore.util.WorldHelper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

public class StorageIOBlockEntity extends BlockEntity implements IControllerBoundable, ILinkable {
	@Nullable
	private BlockPos controllerPos = null;
	private boolean isLinkedToController = false;
	private Map<BlockApiLookup<?, Direction>, Map<Direction, LazyOptional<?>>> capabilitySideCache = new HashMap<>();
	protected StorageIOBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public StorageIOBlockEntity(BlockPos pos, BlockState state) {
		this(ModBlocks.STORAGE_IO_BLOCK_ENTITY_TYPE, pos, state);
	}

	@Override
	public Set<BlockPos> getConnectablePositions() {
		return Collections.emptySet();
	}

	@Override
	public boolean connectLinkedSelf() {
		return true;
	}

	@Override
	public void setControllerPos(BlockPos controllerPos) {
		this.controllerPos = controllerPos;
		invalidateAllCapabilityCache();
		setChanged();
	}

	@Override
	public Optional<BlockPos> getControllerPos() {
		return Optional.ofNullable(controllerPos);
	}

	@Override
	public boolean isLinked() {
		return isLinkedToController && getControllerPos().isPresent();
	}

	@Override
	public void linkToController(BlockPos controllerPos) {
		if (getControllerPos().isPresent()) {
			return;
		}

		isLinkedToController = true;
		ILinkable.super.linkToController(controllerPos);
		setChanged();
	}

	@Override
	public void setNotLinked() {
		ILinkable.super.setNotLinked();
		isLinkedToController = false;
		removeControllerPos();
		setChanged();
	}

	@Override
	public void removeControllerPos() {
		controllerPos = null;
		invalidateAllCapabilityCache();
		capabilitySideCache.clear();
		setChanged();
	}

	@SuppressWarnings("java:S1640") //can't use EnumMap because one of keys is null
	private void invalidateAllCapabilityCache() {
		capabilitySideCache.forEach((cap, map) -> {
			HashMap<Direction, LazyOptional<?>> copy = new HashMap<>(map); //to prevent concurrent modification exception
			copy.forEach((side, storage) -> storage.invalidate());
		});
	}

	@Override
	public BlockPos getStorageBlockPos() {
		return getBlockPos();
	}

	@Override
	public Level getStorageBlockLevel() {
		return getLevel();
	}

	@Override
	public boolean canConnectStorages() {
		return false;
	}

	@Override
	public void addToController(Level level, BlockPos pos, BlockPos controllerPos) {
		WorldHelper.getBlockEntity(level, controllerPos, ControllerBlockEntityBase.class).ifPresent(c -> c.addStorage(pos));
	}

	@Override
	public boolean canBeConnected() {
		return isLinked() || getControllerPos().isEmpty();
	}

	@Override
	public void registerController(ControllerBlockEntityBase controllerBlockEntity) {
		setControllerPos(controllerBlockEntity.getBlockPos());
	}

	@Override
	public void unregisterController() {
		removeControllerPos();
	}

	public void removeFromController() {
		if (!level.isClientSide()) {
			getControllerPos().flatMap(p -> WorldHelper.getBlockEntity(level, p, ControllerBlockEntityBase.class)).ifPresent(c -> c.removeNonConnectingBlock(getBlockPos()));
			removeControllerPos();
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		saveControllerPos(tag);
		if (isLinkedToController) {
			tag.putBoolean("isLinkedToController", isLinkedToController);
		}
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		loadControllerPos(tag);
		isLinkedToController = NBTHelper.getBoolean(tag, "isLinkedToController").orElse(false);
	}

	@SuppressWarnings("java:S1640") //can't use EnumMap because one of keys is null
	public <T> LazyOptional<T> getCapability(BlockApiLookup<T, Direction> cap, @Nullable Direction opt) {
		if (getControllerPos().isEmpty()) {
			return LazyOptional.empty();
		}

		if (!capabilitySideCache.containsKey(cap) || !capabilitySideCache.get(cap).containsKey(opt)) {
			LazyOptional<T> lazyOptional = getControllerPos().flatMap(p -> WorldHelper.getLoadedBlockEntity(getLevel(), p, ControllerBlockEntity.class))
					.map(c -> getControllerCapability(cap, opt, c))
					.orElseGet(LazyOptional::empty);
			capabilitySideCache.computeIfAbsent(cap, k -> new HashMap<>()).put(opt, lazyOptional);
			if (lazyOptional.isPresent()) {
				lazyOptional.addListener(l -> removeCapabilityCacheOnSide(cap, opt));
			}
		}

		return capabilitySideCache.get(cap).get(opt).cast();
	}

	private <T> void removeCapabilityCacheOnSide(BlockApiLookup<T, Direction> cap, @Nullable Direction side) {
		if (capabilitySideCache.containsKey(cap) && capabilitySideCache.get(cap).containsKey(side)) {
			capabilitySideCache.get(cap).get(side).invalidate();
			capabilitySideCache.get(cap).remove(side);
		}
	}

	protected <T> LazyOptional<T> getControllerCapability(BlockApiLookup<T, Direction> cap, @Nullable Direction opt, ControllerBlockEntity c) {
		return c.getCapability(cap, opt);
	}
}
