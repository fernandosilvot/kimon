# CLAUDE.md

Instructions for Claude (and Claude Code) working on **Kimon**, a Minecraft 26.2 / NeoForge 26.2.0.88 / Java 25 mod that
re-implements the mechanics of Dragon Block C.

> This file defers to the shared docs so there is one source of truth. Read them in this order:
>
> 1. **`docs/CONTINUE_HERE.md`** — rules, workflow, working next to other agents, quality gates.
> 2. **`docs/STATUS.md`** — current state, branches, the open bug, next steps, owner questions.
> 3. **`docs/SETUP.md`** · **`docs/ARCHITECTURE.md`** · **`docs/DECISIONS.md`** · **`docs/research/`**.
> 4. `AGENTS.md`, `.kiro/steering/kimon-docs.md`, `README.md`, `CHANGELOG.md`, `docs/DESIGN.md`.

## TL;DR for Claude

- **Toolchain:** JDK 25 (`export JAVA_HOME=...`, see `docs/SETUP.md`); `./gradlew test build`, `./gradlew runClient`.
- **The owner is Fernando** — talk to him in **Spanish**, informal. He play-tests every slice; ask what he saw (feedback appears in the
  action bar, not in chat). He has authorized the assistant to run GitFlow (branch, commit, merge into `develop`, push) and to open the
  game for him; keep unconfirmed work on its branch and push the branch.
- **Server-authoritative**, **pure logic unit-tested**, **balance in config/datapack JSON** (never hard-coded `[PROP]` numbers).
- **Concurrency:** other sessions may use this checkout. Check `git status`, `git worktree list`, `git stash list`; use your own worktree.
- **Provenance:** names/terms follow Dragon Ball's (owner's decision). Do not copy Dragon Block C code or assets; read
  `docs/DECISIONS.md` §2 and **ask the owner before reading the decompiled copies** on his machine (`MC/test/`, outside the repo).
- **Always update docs** (README, CHANGELOG `[Unreleased]`, DESIGN, STATUS, roadmap) and **verify before claiming done** (tests, build,
  `runClient` boot, and tell him what to try in the game).
- **End commits with** `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`.

## Common pitfalls (full list in `docs/ARCHITECTURE.md` §13 and `docs/SETUP.md` §6)

`Identifier` not `ResourceLocation`; GUI `extractRenderState(GuiGraphicsExtractor)`; HUD = `GuiLayer`; `setScreenAndShow`; attachments
`.serialize(MapCodec)` + `.sync(...)`; `hurtServer` for damage; action bar via `sendSystemMessage(c, true)`; command permissions via
`Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)`; `Form` is a record (never `==`); rename a key mapping when its default changes
(saved `options.txt`); zsh needs quoted globs; the default `java` on the owner's Mac is 26 — set `JAVA_HOME` to 25.
