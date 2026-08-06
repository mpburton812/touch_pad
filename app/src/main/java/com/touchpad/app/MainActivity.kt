package com.touchpad.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.touchpad.app.ui.TouchPadApp
import com.touchpad.app.ui.TouchPadViewModel
import com.touchpad.app.ui.TouchPadViewModelFactory
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

/**
 * Single-activity host for the calming pad experience.
 *
 * Oboe stream lifecycle is bound to resumed state so backgrounding always
 * releases the exclusive audio stream.
 */
class MainActivity : ComponentActivity() {
    private val viewModel: TouchPadViewModel by viewModels {
        TouchPadViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.startAudio()
                try {
                    awaitCancellation()
                } finally {
                    viewModel.stopAudio()
                }
            }
        }

        setContent {
            TouchPadApp(viewModel = viewModel)
        }
    }
}
