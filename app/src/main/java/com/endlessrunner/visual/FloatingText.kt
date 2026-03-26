package com.endlessrunner.visual

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.endlessrunner.core.PooledObject

/**
 * Всплывающий текст.
 * Отображает анимированный текст с появлением, задержкой и исчезновением.
 *
 * @param text Текст для отображения
 * @param x Позиция X
 * @param y Позиция Y
 * @param color Цвет текста
 * @param textSize Размер шрифта
 * @param lifetime Время жизни (секунды)
 */
class FloatingText(
    /** Текст для отображения */
    var text: String = "",
    
    /** Позиция X */
    var x: Float = 0f,
    
    /** Позиция Y */
    var y: Float = 0f,
    
    /** Цвет текста */
    var color: Int = Color.WHITE,
    
    /** Размер шрифта */
    var textSize: Float = 32f,
    
    /** Полное время жизни (секунды) */
    var lifetime: Float = 1.5f,
    
    /** Время задержки перед исчезновением */
    var delay: Float = 0.5f,
    
    /** Скорость всплытия (пикселей в секунду) */
    var floatSpeed: Float = 50f,
    
    /** Скорость горизонтального движения */
    var horizontalSpeed: Float = 0f,
    
    /** Конечный масштаб (для анимации появления) */
    var endScale: Float = 1f,
    
    /** Начальный масштаб */
    var startScale: Float = 0f
) : PooledObject {
    
    companion object {
        /** Paint для отрисовки (общий для всех текстов) */
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isFakeBoldText = true
        }
        
        /** Paint для тени */
        private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isFakeBoldText = true
        }
    }
    
    /** Оставшееся время жизни */
    var timeRemaining: Float = lifetime
        private set
    
    /** Текущий масштаб */
    var currentScale: Float = startScale
        private set
    
    /** Текущая позиция Y */
    private var startY: Float = 0f
    
    /** Текущая позиция X */
    private var startX: Float = 0f
    
    /** Активен ли текст */
    override var isActive: Boolean = false
        private set
    
    /** Фаза анимации */
    private var phase: Phase = Phase.APPEARING
    
    /** Альфа-канал */
    private var alpha: Int = 255
    
    /**
     * Инициализация текста.
     */
    fun show(
        text: String,
        x: Float,
        y: Float,
        color: Int = Color.WHITE,
        textSize: Float = 32f,
        lifetime: Float = 1.5f
    ) {
        this.text = text
        this.x = x
        this.y = y
        this.color = color
        this.textSize = textSize
        this.lifetime = lifetime
        this.startY = y
        this.startX = x
        this.timeRemaining = lifetime
        this.currentScale = startScale
        this.alpha = 255
        this.isActive = true
        this.phase = Phase.APPEARING
    }
    
    /**
     * Обновление текста.
     *
     * @param deltaTime Время с последнего кадра
     */
    fun update(deltaTime: Float) {
        if (!isActive) return
        
        timeRemaining -= deltaTime
        
        when (phase) {
            Phase.APPEARING -> {
                // Анимация появления (scale up)
                val appearDuration = 0.2f
                val progress = (lifetime - timeRemaining) / appearDuration
                
                currentScale = lerp(startScale, endScale, progress.coerceIn(0f, 1f))
                
                if (progress >= 1f) {
                    phase = Phase.WAITING
                }
            }
            
            Phase.WAITING -> {
                // Ожидание
                if (timeRemaining <= lifetime - delay) {
                    phase = Phase.DISAPPEARING
                }
            }
            
            Phase.DISAPPEARING -> {
                // Анимация исчезновения (fade out + move up)
                val disappearDuration = lifetime - delay
                val progress = 1f - (timeRemaining / disappearDuration)
                
                currentScale = lerp(endScale, endScale * 1.2f, progress)
                alpha = (255 * (1f - progress)).toInt().coerceIn(0, 255)
                
                // Всплытие
                y = startY - (lifetime - timeRemaining) * floatSpeed
                
                // Горизонтальное движение
                x = startX + (lifetime - timeRemaining) * horizontalSpeed
            }
        }
        
        // Проверка завершения
        if (timeRemaining <= 0f) {
            isActive = false
        }
    }
    
    /**
     * Отрисовка текста.
     */
    fun render(canvas: Canvas) {
        if (!isActive) return
        
        canvas.save()
        
        try {
            // Трансформация
            canvas.translate(x, y)
            canvas.scale(currentScale, currentScale)
            
            // Настройка paint
            textPaint.textSize = textSize
            textPaint.color = color
            textPaint.alpha = alpha
            
            shadowPaint.textSize = textSize
            shadowPaint.color = Color.BLACK
            shadowPaint.alpha = alpha / 2
            
            // Отрисовка тени
            canvas.drawText(text, 2f, 2f, shadowPaint)
            
            // Отрисовка текста
            canvas.drawText(text, 0f, 0f, textPaint)
            
        } finally {
            canvas.restore()
        }
    }
    
    /**
     * Сброс текста.
     */
    override fun reset() {
        text = ""
        x = 0f
        y = 0f
        color = Color.WHITE
        textSize = 32f
        lifetime = 1.5f
        timeRemaining = 0f
        currentScale = 0f
        startY = 0f
        startX = 0f
        isActive = false
        phase = Phase.APPEARING
        alpha = 255
    }
    
    /**
     * Проверка, завершён ли текст.
     */
    fun isFinished(): Boolean = !isActive
    
    /**
     * Фаза анимации.
     */
    private enum class Phase {
        APPEARING,    // Появление
        WAITING,      // Ожидание
        DISAPPEARING  // Исчезновение
    }
    
    /**
     * Линейная интерполяция.
     */
    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t
    }
}

