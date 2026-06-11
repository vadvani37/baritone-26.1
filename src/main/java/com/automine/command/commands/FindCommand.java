package com.automine.command.commands;

import com.automine.command.ArgConsumer;
import com.automine.command.Command;
import com.automine.command.CommandException;
import com.automine.utils.BlockUtils;
import com.automine.utils.WorldScanner;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

/**
 * {@code #find <block>} — report the nearest block of a type in loaded chunks. Doubles as a
 * structure finder: search for a structure's signature block (e.g. {@code spawner},
 * {@code chest}, {@code end_portal_frame}). Client-side, this only sees loaded chunks.
 */
public class FindCommand extends Command {

    public FindCommand() {
        super("Report the nearest block of a type in loaded chunks.", "find", "findblock");
    }

    @Override
    public void execute(String label, ArgConsumer args) throws CommandException {
        if (!args.hasAny()) {
            throw new CommandException("Usage: #find <block>   e.g. #find spawner | #find diamond_ore");
        }
        String id = args.getString();
        Block block = BlockUtils.byId(id);
        if (block == null) {
            throw new CommandException("Unknown block '" + id + "'.");
        }
        BlockPos p = WorldScanner.nearest(block, 96);
        if (p == null) {
            logDirect("No '" + id + "' in loaded chunks. Move closer or raise render distance, then retry.");
            return;
        }
        BlockPos self = Minecraft.getInstance().player.blockPosition();
        int dist = (int) Math.sqrt(self.distSqr(p));
        logDirect("Nearest §b" + id + "§r at §a" + p.getX() + " " + p.getY() + " " + p.getZ()
                + "§r (~" + dist + "m). §7Go with: §f#goto " + p.getX() + " " + p.getY() + " " + p.getZ());
    }
}
