package com.automine.pathing.calc;

import com.automine.goals.Goal;
import com.automine.pathing.movement.CalculationContext;
import com.automine.pathing.movement.Movement;
import com.automine.pathing.movement.Moves;
import com.automine.pathing.path.Path;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A* over the voxel grid. Standard f = g + h with an admissible heuristic from the {@link Goal}.
 * Bounded by a node budget and a wall-clock timeout so it never hangs the calculation thread;
 * if the goal isn't reached in budget it returns the most-promising partial path so the bot
 * still makes forward progress (incremental pathing, like Baritone).
 */
public final class AStarPathFinder {

    /** Heuristic weight. 1.0 is optimal A*; slightly >1 trades optimality for speed (like Baritone). */
    private static final double HEURISTIC_WEIGHT = 1.0;

    private final int startX;
    private final int startY;
    private final int startZ;
    private final Goal goal;
    private final CalculationContext context;

    private final Map<Long, PathNode> allNodes = new HashMap<>(2048);
    private final BinaryHeapOpenSet openSet = new BinaryHeapOpenSet();

    public AStarPathFinder(int startX, int startY, int startZ, Goal goal, CalculationContext context) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.goal = goal;
        this.context = context;
    }

    public Optional<Path> calculate(long maxNodes, long timeoutMs) {
        long startTime = System.currentTimeMillis();

        PathNode start = getNode(startX, startY, startZ);
        start.cost = 0;
        start.combinedCost = start.estimatedCostToGoal * HEURISTIC_WEIGHT;
        openSet.insert(start);

        PathNode bestSoFar = start;
        double bestHeuristic = start.estimatedCostToGoal;

        long numNodes = 0;
        while (!openSet.isEmpty() && numNodes < maxNodes) {
            if ((numNodes & 0x3FF) == 0 && System.currentTimeMillis() - startTime > timeoutMs) {
                break; // out of time — fall through to best partial path
            }
            PathNode current = openSet.removeLowest();
            numNodes++;

            if (goal.isInGoal(current.x, current.y, current.z)) {
                return Optional.of(Path.reconstruct(start, current, goal));
            }

            List<Movement> movements = Moves.getMovements(context, current.x, current.y, current.z);
            for (Movement movement : movements) {
                double actionCost = movement.cost();
                if (actionCost <= 0 || Double.isInfinite(actionCost) || actionCost >= 1_000_000.0) {
                    continue;
                }
                var dest = movement.getDest();
                PathNode neighbor = getNode(dest.x, dest.y, dest.z);
                double tentativeCost = current.cost + actionCost;
                if (tentativeCost < neighbor.cost) {
                    neighbor.cost = tentativeCost;
                    neighbor.combinedCost = tentativeCost + neighbor.estimatedCostToGoal * HEURISTIC_WEIGHT;
                    neighbor.previous = current;
                    neighbor.previousMovement = movement;
                    if (neighbor.isOpen()) {
                        openSet.update(neighbor);
                    } else {
                        openSet.insert(neighbor);
                    }
                    if (neighbor.estimatedCostToGoal < bestHeuristic) {
                        bestHeuristic = neighbor.estimatedCostToGoal;
                        bestSoFar = neighbor;
                    }
                }
            }
        }

        // No complete path; hand back the best partial route if we actually moved toward the goal.
        if (bestSoFar != start) {
            return Optional.of(Path.reconstruct(start, bestSoFar, goal));
        }
        return Optional.empty();
    }

    private PathNode getNode(int x, int y, int z) {
        long hash = BlockPos.asLong(x, y, z);
        PathNode node = allNodes.get(hash);
        if (node == null) {
            node = new PathNode(x, y, z, hash, goal);
            allNodes.put(hash, node);
        }
        return node;
    }
}
