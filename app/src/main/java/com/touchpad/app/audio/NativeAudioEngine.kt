package com.touchpad.app.audio

/**
 * JNI façade over the native Oboe DSP engine.
 *
 * Why a thin Kotlin wrapper: Compose/ViewModel stay free of native pointer
 * management while still binding stream lifecycle to Activity resume/pause.
 */
class NativeAudioEngine {
    init {
        System.loadLibrary("touchpad_audio")
        nativeCreate()
    }

    fun start(): Boolean = nativeStart()

    fun stop() {
        nativeStop()
    }

    fun destroy() {
        nativeDestroy()
    }

    fun setPadIntensity(index: Int, intensity: Float) {
        nativeSetPadIntensity(index, intensity)
    }

    fun setTimbre(value: Float) {
        nativeSetTimbre(value)
    }

    fun setBrightness(value: Float) {
        nativeSetBrightness(value)
    }

    fun setAtmosphere(value: Float) {
        nativeSetAtmosphere(value)
    }

    fun setMuted(muted: Boolean) {
        nativeSetMuted(muted)
    }

    private external fun nativeCreate(): Boolean
    private external fun nativeDestroy()
    private external fun nativeStart(): Boolean
    private external fun nativeStop()
    private external fun nativeSetPadIntensity(index: Int, intensity: Float)
    private external fun nativeSetTimbre(value: Float)
    private external fun nativeSetBrightness(value: Float)
    private external fun nativeSetAtmosphere(value: Float)
    private external fun nativeSetMuted(muted: Boolean)
}
