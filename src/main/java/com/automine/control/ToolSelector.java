package com.automine.control;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Picks the best hotbar slot for breaking a given block, mirroring Baritone's auto-tool:
 * choose the item with the highest mining speed against the block (which naturally prefers the
 * correct tool type and higher material tiers — a diamond pickaxe out-speeds iron, iron beats
 * stone). If nothing in the hotbar actually speeds up the block, switch to an empty slot
 * ("fists") so we don't waste durability swinging the wrong tool. When the held tool breaks it
 * becomes empty, so the next tick's selection automatically moves to the next-best tool.
 */
public final class ToolSelector {

    private ToolSelector() {
    }

    public static void selectBestTool(Minecraft mc, BlockState state) {
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        Inventory inv = player.getInventory();
        int current = inv.getSelectedSlot();
        int bestSlot = current;
        double bestSpeed = -1.0;
        int emptySlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                if (emptySlot < 0) {
                    emptySlot = i;
                }
                continue;
            }
            double speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        // speed <= 1.0 means no tool helps (wrong type / bare block): use fists to save durability.
        if (bestSpeed <= 1.0 && emptySlot >= 0) {
            bestSlot = emptySlot;
        }
        if (bestSlot != current) {
            inv.setSelectedSlot(bestSlot);
        }
    }
}
