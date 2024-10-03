package net.p3pp3rf1y.sophisticatedstorage.common;

import com.google.common.collect.Queues;

import net.fabricmc.fabric.api.block.BlockPickInteractionAware;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;
import net.p3pp3rf1y.sophisticatedcore.network.SyncPlayerSettingsMessage;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsManager;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;
import net.p3pp3rf1y.sophisticatedcore.util.WorldHelper;
import net.p3pp3rf1y.sophisticatedstorage.Config;
import net.p3pp3rf1y.sophisticatedstorage.block.ISneakItemInteractionBlock;
import net.p3pp3rf1y.sophisticatedstorage.block.LimitedBarrelBlock;
import net.p3pp3rf1y.sophisticatedstorage.block.WoodStorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.block.WoodStorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.settings.StorageSettingsHandler;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;

public class CommonEventHandler {
	private static final int AVERAGE_MAX_ITEM_ENTITY_DROP_COUNT = 20;

	private final Queue<TickTask> pendingTickTasks = Queues.newConcurrentLinkedQueue();

	public void registerHandlers() {
		ServerPlayConnectionEvents.JOIN.register(this::onPlayerLoggedIn);
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(this::onPlayerChangedDimension);
		ServerPlayerEvents.AFTER_RESPAWN.register(this::onPlayerRespawn);

		PlayerBlockBreakEvents.BEFORE.register(this::onBlockBreak);

		AttackBlockCallback.EVENT.register(this::onLimitedBarrelLeftClicked);
		UseBlockCallback.EVENT.register(this::onSneakItemBlockInteraction);

		ServerTickEvents.END_SERVER_TICK.register(this::onLevelTick);
	}

	private InteractionResult onLimitedBarrelLeftClicked(Player player, Level level, InteractionHand hand, BlockPos pos, Direction direction) {
		if (!player.isCreative()) {
			return InteractionResult.PASS;
		}

		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof LimitedBarrelBlock limitedBarrel)) {
			return InteractionResult.PASS;
		}
		if (limitedBarrel.tryToTakeItem(state, level, pos, player)) {
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	private InteractionResult onSneakItemBlockInteraction(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
		if (!player.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}

		BlockPos pos = hitResult.getBlockPos();
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof ISneakItemInteractionBlock sneakItemInteractionBlock)) {
			return InteractionResult.PASS;
		}
		if (sneakItemInteractionBlock.trySneakItemInteraction(player, hand, state, level, pos, hitResult, player.getItemInHand(hand))) {
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	private void onPlayerChangedDimension(ServerPlayer player, ServerLevel origin, ServerLevel destination) {
		sendPlayerSettingsToClient(player);
	}

	private void onPlayerLoggedIn(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
		sendPlayerSettingsToClient(handler.player);
	}

	private void sendPlayerSettingsToClient(Player player) {
		String playerTagName = StorageSettingsHandler.SOPHISTICATED_STORAGE_SETTINGS_PLAYER_TAG;
		PacketHandler.sendToClient((ServerPlayer) player, new SyncPlayerSettingsMessage(playerTagName, SettingsManager.getPlayerSettingsTag(player, playerTagName)));
	}

	private void onPlayerRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
		sendPlayerSettingsToClient(newPlayer);
	}

	private void onLevelTick(MinecraftServer server) {
		if (pendingTickTasks.isEmpty()) {
			return;
		}

		Iterator<TickTask> it = pendingTickTasks.iterator();

		while (it.hasNext()) {
			TickTask tickTask = it.next();
			if (tickTask.getTick() <= server.getTickCount()) {
				tickTask.run();
				it.remove();
			}
		}
	}

	private boolean onBlockBreak(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
		if (!(state.getBlock() instanceof WoodStorageBlockBase) || player.isShiftKeyDown()) {
			return true;
		}

		AtomicBoolean cancelEvent = new AtomicBoolean(false);
		WorldHelper.getBlockEntity(level, pos, WoodStorageBlockEntity.class).ifPresent(wbe -> {
			if (wbe.isPacked() || Boolean.TRUE.equals(Config.COMMON.dropPacked.get())) {
				return;
			}

			AtomicInteger droppedItemEntityCount = new AtomicInteger(0);
			InventoryHelper.iterate(wbe.getStorageWrapper().getInventoryHandler(), (slot, stack) -> {
				if (stack.isEmpty()) {
					return;
				}
				droppedItemEntityCount.addAndGet((int) Math.ceil(stack.getCount() / (double) Math.min(stack.getMaxStackSize(), AVERAGE_MAX_ITEM_ENTITY_DROP_COUNT)));
			});

			if (droppedItemEntityCount.get() > Config.SERVER.tooManyItemEntityDrops.get()) {
				cancelEvent.set(true);
				ItemBase packingTapeItem = ModItems.PACKING_TAPE;
				Component packingTapeItemName = packingTapeItem.getName(new ItemStack(packingTapeItem)).copy().withStyle(ChatFormatting.GREEN);

				ItemStack clonedStack;
				if (state.getBlock() instanceof BlockPickInteractionAware) {
					clonedStack = ((BlockPickInteractionAware) state.getBlock()).getPickedStack(state, level, pos, player, new BlockHitResult(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.DOWN, pos, true));
				} else {
					clonedStack = state.getBlock().getCloneItemStack(level, pos, state);
				}

				player.sendSystemMessage(StorageTranslationHelper.INSTANCE.translStatusMessage("too_many_item_entity_drops",
						clonedStack.getHoverName().copy().withStyle(ChatFormatting.GREEN),
						Component.literal(String.valueOf(droppedItemEntityCount)).withStyle(ChatFormatting.RED),
						packingTapeItemName)
				);
				if (level instanceof ServerLevel serverLevel) {
					level.scheduleTick(pos, state.getBlock(), 2);
					pendingTickTasks.add(new TickTask(serverLevel.getServer().getTickCount() + 2, () -> {
						wbe.setUpdateBlockRender();
						WorldHelper.notifyBlockUpdate(wbe);
					}));
				}
			}
		});
		return !cancelEvent.get();
	}
}
