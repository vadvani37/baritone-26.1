package com.automine.command.commands;

import com.automine.AutoMineMod;
import com.automine.command.ArgConsumer;
import com.automine.command.Command;
import com.automine.command.CommandException;
import com.automine.goals.GoalXZ;
import com.automine.utils.WorldScanner;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

/**
 * {@code #findbiome <biome> [goto]} — report the nearest matching biome in loaded chunks
 * (e.g. {@code #findbiome desert}). Add {@code goto} to also path there.
 */
public class FindBiomeCommand extends Command {

    public FindBiomeCommand() {
        super("Find the nearest biome in loaded chunks (optionally path to it).", "findbiome", "biome");
    }

    @Override
    public void execute(String label, ArgConsumer args) throws CommandException {
        if (!args.hasAny()) {
            throw new CommandException("Usage: #findbiome <biome> [goto]   e.g. #findbiome desert");
        }
        String id = args.getString();
        boolean go = args.hasAny() && args.getString().equalsIgnoreCase("goto");
        BlockPos p = WorldScanner.nearestBiome(id, 256);
        if (p == null) {
            logDirect("No '" + id + "' biome in loaded chunks. Explore/raise render distance and retry.");
            return;
        }
        BlockPos self = Minecraft.getInstance().player.blockPosition();
        int dist = (int) Math.sqrt(self.distSqr(p));
        logDirect("Nearest §b" + id + "§r biome near §a" + p.getX() + " " + p.getZ()
                + "§r (~" + dist + "m).");
        if (go) {
            AutoMineMod.pathing().setGoalAndPath(new GoalXZ(p.getX(), p.getZ()));
        } else {
            logDirect("§7Go with: §f#goto " + p.getX() + " " + p.getZ());
        }
    }
}
