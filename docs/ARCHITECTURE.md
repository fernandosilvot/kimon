# ARCHITECTURE — how Kimon is built

Package root: `net.kimon.kimon` (91 source files). Items marked **(feature/kaioken)** exist only on that branch until it is
merged (see `STATUS.md`). Everything else is on `develop`.

## 1. Principles (do not break these)

1. **Server-authoritative.** The client sends *intents* (payloads with a key state or an id); the server validates and
   applies; synced attachments or payloads push the result back. Never trust a client number.
2. **Pure logic is Minecraft-free and unit-tested.** Balance math lives in plain Java classes (no entity/world references) so it
   runs on a bare JVM. The glue (events, attachments, payload handlers, screens) stays thin.
3. **Balance is data.** Anything tagged `[COM]`/`[PROP]` in the research is a config key or a datapack JSON field, never a
   constant in code. Defaults live in `*Params.DEFAULTS` records and JSON files that are also readable from the classpath.
4. **Immutable values.** `PowerState`, `StatBlock`, `SkillData`, `MasteryData`, `FormDef`… are immutable; every change returns a
   new value which is then `setData`-ed on the attachment.
5. **Catalogs reload and sync.** Races, classes, skills and forms are loaded from datapacks by a reload listener into an
   immutable `*Catalog`, swapped whole (`volatile` reference), then sent to clients (`OnDatapackSyncEvent`). Each catalog also
   falls back to the JSON shipped in the jar so it works before the first load and in tests.

## 2. Package map

| Package | Pure / tested | Glue (needs the game) |
| --- | --- | --- |
| `power/` | `PowerState` (Release state machine + Ki/Stamina loop), `PowerParams`, `ReleaseState`, `KiRegenRate`, `CombatCosts`, `SyncPolicy`, `AuraState/AuraCache`, `EnergyBlast`, `PowerData`, `PowerScaling`, `MasteryData`, `FormDef`, `FormCatalog`, `FormRules`, `FormEffect`, `Form`, **`KaiokenRules`** | `CombatHandler` (tick loop, melee bonus, TP, transform, blast), `SyncHandler`, `KiSpending`, `ReleaseCeiling`, `PowerEffects`, `PowerEventHandler`, `ModAttachments`, `MasteryCodecs`, `FormDataLoader/Handler`, **`KaiokenHandler`**, **`PlayerForm`** |
| `stats/` | `StatBlock` (6 attributes + TP), `StatCalculator`, `CostParams`, `LevelCalculator`, `TpGain/TpParams`, `StatMods`, `RaceDef`, `ClassDef`, `CharacterCatalog`, `CharacterProfile`, `Attribute` | `StatEffects`, `StatCodecs`, `ModStatAttachments`, `CharacterDataLoader/Handler` |
| `skill/` | `SkillDef`, `SkillEffect`, `SkillCatalog`, `SkillData`, `SkillRules`, `SkillEffects`, `SkillParams` | `SkillHandler` (learn, Jump, Endurance), `FlightHandler`, `DashHandler`, `ModSkillAttachments`, `SkillDataLoader/Handler` |
| `training/` | `TrainingLoad`, `TrainingEffects`, `TrainingParams` | `TrainingHandler` (every second: weight + gravity), `WeightItem` |
| `network/` | the payload records' codecs | `ModNetworking` (registration + handlers) |
| `config/` | — | `KimonConfig` (server + client `ModConfigSpec`, snapshots into the `*Params` records) |
| `block/`, `wish/` | `Wish` (reward table) | `ModBlocks`, `TrainingAltarBlock`, `WishOrbItem` |
| `command/` | — | `KimonCommands` (`/kimon …`) |
| `client/` | — (all `@Dist.CLIENT`) | `KimonClient` (keys → payloads), `PowerHudLayer`, `StatsScreen`, `SkillsScreen` |

Resources: `src/main/resources/assets/kimon/{lang/en_us.json,lang/es_es.json,items,models,blockstates}`,
`data/kimon/{races,classes,skills,forms,recipe,loot_table}`, `META-INF/neoforge.mods.toml`.

## 3. Data attachments (per player)

