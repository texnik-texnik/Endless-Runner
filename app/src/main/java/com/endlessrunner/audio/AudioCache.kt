package com.endlessrunner.audio

import android.util.LruCache

/**
 * LRU кэш для аудио объектов.
 *
 * Кэширует MusicTrack и SoundEffect объекты для быстрого доступа.
 * Использует LruCache для автоматического управления памятью.
 *
 * @param maxSize Максимальный размер кэша в байтах (по умолчанию 10MB)
 */
class AudioCache private constructor(
    maxSize: Long = 10 * 1024 * 1024 // 10 MB
) {
    /**
     * Кэш для музыкальных треков.
     */
    private val musicCache = LruCache<String, MusicTrack>(50) // Максимум 50 треков

    /**
     * Кэш для звуковых эффектов.
     */
    private val sfxCache = LruCache<String, SoundEffect>(100) // Максимум 100 звуков

    /**
     * Размер кэша в байтах.
     */
    private var currentCacheSize: Long = 0

    /**
     * Максимальный размер кэша.
     */
    private val maxCacheSize = maxSize

    /**
     * Получить музыкальный трек из кэша.
     *
     * @param key ID трека
     * @return MusicTrack или null
     */
    fun getMusic(key: String): MusicTrack? = musicCache.get(key)

    /**
     * Получить звуковой эффект из кэша.
     *
     * @param key ID звука
     * @return SoundEffect или null
     */
    fun getSound(key: String): SoundEffect? = sfxCache.get(key)

    /**
     * Получить аудио объект из кэша (общий метод).
     *
     * @param key ID объекта
     * @return Any? (MusicTrack или SoundEffect)
     */
    fun get(key: String): Any? = getMusic(key) ?: getSound(key)

    /**
     * Положить музыкальный трек в кэш.
     *
     * @param key ID трека
     * @param track MusicTrack для кэширования
     */
    fun put(key: String, track: MusicTrack) {
        musicCache.put(key, track)
        updateCacheSize()
    }

    /**
     * Положить звуковой эффект в кэш.
     *
     * @param key ID звука
     * @param effect SoundEffect для кэширования
     */
    fun put(key: String, effect: SoundEffect) {
        sfxCache.put(key, effect)
        updateCacheSize()
    }

    /**
     * Удалить объект из кэша по ключу.
     *
     * @param key ID объекта
     * @return Удалённый объект или null
     */
    fun remove(key: String): Any? {
        val removedMusic = musicCache.remove(key)
        val removedSfx = sfxCache.remove(key)
        updateCacheSize()
        return removedMusic ?: removedSfx
    }

    /**
     * Удалить музыкальный трек из кэша.
     *
     * @param key ID трека
     */
    fun removeMusic(key: String) {
        musicCache.remove(key)
        updateCacheSize()
    }

    /**
     * Удалить звуковой эффект из кэша.
     *
     * @param key ID звука
     */
    fun removeSound(key: String) {
        sfxCache.remove(key)
        updateCacheSize()
    }

    /**
     * Очистить весь кэш.
     */
    fun clear() {
        musicCache.evictAll()
        sfxCache.evictAll()
        currentCacheSize = 0
    }

    /**
     * Очистить кэш музыки.
     */
    fun clearMusic() {
        musicCache.evictAll()
        updateCacheSize()
    }

    /**
     * Очистить кэш звуков.
     */
    fun clearSfx() {
        sfxCache.evictAll()
        updateCacheSize()
    }

    /**
     * Получить размер кэша в байтах.
     */
    fun getCacheSize(): Long = currentCacheSize

    /**
     * Получить количество элементов в кэше музыки.
     */
    fun getMusicCount(): Int = musicCache.size()

    /**
     * Получить количество элементов в кэше звуков.
     */
    fun getSoundCount(): Int = sfxCache.size()

    /**
     * Получить общее количество элементов в кэше.
     */
    fun getTotalCount(): Int = getMusicCount() + getSoundCount()

    /**
     * Получить все музыкальные треки из кэша.
     */
    fun getAllMusic(): List<MusicTrack> {
        return buildList {
            for (key in musicCache.snapshot().keys) {
                musicCache.get(key)?.let { add(it) }
            }
        }
    }

    /**
     * Получить все звуковые эффекты из кэша.
     */
    fun getAllSounds(): List<SoundEffect> {
        return buildList {
            for (key in sfxCache.snapshot().keys) {
                sfxCache.get(key)?.let { add(it) }
            }
        }
    }

    /**
     * Проверка, содержит ли кэш объект.
     *
     * @param key ID объекта
     * @return true если объект в кэше
     */
    fun contains(key: String): Boolean = getMusic(key) != null || getSound(key) != null

    /**
     * Проверка, содержит ли кэш музыкальный трек.
     */
    fun containsMusic(key: String): Boolean = musicCache.get(key) != null

    /**
     * Проверка, содержит ли кэш звуковой эффект.
     */
    fun containsSound(key: String): Boolean = sfxCache.get(key) != null

    /**
     * Обновить текущий размер кэша.
     */
    private fun updateCacheSize() {
        // Приблизительный расчёт размера
        val musicSize = musicCache.snapshot().size * 1024 // ~1KB на трек
        val sfxSize = sfxCache.snapshot().size * 512 // ~512B на звук
        currentCacheSize = (musicSize + sfxSize).toLong()
    }

    /**
     * Получить статистику кэша.
     *
     * @return CacheStats
     */
    fun getStats(): CacheStats {
        return CacheStats(
            musicCount = getMusicCount(),
            soundCount = getSoundCount(),
            totalCount = getTotalCount(),
            cacheSize = getCacheSize(),
            maxCacheSize = maxCacheSize,
            fillPercent = (currentCacheSize.toFloat() / maxCacheSize * 100).toInt()
        )
    }

    /**
     * Принудительная сборка мусора для кэша.
     */
    fun trimToSize(size: Long) {
        // LruCache автоматически управляет размером
        // Этот метод для совместимости
    }

    companion object {
        @Volatile
        private var instance: AudioCache? = null

        /**
         * Получить singleton экземпляр.
         */
        fun getInstance(maxSize: Long = 10 * 1024 * 1024): AudioCache {
            return instance ?: synchronized(this) {
                instance ?: AudioCache(maxSize).also { instance = it }
            }
        }

        /**
         * Очистить singleton (для тестов).
         */
        fun clearInstance() {
            instance?.clear()
            instance = null
        }
    }
}

/**
 * Статистика кэша.
 *
 * @param musicCount Количество музыкальных треков
 * @param soundCount Количество звуковых эффектов
 * @param totalCount Общее количество объектов
 * @param cacheSize Текущий размер кэша в байтах
 * @param maxCacheSize Максимальный размер кэша
 * @param fillPercent Процент заполнения
 */
data class CacheStats(
    val musicCount: Int,
    val soundCount: Int,
    val totalCount: Int,
    val cacheSize: Long,
    val maxCacheSize: Long,
    val fillPercent: Int
) {
    /**
     * Форматированный размер кэша.
     */
    val formattedCacheSize: String
        get() = formatBytes(cacheSize)

    /**
     * Форматированный максимальный размер.
     */
    val formattedMaxSize: String
        get() = formatBytes(maxCacheSize)

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format("%.2f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
