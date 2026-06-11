package com.automine.pathing.movement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * Static predicates that decide whether the player can stand in, walk through, or break a block.
 * These are the leaves of the cost model — every movement is composed of "can I be here" and
 * "what does it cost to clear/stand on this" questions answered here.
 */
public final class MovementHelper implements ActionCosts {

    private MovementHelper() {
    }

    /** Can the player's body occupy this block (i.e. is it non-colliding) without breaking it? */
    public static boolean canWalkThrough(CalculationContext ctx, int x, int y, int z) {
        BlockState state = ctx.bsi.get(x, y, z);
        return canWalkThrough(ctx, x, y, z, state);
    }

    public static boolean canWalkThrough(CalculationContext ctx, int x, int y, int z, BlockState state) {
        Block block = state.getBlock();
        if (state.isAir()) {
            return true;
        }
        if (block == Blocks.WATER) {
            // flowing/source water is enterable but handled by swim cost elsewhere
            return true;
        }
        if (isLava(state)) {
            return false;
        }
        if (block instanceof LadderBlock || block instanceof VineBlock) {
            return true;
        }
        if (isFlowing(state)) {
            return false;
        }
        // Anything with no collision box (plants, torches, signs, ...) is walk-through.
        return ctx.bsi.getLevel() != null
                && state.getCollisionShape(ctx.bsi.getLevel(), new BlockPos(x, y, z)).isEmpty();
    }

    /** Is this a solid top surface the player can stand on? */
    public static boolean canWalkOn(CalculationContext ctx, int x, int y, int z) {
        BlockState state = ctx.bsi.get(x, y, z);
        return canWalkOn(ctx, x, y, z, state);
    }

    public static boolean canWalkOn(CalculationContext ctx, int x, int y, int z, BlockState state) {
        Block block = state.getBlock();
        if (state.isAir()) {
            return false;
        }
        if (block == Blocks.WATER || isLava(state)) {
            return false;
        }
        if (block instanceof LadderBlock || block instanceof VineBlock) {
            return true; // climbable counts as support
        }
        if (ctx.bsi.getLevel() == null) {
            return false;
        }
        // A full-ish collision shape on top means we can stand here.
        return state.isFaceSturdy(ctx.bsi.getLevel(), new BlockPos(x, y, z), net.minecraft.core.Direction.UP)
                || !state.getCollisionShape(ctx.bsi.getLevel(), new BlockPos(x, y, z)).isEmpty();
    }

    /** Cost in ticks to break this block, or {@link #COST_INF} if we won't / can't. */
    public static double getMiningDurationTicks(CalculationContext ctx, int x, int y, int z, boolean includeFalling) {
        BlockState state = ctx.bsi.get(x, y, z);
        Block block = state.getBlock();
        if (state.isAir() || canWalkThrough(ctx, x, y, z, state)) {
            return 0;
        }
        if (!ctx.allowBreak) {
            return COST_INF;
        }
        if (isLiquid(state) || block == Blocks.BEDROCK || block == Blocks.BARRIER) {
            return COST_INF;
        }
        if (ctx.bsi.getLevel() == null) {
            return COST_INF;
        }
        float hardness = state.getDestroySpeed(ctx.bsi.getLevel(), new BlockPos(x, y, z));
        if (hardness < 0) {
            return COST_INF; // unbreakable
        }
        if (hardness == 0) {
            return 1; // instant-mine (e.g. plants, torches)
        }
        // Rough estimate assuming a serviceable tool; the executor re-checks real progress.
        double ticks = hardness * 1.5 * 20.0 / estimatedToolSpeed(block);
        if (includeFalling) {
            BlockState above = ctx.bsi.get(x, y + 1, z);
            if (above.getBlock() == Blocks.SAND || above.getBlock() == Blocks.GRAVEL) {
                ticks += getMiningDurationTicks(ctx, x, y + 1, z, true);
            }
        }
        return ticks;
    }

    private static double estimatedToolSpeed(Block block) {
        // Approximation of a diamond-tier tool against the common pathing blocks.
        return 8.0;
    }

    public static boolean isLiquid(BlockState state) {
        return !state.getFluidState().isEmpty();
    }

    public static boolean isLava(BlockState state) {
        FluidState fluid = state.getFluidState();
        return !fluid.isEmpty() && state.getBlock() == Blocks.LAVA;
    }

    public static boolean isFlowing(BlockState state) {
        FluidState fluid = state.getFluidState();
        return !fluid.isEmpty() && !fluid.isSource();
    }

    public static boolean isClimbable(BlockState state) {
        return state.getBlock() instanceof LadderBlock || state.getBlock() instanceof VineBlock;
    }
}
