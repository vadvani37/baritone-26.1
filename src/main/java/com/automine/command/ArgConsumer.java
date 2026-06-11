package com.automine.command;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Consumes command arguments left-to-right with typed getters, mirroring Baritone's ArgConsumer.
 * Throws {@link CommandException} on malformed input so commands can stay terse.
 */
public final class ArgConsumer {

    private final Deque<String> args;

    public ArgConsumer(List<String> args) {
        this.args = new ArrayDeque<>(args);
    }

    public boolean hasAny() {
        return !args.isEmpty();
    }

    public int size() {
        return args.size();
    }

    public String peek() {
        return args.peek();
    }

    public String getString() {
        if (args.isEmpty()) {
            throw new CommandException("Expected another argument");
        }
        return args.poll();
    }

    public String getStringOrDefault(String def) {
        return args.isEmpty() ? def : args.poll();
    }

    public int getInt() {
        String s = getString();
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new CommandException("Expected an integer, got '" + s + "'");
        }
    }

    public double getDouble() {
        String s = getString();
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new CommandException("Expected a number, got '" + s + "'");
        }
    }

    /** Remaining args joined with spaces (consumes them). */
    public String rest() {
        StringBuilder sb = new StringBuilder();
        while (!args.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(args.poll());
        }
        return sb.toString();
    }

    public java.util.List<String> drainAll() {
        java.util.List<String> out = new java.util.ArrayList<>(args);
        args.clear();
        return out;
    }
}
