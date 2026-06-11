package com.automine.command;

/** Thrown by commands/arg parsing to report a user-facing error message. */
public class CommandException extends RuntimeException {
    public CommandException(String message) {
        super(message);
    }
}
