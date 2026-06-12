package com.automine.control;

import com.automine.utils.Rotation;
import com.automine.utils.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Correctly breaks blocks the way the vanilla "hold attack" does: aim at the target, then
 * raycast and damage whatever block is actually under the crosshair using its real exposed
 * face. This is what makes tunnelling work — when the real target is occluded (e.g. ore behind
 * dirt), the ray hits the obstruction first, so we break our way through to it instead of
 * uselessly swinging at a block the server won't let us reach.
 */
public final class BlockBreaker {

    /** Survival block-interaction range. Creative is a touch longer; 4.5 is safe for digging. */
    private static final double REACH = 4.5;

    private BlockBreaker() {
    }

    /**
     * Look at {@code lookAt} and break the first block the crosshair hits within reach.
     * Returns the block actually being mined, or null if nothing reachable is in the way.
     */
    public static BlockPos mineTowards(Minecraft mc, BlockPos lookAt) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.gameMode == null) {
            return null;
        }

        Vec3 eyes = player.getEyePosition(1.0f);
        Vec3 targetCenter = Vec3.atCenterOf(lookAt);
        Rotation rot = RotationUtils.calcRotationFromVec3d(eyes, targetCenter);
        player.setYRot(rot.yaw());
        player.yHeadRot = rot.yaw();
        player.yBodyRot = rot.yaw();
        player.setXRot(rot.pitch());

        // Raycast along where we are now looking, up to reach distance.
        Vec3 view = player.getViewVector(1.0f);
        Vec3 end = eyes.add(view.x * REACH, view.y * REACH, view.z * REACH);
        BlockHitResult hit = mc.level.clip(
                new ClipContext(eyes, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null; // nothing within reach along the look vector
        }

        BlockPos hitPos = hit.getBlockPos();
        Direction face = hit.getDirection();
        net.minecraft.world.level.block.state.BlockState state = mc.level.getBlockState(hitPos);
        if (state.isAir()) {
            return null;
        }
        // Auto-swap to the best tool for this block (pickaxe/shovel/axe/..., or fists).
        if (com.automine.AutoMineMod.settings().autoTool) {
            ToolSelector.selectBestTool(mc, state);
        }
        // continueDestroyBlock auto-starts on a new target and advances destroy progress each tick.
        mc.gameMode.continueDestroyBlock(hitPos, face);
        player.swing(InteractionHand.MAIN_HAND);
        return hitPos;
    }
}
