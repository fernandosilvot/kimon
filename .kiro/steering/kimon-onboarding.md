---
inclusion: always
---

# Kimon — session onboarding (read first)

You are working on **Kimon**, a clean-room Minecraft 26.2 / NeoForge 26.2.0.88 / Java 25 mod that re-implements Dragon Block C's
mechanics (names and terms follow Dragon Ball's by the owner's decision; code and assets are original).

**Before doing anything, read in order:** `docs/CONTINUE_HERE.md` → `docs/STATUS.md` → `docs/SETUP.md` → `docs/ARCHITECTURE.md` →
`docs/DECISIONS.md` (and `docs/research/` for the spec).

- Everything is server-authoritative; pure balance logic is unit-tested; balance numbers are config or datapack JSON.
- Several agents may share this repo: check `git status`/`git worktree list`, work in your own worktree, don't switch someone else's branch.
- The owner (Fernando) speaks Spanish and play-tests; report honestly what you verified and what he must try in the game.
- Ask the owner before reading or reusing code from the original Dragon Block C JARs (see `docs/DECISIONS.md` §2).
