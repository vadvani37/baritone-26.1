package com.automine.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

/** Finds the nearest block of a given type in currently-loaded chunks around the player. */
public final class WorldScanner {

    private WorldScanner() {
    }

    public static BlockPos nearest(Block target, int radius) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return null;
        }
        BlockPos origin = mc.player.blockPosition();
        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;
        int minY = mc.level.getMinY();
        int maxY = mc.level.getMaxY();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int wx = origin.getX() + dx;
                int wz = origin.getZ() + dz;
                if (!mc.level.hasChunk(wx >> 4, wz >> 4)) {
                    continue;
                }
                for (int dy = -radius; dy <= radius; dy++) {
                    int wy = origin.getY() + dy;
                    if (wy < minY || wy >= maxY) {
                        continue;
                    }
                    cursor.set(wx, wy, wz);
                    if (mc.level.getBlockState(cursor).getBlock() == target) {
                        double d = origin.distSqr(cursor);
                        if (d < bestDistSq) {
                            bestDistSq = d;
                            best = cursor.immutable();
                        }
                    }
                }
            }
        }
        return best;
    }
}
