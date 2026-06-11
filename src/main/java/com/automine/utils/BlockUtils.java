package com.automine.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Resolves block ids (e.g. "diamond_ore" or "minecraft:stone") to {@link Block} instances. */
public final class BlockUtils {

    private BlockUtils() {
    }

    public static Block byId(String id) {
        // 26.1 renamed ResourceLocation -> Identifier (unobfuscated official mappings).
        Identifier rl = id.contains(":")
                ? Identifier.parse(id)
                : Identifier.fromNamespaceAndPath("minecraft", id);
        Block block = BuiltInRegistries.BLOCK.getValue(rl);
        // getValue returns AIR for unknown ids; treat that as "not found" unless they asked for air.
        if (block == Blocks.AIR && !rl.getPath().equals("air")) {
            return null;
        }
        return block;
    }
}
