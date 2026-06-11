package com.automine.pathing.movement;

import com.automine.AutoMineMod;
import com.automine.api.Settings;
import com.automine.utils.BlockStateInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

/**
 * Immutable snapshot of everything the cost functions need: a world view and the current
 * settings. Created once per path calculation so the search is deterministic and thread-safe
 * with respect to the main thread mutating the world.
 */
public class CalculationContext {

    public final BlockStateInterface bsi;
    public final ClientLevel level;
    public final Settings settings;
    public final boolean allowBreak;
    public final boolean allowPlace;
    public final boolean allowParkour;
    public final boolean allowSprint;
    public final int maxFallHeightNoWater;

    public CalculationContext(Minecraft mc) {
        this.level = mc.level;
        this.bsi = new BlockStateInterface(mc.level);
        this.settings = AutoMineMod.settings();
        this.allowBreak = settings.allowBreak;
        this.allowPlace = settings.allowPlace;
        this.allowParkour = settings.allowParkour;
        this.allowSprint = settings.allowSprint;
        this.maxFallHeightNoWater = settings.maxFallHeightNoWater;
    }
}
