# Kimon

> An original anime-style **Power / energy progression** mod for Minecraft.
> Train your Power, pick a race, charge up, and feel every hit grow stronger.

[![Build](https://github.com/fernandosilvot/kimon/actions/workflows/build.yml/badge.svg)](https://github.com/fernandosilvot/kimon/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
![Minecraft](https://img.shields.io/badge/Minecraft-26.2-brightgreen)
![NeoForge](https://img.shields.io/badge/NeoForge-26.2.0.88-orange)
![Java](https://img.shields.io/badge/Java-25-red)
![Tests](https://img.shields.io/badge/tests-188%20passing-brightgreen)

Kimon is a from-scratch RPG progression mod inspired by the *feel* of classic anime-fighter mods
(train, grow stronger, power up, transform). It is a **clean-room reimplementation of the mechanics**:
none of Dragon Block C's code, textures, sounds or models are used. Names and terms deliberately follow
Dragon Ball's (Saiyan, Namekian, Ki, Release, Super Saiyan…) so players recognise everything. See
[`docs/DESIGN.md`](docs/DESIGN.md) for the design and the legal stance.

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
- [For AI agents / contributors picking this up](#for-ai-agents--contributors-picking-this-up)
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
| **Training Points by fighting**: hits (melee and Ki Blast) with Release ≥ 5% earn TP — `2 + 2·⌊Mind/5⌋·Release/100`, on a configurable hit chance. No free TP source | ✅ |
| Power scales **max health / attack damage / movement speed** in tiers | ✅ |
| **Six attributes** — Strength, Dexterity, Constitution, Willpower, Mind, Spirit | ✅ |
| **Training Points economy** with a rising per-point cost curve | ✅ |
| **Character sheet GUI** (<kbd>K</kbd>) to spend TP on attributes, and a **Skills** screen (<kbd>J</kbd>) | ✅ |
| **Skills as datapack JSON** (Jump, Dash, Fly, Endurance, Potential Unlock, Ki Sense): TP cost grows per level (`base × n`), every level also uses **Mind** from your Mind attribute; effects are data, so new skills need no code | ✅ |
| Attributes drive real combat stats (Constitution→health, Strength→damage, Dexterity→speed) | ✅ |
| **Six races** (Human, Saiyan, Namekian, Arcosian, Majin, Half-Saiyan) with unique starting spreads & modifiers | ✅ |
| **Three classes** (Warrior, Martial Artist, Spiritualist) that further tweak modifiers | ✅ |
| **Release %** state machine — charge (<kbd>C</kbd>), discharge (<kbd>Ctrl+C</kbd>), reset (<kbd>H</kbd>), turbo (<kbd>R</kbd>); states Stable / Charging / At max / Lowering / Exhausted; scales combat output | ✅ |
| **Character level** derived from attributes (every 5 points above 55 = 1 level), shown on the HUD and sheet | ✅ |
| **Attribute cost curve** (UC) configurable via `progression.cost*`; the sheet buys **+1 / +10 / +100 / +1000** points at once (server-validated, stops when you run out of TP) | ✅ |
| **Training load**: carry **Weighted Wraps / Vest / Heavy Plates** (10 / 25 / 50 weight) and stand near a **Gravity Device** (10G). Weight × gravity lowers melee damage, gravity lowers STR/DEX and makes you heavier, and both make TP likelier | ✅ |
| **Combat costs & regen lock**: an empowered hit (Release ≥ 5%) costs Energy (`1 + STR/200`) and Stamina, with vanilla damage if you can't pay; being hurt by a living entity stops Energy regen for 30 s (configurable) | ✅ |
| **Throttled sync**: your resources reach your client every 2 ticks and only when changed; neighbours get a coarse aura state; full re-sync on login, respawn and dimension change | ✅ |
| **Config** (`kimon-server.toml` / `kimon-client.toml`): Release/Energy balance is tunable, no hardcoded numbers | ✅ |
| **Energy & Stamina** resources with regen (Energy regens faster at low Release) | ✅ |
| Melee damage scales with **Strength × Release**; action-bar **"Hit for X"** feedback | ✅ |
| **Energy Blast** attack (<kbd>B</kbd>): raycast that consumes Energy and scales with Energy × Release | ✅ |
| **Forms / transformations** (<kbd>G</kbd> up; <kbd>H</kbd> reverts): multiply combat damage, drain Energy, gated by Power tier + Release | ✅ |
| **Form Mastery**: forms grow stronger (+damage) and cheaper (−drain) the more you use them | ✅ |
| **Training Altar** block: a training dummy — right-click counts as a hit (needs Release ≥ 5%, costs Stamina, chance of TP; no Power) | ✅ |
| **Wish Orb** item: right-click to be granted a random wish (Power and/or Training Points) | ✅ |
| **Races & classes as datapack JSON** (6 + 3 shipped, numbers from the research tables); add your own without code | ✅ |
| **HUD** showing Race/Class, active Form + Mastery, Power, Tier, Release %, Energy, Stamina | ✅ |
| **`/kimon` debug/admin command** to set progression without grinding | ✅ |
| Server-authoritative logic (the client cannot forge values) | ✅ |
| Automatic server → client sync of all stats | ✅ |
| English + Spanish localization | ✅ |
| 76 JUnit unit tests, GitHub Actions CI (build + test with caching) | ✅ |

**Design discipline:** features are added **one vertical slice at a time**. Each slice is complete
and playable, with its logic unit-tested, before the next begins — rather than many half-finished
systems. See the [Roadmap](#roadmap).

---

## Controls

| Key | Action |
| --- | --- |
| <kbd>K</kbd> | Open the **Character Sheet** to spend Training Points on attributes (each point also raises Power) |
| <kbd>C</kbd> (hold) | **Charge** — Release % rises (slower past 50%); with no input it holds, costing Energy |
| <kbd>Ctrl</kbd>+<kbd>C</kbd> (hold) | **Discharge** — Release % drops (wins over Charge) |
| <kbd>R</kbd> (hold) | **Turbo** — faster charge at an extra Energy cost |
| <kbd>H</kbd> | **Reset** — Release to 0% and revert to Base form instantly |
| <kbd>J</kbd> | Open the **Skills** screen (learn skills with TP and Mind) |
| <kbd>Y</kbd> | **Fly** — toggle flight (needs the Fly skill; drains Ki while you fly) |
| <kbd>V</kbd> | **Dash** — burst back, or left/right if you hold A/D (needs Dash; costs Ki, has a cooldown) |
| <kbd>B</kbd> | **Energy Blast** — fire a ranged energy attack along your view (costs Energy) |
| <kbd>G</kbd> | **Transform up** — ascend to the next form (needs Power tier + Release) |
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
/kimon attr <name> <value>  # set an attribute: strength|dexterity|constitution|willpower|mind|spirit (str/dex/con/wil/mnd/spi work too)
/kimon race <race>          # set race: human|saiyan|namekian|arcosian|majin|half_saiyan (reseeds attributes)
/kimon class <class>        # set class: warrior|martial_artist|spiritualist
/kimon form <form>          # force a form: base|super_saiyan|super_saiyan_2|super_saiyan_3
/kimon skill <skill> <lvl>  # set a skill level: jump|dash|fly|endurance|potential_unlock|ki_sense
/kimon mastery <form> <lvl> # set mastery level for a form (super_saiyan|super_saiyan_2|super_saiyan_3)
/kimon reset                # reset character to defaults
```

Mutating subcommands require op (permission level ≈ gamemaster); `/kimon info` is available to
everyone.

---

## Gameplay walkthrough

A quick tour that exercises every system:

1. **Launch** a world (enable cheats so you can use `/kimon`).
2. **Check the HUD** (top-left): your Race/Class, `Power`, `Tier`, `Release %`, `Energy`, `Stamina`.
3. **Earn TP by fighting**: hold <kbd>C</kbd> until Release is above 5%, then hit a mob. Some hits show `(+N TP)` on the action bar. At 0% Release you earn nothing.
4. **Spend TP**: press <kbd>K</kbd> and raise attributes with the `+` buttons. Constitution adds hearts,
   Strength adds damage, Dexterity adds speed. (Or shortcut it: `/kimon tp 100000`.)
5. **Pick an identity**: `/kimon race saiyan` + `/kimon class warrior` for a melee bruiser, or
   `/kimon race namekian` + `/kimon class spiritualist` for an energy build. `/kimon info` shows the effect.
6. **Power up and hit**: hold <kbd>C</kbd> to charge your Release % to the max (watch the HUD state: Charging → At max; <kbd>Ctrl</kbd>+<kbd>C</kbd> lowers it, <kbd>H</kbd> resets it), then
   left-click a mob. The action bar shows **`Hit for X`** — compare hitting at 0% Release vs. fully
   charged to feel the Strength × Release scaling.
7. **Fire an Energy Blast**: with some Release charged, press <kbd>B</kbd> while looking at a target.
   It consumes Energy and deals damage scaling with your Energy attribute × Release. An energy build
   (`/kimon race namekian` + `/kimon class spiritualist`, high Willpower attribute) hits hardest.
8. **Transform**: charge your Release, then press <kbd>G</kbd> to ascend to the next **form**
   (Super Saiyan → Super Saiyan 2 → Super Saiyan 3). Forms multiply all your combat damage but drain Ki per second and
   need enough Power tier + Release — if Energy runs out or Release drops, you revert to Base.
   Press <kbd>H</kbd> to revert (it also resets Release). The active form shows on the HUD.
9. **Build Mastery**: just by spending time in a form, its **Mastery** rises (shown on the HUD).
   Higher mastery means more damage and less Energy drain for that form — so a well-practised Super Saiyan
   can rival a fresh Super Saiyan 2 while costing less. (Shortcut: `/kimon mastery super_saiyan_3 50`.)
10. **Train at an altar**: craft a **Training Altar** (amethyst shards around obsidian; find it in the
    Kimon creative tab too), place it, charge your Release (≥ 5%) and right-click it. It acts as a
    training dummy: each use costs Stamina and has a chance to grant TP, like a hit would.
11. **Make a wish**: craft a **Wish Orb** (ender eye + amethyst + gold) and right-click it to be
    granted a random wish — a boon of Training Points, a surge of Power, or a mix. The orb is
    consumed. Great for a quick power spike.

### "See how hard you hit"

Landing a melee blow prints `Hit for <damage>` on your action bar (the real inflicted damage). This
is driven server-side from `LivingDamageEvent.Post`, so it reflects the true value after armor and
all modifiers.

---

## Races & classes

Each **race** sets your starting attributes (60 points, so everyone is level 1) and percent modifiers
on the derived stats; each **class** adds more modifiers on top. All of it is **data**: shipped as JSON
under `data/kimon/races/` and `data/kimon/classes/`, so a datapack can change or add races and classes
without code. The numbers follow the design research's tables.

| Race | Identity | Starting lean |
| --- | --- | --- |
| **Human** | Balanced all-rounder | even across the board; high stamina |
| **Saiyan** | Hardest-hitting melee | high Strength & Willpower |
| **Namekian** | Strongest Ki user | huge Spirit & Mind, tough body |
| **Arcosian** | Defensive & fast | high Constitution & Spirit; best defense and speed |
| **Majin** | Agile, stamina-rich | high Dexterity |
| **Half-Saiyan** | Between balanced and offensive | Willpower-leaning |

| Class | Lean |
| --- | --- |
| **Warrior** | more melee, body and stamina; less energy and speed |
| **Martial Artist** | the baseline (no changes) |
| **Spiritualist** | more Ki, defense and speed; less melee, body and stamina |

### Adding your own (datapack)

Put JSON files in a datapack (or the mod's resources):

```
data/<namespace>/races/<name>.json      → race id  <namespace>:<name>
data/<namespace>/classes/<name>.json    → class id <namespace>:<name>
```

```json
// races/orc.json — all six attributes are required
{
  "attributes": { "strength": 20, "dexterity": 5, "constitution": 15, "willpower": 5, "mind": 5, "spirit": 10 },
  "modifiers":  { "melee": 25, "body": 10, "run": -10 }
}
// classes/berserker.json — a class only has modifiers
{ "modifiers": { "melee": 20, "defense": -15 } }
```

Modifiers are percents and every one is optional (default 0): `melee`, `defense`, `body`, `stamina`,
`ki_power`, `max_ki`, `run`, `fly`. (`defense` and `fly` are stored already but only take effect once
those stats exist.) Race + class modifiers add up. Run `/reload` after changing a datapack, then
`/kimon race mypack:orc`. Invalid files are skipped with a message in the log; if nothing valid is
found the built-in set stays in use. Names show as the id's path unless you add
`race.<namespace>.<name>` / `class.<namespace>.<name>` to a language file.

---

## Skills

Skills are bought with **TP** and limited by **Mind**: every skill level uses Mind points, and your budget
is your Mind attribute (`skills.mindPerPoint`, default 1), so Mind decides how many levels you can hold.
Reaching level *n* costs `tp_base × n` TP (`tp_base + tp_per_level × (n-1)` in general). For now you learn
them from the Skills screen; teaching them through master NPCs comes with the world step.

| Skill | TP / Mind | What it does |
| --- | --- | --- |
| **Potential Unlock** | 400 / 10 | +5% Release ceiling per level (50% → 100% at level 10) |
| **Endurance** | 150 / 10 | −3% damage taken per level (30% at level 10) |
| **Jump** | 40 / 5 | +10% jump strength and +1 safe-fall block per level |
| **Dash** | 40 / 5 | burst back/left/right on the ground; stronger per level (costs 2% of your max Ki, 1 s cooldown) |
| **Fly** | 60 / 10 | flight, +10% speed per level; drains 2 Ki per second while flying |
| **Ki Sense** | 300 / 10 | the entity you look at (up to 10 blocks per level) is read out at the top of the screen |

Skills are data: `data/<namespace>/skills/<name>.json`.

```json
{
  "max_level": 10,
  "cost": { "tp_base": 150, "tp_per_level": 150, "mind": 10 },
  "effects": [ { "type": "damage_reduction", "per_level": 3 } ]
}
```

`tp_per_level` defaults to `tp_base`, `mind` to 0, `max_level` to 10. Effect types: `release_cap`,
`damage_reduction`, `jump_boost`, `safe_fall`, `flight`, `dash`, `ki_sense`; the value is
`base + per_level × level`, and any skill can add to any type. Not done yet: Ki Sense's lock-on (Z) and
Dash's "swoop" in flight.

---

## Attributes & derived stats

| Attribute | Governs |
| --- | --- |
| **Strength** | Melee damage (scaled by Release) |
| **Dexterity** | Movement speed, defense |
| **Constitution** | Max health (and Stamina pool) |
| **Willpower** | Ki-attack power (Ki Blast) |
| **Mind** | TP per hit, and your **Mind budget** for skills (each skill level uses Mind) |
| **Spirit** | Max Ki pool |

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
├── skill/
│   ├── SkillDef.java / SkillEffect.java   # a skill and its effects (JSON codecs)
│   ├── SkillCatalog.java / SkillData.java # all skills; a player's levels
│   ├── SkillRules.java / SkillEffects.java # learning (TP + Mind) and effect totals (pure, unit-tested)
│   └── SkillHandler / FlightHandler / DashHandler / SkillDataLoader  # server glue
├── training/
│   ├── TrainingLoad.java       # weight + gravity (pure)
│   ├── TrainingEffects.java    # what the load does (pure, unit-tested)
│   ├── TrainingParams.java     # tunable values (pure)
│   ├── TrainingHandler.java    # server: computes the load every second
│   └── WeightItem.java         # carried weights
├── config/
│   └── KimonConfig.java        # SERVER (Release/Energy balance) + CLIENT (HUD) ModConfigSpec
├── power/
│   ├── PowerData.java          # Power stat + tier math (pure, unit-tested)
│   ├── PowerScaling.java       # Power → tiered attribute bonuses (pure, unit-tested)
│   ├── PowerState.java         # Release state machine + Energy/Stamina loop (pure, unit-tested)
│   ├── ReleaseState.java       # Stable / Charging / At max / Lowering / Exhausted
│   ├── CombatCosts.java        # Energy/Stamina cost of an empowered hit (pure, unit-tested)
│   ├── AuraState.java          # coarse aura info neighbours receive (pure)
│   ├── AuraCache.java          # client-side auras of nearby players
│   ├── SyncPolicy.java         # when to send state to clients (pure, unit-tested)
│   ├── SyncHandler.java        # server: throttled state sync + full re-sync on dimension change
│   ├── PowerParams.java        # balance values for the loop (pure; built from the config)
│   ├── KiRegenRate.java        # slow / normal / fast / faster Energy regen
│   ├── EnergyBlast.java        # energy-attack damage/cost rules (pure, unit-tested)
│   ├── Form.java               # transformation ladder: multipliers, drain, gating (pure, unit-tested)
│   ├── MasteryData.java        # per-form mastery: raises damage, lowers drain (pure, unit-tested)
│   ├── MasteryCodecs.java      # NBT + network codecs for MasteryData
│   ├── PowerEffects.java       # applies Power-tier bonuses as vanilla attribute modifiers
│   ├── PowerEventHandler.java  # re-applies bonuses on login / respawn
│   ├── CombatHandler.java      # server tick loop; melee + form×mastery scaling; blast; transform
│   └── ModAttachments.java     # POWER, STATE (synced), MASTERY (persisted+synced) attachments
├── stats/
│   ├── Attribute.java          # the six attributes
│   ├── StatBlock.java          # attributes + Training Points economy (pure, unit-tested)
│   ├── StatCalculator.java     # attributes+profile → derived stats (pure, unit-tested)
│   ├── StatCodecs.java         # NBT + network codecs for StatBlock
│   ├── StatEffects.java        # applies attribute-derived bonuses to the player
│   ├── RaceDef.java / ClassDef.java / StatMods.java   # race, class and modifier data + JSON codecs
│   ├── CharacterCatalog.java   # every known race/class by id; built-in fallback
│   ├── CharacterDataLoader.java / CharacterDataHandler.java   # datapack reload + sync to clients
│   ├── CostParams.java         # attribute cost curve (pure, unit-tested)
│   ├── LevelCalculator.java    # level from attribute points (pure, unit-tested)
│   ├── CharacterProfile.java   # chosen race + class (persisted+synced)
│   └── ModStatAttachments.java # STATS and PROFILE attachments
├── block/
│   ├── TrainingAltarBlock.java # training dummy: costs Stamina, chance of TP, server-authoritative
│   └── ModBlocks.java          # registers blocks, items (incl. Wish Orb), the Kimon creative tab
├── wish/
│   ├── Wish.java               # wish reward table (pure, unit-tested)
│   └── WishOrbItem.java        # consumable: right-click grants a random wish
├── network/
│   ├── RaiseAttributePayload.java   # C→S: spend TP on an attribute
│   ├── PowerSyncPayload.java        # S→C: your Release/Energy/Stamina (every 2 ticks, if changed)
│   ├── AuraPayload.java             # S→C: a nearby player's aura bucket
│   ├── ChargeInputPayload.java      # C→S: held charge / discharge / turbo flags
│   ├── ResetReleasePayload.java     # C→S: reset Release + revert form
│   ├── FireBlastPayload.java        # C→S: fire an Energy Blast
│   ├── TransformPayload.java        # C→S: transform up / down
│   └── ModNetworking.java           # registers payloads + server-side handlers
├── command/
│   └── KimonCommands.java      # /kimon debug/admin command tree
└── client/                     # @Dist.CLIENT only
    ├── KimonClient.java        # keybinds + per-tick input → payloads
    ├── StatsScreen.java        # character sheet GUI (26.2 extractRenderState pipeline)
    └── PowerHudLayer.java      # HUD overlay reading synced attachments
```

### Why it's server-authoritative

The client only ever sends **intents** (raise attribute, charge/discharge/turbo, reset, fire blast, transform). The server
validates and applies them, then NeoForge's synced data attachments push the authoritative result
back to the owning client, which the HUD/GUI simply read. A modified client cannot grant itself free
Training Points or stats.

### Data flow of earning and spending TP

```
[client] left-click a mob (vanilla attack, no Kimon packet)
   └─► [server] CombatHandler.onDamagePost
          ├─► needs Release ≥ 5%  ──(else no TP)
          ├─► TpGain.forHit(Mind, Release, roll)   (pure, unit-tested)
          └─► STATS.trainingPoints += tp           (StatBlock, auto-synced)

[client] press + on the Character Sheet
   └─► ClientPacketDistributor.sendToServer(RaiseAttributePayload)
          └─► [server] ModNetworking.handleRaiseAttribute
                 ├─► StatBlock.raise  (spends TP, validated server-side)
                 ├─► POWER += powerPerPoint  (tier bonuses + form gating)
                 └─► StatEffects / PowerEffects re-derive vanilla attributes
```

---

## Testing

- **Unit tests** (`src/test/java`, 188 tests) cover all pure logic: Power tiers, the TP economy and
  cost curve, attribute-derived stats, the Release/Energy/Stamina loop, race/class modifiers, the
  Energy Blast damage/cost rules, the form ladder, Form Mastery, and the Wish reward table.
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

## For AI agents / contributors picking this up

If you're an AI agent (Claude, Kiro, etc.) or a new contributor, start with
**[`docs/CONTINUE_HERE.md`](docs/CONTINUE_HERE.md)** — a complete handoff covering the current
state, architecture, Minecraft 26.2 API gotchas, the GitFlow workflow, and suggested next steps.
See also [`AGENTS.md`](AGENTS.md), [`CLAUDE.md`](CLAUDE.md), and the standing rules in
`.kiro/steering/`.

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

![Kimon roadmap](docs/roadmap.svg)

Implemented in phases, each a complete vertical slice. See [`docs/DESIGN.md`](docs/DESIGN.md) for the
full plan and [`docs/roadmap.svg`](docs/roadmap.svg) for the diagram source.

**Done**
- ✅ **Phase 0** — Power stat, training keybind, HUD, networking, tests, CI.
- ✅ **Phase 1** — Power scales health / damage / speed in tiers.
- ✅ **Phase 2** — six attributes + Training Points economy, character sheet GUI, attributes drive stats.
- ✅ **Phase 3** — Release % charge, Energy/Stamina resources, melee scaling, "Hit for X" feedback.
- ✅ **Phase 4** — six races + three classes, `/kimon` debug commands.
- ✅ **Phase 5** — Energy Blast: ranged attack consuming Energy, scaling with Energy × Release.
- ✅ **Phase 6** — forms / transformations: multiply combat damage, drain Energy, gated by tier + Release.
- ✅ **Form Mastery** — forms grow stronger (+damage) and cheaper (−drain) the more you use them.
- ✅ **Phase 7 (started)** — **Training Altar** block + **Wish Orb** item: a craftable world block
  you right-click to train, and a consumable that grants a random progression wish.

**Next**
- 🔜 More Phase 7 — master NPCs that teach skills/forms, and data-driven sagas.

**Later**
- ⏳ A visual projectile entity for the Energy Blast; player aura/transform animations.
- ✅ Races and classes are **JSON datapacks**. ⏳ Forms and skills still to move to JSON.
- 💤 **Phase 7** — world, masters, sagas, wishes (data-driven, long-horizon).
- 💤 In-game GameTests on the 26.2 framework.

---

## Legal / originality

Kimon is a **fan project**, **not affiliated with, endorsed by, or derived from** Dragon Ball, its
rights holders, or Dragon Block C. It is a clean-room reimplementation of *game mechanics* (which are
not copyrightable): **no code, textures, models, sounds or configs from Dragon Block C or Dragon Ball are
used**. Names and terminology follow Dragon Ball's by the project owner's choice, so the game is
recognisable; "Dragon Ball" and its names belong to their owners, and a project that uses them can be
asked to change them. Keep it non-commercial. Distributed under the [MIT License](LICENSE).

"Minecraft" is a trademark of Mojang Synergies AB. "NeoForge" is a project of the NeoForged team.
This mod is an independent, unofficial project.
