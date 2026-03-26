package com.endlessrunner.audio

import android.content.Context
import android.media.SoundPool
import android.media.AudioAttributes
import android.os.Build

/**
 * Класс звукового эффекта.
 *
 * Представляет собой загруженный звук для воспроизведения через SoundPool.
 * Поддерживает настройку громкости, высоты тона и панорамирования.
 *
 * @param id Уникальный идентификатор звука
 * @param name Название звука
 * @param assetPath Путь к файлу в assets
 * @param priority Приоритет звука (1-10, где 10 - наивысший)
 * @param volume Громкость по умолчанию
 */
class SoundEffect(
    val id: String,
    val name: String,
    val assetPath: String,
    val priority: Int = AudioConstants.DEFAULT_PRIORITY,
    var volume: Float = 1.0f
) {
    /**
     * ID звука в SoundPool.
     */
    var soundPoolId: Int = -1
        private set

    /**
     * Флаг загрузки звука.
     */
    var isLoaded: Boolean = false
        private set

    /**
     * Ссылка на SoundPool (устанавливается при загрузке).
     */
    private var soundPool: SoundPool? = null

    /**
     * Контекст приложения.
     */
    private var context: Context? = null

    /**
     * Загрузить звук в SoundPool.
     *
     * @param soundPool SoundPool для загрузки
     * @param context Контекст приложения
     * @return true если загрузка успешна
     */
    fun load(soundPool: SoundPool, context: Context): Boolean {
        try {
            this.soundPool = soundPool
            this.context = context.applicationContext

            // Загрузка из assets
            context.assets.openFd(assetPath).use { assetFd ->
                soundPoolId = soundPool.load(
                    assetFd.fileDescriptor,
                    1 // priority (не влияет на SoundPool)
                )
            }

            isLoaded = soundPoolId != -1
            return isLoaded
        } catch (e: Exception) {
            e.printStackTrace()
            isLoaded = false
            soundPoolId = -1
            return false
        }
    }

    /**
     * Воспроизвести звук.
     *
     * @param volume Громкость от 0.0 до 1.0 (по умолчанию 1.0)
     * @param pitch Высота тона (1.0 = оригинальная, 0.5 = октава вниз, 2.0 = октава вверх)
     * @param pan Панорамирование (-1.0 = лево, 0.0 = центр, 1.0 = право)
     * @param loop Количество повторений (-1 = бесконечно, 0 = без повторов)
     * @return streamId для управления воспроизведением, или -1 если ошибка
     */
    fun play(
        volume: Float = this.volume,
        pitch: Float = 1.0f,
        pan: Float = 0.0f,
        loop: Int = 0
    ): Int {
        if (!isLoaded || soundPool == null) {
            return -1
        }

        // Ограничение параметров
        val clampedVolume = volume.coerceIn(0.0f, 1.0f)
        val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
        val clampedPan = pan.coerceIn(-1.0f, 1.0f)

        // Конвертация pan в левый/правый каналы
        val (leftVolume, rightVolume) = panToStereo(clampedPan, clampedVolume)

        return try {
            soundPool!!.play(
                soundPoolId,
                leftVolume,
                rightVolume,
                priority,
                loop,
                clampedPitch
            )
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    /**
     * Остановить конкретное воспроизведение звука.
     *
     * @param soundId streamId возвращённый из play()
     */
    fun stop(soundId: Int) {
        if (soundPool != null && soundId != -1) {
            try {
                soundPool?.stop(soundId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Остановить все воспроизведения этого звука.
     */
    fun stopAll() {
        if (soundPool != null && soundPoolId != -1) {
            try {
                soundPool?.stop(soundPoolId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Выгрузить звук из памяти.
     */
    fun unload() {
        if (soundPool != null && soundPoolId != -1) {
            try {
                soundPool?.unload(soundPoolId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        soundPoolId = -1
        isLoaded = false
        soundPool = null
        context = null
    }

    /**
     * Проверка, может ли звук быть воспроизведён.
     */
    fun canPlay(): Boolean = isLoaded && soundPoolId != -1 && soundPool != null

    /**
     * Создать копию звука с изменёнными параметрами.
     */
    fun copy(
        id: String = this.id,
        name: String = this.name,
        assetPath: String = this.assetPath,
        priority: Int = this.priority,
        volume: Float = this.volume
    ): SoundEffect = SoundEffect(
        id = id,
        name = name,
        assetPath = assetPath,
        priority = priority,
        volume = volume
    )

    companion object {
        /**
         * Конвертация панорамирования в стереогромкость.
         *
         * @param pan Панорамирование (-1.0 до 1.0)
         * @param volume Общая громкость
         * @return Pair(leftVolume, rightVolume)
         */
        private fun panToStereo(pan: Float, volume: Float): Pair<Float, Float> {
            // pan = -1.0 -> left = 1.0, right = 0.0
            // pan = 0.0 -> left = 1.0, right = 1.0
            // pan = 1.0 -> left = 0.0, right = 1.0
            val leftVolume = ((1.0f - pan) * volume).coerceIn(0.0f, 1.0f)
            val rightVolume = ((1.0f + pan) * volume).coerceIn(0.0f, 1.0f)
            return Pair(leftVolume, rightVolume)
        }

        /**
         * Создать пустой звук (заглушку).
         */
        val EMPTY = SoundEffect(
            id = "",
            name = "",
            assetPath = ""
        )

        /**
         * Создать звук с путем по умолчанию.
         */
        fun create(
            id: String,
            name: String,
            folder: String = "sfx",
            extension: String = "ogg"
        ): SoundEffect {
            return SoundEffect(
                id = id,
                name = name,
                assetPath = "$folder/${id}.$extension"
            )
        }
    }
}
