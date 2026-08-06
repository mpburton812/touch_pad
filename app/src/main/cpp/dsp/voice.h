#pragma once

/**
 * Per-sample voice outputs: dry (pre-VCA) and shimmer send (reverb-only).
 */
struct VoiceSample {
    float dry = 0.0f;
    float shimmerSend = 0.0f;
};

/**
 * Multi-oscillator voice: primary + Weight sub + Shimmer octave, with Swell pitch.
 */
class Voice {
public:
    void setSampleRate(float sampleRate);
    void setFrequency(float frequencyHz);

    /**
     * @param timbre sine↔triangle morph 0..1
     * @param driftMultiplier analog pitch wobble
     * @param weightAmt sub-oscillator mix 0..1
     * @param shimmerAmt octave-up send into reverb 0..1
     * @param swellPitchMult attack pitch glide (e.g. 0.9438..1.0)
     */
    VoiceSample render(
        float timbre,
        float driftMultiplier,
        float weightAmt,
        float shimmerAmt,
        float swellPitchMult);

private:
    float sampleRate_ = 48000.0f;
    float frequencyHz_ = 440.0f;
    float phase_ = 0.0f;
    float subPhase_ = 0.0f;
    float shimmerPhase_ = 0.0f;
    float phaseIncrement_ = 0.0f;

    void updateIncrement();
    static float wrapPhase(float phase);
};
