package com.automine.pathing.movement.movements;

import com.automine.pathing.movement.Movement;
import com.automine.utils.BetterBlockPos;

import java.util.List;

/** Pillar straight up by jumping and placing a block underneath. */
public class MovementPillar extends Movement {
    public MovementPillar(BetterBlockPos src, BetterBlockPos dest, double cost, List<BetterBlockPos> toBreak) {
        super(src, dest, cost, toBreak, src, true); // place under the source column
    }
}
