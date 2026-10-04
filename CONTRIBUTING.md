# Contributing to Kimon

Thanks for your interest in improving Kimon! This document describes how the project is
organized and how to propose changes.

## Prerequisites

- **JDK 25** (the toolchain is pinned to it; the system JDK is not used).
- Git.
- No IDE is required, but IntelliJ IDEA or Eclipse are recommended for NeoForge development.

```bash
export JAVA_HOME="$(brew --prefix openjdk@25)/libexec/openjdk.jdk/Contents/Home"  # macOS/Homebrew
./gradlew build
```

## Branching model — GitFlow

This repository follows [GitFlow](https://nvie.com/posts/a-successful-git-branching-model/):

| Branch | Purpose | Base | Merges into |
| --- | --- | --- | --- |
| `main` | Stable, released code only. Tagged releases live here. | — | — |
| `develop` | Integration branch for the next release. | `main` | `main` (via release) |
| `feature/*` | A single feature or change. | `develop` | `develop` |
| `release/*` | Stabilization before a release (version bump, final fixes). | `develop` | `main` + `develop` |
| `hotfix/*` | Urgent fix against a release. | `main` | `main` + `develop` |

### Typical feature workflow

```bash
git switch develop
git switch -c feature/power-attribute-scaling
# ... make changes, commit ...
git push -u origin feature/power-attribute-scaling
# open a Pull Request targeting `develop`
```

Never commit directly to `main` or `develop`; always go through a `feature/*` branch and a PR.

## Commit messages — Conventional Commits

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<optional scope>): <short summary>
```

Common types: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, `build`, `ci`.

Examples:

```
feat(power): scale movement speed with Power level
fix(network): clamp train requests server-side
docs(readme): document the train keybind
test(power): cover ceiling overflow
```

## Before opening a Pull Request

Your PR must pass the same checks CI runs:

```bash
./gradlew test     # unit tests must pass
./gradlew build    # must produce the mod jar without errors
```

Also:

- Keep client-only code (`net.minecraft.client.*`) inside `Dist.CLIENT`-gated classes.
  Importing client classes into common code is a server-crash bug.
- Put non-trivial logic in plain, unit-testable classes (like `PowerData`) where possible,
  rather than entangling it with world/entity objects.
- Use **original** names and assets only. Do not add third-party IP (anime franchises, other
  mods' assets, etc.).

## Scope discipline

Kimon grows **one vertical slice at a time**. A good PR is a single, complete, playable
increment with its logic tested — not a large half-finished subsystem. If a change is big,
split it.
