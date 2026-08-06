#pragma once

#include <array>
#include <cstdint>

/**
 * Lightweight stereo reverb (dual comb banks) for Atmosphere after Echo.
 */
class SimpleReverb {
public:
    void setSampleRate(float sampleRate);
    void process(float inL, float inR, float wetDry, float& outL, float& outR);

private:
    static constexpr int kCombCount = 4;
    static constexpr int kMaxDelay = 8192;

    float sampleRate_ = 48000.0f;
    std::array<std::array<float, kMaxDelay>, kCombCount> buffersL_{};
    std::array<std::array<float, kMaxDelay>, kCombCount> buffersR_{};
    std::array<int, kCombCount> writeIndex_{};
    std::array<int, kCombCount> delays_{};
    std::array<float, kCombCount> feedback_{0.75f, 0.72f, 0.70f, 0.68f};
};
