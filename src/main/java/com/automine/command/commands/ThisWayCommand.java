package com.automine.command.commands;

import com.automine.AutoMineMod;
import com.automine.command.ArgConsumer;
import com.automine.command.Command;
import com.automine.command.CommandException;
import com.automine.goals.GoalXZ;
import net.minecraft.client.Minecraft;

/** {@code #thisway <distance>} — set a goal that far in the direction you're facing. */
public class ThisWayCommand extends Command {

    public ThisWayCommand() {
        super("Create a goal in the direction you're facing.", "thisway", "forward");
    }

    @Override
    public void execute(String label, ArgConsumer args) throws CommandException {
        double distance = args.getDouble();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            throw new CommandException("No player.");
        }
        float yawRad = (float) Math.toRadians(mc.player.getYRot());
        double dx = -Math.sin(yawRad) * distance;
        double dz = Math.cos(yawRad) * distance;
        int x = (int) Math.floor(mc.player.getX() + dx);
        int z = (int) Math.floor(mc.player.getZ() + dz);
        GoalXZ goal = new GoalXZ(x, z);
        AutoMineMod.setStoredGoal(goal);
        AutoMineMod.pathing().setGoalAndPath(goal);
    }
}
