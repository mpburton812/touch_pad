package com.touchpad.app.ui.components

import android.view.SoundEffectConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.touchpad.app.R
import com.touchpad.app.model.PadCatalog
import com.touchpad.app.model.PadDefinition

private val PadGap = 12.dp

/**
 * 2×4 pad grid of translucent top-down cubes; press sustains, release decays.
 */
@Composable
fun PadGrid(
    intensities: List<Float>,
    onPadPressed: (Int) -> Unit,
    onPadReleased: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val cols = 2
        val rows = 4
        val gap = PadGap
        val side = min(
            (maxWidth - gap * (cols - 1)) / cols,
            (maxHeight - gap * (rows - 1)) / rows,
        ).coerceAtLeast(0.dp)

        Column(
            verticalArrangement = Arrangement.spacedBy(gap),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            for (row in 0 until rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    for (col in 0 until cols) {
                        val index = row * cols + col
                        PadCubeCell(
                            pad = PadCatalog.pads[index],
                            intensity = intensities.getOrElse(index) { PadCatalog.IDLE_INTENSITY },
                            side = side,
                            onPressed = { onPadPressed(index) },
                            onReleased = { onPadReleased(index) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PadCubeCell(
    pad: PadDefinition,
    intensity: Float,
    side: Dp,
    onPressed: () -> Unit,
    onReleased: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val view = LocalView.current
    val description = stringResource(
        R.string.pad_content_description,
        pad.index + 1,
        pad.noteName,
    )
    val luminosity = intensity.coerceIn(PadCatalog.IDLE_INTENSITY, 1f)

    Box(
        modifier = Modifier
            .size(side)
            .semantics { contentDescription = description }
            .pointerInput(pad.index) {
                detectTapGestures(
                    onPress = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onPressed()
                        try {
                            tryAwaitRelease()
                        } finally {
                            onReleased()
                        }
                    },
                )
            },
    ) {
        TranslucentTopDownCube(
            color = pad.color,
            luminosity = luminosity,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Translucent cube viewed from above: rounded silhouette, extruded right + bottom faces.
 */
@Composable
private fun TranslucentTopDownCube(
    color: Color,
    luminosity: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val depthX = w * 0.14f
        val depthY = h * 0.14f
        val topW = w - depthX
        val topH = h - depthY
        val r = (minOf(topW, topH) * 0.12f).coerceAtLeast(2f)
        val corner = CornerRadius(r, r)

        val outer = Path().apply {
            addRoundRect(RoundRect(0f, 0f, w, h, corner))
        }
        val topFace = Path().apply {
            addRoundRect(RoundRect(0f, 0f, topW, topH, corner))
        }
        val bottomFace = Path().apply {
            moveTo(0f, topH)
            lineTo(topW, topH)
            lineTo(w, h)
            lineTo(depthX, h)
            close()
        }
        val rightFace = Path().apply {
            moveTo(topW, 0f)
            lineTo(w, depthY)
            lineTo(w, h)
            lineTo(topW, topH)
            close()
        }

        val topAlpha = luminosity
        val sideAlpha = luminosity * 0.55f
        val bottomAlpha = luminosity * 0.4f

        clipPath(outer) {
            drawPath(bottomFace, color = color.copy(alpha = bottomAlpha))
            drawPath(
                rightFace,
                color = Color(
                    red = (color.red * 0.55f).coerceIn(0f, 1f),
                    green = (color.green * 0.55f).coerceIn(0f, 1f),
                    blue = (color.blue * 0.55f).coerceIn(0f, 1f),
                    alpha = sideAlpha,
                ),
            )
        }
        drawPath(topFace, color = color.copy(alpha = topAlpha))
        drawPath(
            path = topFace,
            color = Color.White.copy(alpha = 0.18f * luminosity),
            style = Stroke(width = size.minDimension * 0.02f),
        )
        clipPath(topFace) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f * luminosity),
                        Color.Transparent,
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(topW * 0.55f, topH * 0.45f),
                ),
            )
        }
    }
}
