package com.touchpad.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.touchpad.app.R
import com.touchpad.app.ui.TouchPadUiState
import kotlin.math.roundToInt

private val SliderHeight = 36.dp
private val ThumbWidth = 4.dp
private val ActiveTrack = Color(0xFF7C4DFF)
private val InactiveTrack = Color.White.copy(alpha = 0.25f)
private val LightLabel = Color.White
private val DarkLabel = Color(0xFF1A1028)

/**
 * Full synth control bank used inside the bottom drawer.
 */
@Composable
fun AudioControlSliders(
    state: TouchPadUiState,
    onTimbreChange: (Float) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onAtmosphereChange: (Float) -> Unit,
    onPulseChange: (Float) -> Unit,
    onDriftChange: (Float) -> Unit,
    onChorusChange: (Float) -> Unit,
    onEchoChange: (Float) -> Unit,
    onDecayChange: (Float) -> Unit,
    onTextureChange: (Float) -> Unit,
    onWeightChange: (Float) -> Unit,
    onSwellChange: (Float) -> Unit,
    onShimmerChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        InBarSlider(stringResource(R.string.slider_timbre), state.timbre, onTimbreChange)
        Spacer(Modifier.height(8.dp))
        InBarSlider(stringResource(R.string.slider_brightness), state.brightness, onBrightnessChange)
        Spacer(Modifier.height(8.dp))
        InBarSlider(stringResource(R.string.slider_atmosphere), state.atmosphere, onAtmosphereChange)
        Spacer(Modifier.height(8.dp))
        InBarSlider(stringResource(R.string.slider_pulse), state.pulse, onPulseChange)
        Spacer(Modifier.height(8.dp))
        InBarSlider(stringResource(R.string.slider_drift), state.drift, onDriftChange)
        Spacer(Modifier.height(8.dp))
        InBarSlider(stringResource(R.string.slider_chorus), state.chorus, onChorusChange)
        Spacer(Modifier.height(8.dp))
        InBarSlider(stringResource(R.string.slider_echo), state.echo, onEchoChange)
        Spacer(Modifier.height(8.dp))
        InBarSlider(stringResource(R.string.slider_decay), state.decay, onDecayChange)
        Spacer(Modifier.height(8.dp))
        InBarSlider(stringResource(R.string.slider_texture), state.texture, onTextureChange)
        Spacer(Modifier.height(8.dp))
        InBarSlider(stringResource(R.string.slider_weight), state.weight, onWeightChange)
        Spacer(Modifier.height(8.dp))
        InBarSlider(stringResource(R.string.slider_swell), state.swell, onSwellChange)
        Spacer(Modifier.height(8.dp))
        InBarSlider(stringResource(R.string.slider_shimmer), state.shimmer, onShimmerChange)
    }
}

@Composable
private fun InBarSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    val density = LocalDensity.current
    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    val shape = RoundedCornerShape(10.dp)
    val clamped = value.coerceIn(0f, 1f)
    val thumbPx = with(density) { ThumbWidth.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(SliderHeight)
            .clip(shape)
            .background(InactiveTrack)
            .onSizeChanged { trackWidthPx = it.width.toFloat() }
            .semantics { contentDescription = label }
            .pointerInput(trackWidthPx) {
                if (trackWidthPx <= 0f) return@pointerInput
                detectTapGestures { offset ->
                    onValueChange((offset.x / trackWidthPx).coerceIn(0f, 1f))
                }
            }
            .pointerInput(trackWidthPx) {
                if (trackWidthPx <= 0f) return@pointerInput
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    onValueChange((change.position.x / trackWidthPx).coerceIn(0f, 1f))
                }
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(clamped)
                .background(ActiveTrack),
        )
        val thumbOffsetPx = ((trackWidthPx * clamped) - thumbPx / 2f)
            .coerceIn(0f, (trackWidthPx - thumbPx).coerceAtLeast(0f))
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset(thumbOffsetPx.roundToInt(), 0) }
                .width(ThumbWidth)
                .fillMaxHeight()
                .background(Color.White),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = DarkLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(clamped)
                        .clip(shape),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = LightLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.align(Alignment.CenterStart),
                    )
                }
            }
        }
    }
}
