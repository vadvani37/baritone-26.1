package com.automine.goals;

import com.automine.pathing.movement.ActionCosts;

/** Reach a Y level at any X,Z. Used by {@code #goto <y>} and {@code #elytra}-style descents. */
public class GoalYLevel implements Goal {

    public final int level;

    public GoalYLevel(int level) {
        this.level = level;
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        return y == level;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        return calculate(level, y);
    }

    public static double calculate(int goalY, int currentY) {
        if (currentY > goalY) {
            return ActionCosts.FALL_N_BLOCKS_COST[2] / 2.0 * (currentY - goalY);
        }
        if (currentY < goalY) {
            return ActionCosts.JUMP_ONE_BLOCK_COST * (goalY - currentY);
        }
        return 0;
    }

    @Override
    public String toString() {
        return "GoalYLevel{y=" + level + "}";
    }
}
