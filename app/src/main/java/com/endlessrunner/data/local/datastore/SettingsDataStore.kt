package com.endlessrunner.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.endlessrunner.domain.model.GraphicsQuality
import com.endlessrunner.domain.model.Orientation
import com.endlessrunner.domain.model.SettingsData
import com.endlessrunner.domain.model.VolumeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore для хранения настроек игры.
 */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "game_settings"
)

/**
 * Ключи Preferences для настроек игры.
 */
object SettingsKeys {
    val MASTER_VOLUME = floatPreferencesKey("master_volume")
    val MUSIC_VOLUME = floatPreferencesKey("music_volume")
    val SFX_VOLUME = floatPreferencesKey("sfx_volume")
    val GRAPHICS_QUALITY = intPreferencesKey("graphics_quality")
    val SHOW_FPS = booleanPreferencesKey("show_fps")
    val PARTICLE_EFFECTS = booleanPreferencesKey("particle_effects")
    val SCREEN_ORIENTATION = intPreferencesKey("screen_orientation")
    val TOUCH_SENSITIVITY = floatPreferencesKey("touch_sensitivity")
}

/**
 * Менеджер настроек игры на основе DataStore.
 * Предоставляет реактивный доступ к настройкам через Flow.
 *
 * @param context Context приложения
 */
class SettingsDataStore(private val context: Context) {

    /**
     * Поток настроек игры.
     * Автоматически обновляется при изменении данных.
     */
    val settingsFlow: Flow<SettingsData> = context.settingsDataStore.data
        .map { preferences ->
            SettingsData(
                masterVolume = preferences[SettingsKeys.MASTER_VOLUME] ?: SettingsData.DEFAULT.masterVolume,
                musicVolume = preferences[SettingsKeys.MUSIC_VOLUME] ?: SettingsData.DEFAULT.musicVolume,
                sfxVolume = preferences[SettingsKeys.SFX_VOLUME] ?: SettingsData.DEFAULT.sfxVolume,
                graphicsQuality = GraphicsQuality.values()
                    .getOrNull(preferences[SettingsKeys.GRAPHICS_QUALITY] ?: GraphicsQuality.MEDIUM.ordinal)
                    ?: GraphicsQuality.MEDIUM,
                showFps = preferences[SettingsKeys.SHOW_FPS] ?: SettingsData.DEFAULT.showFps,
                particleEffects = preferences[SettingsKeys.PARTICLE_EFFECTS] ?: SettingsData.DEFAULT.particleEffects,
                screenOrientation = Orientation.values()
                    .getOrNull(preferences[SettingsKeys.SCREEN_ORIENTATION] ?: Orientation.LANDSCAPE.ordinal)
                    ?: Orientation.LANDSCAPE,
                touchSensitivity = preferences[SettingsKeys.TOUCH_SENSITIVITY] ?: SettingsData.DEFAULT.touchSensitivity
            )
        }

    /**
     * Получение текущих настроек (suspend версия).
     */
    suspend fun getSettings(): SettingsData {
        return context.settingsDataStore.data
            .map { preferences ->
                SettingsData(
                    masterVolume = preferences[SettingsKeys.MASTER_VOLUME] ?: SettingsData.DEFAULT.masterVolume,
                    musicVolume = preferences[SettingsKeys.MUSIC_VOLUME] ?: SettingsData.DEFAULT.musicVolume,
                    sfxVolume = preferences[SettingsKeys.SFX_VOLUME] ?: SettingsData.DEFAULT.sfxVolume,
                    graphicsQuality = GraphicsQuality.values()
                        .getOrNull(preferences[SettingsKeys.GRAPHICS_QUALITY] ?: GraphicsQuality.MEDIUM.ordinal)
                        ?: GraphicsQuality.MEDIUM,
                    showFps = preferences[SettingsKeys.SHOW_FPS] ?: SettingsData.DEFAULT.showFps,
                    particleEffects = preferences[SettingsKeys.PARTICLE_EFFECTS] ?: SettingsData.DEFAULT.particleEffects,
                    screenOrientation = Orientation.values()
                        .getOrNull(preferences[SettingsKeys.SCREEN_ORIENTATION] ?: Orientation.LANDSCAPE.ordinal)
                        ?: Orientation.LANDSCAPE,
                    touchSensitivity = preferences[SettingsKeys.TOUCH_SENSITIVITY] ?: SettingsData.DEFAULT.touchSensitivity
                )
            }
            .first()
    }

