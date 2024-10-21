/*
package net.p3pp3rf1y.sophisticatedstorage.compat.sodium;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.p3pp3rf1y.sophisticatedstorage.client.render.TranslucentVertexConsumer;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import org.lwjgl.system.MemoryStack;

public class SodiumTranslucentVertexConsumer extends TranslucentVertexConsumer implements VertexBufferWriter {
	public static void register() {
		TranslucentVertexConsumer.setFactory(SodiumTranslucentVertexConsumer::new);
	}

	private final MultiBufferSource buffer;

	public SodiumTranslucentVertexConsumer(MultiBufferSource buffer, int alpha) {
		super(buffer, alpha);
		this.buffer = buffer;
	}

	@Override
	public void push(MemoryStack stack, long src, int count, VertexFormat format) {
		if (buffer instanceof VertexBufferWriter vertexBufferWriter) {
			vertexBufferWriter.push(stack, src, count, format);
		}
	}
}
*/
