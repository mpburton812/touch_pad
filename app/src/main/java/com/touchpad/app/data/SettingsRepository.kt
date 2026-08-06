package com.touchpad.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "touch_pad_settings")

data class GameSettings(
    val timbre: Float = 0.0f,
    val brightness: Float = 0.7f,
    val atmosphere: Float = 0.35f,
    val pulse: Float = 0.0f,
    val drift: Float = 0.0f,
    val chorus: Float = 0.0f,
    val echo: Float = 0.0f,
    val decay: Float = 0.35f,
    val muted: Boolean = false,
)

/**
 * Asynchronous persistence for slider + mute state via Preferences DataStore.
 */
class SettingsRepository(private val context: Context) {
    private val timbreKey = floatPreferencesKey("timbre")
    private val brightnessKey = floatPreferencesKey("brightness")
    private val atmosphereKey = floatPreferencesKey("atmosphere")
    private val pulseKey = floatPreferencesKey("pulse")
    private val driftKey = floatPreferencesKey("drift")
    private val chorusKey = floatPreferencesKey("chorus")
    private val echoKey = floatPreferencesKey("echo")
    private val decayKey = floatPreferencesKey("decay")
    private val mutedKey = booleanPreferencesKey("muted")

    val settings: Flow<GameSettings> = context.dataStore.data.map { prefs ->
        GameSettings(
            timbre = prefs[timbreKey] ?: 0.0f,
            brightness = prefs[brightnessKey] ?: 0.7f,
            atmosphere = prefs[atmosphereKey] ?: 0.35f,
            pulse = prefs[pulseKey] ?: 0.0f,
            drift = prefs[driftKey] ?: 0.0f,
            chorus = prefs[chorusKey] ?: 0.0f,
            echo = prefs[echoKey] ?: 0.0f,
            decay = prefs[decayKey] ?: 0.35f,
            muted = prefs[mutedKey] ?: false,
        )
    }

    private suspend fun putFloat(key: Preferences.Key<Float>, value: Float) =
        withContext(Dispatchers.IO) {
            context.dataStore.edit { it[key] = value.coerceIn(0f, 1f) }
        }

    suspend fun updateTimbre(value: Float) = putFloat(timbreKey, value)
    suspend fun updateBrightness(value: Float) = putFloat(brightnessKey, value)
    suspend fun updateAtmosphere(value: Float) = putFloat(atmosphereKey, value)
    suspend fun updatePulse(value: Float) = putFloat(pulseKey, value)
    suspend fun updateDrift(value: Float) = putFloat(driftKey, value)
    suspend fun updateChorus(value: Float) = putFloat(chorusKey, value)
    suspend fun updateEcho(value: Float) = putFloat(echoKey, value)
    suspend fun updateDecay(value: Float) = putFloat(decayKey, value)

    suspend fun updateMuted(muted: Boolean) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[mutedKey] = muted }
    }
}
