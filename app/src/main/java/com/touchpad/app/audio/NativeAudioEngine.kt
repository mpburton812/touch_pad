package com.touchpad.app.audio

/**
 * JNI façade over the native stereo Oboe DSP engine.
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

    fun setTimbre(value: Float) = nativeSetTimbre(value)
    fun setBrightness(value: Float) = nativeSetBrightness(value)
    fun setAtmosphere(value: Float) = nativeSetAtmosphere(value)
    fun setPulse(value: Float) = nativeSetPulse(value)
    fun setDrift(value: Float) = nativeSetDrift(value)
    fun setChorus(value: Float) = nativeSetChorus(value)
    fun setEcho(value: Float) = nativeSetEcho(value)
    fun setTexture(value: Float) = nativeSetTexture(value)
    fun setWeight(value: Float) = nativeSetWeight(value)
    fun setSwell(value: Float) = nativeSetSwell(value)
    fun setShimmer(value: Float) = nativeSetShimmer(value)
    fun setMuted(muted: Boolean) = nativeSetMuted(muted)

    private external fun nativeCreate(): Boolean
    private external fun nativeDestroy()
    private external fun nativeStart(): Boolean
    private external fun nativeStop()
    private external fun nativeSetPadIntensity(index: Int, intensity: Float)
    private external fun nativeSetTimbre(value: Float)
    private external fun nativeSetBrightness(value: Float)
    private external fun nativeSetAtmosphere(value: Float)
    private external fun nativeSetPulse(value: Float)
    private external fun nativeSetDrift(value: Float)
    private external fun nativeSetChorus(value: Float)
    private external fun nativeSetEcho(value: Float)
    private external fun nativeSetTexture(value: Float)
    private external fun nativeSetWeight(value: Float)
    private external fun nativeSetSwell(value: Float)
    private external fun nativeSetShimmer(value: Float)
    private external fun nativeSetMuted(muted: Boolean)
}
