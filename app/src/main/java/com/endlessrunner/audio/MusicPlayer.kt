package com.endlessrunner.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Singleton для управления фоновой музыкой.
 *
 * Использует Android MediaPlayer для воспроизведения музыкальных треков.
 * Поддерживает плавное затухание, зацикливание и StateFlow для реактивного UI.
 *
 * @param context Контекст приложения
 */
class MusicPlayer(private val context: Context) {

    /**
     * MediaPlayer для воспроизведения музыки.
     */
    private var mediaPlayer: MediaPlayer? = null

    /**
     * CoroutineScope для асинхронных операций.
     */
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    /**
     * Текущий воспроизводимый трек.
     */
    private val _currentTrack = MutableStateFlow<MusicTrack?>(null)
    val currentTrack: StateFlow<MusicTrack?> = _currentTrack.asStateFlow()

    /**
     * Флаг воспроизведения.
     */
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    /**
     * Текущая громкость музыки.
     */
    private val _volume = MutableStateFlow(AudioConstants.DEFAULT_MUSIC_VOLUME)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    /**
     * Текущее состояние.
     */
    private val _state = MutableStateFlow<AudioState>(AudioState.Idle)
    val state: StateFlow<AudioState> = _state.asStateFlow()

    /**
     * Job для fade операций.
     */
    private var fadeJob: Job? = null

    /**
     * Флаг инициализации.
     */
    private var isInitialized = false

    /**
     * Инициализация MusicPlayer.
     */
    fun initialize() {
        if (isInitialized) return
        isInitialized = true
    }

