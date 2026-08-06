#pragma once

#include <algorithm>
#include <vector>

/**
 * Fixed-time tape-style delay with feedback; stereo-capable (linked L/R).
 *
 * Output feeds the reverb so echoes cascade into the Atmosphere wash.
 */
class StereoEcho {
public:
    void setSampleRate(float sampleRate);

    /**
     * @param amount 0..1 controls both wet mix and feedback strength.
     */
    void process(float inL, float inR, float amount, float& outL, float& outR);

private:
    float sampleRate_ = 48000.0f;
    std::vector<float> bufferL_;
    std::vector<float> bufferR_;
    int writeIndex_ = 0;
    int delaySamples_ = 1;
};
