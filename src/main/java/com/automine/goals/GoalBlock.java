package com.automine.goals;

import com.automine.pathing.movement.ActionCosts;
import net.minecraft.core.BlockPos;

/** Get to an exact block position. The bread-and-butter goal behind {@code #goto x y z}. */
public class GoalBlock implements Goal {

    public final int x;
    public final int y;
    public final int z;

    public GoalBlock(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public GoalBlock(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        return x == this.x && y == this.y && z == this.z;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        int dx = x - this.x;
        int dy = y - this.y;
        int dz = z - this.z;
        return calculate(dx, dy, dz);
    }

    public static double calculate(double dx, double dy, double dz) {
        double horizontal = GoalXZ.calculate(dx, dz);
        // Vertical: ascending and descending have different per-block costs.
        double verticalCost;
        if (dy > 0) {
            verticalCost = dy * ActionCosts.JUMP_ONE_BLOCK_COST;
        } else {
            verticalCost = -dy * ActionCosts.FALL_N_BLOCKS_COST[2] / 2.0;
        }
        return horizontal + verticalCost;
    }

    @Override
    public String toString() {
        return "GoalBlock{x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
