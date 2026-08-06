#include "audio_engine.h"

#include <algorithm>
#include <android/log.h>
#include <cmath>

#define LOG_TAG "TouchPadAudio"
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

namespace touchpad {

namespace {
// C4..C5 frequencies matching .requirements pad table.
constexpr float kFrequencies[kPadCount] = {
    261.63f, 293.66f, 329.63f, 349.23f,
    392.00f, 440.00f, 493.88f, 523.25f,
};

// Headroom coefficient from requirements: prevent clipping when all pads fire.
constexpr float kMasterGain = 0.25f;
}  // namespace

AudioEngine::AudioEngine() {
    for (int i = 0; i < kPadCount; ++i) {
        padIntensity_[i].store(0.0f, std::memory_order_relaxed);
        voices_[i].setFrequency(kFrequencies[i]);
    }
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
        ->setChannelCount(oboe::ChannelCount::Mono)
        ->setDataCallback(this)
        ->setErrorCallback(this);

    oboe::Result result = builder.openStream(stream_);
    if (result != oboe::Result::OK) {
        // Exclusive may be unavailable on some emulators; fall back to shared.
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
    reverb_.setSampleRate(sampleRate_);
    ALOGI("Stream opened sr=%d frames=%d", stream_->getSampleRate(), stream_->getFramesPerBurst());
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
    const float clamped = std::clamp(intensity, 0.0f, 1.0f);
    padIntensity_[index].store(clamped, std::memory_order_relaxed);
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
    const bool muted = muted_.load(std::memory_order_relaxed);

    // Map brightness 0..1 → ~300Hz..3000Hz cutoff.
    const float cutoffHz = 300.0f + brightness * 2700.0f;
    filter_.setCutoffHz(cutoffHz);

    float intensities[kPadCount];
    for (int i = 0; i < kPadCount; ++i) {
        intensities[i] = padIntensity_[i].load(std::memory_order_relaxed);
    }

    for (int32_t frame = 0; frame < numFrames; ++frame) {
        float mixed = 0.0f;
        for (int i = 0; i < kPadCount; ++i) {
            mixed += voices_[i].render(timbre) * intensities[i];
        }
        mixed *= kMasterGain;
        if (muted) {
            mixed = 0.0f;
        }
        mixed = filter_.process(mixed);
        // Reverb after VCA so tails ring while visuals decay to idle alpha.
        mixed = reverb_.process(mixed, atmosphere);
        out[frame] = std::clamp(mixed, -1.0f, 1.0f);
    }
    return oboe::DataCallbackResult::Continue;
}

void AudioEngine::onErrorAfterClose(oboe::AudioStream* /*stream*/, oboe::Result error) {
    ALOGE("Stream error after close: %s", oboe::convertToText(error));
    stream_.reset();
    // Best-effort restart so backgrounding/foregrounding recovers audio.
    start();
}

}  // namespace touchpad
