package com.endlessrunner.domain.model

/**
 * Качество графики.
 */
enum class GraphicsQuality {
    LOW,      // Минимальные настройки для слабых устройств
    MEDIUM,   // Средние настройки
    HIGH      // Максимальное качество
}

/**
 * Ориентация экрана.
 */
enum class Orientation {
    PORTRAIT,  // Портретная
    LANDSCAPE  // Ландшафтная
}

/**
 * Тип громкости для настройки.
 */
enum class VolumeType {
    MASTER,  // Общая громкость
    MUSIC,   // Громкость музыки
    SFX      // Громкость звуковых эффектов
}

/**
 * Настройки игры.
 * Содержит все пользовательские настройки.
 *
 * @property masterVolume Общая громкость (0..1)
 * @property musicVolume Громкость музыки (0..1)
 * @property sfxVolume Громкость звуковых эффектов (0..1)
 * @property graphicsQuality Качество графики
 * @property showFps Показывать FPS счётчик
 * @property particleEffects Эффекты частиц включены
 * @property screenOrientation Ориентация экрана
 * @property touchSensitivity Чувствительность касаний (0.5..2.0)
 */
data class SettingsData(
    val masterVolume: Float = 1.0f,
    val musicVolume: Float = 0.8f,
    val sfxVolume: Float = 0.8f,
    val graphicsQuality: GraphicsQuality = GraphicsQuality.MEDIUM,
    val showFps: Boolean = false,
    val particleEffects: Boolean = true,
    val screenOrientation: Orientation = Orientation.LANDSCAPE,
    val touchSensitivity: Float = 1.0f
) {
    init {
        require(masterVolume in 0f..1f) { "masterVolume должен быть в диапазоне 0..1" }
        require(musicVolume in 0f..1f) { "musicVolume должен быть в диапазоне 0..1" }
        require(sfxVolume in 0f..1f) { "sfxVolume должен быть в диапазоне 0..1" }
        require(touchSensitivity in 0.5f..2.0f) { "touchSensitivity должен быть в диапазоне 0.5..2.0" }
    }

    /**
     * Эффективная громкость музыки (с учётом master).
     */
    val effectiveMusicVolume: Float
        get() = masterVolume * musicVolume

    /**
     * Эффективная громкость SFX (с учётом master).
     */
    val effectiveSfxVolume: Float
        get() = masterVolume * sfxVolume

    /**
     * Копия настроек с изменённой громкостью.
     */
    fun withVolume(type: VolumeType, value: Float): SettingsData {
        require(value in 0f..1f) { "volume должен быть в диапазоне 0..1" }
        return when (type) {
            VolumeType.MASTER -> copy(masterVolume = value)
            VolumeType.MUSIC -> copy(musicVolume = value)
            VolumeType.SFX -> copy(sfxVolume = value)
        }
    }

    companion object {
        /**
         * Настройки по умолчанию.
         */
        val DEFAULT = SettingsData()

        /**
         * Настройки для слабых устройств.
         */
        val LOW_END = SettingsData(
            graphicsQuality = GraphicsQuality.LOW,
            particleEffects = false,
            showFps = true
        )

        /**
         * Настройки для мощных устройств.
         */
        val HIGH_END = SettingsData(
            graphicsQuality = GraphicsQuality.HIGH,
            particleEffects = true,
            showFps = true,
            masterVolume = 1.0f,
            musicVolume = 1.0f,
            sfxVolume = 1.0f
        )
    }
}
