package com.automine.pathing.movement;

/**
 * Movement cost constants, expressed in game ticks. These mirror Baritone's values and are
 * the backbone of the cost model: a cheaper action is preferred, and the goal heuristics
 * are scaled by {@link #WALK_ONE_BLOCK_COST} so they stay admissible.
 */
public interface ActionCosts {

    // Player walks at 4.317 blocks/sec => 20/4.317 ticks per block.
    double WALK_ONE_BLOCK_COST = 20.0 / 4.317;            // ~4.633
    double WALK_ONE_IN_WATER_COST = 20.0 / 2.2;           // swimming is slower
    double SPRINT_ONE_BLOCK_COST = 20.0 / 5.612;          // ~3.564
    double WALK_OFF_BLOCK_COST = WALK_ONE_BLOCK_COST * 0.8;

    double JUMP_ONE_BLOCK_COST = WALK_ONE_BLOCK_COST + 3.0;    // ascending a block costs extra
    double LADDER_UP_ONE_COST = 20.0 / 2.35;
    double LADDER_DOWN_ONE_COST = 20.0 / 3.0;

    double SQRT_2 = Math.sqrt(2.0);

    // Falling: distance -> ticks, precomputed for the first several blocks (kinematics of gravity).
    double[] FALL_N_BLOCKS_COST = generateFallNBlocksCost();

    double FALL_1_25_BLOCKS_COST = distanceToTicks(1.25);
    double FALL_0_25_BLOCKS_COST = distanceToTicks(0.25);

    // Mining a block we don't know how to break quickly: large but finite penalty.
    double COST_INF = 1_000_000.0;

    static double[] generateFallNBlocksCost() {
        double[] costs = new double[4097];
        for (int i = 0; i < costs.length; i++) {
            costs[i] = distanceToTicks(i);
        }
        return costs;
    }

    /** Inverts the gravity kinematics: how many ticks to free-fall {@code blocks} blocks. */
    static double distanceToTicks(double blocks) {
        if (blocks == 0) {
            return 0;
        }
        double distance = 0;
        double velocity = 0;
        for (int ticks = 0; ; ticks++) {
            double prev = distance;
            velocity = (velocity + 0.08) * 0.98; // Minecraft gravity model
            distance += velocity;
            if (distance > blocks) {
                // linear-interpolate the fractional final tick
                return ticks + (blocks - prev) / (distance - prev);
            }
        }
    }
}
