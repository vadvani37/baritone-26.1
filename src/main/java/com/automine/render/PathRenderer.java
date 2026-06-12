package com.automine.render;

import com.automine.AutoMineMod;
import com.automine.pathing.path.Path;
import com.automine.utils.BetterBlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;

import java.util.List;

/**
 * Visualises the active path as a trail of particles along each node. 26.1 removed Fabric's
 * simple world-render event, so rather than a fragile renderer mixin this spawns lightweight
 * particles every few ticks — robust and dependency-free. Toggle with {@code #set renderPath}.
 */
public final class PathRenderer {

    private static final int EVERY_N_TICKS = 4;
    private static final int MAX_NODES = 96;

    private static int tick;

    private PathRenderer() {
    }

    public static void onClientTick(Minecraft mc) {
        if (!AutoMineMod.settings().renderPath || mc.level == null) {
            return;
        }
        Path path = AutoMineMod.pathing().getPath();
        if (path == null) {
            return;
        }
        if (tick++ % EVERY_N_TICKS != 0) {
            return; // throttle so long paths don't flood particles
        }
        List<BetterBlockPos> nodes = path.positions();
        int max = Math.min(nodes.size(), MAX_NODES);
        for (int i = 0; i < max; i++) {
            BetterBlockPos p = nodes.get(i);
            // HAPPY_VILLAGER = green sparkle; marks the route like Baritone's path line.
            mc.level.addParticle(ParticleTypes.HAPPY_VILLAGER, p.x + 0.5, p.y + 0.5, p.z + 0.5, 0, 0, 0);
        }
    }
}
