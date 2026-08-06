# Touch Pad

Relaxing noise-and-color STEM toy for Android.

Eight luminous pads over a drifting nebula; each pad plays a tuned voice through a low-latency Oboe synth with Timbre, Brightness, and Atmosphere controls.

## Requirements

See [`.requirements`](.requirements) and [docs/REQUIREMENTS-WORKFLOW.md](docs/REQUIREMENTS-WORKFLOW.md).

Jira: [TP board](https://mpburton.atlassian.net/jira/software/projects/TP/boards/40)

## Build

- Android Studio Ladybug+ / JDK 17
- `minSdk 27`, NDK + CMake, Oboe Prefab

```bash
./gradlew assembleDebug
./gradlew test
```

Open the project in Android Studio, start an emulator (API 27+), and Run.

## Branches

`feature/*` → `dev` → `test` → `production` (PR promotion only; local Gradle gates).
