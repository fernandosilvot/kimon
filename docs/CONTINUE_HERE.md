# CONTINUE HERE — the entry point for any agent, on any computer

You are an AI coding agent (Claude, Kiro, Cursor, Copilot, …) or a human picking up **Kimon**. Read this page first; it takes five
minutes and tells you what to read next, what the rules are, and how to avoid the mistakes already made.

## 1. What Kimon is

A Minecraft **26.2 / NeoForge 26.2.0.88 / Java 25** mod, written clean-room, that re-implements the mechanics of the discontinued mod
*Dragon Block C*: six attributes bought with Training Points (TP), a Release % power multiplier, Ki and Stamina, races and classes,
skills, per-race transformations and Kaioken. Names and terms follow Dragon Ball's on purpose. It lives in a **public GitHub
repository** (`fernandosilvot/kimon`, MIT). The owner is **Fernando**; he talks to you in **Spanish** (rioplatense/informal) and
cares about *playing* it: he tests every slice in the game and reports what feels wrong.

## 2. Read in this order

1. **This file.**
2. [`STATUS.md`](STATUS.md) — what is done, which branch holds what, the **open bug**, the next steps, questions for the owner.
3. [`SETUP.md`](SETUP.md) — install Java 25, clone, build, test, run (macOS / Linux / Windows).
4. [`ARCHITECTURE.md`](ARCHITECTURE.md) — packages, attachments, payloads, datapack formats, formulas, config, controls, commands, tests,
   how to extend, 26.2 API gotchas.
5. [`DECISIONS.md`](DECISIONS.md) — owner decisions, legal/provenance, contradictions in the research and how they were resolved, our
   own `[PROP]` numbers.
6. [`research/`](research/) — the design source of truth (three documents written by the owner): `01` history/architecture/license,
   `02` Release/Ki/stats with formulas and the config table, `03` everything else + JSON schemas + the **12-step order**.
7. `README.md` (features, controls, user guide), `CHANGELOG.md`, `DESIGN.md` (roadmap), `CONTRIBUTING.md`.

## 3. Ground rules

1. **Server-authoritative.** Clients send intents, never values. Validate everything on the server.
2. **Pure logic first, with tests.** New balance math goes into Minecraft-free classes with JUnit tests; glue stays thin.
3. **Data, not constants.** Anything the research tags `[COM]` or `[PROP]` goes to config or datapack JSON.
4. **The research is the spec.** If you change a number or a rule, say which document it came from, or record the deviation in
   `DECISIONS.md`. Where the documents contradict each other, follow what is already decided there.
5. **Don't copy other people's code or assets.** See `DECISIONS.md` §2 (provenance and legal). Names and terms are free by the owner's
   decision; textures, models, sounds of Dragon Block C are not. Ask the owner before touching the decompiled
   copies that may exist on his machine (`MC/test/`, outside this repo) and never commit them.
6. **One phase per session, one vertical slice per branch.** Finish and verify before starting the next.
7. **Keep old data readable.** Renaming a key in saves/JSON/commands needs a legacy alias (several exist; copy the pattern).
8. **Don't invent facts.** If a number is unknown, make it config, mark it `[PROP]`, and list it in `DECISIONS.md` §4.
9. **Be honest in reports.** Say what you verified (tests, boot, play-test) and what you could not. Do not claim a gameplay feature
   works because it compiles.
10. **Respond in Spanish** to the owner; code, comments in docs, commits and identifiers are English.

## 4. Workflow (GitFlow)

```bash
git switch develop && git pull
git switch -c feature/<short-name>          # never commit features to develop/main directly
# …implement; pure logic + tests…
export JAVA_HOME=<JDK 25>                   # see SETUP.md
./gradlew test build                        # must pass
./gradlew runClient                         # must boot without errors; the owner plays it
# update README / CHANGELOG [Unreleased] / DESIGN / STATUS / roadmap (see STATUS.md §8)
git add -A && git commit                    # Conventional Commits + the co-author trailer (below)
git push -u origin feature/<short-name>     # lets another computer continue the branch
# when verified (the owner has played it, or moved on without reporting problems):
git switch develop && git merge --no-ff feature/<short-name> && git push origin develop
git branch -d feature/<short-name> && git push origin --delete feature/<short-name>
```

