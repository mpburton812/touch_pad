package com.touchpad.app.ui

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touchpad.app.audio.IntensityMapper
import com.touchpad.app.audio.NativeAudioEngine
import com.touchpad.app.data.GameSettings
import com.touchpad.app.data.SettingsRepository
import com.touchpad.app.model.DecayTiming
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
    val pulse: Float = 0.0f,
    val drift: Float = 0.0f,
    val chorus: Float = 0.0f,
    val echo: Float = 0.0f,
    val decay: Float = 0.35f,
    val muted: Boolean = false,
    val drawerOpen: Boolean = false,
    val updateAvailable: RemoteVersion? = null,
)

/**
 * Press-and-hold pad envelopes, slider persistence, and JNI parameter pushes.
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
            pulse = savedStateHandle["pulse"] ?: 0.0f,
            drift = savedStateHandle["drift"] ?: 0.0f,
            chorus = savedStateHandle["chorus"] ?: 0.0f,
            echo = savedStateHandle["echo"] ?: 0.0f,
            decay = savedStateHandle["decay"] ?: 0.35f,
            muted = savedStateHandle["muted"] ?: false,
            drawerOpen = savedStateHandle["drawerOpen"] ?: false,
        ),
    )
    val uiState: StateFlow<TouchPadUiState> = _uiState.asStateFlow()

    private val envelopeJobs = arrayOfNulls<Job>(PadCatalog.pads.size)
    private val pressed = BooleanArray(PadCatalog.pads.size) { false }
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
        pushAllAudioParams()
    }

    fun onPadPressed(index: Int) {
        if (index !in PadCatalog.pads.indices) return
        pressed[index] = true
        envelopeJobs[index]?.cancel()
        envelopeJobs[index] = viewModelScope.launch(Dispatchers.Main.immediate) {
            lerpIntensity(index, target = 1.0f, durationMs = PadCatalog.ATTACK_MS)
            // Sustain while held — intensity stays at peak; native VCA follows.
            while (coroutineContext.isActive && pressed[index]) {
                publishIntensity(index, 1.0f)
                delay(FRAME_MS)
            }
        }
    }

    fun onPadReleased(index: Int) {
        if (index !in PadCatalog.pads.indices) return
        pressed[index] = false
        envelopeJobs[index]?.cancel()
        val decayMs = DecayTiming.sliderToMs(_uiState.value.decay)
        envelopeJobs[index] = viewModelScope.launch(Dispatchers.Main.immediate) {
            lerpIntensity(index, target = PadCatalog.IDLE_INTENSITY, durationMs = decayMs)
            audioEngine.setPadIntensity(index, 0.0f)
        }
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
        audioEngine.setPadIntensity(
            index,
            IntensityMapper.visualToAudio(clamped, PadCatalog.IDLE_INTENSITY),
        )
        _uiState.update { state ->
            val next = state.intensities.toMutableList()
            next[index] = clamped
            state.copy(intensities = next)
        }
    }

    fun setDrawerOpen(open: Boolean) {
        _uiState.update { it.copy(drawerOpen = open) }
        savedStateHandle["drawerOpen"] = open
    }

    fun onTimbreChange(v: Float) = updateFloat("timbre", v, { copy(timbre = it) }, audioEngine::setTimbre) {
        settingsRepository.updateTimbre(it)
    }

    fun onBrightnessChange(v: Float) =
        updateFloat("brightness", v, { copy(brightness = it) }, audioEngine::setBrightness) {
            settingsRepository.updateBrightness(it)
        }

    fun onAtmosphereChange(v: Float) =
        updateFloat("atmosphere", v, { copy(atmosphere = it) }, audioEngine::setAtmosphere) {
            settingsRepository.updateAtmosphere(it)
        }

    fun onPulseChange(v: Float) =
        updateFloat("pulse", v, { copy(pulse = it) }, audioEngine::setPulse) {
            settingsRepository.updatePulse(it)
        }

    fun onDriftChange(v: Float) =
        updateFloat("drift", v, { copy(drift = it) }, audioEngine::setDrift) {
            settingsRepository.updateDrift(it)
        }

    fun onChorusChange(v: Float) =
        updateFloat("chorus", v, { copy(chorus = it) }, audioEngine::setChorus) {
            settingsRepository.updateChorus(it)
        }

    fun onEchoChange(v: Float) =
        updateFloat("echo", v, { copy(echo = it) }, audioEngine::setEcho) {
            settingsRepository.updateEcho(it)
        }

    fun onDecayChange(v: Float) =
        updateFloat("decay", v, { copy(decay = it) }, { _: Float -> }) {
            settingsRepository.updateDecay(it)
        }

    private fun updateFloat(
        key: String,
        value: Float,
        copyState: TouchPadUiState.(Float) -> TouchPadUiState,
        pushNative: (Float) -> Unit,
        persist: suspend (Float) -> Unit,
    ) {
        val v = value.coerceIn(0f, 1f)
        _uiState.update { it.copyState(v) }
        savedStateHandle[key] = v
        pushNative(v)
        viewModelScope.launch { persist(v) }
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

    private fun applySettings(settings: GameSettings, persistHandle: Boolean, pushAudio: Boolean) {
        _uiState.update {
            it.copy(
                timbre = settings.timbre,
                brightness = settings.brightness,
                atmosphere = settings.atmosphere,
                pulse = settings.pulse,
                drift = settings.drift,
                chorus = settings.chorus,
                echo = settings.echo,
                decay = settings.decay,
                muted = settings.muted,
            )
        }
        if (persistHandle) {
            savedStateHandle["timbre"] = settings.timbre
            savedStateHandle["brightness"] = settings.brightness
            savedStateHandle["atmosphere"] = settings.atmosphere
            savedStateHandle["pulse"] = settings.pulse
            savedStateHandle["drift"] = settings.drift
            savedStateHandle["chorus"] = settings.chorus
            savedStateHandle["echo"] = settings.echo
            savedStateHandle["decay"] = settings.decay
            savedStateHandle["muted"] = settings.muted
        }
        if (pushAudio) {
            pushSettingsToNative(settings)
        }
    }

    private fun pushSettingsToNative(settings: GameSettings) {
        audioEngine.setTimbre(settings.timbre)
        audioEngine.setBrightness(settings.brightness)
        audioEngine.setAtmosphere(settings.atmosphere)
        audioEngine.setPulse(settings.pulse)
        audioEngine.setDrift(settings.drift)
        audioEngine.setChorus(settings.chorus)
        audioEngine.setEcho(settings.echo)
        audioEngine.setMuted(settings.muted)
    }

    private fun pushAllAudioParams() {
        val s = _uiState.value
        audioEngine.setTimbre(s.timbre)
        audioEngine.setBrightness(s.brightness)
        audioEngine.setAtmosphere(s.atmosphere)
        audioEngine.setPulse(s.pulse)
        audioEngine.setDrift(s.drift)
        audioEngine.setChorus(s.chorus)
        audioEngine.setEcho(s.echo)
        audioEngine.setMuted(s.muted)
    }

    companion object {
        private const val FRAME_MS = 16L
    }
}
