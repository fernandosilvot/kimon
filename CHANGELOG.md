# Changelog

All notable changes to this project are documented here.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- **Races and classes as datapack JSON (doc 03, step 3).** `data/<ns>/races/*.json` and
  `data/<ns>/classes/*.json`, loaded by `CharacterDataLoader` on every (re)load and synced to clients
  (`CatalogPayload`) so the HUD derives the same stats as the server. New pure `RaceDef`, `ClassDef`,
  `StatMods` (Melee, Defense, Body, Stamina, Ki Power, Max Ki, Run, Fly) and `CharacterCatalog` with a
  built-in classpath fallback; invalid files are reported and skipped. The shipped data reproduces every
  race+class row of the research tables (classes are race-independent offsets from the baseline class).
  Commands (`/kimon race|class`) take any catalog id and tab-complete. Old saves with bare names
  (`"human"`) load as `kimon:human`. Protocol version 5.
- 13 new tests (`CharacterDataTest`, `CatalogPayloadTest`) checking the data against the tables,
  JSON validation, migration, the per-stat modifier columns and the network round trip: 159 total.
- **Progression (doc 03, step 2).**
  - **Character level**: every 5 attribute points above 55 is one level (a fresh character is level 1).
    Shown on the HUD and the sheet. (`LevelCalculator`)
  - **Attribute cost curve (UC)**, configurable: `cost = max(min, round(base + rate·x + x²/startMinus))`,
    `x = level·multiplier` (`progression.costBase/costRate/costMultiplier/costStartMinus/costMin`).
    (`CostParams`)
  - **Bulk buying**: the Character Sheet buys +1 / +10 / +100 / +1000 points per click; the server
    buys one at a time at the rising price and stops at the first unaffordable point
    (`StatBlock.raiseMany`). `RaiseAttributePayload` now carries a count; protocol version 4.
  - **Training load**: Weighted Wraps / Vest / Heavy Plates (weight 10 / 25 / 50, carried in the
    inventory) and the **Gravity Device** block (10G in a radius of 8). Weight × gravity lowers melee
    damage; gravity lowers STR/DEX (melee bonus and DEX-driven speed) and raises the real gravity
    attribute; both raise the chance of earning TP. `training.*` config keys; HUD and sheet show the
    load. (`TrainingLoad`, `TrainingEffects`, `TrainingHandler`, `WeightItem`, `LOAD` attachment)
- 22 new unit tests (`ProgressionTest`, `TrainingEffectsTest`): 146 total.
- **Regeneration lock.** After being hurt by a living entity, Energy stops regenerating for
  `combat.regenLockTicks` (default 600 = 30 s); `combat.staminaRegenLocked` (default false) extends it
  to Stamina. Server-only counter inside `PowerState` (`hurt()`), so it costs no bandwidth.
- **Hit costs.** An empowered melee hit (Release ≥ 5%) now costs Energy (`1 + STR/200`) and
  `combat.hitStaminaCost` (default 2%) of max Stamina. If you can't pay, the hit does vanilla damage
  (and earns no TP). New pure `CombatCosts`.
- **Throttled sync.** `PowerState` is no longer an auto-synced attachment; `SyncHandler` sends it to
  the owner every 2 ticks and only when it changed (`PowerSyncPayload`, `SyncPolicy`). Neighbours get
  an `AuraState` (charging / turbo / 10% Release bucket, never exact numbers) via `AuraPayload`, kept
  client-side in `AuraCache` (the aura *drawing* is a later phase).
