#pragma once

#include <algorithm>
#include <cmath>
#include <vector>

/**
 * Stereo chorus: fractional delay modulated by an LFO, wet/dry mix.
 *
 * Why before reverb: thickens and widens the dry bus; reverb then washes the
 * stereo image into a space rather than chorusing an already-wet tail.
 */
class StereoChorus {
public:
    void setSampleRate(float sampleRate);

    /**
     * Process one mono sample into stereo outL/outR.
     * @param wetDry 0 = dry mono centered, 1 = full chorus width.
     * @param lfoSine precomputed chorus LFO in [-1,1].
     */
    void process(float input, float wetDry, float lfoSine, float& outL, float& outR);

private:
    float sampleRate_ = 48000.0f;
    std::vector<float> buffer_;
    int writeIndex_ = 0;
    int bufferSize_ = 0;

    float readFrac(float delaySamples) const;
};
