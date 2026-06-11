package com.automine.command.commands;

import com.automine.AutoMineMod;
import com.automine.command.ArgConsumer;
import com.automine.command.Command;
import com.automine.goals.GoalXZ;
import net.minecraft.client.Minecraft;

/**
 * {@code #explore [x z]} — head toward a distant point to load new terrain. With no args it
 * explores outward from the current position. (Simplified vs. Baritone's frontier explorer.)
 */
public class ExploreCommand extends Command {

    public ExploreCommand() {
        super("Explore outward to load new chunks.", "explore");
    }

    @Override
    public void execute(String label, ArgConsumer args) {
        Minecraft mc = Minecraft.getInstance();
        int cx = mc.player != null ? (int) mc.player.getX() : 0;
        int cz = mc.player != null ? (int) mc.player.getZ() : 0;
        int x, z;
        if (args.size() >= 2) {
            x = args.getInt();
            z = args.getInt();
        } else {
            // March 1024 blocks in the facing direction.
            float yawRad = (float) Math.toRadians(mc.player.getYRot());
            x = cx + (int) (-Math.sin(yawRad) * 1024);
            z = cz + (int) (Math.cos(yawRad) * 1024);
        }
        AutoMineMod.pathing().setGoalAndPath(new GoalXZ(x, z));
    }
}
