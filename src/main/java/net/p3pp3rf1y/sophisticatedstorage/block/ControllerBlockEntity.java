package net.p3pp3rf1y.sophisticatedstorage.block;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedcore.controller.ControllerBlockEntityBase;
import net.p3pp3rf1y.sophisticatedcore.util.WorldHelper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ControllerBlockEntity extends ControllerBlockEntityBase implements ILockable, ICountDisplay, ITierDisplay, IUpgradeDisplay, IFillLevelDisplay {
	private long lastDepositTime = -100;

	public ControllerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.CONTROLLER_BLOCK_ENTITY_TYPE, pos, state);
	}

	public void depositPlayerItems(Player player, InteractionHand hand) {
		if (getLevel() == null) {
			return;
		}
		long gameTime = getLevel().getGameTime();
		boolean doubleClick = gameTime - lastDepositTime < 10;
		lastDepositTime = gameTime;
		if (doubleClick) {
			PlayerInventoryStorage playerInventory = PlayerInventoryStorage.of(player);
			for (var view : playerInventory.nonEmptyViews()) {
				if (canDepositStack(view.getResource().toStack((int) view.getAmount()))) {
					try(Transaction ctx = Transaction.openOuter()) {
						long inserted = insert(view.getResource(), view.getAmount(), ctx, false);
						if (inserted > 0 && view.extract(view.getResource(), inserted, ctx) == inserted) {
							ctx.commit();
						}
					}
				}
			}
			return;
		}

		ItemStack itemInHand = player.getItemInHand(hand);
		if (!itemInHand.isEmpty() && canDepositStack(itemInHand)) {
			try (Transaction ctx = Transaction.openOuter()) {
				long inserted = insert(ItemVariant.of(itemInHand), itemInHand.getCount(), ctx, false);
				player.setItemInHand(hand, itemInHand.copyWithCount(itemInHand.getCount() - (int) inserted));
				ctx.commit();
			}
		}
	}

	private boolean canDepositStack(ItemStack stack) {
		return hasItem(stack.getItem()) || isMemorizedItem(stack) || isFilterItem(stack.getItem());
	}

	@Override
	public void toggleLock() {
		Set<ILockable> unlockedStorages = new HashSet<>();
		Set<ILockable> lockedStorages = new HashSet<>();
		getStoragePositions().forEach(storagePosition -> WorldHelper.getLoadedBlockEntity(level, storagePosition, ILockable.class).ifPresent(lockable -> {
			if (lockable.isLocked()) {
				lockedStorages.add(lockable);
			} else {
				unlockedStorages.add(lockable);
			}
		}));

		if (unlockedStorages.isEmpty()) {
			lockedStorages.forEach(ILockable::toggleLock);
		} else {
			unlockedStorages.forEach(ILockable::toggleLock);
		}
	}

	@Override
	public boolean isLocked() {
		return false;
	}

	@Override
	public boolean shouldShowLock() {
		return false;
	}

	@Override
	public void toggleLockVisibility() {
		Set<ILockable> invisibleLockStorages = new HashSet<>();
		Set<ILockable> visibleLockStorages = new HashSet<>();
		getStoragePositions().forEach(storagePosition -> WorldHelper.getLoadedBlockEntity(level, storagePosition, ILockable.class).ifPresent(lockable -> {
			if (lockable.isLocked()) {
				if (lockable.shouldShowLock()) {
					visibleLockStorages.add(lockable);
				} else {
					invisibleLockStorages.add(lockable);
				}
			}
		}));

		if (invisibleLockStorages.isEmpty()) {
			visibleLockStorages.forEach(ILockable::toggleLockVisibility);
		} else {
			invisibleLockStorages.forEach(ILockable::toggleLockVisibility);
		}
	}

	@Override
	public boolean shouldShowCounts() {
		return false;
	}

	@Override
	public void toggleCountVisibility() {
		Set<ICountDisplay> invisibleCountStorages = new HashSet<>();
		Set<ICountDisplay> visibleCountStorages = new HashSet<>();
		getStoragePositions().forEach(storagePosition -> WorldHelper.getLoadedBlockEntity(level, storagePosition, ICountDisplay.class).ifPresent(countDisplay -> {
			if (countDisplay.shouldShowCounts()) {
				visibleCountStorages.add(countDisplay);
			} else {
				invisibleCountStorages.add(countDisplay);
			}
		}));

		if (invisibleCountStorages.isEmpty()) {
			visibleCountStorages.forEach(ICountDisplay::toggleCountVisibility);
		} else {
			invisibleCountStorages.forEach(ICountDisplay::toggleCountVisibility);
		}
	}

	@Override
	public List<Integer> getSlotCounts() {
		return List.of();
	}

	@Override
	public boolean shouldShowTier() {
		return false;
	}

	@Override
	public void toggleTierVisiblity() {
		Set<ITierDisplay> invisibleTierStorages = new HashSet<>();
		Set<ITierDisplay> visibleTierStorages = new HashSet<>();
		getStoragePositions().forEach(storagePosition -> WorldHelper.getLoadedBlockEntity(level, storagePosition, ITierDisplay.class).ifPresent(tierDisplay -> {
			if (tierDisplay.shouldShowTier()) {
				visibleTierStorages.add(tierDisplay);
			} else {
				invisibleTierStorages.add(tierDisplay);
			}
		}));

		if (invisibleTierStorages.isEmpty()) {
			visibleTierStorages.forEach(ITierDisplay::toggleTierVisiblity);
		} else {
			invisibleTierStorages.forEach(ITierDisplay::toggleTierVisiblity);
		}
	}

	@Override
	public boolean shouldShowUpgrades() {
		return false;
	}

	@Override
	public void toggleUpgradesVisiblity() {
		Set<IUpgradeDisplay> invisibleUpgradeStorages = new HashSet<>();
		Set<IUpgradeDisplay> visibleUpgradeStorages = new HashSet<>();
		getStoragePositions().forEach(storagePosition -> WorldHelper.getLoadedBlockEntity(level, storagePosition, IUpgradeDisplay.class).ifPresent(upgradeDisplay -> {
			if (upgradeDisplay.shouldShowUpgrades()) {
				visibleUpgradeStorages.add(upgradeDisplay);
			} else {
				invisibleUpgradeStorages.add(upgradeDisplay);
			}
		}));

		if (invisibleUpgradeStorages.isEmpty()) {
			visibleUpgradeStorages.forEach(IUpgradeDisplay::toggleUpgradesVisiblity);
		} else {
			invisibleUpgradeStorages.forEach(IUpgradeDisplay::toggleUpgradesVisiblity);
		}
	}

	@Override
	public boolean shouldShowFillLevels() {
		return false;
	}

	@Override
	public void toggleFillLevelVisibility() {
		Set<IFillLevelDisplay> invisibleFillLevelStorages = new HashSet<>();
		Set<IFillLevelDisplay> visibleFillLevelStorages = new HashSet<>();
		getStoragePositions().forEach(storagePosition -> WorldHelper.getLoadedBlockEntity(level, storagePosition, IFillLevelDisplay.class).ifPresent(fillLevelDisplay -> {
			if (fillLevelDisplay.shouldShowFillLevels()) {
				visibleFillLevelStorages.add(fillLevelDisplay);
			} else {
				invisibleFillLevelStorages.add(fillLevelDisplay);
			}
		}));

		if (invisibleFillLevelStorages.isEmpty()) {
			visibleFillLevelStorages.forEach(IFillLevelDisplay::toggleFillLevelVisibility);
		} else {
			invisibleFillLevelStorages.forEach(IFillLevelDisplay::toggleFillLevelVisibility);
		}
	}

	@Override
	public List<Float> getSlotFillLevels() {
		return List.of();
	}
}
