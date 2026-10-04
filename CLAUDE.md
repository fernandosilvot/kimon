# CLAUDE.md

Instructions for Claude (and Claude Code) working on **Kimon**, an original Minecraft 26.2 /
NeoForge 26.2.0.88 progression mod.

> This file intentionally defers to the shared agent docs so there is a single source of truth.
> Read them in this order and follow them:
>
> 1. **`docs/CONTINUE_HERE.md`** — complete handoff (state, architecture, 26.2 API gotchas, next steps).
> 2. **`AGENTS.md`** — condensed agent guidance.
> 3. **`.kiro/steering/kimon-docs.md`** — standing rules (docs sync, GitFlow, quality gates).
> 4. `README.md`, `docs/DESIGN.md`, `CHANGELOG.md`.

## TL;DR for Claude

- **Toolchain:** JDK 25. `./gradlew test`, `./gradlew build`, `./gradlew runClient`.
- **Clean-room:** never copy Dragon Block C / Dragon Ball code or assets (textures, models, sounds, configs). Names and terms DO follow Dragon Ball's (owner's decision).
- **Server-authoritative:** client sends intent payloads; the server validates and applies; synced
  data attachments push state back to the client.
- **Test pure logic:** balance math lives in Minecraft-free classes with JUnit tests.
- **GitFlow:** branch from `develop`, merge `--no-ff`, delete the feature branch (local + remote);
  keep only `main` + `develop`. Conventional Commits.
- **Always update docs** (README, CHANGELOG `[Unreleased]`, DESIGN, `docs/roadmap.svg`) with each
  change — this is enforced by the steering rule.
- **Verify before claiming done:** tests pass, build passes, and `runClient` loads without crashing.

## Common pitfalls on MC 26.2 (full list in docs/CONTINUE_HERE.md)

`Identifier` not `ResourceLocation`; GUI uses `extractRenderState(GuiGraphicsExtractor)`; HUD uses
`GuiLayer`; open screens with `setScreenAndShow`; attachments `.serialize(MapCodec)` + `.sync(...)`;
damage via `hurtServer`; action bar via `sendSystemMessage(c, true)`; command perms via
`Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)`; datapack dirs are singular (`loot_table/`,
`recipe/`); items need `assets/kimon/items/<name>.json`.
