package net.p3pp3rf1y.sophisticatedstorage.common;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
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
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;
import net.p3pp3rf1y.sophisticatedcore.network.SyncPlayerSettingsMessage;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsManager;
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

import javax.annotation.Nullable;

public class CommonEventHandler {
	private static final int AVERAGE_MAX_ITEM_ENTITY_DROP_COUNT = 20;

	public void registerHandlers() {
		ServerPlayConnectionEvents.JOIN.register(this::onPlayerLoggedIn);
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(this::onPlayerChangedDimension);
		ServerPlayerEvents.AFTER_RESPAWN.register(this::onPlayerRespawn);

		PlayerBlockBreakEvents.BEFORE.register(this::onBlockBreak);

		AttackBlockCallback.EVENT.register(this::onLimitedBarrelLeftClicked);
		UseBlockCallback.EVENT.register(this::onSneakItemBlockInteraction);
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

	private boolean onBlockBreak(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
		if (!(state.getBlock() instanceof WoodStorageBlockBase) || player.isShiftKeyDown()) {
			return true;
		}

		return WorldHelper.getBlockEntity(level, pos, WoodStorageBlockEntity.class).map(wbe -> {
			if (wbe.isPacked()) {
				return true;
			}

			int droppedItemEntityCount = 0;
			InventoryHandler inventoryHandler = wbe.getStorageWrapper().getInventoryHandler();
			for (int slot = 0; slot < inventoryHandler.getSlotCount(); slot++) {
				ItemStack stack = inventoryHandler.getStackInSlot(slot);
				if (stack.isEmpty()) {
					continue;
				}
				droppedItemEntityCount += (int) Math.ceil(stack.getCount() / (double) Math.min(stack.getMaxStackSize(), AVERAGE_MAX_ITEM_ENTITY_DROP_COUNT));
			}

			if (droppedItemEntityCount > Config.SERVER.tooManyItemEntityDrops.get()) {
				ItemBase packingTapeItem = ModItems.PACKING_TAPE;
				Component packingTapeItemName = packingTapeItem.getName(new ItemStack(packingTapeItem)).copy().withStyle(ChatFormatting.GREEN);
				player.sendSystemMessage(StorageTranslationHelper.INSTANCE.translStatusMessage("too_many_item_entity_drops",
						state.getBlock().getName().withStyle(ChatFormatting.GREEN),
						Component.literal(String.valueOf(droppedItemEntityCount)).withStyle(ChatFormatting.RED),
						packingTapeItemName)
				);

				return false;
			}

			return true;
		}).orElse(true);
	}
}
