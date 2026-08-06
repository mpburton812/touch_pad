#include <jni.h>

#include <memory>
#include <mutex>

#include "audio_engine.h"

namespace {
std::unique_ptr<touchpad::AudioEngine> gEngine;
// Protects only engine pointer lifetime (create/destroy/start/stop), never the audio callback.
std::mutex gEngineMutex;
}  // namespace

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeCreate(JNIEnv* /*env*/, jobject /*thiz*/) {
    std::lock_guard<std::mutex> lock(gEngineMutex);
    if (!gEngine) {
        gEngine = std::make_unique<touchpad::AudioEngine>();
    }
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeDestroy(JNIEnv* /*env*/, jobject /*thiz*/) {
    std::lock_guard<std::mutex> lock(gEngineMutex);
    if (gEngine) {
        gEngine->stop();
        gEngine.reset();
    }
}

JNIEXPORT jboolean JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeStart(JNIEnv* /*env*/, jobject /*thiz*/) {
    std::lock_guard<std::mutex> lock(gEngineMutex);
    if (!gEngine) {
        return JNI_FALSE;
    }
    return gEngine->start() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeStop(JNIEnv* /*env*/, jobject /*thiz*/) {
    std::lock_guard<std::mutex> lock(gEngineMutex);
    if (gEngine) {
        gEngine->stop();
    }
}

JNIEXPORT void JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeSetPadIntensity(
    JNIEnv* /*env*/,
    jobject /*thiz*/,
    jint index,
    jfloat intensity) {
    // Intentionally unlocked: AudioEngine stores intensities as atomics.
    if (gEngine) {
        gEngine->setPadIntensity(index, intensity);
    }
}

JNIEXPORT void JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeSetTimbre(
    JNIEnv* /*env*/,
    jobject /*thiz*/,
    jfloat value) {
    if (gEngine) {
        gEngine->setTimbre(value);
    }
}

JNIEXPORT void JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeSetBrightness(
    JNIEnv* /*env*/,
    jobject /*thiz*/,
    jfloat value) {
    if (gEngine) {
        gEngine->setBrightness(value);
    }
}

JNIEXPORT void JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeSetAtmosphere(
    JNIEnv* /*env*/,
    jobject /*thiz*/,
    jfloat value) {
    if (gEngine) {
        gEngine->setAtmosphere(value);
    }
}

JNIEXPORT void JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeSetMuted(
    JNIEnv* /*env*/,
    jobject /*thiz*/,
    jboolean muted) {
    if (gEngine) {
        gEngine->setMuted(muted == JNI_TRUE);
    }
}

}  // extern "C"
