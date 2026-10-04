# Changelog

All notable changes to this project are documented here.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
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
