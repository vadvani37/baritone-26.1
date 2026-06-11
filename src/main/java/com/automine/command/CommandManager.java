package com.automine.command;

import com.automine.command.commands.*;
import com.automine.utils.Helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registers commands and dispatches a {@code #}-prefixed chat line to the right one.
 * Names map case-insensitively; unknown names report an error rather than reaching the server.
 */
public final class CommandManager {

    private final Map<String, Command> byName = new LinkedHashMap<>();
    private final List<Command> commands = new ArrayList<>();

    public CommandManager() {
        register(new HelpCommand(this));
        register(new GotoCommand());
        register(new GoalCommand());
        register(new PathCommand());
        register(new StopCommand());
        register(new MineCommand());
        register(new FollowCommand());
        register(new ComeCommand());
        register(new ThisWayCommand());
        register(new InvertCommand());
        register(new SetCommand());
        register(new ExploreCommand());
        register(new VersionCommand());
        // Known Baritone commands not yet fully implemented — registered so #help lists them
        // and they give a clear message instead of leaking to chat.
        register(new StubCommand("Build a schematic (not implemented).", "build"));
        register(new StubCommand("Manage selections (not implemented).", "sel", "selection"));
        register(new StubCommand("Go to the surface (not implemented).", "surface", "top"));
        register(new StubCommand("Farm nearby crops (not implemented).", "farm"));
        register(new StubCommand("Toggle path rendering (use #set renderpath).", "render"));
        register(new StubCommand("Repack chunks (not implemented).", "repack"));
        register(new StubCommand("Save/load waypoints (not implemented).", "waypoints", "wp"));
    }

    public void register(Command command) {
        commands.add(command);
        for (String name : command.getNames()) {
            byName.put(name.toLowerCase(), command);
        }
    }

    public List<Command> getCommands() {
        return commands;
    }

    /** Handle a chat line that has already had its {@code #} prefix stripped. */
    public void execute(String line) {
        List<String> parts = new ArrayList<>(Arrays.asList(line.trim().split("\\s+")));
        if (parts.isEmpty() || parts.get(0).isEmpty()) {
            Helper.logError("Empty command. Try #help");
            return;
        }
        String label = parts.remove(0).toLowerCase();
        Command command = byName.get(label);
        if (command == null) {
            Helper.logError("Unknown command '" + label + "'. Try #help");
            return;
        }
        try {
            command.execute(label, new ArgConsumer(parts));
        } catch (CommandException e) {
            Helper.logError(e.getMessage());
        } catch (Exception e) {
            Helper.logError("Error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
