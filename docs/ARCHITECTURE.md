# Architecture — Touch Pad

Single-user calming STEM toy: eight colored pads over a nebula backdrop, synthesized through a native stereo Oboe DSP engine.

## Layers

```
Jetpack Compose UI
    │  Press/release pads + bottom Controls drawer
    ▼
TouchPadViewModel (SavedStateHandle + DataStore)
    │  JNI atomics (no locks in audio callback)
    ▼
Native C++ AudioEngine (Oboe LowLatency / Exclusive / Stereo)
    │  LFOs → Voices(+Drift) → VCA → Mix×0.25 → LPF(Pulse)
    │  → Chorus → Echo → Reverb → interleaved L/R
    ▼
Device audio
```

## Envelope

- Press: attack 250ms to peak; sustain while held
- Release: decay duration from Decay slider (80ms–4000ms) back to idle alpha 0.9
- Visual alpha and audio VCA stay coupled via IntensityMapper

## Controls drawer

Bottom handle expands a panel (alpha 0.9) over the pads with Timbre, Brightness, Atmosphere, Pulse, Drift, Chorus, Echo, Decay.