- Commit style: `feat(scope): …`, `fix(scope): …`, `docs: …`, `chore: …`; body lists the changes; end with
  `Co-Authored-By: <your model name> <noreply@anthropic.com>` if you are an Anthropic model (otherwise your own attribution).
- Only `main` and `develop` persist long-term. `main` is touched only for releases: move `[Unreleased]` → `[X.Y.Z]` in the changelog,
  bump `mod_version` in `gradle.properties`, merge `develop` → `main`, tag `vX.Y.Z`, `gh release create`.
- **Never force-push** `main`/`develop`. If a push is rejected, `git fetch` and merge.
- **Keep risky or unconfirmed work on its branch** (and push the branch). Merge to `develop` when verified.

## 5. Working next to other agents

More than one agent (or the owner in another terminal) can work on this repo at the same time — it already happened: another session
switched the shared checkout to another branch and stashed the working tree. Protect yourself and them:

1. At the start: `git status`, `git branch --show-current`, `git worktree list`, `git stash list`. If something is not yours, **don't touch it**.
2. Do your work in **your own worktree** so you never switch branches under someone else:
   ```bash
   git worktree add ../kimon-<task> -b feature/<task> develop     # or an existing branch
   cd ../kimon-<task>      # build, test, commit and push from here
   git worktree remove ../kimon-<task>        # when finished
   ```
3. A branch can be checked out in only one worktree. Use `git stash apply` (not `pop`) when you copy changes someone else stashed.
4. Two agents must not run `runClient` on the same `run/` folder at once (use separate worktrees; each has its own `run/`).
5. Before editing a shared doc (`STATUS.md`, `CHANGELOG.md`, `README.md`), `git fetch` and re-read it — someone may have changed it.
6. If you see a conflicting instruction in a file (e.g. a rule that was removed), don't "fix" it silently: note it in `STATUS.md`/`DECISIONS.md` and tell the owner.

## 6. Quality gates (every slice)

- [ ] `./gradlew test` → all pass (state the count).
- [ ] `./gradlew build` → passes.
- [ ] `./gradlew runClient` boots with **no `ERROR`/exception** in `run/client/logs/latest.log` (`Kimon initializing`, `Loaded N races/skills/forms` appear).
- [ ] New pure logic has tests; tests compare to the research tables where they exist.
- [ ] New strings exist in **both** `en_us.json` and `es_es.json`.
- [ ] Protocol version bumped if a payload changed; new config keys documented in `ARCHITECTURE.md` §9.
- [ ] Docs synced: `README.md` (features, controls, commands, test count), `CHANGELOG.md`, `DESIGN.md`, `STATUS.md`, roadmap SVG.
- [ ] You told the owner **what to test in the game** and what you could not verify.

## 7. Playing it (what the owner does)

See `SETUP.md` §4 for the recipe. Short version, in a creative world with cheats:
`/kimon race saiyan` → `/kimon tp 100000` → `/kimon attr mind 100` → <kbd>J</kbd> learn *Super Form* → hold <kbd>C</kbd> past 10% →
<kbd>G</kbd> transform. TP only come from hitting mobs with Release ≥ 5%. Feedback appears in the **action bar** (not in chat), so
the log may not show what he saw — ask what message appeared.

## 8. Pointers for common questions

- *How do I add a skill/form/race?* `ARCHITECTURE.md` §12.
- *Why is this number 0.2?* `DECISIONS.md` §3–4.
- *What formula does the research give?* `research/02` (Release/Ki/stats) and `research/03` (everything else).
- *Which API call is right in 26.2?* `ARCHITECTURE.md` §13.
- *What should I build next?* `STATUS.md` §6.
