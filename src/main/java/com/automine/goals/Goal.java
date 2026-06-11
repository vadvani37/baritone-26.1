package com.automine.goals;

/**
 * A goal the pathfinder tries to reach. {@link #isInGoal} terminates the search;
 * {@link #heuristic} guides A* toward the goal and MUST be admissible (never overestimate
 * the true remaining cost) for A* to return optimal paths.
 */
public interface Goal {

    boolean isInGoal(int x, int y, int z);

    double heuristic(int x, int y, int z);

    default double heuristic() {
        return 0;
    }
}
