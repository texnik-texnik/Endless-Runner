package com.endlessrunner.particles

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.endlessrunner.core.PooledObject
import kotlin.math.cos
import kotlin.math.sin

/**
 * Класс частицы.
 * Представляет отдельную частицу в системе частиц.
 * Использует object pool для эффективного переиспользования.
 */
class Particle : PooledObject {
    companion object {
        /** Общий Paint для отрисовки частиц (оптимизация) */
        private val sharedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            isDither = true
        }
    }
    
    // ============================================================================
    // ПОЗИЦИЯ И ДВИЖЕНИЕ
    // ============================================================================
    
    /** Позиция X */
    var x: Float = 0f
    
    /** Позиция Y */
    var y: Float = 0f
    
    /** Скорость по X */
    var vx: Float = 0f
    
    /** Скорость по Y */
    var vy: Float = 0f
    
    /** Ускорение по X */
    var ax: Float = 0f
    
    /** Ускорение по Y */
    var ay: Float = 0f
    
    // ============================================================================
    // ВРЕМЯ ЖИЗНИ
    // ============================================================================
    
    /** Полное время жизни (секунды) */
    var lifetime: Float = 1f
    
    /** Текущий возраст (секунды) */
    var age: Float = 0f
    
    /** Задержка перед активацией (секунды) */
    var delay: Float = 0f
    
    // ============================================================================
    // РАЗМЕР
    // ============================================================================
    
    /** Начальный размер */
    var startSize: Float = 10f
    
    /** Конечный размер */
    var endSize: Float = 5f
    
    /** Текущий размер (вычисляется) */
    val currentSize: Float
        get() = lerp(startSize, endSize, getNormalizedAge())
    
    // ============================================================================
    // ЦВЕТ
    // ============================================================================
    
    /** Начальный цвет (ARGB) */
    var startColor: Int = 0xFFFFFFFF.toInt()
    
    /** Конечный цвет (ARGB) */
    var endColor: Int = 0x00FFFFFF.toInt()
    
    /** Текущий цвет (вычисляется) */
    val currentColor: Int
        get() = interpolateColor(getNormalizedAge())
    
    // ============================================================================
    // ВРАЩЕНИЕ
    // ============================================================================
    
    /** Угол вращения (градусы) */
    var rotation: Float = 0f
    
    /** Скорость вращения (градусы/секунду) */
    var rotationSpeed: Float = 0f
    
    // ============================================================================
    // СОСТОЯНИЕ
    // ============================================================================
    
    /** Активна ли частица */
    override var isActive: Boolean = false
        private set
    
    /** Тип частицы (для кастомной отрисовки) */
    var particleType: ParticleType = ParticleType.CIRCLE
    
    /** Дополнительные данные */
    var userData: Any? = null
    
    // ============================================================================
    // ВСПОМОГАТЕЛЬНЫЕ
    // ============================================================================
    
    /** Временный Rect для отрисовки */
    private val tempRect = RectF()
    
    /**
     * Инициализация частицы.
     *
     * @param x Позиция X
     * @param y Позиция Y
     * @param vx Скорость X
     * @param vy Скорость Y
     * @param config Конфигурация
     */
    fun emit(
        x: Float,
        y: Float,
        vx: Float,
        vy: Float,
        config: ParticleConfig
    ) {
        this.x = x
        this.y = y
        this.vx = vx
        this.vy = vy
        this.ax = config.ax
        this.ay = config.ay
        this.lifetime = config.lifetime
        this.age = 0f
        this.delay = config.delay
        this.startSize = config.startSize
        this.endSize = config.endSize
        this.startColor = config.startColor
        this.endColor = config.endColor
        this.rotation = config.initialRotation
        this.rotationSpeed = config.rotationSpeed
        this.particleType = config.particleType
        this.isActive = true
    }
    
    /**
     * Обновление частицы.
     *
     * @param deltaTime Время с последнего кадра (секунды)
     */
    fun update(deltaTime: Float) {
        if (!isActive) return
        
        // Обработка задержки
        if (delay > 0) {
            delay -= deltaTime
            return
        }
        
        // Обновление возраста
        age += deltaTime
        
        // Проверка смерти
        if (age >= lifetime) {
            isActive = false
            return
        }
        
        // Применение ускорения
        vx += ax * deltaTime
        vy += ay * deltaTime
        
        // Применение сопротивления (drag)
        if (config?.drag != 0f) {
            val dragFactor = 1f - config.drag * deltaTime
            vx *= dragFactor
            vy *= dragFactor
        }
        
        // Обновление позиции
        x += vx * deltaTime
        y += vy * deltaTime
        
        // Обновление вращения
        rotation += rotationSpeed * deltaTime
    }
    
    /**
     * Отрисовка частицы.
     *
     * @param canvas Canvas для отрисовки
     */
    fun render(canvas: Canvas) {
        if (!isActive || delay > 0) return
        
        val alpha = getAlpha()
        val size = currentSize
        val color = withAlpha(currentColor, alpha)
        
        sharedPaint.color = color
        
        when (particleType) {
            ParticleType.CIRCLE -> renderCircle(canvas, size)
            ParticleType.SQUARE -> renderSquare(canvas, size)
            ParticleType.TRIANGLE -> renderTriangle(canvas, size)
            ParticleType.LINE -> renderLine(canvas, size)
            ParticleType.STAR -> renderStar(canvas, size)
        }
    }
    
    /**
     * Отрисовка круга.
     */
    private fun renderCircle(canvas: Canvas, size: Float) {
        val radius = size / 2f
        canvas.drawCircle(x, y, radius, sharedPaint)
    }
    
    /**
     * Отрисовка квадрата.
     */
    private fun renderSquare(canvas: Canvas, size: Float) {
        val half = size / 2f
        tempRect.set(x - half, y - half, x + half, y + half)
        canvas.save()
        canvas.rotate(rotation, x, y)
        canvas.drawRect(tempRect, sharedPaint)
        canvas.restore()
    }
    
    /**
     * Отрисовка треугольника.
     */
    private fun renderTriangle(canvas: Canvas, size: Float) {
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(rotation)
        
        val half = size / 2f
        val path = android.graphics.Path().apply {
            moveTo(0f, -half) // Вершина
            lineTo(-half, half) // Левый нижний
            lineTo(half, half) // Правый нижний
            close()
        }
        
        canvas.drawPath(path, sharedPaint)
        canvas.restore()
    }
    
    /**
     * Отрисовка линии.
     */
    private fun renderLine(canvas: Canvas, size: Float) {
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(rotation)
        
        val half = size / 2f
        sharedPaint.strokeWidth = 2f
        sharedPaint.style = Paint.Style.STROKE
        canvas.drawLine(-half, 0f, half, 0f, sharedPaint)
        sharedPaint.style = Paint.Style.FILL
        sharedPaint.strokeWidth = 1f
        
        canvas.restore()
    }
    
    /**
     * Отрисовка звезды.
     */
    private fun renderStar(canvas: Canvas, size: Float) {
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(rotation)
        
        val outerRadius = size / 2f
        val innerRadius = outerRadius * 0.5f
        val points = 5
        
        val path = android.graphics.Path().apply {
            for (i in 0 until points * 2) {
                val radius = if (i % 2 == 0) outerRadius else innerRadius
                val angle = (Math.PI / points * i - Math.PI / 2).toFloat()
                val px = cos(angle) * radius
                val py = sin(angle) * radius
                
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
        
        canvas.drawPath(path, sharedPaint)
        canvas.restore()
    }
    
    /**
     * Сброс частицы.
     */
    override fun reset() {
        x = 0f
        y = 0f
        vx = 0f
        vy = 0f
        ax = 0f
        ay = 0f
        lifetime = 1f
        age = 0f
        delay = 0f
        startSize = 10f
        endSize = 5f
        startColor = 0xFFFFFFFF.toInt()
        endColor = 0x00FFFFFF.toInt()
        rotation = 0f
        rotationSpeed = 0f
        isActive = false
        particleType = ParticleType.CIRCLE
        userData = null
    }
    
    // ============================================================================
    // УТИЛИТЫ
    // ============================================================================
    
    /**
     * Получение нормализованного возраста (0..1).
     */
    fun getNormalizedAge(): Float {
        return (age / lifetime).coerceIn(0f, 1f)
    }
    
    /**
     * Получение альфа-канала на основе возраста.
     */
    fun getAlpha(): Int {
        val normalizedAge = getNormalizedAge()
        
        // Fade in в первые 10% жизни
        val fadeInEnd = 0.1f
        val fadeInAlpha = if (normalizedAge < fadeInEnd) {
            (normalizedAge / fadeInEnd * 255).toInt()
        } else {
            255
        }
        
        // Fade out в последние 20% жизни
        val fadeOutStart = 0.8f
        val fadeOutAlpha = if (normalizedAge > fadeOutStart) {
            ((1f - (normalizedAge - fadeOutStart) / (1f - fadeOutStart)) * 255).toInt()
        } else {
            255
        }
        
        return (fadeInAlpha.coerceAtMost(fadeOutAlpha)).coerceIn(0, 255)
    }
    
    /**
     * Линейная интерполяция между значениями.
     */
    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t
    }
    
    /**
     * Интерполяция цвета.
     */
    private fun interpolateColor(t: Float): Int {
        val startA = android.graphics.Color.alpha(startColor)
        val startR = android.graphics.Color.red(startColor)
        val startG = android.graphics.Color.green(startColor)
        val startB = android.graphics.Color.blue(startColor)
        
        val endA = android.graphics.Color.alpha(endColor)
        val endR = android.graphics.Color.red(endColor)
        val endG = android.graphics.Color.green(endColor)
        val endB = android.graphics.Color.blue(endColor)
        
        return android.graphics.Color.argb(
            (startA + (endA - startA) * t).toInt().coerceIn(0, 255),
            (startR + (endR - startR) * t).toInt().coerceIn(0, 255),
            (startG + (endG - startG) * t).toInt().coerceIn(0, 255),
            (startB + (endB - startB) * t).toInt().coerceIn(0, 255)
        )
    }
    
    /**
     * Добавление альфа-канала к цвету.
     */
    private fun withAlpha(color: Int, alpha: Int): Int {
        val r = android.graphics.Color.red(color)
        val g = android.graphics.Color.green(color)
        val b = android.graphics.Color.blue(color)
        return android.graphics.Color.argb(alpha, r, g, b)
    }
    
    /** Ссылка на конфиг (слабая) */
    @Transient
    var config: ParticleConfig? = null
}

/**
 * Типы частиц для отрисовки.
 */
enum class ParticleType {
    CIRCLE,     // Круг
    SQUARE,     // Квадрат
    TRIANGLE,   // Треугольник
    LINE,       // Линия
    STAR        // Звезда
}

/**
 * Extension функция для создания частицы с конфигурацией.
 */
fun createParticle(
    x: Float,
    y: Float,
    vx: Float,
    vy: Float,
    config: ParticleConfig
): Particle {
    return Particle().apply {
        emit(x, y, vx, vy, config)
        this.config = config
    }
}

/**
 * Extension функция для установки случайного направления скорости.
 *
 * @param speed Базовая скорость
 * @param spread Угол разброса (градусы)
 */
fun Particle.setRandomDirection(speed: Float, spread: Float = 360f) {
    val baseAngle = -90f // Вверх по умолчанию
    val angle = Math.toRadians((baseAngle + (Math.random() * spread - spread / 2)).toDouble()).toFloat()
    
    vx = kotlin.math.cos(angle) * speed
    vy = kotlin.math.sin(angle) * speed
}
