package com.automine.pathing.movement.movements;

import com.automine.pathing.movement.Movement;
import com.automine.utils.BetterBlockPos;

import java.util.List;

/** Move diagonally across a corner (cheaper per block than two cardinal steps). */
public class MovementDiagonal extends Movement {
    public MovementDiagonal(BetterBlockPos src, BetterBlockPos dest, double cost, List<BetterBlockPos> toBreak) {
        super(src, dest, cost, toBreak, null, false);
    }
}
