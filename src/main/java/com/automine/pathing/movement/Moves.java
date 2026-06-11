package com.automine.pathing.movement;

import com.automine.pathing.movement.movements.*;
import com.automine.utils.BetterBlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates the candidate edges (movements) out of a node, each with an admissible cost.
 * This is the successor function A* expands. Correctness of pathing lives here: every move
 * checks headroom, footing, fall safety and break/place permissions before it is offered.
 */
public final class Moves implements ActionCosts {

    public static final double PLACE_ONE_BLOCK_COST = 20.0;

    private static final int[][] CARDINALS = {{0, -1}, {0, 1}, {1, 0}, {-1, 0}};
    private static final int[][] DIAGONALS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    private Moves() {
    }

    /** All legal movements leaving the node whose feet are at (x,y,z). */
    public static List<Movement> getMovements(CalculationContext ctx, int x, int y, int z) {
        List<Movement> out = new ArrayList<>();
        BetterBlockPos src = new BetterBlockPos(x, y, z);

        for (int[] d : CARDINALS) {
            addTraverse(ctx, src, d[0], d[1], out);
            addAscend(ctx, src, d[0], d[1], out);
            addDescendOrFall(ctx, src, d[0], d[1], out);
            addParkour(ctx, src, d[0], d[1], out);
        }
        for (int[] d : DIAGONALS) {
            addDiagonal(ctx, src, d[0], d[1], out);
        }
        addPillar(ctx, src, out);
        addDownward(ctx, src, out);
        return out;
    }

    private static void addTraverse(CalculationContext ctx, BetterBlockPos s, int dx, int dz, List<Movement> out) {
        int nx = s.x + dx, nz = s.z + dz;
        if (!MovementHelper.canWalkOn(ctx, nx, s.y - 1, nz)) {
            return; // nothing to stand on at the same level
        }
        double breakCost = 0;
        List<BetterBlockPos> toBreak = new ArrayList<>(2);
        breakCost += clearance(ctx, nx, s.y, nz, toBreak);
        breakCost += clearance(ctx, nx, s.y + 1, nz, toBreak);
        if (breakCost >= COST_INF) {
            return;
        }
        double walk = ctx.allowSprint ? SPRINT_ONE_BLOCK_COST : WALK_ONE_BLOCK_COST;
        out.add(new MovementTraverse(s, new BetterBlockPos(nx, s.y, nz), walk + breakCost, toBreak));
    }

    private static void addAscend(CalculationContext ctx, BetterBlockPos s, int dx, int dz, List<Movement> out) {
        int nx = s.x + dx, nz = s.z + dz;
        if (!MovementHelper.canWalkOn(ctx, nx, s.y, nz)) {
            return; // need a block one higher to step onto
        }
        List<BetterBlockPos> toBreak = new ArrayList<>(3);
        double breakCost = 0;
        breakCost += clearance(ctx, s.x, s.y + 2, s.z, toBreak);   // headroom to jump
        breakCost += clearance(ctx, nx, s.y + 1, nz, toBreak);
        breakCost += clearance(ctx, nx, s.y + 2, nz, toBreak);
        if (breakCost >= COST_INF) {
            return;
        }
        out.add(new MovementAscend(s, new BetterBlockPos(nx, s.y + 1, nz), JUMP_ONE_BLOCK_COST + breakCost, toBreak));
    }

    private static void addDescendOrFall(CalculationContext ctx, BetterBlockPos s, int dx, int dz, List<Movement> out) {
        int nx = s.x + dx, nz = s.z + dz;
        List<BetterBlockPos> toBreak = new ArrayList<>(2);
        double breakCost = 0;
        breakCost += clearance(ctx, nx, s.y, nz, toBreak);
        breakCost += clearance(ctx, nx, s.y + 1, nz, toBreak);
        if (breakCost >= COST_INF) {
            return;
        }
        // Scan downward for the landing surface.
        for (int fall = 1; fall <= ctx.maxFallHeightNoWater + 1; fall++) {
            int landY = s.y - fall;
            if (MovementHelper.canWalkOn(ctx, nx, landY - 1, nz)) {
                if (!MovementHelper.canWalkThrough(ctx, nx, landY, nz)) {
                    return; // landing block itself is obstructed
                }
                double cost = WALK_OFF_BLOCK_COST + FALL_N_BLOCKS_COST[fall] + breakCost;
                BetterBlockPos dest = new BetterBlockPos(nx, landY, nz);
                if (fall == 1) {
                    out.add(new MovementDescend(s, dest, cost, toBreak));
                } else {
                    out.add(new MovementFall(s, dest, cost, toBreak));
                }
                return;
            }
            if (!MovementHelper.canWalkThrough(ctx, nx, landY, nz)) {
                return; // hit something solid that isn't a valid floor
            }
        }
    }