    /**
     * Загрузить трек без воспроизведения.
     *
     * @param track Музыкальный трек для загрузки
     * @return true если загрузка успешна
     */
    suspend fun load(track: MusicTrack): Boolean = withContext(Dispatchers.IO) {
        try {
            // Освободить предыдущий плеер
            releaseMediaPlayer()

            // Создать новый MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_GAME)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = track.isLooping
                setVolume(_volume.value, _volume.value)

                // Загрузить из assets
                context.assets.openFd(track.assetPath).use { assetFd ->
                    setDataSource(assetFd.fileDescriptor, assetFd.startOffset, assetFd.length)
                    prepare()
                }

                // Обновить длительность трека если она не известна
                if (track.duration == 0L) {
                    _currentTrack.value = track.copy(duration = duration.toLong())
                } else {
                    _currentTrack.value = track
                }
            }

            _state.value = AudioState.Stopped
            true
        } catch (e: IOException) {
            e.printStackTrace()
            _currentTrack.value = null
            _state.value = AudioState.Idle
            false
        } catch (e: Exception) {
            e.printStackTrace()
            _currentTrack.value = null
            _state.value = AudioState.Idle
            false
        }
    }

    /**
     * Воспроизвести трек.
     *
     * @param track Музыкальный трек для воспроизведения
     * @param loop Зацикливать ли трек (по умолчанию true)
     * @param fade Плавное появление (по умолчанию true)
     * @return true если воспроизведение началось успешно
     */
    suspend fun play(
        track: MusicTrack,
        loop: Boolean = true,
        fade: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        // Если уже играет этот трек, ничего не делать
        if (_currentTrack.value?.id == track.id && _isPlaying.value) {
            return@withContext true
        }

        // Остановить текущий трек
        if (_isPlaying.value) {
            if (fade) {
                fadeOut()
            } else {
                stop()
            }
        }

        // Загрузить и воспроизвести новый трек
        val loaded = load(track.copy(isLooping = loop))
        if (!loaded) return@withContext false

        if (fade) {
            // Начать с тихой громкости и сделать fade in
            setVolume(0.0f)
            mediaPlayer?.start()
            _isPlaying.value = true
            _state.value = AudioState.Playing
            fadeIn()
        } else {
            mediaPlayer?.start()
            _isPlaying.value = true
            _state.value = AudioState.Playing
        }

        true
    }

    /**
     * Приостановить воспроизведение.
     */
    fun pause() {
        mediaPlayer?.apply {
            if (isPlaying) {
                pause()
                _isPlaying.value = false
                _state.value = AudioState.Paused
            }
        }
    }

    /**
     * Возобновить воспроизведение.
     */
    fun resume() {
        mediaPlayer?.apply {
            if (!_isPlaying.value) {
                start()
                _isPlaying.value = true
                _state.value = AudioState.Playing
            }
        }
    }

    /**
     * Остановить воспроизведение.
     */
    fun stop() {
        cancelFade()
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
                prepare() // Подготовка к повторному использованию
            }
            seekTo(0)
        }
        _isPlaying.value = false
        _state.value = AudioState.Stopped
    }

    /**
     * Установить громкость.
     *
     * @param volume Громкость от 0.0 до 1.0
     */
    fun setVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(AudioConstants.MIN_VOLUME, AudioConstants.MAX_VOLUME)
        _volume.value = clampedVolume
        mediaPlayer?.setVolume(clampedVolume, clampedVolume)
    }

    /**
     * Плавное изменение громкости до целевого значения.
     *
     * @param targetVolume Целевая громкость
     * @param duration Длительность затухания в мс
     */
    fun fadeTo(targetVolume: Float, duration: Long = AudioConstants.MUSIC_FADE_DURATION) {
        cancelFade()

        val startVolume = _volume.value
        val steps = AudioConstants.FADE_STEPS
        val stepVolume = (targetVolume - startVolume) / steps
        val stepDelay = duration / steps

        fadeJob = scope.launch {
            for (i in 1..steps) {
                val newVolume = (startVolume + stepVolume * i).coerceIn(
                    AudioConstants.MIN_VOLUME,
                    AudioConstants.MAX_VOLUME
                )
                setVolume(newVolume)
                delay(stepDelay)
            }
            setVolume(targetVolume)
        }
    }

    /**
     * Плавное появление (fade in).
     */
    private fun fadeIn() {
        fadeTo(_volume.value)
    }

    /**
     * Плавное затухание (fade out).
     */
    private fun fadeOut(duration: Long = AudioConstants.MUSIC_FADE_DURATION) {
        fadeTo(0.0f, duration)
    }

    /**
     * Отменить текущую fade операцию.
     */
    private fun cancelFade() {
        fadeJob?.cancel()
        fadeJob = null
    }

    /**
     * Получить текущую позицию воспроизведения.
     *
     * @return Позиция в миллисекундах
     */
    fun getPosition(): Long = mediaPlayer?.currentPosition?.toLong() ?: 0L

    /**
     * Перейти к указанной позиции.
     *
     * @param position Позиция в миллисекундах
     */
    fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position.toInt())
    }

    /**
     * Получить длительность трека.
     *
     * @return Длительность в миллисекундах
     */
    fun getDuration(): Long = mediaPlayer?.duration?.toLong() ?: 0L

    /**
     * Проверка, завершен ли трек.
     */
    fun isTrackComplete(): Boolean {
        val position = getPosition()
        val duration = getDuration()
        return duration > 0L && position >= duration
    }

    /**
     * Освободить ресурсы MediaPlayer.
     */
    private fun releaseMediaPlayer() {
        cancelFade()
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        _isPlaying.value = false
        _state.value = AudioState.Idle
    }

    /**
     * Освободить все ресурсы.
     */
    fun release() {
        releaseMediaPlayer()
        scope.launch {
            Job().cancel()
        }
        isInitialized = false
    }

    /**
     * Обработка паузы приложения.
     */
    fun onPause() {
        if (_isPlaying.value) {
            pause()
        }
    }

    /**
     * Обработка возобновления приложения.
     */
    fun onResume() {
        if (_state.value is AudioState.Paused) {
            resume()
        }
    }

    companion object {
        @Volatile
        private var instance: MusicPlayer? = null

        /**
         * Получить singleton экземпляр.
         */
        fun getInstance(context: Context): MusicPlayer {
            return instance ?: synchronized(this) {
                instance ?: MusicPlayer(context.applicationContext).also { instance = it }
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
