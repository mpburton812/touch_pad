#include <jni.h>

#include <memory>
#include <mutex>

#include "audio_engine.h"

namespace {
std::unique_ptr<touchpad::AudioEngine> gEngine;
std::mutex gEngineMutex;
}  // namespace

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeCreate(JNIEnv*, jobject) {
    std::lock_guard<std::mutex> lock(gEngineMutex);
    if (!gEngine) {
        gEngine = std::make_unique<touchpad::AudioEngine>();
    }
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeDestroy(JNIEnv*, jobject) {
    std::lock_guard<std::mutex> lock(gEngineMutex);
    if (gEngine) {
        gEngine->stop();
        gEngine.reset();
    }
}

JNIEXPORT jboolean JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeStart(JNIEnv*, jobject) {
    std::lock_guard<std::mutex> lock(gEngineMutex);
    if (!gEngine) {
        return JNI_FALSE;
    }
    return gEngine->start() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeStop(JNIEnv*, jobject) {
    std::lock_guard<std::mutex> lock(gEngineMutex);
    if (gEngine) {
        gEngine->stop();
    }
}

JNIEXPORT void JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeSetPadIntensity(
    JNIEnv*, jobject, jint index, jfloat intensity) {
    if (gEngine) {
        gEngine->setPadIntensity(index, intensity);
    }
}

JNIEXPORT void JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeSetTimbre(JNIEnv*, jobject, jfloat value) {
    if (gEngine) {
        gEngine->setTimbre(value);
    }
}

JNIEXPORT void JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeSetBrightness(JNIEnv*, jobject, jfloat value) {
    if (gEngine) {
        gEngine->setBrightness(value);
    }
}

JNIEXPORT void JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeSetAtmosphere(JNIEnv*, jobject, jfloat value) {
    if (gEngine) {
        gEngine->setAtmosphere(value);
    }
}

JNIEXPORT void JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeSetPulse(JNIEnv*, jobject, jfloat value) {
    if (gEngine) {
        gEngine->setPulse(value);
    }
}

JNIEXPORT void JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeSetDrift(JNIEnv*, jobject, jfloat value) {
    if (gEngine) {
        gEngine->setDrift(value);
    }
}

JNIEXPORT void JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeSetChorus(JNIEnv*, jobject, jfloat value) {
    if (gEngine) {
        gEngine->setChorus(value);
    }
}

JNIEXPORT void JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeSetEcho(JNIEnv*, jobject, jfloat value) {
    if (gEngine) {
        gEngine->setEcho(value);
    }
}

JNIEXPORT void JNICALL
Java_com_touchpad_app_audio_NativeAudioEngine_nativeSetMuted(JNIEnv*, jobject, jboolean muted) {
    if (gEngine) {
        gEngine->setMuted(muted == JNI_TRUE);
    }
}

}  // extern "C"
