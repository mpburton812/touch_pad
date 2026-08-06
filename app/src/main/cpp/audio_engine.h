#pragma once

#include <atomic>
#include <cstdint>
#include <memory>
#include <oboe/Oboe.h>

#include "dsp/chorus.h"
#include "dsp/echo.h"
#include "dsp/filter.h"
#include "dsp/lfo.h"
#include "dsp/reverb.h"
#include "dsp/voice.h"

namespace touchpad {

constexpr int kPadCount = 8;

/**
 * Lock-free stereo Oboe engine: voices → VCA → LPF(Pulse) → Chorus → Echo → Reverb.
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
    void setPulse(float value);
    void setDrift(float value);
    void setChorus(float value);
    void setEcho(float value);
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
    StereoChorus chorus_;
    StereoEcho echoEffect_;
    SimpleReverb reverb_;

    SineLfo pulseLfo_;
    SineLfo driftLfoA_;
    SineLfo driftLfoB_;
    SineLfo chorusLfo_;

    std::atomic<float> padIntensity_[kPadCount]{};
    std::atomic<float> timbre_{0.0f};
    std::atomic<float> brightness_{0.7f};
    std::atomic<float> atmosphere_{0.35f};
    std::atomic<float> pulse_{0.0f};
    std::atomic<float> drift_{0.0f};
    std::atomic<float> chorusMix_{0.0f};
    std::atomic<float> echoMix_{0.0f};
    std::atomic<bool> muted_{false};

    float sampleRate_ = 48000.0f;
};

}  // namespace touchpad
