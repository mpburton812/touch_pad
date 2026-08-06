#pragma once

/**
 * Single oscillator voice with continuous phase accumulation.
 *
 * Why continuous phase: restarting phase on each tap creates audible clicks.
 * How: advance phase by frequency/sampleRate each sample and wrap at 1.0.
 */
class Voice {
public:
    void setSampleRate(float sampleRate);
    void setFrequency(float frequencyHz);

    /**
     * Render one sample morphing sine→triangle by [timbre] in 0..1.
     */
    float render(float timbre);

private:
    float sampleRate_ = 48000.0f;
    float frequencyHz_ = 440.0f;
    float phase_ = 0.0f;
    float phaseIncrement_ = 0.0f;

    void updateIncrement();
};
