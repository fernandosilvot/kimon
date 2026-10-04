# Kimon

> An original anime-style **Power / energy progression** mod for Minecraft.
> Train your Power, feel it grow.

[![Build](https://github.com/fernandosilvot/kimon/actions/workflows/build.yml/badge.svg)](https://github.com/fernandosilvot/kimon/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
![Minecraft](https://img.shields.io/badge/Minecraft-26.2-brightgreen)
![NeoForge](https://img.shields.io/badge/NeoForge-26.2.0.88-orange)
![Java](https://img.shields.io/badge/Java-25-red)

Kimon is a from-scratch progression mod inspired by the *feel* of classic anime-fighter
mods (train, grow stronger, power up) — built with **100% original names, terms, and
assets** to stay clear of any third-party intellectual property. There is no "Saiyan",
no "Kamehameha", no Dragon Ball branding: just an original *Power* stat and its own vocabulary.

---

## What it does today (v0.1.0 — vertical slice)

The first release is a small but **fully working, end-to-end vertical slice** of the core loop:

| Feature | Status |
| --- | --- |
| Per-player **Power** stat, persisted across relog & death | ✅ |
| **Train** keybind (default <kbd>G</kbd>) that raises Power by `+5` | ✅ |
| Server-authoritative logic (client cannot forge the value) | ✅ |
| Automatic server → client sync of the stat | ✅ |
| **HUD** overlay showing current Power (top-left, hidden under F3) | ✅ |
| English + Spanish localization | ✅ |
| Unit-tested progression rules (9 tests) | ✅ |

> This is deliberately a *thin slice*, not a half-finished galaxy of features. It proves the
> three genuinely hard subsystems (data attachments, payload networking, client HUD) work
> together on Minecraft 26.2, and gives every future feature a verified template to build on.
> See the [Roadmap](#roadmap).

### Try it

1. In game, open a world.
2. Look at the **top-left**: `Power: 0`.
3. Press <kbd>G</kbd> to train — the number rises by 5 each press.
4. Rebind the key under **Options → Controls → Miscellaneous → "Train Power"**.

---

## Requirements

| | |
| --- | --- |
| Minecraft | **26.2** (Java Edition) |
| Mod loader | **NeoForge 26.2.0.88+** |
| Java (to run) | **JDK 25** (Mojang ships Java 25 with 26.2) |
| Java (to develop) | **JDK 25** |

## Building from source

```bash
# JDK 25 must be on JAVA_HOME. On macOS with Homebrew:
export JAVA_HOME="$(brew --prefix openjdk@25)/libexec/openjdk.jdk/Contents/Home"

./gradlew build          # produces build/libs/kimon-<version>.jar
./gradlew test           # runs the JUnit unit tests
./gradlew runClient      # launches a dev client with the mod loaded
```

The built mod jar lands in `build/libs/`. Drop it into a NeoForge 26.2 instance's `mods/`
folder to play outside the dev environment.

---

## Architecture

Kimon follows the standard NeoForge split between the **mod bus** (registration/lifecycle) and
the **game bus** (runtime events), and keeps all client-only code behind a `Dist.CLIENT` gate so
it never loads on a dedicated server.

```
net.kimon.kimon
├── Kimon.java                 # @Mod entry point; wires registries on the mod bus
├── power/
│   ├── PowerData.java         # immutable stat + progression math (pure JVM, unit-tested)
│   └── ModAttachments.java    # registers the POWER data attachment (persist + sync + copy-on-death)
├── network/
│   ├── TrainPowerPayload.java # serverbound "train" request packet
│   └── ModNetworking.java     # registers payloads + server-side handler
└── client/                    # @Dist.CLIENT only
    ├── KimonClient.java       # keybind + per-tick input → sends the train packet
    └── PowerHudLayer.java     # HUD overlay reading the synced stat
```

### Data flow of one "train" action

```
[client] press G
   └─► ClientPacketDistributor.sendToServer(TrainPowerPayload)
          └─► [server] ModNetworking.handleTrain
                 └─► player.setData(POWER, current.train())   // authoritative + clamped
                        └─► attachment auto-syncs back to the owning client
                               └─► [client] PowerHudLayer reads player.getData(POWER) → draws it
```

The key design decision: because the `POWER` attachment is registered with `.sync(...)`, NeoForge
pushes the new value to the client automatically on `setData`. That removes the need for a
hand-written clientbound packet — the only custom packet is the serverbound train request.

---

## Testing

- **Unit tests** (`src/test/java`) cover all of `PowerData`'s balancing rules — initial value,
  accumulation, immutability, clamping, ceiling/overflow behavior. These run on a plain JVM with
  no Minecraft bootstrap, so they are fast and reliable in CI.

  ```bash
  ./gradlew test
  ```

- **In-game GameTests** are intentionally *not* included yet. Minecraft 26.2 replaced the old
  `@GameTest` annotation system with a registry-based `GameTestInstance` framework that requires
  datagen-registered test instances; wiring that up is tracked as future work rather than shipped
  half-done. The gameplay path is instead verified manually (and via the unit-tested core logic).

---

## Roadmap

Features are added **one vertical slice at a time**, each built on the verified pattern above.
Order is indicative, not a promise.

- [ ] Power actually *does* something (scales a movement/attack attribute)
- [ ] A training block / activity that gates Power gain (instead of a free keybind)
- [ ] A dedicated progression screen (GUI) instead of just the HUD number
- [ ] An energy "blast" projectile entity
- [ ] Original "forms" (temporary buffs) — deferred until a 26.2 animation library exists
- [ ] In-game GameTests on the new 26.2 framework

See [CONTRIBUTING.md](CONTRIBUTING.md) for the branching model and how to propose changes.

---

## Legal / originality

Kimon is an **original work** and is **not affiliated with, endorsed by, or derived from** any
anime franchise or any existing Minecraft mod. All names, terminology, text, and assets are
original to this project. It is distributed under the [MIT License](LICENSE).

"Minecraft" is a trademark of Mojang Synergies AB. "NeoForge" is a project of the NeoForged team.
This mod is an independent, unofficial project.
