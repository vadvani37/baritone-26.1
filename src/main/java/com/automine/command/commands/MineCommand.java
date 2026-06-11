package com.automine.command.commands;

import com.automine.AutoMineMod;
import com.automine.command.ArgConsumer;
import com.automine.command.Command;
import com.automine.command.CommandException;
import com.automine.utils.BlockUtils;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashSet;
import java.util.Set;

/** {@code #mine <block> [block...]} — auto-mine the nearest matching blocks, walking to them. */
public class MineCommand extends Command {

    public MineCommand() {
        super("Auto-mine nearby blocks of the given type(s).", "mine");
    }

    @Override
    public void execute(String label, ArgConsumer args) throws CommandException {
        if (!args.hasAny()) {
            throw new CommandException("Usage: #mine <block> [block...]  e.g. #mine diamond_ore iron_ore");
        }
        Set<Block> blocks = new LinkedHashSet<>();
        for (String id : args.drainAll()) {
            Block b = BlockUtils.byId(id);
            if (b == null) {
                throw new CommandException("Unknown block '" + id + "'.");
            }
            blocks.add(b);
        }
        AutoMineMod.mineProcess().mine(blocks);
    }
}
