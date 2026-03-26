package com.endlessrunner.visual

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.abs

/**
 * Анимированная полоска прогресса.
 * Поддерживает плавное изменение значения.
 *
 * @param width Ширина полоски
 * @param height Высота полоски
 * @param type Тип полоски
 */
class ProgressBar(
    /** Ширина полоски */
    var width: Float = 200f,
    
    /** Высота полоски */
    var height: Float = 20f,
    
    /** Тип полоски */
    var type: ProgressBarType = ProgressBarType.HEALTH
) {
    /** Текущее значение */
    var value: Float = 100f
        private set
    
    /** Максимальное значение */
    var maxValue: Float = 100f
    
    /** Целевое значение (для анимации) */
    private var targetValue: Float = 100f
    
    /** Время анимации */
    private var animationTime: Float = 0f
    
    /** Длительность анимации */
    private var animationDuration: Float = 0.3f
    
    /** Позиция X */
    var x: Float = 0f
    
    /** Позиция Y */
    var y: Float = 0f
    
    /** Скругление углов */
    var cornerRadius: Float = 10f
    
    /** Показывать ли текст */
    var showText: Boolean = false
    
    /** Цвет фона */
    var backgroundColor: Int = Color.parseColor("#333333")
    
    /** Цвет полоски */
    var barColor: Int = Color.parseColor("#00FF00")
    
    /** Цвет текста */
    var textColor: Int = Color.WHITE
    
    /** Размер текста */
    var textSize: Float = 14f
    
    /** Градиент */
    var useGradient: Boolean = false
    
    /** Второй цвет градиента */
    var gradientEndColor: Int = Color.parseColor("#00AA00")
    
    /** Обводка */
    var showBorder: Boolean = true
    
    /** Цвет обводки */
    var borderColor: Int = Color.BLACK
    
    /** Толщина обводки */
    var borderWidth: Float = 2f
    
    /** Paint для фона */
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    
    /** Paint для полоски */
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    
    /** Paint для текста */
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    
    /** Paint для обводки */
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    
    /** Rect для отрисовки */
    private val rect = RectF()
    
    /**
     * Установка значения.
     *
     * @param value Текущее значение
     * @param max Максимальное значение
     * @param animate Анимировать изменение
     */
    fun setValue(value: Float, max: Float = maxValue, animate: Boolean = true) {
        this.value = value.coerceIn(0f, max)
        this.maxValue = max
        this.targetValue = this.value
        
        if (!animate) {
            animationTime = animationDuration
        } else {
            animationTime = 0f
        }
    }
    
    /**
     * Плавное изменение значения.
     *
     * @param delta Изменение значения
     * @param duration Длительность анимации
     */
    fun animateChange(delta: Float, duration: Float = 0.3f) {
        targetValue = (value + delta).coerceIn(0f, maxValue)
        animationDuration = duration
        animationTime = 0f
    }
    
    /**
     * Обновление анимации.
     *
     * @param deltaTime Время с последнего кадра
     */
    fun update(deltaTime: Float) {
        if (animationTime < animationDuration) {
            animationTime += deltaTime
            
            val progress = (animationTime / animationDuration).coerceIn(0f, 1f)
            
            // Ease out квадрат
            val easedProgress = 1f - (1f - progress) * (1f - progress)
            
            value = value + (targetValue - value) * easedProgress
        } else {
            value = targetValue
        }
    }
    
    /**
     * Отрисовка полоски.
     */
    fun render(canvas: Canvas) {
        // Фон
        backgroundPaint.color = backgroundColor
        rect.set(x, y, x + width, y + height)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, backgroundPaint)
        
        // Полоска
        val fillWidth = (width * (value / maxValue)).coerceAtLeast(0f)
        
        if (fillWidth > 0) {
            if (useGradient) {
                val gradient = LinearGradient(
                    x, y, x + fillWidth, y,
                    barColor,
                    gradientEndColor,
                    Shader.TileMode.CLAMP
                )
                barPaint.shader = gradient
            } else {
                barPaint.shader = null
                barPaint.color = getDynamicColor()
            }
            
            rect.set(x, y, x + fillWidth, y + height)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, barPaint)
        }
        
        // Обводка
        if (showBorder) {
            borderPaint.color = borderColor
            borderPaint.strokeWidth = borderWidth
            rect.set(x, y, x + width, y + height)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)
        }
        
        // Текст
        if (showText) {
            textPaint.textSize = textSize
            textPaint.color = textColor
            
            val percent = (value / maxValue * 100).toInt()
            canvas.drawText(
                "$percent%",
                x + width / 2f,
                y + height / 2f - (textPaint.ascent() + textPaint.descent()) / 2f,
                textPaint
            )
        }
    }
    
    /**
     * Получение динамического цвета на основе процента.
     */
    private fun getDynamicColor(): Int {
        val percent = value / maxValue
        
        return when (type) {
            ProgressBarType.HEALTH -> {
                when {
                    percent > 0.6f -> Color.parseColor("#00FF00") // Зелёный
                    percent > 0.3f -> Color.parseColor("#FFFF00") // Жёлтый
                    else -> Color.parseColor("#FF0000") // Красный
                }
            }
            ProgressBarType.ENERGY -> {
                Color.parseColor("#00BFFF") // Голубой
            }
            ProgressBarType.PROGRESS -> {
                Color.parseColor("#FFD700") // Золотой
            }
            ProgressBarType.CUSTOM -> {
                barColor
            }
        }
    }
    
    /**
     * Получение процента заполнения.
     */
    fun getPercent(): Float = value / maxValue
    
    /**
     * Проверка, пуста ли полоска.
     */
    fun isEmpty(): Boolean = value <= 0f
    
    /**
     * Проверка, полна ли полоска.
     */
    fun isFull(): Boolean = value >= maxValue
    
    /**
     * Сброс полоски.
     */
    fun reset() {
        value = 0f
        targetValue = 0f
        maxValue = 100f
        animationTime = 0f
    }
}