- **Dimension re-sync.** Login, respawn and dimension change force a full resync of the resources,
  aura and the auto-synced attachments (works around NeoForge issue #2510).
- 17 new unit tests (`RegenLockTest`, `CombatCostsTest`, `SyncPolicyTest`): 124 total.
- **Config.** `kimon-server.toml` (synced to clients) with the Release/Energy balance —
  `release.baseMax`, `allowOvercharge`, `chargeRate`, `slowdownAbove50`, `turboMult`, `turboKiDrain`,
  `dischargeRate`, `upkeepFactor`, and `ki.perSPI`, `regenPct`, `regenRate`, `regenCutoffRelease`,
  `exhaustRecoverPct` — and `kimon-client.toml` (`hud.displayStep`). Values reach the pure logic as an
  immutable `PowerParams` snapshot. Defaults follow `docs/02-release-ki-stats.md`.
- **Release state machine.** `ReleaseState` (Stable / Charging / At max / Lowering / Exhausted),
  computed in `PowerState.tick` and synced for the HUD. Running out of Energy now locks the player
  in **Exhausted** (Release 0, input ignored) until Energy recovers to 5% of the maximum.
- **Release controls.** Charge (<kbd>C</kbd>), Discharge (<kbd>Ctrl+C</kbd>, wins over charge),
  Turbo (<kbd>R</kbd>, faster charge + extra Energy drain) and Reset (<kbd>H</kbd>, Release to 0 and
  revert form). New `ChargeInputPayload` and `ResetReleasePayload`.
- **Overcharge option** (`release.allowOvercharge`): Release ceiling 100% → 200%.
- 19 new unit tests (`ReleaseStateMachineTest`, `StatCalculator` overloads): 100 total.

### Changed
- **Race and class numbers now follow the research tables** (per derived stat: melee, body, stamina,
  max Ki, run, Ki power) instead of the old per-attribute guesses. The `Race` / `PlayerClass` enums are
  gone. Frost's starting Energy went from 5 to 10 so every race has 60 points.
- Attribute costs follow the new curve (a bit steeper at high levels than the old linear one).
- **No more free Training Points.** The Train button/packet is gone; the only organic TP source is
  now hitting things with Release ≥ 5% (melee and Energy Blast), per the research:
  `TP = 2 + 2·⌊FOCUS/5⌋·Release/100`, rolled against `tp.hitChance` (default 0.2). Against another
  player the target's FOCUS is used. New pure `TpGain`/`TpParams` (7 tests) and config keys
  `tp.baseAmount`, `tp.perFocusStep`, `tp.focusDivisor`, `tp.hitChance`.
- **Training Altar is a training dummy.** It needs Release ≥ 5%, costs `tp.altarStaminaCost` of max
  Stamina per use and only sometimes grants TP; it no longer grants Power.
- **Power comes from spending TP.** Each attribute point bought adds `progression.powerPerPoint`
  Power (default 5), so form gating by Power tier follows from fighting → TP → attributes.
- Action bar shows the TP earned: `Hit for X (+N TP)`.
- **Release no longer decays on its own.** With no input it holds its value (Energy upkeep still
  applies); only Discharge or Reset lowers it. Default rates now follow the research doc:
  charge 10 %/s (was 25), discharge 25 %/s, upkeep 0.002 (was 0.004), Energy regen 0.02 (was 0.04).
- **Keybinds.** Transform up moved from <kbd>R</kbd> to <kbd>G</kbd>; <kbd>H</kbd> reverts the form
  (replaces <kbd>V</kbd>); Train moved from <kbd>G</kbd> to a **Train** button on the Character Sheet.
- Network protocol version bumped to 3 (`SetChargingPayload` removed; two clientbound payloads added).
- Melee bonus now needs an active Release (≥ 5%); below that the hit is plain vanilla.
- HUD shows the Release state, steps Release by `hud.displayStep`, and colours it (white ≤ 50%,
  amber ≤ 100%, red when overcharged or exhausted).

- **Agent handoff docs** so any AI agent (Claude, Kiro, etc.) or contributor can continue the
  project cold: `docs/CONTINUE_HERE.md` (full state, architecture, 26.2 API gotchas, workflow,
  next slices), `AGENTS.md`, `CLAUDE.md`, and a `.kiro/steering/kimon-onboarding.md` session
  pointer. README now links them.
- **Wish Orb item (Phase 7).** A craftable consumable (ender eye + amethyst + gold) that, on
  right-click, grants a random `Wish` — a Training-Point boon, a Power surge, or a balanced mix —
  then is consumed. Server-authoritative roll. (`Wish` pure + 5 tests, `WishOrbItem`, client item
  definition + model, recipe, creative-tab entry, en/es names.)

## [0.3.0] - 2026-10-04

### Added
- **Training Altar block (Phase 7 start).** A craftable world block (amethyst shards + obsidian)
  that you right-click to train for a larger reward than the keybind (+15 Power, +15 TP),
  server-authoritative. Comes with blockstate/model/item model, loot table, recipe, a Kimon
  creative tab, and en/es names — giving progression a physical place in the world.
- **Form Mastery.** Each form now accumulates mastery while active, raising its damage multiplier
  (up to +0.25) and lowering its Energy drain (up to −40%) as you practise it. Mastery is per-form,
  persisted and synced, shown on the HUD next to the active form. (`MasteryData` pure + 8 tests,
  `MasteryCodecs`, `MASTERY` attachment, `/kimon mastery <form> <level>`.)
- **Forms / transformations.** Three forms above Base — Surge, Ascent, Zenith — that multiply all
  combat damage (melee and Energy Blast) while draining Energy per second, gated by Power tier and
  minimum Release. Transform up with <kbd>R</kbd>, down with <kbd>V</kbd>; running out of Energy or
  dropping below the required Release reverts you to Base. (`Form` pure + 9 tests, `TransformPayload`,
  `/kimon form <name>`.) The active form shows on the HUD.

## [0.2.0] - 2026-10-04

### Added
- **Energy Blast attack** (<kbd>B</kbd>): a server-side raycast along the player's view that
  consumes Energy and deals magic damage scaling with the Energy attribute × Release
  (`EnergyBlast`, pure + 5 tests; `FireBlastPayload`). Action-bar feedback on hit/miss/no-energy.
- **Races & classes.** Six races (Human, Titan, Sage, Frost, Mystic, Hybrid) each with an original
  starting attribute spread and per-attribute percent modifiers, and three classes (Warrior,
  Brawler, Channeler) that further tweak modifiers. Race + class feed every derived stat
  (`CharacterProfile`, persisted + synced + copy-on-death).
- **`/kimon` debug/admin command** (op level): `info`, `tp <amount>`, `power <value>`,
  `attr <name> <value>`, `race <race>`, `class <class>`, `reset` — inspect and set progression
  without grinding.
- HUD now shows your **Race / Class** at the top.
- 8 additional unit tests (`Race`, `PlayerClass`, `CharacterProfile`, modifier wiring).

### Added (previous, still unreleased)
- **Release % charge mechanic** (hold <kbd>C</kbd> to power up). Release scales combat output and
  decays when you stop charging. Higher Focus raises your Release ceiling (base 50% → up to 100%).
- **Live combat resources**: Energy (from Spirit) and Stamina (from Vitality), advanced each tick
  server-side (`PowerState`, pure + 9 tests). Energy regenerates faster the lower your Release —
  the classic "power up vs. recover" tension — and holding Release costs upkeep Energy.
- **Melee damage now scales with Strength × Release.** Power up before you fight to hit hard.
- **"See how hard you hit"**: landing a melee blow shows `Hit for X` on the action bar
  (`CombatHandler`, via `LivingDamageEvent.Post`).
- HUD now shows **Release %, Energy and Stamina** below Power/Tier.
- `SetChargingPayload` (C→S) toggles the charge state; the server advances Release authoritatively.
- 14 additional unit tests (`PowerState`, resource/Release maxima, melee scaling).

### Added (previous, still unreleased)
- **Character sheet GUI** (press <kbd>K</kbd>): shows the six attributes, unspent Training Points,
  and the TP cost to raise each attribute, with a "+" button per attribute. Server-authoritative —
  the client only requests raises via a `RaiseAttributePayload`; the server validates affordability.
- **Training now earns Training Points.** Pressing <kbd>G</kbd> grants TP (in addition to raising
  Power), which are spent in the character sheet to raise attributes.
- **Attributes now drive real combat stats** (`StatCalculator` + `StatEffects`): Vitality → max
  health, Strength → attack damage, Agility → movement speed, applied as vanilla attribute
  modifiers. Only points above the starting value contribute, so new characters play like vanilla.
- `StatBlock` is now persisted + synced + copied-on-death via a data attachment (`ModStatAttachments`),
  with NBT and network codecs (`StatCodecs`).
- 5 additional unit tests for `StatCalculator`.
- **Design document** (`docs/DESIGN.md`) distilling the reference anime-RPG mechanics into Kimon's
  original stat model, with the clean-room legal stance and the phased roadmap.
- **Six-attribute system + Training Points economy** (`stats` package):
  - `Attribute` enum: Strength, Agility, Vitality, Energy, Focus, Spirit.
  - `StatBlock`: immutable per-player attributes + TP, with a rising per-point cost curve,
    spend-to-raise, and clamping. Pure and fully unit-tested (11 tests).
- **Power now scales combat attributes.** Accumulated Power is converted into bonus max health,
  attack damage, and movement speed via vanilla `AttributeModifier`s, in discrete tiers
  (`PowerScaling`: one tier per 25 Power, capped at 50 tiers). Bonuses are server-authoritative and
  re-applied on login and respawn from the stored Power.
- HUD now also shows the current **Tier** below the Power value.
- Spanish/English translations for the Tier line.
- 7 additional unit tests covering the tier/bonus formulas.

## [0.1.0] - 2026-10-04

### Added
- Per-player **Power** stat stored via a NeoForge data attachment; persisted across relog and
  copied on death.
- Server-authoritative **train** action: a serverbound `TrainPowerPayload` raises Power by `+5`.
- Automatic server → client synchronization of the Power stat.
- **Train Power** key mapping (default <kbd>G</kbd>) in the Miscellaneous controls category.
- **HUD** overlay rendering the current Power in the top-left corner (hidden while F3 is held).
- English (`en_us`) and Spanish (`es_es`) localization.
- JUnit unit test suite covering all `PowerData` progression and clamping rules.
- Project scaffolding: MIT license, README, contributing guide, GitFlow branching model,
  and GitHub Actions CI (build + test).

[Unreleased]: https://github.com/fernandosilvot/kimon/compare/v0.3.0...HEAD
[0.3.0]: https://github.com/fernandosilvot/kimon/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/fernandosilvot/kimon/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/fernandosilvot/kimon/releases/tag/v0.1.0
