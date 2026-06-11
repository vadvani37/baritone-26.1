package com.automine.command.commands;

import com.automine.AutoMineMod;
import com.automine.command.ArgConsumer;
import com.automine.command.Command;
import com.automine.command.CommandException;
import com.automine.goals.Goal;
import com.automine.goals.GoalBlock;
import com.automine.goals.GoalRunAway;
import com.automine.goals.GoalXZ;
import net.minecraft.core.BlockPos;

/** {@code #invert} — flee from the current goal instead of seeking it. */
public class InvertCommand extends Command {

    public InvertCommand() {
        super("Invert the current goal (run away from it).", "invert");
    }

    @Override
    public void execute(String label, ArgConsumer args) throws CommandException {
        Goal goal = AutoMineMod.getStoredGoal();
        if (goal == null && AutoMineMod.pathing().isActive()) {
            goal = AutoMineMod.pathing().getGoal();
        }
        BlockPos from;
        if (goal instanceof GoalBlock gb) {
            from = new BlockPos(gb.x, gb.y, gb.z);
        } else if (goal instanceof GoalXZ xz) {
            from = new BlockPos(xz.x, 64, xz.z);
        } else {
            throw new CommandException("Set a block/XZ goal first, then #invert.");
        }
        GoalRunAway away = new GoalRunAway(100, from);
        AutoMineMod.setStoredGoal(away);
        AutoMineMod.pathing().setGoalAndPath(away);
    }
}
