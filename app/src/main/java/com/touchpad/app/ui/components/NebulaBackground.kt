package com.touchpad.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Full-bleed multi-hued nebula with a smooth, slightly faster spin.
 */
@Composable
fun NebulaBackground(modifier: Modifier = Modifier) {
    var phase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            // ~1.75× prior step (0.004) — still smooth at ~30fps.
            phase = (phase + 0.007f) % (Math.PI.toFloat() * 2f)
            delay(32L)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(Color(0xFF070B1A))

        // Subtle whole-field spin so motion reads as rotation, not only drift.
        rotate(degrees = phase * (180f / Math.PI.toFloat()) * 0.15f) {
            val centers = listOf(
                Offset(w * (0.35f + 0.08f * cos(phase)), h * (0.30f + 0.06f * sin(phase))),
                Offset(w * (0.70f + 0.07f * sin(phase * 0.8f)), h * (0.55f + 0.05f * cos(phase))),
                Offset(w * (0.45f + 0.05f * cos(phase * 1.3f)), h * (0.75f + 0.04f * sin(phase * 1.1f))),
            )
            val colors = listOf(
                Color(0xAA4B1FFF),
                Color(0x887B2DFF),
                Color(0x66FF2D95),
                Color(0x5500C2FF),
            )

            centers.forEachIndexed { i, center ->
                val radius = w * (0.45f + 0.1f * i)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(colors[i % colors.size], Color.Transparent),
                        center = center,
                        radius = radius,
                    ),
                    radius = radius,
                    center = center,
                )
            }
        }
    }
}
