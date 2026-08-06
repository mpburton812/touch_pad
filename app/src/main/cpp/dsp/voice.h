#pragma once

/**
 * Continuous phase oscillator with optional per-sample pitch drift multiplier.
 */
class Voice {
public:
    void setSampleRate(float sampleRate);
    void setFrequency(float frequencyHz);

    /**
     * Render one sample morphing sine→triangle by [timbre] in 0..1.
     * @param driftMultiplier micro pitch scale (e.g. 0.985..1.015); 1.0 = exact.
     */
    float render(float timbre, float driftMultiplier = 1.0f);

private:
    float sampleRate_ = 48000.0f;
    float frequencyHz_ = 440.0f;
    float phase_ = 0.0f;
    float phaseIncrement_ = 0.0f;

    void updateIncrement();
};
