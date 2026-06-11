# AutoMine — a Baritone-style pathfinding / auto-mine mod for Minecraft 26.1.x (Fabric, client-side)

A from-scratch reimplementation of the core ideas behind
[Baritone](https://github.com/cabaletta/baritone): an A\* pathfinding engine, a movement
execution layer, goals, processes (mining, following), and a `#`-prefixed chat command system.
Client-side only — no server mod required.

> **Status:** working foundation — **compiles and builds cleanly against Minecraft 26.1.2**.
> The pathfinder, cost model, several movement types, the mine/follow processes, and the
> command system are implemented. Some advanced Baritone features (schematic building,
> selections, full backfill, elytra, precomputed-data tool selection) are registered as stubs
> and clearly marked.

## Download

Grab the latest `automine-*.jar` from the [**Releases**](../../releases) page, or from the
**Artifacts** of any [Actions run](../../actions). Then install it with Fabric (see below).

Every push is built by CI; pushing a `v*` tag (e.g. `v1.0.0`) publishes a Release with the jar
attached automatically.

## Target toolchain (Minecraft 26.1)

26.1 is the **first unobfuscated Minecraft release**, so this uses **official Mojang mappings
with no Yarn remap** and the new `net.fabricmc.fabric-loom` plugin.

| Thing            | Version                                  |
|------------------|------------------------------------------|
| Minecraft        | 26.1.2                                   |
| Java             | **25** (required)                        |
| Gradle           | 9.5.1 (via the wrapper)                  |
| Fabric Loom      | 1.16.3 (the new no-remap plugin)         |
| Fabric Loader    | 0.19.3                                   |
| Fabric API       | 0.151.0+26.1.2                           |

**26.1 unobfuscated mapping notes** (caught while porting): `ResourceLocation` is now
`net.minecraft.resources.Identifier`, and client chat output is `Player#sendSystemMessage(Component)`
(the old `displayClientMessage(Component, boolean)` is gone).

Sources: [Fabric for 26.1](https://fabricmc.net/2026/03/14/261.html) ·
[Porting to 26.1](https://docs.fabricmc.net/develop/porting/) ·
[Java Edition 26.1](https://minecraft.wiki/w/Java_Edition_26.1)

## Building

1. Install **JDK 25** and make sure `JAVA_HOME` points at it.
2. Set the exact **Fabric API** version in `gradle.properties` — open
   <https://modrinth.com/mod/fabric-api/versions>, copy the version string tagged for 26.1
   (looks like `0.140.x+26.1`), and paste it into `fabric_version`.
3. Generate the Gradle wrapper (one-time) with a local Gradle 9.4, or just run Gradle directly:
   ```powershell
   gradle build        # or: ./gradlew build  once the wrapper exists
   ```
4. The mod jar lands in `build/libs/`. Drop it into `.minecraft/mods` alongside Fabric API.

To run the dev client: `gradle runClient`.

## Commands

Type these in chat (they never reach the server). `#help` lists everything in-game.

| Command | Description |
|---|---|
| `#help [cmd]` | List commands / describe one |
| `#goto <x> <y> <z>` / `<x> <z>` / `<y>` / `<block>` | Path to coords, a Y level, or nearest block (supports `~` relative) |
| `#goal <x> <y> <z>` / `clear` | Set/clear the stored goal (no movement) |
| `#path` | Start pathing to the stored goal |
| `#mine <block> [block...]` | Auto-mine nearest matching blocks (e.g. `#mine diamond_ore iron_ore`) |
| `#follow players` / `entities` / `<name>` | Continuously path near a target |
| `#come` | Path to the block under your crosshair |
| `#thisway <distance>` | Goal that far in the direction you face |
| `#invert` | Flee from the current goal |
| `#explore [x z]` | Head outward to load chunks |
| `#set [name] [value]` | List / view / change settings |
| `#stop` (`#cancel`, `#halt`) | Stop everything, release inputs |
| `#version` | Show version |
| `#build`, `#sel`, `#surface`, `#farm`, `#render`, `#repack`, `#waypoints` | Recognised, not yet implemented (stubs) |

Settings (via `#set`): `allowBreak`, `allowPlace`, `allowSprint`, `allowParkour`,
`allowDiagonal`, `maxFallHeightNoWater`, `maxNodes`, `planningTimeoutMs`, `followRadius`,
`renderPath`.

## Architecture

```
AutoMineMod                 client entrypoint; ticks processes + pathing; routes # commands
├─ command/                 ArgConsumer, CommandManager, Command + commands/*
├─ goals/                   Goal + GoalBlock/XZ/YLevel/Near/Composite/RunAway
├─ pathing/
│  ├─ calc/                 AStarPathFinder, PathNode, BinaryHeapOpenSet (decrease-key heap)
│  ├─ movement/             ActionCosts, CalculationContext, MovementHelper, Moves (successors),
│  │                        Movement (shared executor) + movements/* (Traverse/Ascend/Descend/
│  │                        Fall/Diagonal/Pillar/Downward/Parkour)
│  └─ path/                 IPath, Path (parent-chain reconstruction)
├─ process/                 PathingBehavior (executor), MineProcess, FollowProcess
├─ control/                 InputOverrideHandler (mixin-free key forcing), PlacementHelper
└─ utils/                   BetterBlockPos, BlockStateInterface, Rotation(Utils), WorldScanner, ...
```

**How pathing works.** `Moves.getMovements()` is the A\* successor function: for a node it offers
every legal traverse/ascend/descend/fall/diagonal/parkour/pillar/downward edge, each with an
admissible tick cost from `ActionCosts`. `AStarPathFinder` runs f = g + h on a binary-heap open
set, bounded by a node/time budget, and returns the best partial path if the goal isn't reached.
`PathingBehavior` runs the search on a background thread, then each tick drives the current
`Movement`, which clears obstructing blocks then steers the player via `InputOverrideHandler`.

## Known limitations

Functional gaps vs. real Baritone (by design, as extension points): full backfill/`buildInLayers`,
schematic building, area selections, elytra/boat pathing, swimming/parkour-place, precise
per-tool mining-time and inventory-aware block selection, world caching/repacking, and goal
rendering. The structure mirrors Baritone so these slot in where the stubs already are.
