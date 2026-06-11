package com.automine.pathing.movement.movements;

import com.automine.pathing.movement.Movement;
import com.automine.utils.BetterBlockPos;

import java.util.List;

/** Mine the block directly below and drop into it. */
public class MovementDownward extends Movement {
    public MovementDownward(BetterBlockPos src, BetterBlockPos dest, double cost, List<BetterBlockPos> toBreak) {
        super(src, dest, cost, toBreak, null, false);
    }
}
