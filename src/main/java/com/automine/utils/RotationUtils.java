package com.automine.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Computes the rotation required to look at a point, used by the input handler. */
public final class RotationUtils {

    private RotationUtils() {
    }

    public static Rotation calcRotationFromVec3d(Vec3 orig, Vec3 dest) {
        double dx = dest.x - orig.x;
        double dy = dest.y - orig.y;
        double dz = dest.z - orig.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, dist)));
        return new Rotation(yaw, pitch).normalize();
    }

    public static Rotation calcRotationFromVec3d(Vec3 eyes, BlockPos target) {
        Vec3 center = Vec3.atCenterOf(target);
        return calcRotationFromVec3d(eyes, center);
    }

    /** Shortest signed angular difference from {@code current} to {@code target}, in degrees. */
    public static float angleDiff(float current, float target) {
        float diff = (target - current) % 360.0f;
        if (diff < -180.0f) diff += 360.0f;
        if (diff > 180.0f) diff -= 360.0f;
        return diff;
    }
}
