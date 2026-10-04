# Kimon — Design notes

This document distills the **game-design mechanics** that inspire Kimon, and maps them to Kimon's
original, modern implementation on NeoForge 26.2.

## Legal stance (read first)

Kimon is a **clean-room reimplementation of mechanics**, not a port. We study how training-driven
anime-RPG progression *works* (from public wikis, guides and research) and build original code,
names and assets.

- ✅ Allowed: reimplementing game mechanics and formulas. Game rules/ideas are not copyrightable.
- ❌ Never: decompiling or copying Dragon Block C's code, textures, models, sounds or configs;
  using Dragon Ball trademarks (Saiyan, Kamehameha, etc.) as identifiers; shipping its assets.

All Kimon terms are original: **Power**, **Tier**, generic attribute names, invented form names.

## Core stat model (adapted from the research)

The reference design (DBC) uses six trainable attributes bought with Training Points (TP), a
"Release %" power multiplier, and resources (Ki / Body / Stamina). Kimon adopts an **original,
simplified version** of the same shape:

| Kimon attribute | Governs | Reference analogue |
| --- | --- | --- |
| `STRENGTH` | Melee damage | STR |
| `AGILITY`  | Movement speed, defense | DEX |
| `VITALITY` | Max health (Body) | CON |
| `ENERGY`   | Energy-attack power | WIL |
| `FOCUS`    | TP gain rate, resource cap | MND |
| `SPIRIT`   | Max energy / regen | SPI |

- **Training Points (TP):** earned by training; spent to raise attributes. Cost grows per point.
- **Power:** the aggregate "level" shown on the HUD (already implemented in v0.1.0). Tiers derived
  from Power scale vanilla attributes (`PowerScaling`, implemented).
- **Release %** (future): a 0–max multiplier that scales combat stats and gates TP gain, with the
  classic "charge up vs. regenerate" tension.

Formulas are taken from the research as **starting points**, all made configurable:
- `meleeDamage = STR * 2.5 * (1 + classMod) * form * release/100`
- `maxBody     = VIT * 20`
- `maxEnergy   = SPIRIT * 40`
- `tpPerHit    = 2 + 2*floor(FOCUS/5) * release/100`  (requires release ≥ 5%)

## Implementation phases (Kimon roadmap)

Mirrors the research's phased plan, scoped to one vertical slice at a time:

0. ✅ **Infra & Power stat** — attachment, sync, keybind, HUD, tests, CI. *(v0.1.0)*
1. ✅ **Power → attributes** — tiered scaling of health/damage/speed. *(done)*
2. ✅ **Six attributes + TP economy** — per-player attribute block, TP, spend-to-raise with growing
   cost, character sheet GUI, attributes driving vanilla stats. *(done)*
3. ✅ **Resources** — Energy / Stamina with regen; Release % charge mechanic; melee scales with
   Strength × Release; action-bar damage feedback. *(done)*
4. ✅ **Races & classes** — six races + three classes with starting spreads and derived-stat
   modifiers; `/kimon` debug commands. *(done)*
5. ✅ **Energy attacks** — Energy Blast: raycast attack consuming Energy, scaling with Energy ×
   Release. *(done)*
6. ✅ **Forms** — Base→Surge→Ascent→Zenith; multiply combat damage, drain Energy, gated by tier +
   Release; transform up/down keys; HUD indicator. **Form Mastery**: forms improve (+damage,
   −drain) with use. *(done)*
7. 💤 **World / masters / sagas / wishes** — long-horizon, data-driven. *(next big arc)*

Near-term refinements: a visual projectile entity, and moving races/forms to JSON datapacks.

Deferred (need libraries not yet on 26.2): player animation (forms visuals), in-game GameTests.
Also deferred: moving races/forms/skills to JSON datapacks (currently enum-based; the research's
JSON schema is the target once the feature set stabilizes).

## Server-authoritative principle

Every stat/resource computation happens on the logical server. The client only sends intents
(train, charge, fire slot N) and renders synced state. All balancing math lives in pure,
unit-tested classes (no world/entity refs) — e.g. `PowerScaling`, and the upcoming `StatCalculator`.

## Credits

Game-design research compiled by the project owner (Fernando) from public DBC documentation
(JinGames guides, Fandom wikis, forums) and the open-source DragonMine Z (GPL-3.0) design. Kimon's
code and assets are original and MIT-licensed.
