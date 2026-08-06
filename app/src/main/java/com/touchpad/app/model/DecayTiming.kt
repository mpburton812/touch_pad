package com.touchpad.app.model

/**
 * Maps Decay slider 0..1 to release duration in milliseconds.
 * 0 → 80ms (quick), 1 → 4000ms (long wash).
 */
object DecayTiming {
    const val MIN_MS = 80
    const val MAX_MS = 4000

    fun sliderToMs(slider: Float): Int {
        val t = slider.coerceIn(0f, 1f)
        return (MIN_MS + (MAX_MS - MIN_MS) * t).toInt()
    }
}