    /**
     * Сохранение настроек.
     */
    suspend fun saveSettings(settings: SettingsData) {
        context.settingsDataStore.edit { preferences ->
            preferences[SettingsKeys.MASTER_VOLUME] = settings.masterVolume
            preferences[SettingsKeys.MUSIC_VOLUME] = settings.musicVolume
            preferences[SettingsKeys.SFX_VOLUME] = settings.sfxVolume
            preferences[SettingsKeys.GRAPHICS_QUALITY] = settings.graphicsQuality.ordinal
            preferences[SettingsKeys.SHOW_FPS] = settings.showFps
            preferences[SettingsKeys.PARTICLE_EFFECTS] = settings.particleEffects
            preferences[SettingsKeys.SCREEN_ORIENTATION] = settings.screenOrientation.ordinal
            preferences[SettingsKeys.TOUCH_SENSITIVITY] = settings.touchSensitivity
        }
    }

    /**
     * Обновление громкости.
     */
    suspend fun updateVolume(type: VolumeType, value: Float) {
        require(value in 0f..1f) { "Volume должен быть в диапазоне 0..1" }
        context.settingsDataStore.edit { preferences ->
            when (type) {
                VolumeType.MASTER -> preferences[SettingsKeys.MASTER_VOLUME] = value
                VolumeType.MUSIC -> preferences[SettingsKeys.MUSIC_VOLUME] = value
                VolumeType.SFX -> preferences[SettingsKeys.SFX_VOLUME] = value
            }
        }
    }

    /**
     * Переключение показа FPS.
     */
    suspend fun toggleShowFps() {
        context.settingsDataStore.edit { preferences ->
            val current = preferences[SettingsKeys.SHOW_FPS] ?: SettingsData.DEFAULT.showFps
            preferences[SettingsKeys.SHOW_FPS] = !current
        }
    }

    /**
     * Установка качества графики.
     */
    suspend fun setGraphicsQuality(quality: GraphicsQuality) {
        context.settingsDataStore.edit { preferences ->
            preferences[SettingsKeys.GRAPHICS_QUALITY] = quality.ordinal
        }
    }

    /**
     * Переключение эффектов частиц.
     */
    suspend fun toggleParticleEffects() {
        context.settingsDataStore.edit { preferences ->
            val current = preferences[SettingsKeys.PARTICLE_EFFECTS] ?: SettingsData.DEFAULT.particleEffects
            preferences[SettingsKeys.PARTICLE_EFFECTS] = !current
        }
    }

    /**
     * Установка ориентации экрана.
     */
    suspend fun setScreenOrientation(orientation: Orientation) {
        context.settingsDataStore.edit { preferences ->
            preferences[SettingsKeys.SCREEN_ORIENTATION] = orientation.ordinal
        }
    }

    /**
     * Установка чувствительности касаний.
     */
    suspend fun setTouchSensitivity(sensitivity: Float) {
        require(sensitivity in 0.5f..2.0f) { "Touch sensitivity должен быть в диапазоне 0.5..2.0" }
        context.settingsDataStore.edit { preferences ->
            preferences[SettingsKeys.TOUCH_SENSITIVITY] = sensitivity
        }
    }

    /**
     * Сброс настроек к значениям по умолчанию.
     */
    suspend fun resetToDefault() {
        context.settingsDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Получение отдельной настройки.
     */
    suspend fun getMasterVolume(): Float =
        context.settingsDataStore.data.map { it[SettingsKeys.MASTER_VOLUME] ?: SettingsData.DEFAULT.masterVolume }.first()

    /**
     * Получение настройки музыки.
     */
    suspend fun getMusicVolume(): Float =
        context.settingsDataStore.data.map { it[SettingsKeys.MUSIC_VOLUME] ?: SettingsData.DEFAULT.musicVolume }.first()

    /**
     * Получение настройки SFX.
     */
    suspend fun getSfxVolume(): Float =
        context.settingsDataStore.data.map { it[SettingsKeys.SFX_VOLUME] ?: SettingsData.DEFAULT.sfxVolume }.first()

    /**
     * Проверка, включены ли эффекты частиц.
     */
    suspend fun areParticleEffectsEnabled(): Boolean =
        context.settingsDataStore.data.map { it[SettingsKeys.PARTICLE_EFFECTS] ?: SettingsData.DEFAULT.particleEffects }.first()

    /**
     * Проверка, показывается ли FPS.
     */
    suspend fun isShowFpsEnabled(): Boolean =
        context.settingsDataStore.data.map { it[SettingsKeys.SHOW_FPS] ?: SettingsData.DEFAULT.showFps }.first()
}
