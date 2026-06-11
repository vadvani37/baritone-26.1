package com.automine.command.commands;

import com.automine.AutoMineMod;
import com.automine.command.ArgConsumer;
import com.automine.command.Command;
import com.automine.command.CommandException;
import com.automine.goals.GoalBlock;
import com.automine.utils.BlockUtils;
import com.automine.utils.WorldScanner;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * {@code #findstructure <type> [goto]} — locate a structure by scanning loaded chunks for its
 * signature block(s). NOTE: a client-side mod has no world-generation data, so this cannot do a
 * true server-side {@code /locate} to distant/ungenerated structures — it only finds ones whose
 * chunks the server has already sent you. Heuristic (signature-block) detection.
 */
public class FindStructureCommand extends Command {

    /** structure name -> its most distinctive block id(s). */
    private static final Map<String, String[]> STRUCTURES = new LinkedHashMap<>();

    static {
        STRUCTURES.put("stronghold", new String[]{"end_portal_frame"});
        STRUCTURES.put("end_city", new String[]{"purpur_pillar", "purpur_block"});
        STRUCTURES.put("ancient_city", new String[]{"reinforced_deepslate", "sculk_shrieker", "sculk_catalyst"});
        STRUCTURES.put("trial_chambers", new String[]{"vault", "trial_spawner"});
        STRUCTURES.put("nether_fortress", new String[]{"nether_brick_fence"});
        STRUCTURES.put("bastion", new String[]{"gilded_blackstone", "polished_blackstone_bricks"});
        STRUCTURES.put("ocean_monument", new String[]{"sea_lantern", "dark_prismarine"});
        STRUCTURES.put("village", new String[]{"bell"});
        STRUCTURES.put("ruined_portal", new String[]{"crying_obsidian"});
        STRUCTURES.put("mineshaft", new String[]{"rail", "cobweb"});
        STRUCTURES.put("desert_pyramid", new String[]{"chiseled_sandstone"});
        STRUCTURES.put("nether_fossil", new String[]{"bone_block"});
        STRUCTURES.put("witch_hut", new String[]{"cauldron"});
    }

    /** friendly aliases. */
    private static final Map<String, String> ALIASES = Map.of(
            "monument", "ocean_monument",
            "fortress", "nether_fortress",
            "city", "end_city",
            "endcity", "end_city",
            "ancientcity", "ancient_city",
            "trialchamber", "trial_chambers",
            "trial_chamber", "trial_chambers",
            "pyramid", "desert_pyramid");

    public FindStructureCommand() {
        super("Find a structure by its signature block in loaded chunks.", "findstructure", "locate");
    }

    @Override
    public void execute(String label, ArgConsumer args) throws CommandException {
        if (!args.hasAny() || args.peek().equalsIgnoreCase("list")) {
            logDirect("Structures: §b" + String.join("§r, §b", STRUCTURES.keySet()));
            logDirect("§7Usage: §f#findstructure <type> [goto]");
            return;
        }
        String name = args.getString().toLowerCase();
        name = ALIASES.getOrDefault(name, name);
        String[] sig = STRUCTURES.get(name);
        if (sig == null) {
            throw new CommandException("Unknown structure '" + name + "'. Try #findstructure list");
        }
        boolean go = args.hasAny() && args.getString().equalsIgnoreCase("goto");

        Set<Block> blocks = new LinkedHashSet<>();
        for (String id : sig) {
            Block b = BlockUtils.byId(id);
            if (b != null) {
                blocks.add(b);
            }
        }
        BlockPos p = WorldScanner.nearestAny(blocks, 128);
        if (p == null) {
            logDirect("No §b" + name + "§r signature blocks in loaded chunks. Explore closer and retry.");
            return;
        }
        BlockPos self = Minecraft.getInstance().player.blockPosition();
        int dist = (int) Math.sqrt(self.distSqr(p));
        logDirect("Likely §b" + name + "§r near §a" + p.getX() + " " + p.getY() + " " + p.getZ()
                + "§r (~" + dist + "m).");
        if (go) {
            AutoMineMod.pathing().setGoalAndPath(new GoalBlock(p.above()));
        } else {
            logDirect("§7Go with: §f#goto " + p.getX() + " " + p.getY() + " " + p.getZ());
        }
    }
}
