package com.endlessrunner.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val MASTER_VOLUME = floatPreferencesKey("master_volume")
        private val MUSIC_VOLUME = floatPreferencesKey("music_volume")
        private val SFX_VOLUME = floatPreferencesKey("sfx_volume")
        private val SHOW_FPS = booleanPreferencesKey("show_fps")
        private val PARTICLE_EFFECTS = booleanPreferencesKey("particle_effects")
    }

    val masterVolume: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[MASTER_VOLUME] ?: 1.0f
    }

    val musicVolume: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[MUSIC_VOLUME] ?: 0.7f
    }

    val sfxVolume: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[SFX_VOLUME] ?: 0.8f
    }

    suspend fun setMasterVolume(volume: Float) {
        context.dataStore.edit { it[MASTER_VOLUME] = volume }
    }

    suspend fun setMusicVolume(volume: Float) {
        context.dataStore.edit { it[MUSIC_VOLUME] = volume }
    }

    suspend fun setSfxVolume(volume: Float) {
        context.dataStore.edit { it[SFX_VOLUME] = volume }
    }

    suspend fun setShowFps(show: Boolean) {
        context.dataStore.edit { it[SHOW_FPS] = show }
    }

    suspend fun setParticleEffects(enabled: Boolean) {
        context.dataStore.edit { it[PARTICLE_EFFECTS] = enabled }
    }
}
