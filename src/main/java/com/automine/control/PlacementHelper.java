package com.automine.control;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Places a throwaway block at a target position by clicking a neighbouring solid face.
 * Used by pillar/ascend movements. Keeps placement logic isolated so it can be hardened
 * (block selection, scaffold settings) without touching the movement code.
 */
public final class PlacementHelper {

    /** Try to place a block at {@code target} against any adjacent solid face. */
    public void placeAt(Minecraft mc, BlockPos target) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }
        if (!selectPlaceableBlock(mc)) {
            return; // nothing to place with
        }
        for (Direction dir : Direction.values()) {
            BlockPos against = target.relative(dir);
            if (mc.level.getBlockState(against).isAir()) {
                continue; // need a solid face to click
            }
            Direction face = dir.getOpposite();
            Vec3 hit = Vec3.atCenterOf(against).add(
                    face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
            BlockHitResult result = new BlockHitResult(hit, face, against, false);
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, result);
            mc.player.swing(InteractionHand.MAIN_HAND);
            return;
        }
    }

    /** Selects a hotbar slot holding a placeable block. Returns false if none found. */
    private boolean selectPlaceableBlock(Minecraft mc) {
        var inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i).getItem() instanceof BlockItem) {
                inv.setSelectedSlot(i);
                return true;
            }
        }
        return false;
    }
}
