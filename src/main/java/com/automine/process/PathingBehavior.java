package com.automine.process;

import com.automine.AutoMineMod;
import com.automine.control.InputOverrideHandler;
import com.automine.goals.Goal;
import com.automine.pathing.calc.AStarPathFinder;
import com.automine.pathing.movement.CalculationContext;
import com.automine.pathing.movement.Movement;
import com.automine.pathing.movement.MovementStatus;
import com.automine.pathing.path.Path;
import com.automine.utils.BetterBlockPos;
import com.automine.utils.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Owns the active goal and path, recalculates as needed, and drives the current movement each
 * tick. Calculation runs on a background thread (snapshotting the world reference first) so a
 * long search never freezes the client; the result is consumed on the next client tick.
 */
public final class PathingBehavior {

    private final ExecutorService calcThread =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "AutoMine-Pathfinder");
                t.setDaemon(true);
                return t;
            });

    private volatile Goal goal;
    private volatile Path path;
    private int movementIndex;
    private CompletableFuture<Optional<Path>> pending;

    public void setGoalAndPath(Goal goal) {
        this.goal = goal;
        this.path = null;
        this.movementIndex = 0;
        AutoMineMod.inputHandler().setControlling(true);
        Helper.log("Goal set: " + goal + ". Calculating...");
    }

    public void cancel() {
        this.goal = null;
        this.path = null;
        this.movementIndex = 0;
        if (pending != null) {
            pending.cancel(true);
            pending = null;
        }
        AutoMineMod.inputHandler().setControlling(false);
    }

    public boolean isActive() {
        return goal != null;
    }

    public Goal getGoal() {
        return goal;
    }

    public Path getPath() {
        return path;
    }

    /** Called every client tick (early, before the game reads input). */
    public void onTick() {
        // Idle: never touch the player's own inputs.
        if (goal == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        InputOverrideHandler input = AutoMineMod.inputHandler();
        input.clearAll();

        try {
            if (mc.player == null || mc.level == null) {
                return;
            }

            // Consume a finished background calculation.
            if (pending != null && pending.isDone()) {
                Optional<Path> result = pending.getNow(Optional.empty());
                pending = null;
                if (result.isPresent()) {
                    path = result.get();
                    movementIndex = 0;
                    Helper.log("Path found: " + path.length() + " nodes.");
                } else {
                    Helper.logError("No path to goal.");
                    cancel();
                    return;
                }
            }

            BlockPos feet = mc.player.blockPosition();
            if (goal.isInGoal(feet.getX(), feet.getY(), feet.getZ())) {
                Helper.log("Goal reached.");
                cancel();
                return;
            }

            // Need a (re)calculation?
            if (path == null) {
                if (pending == null) {
                    startCalculation(mc, feet);
                }
                return;
            }

            executePath(mc, feet);
        } finally {
            input.apply(); // push input state before the game reads it
        }
    }

    private void executePath(Minecraft mc, BlockPos feet) {
        if (movementIndex >= path.movements().size()) {
            // Walked the whole path but not in goal — recalc from here.
            path = null;
            return;
        }
        Movement current = path.movements().get(movementIndex);

        // Off-path recovery: if we're far from where this movement starts, recalculate.
        BetterBlockPos expected = current.getSrc();
        if (manhattan(feet, expected) > 2 && manhattan(feet, current.getDest()) > 2) {
            path = null;
            return;
        }

        MovementStatus status = current.tick();
        switch (status) {
            case SUCCESS -> movementIndex++;
            case FAILED, UNREACHABLE -> path = null; // recalc next tick
            case RUNNING -> { /* keep going */ }
        }
    }

    private void startCalculation(Minecraft mc, BlockPos feet) {
        Goal g = this.goal;
        CalculationContext ctx = new CalculationContext(mc); // snapshot world reference on main thread
        int sx = feet.getX(), sy = feet.getY(), sz = feet.getZ();
        long maxNodes = AutoMineMod.settings().maxNodes;
        long timeout = AutoMineMod.settings().planningTimeoutMs;
        pending = CompletableFuture.supplyAsync(
                () -> new AStarPathFinder(sx, sy, sz, g, ctx).calculate(maxNodes, timeout),
                calcThread);
    }

    private static int manhattan(BlockPos a, BetterBlockPos b) {
        return Math.abs(a.getX() - b.x) + Math.abs(a.getY() - b.y) + Math.abs(a.getZ() - b.z);
    }
}
