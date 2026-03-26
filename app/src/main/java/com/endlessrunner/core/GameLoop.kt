package com.endlessrunner.core

import android.view.Choreographer
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.graphics.Canvas
import android.util.Log

/**
 * Главный игровой цикл с использованием SurfaceView и Choreographer.
 * Обеспечивает синхронизацию с VSync для стабильных 60 FPS.
 * 
 * @param surfaceView SurfaceView для рендеринга
 * @param timeProvider Провайдер времени
 */
class GameLoop(
    private val surfaceView: SurfaceView,
    private val timeProvider: TimeProvider = TimeProvider()
) : SurfaceHolder.Callback, Choreographer.FrameCallback {
    
    companion object {
        private const val TAG = "GameLoop"
        private const val DEBUG = false
    }
    
    /** SurfaceHolder для работы с SurfaceView */
    private val holder: SurfaceHolder = surfaceView.holder
    
    /** Флаг работы игрового цикла */
    @Volatile
    private var isRunning: Boolean = false
    
    /** Флаг паузы */
    @Volatile
    private var isPaused: Boolean = false
    
    /** Время последнего кадра в наносекундах */
    private var lastFrameTimeNs: Long = 0L
    
    /** Накопленное время для фиксированного шага физики */
    private var accumulator: Float = 0f
    
    /** Choreographer для синхронизации с VSync */
    private val choreographer: Choreographer = Choreographer.getInstance()
    
    /** Слушатель обновления игры */
    var gameUpdateListener: GameUpdateListener? = null
    
    /** Слушатель рендеринга */
    var gameRenderListener: GameRenderListener? = null
    
    /** Текущее состояние цикла */
    val state: GameLoopState
        get() = when {
            isRunning && !isPaused -> GameLoopState.RUNNING
            isRunning && isPaused -> GameLoopState.PAUSED
            else -> GameLoopState.STOPPED
        }
    
    /** Статистика FPS */
    private var fpsCounter: FpsCounter = FpsCounter()
    
    init {
        holder.addCallback(this)
        surfaceView.isFocusable = true
        surfaceView.isFocusableInTouchMode = true
    }
    
    // ============================================================================
    // УПРАВЛЕНИЕ ЦИКЛОМ
    // ============================================================================
    
    /**
     * Запуск игрового цикла.
     */
    fun start() {
        if (isRunning) {
            Log.w(TAG, "GameLoop уже запущен")
            return
        }
        
        Log.d(TAG, "Запуск GameLoop")
        isRunning = true
        isPaused = false
        lastFrameTimeNs = System.nanoTime()
        accumulator = 0f
        timeProvider.reset()
        fpsCounter.reset()
        
        // Запуск цикла через Choreographer
        choreographer.postFrameCallback(this)
    }
    
    /**
     * Остановка игрового цикла.
     */
    fun stop() {
        if (!isRunning) return
        
        Log.d(TAG, "Остановка GameLoop")
        isRunning = false
        isPaused = false
        
        // Остановка callback
        choreographer.removeFrameCallback(this)
        
        // Уведомление слушателей
        gameUpdateListener?.onGameStop()
        gameRenderListener?.onRenderStop()
    }
    
    /**
     * Пауза игрового цикла.
     */
    fun pause() {
        if (!isRunning || isPaused) return
        
        Log.d(TAG, "Пауза GameLoop")
        isPaused = true
        timeProvider.pause()
        
        // Уведомление слушателей
        gameUpdateListener?.onGamePause()
    }
    
    /**
     * Возобновление игрового цикла.
     */
    fun resume() {
        if (!isRunning || !isPaused) return
        
        Log.d(TAG, "Возобновление GameLoop")
        isPaused = false
        lastFrameTimeNs = System.nanoTime()
        timeProvider.resume()
        
        // Перезапуск цикла
        choreographer.postFrameCallback(this)
        
        // Уведомление слушателей
        gameUpdateListener?.onGameResume()
    }
    
    // ============================================================================
    // ИГРОВОЙ ЦИКЛ
    // ============================================================================
    
    /**
     * Callback от Choreographer.
     * Вызывается перед каждым кадром (синхронизировано с VSync).
     * 
     * @param frameTimeNs Время кадра в наносекундах
     */
    override fun doFrame(frameTimeNs: Long) {
        if (!isRunning) return
        
        // Планируем следующий кадр
        if (!isPaused) {
            choreographer.postFrameCallback(this)
        }
        
        // Расчёт deltaTime
        val deltaTimeNs = frameTimeNs - lastFrameTimeNs
        lastFrameTimeNs = frameTimeNs
        
        // Конвертация в секунды с ограничением
        var deltaTime = deltaTimeNs / 1_000_000_000f
        deltaTime = deltaTime.coerceIn(GameConstants.MIN_DELTA_TIME, GameConstants.MAX_DELTA_TIME)
        
        // Обновление TimeProvider
        timeProvider.update(frameTimeNs)
        
        // Обновление счётчика FPS
        fpsCounter.update()
        
        if (!isPaused) {
            // Фиксированный шаг для физики
            updateWithFixedTimestep(deltaTime)
            
            // Рендеринг
            render()
        }
    }
    
    /**
     * Обновление с фиксированным шагом времени.
     * Обеспечивает детерминированную физику.
     * 
     * @param deltaTime Реальное время прошедшее с последнего кадра
     */
    private fun updateWithFixedTimestep(deltaTime: Float) {
        accumulator += deltaTime
        
        // Выполняем фиксированные шаги пока accumulator не станет меньше fixedTimeStep
        while (accumulator >= GameConstants.FIXED_TIME_STEP) {
            update(GameConstants.FIXED_TIME_STEP)
            accumulator -= GameConstants.FIXED_TIME_STEP
        }
    }
    
    /**
     * Метод обновления игровой логики.
     * Вызывается с фиксированным шагом времени.
     * 
     * @param deltaTime Время в секундах с последнего обновления
     */
    private fun update(deltaTime: Float) {
        try {
            gameUpdateListener?.onUpdate(deltaTime)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка в onUpdate", e)
        }
    }
    
    /**
     * Метод рендеринга.
     * Вызывается каждый кадр.
     */
    private fun render() {
        var canvas: Canvas? = null
        try {
            // Блокировка поверхности для рисования
            canvas = holder.lockCanvas()
            
            // Рендеринг
            gameRenderListener?.onRender(canvas)
            
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка в onRender", e)
        } finally {
            // Разблокировка и отрисовка
            try {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при разблокировке canvas", e)
            }
        }
    }
    
    // ============================================================================
    // SURFACEHOLDER CALLBACK
    // ============================================================================
    
    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "Surface created")
        gameRenderListener?.onSurfaceCreated()
    }
    
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "Surface changed: ${width}x${height}")
        gameRenderListener?.onSurfaceChanged(width, height)
    }
    
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "Surface destroyed")
        // Автоматическая пауза при уничтожении поверхности
        pause()
        gameRenderListener?.onSurfaceDestroyed()
    }
    
    // ============================================================================
    // СТАТИСТИКА
    // ============================================================================
    
    /**
     * Получение текущего FPS.
     */
    fun getCurrentFps(): Float = fpsCounter.currentFps
    
    /**
     * Получение среднего FPS.
     */
    fun getAverageFps(): Float = fpsCounter.averageFps
    
    /**
     * Получение времени кадра в миллисекундах.
     */
    fun getFrameTimeMs(): Float = 1000f / fpsCounter.currentFps
    
    // ============================================================================
    // ИНТЕРФЕЙСЫ
    // ============================================================================
    
    /**
     * Слушатель обновления игры.
     */
    interface GameUpdateListener {
        fun onUpdate(deltaTime: Float)
        fun onGamePause() {}
        fun onGameResume() {}
        fun onGameStop() {}
    }
    
    /**
     * Слушатель рендеринга.
     */
    interface GameRenderListener {
        fun onRender(canvas: Canvas)
        fun onSurfaceCreated() {}
        fun onSurfaceChanged(width: Int, height: Int) {}
        fun onSurfaceDestroyed() {}
        fun onRenderStop() {}
    }
    
    /**
     * Состояние игрового цикла.
     */
    enum class GameLoopState {
        RUNNING,
        PAUSED,
        STOPPED
    }
}

