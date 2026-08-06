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
    val muted: Boolean = false,
)

/**
 * Asynchronous persistence for slider + mute state via Preferences DataStore.
 *
 * Why DataStore (not SharedPreferences): cursorrules require non-blocking disk
 * I/O managed by coroutines so the UI thread never waits on settings reads/writes.
 */
class SettingsRepository(private val context: Context) {
    private val timbreKey = floatPreferencesKey("timbre")
    private val brightnessKey = floatPreferencesKey("brightness")
    private val atmosphereKey = floatPreferencesKey("atmosphere")
    private val mutedKey = booleanPreferencesKey("muted")

    val settings: Flow<GameSettings> = context.dataStore.data.map { prefs ->
        GameSettings(
            timbre = prefs[timbreKey] ?: 0.0f,
            brightness = prefs[brightnessKey] ?: 0.7f,
            atmosphere = prefs[atmosphereKey] ?: 0.35f,
            muted = prefs[mutedKey] ?: false,
        )
    }

    suspend fun updateTimbre(value: Float) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[timbreKey] = value.coerceIn(0f, 1f) }
    }

    suspend fun updateBrightness(value: Float) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[brightnessKey] = value.coerceIn(0f, 1f) }
    }

    suspend fun updateAtmosphere(value: Float) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[atmosphereKey] = value.coerceIn(0f, 1f) }
    }

    suspend fun updateMuted(muted: Boolean) = withContext(Dispatchers.IO) {
        context.dataStore.edit { it[mutedKey] = muted }
    }
}
