package com.endlessrunner.audio

/**
 * Константы аудио системы.
 *
 * Определяет лимиты и настройки по умолчанию для аудио подсистемы.
 */
object AudioConstants {
    /**
     * Максимальное количество аудио каналов в SoundPool.
     * Определяет общее количество одновременно воспроизводимых звуков.
     */
    const val MAX_AUDIO_CHANNELS: Int = 16

    /**
     * Максимальное количество одновременных звуковых эффектов.
     * Ограничивает количество SFX для предотвращения перегрузки.
     */
    const val MAX_SIMULTANEOUS_SFX: Int = 8

    /**
     * Длительность затухания музыки по умолчанию.
     */
    const val MUSIC_FADE_DURATION: Long = 1000L

    /**
     * Размер пула объектов для звуковых эффектов.
     */
    const val AUDIO_POOL_SIZE: Int = 32

    /**
     * Поддерживаемые форматы аудио файлов.
     */
    val SUPPORTED_FORMATS: List<String> = listOf(
        "mp3",
        "ogg",
        "wav",
        "m4a",
        "aac"
    )

    /**
     * Минимальная громкость (в линейной шкале).
     */
    const val MIN_VOLUME: Float = 0.0f

    /**
     * Максимальная громкость (в линейной шкале).
     */
    const val MAX_VOLUME: Float = 1.0f

    /**
     * Громкость по умолчанию для музыки.
     */
    const val DEFAULT_MUSIC_VOLUME: Float = 0.7f

    /**
     * Громкость по умолчанию для звуковых эффектов.
     */
    const val DEFAULT_SFX_VOLUME: Float = 0.8f

    /**
     * Громкость по умолчанию для фоновых звуков.
     */
    const val DEFAULT_AMBIENCE_VOLUME: Float = 0.5f

    /**
     * Приоритет по умолчанию для звуковых эффектов.
     */
    const val DEFAULT_PRIORITY: Int = 5

    /**
     * Максимальный приоритет для критических звуков.
     */
    const val MAX_PRIORITY: Int = 10

    /**
     * Минимальный приоритет для фоновых звуков.
     */
    const val MIN_PRIORITY: Int = 1

    /**
     * Порог для определения "тихого" звука.
     */
    const val SILENT_THRESHOLD: Float = 0.01f

    /**
     * Количество шагов для fade эффекта.
     */
    const val FADE_STEPS: Int = 20

    /**
     * Интервал между шагами fade эффекта (мс).
     */
    val FADE_STEP_INTERVAL: Long = MUSIC_FADE_DURATION / FADE_STEPS
}
