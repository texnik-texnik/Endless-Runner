package com.endlessrunner.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager as AndroidAudioManager
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.endlessrunner.managers.SaveManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Менеджер для управления звуковыми эффектами и вибрацией.
 *
 * Использует SoundPool для воспроизведения коротких звуковых эффектов.
 * Поддерживает настройку громкости и включение/выключение звуков.
 *
 * Интегрируется с новой системой AudioManager для обратной совместимости.
 *
 * @param context Контекст приложения
 * @param saveManager Менеджер сохранений
 */
class SoundManager(
    private val context: Context,
    private val saveManager: SaveManager
) {
    /**
     * SoundPool для воспроизведения звуков.
     */
    private val soundPool: SoundPool

    /**
     * Vibrator для вибрации.
     */
    private val vibrator: Vibrator

    /**
     * Android AudioManager для системной громкости.
     */
    private val systemAudioManager: AndroidAudioManager

    /**
     * CoroutineScope для асинхронных операций.
     */
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * StateFlow для включённости звуков.
     */
    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabledFlow: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    /**
     * StateFlow для включённости музыки.
     */
    private val _musicEnabled = MutableStateFlow(true)
    val musicEnabledFlow: StateFlow<Boolean> = _musicEnabled.asStateFlow()

    /**
     * StateFlow для включённости вибрации.
     */
    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabledFlow: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private var soundEnabled = true
    private var musicEnabled = true
    private var vibrationEnabled = true

    private var musicVolume = 0.6f
    private var sfxVolume = 0.7f

    /**
     * ID загруженных звуков.
     */
    private val soundIds = mutableMapOf<SoundType, Int>()

    /**
     * ID текущих воспроизведений для управления.
     */
    private val activeSounds = mutableListOf<Int>()

    init {
        // Инициализация SoundPool
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(AudioConstants.MAX_AUDIO_CHANNELS)
            .setAudioAttributes(audioAttributes)
            .build()

        // Инициализация Vibrator
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        // Инициализация системного AudioManager
        systemAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AndroidAudioManager

        // Загрузка настроек
        loadSettings()
    }

    /**
     * Загрузка настроек из SaveManager.
     */
    private fun loadSettings() {
        scope.launch {
            soundEnabled = saveManager.isSfxEnabled()
            musicEnabled = saveManager.isMusicEnabled()
            vibrationEnabled = saveManager.isVibrationEnabled()

            _soundEnabled.value = soundEnabled
            _musicEnabled.value = musicEnabled
            _vibrationEnabled.value = vibrationEnabled
        }
    }

    /**
     * Загрузка звукового эффекта.
     *
     * @param type Тип звука
     * @param resId ID ресурса
     */
    fun loadSound(type: SoundType, resId: Int) {
        val soundId = soundPool.load(context, resId, 1)
        soundIds[type] = soundId
    }

    /**
     * Загрузка звукового эффекта из пути.
     */
    fun loadSoundFromPath(type: SoundType, path: String) {
        try {
            context.assets.openFd(path).use { assetFd ->
                val soundId = soundPool.load(assetFd.fileDescriptor, 1)
                soundIds[type] = soundId
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Загрузка всех звуков.
     */
    fun loadAllSounds() {
        // Звуки игрока
        loadSound(SoundType.JUMP, android.R.raw.camera_click)
        loadSound(SoundType.LAND, android.R.raw.camera_shutter_click)
        loadSound(SoundType.HURT, android.R.raw.screen_load_complete)
        loadSound(SoundType.DIE, android.R.raw.lockscreen_failed)

        // Звуки предметов
        loadSound(SoundType.COIN, android.R.raw.notification_tick)
        loadSound(SoundType.POWERUP, android.R.raw.charging_started)

        // Звуки окружения
        loadSound(SoundType.EXPLOSION, android.R.raw.screen_load_complete)
        loadSound(SoundType.CRASH, android.R.raw.screen_lock_sound)
    }

    /**
     * Воспроизведение звукового эффекта.
     *
     * @param type Тип звука
     * @param volume Громкость
     */
    fun playSound(type: SoundType, volume: Float = 1f) {
        if (!soundEnabled) return

        soundIds[type]?.let { soundId ->
            val actualVolume = calculateActualVolume(volume * sfxVolume)
            val streamId = soundPool.play(
                soundId,
                actualVolume.left,
                actualVolume.right,
                AudioConstants.DEFAULT_PRIORITY,
                0,  // loop
                1f  // rate
            )
            activeSounds.add(streamId)
        }
    }

    /**
     * Остановка звукового эффекта.
     */
    fun stopSound(type: SoundType) {
        // SoundPool не позволяет остановить по soundId, только по streamId
    }

    /**
     * Остановка всех звуков.
     */
    fun stopAll() {
        activeSounds.forEach { streamId ->
            soundPool.stop(streamId)
        }
        activeSounds.clear()
    }

    // ==================== КОНВЕНИЕНС МЕТОДЫ ====================

    fun playJump() = playSound(SoundType.JUMP)
    fun playLand() = playSound(SoundType.LAND)
    fun playHurt() = playSound(SoundType.HURT)
    fun playDeath() = playSound(SoundType.DIE)
    fun playCoin() = playSound(SoundType.COIN)
    fun playPowerUp() = playSound(SoundType.POWERUP)
    fun playExplosion() = playSound(SoundType.EXPLOSION)
    fun playCrash() = playSound(SoundType.CRASH)
    fun playMenuSelect() = playSound(SoundType.MENU_SELECT)
    fun playMenuClick() = playSound(SoundType.MENU_CLICK)
    fun playLevelComplete() = playSound(SoundType.LEVEL_COMPLETE)
    fun playGameOver() = playSound(SoundType.GAME_OVER)

    // ==================== ВИБРАЦИЯ ====================

    /**
     * Вибрация с паттерном.
     */
    fun vibrate(pattern: LongArray, repeat: Int = -1) {
        if (!vibrationEnabled) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createWaveform(pattern, repeat)
                vibrator.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, repeat)
            }
        } catch (e: Exception) {
            // Вибрация может быть недоступна на некоторых устройствах
        }
    }

    /**
     * Вибрация с длительностью.
     */
    fun vibrate(duration: Long) {
        if (!vibrationEnabled) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } catch (e: Exception) {
            // Вибрация может быть недоступна
        }
    }

    fun vibrateJump() = vibrate(longArrayOf(0, 50), -1)
    fun vibrateHurt() = vibrate(longArrayOf(0, 100, 50, 100), -1)
    fun vibrateDeath() = vibrate(longArrayOf(0, 200, 100, 200, 100, 300), -1)
    fun vibrateCoin() = vibrate(30)

    // ==================== УПРАВЛЕНИЕ НАСТРОЙКАМИ ====================

    /**
     * Расчёт фактической громкости с учётом системной.
     */
    private fun calculateActualVolume(requestedVolume: Float): StereoVolume {
        val systemVolume = systemAudioManager.getStreamVolume(AndroidAudioManager.STREAM_MUSIC)
        val maxVolume = systemAudioManager.getStreamMaxVolume(AndroidAudioManager.STREAM_MUSIC)
        val systemVolumeRatio = if (maxVolume > 0) systemVolume.toFloat() / maxVolume else 1f

        val actualVolume = requestedVolume * systemVolumeRatio
        return StereoVolume(actualVolume, actualVolume)
    }

    fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
        _soundEnabled.value = enabled
        scope.launch {
            saveManager.setSfxEnabled(enabled)
        }
    }

    fun setMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        _musicEnabled.value = enabled
        scope.launch {
            saveManager.setMusicEnabled(enabled)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        vibrationEnabled = enabled
        _vibrationEnabled.value = enabled
        scope.launch {
            saveManager.setVibrationEnabled(enabled)
        }
    }

    fun isSoundEnabled(): Boolean = soundEnabled
    fun isMusicEnabled(): Boolean = musicEnabled
    fun isVibrationEnabled(): Boolean = vibrationEnabled

    fun setSfxVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0f, 1f)
    }

    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
    }

    // ==================== ИНТЕГРАЦИЯ С НОВОЙ СИСТЕМОЙ ====================

    /**
     * Воспроизвести звук через новую систему.
     */
    fun playSfxNew(soundId: String, volume: Float = 1.0f) {
        if (!soundEnabled) return
        AudioManager.getInstance(context).playSfx(soundId, volume)
    }

    /**
     * Воспроизвести музыку через новую систему.
     */
    suspend fun playMusicNew(trackId: String) {
        if (!musicEnabled) return
        AudioManager.getInstance(context).playMusic(trackId)
    }

    // ==================== ОСВОБОЖДЕНИЕ РЕСУРСОВ ====================

    /**
     * Освобождение ресурсов.
     */
    fun release() {
        stopAll()
        soundPool.release()
        vibrator.cancel()
        scope.cancel()
    }
}

/**
 * Типы звуковых эффектов.
 */
enum class SoundType {
    JUMP,
    LAND,
    HURT,
    DIE,
    COIN,
    POWERUP,
    EXPLOSION,
    CRASH,
    MENU_SELECT,
    MENU_CLICK,
    LEVEL_COMPLETE,
    GAME_OVER
}

/**
 * Стереогромкость (левый/правый канал).
 */
data class StereoVolume(
    val left: Float,
    val right: Float
)
