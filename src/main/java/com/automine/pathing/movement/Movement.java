package com.automine.pathing.movement;

import com.automine.AutoMineMod;
import com.automine.control.InputOverrideHandler;
import com.automine.control.InputOverrideHandler.Input;
import com.automine.utils.BetterBlockPos;
import com.automine.utils.Rotation;
import com.automine.utils.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * One edge in the path: a single block-to-block transition. Subclasses supply the cost and
 * geometry; the shared {@link #tick} drives execution by (1) breaking any obstructing blocks,
 * then (2) steering the player toward the destination centre, jumping/placing as declared.
 */
public abstract class Movement {

    protected final BetterBlockPos src;
    protected final BetterBlockPos dest;
    protected final double cost;

    /** Blocks that must be cleared before/while moving (e.g. the two blocks of the doorway). */
    protected final List<BetterBlockPos> toBreak;
    /** Block to place to support the move (pillar/ascend); null if none. */
    protected final BetterBlockPos toPlace;
    protected final boolean requiresJump;

    protected Movement(BetterBlockPos src, BetterBlockPos dest, double cost,
                       List<BetterBlockPos> toBreak, BetterBlockPos toPlace, boolean requiresJump) {
        this.src = src;
        this.dest = dest;
        this.cost = cost;
        this.toBreak = toBreak;
        this.toPlace = toPlace;
        this.requiresJump = requiresJump;
    }

    public double cost() {
        return cost;
    }

    public BetterBlockPos getSrc() {
        return src;
    }

    public BetterBlockPos getDest() {
        return dest;
    }

    /** Drives one tick of execution. Called by the path executor while this is the active edge. */
    public MovementStatus tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return MovementStatus.FAILED;
        }
        InputOverrideHandler input = AutoMineMod.inputHandler();
        input.clearAll();

        // 1) Clear obstructions first.
        for (BetterBlockPos breakPos : toBreak) {
            if (!isPassable(mc, breakPos)) {
                lookAt(player, breakPos);
                input.setInput(Input.CLICK_LEFT, true); // mine
                mineBlock(mc, breakPos);
                return MovementStatus.RUNNING;
            }
        }

        // 2) Reached destination?
        BlockPos feet = player.blockPosition();
        if (feet.getX() == dest.x && feet.getZ() == dest.z
                && Math.abs(feet.getY() - dest.y) <= 0 && (requiresJump ? player.onGround() : true)) {
            return MovementStatus.SUCCESS;
        }

        // 3) Steer toward destination centre.
        Vec3 destCenter = new Vec3(dest.x + 0.5, dest.y, dest.z + 0.5);
        Rotation rot = RotationUtils.calcRotationFromVec3d(player.getEyePosition(1.0f), destCenter);
        player.setYRot(rot.yaw());
        player.yHeadRot = rot.yaw();
        player.setXRot(rot.pitch());

        input.setInput(Input.MOVE_FORWARD, true);
        if (AutoMineMod.settings().allowSprint) {
            input.setInput(Input.SPRINT, true);
        }
        if (requiresJump || dest.y > src.y) {
            input.setInput(Input.JUMP, true);
            // Pillar: place a block beneath ourselves to gain height.
            if (toPlace != null && player.onGround()) {
                placeBlock(mc, toPlace);
            }
        }
        return MovementStatus.RUNNING;
    }

    protected boolean isPassable(Minecraft mc, BetterBlockPos pos) {
        var state = mc.level.getBlockState(pos);
        return state.isAir() || state.getCollisionShape(mc.level, pos).isEmpty();
    }

    protected void lookAt(LocalPlayer player, BlockPos pos) {
        Rotation rot = RotationUtils.calcRotationFromVec3d(player.getEyePosition(1.0f), pos);
        player.setYRot(rot.yaw());
        player.yHeadRot = rot.yaw();
        player.setXRot(rot.pitch());
    }

    protected void mineBlock(Minecraft mc, BlockPos pos) {
        if (mc.gameMode == null) {
            return;
        }
        Direction face = mc.player.getDirection().getOpposite();
        mc.gameMode.continueDestroyBlock(pos, face);
        mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
    }

    protected void placeBlock(Minecraft mc, BlockPos pos) {
        // A full implementation selects a throwaway block and uses gameMode.useItemOn against a
        // neighbouring face. Left as a focused extension point.
        AutoMineMod.placementHelper().placeAt(mc, pos);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + src + " -> " + dest + " cost=" + String.format("%.1f", cost) + "}";
    }
}
