package com.automine.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Small mixin-free convenience interface for sending client-side chat output,
 * mirroring Baritone's {@code Helper}.
 */
public interface Helper {

    Minecraft MC = Minecraft.getInstance();

    String PREFIX = "§8[§bAutoMine§8]§r ";

    default void logDirect(String message) {
        Helper.log(message);
    }

    static void log(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        // 26.1: client chat output is sendSystemMessage(Component) (displayClientMessage was removed).
        mc.player.sendSystemMessage(Component.literal(PREFIX + message));
    }

    static void logError(String message) {
        log("§c" + message);
    }
}
