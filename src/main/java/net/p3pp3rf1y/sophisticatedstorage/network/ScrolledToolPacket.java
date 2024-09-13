package net.p3pp3rf1y.sophisticatedstorage.network;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageToolItem;

import static net.p3pp3rf1y.sophisticatedstorage.init.ModItems.STORAGE_TOOL;

public class ScrolledToolPacket implements FabricPacket {
	public static final PacketType<ScrolledToolPacket> TYPE = PacketType.create(new ResourceLocation(SophisticatedStorage.MOD_ID, "scrolled_tool"), ScrolledToolPacket::new);
	private final boolean next;

	public ScrolledToolPacket(boolean next) {
		this.next = next;
	}

	public ScrolledToolPacket(FriendlyByteBuf buffer) {
		this(buffer.readBoolean());
	}

	public void handle(ServerPlayer player, PacketSender responseSender) {
		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() == STORAGE_TOOL) {
			StorageToolItem.cycleMode(stack, next);
		}
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(next);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
