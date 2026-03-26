package com.endlessrunner.animation

import android.graphics.Bitmap

/**
 * Базовый класс спрайтовой анимации.
 * Управляет последовательностью кадров и их отображением.
 *
 * @param frames Список кадров анимации (Bitmap)
 * @param frameDuration Длительность одного кадра в секундах
 * @param isLooping Зациклена ли анимация
 */
class Animation(
    /** Список кадров анимации */
    val frames: List<Bitmap>,
    
    /** Длительность одного кадра в секундах */
    var frameDuration: Float = 0.1f,
    
    /** Зациклена ли анимация */
    var isLooping: Boolean = true
) {
    /** Индекс текущего кадра */
    var currentFrame: Int = 0
        private set
    
    /** Накопленное время для переключения кадров */
    var elapsedTime: Float = 0f
        private set
    
    /** Запущена ли анимация */
    var isPlaying: Boolean = false
        private set
    
    /** Общее количество кадров */
    val frameCount: Int
        get() = frames.size
    
    /** Длительность всей анимации в секундах */
    val totalDuration: Float
        get() = frameCount * frameDuration
    
    /** Текущий прогресс анимации (0..1) */
    val progress: Float
        get() = if (frameCount <= 1) 1f else currentFrame.toFloat() / (frameCount - 1)
    
    /**
     * Обновление анимации.
     *
     * @param deltaTime Время, прошедшее с последнего кадра (в секундах)
     */
    fun update(deltaTime: Float) {
        if (!isPlaying || frameCount <= 1) return
        
        elapsedTime += deltaTime
        
        // Проверка на завершение не-зацикленной анимации
        if (!isLooping && elapsedTime >= totalDuration) {
            currentFrame = frameCount - 1
            elapsedTime = totalDuration
            isPlaying = false
            return
        }
        
        // Вычисление текущего кадра
        if (isLooping) {
            // Для зацикленной анимации используем остаток от деления
            val totalTime = elapsedTime % totalDuration
            currentFrame = (totalTime / frameDuration).toInt().coerceIn(0, frameCount - 1)
        } else {
            currentFrame = (elapsedTime / frameDuration).toInt().coerceIn(0, frameCount - 1)
        }
    }
    
    /**
     * Получение текущего кадра.
     *
     * @return Bitmap текущего кадра или null если кадров нет
     */
    fun getCurrentFrame(): Bitmap? {
        if (frames.isEmpty()) return null
        return frames[currentFrame.coerceIn(0, frameCount - 1)]
    }
    
    /**
     * Получение кадра по индексу.
     *
     * @param index Индекс кадра
     * @return Bitmap кадра или null
     */
    fun getFrame(index: Int): Bitmap? {
        if (index < 0 || index >= frameCount) return null
        return frames[index]
    }
    
    /**
     * Запуск анимации.
     */
    fun start() {
        isPlaying = true
        elapsedTime = 0f
        currentFrame = 0
    }
    
    /**
     * Остановка анимации.
     *
     * @param resetFrame Сбросить ли кадр на первый
     */
    fun stop(resetFrame: Boolean = false) {
        isPlaying = false
        if (resetFrame) {
            elapsedTime = 0f
            currentFrame = 0
        }
    }
    
    /**
     * Сброс анимации к началу.
     */
    fun reset() {
        elapsedTime = 0f
        currentFrame = 0
        isPlaying = false
    }
    
    /**
     * Проверка, завершена ли анимация.
     *
     * @return true если анимация не зациклена и дошла до конца
     */
    fun isFinished(): Boolean {
        return !isLooping && (currentFrame >= frameCount - 1 || !isPlaying)
    }
    
    /**
     * Установка конкретного кадра.
     *
     * @param frameIndex Индекс кадра
     */
    fun setFrame(frameIndex: Int) {
        currentFrame = frameIndex.coerceIn(0, frameCount - 1)
        elapsedTime = currentFrame * frameDuration
    }
    
    /**
     * Клонирование анимации.
     *
     * @return Новая копия анимации
     */
    fun copy(): Animation {
        return Animation(
            frames = frames,
            frameDuration = frameDuration,
            isLooping = isLooping
        )
    }
    
    override fun toString(): String {
        return "Animation(frames=$frameCount, duration=$totalDuration, looping=$isLooping, playing=$isPlaying)"
    }
}

/**
 * Extension функция для создания анимации из массива Bitmap.
 */
fun animationOf(
    vararg frames: Bitmap,
    frameDuration: Float = 0.1f,
    isLooping: Boolean = true
): Animation {
    return Animation(frames.toList(), frameDuration, isLooping)
}

/**
 * Extension функция для создания анимации с заданной частотой кадров.
 *
 * @param fps Кадры в секунду
 */
fun List<Bitmap>.toAnimation(
    fps: Float = 10f,
    isLooping: Boolean = true
): Animation {
    return Animation(this, frameDuration = 1f / fps, isLooping = isLooping)
}

/**
 * Extension функция для реверса анимации.
 */
fun Animation.reversed(): Animation {
    return Animation(frames.reversed(), frameDuration, isLooping)
}
