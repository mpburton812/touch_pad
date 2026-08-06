#include "dsp/reverb.h"

#include <algorithm>

void SimpleReverb::setSampleRate(float sampleRate) {
    sampleRate_ = sampleRate > 1.0f ? sampleRate : 48000.0f;
    const float delaysMs[kCombCount] = {29.7f, 37.1f, 41.3f, 43.7f};
    for (int i = 0; i < kCombCount; ++i) {
        const int samples = static_cast<int>(delaysMs[i] * 0.001f * sampleRate_);
        delays_[i] = std::clamp(samples, 64, kMaxDelay - 1);
        writeIndex_[i] = 0;
        buffersL_[static_cast<size_t>(i)].fill(0.0f);
        buffersR_[static_cast<size_t>(i)].fill(0.0f);
    }
}

void SimpleReverb::process(float inL, float inR, float wetDry, float& outL, float& outR) {
    const float mix = std::clamp(wetDry, 0.0f, 1.0f);
    float wetL = 0.0f;
    float wetR = 0.0f;
    for (int i = 0; i < kCombCount; ++i) {
        auto& bufL = buffersL_[static_cast<size_t>(i)];
        auto& bufR = buffersR_[static_cast<size_t>(i)];
        const int delay = delays_[static_cast<size_t>(i)];
        int readIndex = writeIndex_[static_cast<size_t>(i)] - delay;
        if (readIndex < 0) {
            readIndex += kMaxDelay;
        }
        const float delayedL = bufL[static_cast<size_t>(readIndex)];
        const float delayedR = bufR[static_cast<size_t>(readIndex)];
        const float fb = feedback_[static_cast<size_t>(i)];
        bufL[static_cast<size_t>(writeIndex_[static_cast<size_t>(i)])] = inL + delayedL * fb;
        bufR[static_cast<size_t>(writeIndex_[static_cast<size_t>(i)])] = inR + delayedR * fb;
        writeIndex_[static_cast<size_t>(i)] = (writeIndex_[static_cast<size_t>(i)] + 1) % kMaxDelay;
        wetL += delayedL;
        wetR += delayedR;
    }
    wetL *= (1.0f / static_cast<float>(kCombCount));
    wetR *= (1.0f / static_cast<float>(kCombCount));
    outL = inL * (1.0f - mix) + wetL * mix;
    outR = inR * (1.0f - mix) + wetR * mix;
}
