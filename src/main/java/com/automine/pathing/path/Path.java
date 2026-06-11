package com.automine.pathing.path;

import com.automine.goals.Goal;
import com.automine.pathing.calc.PathNode;
import com.automine.pathing.movement.Movement;
import com.automine.utils.BetterBlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Concrete path produced by reconstructing the A* parent chain from goal back to start. */
public final class Path implements IPath {

    private final List<BetterBlockPos> positions;
    private final List<Movement> movements;
    private final Goal goal;

    private Path(List<BetterBlockPos> positions, List<Movement> movements, Goal goal) {
        this.positions = positions;
        this.movements = movements;
        this.goal = goal;
    }

    /** Walk the parent links from {@code end} back to the start and reverse into forward order. */
    public static Path reconstruct(PathNode start, PathNode end, Goal goal) {
        List<BetterBlockPos> positions = new ArrayList<>();
        List<Movement> movements = new ArrayList<>();
        PathNode current = end;
        while (current != null) {
            positions.add(new BetterBlockPos(current.x, current.y, current.z));
            if (current.previousMovement != null) {
                movements.add(current.previousMovement);
            }
            current = current.previous;
        }
        Collections.reverse(positions);
        Collections.reverse(movements);
        return new Path(positions, movements, goal);
    }

    @Override
    public List<BetterBlockPos> positions() {
        return positions;
    }

    @Override
    public List<Movement> movements() {
        return movements;
    }

    @Override
    public BetterBlockPos getSrc() {
        return positions.get(0);
    }

    @Override
    public BetterBlockPos getDest() {
        return positions.get(positions.size() - 1);
    }

    public Goal getGoal() {
        return goal;
    }

    @Override
    public int length() {
        return positions.size();
    }
}
