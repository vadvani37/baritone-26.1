package com.automine.command.commands;

import com.automine.AutoMineMod;
import com.automine.command.ArgConsumer;
import com.automine.command.Command;
import com.automine.command.CommandException;
import com.automine.goals.Goal;

/** {@code #path} — start pathing toward the stored goal. */
public class PathCommand extends Command {

    public PathCommand() {
        super("Start pathing to the stored goal.", "path");
    }

    @Override
    public void execute(String label, ArgConsumer args) throws CommandException {
        Goal goal = AutoMineMod.getStoredGoal();
        if (goal == null) {
            throw new CommandException("No goal set. Use #goal or #goto first.");
        }
        AutoMineMod.pathing().setGoalAndPath(goal);
    }
}
