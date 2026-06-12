package com.automine.process;

import com.automine.AutoMineMod;
import com.automine.control.BlockBreaker;
import com.automine.goals.GoalBlock;
import com.automine.goals.GoalNear;
import com.automine.utils.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

/**
 * Auto-mining: scans nearby loaded chunks for the requested block types, walks to the nearest
 * one via the pathing behaviour, and breaks it when in reach. Re-scans periodically rather than
 * every tick to stay cheap. This is the {@code #mine <blocks...>} brain.
 */
public final class MineProcess {

    private static final int SCAN_RADIUS = 32;
    private static final int SCAN_INTERVAL_TICKS = 20;
    private static final double REACH = 4.5;
    private static final double DROP_RANGE = 16.0;

    private final Set<Block> targets = new HashSet<>();
    private boolean active;
    private int tickCounter;
    private BlockPos currentTarget;

    public void mine(Set<Block> blocks) {
        targets.clear();
        targets.addAll(blocks);
        active = !targets.isEmpty();
        tickCounter = 0;
        currentTarget = null;
        if (active) {
            Helper.log("Mining: " + targets.size() + " block type(s).");
        }
    }

    public void stop() {
        active = false;
        targets.clear();
        currentTarget = null;
    }

    public boolean isActive() {
        return active;
    }

    public void onTick() {
        if (!active) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        if (currentTarget == null || tickCounter % SCAN_INTERVAL_TICKS == 0
                || !targets.contains(mc.level.getBlockState(currentTarget).getBlock())) {
            currentTarget = findNearest(mc);
        }
        tickCounter++;

        if (currentTarget == null) {
            return; // nothing in range; keep scanning
        }

        double dist = mc.player.getEyePosition(1.0f)
                .distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(currentTarget));
        if (dist <= REACH) {
            // Close enough to dig toward it: hold position and break whatever the crosshair hits.
            // If the ore is buried, the raycast hits the blocking dirt/stone first and clears it,
            // tunnelling straight to the ore (and gravity carries us down as we dig).
            AutoMineMod.pathing().cancel();
            BlockBreaker.mineTowards(mc, currentTarget);
            return;
        }

        // Not in reach to mine: collect any dropped items first so we don't leave them behind,
        // then resume walking to the ore. Items auto-collect once we path onto them.
        if (AutoMineMod.settings().mineCollectDrops) {
            ItemEntity drop = nearestDrop(mc, DROP_RANGE);
            if (drop != null && mc.player.distanceToSqr(drop) > 2.0) {
                BlockPos dp = drop.blockPosition();
                if (!(AutoMineMod.pathing().getGoal() instanceof GoalBlock gb)
                        || gb.x != dp.getX() || gb.y != dp.getY() || gb.z != dp.getZ()) {
                    AutoMineMod.pathing().setGoalAndPath(new GoalBlock(dp));
                }
                return;
            }
        }

        // Too far to reach: pathfind toward the ore. The path's movements break blocks en route,
        // so it tunnels through anything in the way until the ore comes within reach.
        if (!(AutoMineMod.pathing().getGoal() instanceof GoalNear g)
                || g.x != currentTarget.getX() || g.y != currentTarget.getY() || g.z != currentTarget.getZ()) {
            AutoMineMod.pathing().setGoalAndPath(
                    new GoalNear(currentTarget.getX(), currentTarget.getY(), currentTarget.getZ(), 1));
        }
    }

    private ItemEntity nearestDrop(Minecraft mc, double range) {
        var box = mc.player.getBoundingBox().inflate(range);
        ItemEntity best = null;
        double bestSq = Double.MAX_VALUE;
        for (ItemEntity e : mc.level.getEntitiesOfClass(ItemEntity.class, box)) {
            double d = e.distanceToSqr(mc.player);
            if (d < bestSq) {
                bestSq = d;
                best = e;
            }
        }
        return best;
    }

    private BlockPos findNearest(Minecraft mc) {
        BlockPos origin = mc.player.blockPosition();
        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;
        int minY = mc.level.getMinY();
        int maxY = mc.level.getMaxY();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                int wx = origin.getX() + dx;
                int wz = origin.getZ() + dz;
                if (!mc.level.hasChunk(wx >> 4, wz >> 4)) {
                    continue;
                }
                for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy++) {
                    int wy = origin.getY() + dy;
                    if (wy < minY || wy >= maxY) {
                        continue;
                    }
                    cursor.set(wx, wy, wz);
                    BlockState state = mc.level.getBlockState(cursor);
                    if (targets.contains(state.getBlock())) {
                        double d = origin.distSqr(cursor);
                        if (d < bestDistSq) {
                            bestDistSq = d;
                            best = cursor.immutable();
                        }
                    }
                }
            }
        }
        return best;
    }
}
