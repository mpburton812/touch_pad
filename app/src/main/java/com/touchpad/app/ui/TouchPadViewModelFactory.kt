package com.touchpad.app.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.touchpad.app.TouchPadApplication
import com.touchpad.app.audio.NativeAudioEngine
import com.touchpad.app.data.SettingsRepository
import com.touchpad.app.update.VersionChecker

/**
 * Builds [TouchPadViewModel] with app-scoped collaborators and a SavedStateHandle.
 */
class TouchPadViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        require(modelClass.isAssignableFrom(TouchPadViewModel::class.java))
        val app = application as TouchPadApplication
        return TouchPadViewModel(
            settingsRepository = SettingsRepository(app),
            audioEngine = NativeAudioEngine(),
            versionChecker = VersionChecker(),
            savedStateHandle = extras.createSavedStateHandle(),
        ) as T
    }
}
