package com.automine.pathing.movement.movements;

import com.automine.pathing.movement.Movement;
import com.automine.utils.BetterBlockPos;

import java.util.List;

/** Step up one block (jump) into an adjacent column. */
public class MovementAscend extends Movement {
    public MovementAscend(BetterBlockPos src, BetterBlockPos dest, double cost, List<BetterBlockPos> toBreak) {
        super(src, dest, cost, toBreak, null, true);
    }
}
