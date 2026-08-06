package com.touchpad.app.ui.components

import android.view.SoundEffectConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
private val PadCorner = 16.dp

/**
 * 2×4 pad grid: press sustains, release starts Decay envelope.
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
                        PadCell(
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
private fun PadCell(
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
    val alpha = intensity.coerceIn(PadCatalog.IDLE_INTENSITY, 1f)

    Box(
        modifier = Modifier
            .size(side)
            .clip(RoundedCornerShape(PadCorner))
            .background(pad.color.copy(alpha = alpha))
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
    )
}
