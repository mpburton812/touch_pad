#include "dsp/chorus.h"

#include <cmath>

void StereoChorus::setSampleRate(float sampleRate) {
    sampleRate_ = sampleRate > 1.0f ? sampleRate : 48000.0f;
    // Max delay ~35ms with headroom for modulation.
    bufferSize_ = static_cast<int>(sampleRate_ * 0.040f) + 4;
    buffer_.assign(static_cast<size_t>(bufferSize_), 0.0f);
    writeIndex_ = 0;
}

float StereoChorus::readFrac(float delaySamples) const {
    float readPos = static_cast<float>(writeIndex_) - delaySamples;
    while (readPos < 0.0f) {
        readPos += static_cast<float>(bufferSize_);
    }
    const int i0 = static_cast<int>(readPos) % bufferSize_;
    const int i1 = (i0 + 1) % bufferSize_;
    const float frac = readPos - std::floor(readPos);
    return buffer_[static_cast<size_t>(i0)] * (1.0f - frac) +
           buffer_[static_cast<size_t>(i1)] * frac;
}

void StereoChorus::process(float input, float wetDry, float lfoSine, float& outL, float& outR) {
    const float mix = std::clamp(wetDry, 0.0f, 1.0f);
    buffer_[static_cast<size_t>(writeIndex_)] = input;
    writeIndex_ = (writeIndex_ + 1) % bufferSize_;

    // Base delay 20ms, modulate ±10ms; L/R opposite phase for width.
    const float base = sampleRate_ * 0.020f;
    const float depth = sampleRate_ * 0.010f;
    const float delayL = base + depth * lfoSine;
    const float delayR = base - depth * lfoSine;
    const float wetL = readFrac(delayL);
    const float wetR = readFrac(delayR);

    outL = input * (1.0f - mix) + wetL * mix;
    outR = input * (1.0f - mix) + wetR * mix;
}