/**
 * Счётчик FPS для статистики.
 */
class FpsCounter {
    
    /** Текущий FPS */
    var currentFps: Float = 0f
        private set
    
    /** Средний FPS */
    var averageFps: Float = 0f
        private set
    
    /** Время последнего подсчёта */
    private var lastTimeNs: Long = 0L
    
    /** Количество кадров с последнего подсчёта */
    private var frameCount: Int = 0
    
    /** Интервал подсчёта FPS (в наносекундах) */
    private val fpsUpdateIntervalNs: Long = 500_000_000L // 500ms
    
    /** Сумма FPS для среднего */
    private var fpsSum: Float = 0f
    
    /** Количество измерений для среднего */
    private var fpsMeasurements: Int = 0
    
    fun reset() {
        lastTimeNs = 0L
        frameCount = 0
        currentFps = 0f
        averageFps = 0f
        fpsSum = 0f
        fpsMeasurements = 0
    }
    
    fun update(currentTimeNs: Long = System.nanoTime()) {
        frameCount++
        
        if (lastTimeNs == 0L) {
            lastTimeNs = currentTimeNs
            return
        }
        
        val elapsedNs = currentTimeNs - lastTimeNs
        
        if (elapsedNs >= fpsUpdateIntervalNs) {
            // Расчёт FPS
            currentFps = frameCount * 1_000_000_000f / elapsedNs
            
            // Обновление среднего
            fpsSum += currentFps
            fpsMeasurements++
            averageFps = fpsSum / fpsMeasurements
            
            // Сброс счётчиков
            frameCount = 0
            lastTimeNs = currentTimeNs
        }
    }
}

/**
 * Extension property для проверки состояния.
 */
val GameLoop.isRunningState: Boolean
    get() = state == GameLoop.GameLoopState.RUNNING

/**
 * Extension property для проверки паузы.
 */
val GameLoop.isPausedState: Boolean
    get() = state == GameLoop.GameLoopState.PAUSED
