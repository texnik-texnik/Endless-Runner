package com.endlessrunner.audio

import android.content.Context
import android.media.SoundPool
import android.media.AudioAttributes
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * Object pool для звуковых эффектов.
 *
 * Управляет пулом SoundEffect объектов для минимизации аллокаций.
 * Использует ConcurrentLinkedQueue для потокобезопасности.
 *
 * @param context Контекст приложения
 * @param initialSize Начальный размер пула
 * @param maxSize Максимальный размер пула
 */
class SoundEffectPool(
    private val context: Context,
    initialSize: Int = AudioConstants.AUDIO_POOL_SIZE,
    private val maxSize: Int = AudioConstants.AUDIO_POOL_SIZE * 2
) {
    /**
     * SoundPool для воспроизведения звуков.
     */
    private val soundPool: SoundPool

    /**
     * Пул свободных SoundEffect объектов.
     */
    private val freePool = ConcurrentLinkedQueue<SoundEffect>()

    /**
     * Список активных (используемых) звуков.
     */
    private val activeEffects = mutableMapOf<String, SoundEffect>()

    /**
     * Счётчик созданных объектов.
     */
    private val createdCount = AtomicInteger(0)

    /**
     * Флаг инициализации.
     */
    private var isInitialized = false

    init {
        // Создание SoundPool с правильными атрибутами
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(AudioConstants.MAX_AUDIO_CHANNELS)
            .setAudioAttributes(audioAttributes)
            .build()

        // Предварительное создание объектов пула
        for (i in 0 until initialSize) {
            freePool.offer(createPooledEffect())
        }
    }

    /**
     * Создать новый объект для пула.
     */
    private fun createPooledEffect(): SoundEffect {
        val id = "pooled_${createdCount.incrementAndGet()}"
        return SoundEffect(
            id = id,
            name = "Pooled Effect",
            assetPath = "",
            priority = AudioConstants.DEFAULT_PRIORITY,
            volume = 1.0f
        )
    }

    /**
     * Получить объект из пула.
     *
     * @return SoundEffect из пула или новый если пул пуст
     */
    fun acquire(): SoundEffect {
        return freePool.poll() ?: createPooledEffect()
    }

    /**
     * Вернуть объект в пул.
     *
     * @param effect SoundEffect для возврата
     */
    fun release(effect: SoundEffect) {
        // Остановить воспроизведение
        effect.stopAll()

        // Сбросить состояние
        effect.unload()

        // Вернуть в пул если не превышен максимальный размер
        if (freePool.size < maxSize) {
            freePool.offer(effect)
        }
    }

    /**
     * Загрузить звук в пул.
     *
     * @param effect SoundEffect для загрузки
     * @return true если загрузка успешна
     */
    fun load(effect: SoundEffect): Boolean {
        val loaded = effect.load(soundPool, context)
        if (loaded) {
            activeEffects[effect.id] = effect
        }
        return loaded
    }

    /**
     * Воспроизвести звук из пула.
     *
     * @param effectId ID звука
     * @param volume Громкость
     * @param pitch Высота тона
     * @param pan Панорамирование
     * @return streamId или -1 если ошибка
     */
    fun play(
        effectId: String,
        volume: Float = 1.0f,
        pitch: Float = 1.0f,
        pan: Float = 0.0f
    ): Int {
        val effect = activeEffects[effectId]
        return effect?.play(volume, pitch, pan) ?: -1
    }

    /**
     * Получить количество свободных объектов в пуле.
     */
    fun getFreeCount(): Int = freePool.size

    /**
     * Получить количество активных звуков.
     */
    fun getActiveCount(): Int = activeEffects.size

    /**
     * Получить общее количество созданных объектов.
     */
    fun getCreatedCount(): Int = createdCount.get()

    /**
     * Расширить пул на указанное количество объектов.
     *
     * @param count Количество объектов для добавления
     */
    fun expand(count: Int) {
        for (i in 0 until count) {
            if (freePool.size + activeEffects.size < maxSize) {
                freePool.offer(createPooledEffect())
            }
        }
    }

    /**
     * Очистить пул.
     */
    fun clear() {
        activeEffects.values.forEach { it.unload() }
        activeEffects.clear()
        freePool.forEach { it.unload() }
        freePool.clear()
    }

    /**
     * Освободить все ресурсы.
     */
    fun release() {
        clear()
        soundPool.release()
        isInitialized = false
    }

    /**
     * Получить SoundPool для прямой работы.
     */
    fun getSoundPool(): SoundPool = soundPool

    /**
     * Обработка паузы приложения.
     */
    fun onPause() {
        soundPool.autoPause()
    }

    /**
     * Обработка возобновления приложения.
     */
    fun onResume() {
        soundPool.autoResume()
    }

    companion object {
        @Volatile
        private var instance: SoundEffectPool? = null

        /**
         * Получить singleton экземпляр.
         */
        fun getInstance(context: Context): SoundEffectPool {
            return instance ?: synchronized(this) {
                instance ?: SoundEffectPool(context.applicationContext).also { instance = it }
            }
        }

        /**
         * Очистить singleton (для тестов).
         */
        fun clearInstance() {
            instance?.release()
            instance = null
        }
    }
}
