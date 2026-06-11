package com.automine.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import java.util.Set;

/** Finds the nearest block of a given type in currently-loaded chunks around the player. */
public final class WorldScanner {

    private WorldScanner() {
    }

    /** Nearest block whose type is in {@code targets} (loaded chunks only). */
    public static BlockPos nearestAny(Set<Block> targets, int radius) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || targets.isEmpty()) {
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
                    if (targets.contains(mc.level.getBlockState(cursor).getBlock())) {
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

    /**
     * Finds the nearest column whose biome matches {@code biomeId} (e.g. "desert" or
     * "minecraft:plains") within loaded chunks. Biomes are stored per 4x4x4 cell, so we sample
     * every 4 blocks at the player's Y — plenty of resolution, far cheaper than every block.
     * Client-side, this only sees chunks the server has actually sent you.
     */
    public static BlockPos nearestBiome(String biomeId, int radius) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return null;
        }
        String want = biomeId.contains(":") ? biomeId : "minecraft:" + biomeId;
        BlockPos origin = mc.player.blockPosition();
        int y = origin.getY();
        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx += 4) {
            for (int dz = -radius; dz <= radius; dz += 4) {
                int wx = origin.getX() + dx;
                int wz = origin.getZ() + dz;
                if (!mc.level.hasChunk(wx >> 4, wz >> 4)) {
                    continue;
                }
                cursor.set(wx, y, wz);
                String id = mc.level.getBiome(cursor).unwrapKey()
                        .map(k -> k.identifier().toString()).orElse("");
                if (id.equals(want)) {
                    double d = origin.distSqr(cursor);
                    if (d < bestDistSq) {
                        bestDistSq = d;
                        best = cursor.immutable();
                    }
                }
            }
        }
        return best;
    }
}
