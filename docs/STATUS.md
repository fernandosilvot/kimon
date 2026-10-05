# STATUS — where the project is right now

> This is the **volatile** file: it changes with every session. The stable references are
> [`CONTINUE_HERE.md`](CONTINUE_HERE.md) (how to work), [`ARCHITECTURE.md`](ARCHITECTURE.md) (how it is built),
> [`DECISIONS.md`](DECISIONS.md) (why) and [`SETUP.md`](SETUP.md) (any computer). **Update this file at the end of every
> session** (see the checklist at the bottom).

Last updated: **2026-10-04**, by Claude (Sonnet 5.5) with the owner (Fernando).

## 1. In one paragraph

Kimon is a Minecraft **26.2 / NeoForge 26.2.0.88 / Java 25** mod that re-implements, from scratch, the mechanics of the
discontinued mod *Dragon Block C*: six trainable attributes bought with Training Points, a Release % power multiplier,
Ki / Stamina resources, races and classes, skills, transformations (per race) and Kaioken. Balance lives in **datapack
JSON** (races, classes, skills, forms) and a server/client **config**; all math is in pure, unit-tested Java classes. The
design source of truth is [`docs/research/`](research/) (three documents written by the owner).

## 2. Git state

| Branch | Where | What it holds | State |
| --- | --- | --- | --- |
| `main` | GitHub + local | tagged releases (last: **v0.3.0**) | stale on purpose; only touched on releases |
| `develop` | GitHub + local | everything finished and merged, **207 tests** | the base for new work |
| `feature/kaioken` | GitHub + local | `develop` + Kaioken (+ its diagnostics), **222 tests** | **not merged**: it has an open bug, see §4 |
| `docs/handoff` | GitHub + local | this documentation set | merged into `develop` when you read this |

Everything merged into `develop` so far (oldest first), each one a `feature/*` branch merged with `--no-ff`:
`release-state-machine` (Release states, config, controls, TP by fighting, regen lock, throttled sync, aura) →
`progression-training` (level, cost curve, bulk buying, weights, gravity) → `datapack-races-classes` → `dragon-ball-names`
→ `skills-json` → `forms-json` (+ the transform-key fix).

