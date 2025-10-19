package net.p3pp3rf1y.sophisticatedstorage.client.util;

import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class QuaternionHelper {
    public static Quaternionf quatFromXYZDegree(Vector3f xyz) {
        return quatFromXYZDegree(xyz.x, xyz.y, xyz.z);
    }
    public static Quaternionf quatFromXYZDegree(float x, float y, float z) {
        return new Quaternionf().rotateXYZ(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
    }

	public static int hashCode(Quaternionf q) {
		final int prime = 31;
		long result = 1;
		result = prime * result + Float.floatToIntBits(q.w);
		result = prime * result + Float.floatToIntBits(q.x);
		result = prime * result + Float.floatToIntBits(q.y);
		result = prime * result + Float.floatToIntBits(q.z);
		return Long.hashCode(result);
	}
}
