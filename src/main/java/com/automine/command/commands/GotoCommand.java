package com.automine.command.commands;

import com.automine.AutoMineMod;
import com.automine.command.ArgConsumer;
import com.automine.command.Command;
import com.automine.command.CommandException;
import com.automine.goals.GoalBlock;
import com.automine.goals.GoalNear;
import com.automine.goals.GoalXZ;
import com.automine.goals.GoalYLevel;
import com.automine.utils.BlockUtils;
import com.automine.utils.WorldScanner;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

/**
 * {@code #goto <x> <y> <z>} | {@code #goto <x> <z>} | {@code #goto <y>} | {@code #goto <block>}.
 * Supports {@code ~} for player-relative coordinates.
 */
public class GotoCommand extends Command {

    public GotoCommand() {
        super("Path to coordinates, a Y level, or the nearest block of a type.", "goto");
    }

    @Override
    public void execute(String label, ArgConsumer args) throws CommandException {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            throw new CommandException("No player.");
        }
        BlockPos self = mc.player.blockPosition();
        int n = args.size();

        if (n == 1) {
            String token = args.peek();
            // Non-numeric single arg -> treat as a block id.
            if (!isCoord(token)) {
                Block block = BlockUtils.byId(args.getString());
                if (block == null) {
                    throw new CommandException("Unknown block '" + token + "'.");
                }
                BlockPos found = WorldScanner.nearest(block, 48);
                if (found == null) {
                    throw new CommandException("No '" + token + "' found nearby.");
                }
                AutoMineMod.pathing().setGoalAndPath(
                        new GoalNear(found.getX(), found.getY(), found.getZ(), 1));
                return;
            }
            int y = coord(args.getString(), self.getY());
            AutoMineMod.pathing().setGoalAndPath(new GoalYLevel(y));
        } else if (n == 2) {
            int x = coord(args.getString(), self.getX());
            int z = coord(args.getString(), self.getZ());
            AutoMineMod.pathing().setGoalAndPath(new GoalXZ(x, z));
        } else if (n >= 3) {
            int x = coord(args.getString(), self.getX());
            int y = coord(args.getString(), self.getY());
            int z = coord(args.getString(), self.getZ());
            AutoMineMod.pathing().setGoalAndPath(new GoalBlock(x, y, z));
        } else {
            throw new CommandException("Usage: #goto <x> <y> <z> | <x> <z> | <y> | <block>");
        }
    }

    private static boolean isCoord(String s) {
        return s.startsWith("~") || s.matches("-?\\d+");
    }

    private static int coord(String token, int base) {
        if (token.startsWith("~")) {
            String rest = token.substring(1);
            return rest.isEmpty() ? base : base + Integer.parseInt(rest);
        }
        return Integer.parseInt(token);
    }
}
