# Architecture — Touch Pad

Single-user calming STEM toy: eight colored pads over a nebula backdrop, synthesized through a native Oboe DSP engine.

## Layers

```
Jetpack Compose UI
    │  PointerInput taps + slider StateFlow
    ▼
TouchPadViewModel (SavedStateHandle + DataStore)
    │  JNI atomics (no locks in audio callback)
    ▼
Native C++ AudioEngine (Oboe LowLatency / Exclusive)
    │  Voice → Mix×0.25 → LPF → Reverb → Output
    ▼
Device audio
```

## Pad table

| # | Color | Note | Hz |
|---|---|---|---|
| 0 | #FF0000 | C4 | 261.63 |
| 1 | #FFBF00 | D4 | 293.66 |
| 2 | #80FF00 | E4 | 329.63 |
| 3 | #00FF40 | F4 | 349.23 |
| 4 | #00FFFF | G4 | 392.00 |
| 5 | #0040FF | A4 | 440.00 |
| 6 | #8000FF | B4 | 493.88 |
| 7 | #FF00BF | C5 | 523.25 |

## Envelope

Visual alpha and audio VCA are coupled:

- Idle visual alpha `0.2`, audio amplitude `0.0`
- Attack `250ms` → Hold `1000ms` → Decay `500ms`
- Retrigger cancels and lerps from current value (no snap)
- Reverb sits **after** VCA so tails ring while pads dim

## Security / lifecycle

- `network_security_config` disables cleartext
- Manifest components declare `android:exported` explicitly
- Version checks use static `version.json` (not GitHub REST)
- Oboe start/stop bound to Activity `RESUMED`
