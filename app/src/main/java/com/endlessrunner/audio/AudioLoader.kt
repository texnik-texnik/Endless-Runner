package com.endlessrunner.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Загрузчик аудио файлов.
 *
 * Отвечает за загрузку и выгрузку аудио файлов из assets.
 * Поддерживает асинхронную загрузку и предзагрузку по категориям.
 *
 * @param context Контекст приложения
 * @param audioManager Менеджер аудио
 */
class AudioLoader(
    private val context: Context,
    private val audioManager: AudioManager
) {
    /**
     * CoroutineScope для асинхронных операций.
     */
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * AudioCache для кэширования загруженных звуков.
     */
    private val audioCache: AudioCache

    /**
     * Progress загрузки (0.0 - 1.0).
     */
    private val _loadProgress = MutableStateFlow(0.0f)
    val loadProgress: StateFlow<Float> = _loadProgress.asStateFlow()

    /**
     * Флаг загрузки.
     */
    private var isLoading = false

    /**
     * Список загруженных путей.
     */
    private val loadedPaths = mutableSetOf<String>()

    init {
        audioCache = AudioCache.getInstance()
    }

    /**
     * Загрузить музыкальный трек из assets.
     *
     * @param path Путь к файлу в assets
     * @return MusicTrack или null если ошибка
     */
    suspend fun loadMusicFromAsset(path: String): MusicTrack? = withContext(Dispatchers.IO) {
        try {
            if (!isFileExists(path)) {
                return@withContext null
            }

            val id = extractIdFromPath(path)
            val name = extractNameFromPath(path)

            // Получить длительность
            val duration = getAssetDuration(path)

            MusicTrack(
                id = id,
                name = name,
                assetPath = path,
                duration = duration,
                isLooping = true
            )
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Загрузить звуковой эффект из assets.
     *
     * @param path Путь к файлу в assets
     * @param priority Приоритет звука
     * @return SoundEffect или null если ошибка
     */
    suspend fun loadSfxFromAsset(
        path: String,
        priority: Int = AudioConstants.DEFAULT_PRIORITY
    ): SoundEffect? = withContext(Dispatchers.IO) {
        try {
            if (!isFileExists(path)) {
                return@withContext null
            }

            val id = extractIdFromPath(path)
            val name = extractNameFromPath(path)

            SoundEffect(
                id = id,
                name = name,
                assetPath = path,
                priority = priority
            )
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Предзагрузить всю музыку.
     */
    suspend fun preloadAllMusic() {
        if (isLoading) return
        isLoading = true

        try {
            val musicTracks = MusicLibrary.ALL_TRACKS
            val total = musicTracks.size
            var loaded = 0

            musicTracks.forEach { track ->
                val loadedTrack = loadMusicFromAsset(track.assetPath)
                if (loadedTrack != null) {
                    loadedPaths.add(track.assetPath)
                    audioCache.put(track.id, loadedTrack)
                }
                loaded++
                _loadProgress.value = loaded.toFloat() / total
            }
        } finally {
            isLoading = false
        }
    }

    /**
     * Предзагрузить все звуковые эффекты.
     */
    suspend fun preloadAllSfx() {
        if (isLoading) return
        isLoading = true

        try {
            val sounds = SoundLibrary.ALL_SOUNDS
            val total = sounds.size
            var loaded = 0

            sounds.forEach { sound ->
                val loadedSound = loadSfxFromAsset(sound.assetPath, sound.priority)
                if (loadedSound != null) {
                    loadedPaths.add(sound.assetPath)
                    audioCache.put(sound.id, loadedSound)
                }
                loaded++
                _loadProgress.value = loaded.toFloat() / total
            }
        } finally {
            isLoading = false
        }
    }

    /**
     * Предзагрузить категорию звуков.
     *
     * @param category Категория звуков
     */
    suspend fun preloadCategory(category: SoundLibrary.SoundCategory) {
        if (isLoading) return
        isLoading = true

        try {
            val sounds = SoundLibrary.getSoundsByCategory(category)
            val total = sounds.size
            var loaded = 0

            sounds.forEach { sound ->
                val loadedSound = loadSfxFromAsset(sound.assetPath, sound.priority)
                if (loadedSound != null) {
                    loadedPaths.add(sound.assetPath)
                    audioCache.put(sound.id, loadedSound)
                }
                loaded++
                _loadProgress.value = loaded.toFloat() / total
            }
        } finally {
            isLoading = false
        }
    }

    /**
     * Предзагрузить музыку и звуки асинхронно.
     *
     * @param onProgress Callback прогресса (0.0 - 1.0)
     * @param onComplete Callback завершения
     */
    fun preloadAllAsync(
        onProgress: (Float) -> Unit = {},
        onComplete: () -> Unit = {}
    ) {
        scope.launch {
            try {
                // Загрузка музыки и звуков параллельно
                val musicJob = async { preloadAllMusic() }
                val sfxJob = async { preloadAllSfx() }

                awaitAll(musicJob, sfxJob)
                onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }

        // Наблюдение за прогрессом
        scope.launch {
            loadProgress.collect { progress ->
                onProgress(progress)
            }
        }
    }

    /**
     * Выгрузить все аудио ресурсы.
     */
    fun unloadAll() {
        // Выгрузка звуков из кэша
        audioCache.getAllSounds().forEach { sound ->
            sound.unload()
        }

        // Очистка кэша
        audioCache.clear()

        // Очистка загруженных путей
        loadedPaths.clear()

        // Сброс прогресса
        _loadProgress.value = 0.0f
    }

    /**
     * Выгрузить конкретный трек.
     *
     * @param trackId ID трека
     */
    fun unloadTrack(trackId: String) {
        audioCache.remove(trackId)
    }

    /**
     * Выгрузить конкретный звук.
     *
     * @param soundId ID звука
     */
    fun unloadSound(soundId: String) {
        val sound = audioCache.getSound(soundId)
        sound?.unload()
        audioCache.remove(soundId)
    }

    /**
     * Проверка, существует ли файл в assets.
     */
    private fun isFileExists(path: String): Boolean {
        return try {
            context.assets.openFd(path).close()
            true
        } catch (e: IOException) {
            false
        }
    }

    /**
     * Получить длительность аудио файла.
     */
    private fun getAssetDuration(path: String): Long {
        return try {
            context.assets.openFd(path).use { fd ->
                fd.length * 8 / 128 // Приблизительная оценка для MP3
            }
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Извлечь ID из пути.
     */
    private fun extractIdFromPath(path: String): String {
        return path.substringAfterLast('/')
            .substringBeforeLast('.')
    }

    /**
     * Извлечь название из пути.
     */
    private fun extractNameFromPath(path: String): String {
        return extractIdFromPath(path)
            .replace('_', ' ')
            .replaceFirstChar { it.uppercase() }
    }

    /**
     * Получить список загруженных путей.
     */
    fun getLoadedPaths(): Set<String> = loadedPaths.toSet()

    /**
     * Проверка, загружен ли путь.
     */
    fun isPathLoaded(path: String): Boolean = path in loadedPaths

    /**
     * Освободить ресурсы.
     */
    fun release() {
        unloadAll()
        scope.launch {
            SupervisorJob().cancel()
        }
    }

    companion object {
        @Volatile
        private var instance: AudioLoader? = null

        /**
         * Получить singleton экземпляр.
         */
        fun getInstance(context: Context, audioManager: AudioManager): AudioLoader {
            return instance ?: synchronized(this) {
                instance ?: AudioLoader(context.applicationContext, audioManager)
                    .also { instance = it }
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
