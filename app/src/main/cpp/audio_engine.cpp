#include "audio_engine.h"

#include <algorithm>
#include <android/log.h>
#include <cmath>

#define LOG_TAG "TouchPadAudio"
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

namespace touchpad {

namespace {
constexpr float kFrequencies[kPadCount] = {
    261.63f, 293.66f, 329.63f, 349.23f,
    392.00f, 440.00f, 493.88f, 523.25f,
};
constexpr float kMasterGain = 0.25f;
// One semitone down as a frequency ratio (2^(-1/12)).
constexpr float kSemitoneDown = 0.943874f;
}  // namespace

AudioEngine::AudioEngine() {
    for (int i = 0; i < kPadCount; ++i) {
        padIntensity_[i].store(0.0f, std::memory_order_relaxed);
        voices_[i].setFrequency(kFrequencies[i]);
    }
    pulseLfo_.setFrequencyHz(0.35f);
    driftLfoA_.setFrequencyHz(0.3f);
    driftLfoB_.setFrequencyHz(0.7f);
    chorusLfo_.setFrequencyHz(1.0f);
}

AudioEngine::~AudioEngine() {
    stop();
}

bool AudioEngine::openStream() {
    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Output)
        ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
        ->setSharingMode(oboe::SharingMode::Exclusive)
        ->setFormat(oboe::AudioFormat::Float)
        ->setChannelCount(oboe::ChannelCount::Stereo)
        ->setDataCallback(static_cast<oboe::AudioStreamDataCallback*>(this))
        ->setErrorCallback(static_cast<oboe::AudioStreamErrorCallback*>(this));

    oboe::Result result = builder.openStream(stream_);
    if (result != oboe::Result::OK) {
        ALOGE("Exclusive open failed (%s); retrying Shared", oboe::convertToText(result));
        builder.setSharingMode(oboe::SharingMode::Shared);
        result = builder.openStream(stream_);
        if (result != oboe::Result::OK) {
            ALOGE("Failed to open stream: %s", oboe::convertToText(result));
            return false;
        }
    }

    sampleRate_ = static_cast<float>(stream_->getSampleRate());
    for (auto& voice : voices_) {
        voice.setSampleRate(sampleRate_);
    }
    filter_.setSampleRate(sampleRate_);
    chorus_.setSampleRate(sampleRate_);
    echoEffect_.setSampleRate(sampleRate_);
    reverb_.setSampleRate(sampleRate_);
    pulseLfo_.setSampleRate(sampleRate_);
    driftLfoA_.setSampleRate(sampleRate_);
    driftLfoB_.setSampleRate(sampleRate_);
    chorusLfo_.setSampleRate(sampleRate_);
    ALOGI("Stereo stream opened sr=%d", stream_->getSampleRate());
    return true;
}

bool AudioEngine::start() {
    if (stream_ && stream_->getState() == oboe::StreamState::Started) {
        return true;
    }
    stop();
    if (!openStream()) {
        return false;
    }
    const auto result = stream_->requestStart();
    if (result != oboe::Result::OK) {
        ALOGE("requestStart failed: %s", oboe::convertToText(result));
        return false;
    }
    return true;
}

void AudioEngine::stop() {
    if (!stream_) {
        return;
    }
    stream_->requestStop();
    stream_->close();
    stream_.reset();
}

void AudioEngine::setPadIntensity(int index, float intensity) {
    if (index < 0 || index >= kPadCount) {
        return;
    }
    padIntensity_[index].store(std::clamp(intensity, 0.0f, 1.0f), std::memory_order_relaxed);
}

void AudioEngine::setTimbre(float value) {
    timbre_.store(std::clamp(value, 0.0f, 1.0f), std::memory_order_relaxed);
}
void AudioEngine::setBrightness(float value) {
    brightness_.store(std::clamp(value, 0.0f, 1.0f), std::memory_order_relaxed);
}
void AudioEngine::setAtmosphere(float value) {
    atmosphere_.store(std::clamp(value, 0.0f, 1.0f), std::memory_order_relaxed);
}
void AudioEngine::setPulse(float value) {
    pulse_.store(std::clamp(value, 0.0f, 1.0f), std::memory_order_relaxed);
}
void AudioEngine::setDrift(float value) {
    drift_.store(std::clamp(value, 0.0f, 1.0f), std::memory_order_relaxed);
}
void AudioEngine::setChorus(float value) {
    chorusMix_.store(std::clamp(value, 0.0f, 1.0f), std::memory_order_relaxed);
}
void AudioEngine::setEcho(float value) {
    echoMix_.store(std::clamp(value, 0.0f, 1.0f), std::memory_order_relaxed);
}
void AudioEngine::setTexture(float value) {
    texture_.store(std::clamp(value, 0.0f, 1.0f), std::memory_order_relaxed);
}
void AudioEngine::setWeight(float value) {
    weight_.store(std::clamp(value, 0.0f, 1.0f), std::memory_order_relaxed);
}
void AudioEngine::setSwell(float value) {
    swell_.store(std::clamp(value, 0.0f, 1.0f), std::memory_order_relaxed);
}
void AudioEngine::setShimmer(float value) {
    shimmer_.store(std::clamp(value, 0.0f, 1.0f), std::memory_order_relaxed);
}
void AudioEngine::setMuted(bool muted) {
    muted_.store(muted, std::memory_order_relaxed);
}

