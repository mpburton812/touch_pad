#include "dsp/reverb.h"

#include <algorithm>
#include <cmath>

void SimpleReverb::setSampleRate(float sampleRate) {
    sampleRate_ = sampleRate > 1.0f ? sampleRate : 48000.0f;
    // Prime-ish delay lengths in ms for a dense but cheap wash.
    const float delaysMs[kCombCount] = {29.7f, 37.1f, 41.3f, 43.7f};
    for (int i = 0; i < kCombCount; ++i) {
        const int samples = static_cast<int>(delaysMs[i] * 0.001f * sampleRate_);
        delays_[i] = std::clamp(samples, 64, kMaxDelay - 1);
        writeIndex_[i] = 0;
        buffers_[i].fill(0.0f);
    }
}

float SimpleReverb::process(float input, float wetDry) {
    const float mix = std::clamp(wetDry, 0.0f, 1.0f);
    float wet = 0.0f;
    for (int i = 0; i < kCombCount; ++i) {
        auto& buffer = buffers_[i];
        const int delay = delays_[i];
        int readIndex = writeIndex_[i] - delay;
        if (readIndex < 0) {
            readIndex += kMaxDelay;
        }
        const float delayed = buffer[static_cast<size_t>(readIndex)];
        const float stored = input + delayed * feedback_[static_cast<size_t>(i)];
        buffer[static_cast<size_t>(writeIndex_[i])] = stored;
        writeIndex_[i] = (writeIndex_[i] + 1) % kMaxDelay;
        wet += delayed;
    }
    wet *= (1.0f / static_cast<float>(kCombCount));
    return input * (1.0f - mix) + wet * mix;
}
