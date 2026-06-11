package com.automine.goals;

/** Get within a given radius of a block. Used by follow/come and {@code #goto <block> near}. */
public class GoalNear implements Goal {

    public final int x;
    public final int y;
    public final int z;
    public final int rangeSq;

    public GoalNear(int x, int y, int z, int range) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rangeSq = range * range;
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        int dx = x - this.x;
        int dy = y - this.y;
        int dz = z - this.z;
        return dx * dx + dy * dy + dz * dz <= rangeSq;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        return GoalBlock.calculate(x - this.x, y - this.y, z - this.z);
    }

    @Override
    public String toString() {
        return "GoalNear{x=" + x + ", y=" + y + ", z=" + z + ", rangeSq=" + rangeSq + "}";
    }
}
