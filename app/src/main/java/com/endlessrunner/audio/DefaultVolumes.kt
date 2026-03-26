package com.endlessrunner.audio

/**
 * Data class громкостей по умолчанию.
 *
 * Определяет начальные значения громкости для различных каналов.
 * Все значения должны быть в диапазоне от 0.0 до 1.0.
 *
 * @param master Общая громкость
 * @param music Громкость музыки
 * @param sfx Громкость звуковых эффектов
 * @param ambience Громкость фоновых звуков
 * @param ui Громкость UI звуков
 */
data class DefaultVolumes(
    val master: Float = 1.0f,
    val music: Float = AudioConstants.DEFAULT_MUSIC_VOLUME,
    val sfx: Float = AudioConstants.DEFAULT_SFX_VOLUME,
    val ambience: Float = AudioConstants.DEFAULT_AMBIENCE_VOLUME,
    val ui: Float = 0.6f
) {
    init {
        validateVolume(master, "master")
        validateVolume(music, "music")
        validateVolume(sfx, "sfx")
        validateVolume(ambience, "ambience")
        validateVolume(ui, "ui")
    }

    /**
     * Проверка валидности громкости.
     */
    private fun validateVolume(volume: Float, name: String) {
        require(volume in AudioConstants.MIN_VOLUME..AudioConstants.MAX_VOLUME) {
            "$name volume must be between ${AudioConstants.MIN_VOLUME} and ${AudioConstants.MAX_VOLUME}, was $volume"
        }
    }

    /**
     * Получить громкость для канала.
     *
     * @param channel Канал
     * @return Громкость канала
     */
    fun getVolumeForChannel(channel: AudioChannel): Float = when (channel) {
        AudioChannel.MASTER -> master
        AudioChannel.MUSIC -> music
        AudioChannel.SFX -> sfx
        AudioChannel.AMBIENCE -> ambience
        AudioChannel.UI -> ui
    }

    /**
     * Создать копию с изменёнными параметрами.
     */
    fun copy(
        master: Float = this.master,
        music: Float = this.music,
        sfx: Float = this.sfx,
        ambience: Float = this.ambience,
        ui: Float = this.ui
    ): DefaultVolumes = DefaultVolumes(
        master = master,
        music = music,
        sfx = sfx,
        ambience = ambience,
        ui = ui
    )

    /**
     * Применить множитель ко всем громкостям.
     *
     * @param multiplier Множитель (0.0 - 1.0)
     */
    fun applyMultiplier(multiplier: Float): DefaultVolumes {
        val clampedMultiplier = multiplier.coerceIn(0.0f, 1.0f)
        return copy(
            master = (master * clampedMultiplier).coerceIn(AudioConstants.MIN_VOLUME, AudioConstants.MAX_VOLUME),
            music = (music * clampedMultiplier).coerceIn(AudioConstants.MIN_VOLUME, AudioConstants.MAX_VOLUME),
            sfx = (sfx * clampedMultiplier).coerceIn(AudioConstants.MIN_VOLUME, AudioConstants.MAX_VOLUME),
            ambience = (ambience * clampedMultiplier).coerceIn(AudioConstants.MIN_VOLUME, AudioConstants.MAX_VOLUME),
            ui = (ui * clampedMultiplier).coerceIn(AudioConstants.MIN_VOLUME, AudioConstants.MAX_VOLUME)
        )
    }

    /**
     * Сбросить все громкости к максимальным.
     */
    fun resetToMax(): DefaultVolumes = DefaultVolumes(
        master = 1.0f,
        music = 1.0f,
        sfx = 1.0f,
        ambience = 1.0f,
        ui = 1.0f
    )

    /**
     * Сбросить все громкости к значениям по умолчанию.
     */
    fun resetToDefault(): DefaultVolumes = DefaultVolumes()

    /**
     * Конфигурация для тихого режима.
     */
    fun toQuiet(): DefaultVolumes = copy(
        master = 0.5f,
        music = 0.3f,
        sfx = 0.4f,
        ambience = 0.2f,
        ui = 0.3f
    )

    /**
     * Конфигурация для громкого режима.
     */
    fun toLoud(): DefaultVolumes = copy(
        master = 1.0f,
        music = 0.9f,
        sfx = 1.0f,
        ambience = 0.7f,
        ui = 0.8f
    )

    /**
     * Преобразовать в Map.
     */
    fun toMap(): Map<String, Float> = mapOf(
        "master" to master,
        "music" to music,
        "sfx" to sfx,
        "ambience" to ambience,
        "ui" to ui
    )

    companion object {
        /**
         * Значения по умолчанию.
         */
        val DEFAULT = DefaultVolumes()

        /**
         * Тихие значения.
         */
        val QUIET = DefaultVolumes().toQuiet()

        /**
         * Громкие значения.
         */
        val LOUD = DefaultVolumes().toLoud()

        /**
         * Только музыка.
         */
        val MUSIC_ONLY = DefaultVolumes(
            master = 1.0f,
            music = 0.8f,
            sfx = 0.0f,
            ambience = 0.0f,
            ui = 0.0f
        )

        /**
         * Только звуки.
         */
        val SFX_ONLY = DefaultVolumes(
            master = 1.0f,
            music = 0.0f,
            sfx = 0.8f,
            ambience = 0.0f,
            ui = 0.6f
        )

        /**
         * Полная тишина.
         */
        val SILENT = DefaultVolumes(
            master = 0.0f,
            music = 0.0f,
            sfx = 0.0f,
            ambience = 0.0f,
            ui = 0.0f
        )

        /**
         * Создать из Map.
         */
        fun fromMap(map: Map<String, Float>): DefaultVolumes {
            return DefaultVolumes(
                master = map["master"] ?: 1.0f,
                music = map["music"] ?: AudioConstants.DEFAULT_MUSIC_VOLUME,
                sfx = map["sfx"] ?: AudioConstants.DEFAULT_SFX_VOLUME,
                ambience = map["ambience"] ?: AudioConstants.DEFAULT_AMBIENCE_VOLUME,
                ui = map["ui"] ?: 0.6f
            )
        }
    }
}
