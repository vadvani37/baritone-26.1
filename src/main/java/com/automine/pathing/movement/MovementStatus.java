package com.automine.pathing.movement;

public enum MovementStatus {
    /** Still working toward the destination. */
    RUNNING,
    /** Reached the destination block. */
    SUCCESS,
    /** Something went wrong (blocked, fell off, etc.) — triggers a recalculation. */
    FAILED,
    /** Was never possible to begin with. */
    UNREACHABLE
}
