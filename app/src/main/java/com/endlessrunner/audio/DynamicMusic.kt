package com.endlessrunner.audio

import com.endlessrunner.powerups.PowerUpType
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
 * Система адаптивной динамической музыки.
 *
 * Изменяет музыкальное сопровождение в зависимости от игровых событий:
 * - Уровень здоровья игрока
 * - Комбо счёт
 * - Битва с боссом
 * - Собранные бонусы
 *
 * @param audioManager Менеджер аудио
 */
class DynamicMusic private constructor(
    private val audioManager: AudioManager
) {
    /**
     * CoroutineScope для асинхронных операций.
     */
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * Текущий уровень интенсивности (0-3).
     */
    private val _intensityLevel = MutableStateFlow(0)
    val intensityLevel: StateFlow<Int> = _intensityLevel.asStateFlow()

    /**
     * Текущий трек геймплея.
     */
    private var currentGameplayTrack: MusicTrack = MusicLibrary.GAMEPLAY_1

    /**
     * Флаг низкой здоровья.
     */
    private var isLowHealth = false

    /**
     * Флаг активной битвы с боссом.
     */
    private var isBossFightActive = false

    /**
     * Текущий комбо множитель.
     */
    private var comboMultiplier = 1.0f

    /**
     * Job для таймера возврата интенсивности.
     */
    private var intensityTimerJob: Job? = null

    /**
     * Базовый трек геймплея.
     */
    private var baseTrack: MusicTrack = MusicLibrary.GAMEPLAY_1

    /**
     * Установить уровень интенсивности музыки.
     *
     * @param level Уровень от 0 (спокойный) до 3 (максимальный)
     */
    fun setIntensity(level: Int) {
        val clampedLevel = level.coerceIn(0, 3)
        _intensityLevel.value = clampedLevel

        // Отмена предыдущего таймера
        intensityTimerJob?.cancel()

        when (clampedLevel) {
            0 -> currentGameplayTrack = MusicLibrary.GAMEPLAY_1
            1 -> currentGameplayTrack = MusicLibrary.GAMEPLAY_1
            2 -> currentGameplayTrack = MusicLibrary.GAMEPLAY_2
            3 -> currentGameplayTrack = MusicLibrary.GAMEPLAY_2
        }

        // Плавный переход к новому треку
        if (audioManager.isMusicPlaying() && !isBossFightActive) {
            scope.launch {
                audioManager.playMusic(currentGameplayTrack, fade = true)
            }
        }

        // Таймер возврата к базовой интенсивности
        startIntensityTimer()
    }

    /**
     * Запустить таймер возврата интенсивности.
     */
    private fun startIntensityTimer() {
        intensityTimerJob?.cancel()
        intensityTimerJob = scope.launch {
            delay(5000) // 5 секунд до возврата
            if (!isBossFightActive && !isLowHealth) {
                _intensityLevel.value = 0
                currentGameplayTrack = baseTrack
            }
        }
    }

    /**
     * Событие низкого здоровья игрока.
     * Включает напряжённую музыку.
     */
    fun onLowHealth() {
        if (isBossFightActive) return // Не перебивать музыку босса

        isLowHealth = true
        intensityTimerJob?.cancel()

        scope.launch {
            // Можно добавить специальный трек для низкого здоровья
            setIntensity(3)
        }

        // Сброс флага через время
        scope.launch {
            delay(10000) // 10 секунд
            isLowHealth = false
            if (!isBossFightActive) {
                setIntensity(0)
            }
        }
    }

    /**
     * Событие комбо.
     *
     * @param combo Текущий комбо счёт
     */
    fun onCombo(combo: Int) {
        if (isBossFightActive) return

        // Увеличение интенсивности с ростом комбо
        val intensity = when {
            combo >= 100 -> 3
            combo >= 50 -> 2
            combo >= 25 -> 1
            else -> 0
        }

        comboMultiplier = 1.0f + (combo.toFloat() / 100.0f)
        setIntensity(intensity)
    }

    /**
     * Событие начала битвы с боссом.
     */
    fun onBossFight() {
        isBossFightActive = true
        intensityTimerJob?.cancel()

        scope.launch {
            audioManager.playMusic(MusicLibrary.BOSS_BATTLE, fade = true)
        }
    }

    /**
     * Событие окончания битвы с боссом.
     */
    fun onBossFightEnd() {
        isBossFightActive = false
        isLowHealth = false
        comboMultiplier = 1.0f

        scope.launch {
            audioManager.playMusic(baseTrack, fade = true)
        }
    }

    /**
     * Событие сбора бонуса.
     *
     * @param type Тип бонуса
     */
    fun onPowerUpCollected(type: PowerUpType) {
        when (type) {
            PowerUpType.SPEED -> {
                // Ускорение темпа музыки
                setIntensity(_intensityLevel.value.coerceAtLeast(1))
            }
            PowerUpType.INVINCIBILITY -> {
                // Максимальная интенсивность
                setIntensity(3)
            }
            else -> {
                // Небольшое увеличение интенсивности
                setIntensity(_intensityLevel.value.coerceIn(0, 3) + 1)
            }
        }
    }

    /**
     * Событие ускорения (speed boost).
     */
    fun onSpeedBoost() {
        setIntensity(_intensityLevel.value.coerceAtLeast(2))
    }

    /**
     * Сбросить состояние динамической музыки.
     */
    fun reset() {
        isLowHealth = false
        isBossFightActive = false
        comboMultiplier = 1.0f
        _intensityLevel.value = 0
        currentGameplayTrack = baseTrack
        intensityTimerJob?.cancel()
    }

    /**
     * Установить базовый трек.
     *
     * @param track Базовый трек
     */
    fun setBaseTrack(track: MusicTrack) {
        baseTrack = track
        if (!isBossFightActive && !isLowHealth) {
            currentGameplayTrack = track
        }
    }

    /**
     * Получить текущий трек.
     */
    fun getCurrentTrack(): MusicTrack = currentGameplayTrack

    /**
     * Получить множитель комбо.
     */
    fun getComboMultiplier(): Float = comboMultiplier

    /**
     * Проверка, активна ли битва с боссом.
     */
    fun isBossFight(): Boolean = isBossFightActive

    /**
     * Проверка, низкое ли здоровье.
     */
    fun isLowHealthActive(): Boolean = isLowHealth

    /**
     * Освободить ресурсы.
     */
    fun release() {
        scope.cancel()
        reset()
    }

    companion object {
        @Volatile
        private var instance: DynamicMusic? = null

        /**
         * Получить singleton экземпляр.
         */
        fun getInstance(audioManager: AudioManager): DynamicMusic {
            return instance ?: synchronized(this) {
                instance ?: DynamicMusic(audioManager).also { instance = it }
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
