# Ambitious Bees

Hardworking bees with a career. *Bee a good worker!*

An addon for **[Forestry: Community Edition](https://github.com/ForestryMC/ForestryCE)** (NeoForge 1.21.1).
Breed 44 new bees, each with a job: mining, healing, smelting, trading, policing the neighbourhood,
swapping blocks, and more.

> Ambitious Bees is an independent project. It is **not affiliated with** Forestry or with any other
> addon. It is a clean-room work: its functionality is based on public descriptions only, and the code,
> textures, names and numbers are original.

## Requirements

| | |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.251 or newer |
| Forestry: Community Edition | 3.0.0-alpha6 or newer (tested with 3.0.0-alpha9) |
| Java | 21 |

Forestry CE is still in alpha, so a Forestry update may break this mod until it is updated.

## What is in the mod

### Bees
Every bee has its own mutation tree, a hand-drawn overlay and an effect that runs inside its territory.
Some bees only exist to breed others (no effect).

<details>
<summary>All 44 bees and their effects</summary>

| Bee | Effect |
|---|---|
| Student | No effect. Shift-right-click a Student drone onto a bee housing and you are alerted when its queen is gone |
| Collecting | Picks up items |
| Graduate | Retrains purebred Ambitious Bees on a Flower Pedestal (restores their default effect) |
| PHD | Same as Graduate |
| Artistic | Hangs paintings |
| Bensen | Plays jazz on note blocks |
| Armorer | Buffs armor |
| Sharpening | Sharpens weapons |
| Repair | Repairs items |
| Police | Hurts hostile mobs and collects the drops |
| Lumber | Strips logs |
| Husbandry | Breeds animals |
| Butcher | Kills surplus adult animals (keeps a breeding pair) |
| Smelter | Smelts items |
| Honey-Smelter | Turns an iron ingot on a pedestal into a Honey-Comb Ingot |
| Mason | Cobblestone to stone bricks |
| Ore Crusher | Crushes ores |
| Business | Sells items on a pedestal for emeralds |
| Junk Seller | Turns an emerald on a pedestal into a random loot item |
| Taxcollector | Takes loot without harming |
| Yente | Puts a princess and a drone back for breeding |
| Priest | Adds enchantments to items on a pedestal (slow and capped, see config) |
| Doctor | Heals players, animals and villagers |
| Plague | Cures harmful effects |
| Miner | Breaks ores and stores their drops |
| Silky Miner | Like the Miner, with Silk Touch |
| Fortunate Miner | Like the Miner, with Fortune III |
| Chef | Honey-glazes food |
| Thief | Steals items from players |
| Clockwork | Winds Forestry clockwork engines |
| Energy | Pushes FE into adjacent blocks |
| Electrician | Calls down lightning |
| Devil | Sets living things on fire |
| Assassin | Kills the queens of other bee housings in its territory |
| Mad Scientist | Explosions that wreck blocks but never hurt entities. Its mutation needs an explosion nearby |
| Quantum Charming | Swaps pairs of similar blocks (planks, logs, wool, stones...) |
| Quantum Strange | Swaps pairs of similar blocks (ores, leaves, sand, dirt, flowers...) |
| Temporal | Gives extra random ticks to blocks in its territory |
| N.C.A. | Cancels creeper explosions in its territory |
| Rainbow, Science, Engineer, Robot, Politician | No effect: steps of the mutation tree |

</details>

### Items and blocks
- **Flower Pedestal**: holds one item. Pedestal bees (Smelter, Business, Priest, ...) work on it.
  *Recipe: 3 sticks and any flower (the flower between two sticks, the third stick underneath).*
- **Bee-Bee Gun**: right click fires the effect of the selected queen in a cone in front of you for
  100 honey. Sneak + right click opens the loading screen (honey feed slot and six queen slots; right
  click a bee there to select it). Only queens whose effect can be fired on demand can be loaded.
- **Frames**: Mutation Frame (mutation chance x5), Eternal Frame (the queen almost never ages,
  but produces nothing) and Poison Frame (poisons everything around the housing, faster aging).
- **Honey-Comb Ingot**, **Yin Essence**, **Yang Essence** (rare products of the Quantum bees) and the
  **Triquetra** (no use yet).

Everything is in the *Ambitious Bees* creative tab, bees included.

## Configuration

`config/ambitious_bees-common.toml`:

| Option | What it does |
|---|---|
| `effects.disabled_effects` | List of effects that do nothing (also with the Bee-Bee Gun), e.g. `["thief", "devil"]` |
| `safety.affect_players` | `false`: Thief, Electrician, Devil and the Poison Frame ignore players |
| `safety.protect_pets_and_villagers` | `true`: harmful effects skip villagers, tamed, named and leashed mobs |
| `safety.respect_claims` | `true`: bees that change blocks ask claim/protection mods first (block break event) |
| `safety.mad_scientist_breaks_blocks` | `false`: the Mad Scientist makes noise but never destroys blocks |
| `temporal.extra_ticks_per_cycle` | Extra random ticks per Temporal bee cycle |
| `temporal.budget_per_game_tick` | Global cap on those ticks per game tick, for the TPS |

Effect numbers such as speed or throttle can also be changed per bee in a datapack by overriding
`data/ambitious_bees/bee_effect/*.json` and `bee_species/*.json` (for example, the products of a bee).

## Building

1. Install JDK 21.
2. Put the Forestry CE and Patchouli jars into `libs/` (see `libs/README.txt`; they are git-ignored).
3. Run `./gradlew build`. The jar is in `build/libs/`.
4. For development, open the folder in IntelliJ IDEA and run `runClient` or `runServer`.

## Contributing

Issues and pull requests are welcome, but please read [CONTRIBUTING.md](CONTRIBUTING.md) first:
this is a clean-room project, so only your own work can be accepted.

## License

[MIT](LICENSE) © 2026 RickWeek. This covers the code and the assets of this repository.
Forestry, Patchouli and other dependencies have their own licenses and are not part of this repository.