/**
 * Типы полосок прогресса.
 */
enum class ProgressBarType {
    HEALTH,     // Здоровье (зелёный → жёлтый → красный)
    ENERGY,     // Энергия (голубой)
    PROGRESS,   // Прогресс (золотой)
    CUSTOM      // Кастомный цвет
}

/**
 * Вертикальная полоска прогресса.
 */
class VerticalProgressBar(
    width: Float = 20f,
    height: Float = 200f,
    type: ProgressBarType = ProgressBarType.HEALTH
) : ProgressBar(width, height, type) {
    
    override fun render(canvas: Canvas) {
        canvas.save()
        
        try {
            // Поворот на 90 градусов
            canvas.rotate(90f, x + height / 2f, y + height / 2f)
            canvas.translate(-(height - width) / 2f, 0f)
            
            // Временная замена размеров
            val tempWidth = this.width
            val tempHeight = this.height
            this.width = height
            this.height = width
            
            super.render(canvas)
            
            // Возврат размеров
            this.width = tempWidth
            this.height = tempHeight
            
        } finally {
            canvas.restore()
        }
    }
}

/**
 * Кольцевая полоска прогресса.
 */
class CircularProgressBar(
    /** Радиус кольца */
    var radius: Float = 50f,
    
    /** Толщина кольца */
    var strokeWidth: Float = 10f,
    
    type: ProgressBarType = ProgressBarType.HEALTH
) : ProgressBar(radius * 2, radius * 2, type) {
    
    /** Начальный угол (градусы) */
    var startAngle: Float = -90f
    
    /** Paint для кольца */
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@CircularProgressBar.strokeWidth
        strokeCap = Paint.Cap.ROUND
    }
    
    override fun render(canvas: Canvas) {
        val centerX = x + radius
        val centerY = y + radius
        
        // Фон (серое кольцо)
        backgroundPaint.color = backgroundColor
        canvas.drawCircle(centerX, centerY, radius - strokeWidth / 2f, backgroundPaint)
        
        // Кольцо прогресса
        val sweepAngle = 360f * (value / maxValue)
        
        ringPaint.color = getDynamicColor()
        ringPaint.strokeWidth = strokeWidth
        
        canvas.drawArc(
            RectF(
                centerX - radius + strokeWidth / 2f,
                centerY - radius + strokeWidth / 2f,
                centerX + radius - strokeWidth / 2f,
                centerY + radius - strokeWidth / 2f
            ),
            startAngle,
            sweepAngle,
            false,
            ringPaint
        )
        
        // Текст в центре
        if (showText) {
            textPaint.textSize = textSize
            textPaint.color = textColor
            
            val percent = (value / maxValue * 100).toInt()
            canvas.drawText(
                "$percent%",
                centerX,
                centerY - (textPaint.ascent() + textPaint.descent()) / 2f,
                textPaint
            )
        }
    }
}

/**
 * Менеджер полосок прогресса.
 */
class ProgressBarManager {
    
    /** Активные полоски */
    private val bars: MutableList<ProgressBar> = mutableListOf()
    
    /**
     * Добавление полоски.
     */
    fun add(bar: ProgressBar) {
        bars.add(bar)
    }
    
    /**
     * Удаление полоски.
     */
    fun remove(bar: ProgressBar) {
        bars.remove(bar)
    }
    
    /**
     * Обновление всех полосок.
     */
    fun update(deltaTime: Float) {
        bars.forEach { it.update(deltaTime) }
    }
    
    /**
     * Отрисовка всех полосок.
     */
    fun render(canvas: Canvas) {
        bars.forEach { it.render(canvas) }
    }
    
    /**
     * Очистка всех полосок.
     */
    fun clear() {
        bars.clear()
    }
}

/**
 * Extension функция для создания health bar.
 */
fun createHealthBar(
    x: Float,
    y: Float,
    width: Float = 200f,
    height: Float = 20f,
    value: Float = 100f,
    maxValue: Float = 100f
): ProgressBar {
    return ProgressBar(width, height, ProgressBarType.HEALTH).apply {
        this.x = x
        this.y = y
        setValue(value, maxValue, animate = false)
    }
}

/**
 * Extension функция для создания energy bar.
 */
fun createEnergyBar(
    x: Float,
    y: Float,
    width: Float = 150f,
    height: Float = 15f,
    value: Float = 100f,
    maxValue: Float = 100f
): ProgressBar {
    return ProgressBar(width, height, ProgressBarType.ENERGY).apply {
        this.x = x
        this.y = y
        setValue(value, maxValue, animate = false)
        barColor = Color.parseColor("#00BFFF")
        useGradient = true
        gradientEndColor = Color.parseColor("#0080BF")
    }
}
