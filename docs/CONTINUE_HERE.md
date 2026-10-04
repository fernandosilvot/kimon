# CONTINUE HERE — agent handoff

This file is the single entry point for any AI agent (Claude, Kiro, Cursor, etc.) picking up Kimon.
Read it first, then `README.md`, `docs/DESIGN.md`, and `CHANGELOG.md`. Follow the rules in
`.kiro/steering/kimon-docs.md` on every change.

## What Kimon is

An original, clean-room anime-style **Power/energy progression** mod for **Minecraft 26.2** on
**NeoForge 26.2.0.88**, built with original names/assets only (no Dragon Ball / DBC IP). Mechanics
are reimplemented from the research in `docs/DESIGN.md`.

## Environment (verified working)

- **JDK 25** required. On macOS/Homebrew:
  `export JAVA_HOME="$(brew --prefix openjdk@25)/libexec/openjdk.jdk/Contents/Home"`
- Build: `./gradlew build` · Test: `./gradlew test` · Dev client: `./gradlew runClient`
- First build decompiles MC (slow, needs the `-Xmx3G` already set in `gradle.properties`).
- `org.gradle.configuration-cache=false` on purpose (NeoGradle 7.1.39 breakage).

## Current state (as of this handoff)

- Branch of record: **`develop`** (latest work). `main` holds tagged releases.
- Latest release: **v0.3.0**. ~81 passing unit tests. CI green (`.github/workflows/build.yml`).
- Implemented: Phases 0–6 + Form Mastery + Phase 7 (Training Altar block, Wish Orb item).
  See `README.md` feature table and the roadmap diagram (`docs/roadmap.svg`, embedded in README).

## Architecture (where things live)

Package root: `net.kimon.kimon`

- `power/` — Power stat, tiers, Release%/Energy/Stamina loop, forms + mastery, combat.
  - Pure/unit-tested cores: `PowerData`, `PowerScaling`, `PowerState`, `EnergyBlast`, `Form`, `MasteryData`.
  - Server glue: `CombatHandler` (tick loop, melee/blast scaling, transform, blast raycast),
    `PowerEffects`, `PowerEventHandler`, `ModAttachments` (POWER, STATE, MASTERY).
- `stats/` — six attributes + TP economy, races/classes, derived stats.
  - Pure/unit-tested: `StatBlock`, `StatCalculator`, `RaceDef`, `ClassDef`, `StatMods`, `CharacterCatalog`, `CharacterProfile`.
  Races/classes are JSON under `data/kimon/{races,classes}/`, loaded by `CharacterDataLoader` and synced by `CatalogPayload`.
  - Glue: `StatEffects`, `StatCodecs`, `ModStatAttachments` (STATS, PROFILE).
- `network/` — serverbound payloads (raise attribute, charge input, reset release, fire blast, transform) and clientbound
  `PowerSyncPayload` / `AuraPayload` sent by `power/SyncHandler` (STATE is NOT an auto-synced attachment) +
  `ModNetworking` handlers.
- `block/` — `TrainingAltarBlock`, `ModBlocks` (blocks, items incl. Wish Orb, creative tab).
- `wish/` — `Wish` (reward table, pure), `WishOrbItem`.
- `command/` — `KimonCommands` (`/kimon ...` debug/admin tree).
- `client/` — `KimonClient` (keybinds→payloads), `StatsScreen` (GUI), `PowerHudLayer` (HUD). All
  `@Dist.CLIENT`.

Resources: `src/main/resources/assets/kimon/` (lang en/es, blockstates, models, `items/` client
item defs), `data/kimon/` (`loot_table/`, `recipe/`), `META-INF/neoforge.mods.toml`.

## Non-negotiable conventions

1. **Clean-room / original IP only.** No DBC/Dragon Ball code, assets, or names. Invented terms only.
2. **Server-authoritative.** Client sends intents (payloads); server validates and applies; synced
   data attachments push state back. Never trust client values.
3. **Pure logic is unit-tested.** Put balance math in Minecraft-free classes and add JUnit tests.
4. **26.2 API gotchas** (already handled, keep consistent):
   - `Identifier` (not `ResourceLocation`).
   - GUI render = `extractRenderState(GuiGraphicsExtractor)`, text via `guiGraphics.text(...)`.
   - HUD layer implements `net.neoforged.neoforge.client.gui.GuiLayer`.
   - Open screens with `Minecraft.setScreenAndShow(...)`.
   - Attach `.serialize(MapCodec)` (not Codec); `.sync(predicate, StreamCodec)`.
   - Damage via `LivingEntity.hurtServer(ServerLevel, DamageSource, float)`.
   - Action-bar message = `ServerPlayer.sendSystemMessage(component, true)`.
   - Command perms via `Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)`.
   - Blocks: `registerBlock(name, factory, UnaryOperator<Properties>)`; interaction =
     `useWithoutItem(...)`. Items: `registerItem(...)`; `use(Level, Player, InteractionHand)`.
   - Datapacks use singular dirs: `data/<ns>/loot_table/`, `data/<ns>/recipe/`.
   - Items need a client item definition at `assets/<ns>/items/<name>.json` plus the model.

## Workflow (GitFlow) — do this every feature

1. `git switch develop && git switch -c feature/<name>` (branch from develop).
2. Implement; keep pure logic testable; add tests.
3. `./gradlew test` and `./gradlew build` (JDK 25) must pass; verify `runClient` loads w/o crash.
4. **Update docs** (steering rule): `README.md`, `CHANGELOG.md` (`[Unreleased]`), `docs/DESIGN.md`,
   and regenerate `docs/roadmap.svg` (mermaid) if a phase changed.
5. Conventional-commit; `git merge --no-ff` into `develop`; push; **delete the feature branch**
   (local and remote). Keep branches to just `main` + `develop`.
6. Release: move `[Unreleased]`→`[X.Y.Z]`, bump `mod_version`, merge `develop`→`main`, tag `vX.Y.Z`,
   `gh release create`.

## Suggested next slices (Phase 7 and refinements)

Pick one, keep it a complete vertical slice:

- **Master NPCs** — an entity the player interacts with to learn skills/forms. Larger: needs an
  `EntityType`, a renderer (`EntityRenderersEvent.RegisterRenderers`), and a dialog/teach flow.
  Consider starting with a villager-reskin or a simple `PathfinderMob` to avoid custom rigging.
- **Visual projectile for the Energy Blast** — a `Projectile` entity + renderer so the blast is seen,
  replacing the instant raycast (or complementing it). High visual payoff.
- **Data-driven sagas/quests** — a JSON mission format (see the schema sketch in `docs/DESIGN.md`).
- **Move forms/skills to JSON datapacks** — races and classes are done; the research targets datapack-defined
  content for server customization.
- **Skills** — e.g. Fly/Dash/Endurance as learnable, TP-costed abilities (pure cost logic + effects).

## Quick manual test recipe

```
/op <you>                 # or a creative world with cheats
/kimon tp 100000
/kimon race titan
/kimon class warrior
/kimon attr strength 1000
/kimon power 5000
# hold C to charge Release (Ctrl+C lowers it, H resets, hold R for turbo), press G to transform,
# left-click a mob (see "Hit for X (+N TP)" — TP only comes from hits with Release >= 5%),
# press B to fire an Energy Blast, press K for the character sheet.
/give @s kimon:wish_orb 4
/give @s kimon:training_altar
/give @s kimon:gravity_device      # stand within 8 blocks: 10G, heavier, STR/DEX down, TP likelier
/give @s kimon:weighted_vest        # carried weight: lowers melee damage, TP likelier
```
