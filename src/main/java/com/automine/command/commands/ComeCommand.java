package com.automine.command.commands;

import com.automine.AutoMineMod;
import com.automine.command.ArgConsumer;
import com.automine.command.Command;
import com.automine.command.CommandException;
import com.automine.goals.GoalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** {@code #come} — path to the block you are currently looking at. */
public class ComeCommand extends Command {

    public ComeCommand() {
        super("Path to the block under your crosshair.", "come");
    }

    @Override
    public void execute(String label, ArgConsumer args) throws CommandException {
        Minecraft mc = Minecraft.getInstance();
        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult bhr) || hit.getType() == HitResult.Type.MISS) {
            throw new CommandException("Not looking at a block.");
        }
        var pos = bhr.getBlockPos().above();
        AutoMineMod.pathing().setGoalAndPath(new GoalBlock(pos));
    }
}
