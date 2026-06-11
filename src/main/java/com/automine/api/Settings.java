package com.automine.api;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mutable, named settings — the backing store for the {@code #set} command, modelled on
 * Baritone's Settings. Booleans/numbers are reflected by name so commands can toggle them
 * generically without a switch statement per field.
 */
public final class Settings {

    public boolean allowBreak = true;
    public boolean allowPlace = true;
    public boolean allowSprint = true;
    public boolean allowParkour = true;
    public boolean allowDiagonal = true;

    /** Max blocks to free-fall without water below. */
    public int maxFallHeightNoWater = 3;

    /** A* budget per calculation. */
    public int maxNodes = 2_000_000;
    public int planningTimeoutMs = 2000;

    /** How close (blocks) follow/come tries to get. */
    public int followRadius = 3;

    /** Render the active path. */
    public boolean renderPath = true;

    private final Map<String, java.lang.reflect.Field> byName = new LinkedHashMap<>();

    public Settings() {
        for (var f : Settings.class.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isPublic(f.getModifiers())) {
                byName.put(f.getName().toLowerCase(), f);
            }
        }
    }

    public Map<String, java.lang.reflect.Field> all() {
        return byName;
    }

    public String get(String name) {
        var f = byName.get(name.toLowerCase());
        if (f == null) {
            return null;
        }
        try {
            return String.valueOf(f.get(this));
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /** Parse and assign a setting by name. Returns false if unknown or unparseable. */
    public boolean set(String name, String value) {
        var f = byName.get(name.toLowerCase());
        if (f == null) {
            return false;
        }
        try {
            Class<?> t = f.getType();
            if (t == boolean.class) {
                f.setBoolean(this, Boolean.parseBoolean(value));
            } else if (t == int.class) {
                f.setInt(this, Integer.parseInt(value));
            } else if (t == double.class) {
                f.setDouble(this, Double.parseDouble(value));
            } else {
                return false;
            }
            return true;
        } catch (IllegalAccessException | NumberFormatException e) {
            return false;
        }
    }
}
