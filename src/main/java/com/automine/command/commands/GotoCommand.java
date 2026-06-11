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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

/**
 * {@code #goto <x> <y> <z>} | {@code #goto <x> <z>} | {@code #goto <y>} | {@code #goto <block>}
 * | {@code #goto player [name]}. Supports {@code ~} for player-relative coordinates.
 */
public class GotoCommand extends Command {

    public GotoCommand() {
        super("Path to coordinates, a Y level, the nearest block, or a player.", "goto");
    }

    @Override
    public void execute(String label, ArgConsumer args) throws CommandException {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            throw new CommandException("No player.");
        }
        BlockPos self = mc.player.blockPosition();

        // #goto player [name] -> path to that player's CURRENT position (one-shot; use #follow to track).
        if (args.hasAny() && (args.peek().equalsIgnoreCase("player") || args.peek().equalsIgnoreCase("p"))) {
            args.getString(); // consume "player"
            String name = args.hasAny() ? args.getString() : null;
            Player target = findPlayer(mc, name);
            if (target == null) {
                throw new CommandException(name == null
                        ? "No other players in range."
                        : "Player '" + name + "' not found in range.");
            }
            BlockPos p = target.blockPosition();
            AutoMineMod.pathing().setGoalAndPath(new GoalNear(p.getX(), p.getY(), p.getZ(), 2));
            logDirect("Pathing to §b" + target.getName().getString() + "§r at §a"
                    + p.getX() + " " + p.getY() + " " + p.getZ() + "§r §7(#follow player to keep tracking)");
            return;
        }

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

    /** Finds a player by name (case-insensitive), or the nearest other player if name is null. */
    private static Player findPlayer(Minecraft mc, String name) {
        if (mc.level == null) {
            return null;
        }
        Player best = null;
        double bestDist = Double.MAX_VALUE;
        for (Player pl : mc.level.players()) {
            if (pl == mc.player) {
                continue;
            }
            if (name != null) {
                if (pl.getName().getString().equalsIgnoreCase(name)) {
                    return pl;
                }
            } else {
                double d = pl.distanceToSqr(mc.player);
                if (d < bestDist) {
                    bestDist = d;
                    best = pl;
                }
            }
        }
        return best;
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
