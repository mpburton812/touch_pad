#include "dsp/voice.h"

#include <algorithm>
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

float Voice::wrapPhase(float phase) {
    while (phase >= 1.0f) {
        phase -= 1.0f;
    }
    while (phase < 0.0f) {
        phase += 1.0f;
    }
    return phase;
}

VoiceSample Voice::render(
    float timbre,
    float driftMultiplier,
    float weightAmt,
    float shimmerAmt,
    float swellPitchMult) {
    const float pitchScale = driftMultiplier * swellPitchMult;
    const float inc = phaseIncrement_ * pitchScale;
    const float subInc = phaseIncrement_ * 0.5f * pitchScale;
    const float shimmerInc = phaseIncrement_ * 2.0f * pitchScale;

    phase_ = wrapPhase(phase_ + inc);
    subPhase_ = wrapPhase(subPhase_ + subInc);
    shimmerPhase_ = wrapPhase(shimmerPhase_ + shimmerInc);

    const float twoPi = static_cast<float>(2.0 * M_PI);
    const float sine = std::sin(phase_ * twoPi);
    const float triangle = (phase_ < 0.5f)
        ? (4.0f * phase_ - 1.0f)
        : (3.0f - 4.0f * phase_);
    const float t = std::clamp(timbre, 0.0f, 1.0f);
    const float primary = sine * (1.0f - t) + triangle * t;

    const float sub = std::sin(subPhase_ * twoPi) * std::clamp(weightAmt, 0.0f, 1.0f);
    const float shimmer = std::sin(shimmerPhase_ * twoPi) * std::clamp(shimmerAmt, 0.0f, 1.0f);

    VoiceSample out;
    out.dry = primary + sub;
    out.shimmerSend = shimmer;
    return out;
}
