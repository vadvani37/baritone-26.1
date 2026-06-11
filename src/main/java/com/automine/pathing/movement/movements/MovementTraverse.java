package com.automine.pathing.movement.movements;

import com.automine.pathing.movement.Movement;
import com.automine.utils.BetterBlockPos;

import java.util.List;

/** Walk one block horizontally on flat ground (optionally breaking the doorway blocks). */
public class MovementTraverse extends Movement {
    public MovementTraverse(BetterBlockPos src, BetterBlockPos dest, double cost, List<BetterBlockPos> toBreak) {
        super(src, dest, cost, toBreak, null, false);
    }
}
