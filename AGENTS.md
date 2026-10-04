# AGENTS.md

Guidance for AI coding agents working in this repository (Kimon, a Minecraft 26.2 / NeoForge mod).

## Start here

1. Read **`docs/CONTINUE_HERE.md`** — the full handoff: state, architecture, conventions, next steps.
2. Then `README.md` (features, controls, commands), `docs/DESIGN.md` (design + clean-room legal
   stance), `CHANGELOG.md`.
3. Obey **`.kiro/steering/kimon-docs.md`** — the standing rules (docs sync, GitFlow, quality gates).

## Build / test / run

JDK **25** only:

```bash
export JAVA_HOME="$(brew --prefix openjdk@25)/libexec/openjdk.jdk/Contents/Home"  # macOS/Homebrew
./gradlew test        # unit tests (must pass)
./gradlew build       # full build (must pass)
./gradlew runClient   # dev client — verify it loads without crashing
```

## Golden rules (summary — details in docs/CONTINUE_HERE.md)

- **Clean-room.** No Dragon Block C / Dragon Ball code, textures, models, sounds or configs. *Names and terms may follow Dragon Ball's* (owner's decision, see docs/DESIGN.md).
- **Server-authoritative.** Client sends intents; server validates; synced attachments return state.
- **Unit-test pure logic.** Balance math goes in Minecraft-free classes with JUnit tests.
- **GitFlow.** Feature branch off `develop` → `--no-ff` merge → delete the branch (local + remote).
  Only `main` + `develop` persist. Conventional Commits.
- **Keep docs in sync** on every change: README, CHANGELOG, DESIGN, and the roadmap diagram
  (`docs/roadmap.svg`, embedded in the README).
- Target **Minecraft 26.2 / NeoForge 26.2.0.88 / Java 25**. See the 26.2 API gotchas list in
  `docs/CONTINUE_HERE.md` before touching GUI, networking, blocks/items, or datapacks.

## Project layout

Java under `src/main/java/net/kimon/kimon/`: `power/`, `stats/`, `network/`, `block/`, `wish/`,
`command/`, `client/`. Resources under `src/main/resources/{assets,data}/kimon/`. Tests under
`src/test/java/`.
