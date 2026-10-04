# Changelog

All notable changes to this project are documented here.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [0.1.0] - 2026-10-04

### Added
- Per-player **Power** stat stored via a NeoForge data attachment; persisted across relog and
  copied on death.
- Server-authoritative **train** action: a serverbound `TrainPowerPayload` raises Power by `+5`.
- Automatic server → client synchronization of the Power stat.
- **Train Power** key mapping (default <kbd>G</kbd>) in the Miscellaneous controls category.
- **HUD** overlay rendering the current Power in the top-left corner (hidden while F3 is held).
- English (`en_us`) and Spanish (`es_es`) localization.
- JUnit unit test suite covering all `PowerData` progression and clamping rules.
- Project scaffolding: MIT license, README, contributing guide, GitFlow branching model,
  and GitHub Actions CI (build + test).

[Unreleased]: https://github.com/fernandosilvot/kimon/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/fernandosilvot/kimon/releases/tag/v0.1.0
