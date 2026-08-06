package com.touchpad.app.audio

/**
 * Maps visual pad alpha to native VCA amplitude.
 *
 * Idle visual floor is 0.2 while audio must be silent at rest; peak (1.0) maps
 * to full amplitude so envelopes stay perceptually locked.
 */
object IntensityMapper {
    fun visualToAudio(visualAlpha: Float, idle: Float = 0.9f): Float {
        return ((visualAlpha - idle) / (1.0f - idle)).coerceIn(0f, 1f)
    }
}
