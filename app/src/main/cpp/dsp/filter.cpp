#include "dsp/filter.h"

#include <algorithm>
#include <cmath>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

void LowPassFilter::setSampleRate(float sampleRate) {
    sampleRate_ = sampleRate > 1.0f ? sampleRate : 48000.0f;
}

void LowPassFilter::setCutoffHz(float cutoffHz) {
    const float clamped = std::clamp(cutoffHz, 20.0f, sampleRate_ * 0.45f);
    // Bilinear-ish one-pole coefficient from cutoff.
    const float rc = 1.0f / (2.0f * static_cast<float>(M_PI) * clamped);
    const float dt = 1.0f / sampleRate_;
    alpha_ = dt / (rc + dt);
}

float LowPassFilter::process(float input) {
    z1_ += alpha_ * (input - z1_);
    return z1_;
}