    private static void addDiagonal(CalculationContext ctx, BetterBlockPos s, int dx, int dz, List<Movement> out) {
        int nx = s.x + dx, nz = s.z + dz;
        if (!MovementHelper.canWalkOn(ctx, nx, s.y - 1, nz)) {
            return;
        }
        // Destination clearance.
        if (!MovementHelper.canWalkThrough(ctx, nx, s.y, nz) || !MovementHelper.canWalkThrough(ctx, nx, s.y + 1, nz)) {
            return; // no breaking on diagonals
        }
        // Both adjacent cells must be open so we don't clip a corner.
        boolean side1 = MovementHelper.canWalkThrough(ctx, s.x + dx, s.y, s.z)
                && MovementHelper.canWalkThrough(ctx, s.x + dx, s.y + 1, s.z);
        boolean side2 = MovementHelper.canWalkThrough(ctx, s.x, s.y, s.z + dz)
                && MovementHelper.canWalkThrough(ctx, s.x, s.y + 1, s.z + dz);
        if (!side1 || !side2) {
            return;
        }
        double walk = (ctx.allowSprint ? SPRINT_ONE_BLOCK_COST : WALK_ONE_BLOCK_COST) * SQRT_2;
        out.add(new MovementDiagonal(s, new BetterBlockPos(nx, s.y, nz), walk, Collections.emptyList()));
    }

    private static void addParkour(CalculationContext ctx, BetterBlockPos s, int dx, int dz, List<Movement> out) {
        if (!ctx.allowParkour || !ctx.allowSprint) {
            return;
        }
        // Only over a genuine gap: the adjacent floor must be missing.
        if (MovementHelper.canWalkOn(ctx, s.x + dx, s.y - 1, s.z + dz)) {
            return;
        }
        for (int dist = 2; dist <= 4; dist++) {
            int nx = s.x + dx * dist, nz = s.z + dz * dist;
            // Air all the way across at body height.
            boolean clear = true;
            for (int j = 1; j < dist; j++) {
                if (!MovementHelper.canWalkThrough(ctx, s.x + dx * j, s.y, s.z + dz * j)
                        || !MovementHelper.canWalkThrough(ctx, s.x + dx * j, s.y + 1, s.z + dz * j)) {
                    clear = false;
                    break;
                }
            }
            if (!clear) {
                break;
            }
            if (MovementHelper.canWalkOn(ctx, nx, s.y - 1, nz)
                    && MovementHelper.canWalkThrough(ctx, nx, s.y, nz)
                    && MovementHelper.canWalkThrough(ctx, nx, s.y + 1, nz)) {
                double cost = SPRINT_ONE_BLOCK_COST * dist + 2.0;
                out.add(new MovementParkour(s, new BetterBlockPos(nx, s.y, nz), cost, Collections.emptyList()));
                return; // shortest valid jump wins
            }
        }
    }

    private static void addPillar(CalculationContext ctx, BetterBlockPos s, List<Movement> out) {
        if (!ctx.allowPlace) {
            return;
        }
        List<BetterBlockPos> toBreak = new ArrayList<>(1);
        double breakCost = clearance(ctx, s.x, s.y + 2, s.z, toBreak); // headroom
        if (breakCost >= COST_INF) {
            return;
        }
        out.add(new MovementPillar(s, s.up(), JUMP_ONE_BLOCK_COST + PLACE_ONE_BLOCK_COST + breakCost, toBreak));
    }

    private static void addDownward(CalculationContext ctx, BetterBlockPos s, List<Movement> out) {
        if (!ctx.allowBreak) {
            return;
        }
        double mine = MovementHelper.getMiningDurationTicks(ctx, s.x, s.y - 1, s.z, false);
        if (mine >= COST_INF) {
            return;
        }
        if (!MovementHelper.canWalkOn(ctx, s.x, s.y - 2, s.z)) {
            return; // don't mine into a void
        }
        List<BetterBlockPos> toBreak = new ArrayList<>(1);
        toBreak.add(s.down());
        out.add(new MovementDownward(s, s.down(), mine + FALL_N_BLOCKS_COST[1], toBreak));
    }

    /**
     * Returns the cost to make (x,y,z) passable, adding it to {@code toBreak} if it must be mined.
     * Returns {@link #COST_INF} if it cannot be cleared.
     */
    private static double clearance(CalculationContext ctx, int x, int y, int z, List<BetterBlockPos> toBreak) {
        if (MovementHelper.canWalkThrough(ctx, x, y, z)) {
            return 0;
        }
        double mine = MovementHelper.getMiningDurationTicks(ctx, x, y, z, true);
        if (mine >= COST_INF) {
            return COST_INF;
        }
        toBreak.add(new BetterBlockPos(x, y, z));
        return mine;
    }
}
