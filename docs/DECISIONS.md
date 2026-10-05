# DECISIONS — why things are the way they are

A log of what the owner decided, where the research documents contradict each other (and what we chose), which numbers are our
own proposals, and where Kimon deliberately differs from the research. Add to it whenever you make a call someone could question.

Dates are 2026-10-04 unless noted (the whole project so far was built in that one long session).

## 1. Decisions by the owner (Fernando)

| Decision | Detail |
| --- | --- |
| Continue **Kimon** on **Minecraft 26.2 / NeoForge 26.2.0.88 / JDK 25** | The first prompt asked for NeoForge 1.21.1 / Java 21 from scratch; the owner chose to keep the existing `kimon/` project (a `dbc-style-mod/` MDK template sits unused next to it). |
| **One phase per session**, one objective at a time | Orders of work follow the 12 steps of `research/03-resto-sistemas.md`. |
| **GitFlow with the assistant doing the git passes** | Branch from `develop`, `--no-ff` merge, push `develop`, delete the feature branch; Conventional Commits; co-author trailer. The assistant also **opens the game** (`runClient`) so the owner can play-test. |
| **No free TP** | "Que sea en verdad como Dragon Block C": TP only from fighting. The Train key/button was removed. |
| **Use Dragon Ball names** | Saiyan, Namekian, Arcosian, Majin, Half-Saiyan, Super Saiyan, Ki, Release, Kaioken, Dragon Ball… because invented names "no se entiende nada". Names live in lang files and data JSON so a rename is cheap. |
| **Design pass later** | "Después mejoramos el diseño y todo eso": HUD/GUI/aura visuals are deliberately postponed. |
| **Self-contained handoff** | "Que cualquier agente me ayude de cualquier PC": this documentation set, with the research documents copied into `docs/research/`. |
| Defaults follow `research/02` | Where Kimon's earlier numbers differed (charge 25, upkeep 0.004…), the research's values were adopted (charge 10, upkeep 0.002…), all configurable. |
| Keys follow the research | G transform, H reset, C / Ctrl+C charge/discharge, R turbo (the old Train key moved out; Transform key mapping renamed to avoid stale bindings). |

## 2. Provenance and legal (read before you copy anything)

Facts, without drama:

- **Dragon Ball** names and characters belong to Bird Studio / Shueisha / Toei Animation, which enforces its rights.
- **Kimon is a public GitHub repository under the MIT license**, non-commercial. Game rules and formulas are not copyrightable;
  other people's code, textures, models, sounds and configs are.
- The mechanics were reconstructed from **public** sources only: JinGames' guides and changelogs, the Fandom wikis, forum posts and
  the open-source DragonMine Z design (GPL-3.0; designs may be studied, code may not be copied unless Kimon becomes GPL) — all in
  `docs/research/`.
- 2026-10-04: the owner **lifted the "invented names only" rule** (names are fine).
- 2026-10-04: the owner decompiled the three original JARs into a folder **outside this repository**
  (`MC/test/`, with `src/`, `extracted/`, a decompiler) and asked the assistant to base fixes on how the original works. **The assistant
  declined to read or use that code** (license + the repo being public MIT) and offered to rely on public documentation and on behaviour
  the owner observes while playing the original. Afterwards the owner edited `AGENTS.md`, `CLAUDE.md` and `docs/DESIGN.md` to drop the
  sentence forbidding copying the original's code.
- **What an agent should do:** the owner's edits are respected (not reverted), but they do not change what the license says. Use your
  own judgment; **ask the owner before reading or reproducing anything from the decompiled sources**, never commit those folders (or
  any file from the original JARs, including textures/sounds) to this repository, and keep new code written from the research and from
  observed behaviour. If a rights holder objects, names and assets can be changed without touching the logic.

## 3. Contradictions inside the research documents, and what we chose