| Name | Type | Saved | Synced to owner | Kept on death | Purpose |
| --- | --- | --- | --- | --- | --- |
| `power` | `PowerData` | ✅ | auto | ✅ | aggregate "Power" → tier bonuses (health/attack/speed) |
| `state` | `PowerState` | ❌ | **manual**, every 2 ticks if changed (`SyncHandler` → `power_sync`) | ❌ | Release, Ki, Stamina, Release state, active form, inputs, regen lock |
| `stats` | `StatBlock` | ✅ | auto | ✅ | the 6 attributes + unspent TP |
| `profile` | `CharacterProfile` | ✅ | auto | ✅ | race id + class id (old bare names are migrated to `kimon:<name>`) |
| `mastery` | `MasteryData` | ✅ | auto | ✅ | Form Mastery per form id (old `surge/ascent/zenith` fields still load) |
| `skills` | `SkillData` | ✅ | auto | ✅ | skill id → level |
| `load` | `TrainingLoad` | ❌ | auto | ❌ | carried weight + gravity (recomputed every second) |
| `kaioken` **(feature/kaioken)** | `Form` | ❌ | auto | ❌ | active Kaioken tier (Base = off) |

Why `state` is manual: it changes almost every tick, so auto-sync would send ~20 packets/s per player. Other players only get
a coarse `AuraState` (charging, turbo, 10%-Release bucket), never real numbers.

## 4. Network (protocol version `"7"` on develop, `"8"` on feature/kaioken)

Bump `PROTOCOL_VERSION` in `ModNetworking` whenever a payload changes shape.

| Payload (`kimon:<id>`) | Dir | Fields | Server/Client behaviour |
| --- | --- | --- | --- |
| `charge_input` | C→S | charge, discharge, turbo (booleans) | stored on `PowerState`; the tick loop acts on them (discharge wins over charge) |
| `reset_release` | C→S | — | `PowerState.reset()` (Release 0 + Base form) (+ clears Kaioken on the branch) |
| `transform` | C→S | up (bool) | `CombatHandler.transform` climbs/descends the race ladder |
| `fire_blast` | C→S | — | raycast Ki Blast |
| `raise_attribute` | C→S | attribute ordinal, count (1..1000) | `StatBlock.raiseMany`, +Power per point |
| `learn_skill` | C→S | skill id string | `SkillHandler.learn` (TP + Mind + race checked) |
| `toggle_flight` | C→S | — | `FlightHandler.toggle` |
| `dash` | C→S | direction 0 back / 1 left / 2 right | `DashHandler.dash` |
| `kaioken` **(branch)** | C→S | — | `KaiokenHandler.cycle` |
| `power_sync` | S→C | `PowerState` | stored on the local player; the HUD reads it |
| `aura` | S→C | entity id + `AuraState` | `AuraCache` (no renderer yet) |
| `catalog` | S→C | races + classes | `CharacterCatalog.set` |
| `skill_catalog` | S→C | skills | `SkillCatalog.set` |
| `form_catalog` | S→C | forms | `FormCatalog.set` |

Server-side `SyncHandler.fullSync` runs on login, respawn and dimension change (NeoForge issue #2510: synced attachments are not
always re-sent on dimension change), re-sending resources, aura and the auto-synced attachments.

## 5. Datapack formats (all optional fields have defaults)

Ids are `namespace:name` from the file path. A broken file is skipped with a log message; if nothing valid loads, the shipped
set stays. `/reload` re-reads them in a running world.

**Race** — `data/<ns>/races/<name>.json`: `attributes` (**all six required**, 0..10000: `strength dexterity constitution willpower
mind spirit`), `modifiers` (optional `StatMods`).
**Class** — `data/<ns>/classes/<name>.json`: `modifiers` only. Race and class modifiers **add**.
`StatMods` (percent; `30` = +30%): `melee defense body stamina ki_power max_ki run fly`. `defense` and `fly` are stored but unused
until those stats exist.

**Skill** — `data/<ns>/skills/<name>.json`:
`max_level` (1..1000, default 10) · `cost.tp_base` (required) · `cost.tp_per_level` (default = `tp_base`, so level *n* costs
`tp_base·n`; in general `tp_base + tp_per_level·(n-1)`) · `cost.mind` (Mind used by **each** level, default 0) · `races` (list; empty
= every race, used by racial skills) · `effects` (list of `{type, base, per_level}`; value = `base + per_level·level`).
Effect types: `release_cap` (+% Release ceiling), `damage_reduction` (%), `jump_boost` (% jump), `safe_fall` (blocks), `flight`
(% flying speed; enables flight), `dash` (horizontal speed), `ki_sense` (range in blocks), `kaioken_cost_reduction` (% less health
cost, **branch**). Any skill can add to any type.

**Form** — `data/<ns>/forms/<name>.json`: `races` (list; empty = every race) · `order` (position in the race ladder, ≥1) ·
`requires: {skill, level}` · `multipliers: {str, dex, wil}` (default 1) · `flat_bonus` (attribute points) ·
`damage_taken_divisor` (>0, default 1) · `ki_per_second` · `min_release` (%) · **`stacking`** (overlay such as Kaioken; excluded from
ladders) · **`health_per_second`** (**branch**: both stacking/health fields).

