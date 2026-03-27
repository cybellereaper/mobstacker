# MobStacker

![MobStacker Icon](./assets/mobstacker-icon.png)

A PaperMC plugin that merges nearby mobs into larger, denser, more rewarding stacks.

## Overview

MobStacker reduces mob clutter by combining nearby mobs of the same type into a single representative entity.  
That stacked mob grows visually, becomes tougher, hits harder, and drops more loot based on the stack size.

This keeps farms and natural mob groups cleaner while still preserving the value of the mobs that were merged.

## Features

- Automatic nearby mob merging
- Two merge behaviors:
  - `ALWAYS` — every eligible merge is applied
  - `RANDOM_CHANCE` — each eligible merge attempt rolls against `merge-chance`
- Configurable merge radius and scan interval
- Configurable maximum stack size
- Visual growth as stacks increase
- Increased health, damage, and knockback resistance per stacked mob
- Health cap support to avoid invalid Bukkit health values
- Loot and experience scaling based on stack size
- Entity blacklist support for bosses and special mobs
- Built for modern Paper with Java 21

## How It Works

When the plugin scans the world, it looks for eligible nearby mobs of the same type.

When two eligible mobs merge:

1. One mob becomes the stack representative
2. The other mob is absorbed into the stack
3. The representative mob is updated with:
   - a larger stack count
   - scaled stats
   - a visible custom name such as `Zombie x12`
4. On death, drops and experience can be multiplied by the stack size

## Merge Modes

### `ALWAYS`

Every valid merge is applied immediately when checked.

Use this if you want aggressive cleanup and dense stacked mobs as quickly as possible.

### `RANDOM_CHANCE`

Each valid merge attempt rolls against the configured `merge-chance`.

Use this if you want more natural-looking stacking and slower consolidation.

Example:

```yml
merge-mode: RANDOM_CHANCE
merge-chance: 0.35
```

With the example above, each eligible merge attempt has a 35% chance to happen.

## Requirements

- Java 21
- Paper 1.21.11+

## Installation

1. Build the plugin or download the compiled JAR
2. Place the JAR in your server's `plugins/` folder
3. Start the server
4. Edit `plugins/MobStacker/config.yml`
5. Restart the server or reload the plugin through your normal deployment workflow

## Build From Source

```bash
./gradlew build
```

The compiled JAR will be generated in:

```text
build/libs/
```

## Configuration

Default configuration:

```yml
merge-radius: 8.0
merge-interval-ticks: 100
max-stack-size: 64

merge-mode: ALWAYS
merge-chance: 0.35

extra-health-per-mob: 0.35
extra-damage-per-mob: 0.10
extra-knockback-resistance-per-mob: 0.04

scale-step: 0.22
max-scale: 3.0
max-slime-size: 10
max-applied-health: 1024.0

multiply-drops: true
multiply-experience: true

blacklist:
  - ENDER_DRAGON
  - WITHER
  - WARDEN
```

### Configuration Reference

| Key | Purpose |
|---|---|
| `merge-radius` | Radius used to find merge candidates |
| `merge-interval-ticks` | How often the merge scan runs |
| `max-stack-size` | Hard limit for one stack |
| `merge-mode` | `ALWAYS` or `RANDOM_CHANCE` |
| `merge-chance` | Chance per valid merge attempt when using `RANDOM_CHANCE` |
| `extra-health-per-mob` | Additional health scaling per stacked mob |
| `extra-damage-per-mob` | Additional damage scaling per stacked mob |
| `extra-knockback-resistance-per-mob` | Additional knockback resistance per stacked mob |
| `scale-step` | Visual growth step as the stack grows |
| `max-scale` | Maximum visual scale for non-slime mobs |
| `max-slime-size` | Maximum slime size when slimes stack |
| `max-applied-health` | Safety cap for applied max health |
| `multiply-drops` | Whether drops scale with stack size |
| `multiply-experience` | Whether EXP scales with stack size |
| `blacklist` | Entity types that should never stack |

## Notes

- Stacking is intended for hostile mobs and other eligible mob entities
- Bosses are blacklisted by default
- Slimes should be tuned separately because their size has gameplay side effects
- Very high scaling values can make stacked mobs much more dangerous than vanilla

## Current Project Structure

- `src/main/java/com/github/cybellereaper`
- `src/main/resources/plugin.yml`
- `src/main/resources/config.yml`

At the moment, the plugin is configuration-driven and does not expose player commands or permissions in `plugin.yml`.

## Roadmap Ideas

- Commands for reload and debug
- Per-entity stacking rules
- Partial stack peeling on death
- Hologram-style display options
- Economy or farming integration hooks

## License

GPL-3.0
