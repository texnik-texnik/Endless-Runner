package com.endlessrunner.particles

import android.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Конфигурация системы частиц.
 * Определяет параметры генерации и поведения частиц.
 */
data class ParticleConfig(
    // ============================================================================
    // ВРЕМЯ ЖИЗНИ
    // ============================================================================
    
    /** Минимальное время жизни (секунды) */
    val minLifetime: Float = 0.5f,
    
    /** Максимальное время жизни (секунды) */
    val maxLifetime: Float = 1.0f,
    
    /** Задержка перед активацией */
    val delay: Float = 0f,
    
    // ============================================================================
    // СКОРОСТЬ И ДВИЖЕНИЕ
    // ============================================================================
    
    /** Минимальная скорость */
    val minSpeed: Float = 50f,
    
    /** Максимальная скорость */
    val maxSpeed: Float = 100f,
    
    /** Ускорение по X */
    val ax: Float = 0f,
    
    /** Ускорение по Y (гравитация) */
    val ay: Float = 0f,
    
    /** Сопротивление воздуха */
    val drag: Float = 0f,
    
    // ============================================================================
    // РАЗМЕР
    // ============================================================================
    
    /** Минимальный размер */
    val minSize: Float = 5f,
    
    /** Максимальный размер */
    val maxSize: Float = 15f,
    
    // ============================================================================
    // ЦВЕТ
    // ============================================================================
    
    /** Начальный цвет */
    val startColor: Int = Color.WHITE,
    
    /** Конечный цвет */
    val endColor: Int = Color.TRANSPARENT,
    
    /** Варианты цветов (выбирается случайно) */
    val colorVariations: List<Int> = emptyList(),
    
    // ============================================================================
    // ЭМИССИЯ
    // ============================================================================
    
    /** Частота эмиссии (частиц в секунду) */
    val emissionRate: Float = 10f,
    
    /** Максимальное количество активных частиц */
    val maxParticles: Int = 100,
    
    /** Угол разлёта (градусы, 360 = полный круг) */
    val spreadAngle: Float = 360f,
    
    /** Базовый угол направления */
    val baseAngle: Float = -90f,
    
    // ============================================================================
    // ВРАЩЕНИЕ
    // ============================================================================
    
    /** Начальное вращение */
    val initialRotation: Float = 0f,
    
    /** Скорость вращения (градусы/секунду) */
    val rotationSpeed: Float = 0f,
    
    /** Случайное вращение */
    val randomRotation: Boolean = false,
    
    // ============================================================================
    // ФОРМА
    // ============================================================================
    
    /** Тип частицы */
    val particleType: ParticleType = ParticleType.CIRCLE,
    
    // ============================================================================
    // ЭМИТТЕР
    // ============================================================================
    
    /** Размер области эмиссии по X */
    val emissionAreaWidth: Float = 0f,
    
    /** Размер области эмиссии по Y */
    val emissionAreaHeight: Float = 0f,
    
    /** Форма области эмиссии */
    val emissionShape: EmissionShape = EmissionShape.POINT
) {
    companion object {
        /** Конфигурация по умолчанию */
        val DEFAULT = ParticleConfig()
        
        /** Пустая конфигурация (без эмиссии) */
        val EMPTY = ParticleConfig(emissionRate = 0f)
    }
    
    /**
     * Получение случайного времени жизни.
     */
    fun getRandomLifetime(): Float {
        return Random.nextFloat() * (maxLifetime - minLifetime) + minLifetime
    }
    
    /**
     * Получение случайной скорости.
     */
    fun getRandomSpeed(): Float {
        return Random.nextFloat() * (maxSpeed - minSpeed) + minSpeed
    }
    
    /**
     * Получение случайного размера.
     */
    fun getRandomSize(): Float {
        return Random.nextFloat() * (maxSize - minSize) + minSize
    }
    
    /**
     * Получение случайного цвета.
     */
    fun getRandomColor(): Int {
        return if (colorVariations.isNotEmpty()) {
            colorVariations.random()
        } else {
            startColor
        }
    }
    
    /**
     * Получение случайного угла в пределах spread.
     */
    fun getRandomAngle(): Float {
        if (spreadAngle >= 360f) {
            return Random.nextFloat() * 360f
        }
        return baseAngle + Random.nextFloat() * spreadAngle - spreadAngle / 2f
    }
    
    /**
     * Получение скорости по углу.
     */
    fun getVelocity(angle: Float): Pair<Float, Float> {
        val rad = Math.toRadians(angle.toDouble()).toFloat()
        val speed = getRandomSpeed()
        return Pair(
            cos(rad) * speed,
            sin(rad) * speed
        )
    }
    
    /**
     * Получение случайной позиции в области эмиссии.
     */
    fun getEmissionPosition(originX: Float, originY: Float): Pair<Float, Float> {
        return when (emissionShape) {
            EmissionShape.POINT -> Pair(originX, originY)
            EmissionShape.RECTANGLE -> getRectanglePosition(originX, originY)
            EmissionShape.CIRCLE -> getCirclePosition(originX, originY)
            EmissionShape.LINE -> getLinePosition(originX, originY)
        }
    }
    
    /**
     * Позиция в прямоугольнике.
     */
    private fun getRectanglePosition(originX: Float, originY: Float): Pair<Float, Float> {
        val x = originX + (Random.nextFloat() - 0.5f) * emissionAreaWidth
        val y = originY + (Random.nextFloat() - 0.5f) * emissionAreaHeight
        return Pair(x, y)
    }
    
    /**
     * Позиция в круге.
     */
    private fun getCirclePosition(originX: Float, originY: Float): Pair<Float, Float> {
        val radius = minOf(emissionAreaWidth, emissionAreaHeight) / 2f
        val angle = Random.nextFloat() * 2 * PI
        val r = kotlin.math.sqrt(Random.nextFloat()) * radius
        
        val x = originX + cos(angle.toFloat()) * r
        val y = originY + sin(angle.toFloat()) * r
        return Pair(x, y)
    }
    
    /**
     * Позиция на линии.
     */
    private fun getLinePosition(originX: Float, originY: Float): Pair<Float, Float> {
        val t = Random.nextFloat()
        val x = originX + (t - 0.5f) * emissionAreaWidth
        val y = originY + (t - 0.5f) * emissionAreaHeight
        return Pair(x, y)
    }
    
    /**
     * Builder для создания конфигурации.
     */
    fun copy(
        minLifetime: Float = this.minLifetime,
        maxLifetime: Float = this.maxLifetime,
        delay: Float = this.delay,
        minSpeed: Float = this.minSpeed,
        maxSpeed: Float = this.maxSpeed,
        ax: Float = this.ax,
        ay: Float = this.ay,
        drag: Float = this.drag,
        minSize: Float = this.minSize,
        maxSize: Float = this.maxSize,
        startColor: Int = this.startColor,
        endColor: Int = this.endColor,
        colorVariations: List<Int> = this.colorVariations,
        emissionRate: Float = this.emissionRate,
        maxParticles: Int = this.maxParticles,
        spreadAngle: Float = this.spreadAngle,
        baseAngle: Float = this.baseAngle,
        initialRotation: Float = this.initialRotation,
        rotationSpeed: Float = this.rotationSpeed,
        randomRotation: Boolean = this.randomRotation,
        particleType: ParticleType = this.particleType,
        emissionAreaWidth: Float = this.emissionAreaWidth,
        emissionAreaHeight: Float = this.emissionAreaHeight,
        emissionShape: EmissionShape = this.emissionShape
    ): ParticleConfig {
        return ParticleConfig(
            minLifetime = minLifetime,
            maxLifetime = maxLifetime,
            delay = delay,
            minSpeed = minSpeed,
            maxSpeed = maxSpeed,
            ax = ax,
            ay = ay,
            drag = drag,
            minSize = minSize,
            maxSize = maxSize,
            startColor = startColor,
            endColor = endColor,
            colorVariations = colorVariations,
            emissionRate = emissionRate,
            maxParticles = maxParticles,
            spreadAngle = spreadAngle,
            baseAngle = baseAngle,
            initialRotation = initialRotation,
            rotationSpeed = rotationSpeed,
            randomRotation = randomRotation,
            particleType = particleType,
            emissionAreaWidth = emissionAreaWidth,
            emissionAreaHeight = emissionAreaHeight,
            emissionShape = emissionShape
        )
    }
}

