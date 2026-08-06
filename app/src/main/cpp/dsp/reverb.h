#pragma once

#include <array>
#include <cstdint>

/**
 * Lightweight feedback delay network style reverb for spatial Atmosphere.
 *
 * Why after VCA: requirements require reverb tails to continue after pad visuals
 * decay. How: comb delays + wet/dry mix controlled by atmosphere 0..1.
 */
class SimpleReverb {
public:
    void setSampleRate(float sampleRate);
    float process(float input, float wetDry);

private:
    static constexpr int kCombCount = 4;
    static constexpr int kMaxDelay = 8192;

    float sampleRate_ = 48000.0f;
    std::array<std::array<float, kMaxDelay>, kCombCount> buffers_{};
    std::array<int, kCombCount> writeIndex_{};
    std::array<int, kCombCount> delays_{};
    std::array<float, kCombCount> feedback_{0.75f, 0.72f, 0.70f, 0.68f};
};
