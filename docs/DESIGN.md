# Kimon — Design notes

This document distills the **game-design mechanics** that inspire Kimon, and maps them to Kimon's
original, modern implementation on NeoForge 26.2.

## Legal stance (read first)

Kimon is a **clean-room reimplementation of mechanics**, not a port. We study how training-driven
anime-RPG progression *works* (from public wikis, guides and research) and write our own code, and
use our own assets.

- ✅ Allowed: reimplementing game mechanics and formulas. Game rules/ideas are not copyrightable.
- ❌ Never: decompiling or copying Dragon Block C's code, textures, models, sounds or configs, or
  shipping its assets.
- ⚠️ **Names (owner's decision, 2026-10-04):** the earlier "invented names only" rule was lifted. Races,
  classes, attributes, forms and terms now use Dragon Ball's (Saiyan, Namekian, Ki, Release, Super
  Saiyan…) so players recognise them. They are trademarks of their owners and may have to be changed if
  a rights holder asks; keep the project non-commercial. Keeping names in lang files and data JSON
  (not scattered through code) makes a future rename cheap.

## Core stat model (adapted from the research)

The reference design (DBC) uses six trainable attributes bought with Training Points (TP), a
"Release %" power multiplier, and resources (Ki / Body / Stamina). Kimon adopts an **original,
simplified version** of the same shape:

| Attribute | Governs | Doc abbreviation |
| --- | --- | --- |
| `STRENGTH` | Melee damage | STR |
| `DEXTERITY` | Movement speed, defense | DEX |
| `CONSTITUTION` | Max health (Body) | CON |
| `WILLPOWER` | Ki-attack power | WIL |
| `MIND` | TP gain rate, Release ceiling | MND |
| `SPIRIT` | Max Ki / regen | SPI |

- **Training Points (TP):** earned by training; spent to raise attributes. Cost grows per point.
- **Power:** the aggregate "level" shown on the HUD (already implemented in v0.1.0). Tiers derived
  from Power scale vanilla attributes (`PowerScaling`, implemented).
- **Release %**: a 0–max multiplier that scales combat stats and gates TP gain, with the
  classic "charge up vs. regenerate" tension.

Formulas are taken from the research as **starting points**, all made configurable:
- `meleeDamage = STR * 2.5 * (1 + classMod) * form * release/100`
- `maxBody     = VIT * 20`
- `maxEnergy   = SPIRIT * 40`
- `tpPerHit    = 2 + 2*floor(MIND/5) * release/100`  (requires release ≥ 5%)

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
6. ✅ **Forms** — Base→Super Saiyan→Super Saiyan 2→Super Saiyan 3 (race gating comes with step 5); multiply combat damage, drain Energy, gated by tier +
   Release; transform up/down keys; HUD indicator. **Form Mastery**: forms improve (+damage,
   −drain) with use. *(done)*
7. 🔜 **World / masters / sagas / wishes** — long-horizon, data-driven. *(in progress)*
   - ✅ **Training Altar** block: craftable, right-click to train for a bigger reward.
   - ✅ **Wish Orb** item: craftable consumable granting a random progression wish.
   - ⏳ Master NPCs that teach skills/forms, data-driven sagas.

   - ✅ **Release state machine + config** (Unreleased): explicit Release states, charge / discharge /
     reset / turbo controls and a server/client config, per `docs/02-release-ki-stats.md`.
   - ✅ **TP by fighting**: the Train key/button is gone; TP come from hits with Release ≥ 5%
     (`TpGain`), and spending TP on attributes raises Power.
   - ✅ **Combat costs, regen lock and sync**: hit costs (Energy + Stamina), 30 s Energy-regen lock
     after being hurt, throttled sync every 2 ticks, aura state for neighbours, dimension re-sync.
   - ✅ **Progression (doc 03 step 2)**: character level, configurable attribute cost curve with bulk
     buying, training weights and Gravity Device. Simple minigames are not done.
   - ✅ **Races and classes as datapack JSON (doc 03 step 3)**, with the research tables as test oracle.
   - ✅ **Skills as JSON (doc 03 step 4)**: Jump, Dash, Fly, Endurance, Potential Unlock, Ki Sense. Not
     done: Ki Sense lock-on (Z), Dash swoop, learning from master NPCs (step 8).
   - ⏳ Next (`docs/03`, order of 12): step 5 forms from JSON (race-specific), step 6 Form Mastery
     rework, step 7 Ki techniques.

Near-term refinements: a visual projectile entity, and moving races/forms to JSON datapacks.

Deferred (need libraries not yet on 26.2): player animation (forms visuals), in-game GameTests.
Also deferred: moving forms/skills to JSON datapacks (races and classes are done; the research's
JSON schema is the target once the feature set stabilizes).

## Server-authoritative principle

Every stat/resource computation happens on the logical server. The client only sends intents
(train, charge, fire slot N) and renders synced state. All balancing math lives in pure,
unit-tested classes (no world/entity refs) — e.g. `PowerScaling`, and the upcoming `StatCalculator`.

## Credits

Game-design research compiled by the project owner (Fernando) from public DBC documentation
(JinGames guides, Fandom wikis, forums) and the open-source DragonMine Z (GPL-3.0) design. Kimon's
code and assets are original and MIT-licensed.
