---
inclusion: always
---

# Kimon — session onboarding

You are working on **Kimon**, a clean-room anime-style progression mod (names follow Dragon Ball's) for
**Minecraft 26.2 / NeoForge 26.2.0.88 / Java 25**.

**Before doing anything, read `docs/CONTINUE_HERE.md`** — it is the authoritative handoff: current
state, package architecture, 26.2 API gotchas, the GitFlow workflow, and the suggested next slices.
Also read `AGENTS.md` and the companion rules in `kimon-docs.md`.

Key facts to anchor on:

- Branch of record is `develop`; `main` holds tagged releases (latest: v0.3.0).
- Build/test/run with JDK 25: `./gradlew test`, `./gradlew build`, `./gradlew runClient`
  (`JAVA_HOME="$(brew --prefix openjdk@25)/libexec/openjdk.jdk/Contents/Home"` on macOS).
- Everything is server-authoritative; pure balance logic is unit-tested; code and assets are original; only names/terms follow Dragon Ball's.
- Keep README, CHANGELOG, DESIGN, and `docs/roadmap.svg` in sync with every change (see kimon-docs).

When unsure where a system lives, consult the architecture map in `docs/CONTINUE_HERE.md` rather
than guessing.
