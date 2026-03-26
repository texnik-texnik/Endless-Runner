package com.endlessrunner.audio

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Главный контроллер аудио системы.
 *
 * Singleton для управления всей аудио подсистемой игры.
 * Координирует работу MusicPlayer и SoundEffectPool.
 *
 * @param context Контекст приложения
 */
class AudioManager private constructor(
    private val context: Context
) {
    /**
     * MusicPlayer для фоновой музыки.
     */
    private val musicPlayer: MusicPlayer

    /**
     * SoundEffectPool для звуковых эффектов.
     */
    private val soundEffectPool: SoundEffectPool

    /**
     * AudioMixer для управления каналами.
     */
    private val audioMixer: AudioMixer

    /**
     * CoroutineScope для асинхронных операций.
     */
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * Громкость master (общая).
     */
    private val _masterVolume = MutableStateFlow(AudioConstants.DEFAULT_MUSIC_VOLUME)
    val masterVolumeFlow: StateFlow<Float> = _masterVolume.asStateFlow()

    /**
     * Громкость музыки.
     */
    private val _musicVolume = MutableStateFlow(AudioConstants.DEFAULT_MUSIC_VOLUME)
    val musicVolumeFlow: StateFlow<Float> = _musicVolume.asStateFlow()

    /**
     * Громкость звуковых эффектов.
     */
    private val _sfxVolume = MutableStateFlow(AudioConstants.DEFAULT_SFX_VOLUME)
    val sfxVolumeFlow: StateFlow<Float> = _sfxVolume.asStateFlow()

    /**
     * Флаг mute.
     */
    private val _isMuted = MutableStateFlow(false)
    val isMutedFlow: StateFlow<Boolean> = _isMuted.asStateFlow()

    /**
     * Текущий музыкальный трек.
     */
    private val _currentMusic = MutableStateFlow<MusicTrack?>(null)
    val currentMusicFlow: StateFlow<MusicTrack?> = _currentMusic.asStateFlow()

    /**
     * Флаг инициализации.
     */
    private var isInitialized = false

    /**
     * Предыдущие значения громкости для mute/unmute.
     */
    private var previousMasterVolume = AudioConstants.DEFAULT_MUSIC_VOLUME

    init {
        musicPlayer = MusicPlayer.getInstance(context)
        soundEffectPool = SoundEffectPool.getInstance(context)
        audioMixer = AudioMixer.getInstance()

        // Подписка на изменения громкости музыки
        scope.launch {
            musicPlayer.volume.collect { volume ->
                _musicVolume.value = volume
            }
        }

        // Подписка на текущий трек
        scope.launch {
            musicPlayer.currentTrack.collect { track ->
                _currentMusic.value = track
            }
        }
    }

    /**
     * Инициализация аудио системы.
     */
    fun initialize() {
        if (isInitialized) return

        musicPlayer.initialize()
        isInitialized = true
    }

    // ==================== ГРОМКОСТЬ ====================

    /**
     * Установить общую громкость.
     *
     * @param volume Громкость от 0.0 до 1.0
     */
    fun setMasterVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(AudioConstants.MIN_VOLUME, AudioConstants.MAX_VOLUME)
        _masterVolume.value = clampedVolume
        previousMasterVolume = clampedVolume

        // Если включен mute, сохраняем значение но не применяем
        if (!_isMuted.value) {
            applyMasterVolume(clampedVolume)
        }
    }

    /**
     * Применить master громкость ко всем каналам.
     */
    private fun applyMasterVolume(volume: Float) {
        audioMixer.setChannelVolume(AudioChannel.MASTER, volume)
    }

    /**
     * Установить громкость музыки.
     *
     * @param volume Громкость от 0.0 до 1.0
     */
    fun setMusicVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(AudioConstants.MIN_VOLUME, AudioConstants.MAX_VOLUME)
        _musicVolume.value = clampedVolume
        musicPlayer.setVolume(clampedVolume)
    }

    /**
     * Установить громкость звуковых эффектов.
     *
     * @param volume Громкость от 0.0 до 1.0
     */
    fun setSfxVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(AudioConstants.MIN_VOLUME, AudioConstants.MAX_VOLUME)
        _sfxVolume.value = clampedVolume
        audioMixer.setChannelVolume(AudioChannel.SFX, clampedVolume)
    }

    /**
     * Заглушить всё аудио.
     */
    fun mute() {
        if (_isMuted.value) return

        _isMuted.value = true
        previousMasterVolume = _masterVolume.value
        applyMasterVolume(0.0f)
    }

    /**
     * Включить аудио после mute.
     */
    fun unmute() {
        if (!_isMuted.value) return

        _isMuted.value = false
        applyMasterVolume(previousMasterVolume)
    }

    /**
     * Переключить mute.
     */
    fun toggleMute() {
        if (_isMuted.value) {
            unmute()
        } else {
            mute()
        }
    }

    // ==================== МУЗЫКА ====================

    /**
     * Воспроизвести музыкальный трек.
     *
     * @param trackId ID трека из MusicLibrary
     * @param fade Плавное появление (по умолчанию true)
     */
    suspend fun playMusic(trackId: String, fade: Boolean = true) {
        if (_isMuted.value) return

        val track = MusicLibrary.getTrackById(trackId) ?: return
        playMusic(track, fade)
    }

    /**
     * Воспроизвести музыкальный трек.
     *
     * @param track Музыкальный трек
     * @param fade Плавное появление (по умолчанию true)
     */
    suspend fun playMusic(track: MusicTrack, fade: Boolean = true) {
        if (_isMuted.value) return

        musicPlayer.play(track, loop = track.isLooping, fade = fade)
    }

    /**
     * Остановить музыку.
     *
     * @param fade Плавное затухание (по умолчанию true)
     */
    fun stopMusic(fade: Boolean = true) {
        if (fade) {
            scope.launch {
                musicPlayer.fadeTo(0.0f, AudioConstants.MUSIC_FADE_DURATION)
                delay(AudioConstants.MUSIC_FADE_DURATION)
                musicPlayer.stop()
            }
        } else {
            musicPlayer.stop()
        }
    }

    /**
     * Приостановить музыку.
     */
    fun pauseMusic() {
        musicPlayer.pause()
    }

    /**
     * Возобновить музыку.
     */
    fun resumeMusic() {
        musicPlayer.resume()
    }

    /**
     * Получить текущий музыкальный трек.
     */
    fun getCurrentMusic(): MusicTrack? = _currentMusic.value

    /**
     * Проверка, воспроизводится ли музыка.
     */
    fun isMusicPlaying(): Boolean = musicPlayer.isPlaying.value

    // ==================== ЗВУКОВЫЕ ЭФФЕКТЫ ====================

    /**
     * Воспроизвести звуковой эффект.
     *
     * @param soundId ID звука из SoundLibrary
     * @param volume Громкость от 0.0 до 1.0 (по умолчанию 1.0)
     * @return streamId или -1 если ошибка
     */
    fun playSfx(soundId: String, volume: Float = 1.0f): Int {
        if (_isMuted.value) return -1

        val sound = SoundLibrary.getSoundById(soundId) ?: return -1
        return playSfx(sound, volume)
    }

    /**
     * Воспроизвести звуковой эффект с полными параметрами.
     *
     * @param soundId ID звука
     * @param volume Громкость
     * @param pitch Высота тона
     * @param pan Панорамирование
     * @return streamId или -1 если ошибка
     */
    fun playSfx(soundId: String, volume: Float, pitch: Float, pan: Float): Int {
        if (_isMuted.value) return -1

        val sound = SoundLibrary.getSoundById(soundId) ?: return -1
        return sound.play(volume, pitch, pan)
    }

    /**
     * Воспроизвести звуковой эффект.
     *
     * @param sound Звуковой эффект
     * @param volume Громкость от 0.0 до 1.0 (по умолчанию 1.0)
     * @return streamId или -1 если ошибка
     */
    fun playSfx(sound: SoundEffect, volume: Float = 1.0f): Int {
        if (_isMuted.value) return -1

        val sfxVolume = volume * _sfxVolume.value * audioMixer.getChannelVolume(AudioChannel.SFX)
        return sound.play(sfxVolume)
    }

    /**
     * Остановить звуковой эффект.
     *
     * @param soundId ID звука
     * @param streamId streamId для остановки
     */
    fun stopSfx(soundId: String, streamId: Int) {
        val sound = SoundLibrary.getSoundById(soundId)
        sound?.stop(streamId)
    }

    /**
     * Остановить все звуковые эффекты.
     */
    fun stopAllSfx() {
        SoundLibrary.ALL_SOUNDS.forEach { it.stopAll() }
    }

    /**
     * Предзагрузить звуки категории.
     *
     * @param category Категория звуков
     */
    fun preloadSfx(category: SoundLibrary.SoundCategory) {
        val sounds = SoundLibrary.getSoundsByCategory(category)
        sounds.forEach { sound ->
            if (!sound.isLoaded) {
                soundEffectPool.load(sound)
            }
        }
    }

    /**
     * Предзагрузить все звуки.
     */
    fun preloadAllSfx() {
        SoundLibrary.ALL_SOUNDS.forEach { sound ->
            if (!sound.isLoaded) {
                soundEffectPool.load(sound)
            }
        }
    }

    // ==================== УПРАВЛЕНИЕ ====================

    /**
     * Обработка паузы приложения.
     */
    fun onPause() {
        musicPlayer.onPause()
        soundEffectPool.onPause()
    }

    /**
     * Обработка возобновления приложения.
     */
    fun onResume() {
        musicPlayer.onResume()
        soundEffectPool.onResume()
    }

    /**
     * Освободить все ресурсы.
     */
    fun release() {
        scope.cancel()
        musicPlayer.release()
        soundEffectPool.release()
        isInitialized = false
    }

    companion object {
        @Volatile
        private var instance: AudioManager? = null

        /**
         * Получить singleton экземпляр.
         */
        fun getInstance(context: Context): AudioManager {
            return instance ?: synchronized(this) {
                instance ?: AudioManager(context.applicationContext).also { instance = it }
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
