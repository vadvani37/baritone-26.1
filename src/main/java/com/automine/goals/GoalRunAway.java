package com.automine.goals;

import net.minecraft.core.BlockPos;

/** Move AWAY from one or more points (negated heuristic). Backs {@code #thisway} / flee. */
public class GoalRunAway implements Goal {

    private final BlockPos[] from;
    private final double distanceSq;

    public GoalRunAway(double distance, BlockPos... from) {
        this.from = from;
        this.distanceSq = distance * distance;
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        for (BlockPos p : from) {
            int dx = x - p.getX();
            int dz = z - p.getZ();
            if (dx * dx + dz * dz < distanceSq) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        double min = Double.MAX_VALUE;
        for (BlockPos p : from) {
            double dx = x - p.getX();
            double dz = z - p.getZ();
            min = Math.min(min, dx * dx + dz * dz);
        }
        // Larger distance => smaller (better) heuristic.
        return 1000.0 / Math.sqrt(min + 1);
    }

    @Override
    public String toString() {
        return "GoalRunAway{points=" + from.length + "}";
    }
}
