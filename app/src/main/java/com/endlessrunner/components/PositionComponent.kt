package com.endlessrunner.components

import android.graphics.RectF
import kotlin.math.sqrt

/**
 * Компонент позиции и движения.
 * Хранит координаты, скорость и ускорение сущности.
 * 
 * @param x Начальная позиция X
 * @param y Начальная позиция Y
 * @param vx Начальная скорость X
 * @param vy Начальная скорость Y
 */
open class PositionComponent(
    x: Float = 0f,
    y: Float = 0f,
    vx: Float = 0f,
    vy: Float = 0f
) : Component() {
    
    // ============================================================================
    // ПОЗИЦИЯ
    // ============================================================================
    
    /** Позиция по оси X (пиксели) */
    var x: Float = x
    
    /** Позиция по оси Y (пиксели) */
    var y: Float = y
    
    /**
     * Получение позиции как пары.
     */
    val position: Pair<Float, Float>
        get() = Pair(x, y)
    
    // ============================================================================
    // СКОРОСТЬ
    // ============================================================================
    
    /** Скорость по оси X (пикселей/сек) */
    var vx: Float = vx
    
    /** Скорость по оси Y (пикселей/сек) */
    var vy: Float = vy
    
    /**
     * Получение скорости как пары.
     */
    val velocity: Pair<Float, Float>
        get() = Pair(vx, vy)
    
    /**
     * Величина скорости (скаляр).
     */
    val speed: Float
        get() = sqrt(vx * vx + vy * vy)
    
    // ============================================================================
    // УСКОРЕНИЕ
    // ============================================================================
    
    /** Ускорение по оси X */
    var ax: Float = 0f
    
    /** Ускорение по оси Y */
    var ay: Float = 0f
    
    /**
     * Получение ускорения как пары.
     */
    val acceleration: Pair<Float, Float>
        get() = Pair(ax, ay)
    
    // ============================================================================
    // ВРАЩЕНИЕ
    // ============================================================================
    
    /** Угол поворота (градусы) */
    var rotation: Float = 0f
    
    /** Угловая скорость (градусов/сек) */
    var angularVelocity: Float = 0f
    
    // ============================================================================
    // ОБНОВЛЕНИЕ
    // ============================================================================
    
    override fun onUpdate(deltaTime: Float) {
        super.onUpdate(deltaTime)
        
        // Применение ускорения к скорости
        vx += ax * deltaTime
        vy += ay * deltaTime
        
        // Применение скорости к позиции
        x += vx * deltaTime
        y += vy * deltaTime
        
        // Применение угловой скорости
        rotation += angularVelocity * deltaTime
        
        // Нормализация угла (0-360)
        normalizeRotation()
    }
    
    /**
     * Установка позиции.
     */
    fun setPosition(x: Float, y: Float) {
        this.x = x
        this.y = y
    }
    
    /**
     * Установка позиции из пары.
     */
    fun setPosition(pos: Pair<Float, Float>) {
        setPosition(pos.first, pos.second)
    }
    
    /**
     * Установка скорости.
     */
    fun setVelocity(vx: Float, vy: Float) {
        this.vx = vx
        this.vy = vy
    }
    
    /**
     * Установка скорости из пары.
     */
    fun setVelocity(vel: Pair<Float, Float>) {
        setVelocity(vel.first, vel.second)
    }
    
    /**
     * Установка ускорения.
     */
    fun setAcceleration(ax: Float, ay: Float) {
        this.ax = ax
        this.ay = ay
    }
    
    /**
     * Добавление к скорости.
     */
    fun addVelocity(dx: Float, dy: Float) {
        vx += dx
        vy += dy
    }
    
    /**
     * Добавление ускорения.
     */
    fun addAcceleration(da: Float, db: Float) {
        ax += da
        ay += db
    }
    
    /**
     * Остановка (обнуление скорости).
     */
    fun stop() {
        vx = 0f
        vy = 0f
        ax = 0f
        ay = 0f
    }
    
    /**
     * Ограничение скорости.
     * 
     * @param maxSpeed Максимальная скорость
     */
    fun clampSpeed(maxSpeed: Float) {
        if (speed > maxSpeed) {
            val scale = maxSpeed / speed
            vx *= scale
            vy *= scale
        }
    }
    
    /**
     * Ограничение позиции в прямоугольнике.
     */
    fun clampPosition(minX: Float, minY: Float, maxX: Float, maxY: Float) {
        x = x.coerceIn(minX, maxX)
        y = y.coerceIn(minY, maxY)
    }
    
    /**
     * Проверка, движется ли сущность.
     */
    fun isMoving(): Boolean = speed > 0.01f
    
    /**
     * Проверка, движется ли сущность вверх.
     */
    fun isMovingUp(): Boolean = vy < 0
    
    /**
     * Проверка, движется ли сущность вниз.
     */
    fun isMovingDown(): Boolean = vy > 0
    
    /**
     * Проверка, движется ли сущность влево.
     */
    fun isMovingLeft(): Boolean = vx < 0
    
    /**
     * Проверка, движется ли сущность вправо.
     */
    fun isMovingRight(): Boolean = vx > 0
    
    /**
     * Расстояние до другой позиции.
     */
    fun distanceTo(otherX: Float, otherY: Float): Float {
        val dx = x - otherX
        val dy = y - otherY
        return sqrt(dx * dx + dy * dy)
    }
    
    /**
     * Расстояние до другого PositionComponent.
     */
    fun distanceTo(other: PositionComponent): Float {
        return distanceTo(other.x, other.y)
    }
    
    /**
     * Квадрат расстояния (быстрее, без корня).
     */
    fun distanceSquaredTo(otherX: Float, otherY: Float): Float {
        val dx = x - otherX
        val dy = y - otherY
        return dx * dx + dy * dy
    }
    
    override fun reset() {
        super.reset()
        x = 0f
        y = 0f
        vx = 0f
        vy = 0f
        ax = 0f
        ay = 0f
        rotation = 0f
        angularVelocity = 0f
    }
    
    // ============================================================================
    // ПРИВАТНЫЕ МЕТОДЫ
    // ============================================================================
    
    private fun normalizeRotation() {
        rotation = rotation % 360f
        if (rotation < 0) rotation += 360f
    }
}

/**
 * Extension функция для перемещения к точке.
 */
fun PositionComponent.moveTo(targetX: Float, targetY: Float, speed: Float, deltaTime: Float) {
    val dx = targetX - x
    val dy = targetY - y
    val distance = sqrt(dx * dx + dy * dy)
    
    if (distance > 0.1f) {
        val moveX = (dx / distance) * speed * deltaTime
        val moveY = (dy / distance) * speed * deltaTime
        x += moveX
        y += moveY
    }
}

/**
 * Extension функция для движения в направлении.
 */
fun PositionComponent.moveInDirection(angleDegrees: Float, speed: Float, deltaTime: Float) {
    val radians = Math.toRadians(angleDegrees.toDouble()).toFloat()
    vx = kotlin.math.cos(radians) * speed
    vy = kotlin.math.sin(radians) * speed
    x += vx * deltaTime
    y += vy * deltaTime
}

/**
 * Extension функция для получения направления движения.
 */
fun PositionComponent.getMovementDirection(): Float {
    return Math.toDegrees(kotlin.math.atan2(vy.toDouble(), vx.toDouble())).toFloat()
}