/**
 * Форма области эмиссии.
 */
enum class EmissionShape {
    POINT,      // Точка
    RECTANGLE,  // Прямоугольник
    CIRCLE,     // Круг
    LINE        // Линия
}

/**
 * Builder для удобного создания ParticleConfig.
 */
class ParticleConfigBuilder {
    private var minLifetime: Float = 0.5f
    private var maxLifetime: Float = 1.0f
    private var delay: Float = 0f
    private var minSpeed: Float = 50f
    private var maxSpeed: Float = 100f
    private var ax: Float = 0f
    private var ay: Float = 0f
    private var drag: Float = 0f
    private var minSize: Float = 5f
    private var maxSize: Float = 15f
    private var startColor: Int = Color.WHITE
    private var endColor: Int = Color.TRANSPARENT
    private var colorVariations: MutableList<Int> = mutableListOf()
    private var emissionRate: Float = 10f
    private var maxParticles: Int = 100
    private var spreadAngle: Float = 360f
    private var baseAngle: Float = -90f
    private var initialRotation: Float = 0f
    private var rotationSpeed: Float = 0f
    private var randomRotation: Boolean = false
    private var particleType: ParticleType = ParticleType.CIRCLE
    private var emissionAreaWidth: Float = 0f
    private var emissionAreaHeight: Float = 0f
    private var emissionShape: EmissionShape = EmissionShape.POINT
    
