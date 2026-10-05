# AGENTS.md

Guidance for AI coding agents working in this repository (**Kimon**, a Minecraft 26.2 / NeoForge 26.2.0.88 / Java 25 mod). It is a
short summary; the full picture is in `docs/`.

## Start here (in this order)

1. **`docs/CONTINUE_HERE.md`** — the entry point: rules, GitFlow, working next to other agents, quality gates.
2. **`docs/STATUS.md`** — what is done, which branch holds what, the **open bug**, next steps, open questions for the owner.
3. **`docs/SETUP.md`** — set up Java 25 and run it on any computer.
4. **`docs/ARCHITECTURE.md`** — packages, attachments, payloads, datapack formats, formulas, config, how to extend, 26.2 gotchas.
5. **`docs/DECISIONS.md`** — owner decisions, provenance/legal, research contradictions, our `[PROP]` numbers.
6. **`docs/research/`** — the design specification (3 documents); `03` has the 12-step order of work.
7. Standing rules: `.kiro/steering/kimon-docs.md`.

## Build / test / run

JDK **25** is required (the machine default may be 26 — set `JAVA_HOME`):

```bash
export JAVA_HOME="$(brew --prefix openjdk@25)/libexec/openjdk.jdk/Contents/Home"  # macOS/Homebrew; see docs/SETUP.md for Linux/Windows
./gradlew test        # unit tests (must pass)
./gradlew build       # full build (must pass)
./gradlew runClient   # dev client — must boot without errors; the owner plays it
```

## Golden rules

- **Server-authoritative.** Clients send intents; the server validates; synced attachments / payloads return state.
- **Pure logic is unit-tested.** Balance math lives in Minecraft-free classes; every new one gets JUnit tests.
- **Balance is data.** `[COM]`/`[PROP]` numbers go to config (`KimonConfig`) or datapack JSON, never constants.
- **GitFlow.** Feature branch off `develop` → verify → `--no-ff` merge → push → delete the branch. Conventional Commits. Push your
  branch so another computer can continue it. Never force-push `main`/`develop`.
- **Several agents may work at once.** Check `git status` / `git worktree list` / `git stash list` first, work in **your own worktree**,
  never switch branches in a checkout you did not create. See `docs/CONTINUE_HERE.md` §5.
- **Keep docs in sync** on every change: `README.md`, `CHANGELOG.md`, `docs/DESIGN.md`, `docs/STATUS.md`, `docs/roadmap.svg`
  (`python3 docs/tools/make_roadmap.py`).
- **Provenance:** names and terms follow Dragon Ball's (owner's decision). Read `docs/DECISIONS.md` §2 before copying anything from
  other mods, and **ask the owner before touching the decompiled copies** that may exist on his machine (`MC/test/`, outside this repo).
- **Be honest in reports:** say what you verified (tests, boot) and what only the owner can verify by playing. Respond to the owner in
  **Spanish**; code, commits and docs are English.

## Project layout

Java under `src/main/java/net/kimon/kimon/`: `power/`, `stats/`, `skill/`, `training/`, `network/`, `config/`, `block/`, `wish/`,
`command/`, `client/`. Resources under `src/main/resources/{assets,data}/kimon/` (datapack JSON: `races`, `classes`, `skills`, `forms`).
Tests under `src/test/java/`. Details: `docs/ARCHITECTURE.md`.
