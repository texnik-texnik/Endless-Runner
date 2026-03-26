package com.endlessrunner.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.endlessrunner.audio.AudioManager
import com.endlessrunner.audio.AudioChannel
import com.endlessrunner.audio.AudioMixer
import com.endlessrunner.data.local.datastore.SettingsDataStore
import com.endlessrunner.domain.model.GraphicsQuality
import com.endlessrunner.domain.model.Orientation
import com.endlessrunner.domain.model.SettingsData
import com.endlessrunner.domain.model.VolumeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Настройки звука.
 */
data class SoundSettings(
    val masterVolume: Float = 0.8f,
    val musicVolume: Float = 0.7f,
    val sfxVolume: Float = 0.9f,
    val ambienceVolume: Float = 0.5f,
    val uiVolume: Float = 0.6f,
    val isMuted: Boolean = false
)

/**
 * Настройки графики.
 */
data class GraphicsSettings(
    val quality: GraphicsQuality = GraphicsQuality.MEDIUM,
    val showFps: Boolean = false,
    val particleEffects: Boolean = true,
    val antiAliasing: Boolean = false,
    val shadows: Boolean = true
)

/**
 * Настройки геймплея.
 */
data class GameplaySettings(
    val orientation: Orientation = Orientation.LANDSCAPE,
    val touchSensitivity: Float = 1.0f,
    val vibrationEnabled: Boolean = true,
    val showTutorial: Boolean = true,
    val autoJump: Boolean = false
)

/**
 * Полные настройки игры.
 */
data class GameSettings(
    val sound: SoundSettings = SoundSettings(),
    val graphics: GraphicsSettings = GraphicsSettings(),
    val gameplay: GameplaySettings = GameplaySettings()
)

/**
 * Состояние экрана настроек.
 */
data class SettingsState(
    val settings: GameSettings = GameSettings(),
    val hasUnsavedChanges: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)

/**
 * ViewModel для экрана настроек.
 * Интегрирована с SettingsDataStore и AudioManager.
 */
