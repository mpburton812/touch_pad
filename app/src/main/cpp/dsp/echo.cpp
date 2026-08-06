#include "dsp/echo.h"

void StereoEcho::setSampleRate(float sampleRate) {
    sampleRate_ = sampleRate > 1.0f ? sampleRate : 48000.0f;
    // Fixed ~625ms relaxing delay.
    delaySamples_ = std::max(1, static_cast<int>(sampleRate_ * 0.625f));
    const int size = delaySamples_ + 1;
    bufferL_.assign(static_cast<size_t>(size), 0.0f);
    bufferR_.assign(static_cast<size_t>(size), 0.0f);
    writeIndex_ = 0;
}

void StereoEcho::process(float inL, float inR, float amount, float& outL, float& outR) {
    const float mix = std::clamp(amount, 0.0f, 1.0f);
    // Feedback rises with amount but stays stable (< 0.85).
    const float feedback = mix * 0.75f;

    const int size = static_cast<int>(bufferL_.size());
    int readIndex = writeIndex_ - delaySamples_;
    if (readIndex < 0) {
        readIndex += size;
    }

    const float delayedL = bufferL_[static_cast<size_t>(readIndex)];
    const float delayedR = bufferR_[static_cast<size_t>(readIndex)];

    bufferL_[static_cast<size_t>(writeIndex_)] = inL + delayedL * feedback;
    bufferR_[static_cast<size_t>(writeIndex_)] = inR + delayedR * feedback;
    writeIndex_ = (writeIndex_ + 1) % size;

    outL = inL * (1.0f - mix) + delayedL * mix;
    outR = inR * (1.0f - mix) + delayedR * mix;
}
