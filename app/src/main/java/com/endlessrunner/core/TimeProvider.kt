package com.endlessrunner.core

/**
 * Абстракция времени для игрового цикла.
 * Позволяет легко тестировать игру с ручным управлением временем.
 * 
 * @property isManualMode Если true, время управляется вручную (для тестов)
 */
class TimeProvider(
    private var isManualMode: Boolean = false
) {
    
    /** Время последнего кадра в наносекундах */
    private var lastFrameTimeNs: Long = 0L
    
    /** Общее время игры в наносекундах */
    private var totalElapsedTimeNs: Long = 0L
    
    /** deltaTime последнего кадра в секундах */
    private var lastDeltaTime: Float = 0f
    
    /** Флаг паузы времени */
    private var isPaused: Boolean = false
    
    /** Ручное значение deltaTime для тестов (в секундах) */
    private var manualDeltaTime: Float = 0f
    
    /**
     * Текущее значение deltaTime в секундах.
     * Возвращает ручное значение в режиме тестов.
     */
    fun getDeltaTime(): Float = if (isManualMode) {
        manualDeltaTime
    } else {
        lastDeltaTime
    }
    
    /**
     * Общее время игры в секундах.
     */
    fun getElapsedTime(): Float = if (isManualMode) {
        totalElapsedTimeNs / 1_000_000_000f
    } else {
        totalElapsedTimeNs / 1_000_000_000f
    }
    
    /**
     * Общее время игры в миллисекундах.
     */
    fun getElapsedTimeMs(): Long = totalElapsedTimeNs / 1_000_000L
    
    /**
     * Обновление времени. Вызывается каждый кадр.
     * 
     * @param currentTimeNs Текущее время в наносекундах (System.nanoTime())
     * @return deltaTime в секундах
     */
    fun update(currentTimeNs: Long = System.nanoTime()): Float {
        if (isManualMode || isPaused) {
            return getDeltaTime()
        }
        
        if (lastFrameTimeNs == 0L) {
            // Первый кадр
            lastFrameTimeNs = currentTimeNs
            lastDeltaTime = 0f
            return 0f
        }
        
        // Расчёт deltaTime в наносекундах
        val deltaTimeNs = currentTimeNs - lastFrameTimeNs
        
        // Ограничение deltaTime для предотвращения скачков
        val clampedDeltaNs = deltaTimeNs.coerceAtMost(
            (GameConstants.MAX_DELTA_TIME * 1_000_000_000).toLong()
        )
        
        // Конвертация в секунды
        lastDeltaTime = clampedDeltaNs / 1_000_000_000f
        
        // Обновление общего времени
        totalElapsedTimeNs += deltaTimeNs
        
        // Обновление времени последнего кадра
        lastFrameTimeNs = currentTimeNs
        
        return lastDeltaTime
    }
    
    /**
     * Сброс таймеров.
     */
    fun reset() {
        lastFrameTimeNs = 0L
        totalElapsedTimeNs = 0L
        lastDeltaTime = 0f
        isPaused = false
    }
    
    /**
     * Установка ручного режима для тестов.
     * 
     * @param enabled Включить/выключить ручной режим
     * @param deltaTime Значение deltaTime для использования в ручном режиме
     */
    fun setManualMode(enabled: Boolean, deltaValue: Float = GameConstants.FIXED_TIME_STEP) {
        isManualMode = enabled
        manualDeltaTime = deltaValue
        if (enabled && lastFrameTimeNs == 0L) {
            lastFrameTimeNs = System.nanoTime()
        }
    }
    
    /**
     * Продвижение времени вручную (для тестов).
     * 
     * @param deltaSeconds Время для продвижения в секундах
     */
    fun advanceTime(deltaSeconds: Float) {
        if (!isManualMode) {
            throw IllegalStateException("advanceTime можно вызывать только в ручном режиме")
        }
        manualDeltaTime = deltaSeconds
        totalElapsedTimeNs += (deltaSeconds * 1_000_000_000).toLong()
    }
    
    /**
     * Пауза времени.
     */
    fun pause() {
        isPaused = true
    }
    
    /**
     * Возобновление времени.
     */
    fun resume() {
        if (isPaused) {
            isPaused = false
            lastFrameTimeNs = System.nanoTime()
        }
    }
    
    /**
     * Проверка, находится ли время на паузе.
     */
    fun isTimePaused(): Boolean = isPaused
    
    /**
     * Проверка, включён ли ручной режим.
     */
    fun isManual(): Boolean = isManualMode
    
    companion object {
        /**
         * Создание TimeProvider в автоматическом режиме (по умолчанию).
         */
        fun auto(): TimeProvider = TimeProvider(isManualMode = false)
        
        /**
         * Создание TimeProvider в ручном режиме для тестов.
         * 
         * @param deltaTime Значение deltaTime для использования
         */
        fun manual(deltaTime: Float = GameConstants.FIXED_TIME_STEP): TimeProvider {
            return TimeProvider(isManualMode = true).apply {
                manualDeltaTime = deltaTime
            }
        }
    }
}

/**
 * Расширение для получения фиксированного deltaTime.
 * Используется в системах с фиксированным шагом времени.
 */
fun TimeProvider.getFixedDeltaTime(): Float = GameConstants.FIXED_TIME_STEP

/**
 * Расширение для конвертации deltaTime в миллисекунды.
 */
fun TimeProvider.getDeltaTimeMs(): Float = getDeltaTime() * 1000f