/**
 * Менеджер всплывающих текстов.
 * Управляет созданием и обновлением текстов.
 */
class FloatingTextManager {
    
    /** Активные тексты */
    private val texts: MutableList<FloatingText> = mutableListOf()
    
    /** Пул текстов */
    private val pool: MutableList<FloatingText> = mutableListOf()
    
    /** Максимальное количество текстов */
    var maxTexts: Int = 50
    
    /**
     * Показ текста.
     */
    fun show(
        text: String,
        x: Float,
        y: Float,
        color: Int = Color.WHITE,
        textSize: Float = 32f,
        lifetime: Float = 1.5f
    ): FloatingText {
        val floatingText = acquireFromPool()
        floatingText.show(text, x, y, color, textSize, lifetime)
        texts.add(floatingText)
        return floatingText
    }
    
    /**
     * Показ текста с получением урона.
     */
    fun showDamage(
        damage: Int,
        x: Float,
        y: Float,
        isCritical: Boolean = false
    ): FloatingText {
        val color = if (isCritical) {
            Color.parseColor("#FF4500")
        } else {
            Color.parseColor("#FF0000")
        }
        
        val size = if (isCritical) 48f else 36f
        
        return show("-$damage", x, y, color, size)
    }
    
    /**
     * Показ текста с лечением.
     */
    fun showHeal(
        amount: Int,
        x: Float,
        y: Float
    ): FloatingText {
        return show("+$amount", x, y, Color.parseColor("#00FF00"), 36f)
    }
    
    /**
     * Показ текста сбора монет.
     */
    fun showCoin(
        amount: Int,
        x: Float,
        y: Float
    ): FloatingText {
        return show("+$amount", x, y, Color.parseColor("#FFD700"), 32f)
    }
    
    /**
     * Показ обычного текста.
     */
    fun showText(
        text: String,
        x: Float,
        y: Float,
        color: Int = Color.WHITE
    ): FloatingText {
        return show(text, x, y, color)
    }
    
    /**
     * Обновление всех текстов.
     */
    fun update(deltaTime: Float) {
        val iterator = texts.iterator()
        
        while (iterator.hasNext()) {
            val text = iterator.next()
            
            if (text.isActive) {
                text.update(deltaTime)
            } else {
                releaseToPool(text)
                iterator.remove()
            }
        }
    }
    
    /**
     * Отрисовка всех текстов.
     */
    fun render(canvas: Canvas) {
        for (text in texts) {
            if (text.isActive) {
                text.render(canvas)
            }
        }
    }
    
    /**
     * Очистка всех текстов.
     */
    fun clear() {
        texts.forEach { releaseToPool(it) }
        texts.clear()
    }
    
    /**
     * Получение из пула.
     */
    private fun acquireFromPool(): FloatingText {
        return if (pool.isNotEmpty()) {
            pool.removeAt(pool.lastIndex)
        } else {
            FloatingText()
        }
    }
    
    /**
     * Возврат в пул.
     */
    private fun releaseToPool(text: FloatingText) {
        if (pool.size < maxTexts) {
            text.reset()
            pool.add(text)
        }
    }
    
    /**
     * Количество активных текстов.
     */
    fun getActiveCount(): Int = texts.count { it.isActive }
}

/**
 * Extension функция для показа комбо текста.
 */
fun FloatingTextManager.showCombo(
    combo: Int,
    x: Float,
    y: Float
): FloatingText {
    val color = when {
        combo >= 50 -> Color.parseColor("#FF1493") // Deep Pink
        combo >= 30 -> Color.parseColor("#FFD700") // Gold
        combo >= 20 -> Color.parseColor("#00BFFF") // Deep Sky Blue
        combo >= 10 -> Color.parseColor("#32CD32") // Lime Green
        else -> Color.WHITE
    }
    
    return show("COMBO x$combo", x, y, color, 40f, 1.0f)
}

/**
 * Extension функция для показа уровня.
 */
fun FloatingTextManager.showLevel(
    level: Int,
    x: Float,
    y: Float
): FloatingText {
    return show("LEVEL $level", x, y, Color.parseColor("#FFD700"), 48f, 2.0f)
}
