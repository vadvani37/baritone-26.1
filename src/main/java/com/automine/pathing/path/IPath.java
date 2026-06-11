package com.automine.pathing.path;

import com.automine.pathing.movement.Movement;
import com.automine.utils.BetterBlockPos;

import java.util.List;

/** A computed path: the sequence of positions and the movement edges connecting them. */
public interface IPath {

    List<BetterBlockPos> positions();

    List<Movement> movements();

    BetterBlockPos getSrc();

    BetterBlockPos getDest();

    int length();
}
