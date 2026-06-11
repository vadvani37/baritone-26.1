package com.automine.process;

import com.automine.AutoMineMod;
import com.automine.goals.GoalNear;
import com.automine.utils.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

/**
 * Continuously re-targets a goal near the followed entity. Backs {@code #follow} and {@code #come}.
 */
public final class FollowProcess {

    private Predicate<Entity> filter;
    private boolean active;
    private String label = "";

    public void follow(Predicate<Entity> filter, String label) {
        this.filter = filter;
        this.label = label;
        this.active = true;
        Helper.log("Following: " + label);
    }

    public void stop() {
        active = false;
        filter = null;
    }

    public boolean isActive() {
        return active;
    }

    public void onTick() {
        if (!active || filter == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        Entity target = nearest(mc);
        if (target == null) {
            return;
        }
        int radius = AutoMineMod.settings().followRadius;
        var pos = target.blockPosition();
        if (!(AutoMineMod.pathing().getGoal() instanceof GoalNear g)
                || g.x != pos.getX() || g.z != pos.getZ()) {
            AutoMineMod.pathing().setGoalAndPath(new GoalNear(pos.getX(), pos.getY(), pos.getZ(), radius));
        }
    }

    private Entity nearest(Minecraft mc) {
        Entity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == mc.player || !filter.test(e)) {
                continue;
            }
            double d = e.distanceToSqr(mc.player);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        return best;
    }

    public static Predicate<Entity> players() {
        return e -> e instanceof Player;
    }
}