## 6. Controls (`KimonClient`; all rebindable in Options → Controls)

| Key | Action | | Key | Action |
| --- | --- | --- | --- | --- |
| <kbd>K</kbd> | Character sheet | | <kbd>H</kbd> | Reset Release + revert form (+ Kaioken off) |
| <kbd>J</kbd> | Skills screen | | <kbd>G</kbd> | Transform up one rung (`key.kimon.transform`) |
| <kbd>C</kbd> (hold) | Charge Release | | <kbd>B</kbd> | Ki Blast |
| <kbd>Ctrl</kbd>+<kbd>C</kbd> (hold) | Lower Release | | <kbd>Y</kbd> | Toggle flight |
| <kbd>R</kbd> (hold) | Turbo | | <kbd>V</kbd> | Dash (A/D = side, else back) |
| <kbd>X</kbd> **(branch)** | Cycle Kaioken tier | | <kbd>F3</kbd> | hides the Kimon HUD |

## 7. Commands (`/kimon …`, all except `info` need permission level 2)

`info` · `tp <amount>` · `power <value>` · `attr <name> <value>` (names `strength dexterity constitution willpower mind spirit`, also
`str dex con wil mnd spi` and the old names) · `race <id>` (**re-seeds attributes, keeping TP**) · `class <id>` · `form <id>` (only your
race's forms) · `mastery <form> <level>` · `skill <id> <level>` · `reset`. Old names still resolve (`titan`, `brawler`, `surge`…).
`/give @s kimon:<item>`: `training_altar`, `gravity_device`, `weighted_wraps`, `weighted_vest`, `heavy_plates`, `wish_orb`.

## 8. The formulas in code (defaults; every number is config or data)

- **Max Ki** = `SPIRIT × ki.perSPI(40) × (1 + max_ki%)`; **max Stamina** = `CONSTITUTION × 3.5 × (1 + stamina%)`; Stamina regen 5%/s.
- **Release ceiling** = `release.baseMax(50) + Potential Unlock bonus (5%/level)`, capped at 100 (200 with `allowOvercharge`).
- **Release dynamics** (per second): charge `+10` (×0.5 from 50% up, ×1.5 with Turbo), discharge `−25`; with no input it **holds**.
  Upkeep `maxKi × 0.002 × (release/100)²` Ki/s. Ki regen `maxKi × 0.02 × max(0, 1 − release/50)` Ki/s, **blocked 600 ticks after being hurt
  by a living entity**. Ki reaching 0 → `EXHAUSTED` (Release 0, form dropped) until Ki ≥ 5% of max.
- **Empowered hit** (needs Release ≥ 5%): costs `1 + STR/200` Ki and 2% of max Stamina; if it cannot be paid the hit is vanilla damage and earns
  no TP. Bonus damage = `min(200, max(0, effSTR − 5) × 0.1) × (1 + melee%) × release/100 × weightFactor × gravityFactor`, added on top.
- **TP per hit** = `floor(2 + 2·⌊MIND/5⌋ · release/100)` rolled against `tp.hitChance` (0.2), multiplied by the weight/gravity chance bonus
  (capped at 1). TP only comes from empowered hits and Ki Blasts (+ the Training Altar, which counts as a hit on a dummy).
- **Attribute cost** at level *L*: `max(costMin, round(costBase + costRate·x + x²/costStartMinus))`, `x = L·costMultiplier(0.75)`.
  Each point bought also gives `progression.powerPerPoint` (5) Power. **Level** = `max(1, (Σattributes − 55) / 5)`.
- **Forms**: `effective = max(attr × mult, attr + flat)` for STR (melee), DEX (speed), WIL (Ki Blast); damage taken ÷ `damage_taken_divisor`;
  Ki/s drain; mastery adds `+0.25·(level/50)` to each multiplier and cuts the drain up to 40%. Kaioken multiplies on top (`stackedWith`).
- **Training load**: effective weight = `weight × gravity`; melee damage × `1 − min(0.6, 0.002·w)`; STR/DEX ÷ `(1 + (G−1)·0.1)`; TP chance × `1 +
  0.01·w + 0.05·(G−1)` (cap 3).
- **Skills**: Mind budget = `MIND × skills.mindPerPoint`; every learned level uses the skill's `mind`. Learn = all-or-nothing.
- Vanilla attribute bonuses (applied as transient modifiers on login/respawn/changes by `StatEffects`/`PowerEffects`/`SkillHandler`):
  health `min(400, (CON−5)·0.4) × body%`, speed `min(0.1, (DEX−5)·0.0008) × run%`, plus Power tiers (every 25 Power: +2 health, +0.5 attack, +0.002 speed, max 50).

## 9. Config reference (`kimon-server.toml` synced to clients; `kimon-client.toml`)

| Key | Default | Meaning |
| --- | --- | --- |
| `release.baseMax` / `allowOvercharge` | 50 / false | base Release ceiling %; allow up to 200% |
| `release.chargeRate` / `dischargeRate` | 10 / 25 | %/s |
| `release.slowdownAbove50` / `turboMult` / `turboKiDrain` | 0.5 / 1.5 / 0.01 | charge slowdown above 50%; Turbo speed-up; Ki fraction/s drained while charging with Turbo |
| `release.upkeepFactor` | 0.002 | Ki upkeep for holding Release |
| `ki.perSPI` / `regenPct` / `regenRate` / `regenCutoffRelease` / `exhaustRecoverPct` | 40 / 0.02 / NORMAL / 50 / 0.05 | Ki pool and regeneration (`SLOW NORMAL FAST FASTER`) |
| `combat.regenLockTicks` / `staminaRegenLocked` / `hitStaminaCost` | 600 / false / 0.02 | post-damage lock; hit Stamina cost |
| `tp.baseAmount` / `perFocusStep` / `focusDivisor` / `hitChance` / `altarStaminaCost` | 2 / 2 / 5 / 0.2 / 0.25 | TP formula |
| `progression.costBase` / `costRate` / `costMultiplier` / `costStartMinus` / `costMin` / `powerPerPoint` | 1 / 0.5 / 0.75 / 140 / 1 / 5 | attribute cost curve; Power per point |
| `training.weightPenaltyPerPoint` / `maxWeightPenalty` / `weightTpBonusPerPoint` / `maxWeight` | 0.002 / 0.6 / 0.01 / 200 | weights |
| `training.gravityStatDrop` / `gravityTpBonusPerG` / `deviceGravity` / `scanRadius` / `gravityAttributeFactor` / `maxTpMultiplier` | 0.1 / 0.05 / 10 / 8 / 0.1 / 3 | gravity |
| `skills.mindPerPoint` / `flightKiPerSecond` / `dashKiFraction` / `dashCooldownTicks` | 1 / 2 / 0.02 / 20 | skills |
| `skills.kaiokenStopHealth` **(branch)** | 0.2 | Kaioken switches off at this health fraction |
| `hud.displayStep` (client) | 5 | HUD Release step in % |

## 10. Key flows

```
Charge:  [C held] → ChargeInputPayload → PowerState.inputs → PowerState.tick(20 Hz) → SyncHandler → power_sync → HUD
Hit:     vanilla attack → CombatHandler.onIncomingDamage (empowered? pay Ki+Stamina, add bonus)
                        → onDamagePost (TpGain.forHit → StatBlock.addTrainingPoints; hurt victim → regen lock)
Spend:   sheet "+N" → RaiseAttributePayload → StatBlock.raiseMany → StatEffects.apply + Power += points×5
Skill:   Skills screen → LearnSkillPayload → SkillRules.learn (TP, Mind, race) → SkillData + effects applied
Form:    [G] → TransformPayload → CombatHandler.transform: FormRules.ladder(race) → canEnter(skill level, Release)
                                → PowerState.withForm → StatEffects.apply ; tick drains Ki, drops the form on low Release/Ki
Reload:  /reload or login → *DataLoader builds a Catalog → set() → OnDatapackSyncEvent → payload to clients → StatEffects.apply
```

## 11. Tests (`src/test/java`, plain JUnit 5, no game)

| Test class | Covers |
| --- | --- |
| `power/ReleaseStateMachineTest`, `PowerStateTest`, `RegenLockTest`, `CombatCostsTest`, `SyncPolicyTest` | the Release/Ki loop, states, regen lock, hit costs, sync throttling, aura |
| `power/FormTest`, `MasteryDataTest`, `KaiokenTest` (branch) | forms as data (ladders, unlocks, pipeline, JSON validation, network round trip), mastery incl. legacy saves, Kaioken rules |
| `power/EnergyBlastTest`, `PowerDataTest`, `PowerScalingTest` | Ki Blast, Power stat/tiers |
| `stats/CharacterDataTest` | races/classes **against the research tables** (every race+class row), JSON validation, legacy names |
| `stats/ProgressionTest`, `StatBlockTest`, `StatCalculatorTest`, `TpGainTest` | level, cost curve, bulk buying, derived stats, TP rules |
| `skill/SkillTest` | skills data, costs vs the research table, learning rules, effects, dash geometry, payload round trip |
| `training/TrainingEffectsTest`, `wish/WishTest`, `network/CatalogPayloadTest` | weights/gravity, wish table, catalog wire format |

Rule of thumb: every new pure class gets a test class in the matching package; tests that compare to the research tables should
name the table in their `@DisplayName`.

## 12. How to extend

- **New skill / form / race / class (data only):** add the JSON under `data/kimon/…`, add the display name in **both** lang files
  (`skill.kimon.<name>`, `form.kimon.<name>`, `race.kimon.<name>`, `class.kimon.<name>`), and — if it ships with the mod — add its name to the
  `BUILTIN` list in the matching `*Catalog` (that list is only an index; the JSON is the data). Add/adjust a test that checks the values.
- **New effect type:** add the `TYPE_*` constant in `SkillEffect`, read it with `SkillEffects.total(...)` where it applies, test it.
- **New payload:** record + `TYPE` + `STREAM_CODEC` in `network/`, register in `ModNetworking`, handler (`enqueueWork`, check `ServerPlayer`,
  validate on the server), **bump `PROTOCOL_VERSION`**, add a round-trip test if it carries data.
- **New config key:** `KimonConfig` (define + snapshot) and the matching `*Params` record with its default; document it in §9.
- **New attribute:** `Attribute` enum (key = datapack/lang key), `StatCodecs` (keep old stored field names for saves!), race JSONs, lang,
  `StatCalculator`, the sheet.

## 13. Minecraft 26.2 / NeoForge gotchas (learned the hard way)

- `Identifier` (not `ResourceLocation`); `Identifier.CODEC`; `FriendlyByteBuf.writeIdentifier/readIdentifier`; `Component.translatableWithFallback`.
- GUI: `extractRenderState(GuiGraphicsExtractor, …)`, text via `g.text(font, component, x, y, color)` / `g.centeredText(...)`; HUD is a
  `net.neoforged.neoforge.client.gui.GuiLayer`; open screens with `Minecraft.setScreenAndShow`.
- Attachments: `.serialize(MapCodec)` (not `Codec`), `.sync(predicate, StreamCodec)`, `.copyOnDeath()`; a *transient* synced attachment is just
  `.sync(...)`. `player.syncData(type)` re-sends one. Values are immutable records → always `setData`.
- Networking: `RegisterPayloadHandlersEvent` + `PayloadRegistrar.playToServer/playToClient`; send with `ClientPacketDistributor.sendToServer` and
  `PacketDistributor.sendToPlayer / sendToPlayersTrackingEntityAndSelf`; handlers must `context.enqueueWork`.
- Reload listeners: extend `SimplePreparableReloadListener<T>` (`prepare`/`apply`), scan with
  `SimpleJsonResourceReloadListener.scanDirectory(rm, FileToIdConverter.json("dir"), JsonOps.INSTANCE, codec, map)`; register in
  `AddServerReloadListenersEvent`; sync in `OnDatapackSyncEvent` (`getRelevantPlayers()`). Datapack folders use **singular** names only for
  vanilla types (`loot_table`, `recipe`); ours are plural (`races`, `skills`, `forms`).
- Config: `net.neoforged.neoforge.common.ModConfigSpec`; events in `net.neoforged.fml.event.config.ModConfigEvent` (`Loading`/`Reloading`);
  register with `modContainer.registerConfig(ModConfig.Type.SERVER|CLIENT, spec)`. SERVER config is synced to clients.
- Damage: `LivingEntity.hurtServer(ServerLevel, DamageSource, float)`; events `LivingIncomingDamageEvent` (modify `setAmount`) and `LivingDamageEvent.Post`.
  `player.setHealth` does not fire damage events. `player.hurtMarked = true` makes the server resend a velocity you set.
- Keys: `new KeyMapping(name, KeyConflictContext.IN_GAME, KeyModifier.CONTROL, type, key, KeyMapping.Category.MISC)`. Saved bindings in
  `options.txt` override defaults — **rename a mapping** if you change its default.
- Items need `assets/<ns>/items/<name>.json` (client item definition) + `models/item/<name>.json`; blocks `blockstates/`, `models/block/`,
  `loot_table/blocks/`, `recipe/`.
- **Pitfalls in our own code:** `Form` is a `record` → compare with `.equals`/`isBase()`, never `==`. In the `stats` package the name `Attribute`
  is Kimon's enum but `net.minecraft.world.entity.ai.attributes.Attribute` is also imported in some files — qualify one of them.
