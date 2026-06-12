package com.automine;

import com.automine.api.Settings;
import com.automine.command.CommandManager;
import com.automine.control.InputOverrideHandler;
import com.automine.control.PlacementHelper;
import com.automine.goals.Goal;
import com.automine.process.FollowProcess;
import com.automine.process.MineProcess;
import com.automine.process.PathingBehavior;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;

/**
 * Client entrypoint. Wires up the singletons, ticks the processes/pathing every client tick
 * (early, so synthesised inputs are read by the game), and routes {@code #}-prefixed chat lines
 * to the command system instead of sending them to the server.
 */
public final class AutoMineMod implements ClientModInitializer {

    private static Settings settings;
    private static InputOverrideHandler inputHandler;
    private static PlacementHelper placementHelper;
    private static PathingBehavior pathing;
    private static MineProcess mineProcess;
    private static FollowProcess followProcess;
    private static CommandManager commandManager;
    private static Goal storedGoal;

    @Override
    public void onInitializeClient() {
        settings = new Settings();
        inputHandler = new InputOverrideHandler();
        placementHelper = new PlacementHelper();
        pathing = new PathingBehavior();
        mineProcess = new MineProcess();
        followProcess = new FollowProcess();
        commandManager = new CommandManager();

        // Tick order: processes pick a goal, then the pathing behaviour drives toward it.
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                return;
            }
            mineProcess.onTick();
            followProcess.onTick();
            pathing.onTick();
            com.automine.render.PathRenderer.onClientTick(client);
        });

        // Intercept `#` commands from both the chat box and the command box.
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (message.startsWith("#")) {
                commandManager.execute(message.substring(1));
                return false; // don't send to server
            }
            return true;
        });
        ClientSendMessageEvents.ALLOW_COMMAND.register(command -> {
            if (command.startsWith("#")) {
                commandManager.execute(command.substring(1));
                return false;
            }
            return true;
        });
    }

    public static Settings settings() {
        return settings;
    }

    public static InputOverrideHandler inputHandler() {
        return inputHandler;
    }

    public static PlacementHelper placementHelper() {
        return placementHelper;
    }

    public static PathingBehavior pathing() {
        return pathing;
    }

    public static MineProcess mineProcess() {
        return mineProcess;
    }

    public static FollowProcess followProcess() {
        return followProcess;
    }

    public static CommandManager commandManager() {
        return commandManager;
    }

    public static Goal getStoredGoal() {
        return storedGoal;
    }

    public static void setStoredGoal(Goal goal) {
        storedGoal = goal;
    }
}
