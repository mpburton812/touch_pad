package com.touchpad.app.model

import androidx.compose.ui.graphics.Color

/**
 * Static definition of one pad in the 2×4 calming grid.
 *
 * Colors and frequencies are sourced from `.requirements` so the UI and native
 * oscillator table stay aligned.
 */
data class PadDefinition(
    val index: Int,
    val color: Color,
    val noteName: String,
    val frequencyHz: Float,
)

object PadCatalog {
    val pads: List<PadDefinition> = listOf(
        PadDefinition(0, Color(0xFFFF0000), "C4", 261.63f),
        PadDefinition(1, Color(0xFFFFBF00), "D4", 293.66f),
        PadDefinition(2, Color(0xFF80FF00), "E4", 329.63f),
        PadDefinition(3, Color(0xFF00FF40), "F4", 349.23f),
        PadDefinition(4, Color(0xFF00FFFF), "G4", 392.00f),
        PadDefinition(5, Color(0xFF0040FF), "A4", 440.00f),
        PadDefinition(6, Color(0xFF8000FF), "B4", 493.88f),
        PadDefinition(7, Color(0xFFFF00BF), "C5", 523.25f),
    )

    const val IDLE_INTENSITY = 0.9f
    const val ATTACK_MS = 250
    const val HOLD_MS = 1000
    const val DECAY_MS = 500
}
