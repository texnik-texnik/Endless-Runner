package com.endlessrunner.audio

/**
 * Data class музыкального трека.
 *
 * Представляет собой метаданные музыкального файла для фоновой музыки.
 *
 * @param id Уникальный идентификатор трека
 * @param name Название трека для отображения
 * @param assetPath Путь к файлу в assets
 * @param duration Длительность в миллисекундах (0 если неизвестна)
 * @param isLooping Зацикливать ли трек
 * @param volume Громкость от 0.0 до 1.0
 * @param isPlaying Флаг воспроизведения (вычисляется динамически)
 * @param currentPosition Текущая позиция в мс (вычисляется динамически)
 */
data class MusicTrack(
    val id: String,
    val name: String,
    val assetPath: String,
    val duration: Long = 0L,
    val isLooping: Boolean = true,
    val volume: Float = 1.0f,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L
) {
    init {
        require(id.isNotBlank()) { "MusicTrack id cannot be blank" }
        require(name.isNotBlank()) { "MusicTrack name cannot be blank" }
        require(assetPath.isNotBlank()) { "MusicTrack assetPath cannot be blank" }
        require(volume in AudioConstants.MIN_VOLUME..AudioConstants.MAX_VOLUME) {
            "Volume must be between ${AudioConstants.MIN_VOLUME} and ${AudioConstants.MAX_VOLUME}"
        }
        require(duration >= 0L) { "Duration cannot be negative" }
        require(currentPosition >= 0L) { "Current position cannot be negative" }
    }

    /**
     * Проверка, является ли формат файла поддерживаемым.
     */
    val isSupportedFormat: Boolean
        get() {
            val extension = assetPath.substringAfterLast('.', "").lowercase()
            return extension in AudioConstants.SUPPORTED_FORMATS
        }

    /**
     * Форматированная длительность в виде mm:ss.
     */
    val formattedDuration: String
        get() = duration.formatDuration()

    /**
     * Форматированная текущая позиция в виде mm:ss.
     */
    val formattedCurrentPosition: String
        get() = currentPosition.formatDuration()

    /**
     * Процент воспроизведения (0.0 - 1.0).
     */
    val progressPercent: Float
        get() = if (duration > 0L) currentPosition.toFloat() / duration else 0.0f

    /**
     * Создать копию трека с изменёнными параметрами.
     */
    fun copy(
        id: String = this.id,
        name: String = this.name,
        assetPath: String = this.assetPath,
        duration: Long = this.duration,
        isLooping: Boolean = this.isLooping,
        volume: Float = this.volume,
        isPlaying: Boolean = this.isPlaying,
        currentPosition: Long = this.currentPosition
    ): MusicTrack = MusicTrack(
        id = id,
        name = name,
        assetPath = assetPath,
        duration = duration,
        isLooping = isLooping,
        volume = volume,
        isPlaying = isPlaying,
        currentPosition = currentPosition
    )

    companion object {
        /**
         * Пустой трек (заглушка).
         */
        val EMPTY = MusicTrack(
            id = "",
            name = "",
            assetPath = "",
            duration = 0L,
            isLooping = false,
            volume = 0.0f
        )

        /**
         * Создать трек с путем по умолчанию.
         */
        fun create(
            id: String,
            name: String,
            folder: String = "music",
            extension: String = "ogg"
        ): MusicTrack {
            return MusicTrack(
                id = id,
                name = name,
                assetPath = "$folder/${id}.$extension"
            )
        }
    }
}

/**
 * Расширение для форматирования длительности.
 */
private fun Long.formatDuration(): String {
    val seconds = this / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}