| Contradiction | Choice |
| --- | --- |
| `01`: a class changes the **starting attributes**; `03`: a class only changes **modifiers** | Modifiers only. The race fixes the 60 starting points. |
| Max Ki from **Mind** (old guide) vs **Spirit** (modern) | Spirit. |
| TP per hit: `02` formula `2+2·⌊MND/5⌋·Release/100` (no probability) vs `03` "~20% per hit" and a `rate 200` config | The formula of `02`, rolled against `tp.hitChance = 0.2` (configurable; 1.0 = every hit). |
| `02`: maxRelease `min(100, 50+5·PU)` "or 200 with overcharge" (slope unspecified) | `release.allowOvercharge` raises the **cap** to 200; `release.baseMax` is the starting point; Potential Unlock adds 5%/level. |
| `03` unlock order for Human/Namekian: Buffed/Giant first, Full Released second — but Full Released has the **lower** multiplier | Ordered by strength: Full Released = skill level 1, Buffed/Giant = level 2. |
| Three versions of the form multipliers (2016 config, old wiki, recent wiki) | The "modern config" the documents point to (SSJ 150 … SSJ4 400, etc.). |
| Arcosian starting STR and WIL "sum 20" | Split 10 / 10. (Their old Frost spread summed 55; fixed to 60.) |
| Half-Saiyan **Warrior** row missing from the modifier table | Derived: the table decomposes exactly into *race base + class offset* (Spiritualist and Warrior are the same offsets on every race), so the missing row is computed. This is also why races × classes are 6 + 3 JSON files. |
| UC (attribute cost): only its knobs are public (rate, ×0.75 per attribute, "start minus" 140, minimum) | `CostParams` curve `base + rate·x + x²/startMinus` — **our own formula** exposing those knobs. |
| Ultra Instinct skill cost: 50,000 TP vs 500,000 TP | Not implemented yet (step 12); keep it as data when you do. |

## 4. Numbers that are our own proposals ([PROP]) — tune by playing

`tp.hitChance` 0.2 (from "~20%") · `combat.hitStaminaCost` 2% · `tp.altarStaminaCost` 25% · `release.turboKiDrain` 1%/s · `ki.exhaustRecoverPct` 5% ·
`skills.mindPerPoint` 1 · racial skill costs (300 TP, 5 Mind) · Dash (40 TP / 5 Mind, 2% Ki, 1 s, strength 0.6 + 0.08/level) ·
Ki Sense range 10 blocks/level · all form numbers other than the multipliers (Ki per second, minimum Release, flat bonus, divisor =
multiplier) · Majin Super/Pure multipliers (only Evil ×2.2 is documented) · Kaioken unlock levels (1,2,4,6,8,10), health per second
(0.5…8), 5% cost reduction per level, 20% safety margin **(branch)** · `progression.powerPerPoint` 5 · the cost curve · every
`training.*` value (the research only says what weights/gravity do, and that the first device was 10G) · `StatMods` for Arcosian `fly` 30.

## 5. Intentional differences from the research

| Research | Kimon | Why |
| --- | --- | --- |
| Body is its own pool | Body = **vanilla health** (+ modifiers) | keeps vanilla damage, healing and mods working; coupling comes with the combat rework. |
| `melee = STR × 2.5 × …` | bonus is on top of the **vanilla hit**, from attribute points **above the start value** (0.1 per point) | fits Minecraft's damage scale; a new character plays like vanilla. |
| Skills taught by master NPCs | learned from a **Skills screen** with TP + Mind | NPCs are step 8. |
| Forms gated by Power tier (Kimon's first design) | gated by the **racial skill level** (the research's rule) | |
| Release decays on its own (Kimon's first design) | Release **holds** until you lower it | matches the state machine of `research/02`. |
| Mind raised the Release ceiling (first design) | **Potential Unlock** does | matches the research. |
| Wish "Dragon Ball" with 7 balls | a single-use random-reward item (id `wish_orb`) | placeholder until step 11. |

## 6. Process lessons (so you do not repeat them)

- Don't trust "it compiles": two features (Kaioken, transform key) failed only in a running game. Ask the owner to play-test, and add
  messages that say **why** something refused or ended.
- Saved `options.txt` bindings outlive code changes → rename a key mapping when its default changes.
- When a refactor changes a string-keyed format (saves, JSON), keep the **old keys readable** (see `StatCodecs`, `MasteryCodecs`,
  `CharacterCatalog.LEGACY_NAMES`, `Form` legacy names).
- `str.index('---')` on Markdown also matches table separators `| --- |`; match on a line (`\n---\n`) when cutting README sections.
- A shared checkout can be changed by another agent; use worktrees (see `CONTINUE_HERE.md`).
