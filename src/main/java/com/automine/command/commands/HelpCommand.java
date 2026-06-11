package com.automine.command.commands;

import com.automine.command.ArgConsumer;
import com.automine.command.Command;
import com.automine.command.CommandManager;

/** {@code #help [command]} — list commands or describe one. */
public class HelpCommand extends Command {

    private final CommandManager manager;

    public HelpCommand(CommandManager manager) {
        super("List all commands, or describe one.", "help", "?");
        this.manager = manager;
    }

    @Override
    public void execute(String label, ArgConsumer args) {
        if (args.hasAny()) {
            String name = args.getString().toLowerCase();
            for (Command c : manager.getCommands()) {
                if (c.getNames().contains(name)) {
                    logDirect("§b#" + String.join(", #", c.getNames()));
                    logDirect(c.getShortDesc());
                    return;
                }
            }
            logDirect("§cNo such command: " + name);
            return;
        }
        logDirect("§bAutoMine commands §7(prefix with #):");
        for (Command c : manager.getCommands()) {
            logDirect(" §b#" + c.getNames().get(0) + " §7- " + c.getShortDesc());
        }
    }
}
