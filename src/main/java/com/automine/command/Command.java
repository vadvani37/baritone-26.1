package com.automine.command;

import com.automine.utils.Helper;

import java.util.List;

/** Base class for a chat command. Subclasses declare their names and implement {@link #execute}. */
public abstract class Command implements Helper {

    private final List<String> names;
    private final String shortDesc;

    protected Command(String shortDesc, String... names) {
        this.shortDesc = shortDesc;
        this.names = List.of(names);
    }

    public List<String> getNames() {
        return names;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public abstract void execute(String label, ArgConsumer args) throws CommandException;
}
