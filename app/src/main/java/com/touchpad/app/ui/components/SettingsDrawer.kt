package com.touchpad.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.touchpad.app.R
import com.touchpad.app.ui.TouchPadUiState

/** Drawer panel is 10% transparent so pads remain faintly visible underneath. */
private val DrawerScrim = Color.Black.copy(alpha = 0.9f)

/**
 * Bottom controls drawer that expands over the pad grid.
 */
@Composable
fun SettingsDrawer(
    state: TouchPadUiState,
    onToggle: () -> Unit,
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
    val handleLabel = if (state.drawerOpen) {
        stringResource(R.string.controls_hide)
    } else {
        stringResource(R.string.controls_show)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(DrawerScrim),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .semantics { contentDescription = handleLabel }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.controls),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (state.drawerOpen) {
                    Icons.Filled.KeyboardArrowDown
                } else {
                    Icons.Filled.KeyboardArrowUp
                },
                contentDescription = handleLabel,
                tint = Color.White,
            )
        }

        AnimatedVisibility(
            visible = state.drawerOpen,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            AudioControlSliders(
                state = state,
                onTimbreChange = onTimbreChange,
                onBrightnessChange = onBrightnessChange,
                onAtmosphereChange = onAtmosphereChange,
                onPulseChange = onPulseChange,
                onDriftChange = onDriftChange,
                onChorusChange = onChorusChange,
                onEchoChange = onEchoChange,
                onDecayChange = onDecayChange,
                onTextureChange = onTextureChange,
                onWeightChange = onWeightChange,
                onSwellChange = onSwellChange,
                onShimmerChange = onShimmerChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}
