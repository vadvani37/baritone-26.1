package com.automine.command.commands;

import com.automine.AutoMineMod;
import com.automine.command.ArgConsumer;
import com.automine.command.Command;
import com.automine.process.FollowProcess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

/** {@code #follow players|entities|<name>} — continuously path near the chosen target. */
public class FollowCommand extends Command {

    public FollowCommand() {
        super("Follow players, entities, or a named entity.", "follow");
    }

    @Override
    public void execute(String label, ArgConsumer args) {
        String what = args.getStringOrDefault("players").toLowerCase();
        switch (what) {
            case "players", "player" ->
                    AutoMineMod.followProcess().follow(FollowProcess.players(), "players");
            case "entities", "entity" -> {
                Predicate<Entity> p = e -> e instanceof LivingEntity && !(e instanceof Player);
                AutoMineMod.followProcess().follow(p, "entities");
            }
            default -> {
                Predicate<Entity> byName = e -> e.getName().getString().equalsIgnoreCase(what);
                AutoMineMod.followProcess().follow(byName, what);
            }
        }
    }
}
