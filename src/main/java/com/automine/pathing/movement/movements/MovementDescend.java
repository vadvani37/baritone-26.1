package com.automine.pathing.movement.movements;

import com.automine.pathing.movement.Movement;
import com.automine.utils.BetterBlockPos;

import java.util.List;

/** Step down one or more blocks into an adjacent column (controlled descent / short fall). */
public class MovementDescend extends Movement {
    public MovementDescend(BetterBlockPos src, BetterBlockPos dest, double cost, List<BetterBlockPos> toBreak) {
        super(src, dest, cost, toBreak, null, false);
    }
}
