package com.automine.goals;

import com.automine.pathing.movement.ActionCosts;

/** Reach an X,Z column at any Y. Used by {@code #goto x z}. */
public class GoalXZ implements Goal {

    public final int x;
    public final int z;

    public GoalXZ(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        return x == this.x && z == this.z;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        return calculate(x - this.x, z - this.z);
    }

    /**
     * Diagonal (octile) distance scaled by walking cost: moving diagonally is sqrt(2) blocks
     * but cheaper per axis, so we credit the diagonal portion and walk the remainder straight.
     */
    public static double calculate(double dx, double dz) {
        double x = Math.abs(dx);
        double z = Math.abs(dz);
        double straight;
        double diagonal;
        if (x < z) {
            straight = z - x;
            diagonal = x;
        } else {
            straight = x - z;
            diagonal = z;
        }
        return diagonal * ActionCosts.SQRT_2 * ActionCosts.WALK_ONE_BLOCK_COST
                + straight * ActionCosts.WALK_ONE_BLOCK_COST;
    }

    @Override
    public String toString() {
        return "GoalXZ{x=" + x + ", z=" + z + "}";
    }
}
