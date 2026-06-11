package com.automine.command.commands;

import com.automine.AutoMineMod;
import com.automine.command.ArgConsumer;
import com.automine.command.Command;
import com.automine.command.CommandException;
import com.automine.goals.Goal;
import com.automine.goals.GoalBlock;
import com.automine.goals.GoalXZ;
import com.automine.goals.GoalYLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

/** {@code #goal <x> <y> <z>|<x> <z>|<y>|clear} — sets the stored goal (use {@code #path} to go). */
public class GoalCommand extends Command {

    public GoalCommand() {
        super("Set or clear the stored goal (does not start pathing; use #path).", "goal");
    }

    @Override
    public void execute(String label, ArgConsumer args) throws CommandException {
        if (!args.hasAny()) {
            Goal g = AutoMineMod.getStoredGoal();
            logDirect("Goal: " + (g == null ? "none" : g));
            return;
        }
        String first = args.peek();
        if (first.equalsIgnoreCase("clear") || first.equalsIgnoreCase("none") || first.equalsIgnoreCase("reset")) {
            AutoMineMod.setStoredGoal(null);
            logDirect("Goal cleared.");
            return;
        }
        BlockPos self = Minecraft.getInstance().player.blockPosition();
        int n = args.size();
        Goal goal;
        if (n == 1) {
            goal = new GoalYLevel(coord(args.getString(), self.getY()));
        } else if (n == 2) {
            goal = new GoalXZ(coord(args.getString(), self.getX()), coord(args.getString(), self.getZ()));
        } else {
            goal = new GoalBlock(coord(args.getString(), self.getX()),
                    coord(args.getString(), self.getY()), coord(args.getString(), self.getZ()));
        }
        AutoMineMod.setStoredGoal(goal);
        logDirect("Goal set: " + goal);
    }

    private static int coord(String token, int base) {
        if (token.startsWith("~")) {
            String rest = token.substring(1);
            return rest.isEmpty() ? base : base + Integer.parseInt(rest);
        }
        return Integer.parseInt(token);
    }
}
