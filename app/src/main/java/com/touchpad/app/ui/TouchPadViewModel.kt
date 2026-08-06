package com.touchpad.app.ui

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touchpad.app.audio.IntensityMapper
import com.touchpad.app.audio.NativeAudioEngine
import com.touchpad.app.data.GameSettings
import com.touchpad.app.data.SettingsRepository
import com.touchpad.app.model.PadCatalog
import com.touchpad.app.update.RemoteVersion
import com.touchpad.app.update.VersionChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

data class TouchPadUiState(
    val intensities: List<Float> = List(PadCatalog.pads.size) { PadCatalog.IDLE_INTENSITY },
    val timbre: Float = 0.0f,
    val brightness: Float = 0.7f,
    val atmosphere: Float = 0.35f,
    val muted: Boolean = false,
    val updateAvailable: RemoteVersion? = null,
)

/**
 * Orchestrates pad envelopes, slider persistence, and JNI parameter pushes.
 *
 * Critical UI state is mirrored into [SavedStateHandle] so process death while
 * backgrounded can rebuild the board without snapping to defaults.
 */
class TouchPadViewModel(
    private val settingsRepository: SettingsRepository,
    private val audioEngine: NativeAudioEngine,
    private val versionChecker: VersionChecker,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TouchPadUiState(
            timbre = savedStateHandle["timbre"] ?: 0.0f,
            brightness = savedStateHandle["brightness"] ?: 0.7f,
            atmosphere = savedStateHandle["atmosphere"] ?: 0.35f,
            muted = savedStateHandle["muted"] ?: false,
        ),
    )
    val uiState: StateFlow<TouchPadUiState> = _uiState.asStateFlow()

    private val envelopeJobs = arrayOfNulls<Job>(PadCatalog.pads.size)

    /** Current visual intensity per pad; used so retriggers lerp from mid-envelope values. */
    private val currentIntensities = FloatArray(PadCatalog.pads.size) { PadCatalog.IDLE_INTENSITY }

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                applySettings(settings, persistHandle = true, pushAudio = true)
            }
        }
        viewModelScope.launch {
            val update = versionChecker.checkForUpdate()
            if (update != null) {
                _uiState.update { it.copy(updateAvailable = update) }
            }
        }
        // Push restored slider values into native atomics immediately.
        pushAllAudioParams()
    }

    fun onPadTapped(index: Int) {
        if (index !in PadCatalog.pads.indices) return
        envelopeJobs[index]?.cancel()
        // Main.immediate: UI StateFlow updates stay on the UI thread without a Compose frame clock.
        envelopeJobs[index] = viewModelScope.launch(Dispatchers.Main.immediate) {
            runEnvelope(index)
        }
    }

    /**
     * Attack → Hold → Decay envelope bound to visual alpha and native VCA.
     *
     * Why wall-clock lerp instead of Compose Animatable: Animatable requires a
     * MonotonicFrameClock that viewModelScope does not provide, which crashed on tap.
     */
    private suspend fun runEnvelope(index: Int) {
        lerpIntensity(index, target = 1.0f, durationMs = PadCatalog.ATTACK_MS)
        delay(PadCatalog.HOLD_MS.toLong())
        lerpIntensity(index, target = PadCatalog.IDLE_INTENSITY, durationMs = PadCatalog.DECAY_MS)
        // Audio amplitude returns to 0 while visual idle alpha stays at IDLE_INTENSITY.
        audioEngine.setPadIntensity(index, 0.0f)
    }

    private suspend fun lerpIntensity(index: Int, target: Float, durationMs: Int) {
        val start = currentIntensities[index]
        if (durationMs <= 0) {
            publishIntensity(index, target)
            return
        }
        val startMs = SystemClock.uptimeMillis()
        while (coroutineContext.isActive) {
            val elapsed = (SystemClock.uptimeMillis() - startMs).toFloat()
            val t = (elapsed / durationMs).coerceIn(0f, 1f)
            publishIntensity(index, start + (target - start) * t)
            if (t >= 1f) break
            delay(FRAME_MS)
        }
    }

    private fun publishIntensity(index: Int, visualAlpha: Float) {
        val clamped = visualAlpha.coerceIn(PadCatalog.IDLE_INTENSITY, 1.0f)
        currentIntensities[index] = clamped
        val audioAmp = IntensityMapper.visualToAudio(clamped, PadCatalog.IDLE_INTENSITY)
        audioEngine.setPadIntensity(index, audioAmp)
        _uiState.update { state ->
            val next = state.intensities.toMutableList()
            next[index] = clamped
            state.copy(intensities = next)
        }
    }

    fun onTimbreChange(value: Float) {
        val v = value.coerceIn(0f, 1f)
        _uiState.update { it.copy(timbre = v) }
        savedStateHandle["timbre"] = v
        audioEngine.setTimbre(v)
        viewModelScope.launch { settingsRepository.updateTimbre(v) }
    }

    fun onBrightnessChange(value: Float) {
        val v = value.coerceIn(0f, 1f)
        _uiState.update { it.copy(brightness = v) }
        savedStateHandle["brightness"] = v
        audioEngine.setBrightness(v)
        viewModelScope.launch { settingsRepository.updateBrightness(v) }
    }

    fun onAtmosphereChange(value: Float) {
        val v = value.coerceIn(0f, 1f)
        _uiState.update { it.copy(atmosphere = v) }
        savedStateHandle["atmosphere"] = v
        audioEngine.setAtmosphere(v)
        viewModelScope.launch { settingsRepository.updateAtmosphere(v) }
    }

    fun onMuteToggle() {
        val muted = !_uiState.value.muted
        _uiState.update { it.copy(muted = muted) }
        savedStateHandle["muted"] = muted
        audioEngine.setMuted(muted)
        viewModelScope.launch { settingsRepository.updateMuted(muted) }
    }

    fun dismissUpdate() {
        _uiState.update { it.copy(updateAvailable = null) }
    }

    fun startAudio() {
        audioEngine.start()
        pushAllAudioParams()
    }

    fun stopAudio() {
        audioEngine.stop()
    }

    override fun onCleared() {
        envelopeJobs.forEach { it?.cancel() }
        audioEngine.stop()
        audioEngine.destroy()
        super.onCleared()
    }

    private fun applySettings(
        settings: GameSettings,
        persistHandle: Boolean,
        pushAudio: Boolean,
    ) {
        _uiState.update {
            it.copy(
                timbre = settings.timbre,
                brightness = settings.brightness,
                atmosphere = settings.atmosphere,
                muted = settings.muted,
            )
        }
        if (persistHandle) {
            savedStateHandle["timbre"] = settings.timbre
            savedStateHandle["brightness"] = settings.brightness
            savedStateHandle["atmosphere"] = settings.atmosphere
            savedStateHandle["muted"] = settings.muted
        }
        if (pushAudio) {
            audioEngine.setTimbre(settings.timbre)
            audioEngine.setBrightness(settings.brightness)
            audioEngine.setAtmosphere(settings.atmosphere)
            audioEngine.setMuted(settings.muted)
        }
    }

    private fun pushAllAudioParams() {
        val state = _uiState.value
        audioEngine.setTimbre(state.timbre)
        audioEngine.setBrightness(state.brightness)
        audioEngine.setAtmosphere(state.atmosphere)
        audioEngine.setMuted(state.muted)
    }

    companion object {
        private const val FRAME_MS = 16L
    }
}
