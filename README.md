# Kimon

> An original anime-style **Power / energy progression** mod for Minecraft.
> Train your Power, pick a race, charge up, and feel every hit grow stronger.

[![Build](https://github.com/fernandosilvot/kimon/actions/workflows/build.yml/badge.svg)](https://github.com/fernandosilvot/kimon/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
![Minecraft](https://img.shields.io/badge/Minecraft-26.2-brightgreen)
![NeoForge](https://img.shields.io/badge/NeoForge-26.2.0.88-orange)
![Java](https://img.shields.io/badge/Java-25-red)
![Tests](https://img.shields.io/badge/tests-54%20passing-brightgreen)

Kimon is a from-scratch RPG progression mod inspired by the *feel* of classic anime-fighter mods
(train, grow stronger, power up, transform) — built as a **clean-room reimplementation** with
**100% original names, terms, and assets** to stay clear of any third-party intellectual property.
There is no "Saiyan", no "Kamehameha", no Dragon Ball branding: just an original *Power* stat,
original races, and its own vocabulary. See [`docs/DESIGN.md`](docs/DESIGN.md) for the design and
the legal stance.

---

## Table of contents

- [Features](#features)
- [Controls](#controls)
- [Debug / admin commands](#debug--admin-commands)
- [Gameplay walkthrough](#gameplay-walkthrough)
- [Races & classes](#races--classes)
- [Attributes & derived stats](#attributes--derived-stats)
- [Requirements](#requirements)
- [Install (play)](#install-play)
- [Build from source](#build-from-source)
- [Architecture](#architecture)
- [Testing](#testing)
- [Development workflow (GitFlow)](#development-workflow-gitflow)
- [Roadmap](#roadmap)
- [Legal / originality](#legal--originality)

---

## Features

Everything below is implemented, unit-tested where it has logic, and verified to load in-game on
Minecraft 26.2 / NeoForge 26.2.0.88.

| Feature | Status |
| --- | --- |
| Per-player **Power** stat, persisted across relog & death, synced to the client | ✅ |
| **Train** keybind (<kbd>G</kbd>): raises Power `+5` and earns Training Points | ✅ |
| Power scales **max health / attack damage / movement speed** in tiers | ✅ |
| **Six attributes** — Strength, Agility, Vitality, Energy, Focus, Spirit | ✅ |
| **Training Points economy** with a rising per-point cost curve | ✅ |
| **Character sheet GUI** (<kbd>K</kbd>) to spend TP on attributes | ✅ |
| Attributes drive real combat stats (Vitality→health, Strength→damage, Agility→speed) | ✅ |
| **Six races** (Human, Titan, Sage, Frost, Mystic, Hybrid) with unique starting spreads & modifiers | ✅ |
| **Three classes** (Warrior, Brawler, Channeler) that further tweak modifiers | ✅ |
| **Release %** charge mechanic (hold <kbd>C</kbd>) that scales combat output | ✅ |
| **Energy & Stamina** resources with regen (Energy regens faster at low Release) | ✅ |
| Melee damage scales with **Strength × Release**; action-bar **"Hit for X"** feedback | ✅ |
| **HUD** showing Race/Class, Power, Tier, Release %, Energy, Stamina (hidden under F3) | ✅ |
| **`/kimon` debug/admin command** to set progression without grinding | ✅ |
| Server-authoritative logic (the client cannot forge values) | ✅ |
| Automatic server → client sync of all stats | ✅ |
| English + Spanish localization | ✅ |
| 54 JUnit unit tests, GitHub Actions CI (build + test with caching) | ✅ |

**Design discipline:** features are added **one vertical slice at a time**. Each slice is complete
and playable, with its logic unit-tested, before the next begins — rather than many half-finished
systems. See the [Roadmap](#roadmap).

---

## Controls

| Key | Action |
| --- | --- |
| <kbd>G</kbd> | **Train** — raise Power and earn Training Points |
| <kbd>K</kbd> | Open the **Character Sheet** to spend Training Points on attributes |
| <kbd>C</kbd> (hold) | **Charge** — power up; your Release % rises (and decays when released) |
| <kbd>F3</kbd> | Vanilla debug screen — hides the Kimon HUD while held |

All keys are rebindable under **Options → Controls → Miscellaneous**.

---

## Debug / admin commands

Op-level (`/op <you>` or a creative world with cheats) `/kimon` commands let you test progression
instantly, without grinding:

```
/kimon info                 # show race, class, Power, TP, attributes and derived stats
/kimon tp <amount>          # grant Training Points
/kimon power <value>        # set your Power
/kimon attr <name> <value>  # set an attribute: strength|agility|vitality|energy|focus|spirit
/kimon race <race>          # set race: human|titan|sage|frost|mystic|hybrid (reseeds attributes)
/kimon class <class>        # set class: warrior|brawler|channeler
/kimon reset                # reset character to defaults
```

Mutating subcommands require op (permission level ≈ gamemaster); `/kimon info` is available to
everyone.

---

## Gameplay walkthrough

A quick tour that exercises every system:

1. **Launch** a world (enable cheats so you can use `/kimon`).
2. **Check the HUD** (top-left): your Race/Class, `Power`, `Tier`, `Release %`, `Energy`, `Stamina`.
3. **Train**: press <kbd>G</kbd> a few times — Power and Training Points go up.
4. **Spend TP**: press <kbd>K</kbd> and raise attributes with the `+` buttons. Vitality adds hearts,
   Strength adds damage, Agility adds speed. (Or shortcut it: `/kimon tp 100000`.)
5. **Pick an identity**: `/kimon race titan` + `/kimon class warrior` for a melee bruiser, or
   `/kimon race sage` + `/kimon class channeler` for an energy build. `/kimon info` shows the effect.
6. **Power up and hit**: hold <kbd>C</kbd> to charge your Release % to the max (watch the HUD), then
   left-click a mob. The action bar shows **`Hit for X`** — compare hitting at 0% Release vs. fully
   charged to feel the Strength × Release scaling.

### "See how hard you hit"

Landing a melee blow prints `Hit for <damage>` on your action bar (the real inflicted damage). This
is driven server-side from `LivingDamageEvent.Post`, so it reflects the true value after armor and
all modifiers.

---

## Races & classes

Each **race** sets your starting attribute spread and a per-attribute percent modifier; each
**class** adds further modifiers. Combined, they shape every derived stat. All names and numbers are
original to Kimon.

| Race | Identity | Starting lean |
| --- | --- | --- |
| **Human** | Balanced all-rounder | even across the board |
| **Titan** | Hardest-hitting melee | high Strength & Energy |
| **Sage** | Strongest energy user | huge Energy & Spirit, low body |
| **Frost** | Defensive & fast | high Vitality, Agility, Spirit |
| **Mystic** | Agile, regen-focused | high Agility |
| **Hybrid** | Between balanced and offensive | Strength + Energy |

| Class | Lean |
| --- | --- |
| **Warrior** | more physical power, less energy |
| **Brawler** | balanced, slightly more stamina/focus |
| **Channeler** | more energy power & spirit, less physical |

---

## Attributes & derived stats

| Attribute | Governs |
| --- | --- |
| **Strength** | Melee damage (scaled by Release) |
| **Agility** | Movement speed, defense |
| **Vitality** | Max health (and Stamina pool) |
| **Energy** | Energy-attack power *(used by upcoming energy attacks)* |
| **Focus** | Raises your Release ceiling (50% → up to 100%) |
| **Spirit** | Max Energy pool |

Only attribute points **above the starting value** contribute bonuses, so a brand-new character
plays like vanilla and grows from there. All balancing math lives in pure, unit-tested classes
(`StatBlock`, `StatCalculator`, `PowerState`, `PowerScaling`).

---

## Requirements

| | |
| --- | --- |
| Minecraft | **26.2** (Java Edition) |
| Mod loader | **NeoForge 26.2.0.88+** |
| Java (run & develop) | **JDK 25** (Mojang ships Java 25 with 26.2) |

## Install (play)

1. Install **NeoForge 26.2.0.88+** for Minecraft 26.2.
2. Build the jar (below) or grab it from a release, and drop `kimon-<version>.jar` into your
   instance's `mods/` folder.
3. Launch. Open a world and follow the [walkthrough](#gameplay-walkthrough).

## Build from source

```bash
# JDK 25 must be on JAVA_HOME. On macOS with Homebrew:
export JAVA_HOME="$(brew --prefix openjdk@25)/libexec/openjdk.jdk/Contents/Home"

./gradlew build          # compiles + runs tests, produces build/libs/kimon-<version>.jar
./gradlew test           # just the JUnit unit tests
./gradlew runClient      # launches a dev client with the mod loaded
```

> First build downloads and decompiles Minecraft 26.2; it can take several minutes and needs a few
> GB of RAM (the project sets `-Xmx3G`). Later builds are fast thanks to Gradle caching.

---

## Architecture

Kimon follows the standard NeoForge split between the **mod bus** (registration/lifecycle) and the
**game bus** (runtime events), keeps all client-only code behind a `Dist.CLIENT` gate so it never
loads on a dedicated server, and keeps every stat/resource computation **server-authoritative**.

```
net.kimon.kimon
├── Kimon.java                  # @Mod entry point; registers attachments on the mod bus
├── power/
│   ├── PowerData.java          # Power stat + tier math (pure, unit-tested)
│   ├── PowerScaling.java       # Power → tiered attribute bonuses (pure, unit-tested)
│   ├── PowerState.java         # Release %, Energy, Stamina loop (pure, unit-tested)
│   ├── PowerEffects.java       # applies Power-tier bonuses as vanilla attribute modifiers
│   ├── PowerEventHandler.java  # re-applies bonuses on login / respawn
│   ├── CombatHandler.java      # server tick loop; melee scaling; "Hit for X" feedback
│   └── ModAttachments.java     # POWER (persisted+synced) and STATE (synced) attachments
├── stats/
│   ├── Attribute.java          # the six attributes
│   ├── StatBlock.java          # attributes + Training Points economy (pure, unit-tested)
│   ├── StatCalculator.java     # attributes+profile → derived stats (pure, unit-tested)
│   ├── StatCodecs.java         # NBT + network codecs for StatBlock
│   ├── StatEffects.java        # applies attribute-derived bonuses to the player
│   ├── Race.java               # six races: starting spreads + modifiers
│   ├── PlayerClass.java        # three classes: modifiers
│   ├── CharacterProfile.java   # chosen race + class (persisted+synced)
│   └── ModStatAttachments.java # STATS and PROFILE attachments
├── network/
│   ├── TrainPowerPayload.java       # C→S: train
│   ├── RaiseAttributePayload.java   # C→S: spend TP on an attribute
│   ├── SetChargingPayload.java      # C→S: toggle charging (hold C)
│   └── ModNetworking.java           # registers payloads + server-side handlers
├── command/
│   └── KimonCommands.java      # /kimon debug/admin command tree
└── client/                     # @Dist.CLIENT only
    ├── KimonClient.java        # keybinds + per-tick input → payloads
    ├── StatsScreen.java        # character sheet GUI (26.2 extractRenderState pipeline)
    └── PowerHudLayer.java      # HUD overlay reading synced attachments
```

### Why it's server-authoritative

The client only ever sends **intents** (train, raise attribute, charging on/off). The server
validates and applies them, then NeoForge's synced data attachments push the authoritative result
back to the owning client, which the HUD/GUI simply read. A modified client cannot grant itself free
Training Points or stats.

### Data flow of one "train" action

```
[client] press G
   └─► ClientPacketDistributor.sendToServer(TrainPowerPayload)
          └─► [server] ModNetworking.handleTrain
                 ├─► POWER += 5           (PowerData, auto-synced)
                 ├─► PowerEffects.apply    (re-derive tier bonuses)
                 └─► STATS.trainingPoints += 5  (StatBlock, auto-synced)
                        └─► [client] HUD / Character Sheet read the synced values
```

---

## Testing

- **Unit tests** (`src/test/java`, 54 tests) cover all pure logic: Power tiers, the TP economy and
  cost curve, attribute-derived stats, the Release/Energy/Stamina loop, and race/class modifiers.
  They run on a plain JVM with no Minecraft bootstrap, so they are fast and reliable in CI.

  ```bash
  ./gradlew test
  ```

- **CI** (`.github/workflows/build.yml`) runs the tests and the full build with Gradle caching on
  every push / PR to `main` and `develop`.

- **In-game GameTests** are intentionally *not* included yet: Minecraft 26.2 replaced the old
  `@GameTest` annotation system with a registry-based `GameTestInstance` framework that needs
  datagen-registered test instances. Rather than ship that half-done, the in-game path is verified
  manually (and the hard logic is covered by the unit tests). Tracked as future work.

---

## Development workflow (GitFlow)

This repo follows [GitFlow](CONTRIBUTING.md):

- `main` — stable, released code (tagged, e.g. `v0.1.0`).
- `develop` — integration branch for the next release; **all current work lives here**.
- `feature/*` — one feature each, branched from `develop`, merged back with `--no-ff`, then deleted.

Feature branches are **deleted after merging** to keep the branch list clean; the merge commits on
`develop` preserve the history. See [CONTRIBUTING.md](CONTRIBUTING.md) for commit conventions
(Conventional Commits) and the full process.

---

## Roadmap

Implemented in phases, each a complete vertical slice. See [`docs/DESIGN.md`](docs/DESIGN.md) for the
full plan and [`docs/roadmap.svg`](docs/roadmap.svg) for the diagram.

**Done**
- ✅ **Phase 0** — Power stat, training keybind, HUD, networking, tests, CI.
- ✅ **Phase 1** — Power scales health / damage / speed in tiers.
- ✅ **Phase 2** — six attributes + Training Points economy, character sheet GUI, attributes drive stats.
- ✅ **Phase 3** — Release % charge, Energy/Stamina resources, melee scaling, "Hit for X" feedback.
- ✅ **Phase 4** — six races + three classes, `/kimon` debug commands.

**Next**
- 🔜 **Phase 5** — energy attacks: a projectile that consumes Energy and scales with Energy × Release.

**Later**
- ⏳ **Phase 6** — forms / transformations (multipliers + mastery).
- ⏳ Move races/forms/skills to **JSON datapacks** (currently enum-based) for server customization.
- 💤 **Phase 7** — world, masters, sagas, wishes (data-driven, long-horizon).
- 💤 Player animations (needs a 26.2 animation library) and in-game GameTests.

---

## Legal / originality

Kimon is an **original work** and is **not affiliated with, endorsed by, or derived from** any anime
franchise or any existing Minecraft mod. It is a clean-room reimplementation of *game mechanics*
(which are not copyrightable); all names, terminology, text, and assets are original to this project.
No third-party code or assets are used. Distributed under the [MIT License](LICENSE).

"Minecraft" is a trademark of Mojang Synergies AB. "NeoForge" is a project of the NeoForged team.
This mod is an independent, unofficial project.
