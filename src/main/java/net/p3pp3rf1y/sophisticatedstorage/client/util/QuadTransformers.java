package net.p3pp3rf1y.sophisticatedstorage.client.util;

import com.mojang.math.Transformation;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.ArrayList;
import java.util.List;

public class QuadTransformers {
	private static MutableQuadViewImpl getEditorQuad(BakedQuad quad) {
		MutableQuadViewImpl mqv = new MutableQuadViewImpl() {
			{
				this.data = new int[EncodingFormat.TOTAL_STRIDE];
			}

			public void emitDirectly() {
				// noop
			}
		};

		mqv.fromVanilla(quad.getVertices(), 0);
		mqv.nominalFace(quad.getDirection());
		mqv.colorIndex(quad.getTintIndex());
		mqv.tag(0);

		return mqv;
	}

	public static List<BakedQuad> process(RenderContext.QuadTransform transform, List<BakedQuad> quads) {
		List<BakedQuad> transformedQuads = new ArrayList<>();

		for (int i = 0; i < quads.size(); i++) {
			BakedQuad quad = quads.get(i);
			MutableQuadView mqv = getEditorQuad(quad);
			transform.transform(mqv);

			BakedQuad transformedQuad = mqv.toBakedQuad(quad.getSprite());
			transformedQuads.add(transformedQuad);
		}

		return transformedQuads;
	}

    public static RenderContext.QuadTransform applying(Transformation transform) {
        if (transform.isIdentity())
            return quad -> true;

        return quad -> {
            for (int i = 0; i < 4; i++) {
                Vector4f pos = new Vector4f(quad.x(i), quad.y(i), quad.z(i), 1);
				transform.transformPosition(pos);
                pos.div(pos.w);
                quad.pos(i, pos.x(), pos.y(), pos.z());
            }

            for (int i = 0; i < 4; i++) {
                if (quad.hasNormal(i)) {
					Vector3f normal = new Vector3f(quad.normalX(i), quad.normalY(i), quad.normalZ(i));
					transform.transformNormal(normal);
                    quad.normal(i, normal);
                }
            }
            return true;
        };
    }
}
