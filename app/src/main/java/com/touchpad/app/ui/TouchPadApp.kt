package com.touchpad.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touchpad.app.R
import com.touchpad.app.ui.components.NebulaBackground
import com.touchpad.app.ui.components.PadGrid
import com.touchpad.app.ui.components.SettingsDrawer

@Composable
fun TouchPadApp(viewModel: TouchPadViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    MaterialTheme(colorScheme = darkColorScheme()) {
        Box(modifier = Modifier.fillMaxSize()) {
            NebulaBackground()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.CenterStart),
                    )
                    val muteLabel = if (state.muted) {
                        stringResource(R.string.unmute)
                    } else {
                        stringResource(R.string.mute)
                    }
                    IconButton(
                        onClick = viewModel::onMuteToggle,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .semantics { contentDescription = muteLabel },
                    ) {
                        Icon(
                            imageVector = if (state.muted) {
                                Icons.AutoMirrored.Filled.VolumeOff
                            } else {
                                Icons.AutoMirrored.Filled.VolumeUp
                            },
                            contentDescription = muteLabel,
                            tint = Color.White,
                        )
                    }
                }

                // Pads fill remaining space; drawer overlays from the bottom.
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    PadGrid(
                        intensities = state.intensities,
                        onPadPressed = viewModel::onPadPressed,
                        onPadReleased = viewModel::onPadReleased,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                    SettingsDrawer(
                        state = state,
                        onToggle = { viewModel.setDrawerOpen(!state.drawerOpen) },
                        onTimbreChange = viewModel::onTimbreChange,
                        onBrightnessChange = viewModel::onBrightnessChange,
                        onAtmosphereChange = viewModel::onAtmosphereChange,
                        onPulseChange = viewModel::onPulseChange,
                        onDriftChange = viewModel::onDriftChange,
                        onChorusChange = viewModel::onChorusChange,
                        onEchoChange = viewModel::onEchoChange,
                        onDecayChange = viewModel::onDecayChange,
                        onTextureChange = viewModel::onTextureChange,
                        onWeightChange = viewModel::onWeightChange,
                        onSwellChange = viewModel::onSwellChange,
                        onShimmerChange = viewModel::onShimmerChange,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }

            state.updateAvailable?.let { update ->
                AlertDialog(
                    onDismissRequest = {
                        if (!update.critical) viewModel.dismissUpdate()
                    },
                    title = { Text(stringResource(R.string.update_available_title)) },
                    text = {
                        Text(
                            stringResource(
                                R.string.update_available_message,
                                update.versionName,
                            ),
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { uriHandler.openUri(update.apkUrl) }) {
                            Text(stringResource(R.string.update_now))
                        }
                    },
                    dismissButton = {
                        if (!update.critical) {
                            TextButton(onClick = viewModel::dismissUpdate) {
                                Text(stringResource(R.string.update_later))
                            }
                        }
                    },
                )
            }
        }
    }
}