> **Several agents can touch this repo at the same time.** On 2026-10-04 another session switched the shared checkout
> from `docs/handoff` to `feature/kaioken` and stashed the working tree (`git stash list` shows it). Use a **git
> worktree** for your own work and never switch branches in a checkout you did not create. See
> [`CONTINUE_HERE.md`](CONTINUE_HERE.md#working-next-to-other-agents).

## 3. The 12 implementation steps of `docs/research/03-resto-sistemas.md`

| # | Step | Status | What exists / what is missing |
| --- | --- | --- | --- |
| 1 | Base: player data, sync, attributes, TP, Release, Ki, health, Stamina | ✅ done | Release 5-state machine, charge/discharge/reset/turbo, throttled sync, aura state, dimension re-sync. Body is **vanilla health** (no separate pool). |
| 2 | Progression: TP, attribute cost, level, weights, gravity | ✅ done | Missing: the simple **minigames** (cost 1 TP to start), "Hard/Insane" difficulty, weights in dedicated slots (today: carried in the inventory). |
| 3 | Races and classes from JSON, creation screen | ◐ data done | 6 races × 3 classes as JSON. Missing: the **character-creation screen** (races are set with `/kimon race`). |
| 4 | Skills from JSON | ✅ done | Jump, Dash, Fly, Endurance, Potential Unlock, Ki Sense + 5 racial skills. Missing: Ki Sense **lock-on (Z)**, Dash **swoop**, learning from **master NPCs** (today: Skills screen). |
| 5 | Forms from JSON + the transformation state machine | ✅ done | 15 forms in per-race ladders, unlocked by racial skills. Missing: **Oozaru / Golden Oozaru**, **Full Power SSJ**, God forms, Arcosian "minimal forms" and Power Points, **instant transform (double-tap G)**. |
| 5b | Kaioken | ◐ on branch | Written and tested on `feature/kaioken`; **bug in-game**, see §4. |
| 6 | Form Mastery | ◐ basic | Exists and is saved, but with a simple rule (+0.5 mastery/s). Missing the research's rework: 4 gain paths, diminishing returns `D/(L+D)`, cost reductions, instant-transform unlock, auto-learn rules. |
| 7 | Ki attacks (prefixed + custom techniques) | ◐ minimal | One **Ki Blast** by raycast. Missing: the 9 technique types, custom technique creator, a **projectile entity** + renderer, charge/overcharge, clash. |
| 8 | World: Lookout, dimensions, Spacepod, master NPCs | ⏳ not started | Needs entities, structures, dimensions (datapack), dialogue. |
| 9 | Death, Other World, alignment, Enma | ⏳ not started | |
| 10 | Sagas / missions engine (JSON) and enemies | ⏳ not started | Schema is in `research/03` ("Motor de misiones"). |
| 11 | Dragon Balls and wishes, Senzu, scouter, Medical Pod | ◐ placeholder | The **Dragon Ball** item (id `wish_orb`) is a one-use random reward, not the 7-ball summon. |
| 12 | Special forms: God, Ultra Instinct, Mystic, God of Destruction, Power Points, Majin absorption, fusion, Instant Transmission | ⏳ not started | |

Cross-cutting work that is **not** a numbered step: the **visual/design pass** the owner asked for ("después mejoramos el
diseño"): HUD bars instead of text, a proper character sheet, aura rendering, real textures (blocks/items still use vanilla
textures), animations; Battle Power and the scouter; a Defense stat; the exact attribute-cost formula (unknown).

## 4. The open bug: Kaioken switches itself off

**Branch:** `feature/kaioken` (HEAD `382e10b`). **Reported by the owner:** "no se mantiene el kaioken" — after pressing
<kbd>X</kbd> the tier turns on but does not stay on.

What is known:
- The pure logic (`KaiokenRules`, `FormRules.cycleStacking/canEnter`, `FormEffect.stackedWith`) is covered by 15 tests and
  behaves; the dev log has **no errors**.
- `KaiokenHandler.onPlayerTick` (server, every tick) switches Kaioken off in exactly two cases: `FormRules.canEnter(...) != OK`
  (skill level too low, **Release below 5%**, wrong race, tier missing) or health at/below `skills.kaiokenStopHealth` (20%).
- Commit `382e10b` makes it **say why** (action bar message `msg.kimon.kaioken_ended_*` + an INFO line in the log:
  `Kaioken ended for <name>: <check> (release …, state …)`).

Hypotheses, most likely first:
1. **Ki runs out** → `ReleaseState.EXHAUSTED` sets Release to 0 → Kaioken's 5% Release requirement fails. Likely when stacked on
   an expensive form (Super Saiyan 3 drains 12 Ki/s; a fresh Saiyan has ~200 max Ki).
2. Release is lowered by something else (e.g. the discharge key `Ctrl+C` mapping reporting `isDown()` for plain C).
3. A key problem: <kbd>X</kbd> is also vanilla's "Load Hotbar Activator"; the cycle key could be firing twice.

**Next action:** run the game (`./gradlew runClient`), `/kimon race saiyan`, `/kimon tp 100000`, `/kimon attr mind 100`, learn
Kaioken in <kbd>J</kbd>, charge with <kbd>C</kbd> past 5%, press <kbd>X</kbd>, and read the action bar / `run/client/logs/latest.log`.
Then fix the cause, remove or keep the diagnostics, and merge. If hypothesis 1 is right, the design question is whether
Kaioken should require Release at all once active (the research does not say).

## 5. Test and build state

- `develop`: **207** tests, 0 failures, `./gradlew build` passes, the dev client boots with no errors.
- `feature/kaioken`: **222** tests, 0 failures.
- Not verified by an automated test: anything that needs a running world (flight feel, dash, the HUD, key presses, multiplayer
  aura). The owner tests those by playing; ask them.

## 6. Suggested next steps (in order)

1. Close the Kaioken bug (§4) and merge `feature/kaioken`.
2. **Step 6 — Form Mastery rework + instant transform (double-tap G)**; it reuses `FormDef`/`MasteryData` and is the natural next slice.
3. **Step 7 — Ki techniques** with a real projectile entity (high visual payoff, unlocks the "design pass").
4. The **design pass** (HUD bars, character sheet layout, aura rendering) — the owner postponed it, ask before starting.
5. Steps 8–12 in order; each is large, split them into vertical slices.

## 7. Questions waiting for the owner

- Should Kaioken keep working while Release is below 5% (see §4)?
- A form to bring back one-step-down on **double-tap H** (the old <kbd>V</kbd> key was removed)?
- Wish Orb / Dragon Ball item: keep as a placeholder or remove until the 7-ball system?
- `tp.hitChance` is **0.2** (from the research's "~20% per hit"); the formula document has no probability. Keep, or use 1.0?
- Is the owner fine with the visible Dragon Ball names in a **public** repository? (their decision, recorded in `DECISIONS.md`)

## 8. End-of-session checklist (do this before you stop)

1. `./gradlew test build` pass; `./gradlew runClient` boots (the owner plays it).
2. Update **this file** (§2 branches, §3 table, §4 bugs, §5 counts, §6 next steps) and the test count in `README.md`.
3. Update `CHANGELOG.md` `[Unreleased]`, and `docs/DESIGN.md` if a step changed status; regenerate the roadmap:
   `python3 docs/tools/make_roadmap.py`.
4. Commit with Conventional Commits and the co-author trailer; push **the branch you worked on** so another computer can
   fetch it (`git push -u origin <branch>`).
5. If the work is finished and verified: merge into `develop` with `--no-ff`, push `develop`, delete the feature branch.
