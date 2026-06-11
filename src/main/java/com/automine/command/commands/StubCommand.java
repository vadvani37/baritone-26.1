package com.automine.command.commands;

import com.automine.command.ArgConsumer;
import com.automine.command.Command;

/** Placeholder for known Baritone commands not yet implemented; reports a clear message. */
public class StubCommand extends Command {

    public StubCommand(String shortDesc, String... names) {
        super(shortDesc, names);
    }

    @Override
    public void execute(String label, ArgConsumer args) {
        logDirect("§e#" + label + " is recognised but not implemented yet: " + getShortDesc());
    }
}
