package com.endlessrunner.audio

/**
 * Sealed class представляющая состояние аудио компонента.
 *
 * Используется для отслеживания состояния воспроизведения музыки и звуков.
 */
sealed class AudioState {
    /**
     * Компонент неактивен, ничего не воспроизводится.
     */
    object Idle : AudioState()

    /**
     * Компонент активно воспроизводит аудио.
     */
    object Playing : AudioState()

    /**
     * Воспроизведение приостановлено.
     */
    object Paused : AudioState()

    /**
     * Воспроизведение остановлено.
     */
    object Stopped : AudioState()
}

/**
 * Параметры аудио состояния.
 *
 * @param volume Громкость от 0.0 до 1.0
 * @param pitch Высота тона (1.0 = оригинальная)
 * @param pan Панорамирование (-1.0 = лево, 0.0 = центр, 1.0 = право)
 */
data class AudioParams(
    val volume: Float = 1.0f,
    val pitch: Float = 1.0f,
    val pan: Float = 0.0f
) {
    init {
        require(volume in AudioConstants.MIN_VOLUME..AudioConstants.MAX_VOLUME) {
            "Volume must be between ${AudioConstants.MIN_VOLUME} and ${AudioConstants.MAX_VOLUME}, was $volume"
        }
        require(pitch > 0f) {
            "Pitch must be positive, was $pitch"
        }
        require(pan in -1.0f..1.0f) {
            "Pan must be between -1.0 and 1.0, was $pan"
        }
    }

    companion object {
        /**
         * Параметры по умолчанию (полная громкость, нормальная высота, центр).
         */
        val DEFAULT = AudioParams()

        /**
         * Тихие параметры (для фоновых звуков).
         */
        val QUIET = AudioParams(volume = 0.5f)

        /**
         * Громкие параметры (для важных звуков).
         */
        val LOUD = AudioParams(volume = 1.0f)
    }
}

/**
 * Расширения для работы с AudioState.
 */

/**
 * Проверка, является ли состояние активным (Playing или Paused).
 */
fun AudioState.isActive(): Boolean = this is AudioState.Playing || this is AudioState.Paused

/**
 * Проверка, воспроизводится ли аудио в данный момент.
 */
fun AudioState.isPlaying(): Boolean = this is AudioState.Playing

/**
 * Проверка, приостановлено ли аудио.
 */
fun AudioState.isPaused(): Boolean = this is AudioState.Paused

/**
 * Проверка, остановлено ли аудио.
 */
fun AudioState.isStopped(): Boolean = this is AudioState.Stopped

/**
 * Проверка, находится ли аудио в состоянии покоя.
 */
fun AudioState.isIdle(): Boolean = this is AudioState.Idle
