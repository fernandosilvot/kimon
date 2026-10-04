# Changelog

All notable changes to this project are documented here.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
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

[Unreleased]: https://github.com/fernandosilvot/kimon/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/fernandosilvot/kimon/releases/tag/v0.1.0
