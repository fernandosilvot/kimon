# SETUP — work on Kimon from any computer

Everything needed to go from a blank machine to a running game. Verified on macOS (Apple Silicon); the Linux and Windows
steps use the same Gradle wrapper, so they differ only in how you install Java.

## 1. What you need

| Tool | Version | Notes |
| --- | --- | --- |
| **JDK** | **25** (exactly; 26 is *not* what the build is verified with) | Temurin, Oracle or Homebrew OpenJDK all work. |
| **Git** | any recent | |
| **GitHub CLI `gh`** | optional | for pushing/PRs; `gh auth login` |
| Gradle | **do not install** | the repo ships `./gradlew` (Gradle 9.2.1, NeoGradle 7.1.39) |
| Disk / RAM | ~6 GB free, 8 GB RAM | the first build decompiles Minecraft (`-Xmx3G` is set in `gradle.properties`) |
| Display + GPU | only for `runClient` | tests and `build` are headless |

## 2. Install Java 25

```bash
# macOS (Homebrew)
brew install openjdk@25
export JAVA_HOME="$(brew --prefix openjdk@25)/libexec/openjdk.jdk/Contents/Home"

# Linux (Debian/Ubuntu) – Temurin 25 from https://adoptium.net, or SDKMAN:
curl -s https://get.sdkman.io | bash && sdk install java 25-tem
export JAVA_HOME="$HOME/.sdkman/candidates/java/current"

# Windows (PowerShell)
winget install EclipseAdoptium.Temurin.25.JDK
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25..."   # adjust to the real folder
```

Check with `"$JAVA_HOME/bin/java" -version` → must print `25`. **Setting `JAVA_HOME` matters**: on the owner's Mac the default
`java` is 26, so a bare `./gradlew` would use the wrong one. Put the `export` in your shell profile or prefix every command.

## 3. Get the code

```bash
git clone https://github.com/fernandosilvot/kimon.git
cd kimon
git switch develop            # the integration branch; main only holds releases
git fetch --all --prune       # see every branch (feature/*)
```

Work happens on `feature/<name>` branches cut from `develop` (see `CONTINUE_HERE.md`). To continue unfinished work, check the
branch table in `docs/STATUS.md` and `git switch feature/kaioken` (or whichever is listed).

## 4. Build, test, run

```bash
./gradlew test          # all unit tests (pure Java, no game) — must pass
./gradlew build         # compile + test + jar — must pass
./gradlew runClient     # opens a Minecraft window with the mod loaded (needs a display)
```

- **First run is slow** (10–20 min): NeoGradle downloads and decompiles Minecraft 26.2. Later runs take seconds.
- `runClient` creates `run/client/` (git-ignored): `options.txt`, `config/`, `saves/`, `logs/latest.log`.
- Kimon's config files appear after the first launch: `run/client/config/kimon-client.toml` immediately, and
  `run/client/saves/<World>/serverconfig/kimon-server.toml` once you enter a world.
- Running tests only: `./gradlew test --console=plain 2>&1 | tail -40`. Reports: `build/reports/tests/test/index.html`
  and `build/test-results/test/*.xml`.
- A dedicated server (`./gradlew runServer`) needs you to accept the Minecraft **EULA** in `run/server/eula.txt` yourself;
  an agent should not accept it for you.

### Play-test recipe (creative world with cheats)

```
/op <you>
/kimon race saiyan          # choose the race FIRST: it re-seeds your attributes (Mind included)
/kimon tp 100000
/kimon attr mind 100        # Mind is the budget for skills
```
Then <kbd>J</kbd> to learn **Super Form**, hold <kbd>C</kbd> until Release > 10%, press <kbd>G</kbd> to transform. The full
list of keys and commands is in `ARCHITECTURE.md` §6–§7.

## 5. The GitHub side

```bash
gh auth login                       # once per computer
git push -u origin feature/<name>   # share a branch so another computer can pick it up
```
CI (`.github/workflows/build.yml`) builds and tests every push/PR on `main` and `develop` with JDK 25.

## 6. Troubleshooting (all of these happened)

| Symptom | Cause / fix |
| --- | --- |
| Build uses Java 26 or "unsupported class file" | `JAVA_HOME` not set to JDK 25 (see §2). |
| `OutOfMemoryError` while decompiling | keep `org.gradle.jvmargs=-Xmx3G`; close other apps. |
| Weird Gradle configuration-cache errors | `org.gradle.configuration-cache=false` is **on purpose** (NeoGradle 7.1.39). Do not enable it. |
| A key does nothing / does the wrong thing after an update | `run/client/options.txt` keeps old key bindings (e.g. the old Transform = R). Delete the `key_key.kimon.*` lines, or rebind in Options → Controls. A renamed mapping avoids the clash (see `CHANGELOG.md`, "Fixed"). |
| The game window closes by itself | closing the window ends `runClient`; relaunch. Do not run two `runClient` at once on the same `run/` folder. |
| `zsh: no matches found: --include=*.java` | zsh expands globs; quote them: `grep -rn x --include='*.java' src`. |
| `zsh: ==== not found` | a line starting with `=` is a command in zsh; use `echo "----"` instead. |
| Data/lang changes do not show up | they are read from `build/resources/main`; re-run `./gradlew runClient` (it re-processes resources). A datapack change inside a world needs `/reload`. |
| `git worktree` / branch checked out elsewhere | a branch can live in one worktree at a time; use `git worktree list`. |
| Pushing says "non-fast-forward" | someone (another agent/computer) pushed first: `git fetch`, then merge or rebase; **never** `--force` to `develop`/`main`. |

## 7. IDE

IntelliJ IDEA: *Open* the folder as a Gradle project, set the **Project SDK and Gradle JVM to 25**, then run the `runClient`
Gradle task (or the generated run configuration). Eclipse works the same way via Buildship. VS Code needs the Java + Gradle
extensions. Commit no IDE files (`.idea/`, `.vscode/` are git-ignored).

## 8. Where the design documents are

Everything an agent needs to understand *what* to build is **inside the repo**:

- [`docs/research/01-investigacion-dbc.md`](research/01-investigacion-dbc.md) — history, architecture, license, port guide.
- [`docs/research/02-release-ki-stats.md`](research/02-release-ki-stats.md) — Release, Ki, health, Stamina, formulas, config table.
- [`docs/research/03-resto-sistemas.md`](research/03-resto-sistemas.md) — progression, races, forms, skills, combat, death, world,
  sagas, Dragon Balls, JSON schemas and **the 12-step implementation order**.

Confidence tags in those files: `[OF]` official, `[COM]` community, `[DMZ]` DragonMine Z design, `[PROP]` proposal. Any number
tagged `[COM]`/`[PROP]` must be **configurable or in data, never hard-coded**.
