package com.automine.command.commands;

import com.automine.command.ArgConsumer;
import com.automine.command.Command;

/** {@code #version} — print the mod version. */
public class VersionCommand extends Command {

    public VersionCommand() {
        super("Show the AutoMine version.", "version");
    }

    @Override
    public void execute(String label, ArgConsumer args) {
        logDirect("AutoMine 1.0.0 (Baritone-style) for Minecraft 26.1.x");
    }
}
