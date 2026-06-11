package com.automine.utils;

/** A yaw/pitch pair, in degrees, matching Minecraft's conventions. */
public record Rotation(float yaw, float pitch) {

    public Rotation normalize() {
        float y = yaw % 360.0f;
        if (y < -180.0f) y += 360.0f;
        if (y > 180.0f) y -= 360.0f;
        float p = Math.max(-90.0f, Math.min(90.0f, pitch));
        return new Rotation(y, p);
    }
}
