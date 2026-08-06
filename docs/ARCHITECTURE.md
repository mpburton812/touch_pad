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
    │  LFOs → Voices(+Drift/Weight/Swell/Shimmer) → VCA → Mix×0.25
    │  → Texture(pink) → LPF(Pulse) → Chorus → Echo → Reverb → interleaved L/R
    ▼
Device audio
```

## Envelope

- Press: attack 250ms to peak; sustain while held
- Release: decay duration from Decay slider (80ms–4000ms) back to idle alpha 0.3
- Visual alpha and audio VCA stay coupled via IntensityMapper
- Swell: attack pitch starts ~1 semitone down and glides to target with intensity
- Shimmer: octave-up partial sent only into the reverb bus

## Controls drawer

Bottom handle expands a panel (alpha 0.9) over the pads with Timbre, Brightness, Atmosphere,
Pulse, Drift, Chorus, Echo, Decay, Texture, Weight, Swell, Shimmer.
