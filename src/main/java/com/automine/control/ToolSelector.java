package com.automine.control;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Picks the best hotbar slot for breaking a block, ranking candidates by an EXPLICIT material
 * tier (wood &lt; stone &lt; copper &lt; gold &lt; iron &lt; diamond &lt; netherite) rather than raw
 * mining speed — this fixes the quirk where a golden tool (fastest by speed) would otherwise win.
 * Enchantments break ties WITHIN a tier only, so an enchanted gold pickaxe beats a plain gold one
 * but still loses to any iron pickaxe.
 *
 * <p>A tool only counts if it's the right type for the block ({@code getDestroySpeed > 1}); if
 * nothing qualifies we switch to fists (empty slot) to avoid wasting durability. A broken tool
 * empties its slot, so the next tick automatically falls through to the next-best tool.
 */
public final class ToolSelector {

    /** Tier dominates; enchantment score is capped below this so it never crosses a tier. */
    private static final double TIER_STEP = 100.0;

    private ToolSelector() {
    }

    public static void selectBestTool(Minecraft mc, BlockState state) {
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        Inventory inv = player.getInventory();
        int current = inv.getSelectedSlot();
        int emptySlot = -1;
        int bestSlot = -1;
        double bestScore = -1.0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                if (emptySlot < 0) {
                    emptySlot = i;
                }
                continue;
            }
            if (stack.getDestroySpeed(state) <= 1.0) {
                continue; // not an effective tool type for this block
            }
            double score = tierRank(stack) * TIER_STEP + enchantScore(stack);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        // Nothing effective -> fists, so we don't burn durability on the wrong tool.
        if (bestSlot < 0) {
            bestSlot = emptySlot >= 0 ? emptySlot : current;
        }
        if (bestSlot != current && bestSlot >= 0) {
            inv.setSelectedSlot(bestSlot);
        }
    }

    /** Material tier from the item id (wooden_/stone_/copper_/golden_/iron_/diamond_/netherite_). */
    private static int tierRank(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (path.startsWith("netherite_")) return 7;
        if (path.startsWith("diamond_")) return 6;
        if (path.startsWith("iron_")) return 5;
        if (path.startsWith("golden_")) return 4;
        if (path.startsWith("copper_")) return 3;
        if (path.startsWith("stone_")) return 2;
        if (path.startsWith("wooden_")) return 1;
        return 0; // effective but untiered (e.g. shears) — still beats fists
    }

    /** Within-tier bonus for enchantments; capped below {@link #TIER_STEP}. */
    private static int enchantScore(ItemStack stack) {
        if (!stack.isEnchanted()) {
            return 0;
        }
        return Math.min(50, 1 + stack.getEnchantments().size());
    }
}
