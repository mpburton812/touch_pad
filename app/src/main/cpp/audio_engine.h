#pragma once

#include <atomic>
#include <cstdint>
#include <memory>
#include <oboe/Oboe.h>

#include "dsp/filter.h"
#include "dsp/reverb.h"
#include "dsp/voice.h"

namespace touchpad {

constexpr int kPadCount = 8;

/**
 * Lock-free native audio engine driven by Oboe.
 *
 * Why: pad intensity and slider parameters are written from the UI/JNI thread
 * while the audio callback runs on a high-priority audio thread. Atomics avoid
 * mutexes inside onAudioReady, which would cause priority inversion and dropouts.
 */
class AudioEngine : public oboe::AudioStreamDataCallback,
                    public oboe::AudioStreamErrorCallback {
public:
    AudioEngine();
    ~AudioEngine() override;

    bool start();
    void stop();

    void setPadIntensity(int index, float intensity);
    void setTimbre(float value);
    void setBrightness(float value);
    void setAtmosphere(float value);
    void setMuted(bool muted);

    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream* audioStream,
        void* audioData,
        int32_t numFrames) override;

    void onErrorAfterClose(oboe::AudioStream* stream, oboe::Result error) override;

private:
    bool openStream();

    std::shared_ptr<oboe::AudioStream> stream_;
    Voice voices_[kPadCount];
    LowPassFilter filter_;
    SimpleReverb reverb_;

    std::atomic<float> padIntensity_[kPadCount]{};
    std::atomic<float> timbre_{0.0f};
    std::atomic<float> brightness_{0.7f};
    std::atomic<float> atmosphere_{0.35f};
    std::atomic<bool> muted_{false};

    float sampleRate_ = 48000.0f;
};

}  // namespace touchpad
