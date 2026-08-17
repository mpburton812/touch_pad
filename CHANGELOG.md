# Changelog

All notable changes to Touch Pad are documented here.

## [1.4.0] - 2026-08-17

### Changed
- Cube pads use a rounded outer silhouette with matching extruded faces (no leftover highlight rectangle).
- In-app update check reads `docs/version.json` from raw GitHub while Pages deploy is unreliable.

## [1.3.0] - 2026-08-06

### Added
- Texture (pink-noise bed), Weight (sub-octave), Swell (attack pitch glide), and Shimmer (octave-up reverb send) sliders.
- Rounded edges on translucent top-down cube pads.

### Changed
- DSP mix path inserts continuous Texture before the brightness LPF; Shimmer feeds the reverb bus.

## [1.2.0] - 2026-08-06

### Added
- Translucent top-down cube appearance for each pad.

### Changed
- Idle pad luminosity set to 30% (scales to 100% at peak volume).
- Nebula backdrop spins slightly faster with a smooth field rotation.

## [1.1.0] - 2026-08-06

### Added
- Press-and-hold pad sustain with Decay-controlled release.
- Bottom Controls drawer (translucent) hosting all synth sliders.
- Pulse, Drift, Chorus, and Echo effects in a stereo Oboe pipeline.

### Changed
- DSP order: LFOs → voices → VCA → mix → LPF → chorus → echo → reverb → stereo out.

## [1.0.0] - 2026-08-06

### Added
- Initial Jetpack Compose pad grid (2x4) with requirement colors and notes.
- Native Oboe DSP engine (sine/triangle timbre, LPF brightness, post-VCA reverb).
- Attack/hold/decay visual-audio envelope with retrigger lerp.
- Fit-to-bounds pad grid, 10 percent transparent idle pads, compact in-bar sliders.
- Preferences DataStore for slider/mute persistence.
- Static version.json update check scaffold and FileProvider paths.
- 16 KB page-size native alignment (NDK 28.2).