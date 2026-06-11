package com.automine.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * An immutable {@link BlockPos} subclass with cheap hashCode/equals and helpers,
 * matching Baritone's pathfinding node coordinate type. Using a dedicated type keeps
 * the open/closed sets fast and avoids the mutable-pos foot-guns.
 */
public final class BetterBlockPos extends BlockPos {

    public final int x;
    public final int y;
    public final int z;

    public BetterBlockPos(int x, int y, int z) {
        super(x, y, z);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BetterBlockPos(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BetterBlockPos from(BlockPos pos) {
        if (pos instanceof BetterBlockPos better) {
            return better;
        }
        return new BetterBlockPos(pos);
    }

    public BetterBlockPos offset(int dx, int dy, int dz) {
        return new BetterBlockPos(x + dx, y + dy, z + dz);
    }

    public BetterBlockPos up() {
        return new BetterBlockPos(x, y + 1, z);
    }

    public BetterBlockPos up(int n) {
        return new BetterBlockPos(x, y + n, z);
    }

    public BetterBlockPos down() {
        return new BetterBlockPos(x, y - 1, z);
    }

    public BetterBlockPos down(int n) {
        return new BetterBlockPos(x, y - n, z);
    }

    public BetterBlockPos offset(Direction dir) {
        return new BetterBlockPos(x + dir.getStepX(), y + dir.getStepY(), z + dir.getStepZ());
    }

    public BetterBlockPos offset(Direction dir, int n) {
        return new BetterBlockPos(x + dir.getStepX() * n, y + dir.getStepY() * n, z + dir.getStepZ() * n);
    }

    /** Packs x,y,z into a single long; used as the hash key for open/closed sets. */
    public long toLong() {
        return BlockPos.asLong(x, y, z);
    }

    @Override
    public int hashCode() {
        long l = toLong();
        return (int) (l ^ (l >>> 32));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof BetterBlockPos other) {
            return x == other.x && y == other.y && z == other.z;
        }
        if (o instanceof BlockPos other) {
            return x == other.getX() && y == other.getY() && z == other.getZ();
        }
        return false;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
