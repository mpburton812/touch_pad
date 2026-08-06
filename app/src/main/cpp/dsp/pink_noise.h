#pragma once

#include <cstdint>

/**
 * Continuous pink-noise bed (Paul Kellet refined filter on white noise).
 *
 * Why not through VCA: Texture is an environmental floor that should not
 * follow pad envelopes — it sits under the mix before the master LPF.
 */
class PinkNoise {
public:
    float next(float amplitude) {
        // xorshift32 white noise
        state_ ^= state_ << 13;
        state_ ^= state_ >> 17;
        state_ ^= state_ << 5;
        const float white = (static_cast<int32_t>(state_) / 2147483648.0f);

        b0_ = 0.99886f * b0_ + white * 0.0555179f;
        b1_ = 0.99332f * b1_ + white * 0.0750759f;
        b2_ = 0.96900f * b2_ + white * 0.1538520f;
        b3_ = 0.86650f * b3_ + white * 0.3104856f;
        b4_ = 0.55000f * b4_ + white * 0.5329522f;
        b5_ = -0.7616f * b5_ - white * 0.0168980f;
        const float pink = b0_ + b1_ + b2_ + b3_ + b4_ + b5_ + b6_ + white * 0.5362f;
        b6_ = white * 0.115926f;
        return pink * 0.11f * amplitude;  // scale to sensible floor level
    }

private:
    uint32_t state_ = 22222u;
    float b0_ = 0, b1_ = 0, b2_ = 0, b3_ = 0, b4_ = 0, b5_ = 0, b6_ = 0;
};
