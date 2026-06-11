package com.automine.control;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

import java.util.EnumMap;
import java.util.Map;

/**
 * Synthesises player input without mixins by forcing the relevant {@link KeyMapping}s down each
 * tick. Movements call {@link #setInput} during the tick; {@link #apply} then pushes the desired
 * state into Minecraft's key bindings BEFORE the game reads them, so the vanilla movement code
 * moves the player for us. This mirrors Baritone's InputOverrideHandler.
 */
public final class InputOverrideHandler {

    public enum Input {
        MOVE_FORWARD,
        MOVE_BACK,
        MOVE_LEFT,
        MOVE_RIGHT,
        JUMP,
        SNEAK,
        SPRINT,
        CLICK_LEFT,
        CLICK_RIGHT
    }

    private final Map<Input, Boolean> states = new EnumMap<>(Input.class);
    private boolean controlling;

    public void setInput(Input input, boolean held) {
        states.put(input, held);
    }

    public boolean isInputForcedDown(Input input) {
        return states.getOrDefault(input, false);
    }

    public void clearAll() {
        states.clear();
    }

    public void setControlling(boolean controlling) {
        this.controlling = controlling;
        if (!controlling) {
            clearAll();
            apply(); // release everything
        }
    }

    public boolean isControlling() {
        return controlling;
    }

    /** Push the desired input state into the live key bindings. Call once per tick, early. */
    public void apply() {
        Minecraft mc = Minecraft.getInstance();
        Options o = mc.options;
        if (o == null) {
            return;
        }
        set(o.keyUp, Input.MOVE_FORWARD);
        set(o.keyDown, Input.MOVE_BACK);
        set(o.keyLeft, Input.MOVE_LEFT);
        set(o.keyRight, Input.MOVE_RIGHT);
        set(o.keyJump, Input.JUMP);
        set(o.keyShift, Input.SNEAK);
        set(o.keySprint, Input.SPRINT);
    }

    private void set(KeyMapping key, Input input) {
        key.setDown(isInputForcedDown(input));
    }
}
