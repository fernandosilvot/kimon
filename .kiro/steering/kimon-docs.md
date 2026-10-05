---
inclusion: always
---

# Kimon — documentation & housekeeping rules

These rules apply to ALL work on the Kimon mod. Follow them automatically without being asked.

## Always keep docs in sync with code

After implementing or changing any feature, BEFORE committing:

1. **README.md** — update the feature table, controls, `/kimon` commands, walkthrough, architecture
   map, test count, and roadmap so they reflect the real current state. The README must never lag
   behind the code.
2. **CHANGELOG.md** — add an entry under `## [Unreleased]` describing the change (Keep a Changelog
   format, Conventional-Commit spirit).
3. **docs/DESIGN.md** — if a roadmap phase advances, mark it done and update the "next" pointer.
4. **docs/STATUS.md** — the volatile handoff file: branches, the 12-step table, open bugs, test counts, next steps, questions for the
   owner. Update it at the end of **every** session (checklist in the file).
5. **docs/ARCHITECTURE.md / DECISIONS.md** — update when you add a payload, attachment, config key, datapack field, formula or make a
   judgment call (record `[PROP]` numbers and research contradictions in DECISIONS).
6. **Roadmap diagram** — edit the status list in `docs/tools/make_roadmap.py` and run `python3 docs/tools/make_roadmap.py` to regenerate
   `docs/roadmap.svg` (the README embeds it).

## Branching & cleanliness (GitFlow)

- Work on `feature/*` branches cut from `develop`; never commit features directly to `main`/`develop`.
- Merge features into `develop` with `--no-ff`.
- **Delete the feature branch (local AND remote) immediately after merging.** Keep the branch list
  to just `main` + `develop` + any in-flight feature.
- Use Conventional Commits (`feat(scope): …`, `docs: …`, etc.).

## Quality gates (every feature)

- Put non-trivial logic in pure, unit-testable classes and add JUnit tests for it.
- Run `./gradlew test` and `./gradlew build` (JDK 25) and confirm they pass.
- Verify `runClient` loads without crashing before claiming a gameplay feature works.
- Keep everything server-authoritative; client sends intents only.
- Clean-room: never copy Dragon Block C / Dragon Ball code or assets. Names and terms follow Dragon Ball's (owner's decision; see docs/DESIGN.md).

## Releases

- A release merges `develop` → `main`, moves `[Unreleased]` CHANGELOG entries under the new version,
  bumps `mod_version`, and tags `vX.Y.Z` on `main`.
