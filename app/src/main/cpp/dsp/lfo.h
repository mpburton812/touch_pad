#pragma once

#include <cmath>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

/**
 * Free-running sine LFO for Pulse / Drift / Chorus modulation.
 */
class SineLfo {
public:
    void setSampleRate(float sampleRate) {
        sampleRate_ = sampleRate > 1.0f ? sampleRate : 48000.0f;
        updateIncrement();
    }

    void setFrequencyHz(float hz) {
        frequencyHz_ = hz;
        updateIncrement();
    }

    /** Advance one sample and return sine in [-1, 1]. */
    float next() {
        phase_ += phaseIncrement_;
        if (phase_ >= 1.0f) {
            phase_ -= 1.0f;
        }
        return std::sin(phase_ * static_cast<float>(2.0 * M_PI));
    }

private:
    void updateIncrement() {
        phaseIncrement_ = frequencyHz_ / sampleRate_;
    }

    float sampleRate_ = 48000.0f;
    float frequencyHz_ = 0.35f;
    float phase_ = 0.0f;
    float phaseIncrement_ = 0.0f;
};
