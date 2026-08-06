#include "dsp/voice.h"

#include <cmath>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

void Voice::setSampleRate(float sampleRate) {
    sampleRate_ = sampleRate > 1.0f ? sampleRate : 48000.0f;
    updateIncrement();
}

void Voice::setFrequency(float frequencyHz) {
    frequencyHz_ = frequencyHz;
    updateIncrement();
}

void Voice::updateIncrement() {
    phaseIncrement_ = frequencyHz_ / sampleRate_;
}

float Voice::render(float timbre) {
    // Wrap phase into [0, 1) without using fmod (cheaper / branch-light).
    phase_ += phaseIncrement_;
    if (phase_ >= 1.0f) {
        phase_ -= 1.0f;
    }

    const float sine = std::sin(phase_ * static_cast<float>(2.0 * M_PI));
    // Triangle from phase: 4*|x-0.5|-1 style mapped to [-1,1].
    const float triangle = (phase_ < 0.5f)
        ? (4.0f * phase_ - 1.0f)
        : (3.0f - 4.0f * phase_);

    const float t = timbre < 0.0f ? 0.0f : (timbre > 1.0f ? 1.0f : timbre);
    return sine * (1.0f - t) + triangle * t;
}