class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val context: Context
) : ViewModel() {

    /**
     * AudioManager для применения настроек звука.
     */
    private val audioManager: AudioManager by lazy {
        AudioManager.getInstance(context)
    }

    /**
     * AudioMixer для управления каналами.
     */
    private val audioMixer: AudioMixer by lazy {
        AudioMixer.getInstance()
    }

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
        observeSettings()
        observeAudioManager()
    }

    /**
     * Наблюдение за изменениями AudioManager.
     */
    private fun observeAudioManager() {
        viewModelScope.launch {
            audioManager.masterVolumeFlow.collect { volume ->
                // Обновление состояния если изменилось извне
                val currentSettings = _state.value.settings.sound
                if (currentSettings.masterVolume != volume) {
                    _state.value = _state.value.copy(
                        settings = _state.value.settings.copy(
                            sound = currentSettings.copy(masterVolume = volume)
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            audioManager.musicVolumeFlow.collect { volume ->
                val currentSettings = _state.value.settings.sound
                if (currentSettings.musicVolume != volume) {
                    _state.value = _state.value.copy(
                        settings = _state.value.settings.copy(
                            sound = currentSettings.copy(musicVolume = volume)
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            audioManager.sfxVolumeFlow.collect { volume ->
                val currentSettings = _state.value.settings.sound
                if (currentSettings.sfxVolume != volume) {
                    _state.value = _state.value.copy(
                        settings = _state.value.settings.copy(
                            sound = currentSettings.copy(sfxVolume = volume)
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            audioManager.isMutedFlow.collect { isMuted ->
                val currentSettings = _state.value.settings.sound
                if (currentSettings.isMuted != isMuted) {
                    _state.value = _state.value.copy(
                        settings = _state.value.settings.copy(
                            sound = currentSettings.copy(isMuted = isMuted)
                        )
                    )
                }
            }
        }
    }

    /**
     * Наблюдение за изменениями настроек.
     */
    private fun observeSettings() {
        viewModelScope.launch {
            settingsDataStore.settingsFlow.collect { settings ->
                _state.value = _state.value.copy(
                    settings = settings.toGameSettings(),
                    hasUnsavedChanges = false
                )
            }
        }
    }

    /**
     * Загрузка настроек.
     */
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val settings = settingsDataStore.getSettings()
                _state.value = _state.value.copy(
                    settings = settings.toGameSettings(),
                    hasUnsavedChanges = false
                )
            } catch (e: Exception) {
                // Использовать настройки по умолчанию
                _state.value = _state.value.copy(
                    settings = GameSettings()
                )
            }
        }
    }

    /**
     * Обновление настроек звука.
     */
    fun updateSoundSettings(sound: SoundSettings) {
        _state.value = _state.value.copy(
            settings = _state.value.settings.copy(sound = sound),
            hasUnsavedChanges = true
        )

        // Применение настроек к AudioManager
        applySoundSettings(sound)
    }

    /**
     * Применение настроек звука к AudioManager.
     */
    private fun applySoundSettings(sound: SoundSettings) {
        if (sound.isMuted) {
            audioManager.mute()
        } else {
            audioManager.unmute()
            audioManager.setMasterVolume(sound.masterVolume)
        }
        audioManager.setMusicVolume(sound.musicVolume)
        audioManager.setSfxVolume(sound.sfxVolume)

        // Применение настроек каналов через AudioMixer
        audioMixer.setChannelVolume(AudioChannel.AMBIENCE, sound.ambienceVolume)
        audioMixer.setChannelVolume(AudioChannel.UI, sound.uiVolume)
    }

    /**
     * Обновление громкости.
     */
    fun updateVolume(type: VolumeType, value: Float) {
        viewModelScope.launch {
            settingsDataStore.updateVolume(type, value)
        }

        // Применение к AudioManager
        when (type) {
            VolumeType.MASTER -> audioManager.setMasterVolume(value)
            VolumeType.MUSIC -> audioManager.setMusicVolume(value)
            VolumeType.SFX -> audioManager.setSfxVolume(value)
        }
    }

    /**
     * Обновление громкости master.
     */
    fun setMasterVolume(value: Float) {
        val currentSettings = _state.value.settings.sound
        val newSettings = currentSettings.copy(masterVolume = value)
        updateSoundSettings(newSettings)
    }

    /**
     * Обновление громкости музыки.
     */
    fun setMusicVolume(value: Float) {
        val currentSettings = _state.value.settings.sound
        val newSettings = currentSettings.copy(musicVolume = value)
        updateSoundSettings(newSettings)
    }

    /**
     * Обновление громкости SFX.
     */
    fun setSfxVolume(value: Float) {
        val currentSettings = _state.value.settings.sound
        val newSettings = currentSettings.copy(sfxVolume = value)
        updateSoundSettings(newSettings)
    }

    /**
     * Переключение mute.
     */
    fun toggleMute() {
        val currentSettings = _state.value.settings.sound
        val newSettings = currentSettings.copy(isMuted = !currentSettings.isMuted)
        updateSoundSettings(newSettings)
    }

    /**
     * Обновление настроек графики.
     */
    fun updateGraphicsSettings(graphics: GraphicsSettings) {
        _state.value = _state.value.copy(
            settings = _state.value.settings.copy(graphics = graphics),
            hasUnsavedChanges = true
        )
    }

    /**
     * Обновление настроек геймплея.
     */
    fun updateGameplaySettings(gameplay: GameplaySettings) {
        _state.value = _state.value.copy(
            settings = _state.value.settings.copy(gameplay = gameplay),
            hasUnsavedChanges = true
        )
    }

    /**
     * Сохранение настроек.
     */
    fun saveSettings() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)

            try {
                val domainSettings = _state.value.settings.toDomainSettings()
                settingsDataStore.saveSettings(domainSettings)

                _state.value = _state.value.copy(
                    isSaving = false,
                    isSaved = true,
                    hasUnsavedChanges = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false)
            }
        }
    }

    /**
     * Сброс к настройкам по умолчанию.
     */
    fun resetToDefault() {
        viewModelScope.launch {
            settingsDataStore.resetToDefault()
        }
    }

    /**
     * Отмена изменений.
     */
    fun discardChanges() {
        loadSettings()
    }

    /**
     * Применение изменений.
     */
    fun applyChanges() {
        saveSettings()
    }

    /**
     * Переключение показа FPS.
     */
    fun toggleShowFps() {
        viewModelScope.launch {
            settingsDataStore.toggleShowFps()
        }
    }

    /**
     * Переключение эффектов частиц.
     */
    fun toggleParticleEffects() {
        viewModelScope.launch {
            settingsDataStore.toggleParticleEffects()
        }
    }

    /**
     * Установка качества графики.
     */
    fun setGraphicsQuality(quality: GraphicsQuality) {
        viewModelScope.launch {
            settingsDataStore.setGraphicsQuality(quality)
        }
    }

    /**
     * Установка ориентации экрана.
     */
    fun setScreenOrientation(orientation: Orientation) {
        viewModelScope.launch {
            settingsDataStore.setScreenOrientation(orientation)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Автосохранение при выходе если есть изменения
        if (_state.value.hasUnsavedChanges) {
            saveSettings()
        }
    }
}

/**
 * Extension функции для конвертации между типами.
 */
private fun SettingsData.toGameSettings(): GameSettings {
    return GameSettings(
        sound = SoundSettings(
            masterVolume = masterVolume,
            musicVolume = musicVolume,
            sfxVolume = sfxVolume,
            ambienceVolume = 0.5f,
            uiVolume = 0.6f,
            isMuted = masterVolume == 0f
        ),
        graphics = GraphicsSettings(
            quality = graphicsQuality,
            showFps = showFps,
            particleEffects = particleEffects,
            antiAliasing = graphicsQuality == GraphicsQuality.HIGH,
            shadows = graphicsQuality != GraphicsQuality.LOW
        ),
        gameplay = GameplaySettings(
            orientation = screenOrientation,
            touchSensitivity = touchSensitivity,
            vibrationEnabled = true,
            showTutorial = true,
            autoJump = false
        )
    )
}

private fun GameSettings.toDomainSettings(): SettingsData {
    return SettingsData(
        masterVolume = sound.masterVolume,
        musicVolume = sound.musicVolume,
        sfxVolume = sound.sfxVolume,
        graphicsQuality = graphics.quality,
        showFps = graphics.showFps,
        particleEffects = graphics.particleEffects,
        screenOrientation = gameplay.orientation,
        touchSensitivity = gameplay.touchSensitivity
    )
}
