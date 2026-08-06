#pragma once

/**
 * One-pole low-pass filter approximating a gentle 12dB-style roll-off.
 *
 * Why one-pole: cheap enough for per-sample use on the audio thread while still
 * giving a warm→bright sweep driven by the Brightness slider.
 */
class LowPassFilter {
public:
    void setSampleRate(float sampleRate);
    void setCutoffHz(float cutoffHz);
    float process(float input);

private:
    float sampleRate_ = 48000.0f;
    float alpha_ = 1.0f;
    float z1_ = 0.0f;
};
