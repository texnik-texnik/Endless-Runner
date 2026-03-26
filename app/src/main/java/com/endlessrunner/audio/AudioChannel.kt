package com.endlessrunner.audio

/**
 * Enum каналов аудио микшера.
 *
 * Определяет логические каналы для разделения типов аудио контента.
 * Каждый канал может иметь независимую громкость, панорамирование и настройки.
 */
enum class AudioChannel(
    val defaultVolume: Float,
    val description: String
) {
    /**
     * Главный канал - контролирует общую громкость всего аудио.
     * Все остальные каналы умножаются на громкость MASTER.
     */
    MASTER(
        defaultVolume = 1.0f,
        description = "Общая громкость всего аудио"
    ),

    /**
     * Канал фоновой музыки (BGM - Background Music).
     * Используется для музыкальных треков меню, геймплея, боссов.
     */
    MUSIC(
        defaultVolume = AudioConstants.DEFAULT_MUSIC_VOLUME,
        description = "Фоновая музыка"
    ),

    /**
     * Канал звуковых эффектов (SFX - Sound Effects).
     * Используется для коротких звуков: прыжки, удары, сбор предметов.
     */
    SFX(
        defaultVolume = AudioConstants.DEFAULT_SFX_VOLUME,
        description = "Звуковые эффекты"
    ),

    /**
     * Канал фоновых звуков окружения.
     * Используется для атмосферных звуков: ветер, вода, город.
     */
    AMBIENCE(
        defaultVolume = AudioConstants.DEFAULT_AMBIENCE_VOLUME,
        description = "Фоновые звуки окружения"
    ),

    /**
     * Канал UI звуков.
     * Используется для звуков интерфейса: клики, переходы, уведомления.
     */
    UI(
        defaultVolume = 0.6f,
        description = "Звуки пользовательского интерфейса"
    );

    companion object {
        /**
         * Все каналы в виде списка.
         */
        val ALL: List<AudioChannel> = entries.toList()

        /**
         * Каналы, которые могут быть заглушены независимо.
         */
        val INDEPENDENT: List<AudioChannel> = listOf(MUSIC, SFX, AMBIENCE, UI)
    }
}

/**
 * Состояние канала аудио микшера.
 *
 * @param volume Громкость канала (0.0 - 1.0)
 * @param pan Панорамирование (-1.0 - 1.0)
 * @param pitch Высота тона (0.5 - 2.0)
 * @param isMuted Заглушен ли канал
 */
data class ChannelState(
    val volume: Float = 1.0f,
    val pan: Float = 0.0f,
    val pitch: Float = 1.0f,
    val isMuted: Boolean = false
) {
    /**
     * Проверка, активен ли канал (не заглушен и громкость > 0).
     */
    val isActive: Boolean
        get() = !isMuted && volume > AudioConstants.SILENT_THRESHOLD

    /**
     * Эффективная громкость с учётом mute.
     */
    val effectiveVolume: Float
        get() = if (isMuted) 0.0f else volume

    companion object {
        /**
         * Состояние по умолчанию для канала.
         */
        fun default() = ChannelState()

        /**
         * Заглушенное состояние.
         */
        val MUTED = ChannelState(isMuted = true)

        /**
         * Тихое состояние.
         */
        val QUIET = ChannelState(volume = 0.3f)
    }
}
