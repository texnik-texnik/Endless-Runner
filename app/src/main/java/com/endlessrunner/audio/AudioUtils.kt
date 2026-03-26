package com.endlessrunner.audio

import kotlin.math.ln
import kotlin.math.pow

/**
 * Утилиты для работы с аудио.
 *
 * Extension функции и вспомогательные методы для аудио операций.
 */
object AudioUtils {

    /**
     * Конвертировать децибелы в линейную громкость.
     *
     * @receiver Значение в дБ (обычно от -60 до 0)
     * @return Линейная громкость (0.0 - 1.0)
     */
    fun Float.dBToLinear(): Float {
        // dB = 20 * log10(linear)
        // linear = 10^(dB/20)
        return 10.0.pow(this / 20.0).toFloat().coerceIn(0.0f, 1.0f)
    }

    /**
     * Конвертировать линейную громкость в децибелы.
     *
     * @receiver Линейная громкость (0.0 - 1.0)
     * @return Значение в дБ (обычно от -60 до 0)
     */
    fun Float.linearToDB(): Float {
        if (this <= 0.0f) return -60.0f // Порог слышимости
        // dB = 20 * log10(linear)
        return (20.0 * ln(this) / ln(10.0)).toFloat().coerceAtLeast(-60.0f)
    }

    /**
     * Конвертировать значение панорамирования из Int в Float.
     *
     * @receiver Значение от -127 до 127 (MIDI pan)
     * @return Панорамирование от -1.0 до 1.0
     */
    fun Int.toPan(): Float {
        return (this / 127.0f * 2.0f - 1.0f).coerceIn(-1.0f, 1.0f)
    }

    /**
     * Конвертировать значение панорамирования из Float в Int.
     *
     * @receiver Панорамирование от -1.0 до 1.0
     * @return Значение от -127 до 127 (MIDI pan)
     */
    fun Float.toMidiPan(): Int {
        return ((this + 1.0f) / 2.0f * 127).toInt().coerceIn(-127, 127)
    }

    /**
     * Форматировать длительность в строку mm:ss.
     *
     * @receiver Длительность в миллисекундах
     * @return Строка в формате mm:ss
     */
    fun Long.formatDuration(): String {
        val seconds = this / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    /**
     * Форматировать длительность в строку hh:mm:ss.
     */
    fun Long.formatDurationLong(): String {
        val seconds = this / 1000
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
        } else {
            String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }

    /**
     * Интерполяция громкости (плавное изменение).
     *
     * @param from Начальная громкость
     * @param to Конечная громкость
     * @param progress Прогресс от 0.0 до 1.0
     * @return Интерполированная громкость
     */
    fun lerpVolume(from: Float, to: Float, progress: Float): Float {
        return from + (to - from) * progress.coerceIn(0.0f, 1.0f)
    }

    /**
     * Логарифмическая интерполяция громкости (более естественное восприятие).
     */
    fun logLerpVolume(from: Float, to: Float, progress: Float): Float {
        val fromDB = from.linearToDB()
        val toDB = to.linearToDB()
        val resultDB = lerpVolume(fromDB, toDB, progress)
        return resultDB.dBToLinear()
    }

    /**
     * Конвертировать панорамирование в стереогромкость.
     *
     * @param pan Панорамирование от -1.0 до 1.0
     * @param volume Общая громкость
     * @return Pair(leftVolume, rightVolume)
     */
    fun panToStereo(pan: Float, volume: Float = 1.0f): Pair<Float, Float> {
        val clampedPan = pan.coerceIn(-1.0f, 1.0f)
        val clampedVolume = volume.coerceIn(0.0f, 1.0f)

        // Constant power panning
        val leftVolume = (kotlin.math.cos((clampedPan + 1.0f) * kotlin.math.PI / 4) * clampedVolume).toFloat()
        val rightVolume = (kotlin.math.sin((clampedPan + 1.0f) * kotlin.math.PI / 4) * clampedVolume).toFloat()

        return Pair(leftVolume, rightVolume)
    }

    /**
     * Смешать две громкости.
     */
    fun mixVolumes(vararg volumes: Float): Float {
        return volumes.map { it.coerceIn(0.0f, 1.0f) }.average().toFloat()
    }

    /**
     * Применить кривую громкости (exponential fade).
     */
    fun applyVolumeCurve(volume: Float, exponent: Float = 2.0f): Float {
        return volume.pow(exponent).coerceIn(0.0f, 1.0f)
    }

    /**
     * Проверка, является ли громкость "тихой".
     */
    fun Float.isSilent(): Boolean = this < AudioConstants.SILENT_THRESHOLD

    /**
     * Проверка, является ли громкость "полной".
     */
    fun Float.isFullVolume(): Boolean = this > 0.99f

    /**
     * Получить следующую ступень громкости.
     */
    fun Float.nextVolumeStep(step: Float = 0.1f): Float =
        (this + step).coerceIn(AudioConstants.MIN_VOLUME, AudioConstants.MAX_VOLUME)

    /**
     * Получить предыдущую ступень громкости.
     */
    fun Float.previousVolumeStep(step: Float = 0.1f): Float =
        (this - step).coerceIn(AudioConstants.MIN_VOLUME, AudioConstants.MAX_VOLUME)

    /**
     * Конвертировать частоту ноты в MIDI номер.
     */
    fun frequencyToMidiNote(frequency: Float): Int {
        return (69 + 12 * kotlin.math.log2(frequency / 440.0)).toInt()
    }

    /**
     * Конвертировать MIDI номер ноты в частоту.
     */
    fun midiNoteToFrequency(midiNote: Int): Float {
        return 440.0f * 2.0f.pow((midiNote - 69) / 12.0f)
    }

    /**
     * Расстояние между двумя точками панорамирования.
     */
    fun panDistance(from: Float, to: Float): Float = kotlin.math.abs(to - from)

    /**
     * Нормализовать громкость относительно максимальной.
     */
    fun normalizeVolume(volume: Float, maxVolume: Float): Float =
        (volume / maxVolume).coerceIn(0.0f, 1.0f)
}

// Extension свойства для удобного доступа

/**
 * Конвертировать Float из дБ в линейную шкалу.
 */
val Float.dBToLinear: Float
    get() = AudioUtils.dBToLinear(this)

/**
 * Конвертировать Float из линейной шкалы в дБ.
 */
val Float.linearToDB: Float
    get() = AudioUtils.linearToDB(this)

/**
 * Конвертировать Int в панорамирование.
 */
val Int.toPan: Float
    get() = AudioUtils.toPan(this)

/**
 * Форматировать Long как длительность.
 */
val Long.formattedDuration: String
    get() = AudioUtils.formatDuration(this)

/**
 * Проверка, является ли громкость тихой.
 */
val Float.isSilent: Boolean
    get() = AudioUtils.isSilent(this)

/**
 * Проверка, является ли громкость полной.
 */
val Float.isFullVolume: Boolean
    get() = AudioUtils.isFullVolume(this)
