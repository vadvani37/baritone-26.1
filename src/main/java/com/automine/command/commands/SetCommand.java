package com.automine.command.commands;

import com.automine.AutoMineMod;
import com.automine.api.Settings;
import com.automine.command.ArgConsumer;
import com.automine.command.Command;
import com.automine.command.CommandException;

/** {@code #set} | {@code #set <name>} | {@code #set <name> <value>} — view/change settings. */
public class SetCommand extends Command {

    public SetCommand() {
        super("List, view, or change settings.", "set", "setting", "settings");
    }

    @Override
    public void execute(String label, ArgConsumer args) throws CommandException {
        Settings settings = AutoMineMod.settings();
        if (!args.hasAny()) {
            logDirect("Settings:");
            settings.all().forEach((name, field) -> logDirect(" §7" + name + "§r = " + settings.get(name)));
            return;
        }
        String name = args.getString();
        if (!args.hasAny()) {
            String value = settings.get(name);
            if (value == null) {
                throw new CommandException("Unknown setting '" + name + "'.");
            }
            logDirect(name + " = " + value);
            return;
        }
        String value = args.getString();
        if (!settings.set(name, value)) {
            throw new CommandException("Couldn't set '" + name + "' to '" + value + "'.");
        }
        logDirect("Set " + name + " = " + settings.get(name));
    }
}
