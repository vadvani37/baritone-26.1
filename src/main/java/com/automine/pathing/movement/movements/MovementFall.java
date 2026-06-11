package com.automine.pathing.movement.movements;

import com.automine.pathing.movement.Movement;
import com.automine.utils.BetterBlockPos;

import java.util.List;

/** Fall straight down (or after a step) multiple blocks to a safe landing. */
public class MovementFall extends Movement {
    public MovementFall(BetterBlockPos src, BetterBlockPos dest, double cost, List<BetterBlockPos> toBreak) {
        super(src, dest, cost, toBreak, null, false);
    }
}
