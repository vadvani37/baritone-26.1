package com.automine.process;

import com.automine.AutoMineMod;
import com.automine.goals.GoalNear;
import com.automine.utils.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
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

        double dist = Math.sqrt(mc.player.distanceToSqr(
                currentTarget.getX() + 0.5, currentTarget.getY() + 0.5, currentTarget.getZ() + 0.5));
        if (dist <= REACH) {
            // In reach: mine it directly.
            AutoMineMod.pathing().cancel(); // stop walking, hold position
            mineDirectly(mc, currentTarget);
        } else {
            // Walk adjacent to it.
            if (!(AutoMineMod.pathing().getGoal() instanceof GoalNear g)
                    || g.x != currentTarget.getX() || g.y != currentTarget.getY() || g.z != currentTarget.getZ()) {
                AutoMineMod.pathing().setGoalAndPath(
                        new GoalNear(currentTarget.getX(), currentTarget.getY(), currentTarget.getZ(), 1));
            }
        }
    }

    private void mineDirectly(Minecraft mc, BlockPos pos) {
        var rot = com.automine.utils.RotationUtils.calcRotationFromVec3d(mc.player.getEyePosition(1.0f), pos);
        mc.player.setYRot(rot.yaw());
        mc.player.yHeadRot = rot.yaw();
        mc.player.setXRot(rot.pitch());
        if (mc.gameMode != null) {
            mc.gameMode.continueDestroyBlock(pos, Direction.UP);
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
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