oboe::DataCallbackResult AudioEngine::onAudioReady(
    oboe::AudioStream* /*audioStream*/,
    void* audioData,
    int32_t numFrames) {
    auto* out = static_cast<float*>(audioData);
    const float timbre = timbre_.load(std::memory_order_relaxed);
    const float brightness = brightness_.load(std::memory_order_relaxed);
    const float atmosphere = atmosphere_.load(std::memory_order_relaxed);
    const float pulse = pulse_.load(std::memory_order_relaxed);
    const float driftAmt = drift_.load(std::memory_order_relaxed);
    const float chorusAmt = chorusMix_.load(std::memory_order_relaxed);
    const float echoAmt = echoMix_.load(std::memory_order_relaxed);
    const float texture = texture_.load(std::memory_order_relaxed);
    const float weight = weight_.load(std::memory_order_relaxed);
    const float swell = swell_.load(std::memory_order_relaxed);
    const float shimmer = shimmer_.load(std::memory_order_relaxed);
    const bool muted = muted_.load(std::memory_order_relaxed);

    const float baseCutoff = 300.0f + brightness * 2700.0f;

    float intensities[kPadCount];
    for (int i = 0; i < kPadCount; ++i) {
        intensities[i] = padIntensity_[i].load(std::memory_order_relaxed);
    }

    for (int32_t frame = 0; frame < numFrames; ++frame) {
        const float pulseSine = pulseLfo_.next();
        const float driftSine =
            0.5f * driftLfoA_.next() + 0.5f * driftLfoB_.next();
        const float chorusSine = chorusLfo_.next();
        const float driftMult = 1.0f + driftSine * driftAmt * 0.015f;

        float mixed = 0.0f;
        float shimmerBus = 0.0f;
        for (int i = 0; i < kPadCount; ++i) {
            const float n = intensities[i];
            // Swell: at full depth, start ~1 semitone flat when intensity is 0.
            const float swellPitch =
                1.0f - swell * (1.0f - n) * (1.0f - kSemitoneDown);
            const VoiceSample sample = voices_[i].render(
                timbre, driftMult, weight, shimmer, swellPitch);
            mixed += sample.dry * n;
            shimmerBus += sample.shimmerSend * n;
        }

        mixed *= kMasterGain;
        shimmerBus *= kMasterGain;

        // Texture pink noise: continuous, not VCA'd — before LPF.
        if (texture > 0.0001f) {
            mixed += pinkNoise_.next(texture) * kMasterGain;
        }

        if (muted) {
            mixed = 0.0f;
            shimmerBus = 0.0f;
        }

        const float oct = pulseSine * pulse * 2.0f;
        const float cutoff = baseCutoff * std::pow(2.0f, oct);
        filter_.setCutoffHz(cutoff);
        mixed = filter_.process(mixed);

        float left = mixed;
        float right = mixed;
        chorus_.process(mixed, chorusAmt, chorusSine, left, right);

        float echoL = left;
        float echoR = right;
        echoEffect_.process(left, right, echoAmt, echoL, echoR);

        // Shimmer injects exclusively into the reverb input.
        const float revInL = echoL + shimmerBus;
        const float revInR = echoR + shimmerBus;
        float revL = revInL;
        float revR = revInR;
        reverb_.process(revInL, revInR, atmosphere, revL, revR);

        out[frame * 2] = std::clamp(revL, -1.0f, 1.0f);
        out[frame * 2 + 1] = std::clamp(revR, -1.0f, 1.0f);
    }
    return oboe::DataCallbackResult::Continue;
}

void AudioEngine::onErrorAfterClose(oboe::AudioStream* /*stream*/, oboe::Result error) {
    ALOGE("Stream error after close: %s", oboe::convertToText(error));
    stream_.reset();
    start();
}

}  // namespace touchpad