    fun lifetime(min: Float, max: Float) = apply {
        minLifetime = min
        maxLifetime = max
    }
    
    fun speed(min: Float, max: Float) = apply {
        minSpeed = min
        maxSpeed = max
    }
    
    fun acceleration(x: Float = 0f, y: Float = 0f) = apply {
        ax = x
        ay = y
    }
    
    fun gravity(value: Float) = apply { ay = value }
    fun drag(value: Float) = apply { drag = value }
    
    fun size(min: Float, max: Float) = apply {
        minSize = min
        maxSize = max
    }
    
    fun colors(start: Int, end: Int) = apply {
        startColor = start
        endColor = end
    }
    
    fun colorVariation(vararg colors: Int) = apply {
        colorVariations.addAll(colors)
    }
    
    fun emission(rate: Float, max: Int = 100) = apply {
        emissionRate = rate
        maxParticles = max
    }
    
    fun spread(angle: Float, base: Float = -90f) = apply {
        spreadAngle = angle
        baseAngle = base
    }
    
    fun rotation(initial: Float = 0f, speed: Float = 0f, random: Boolean = false) = apply {
        initialRotation = initial
        rotationSpeed = speed
        randomRotation = random
    }
    
    fun shape(type: ParticleType) = apply { particleType = type }
    
    fun area(width: Float, height: Float, shape: EmissionShape = EmissionShape.RECTANGLE) = apply {
        emissionAreaWidth = width
        emissionAreaHeight = height
        emissionShape = shape
    }
    
    fun build(): ParticleConfig {
        return ParticleConfig(
            minLifetime = minLifetime,
            maxLifetime = maxLifetime,
            delay = delay,
            minSpeed = minSpeed,
            maxSpeed = maxSpeed,
            ax = ax,
            ay = ay,
            drag = drag,
            minSize = minSize,
            maxSize = maxSize,
            startColor = startColor,
            endColor = endColor,
            colorVariations = colorVariations,
            emissionRate = emissionRate,
            maxParticles = maxParticles,
            spreadAngle = spreadAngle,
            baseAngle = baseAngle,
            initialRotation = initialRotation,
            rotationSpeed = rotationSpeed,
            randomRotation = randomRotation,
            particleType = particleType,
            emissionAreaWidth = emissionAreaWidth,
            emissionAreaHeight = emissionAreaHeight,
            emissionShape = emissionShape
        )
    }
}

/**
 * DSL функция для создания ParticleConfig.
 */
fun particleConfig(block: ParticleConfigBuilder.() -> Unit): ParticleConfig {
    return ParticleConfigBuilder().apply(block).build()
}

/**
 * Пресеты конфигураций.
 */
object ParticlePresets {
    /** Искры */
    val SPARKS = particleConfig {
        lifetime(0.3f, 0.6f)
        speed(100f, 200f)
        size(3f, 8f)
        colors(
            Color.parseColor("#FFFF00"),
            Color.parseColor("#FFA500")
        )
        colorVariation(
            Color.parseColor("#FFFF00"),
            Color.parseColor("#FFA500"),
            Color.parseColor("#FF4500"),
            Color.WHITE
        )
        emission(30f, 50)
        spread(60f, -90f)
        gravity(200f)
        shape(ParticleType.CIRCLE)
    }
    
    /** Дым */
    val SMOKE = particleConfig {
        lifetime(1.0f, 2.0f)
        speed(20f, 40f)
        size(10f, 30f)
        colors(
            Color.parseColor("#808080"),
            Color.TRANSPARENT
        )
        emission(5f, 30)
        spread(30f, -90f)
        drag(0.5f)
        shape(ParticleType.CIRCLE)
    }
    
    /** Огонь */
    val FIRE = particleConfig {
        lifetime(0.5f, 1.0f)
        speed(50f, 100f)
        size(10f, 25f)
        colors(
            Color.parseColor("#FF4500"),
            Color.parseColor("#800000")
        )
        colorVariation(
            Color.parseColor("#FFFF00"),
            Color.parseColor("#FF4500"),
            Color.parseColor("#FF0000")
        )
        emission(20f, 40)
        spread(45f, -90f)
        gravity(-50f)
        shape(ParticleType.TRIANGLE)
    }
    
    /** Магическое свечение */
    val MAGIC_GLOW = particleConfig {
        lifetime(0.8f, 1.5f)
        speed(30f, 60f)
        size(5f, 15f)
        colors(
            Color.parseColor("#00BFFF"),
            Color.TRANSPARENT
        )
        colorVariation(
            Color.parseColor("#00BFFF"),
            Color.parseColor("#9370DB"),
            Color.parseColor("#FF69B4")
        )
        emission(15f, 30)
        spread(360f)
        rotation(0f, 90f, true)
        shape(ParticleType.STAR)
    }
}
