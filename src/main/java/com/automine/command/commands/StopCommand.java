package com.automine.command.commands;

import com.automine.AutoMineMod;
import com.automine.command.ArgConsumer;
import com.automine.command.Command;

/** {@code #stop} — cancel all pathing, mining and following, and release inputs. */
public class StopCommand extends Command {

    public StopCommand() {
        super("Stop everything (pathing, mining, following).", "stop", "cancel", "halt");
    }

    @Override
    public void execute(String label, ArgConsumer args) {
        AutoMineMod.mineProcess().stop();
        AutoMineMod.followProcess().stop();
        AutoMineMod.pathing().cancel();
        logDirect("Stopped.");
    }
}
