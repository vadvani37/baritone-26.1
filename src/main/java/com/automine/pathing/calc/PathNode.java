package com.automine.pathing.calc;

import com.automine.goals.Goal;
import com.automine.pathing.movement.Movement;

/**
 * A node in the A* search graph. Holds the running g-cost (cost from start), the cached
 * heuristic, the f-cost used for ordering, the parent link for reconstruction, and the
 * movement edge taken to arrive here (so the executor can replay it).
 */
public final class PathNode {

    public final int x;
    public final int y;
    public final int z;
    public final long hashCode;

    /** Cost from start to this node. */
    public double cost;
    /** Admissible estimate of remaining cost to the goal. */
    public final double estimatedCostToGoal;
    /** f = cost + estimatedCostToGoal. */
    public double combinedCost;

    public PathNode previous;
    public Movement previousMovement;

    /** Index in the binary heap, or -1 if not present. */
    public int heapPosition = -1;

    public PathNode(int x, int y, int z, long hash, Goal goal) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.hashCode = hash;
        this.cost = Double.MAX_VALUE;
        this.estimatedCostToGoal = goal.heuristic(x, y, z);
        this.combinedCost = Double.MAX_VALUE;
    }

    public boolean isOpen() {
        return heapPosition != -1;
    }
}
