package com.automine.utils;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Read-only access to the world for the pathfinder. Centralising this means the cost
 * functions all see the same view of the world and out-of-bounds reads degrade gracefully
 * to air rather than throwing.
 */
public final class BlockStateInterface {

    private final ClientLevel level;

    public BlockStateInterface(ClientLevel level) {
        this.level = level;
    }

    public BlockState get(int x, int y, int z) {
        if (level == null) {
            return Blocks.AIR.defaultBlockState();
        }
        if (y < level.getMinY() || y >= level.getMaxY()) {
            return Blocks.AIR.defaultBlockState();
        }
        // Only read loaded chunks; treat unloaded as air so calc never blocks on chunk loads.
        if (!level.hasChunk(x >> 4, z >> 4)) {
            return Blocks.AIR.defaultBlockState();
        }
        return level.getBlockState(new BlockPos(x, y, z));
    }

    public BlockState get(BlockPos pos) {
        return get(pos.getX(), pos.getY(), pos.getZ());
    }

    public ClientLevel getLevel() {
        return level;
    }
}
