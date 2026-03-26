package com.endlessrunner.particles

import android.graphics.Color
import com.endlessrunner.particles.ParticlePresets.FIRE
import com.endlessrunner.particles.ParticlePresets.MAGIC_GLOW
import com.endlessrunner.particles.ParticlePresets.SMOKE
import com.endlessrunner.particles.ParticlePresets.SPARKS
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Пресеты визуальных эффектов.
 * Объект содержит готовые конфигурации для типовых эффектов игры.
 */
object Effects {
    
    // ============================================================================
    // ЭФФЕКТЫ ИГРОКА
    // ============================================================================
    
    /**
     * CoinSpark - искры при сборе монеты.
     * Золотые частицы, короткий lifetime, разлёт в стороны.
     */
    val CoinSpark: ParticleConfig = particleConfig {
        lifetime(0.3f, 0.6f)
        speed(80f, 150f)
        size(4f, 10f)
        colors(
            Color.parseColor("#FFD700"),
            Color.TRANSPARENT
        )
        colorVariation(
            Color.parseColor("#FFD700"), // Gold
            Color.parseColor("#FFA500"), // Orange
            Color.parseColor("#FFFF00"), // Yellow
            Color.parseColor("#FFFACD")  // LemonChiffon
        )
        emission(50f, 30)
        spread(120f, -90f)
        gravity(100f)
        shape(ParticleType.CIRCLE)
    }
    
    /**
     * PlayerTrail - след игрока при движении.
     * Полупрозрачные частицы, следуют за игроком.
     */
    val PlayerTrail: ParticleConfig = particleConfig {
        lifetime(0.4f, 0.8f)
        speed(0f, 20f)
        size(15f, 30f)
        colors(
            Color.parseColor("#40FFFFFF"),
            Color.TRANSPARENT
        )
        emission(20f, 50)
        spread(30f, 0f)
        drag(0.8f)
        shape(ParticleType.CIRCLE)
    }
    
    /**
     * JumpDust - пыль при прыжке.
     * Серые/коричневые частицы, внизу экрана.
     */
    val JumpDust: ParticleConfig = particleConfig {
        lifetime(0.5f, 1.0f)
        speed(30f, 60f)
        size(8f, 20f)
        colors(
            Color.parseColor("#8B7355"),
            Color.TRANSPARENT
        )
        colorVariation(
            Color.parseColor("#8B7355"),
            Color.parseColor("#A0826D"),
            Color.parseColor("#696969")
        )
        emission(30f, 20)
        spread(90f, -90f)
        gravity(50f)
        drag(0.3f)
        shape(ParticleType.CIRCLE)
    }
    
    /**
     * LandingDust - пыль при приземлении.
     * Больше частиц, горизонтальный разлёт.
     */
    val LandingDust: ParticleConfig = particleConfig {
        lifetime(0.4f, 0.8f)
        speed(50f, 100f)
        size(5f, 15f)
        colors(
            Color.parseColor("#A0826D"),
            Color.TRANSPARENT
        )
        colorVariation(
            Color.parseColor("#8B7355"),
            Color.parseColor("#A0826D"),
            Color.parseColor("#D2B48C")
        )
        emission(40f, 30)
        spread(160f, 180f) // Горизонтальный разлёт
        gravity(30f)
        drag(0.5f)
        shape(ParticleType.CIRCLE)
    }
    
    /**
     * HitSpark - искры при ударе/получении урона.
     * Красные/оранжевые частицы, вспышка.
     */
    val HitSpark: ParticleConfig = particleConfig {
        lifetime(0.2f, 0.5f)
        speed(100f, 200f)
        size(5f, 12f)
        colors(
            Color.parseColor("#FF4500"),
            Color.TRANSPARENT
        )
        colorVariation(
            Color.parseColor("#FF0000"),
            Color.parseColor("#FF4500"),
            Color.parseColor("#FFA500"),
            Color.parseColor("#FFFF00")
        )
        emission(60f, 40)
        spread(180f, 0f) // Во все стороны
        gravity(150f)
        shape(ParticleType.STAR)
    }
    
    /**
     * DeathExplosion - взрыв при смерти.
     * Много частиц, большой разлёт, разные цвета.
     */
    val DeathExplosion: ParticleConfig = particleConfig {
        lifetime(0.8f, 1.5f)
        speed(150f, 300f)
        size(10f, 30f)
        colors(
            Color.parseColor("#FF0000"),
            Color.TRANSPARENT
        )
        colorVariation(
            Color.parseColor("#FF0000"),
            Color.parseColor("#FF4500"),
            Color.parseColor("#FFA500"),
            Color.parseColor("#FFFF00"),
            Color.parseColor("#8B0000")
        )
        emission(100f, 100)
        spread(360f)
        gravity(200f)
        shape(ParticleType.CIRCLE)
    }
    
    // ============================================================================
    // ЭФФЕКТЫ БОНУСОВ
    // ============================================================================
    
    /**
     * PowerUpGlow - свечение бонуса.
     * Цветные частицы, вращение.
     */
    val PowerUpGlow: ParticleConfig = particleConfig {
        lifetime(0.6f, 1.2f)
        speed(20f, 50f)
        size(8f, 20f)
        colors(
            Color.parseColor("#00FF00"),
            Color.parseColor("#006400")
        )
        colorVariation(
            Color.parseColor("#00FF00"),
            Color.parseColor("#00CED1"),
            Color.parseColor("#9370DB"),
            Color.parseColor("#FF69B4")
        )
        emission(15f, 30)
        spread(360f)
        rotation(0f, 60f, true)
        shape(ParticleType.STAR)
    }
    
    /**
     * ShieldBubble - щит вокруг игрока.
     * Голубые частицы, круговой паттерн.
     */
    val ShieldBubble: ParticleConfig = particleConfig {
        lifetime(1.0f, 2.0f)
        speed(10f, 30f)
        size(5f, 15f)
        colors(
            Color.parseColor("#4000BFFF"),
            Color.parseColor("#0000BFFF")
        )
        emission(10f, 40)
        spread(360f)
        area(60f, 60f, EmissionShape.CIRCLE)
        shape(ParticleType.CIRCLE)
    }
    
    /**
     * MagnetField - поле магнита.
     * Линии частиц к игроку.
     */
    val MagnetField: ParticleConfig = particleConfig {
        lifetime(0.3f, 0.6f)
        speed(100f, 200f)
        size(3f, 8f)
        colors(
            Color.parseColor("#FF1493"),
            Color.TRANSPARENT
        )
        emission(30f, 50)
        spread(360f)
        shape(ParticleType.LINE)
    }
    
    /**
     * CoinMultiplier - эффект x2 монет.
     * Золотые искры вокруг игрока.
     */
    val CoinMultiplier: ParticleConfig = particleConfig {
        lifetime(0.5f, 1.0f)
        speed(50f, 100f)
        size(6f, 15f)
        colors(
            Color.parseColor("#FFD700"),
            Color.TRANSPARENT
        )
        colorVariation(
            Color.parseColor("#FFD700"),
            Color.parseColor("#FFA500"),
            Color.parseColor("#FFFF00")
        )
        emission(40f, 40)
        spread(360f)
        gravity(-50f)
        shape(ParticleType.STAR)
    }
    
    /**
     * SpeedBoost - эффект ускорения.
     * Горизонтальные линии (speed lines).
     */
    val SpeedBoost: ParticleConfig = particleConfig {
        lifetime(0.2f, 0.4f)
        speed(200f, 400f)
        size(20f, 50f)
        colors(
            Color.parseColor("#80FFFFFF"),
            Color.TRANSPARENT
        )
        emission(30f, 30)
        spread(20f, 0f) // Горизонтально
        shape(ParticleType.LINE)
    }
    
    // ============================================================================
    // ЭФФЕКТЫ ОКРУЖЕНИЯ
    // ============================================================================
    
    /**
     * Rain - дождь (фон).
     * Синие линии, падение вниз.
     */
    val Rain: ParticleConfig = particleConfig {
        lifetime(0.5f, 1.0f)
        speed(300f, 500f)
        size(2f, 15f)
        colors(
            Color.parseColor("#6495ED"),
            Color.TRANSPARENT
        )
        emission(100f, 200)
        spread(10f, -90f)
        gravity(400f)
        shape(ParticleType.LINE)
    }
    
    /**
     * Snow - снег (фон).
     * Белые частицы, медленное падение.
     */
    val Snow: ParticleConfig = particleConfig {
        lifetime(2.0f, 4.0f)
        speed(30f, 60f)
        size(5f, 12f)
        colors(
            Color.parseColor("#FFFAFA"),
            Color.parseColor("#E0E0E0")
        )
        emission(20f, 150)
        spread(30f, -90f)
        gravity(50f)
        drag(0.2f)
        rotation(0f, 30f, true)
        shape(ParticleType.CIRCLE)
    }
    
    /**
     * Leaves - листья (фон).
     * Зелёные/жёлтые, вращение.
     */
    val Leaves: ParticleConfig = particleConfig {
        lifetime(2.0f, 4.0f)
        speed(20f, 40f)
        size(10f, 20f)
        colors(
            Color.parseColor("#228B22"),
            Color.TRANSPARENT
        )
        colorVariation(
            Color.parseColor("#228B22"), // Forest Green
            Color.parseColor("#9ACD32"), // Yellow Green
            Color.parseColor("#DAA520"), // Goldenrod
            Color.parseColor("#B8860B")  // Dark Goldenrod
        )
        emission(5f, 50)
        spread(60f, -90f)
        gravity(30f)
        drag(0.1f)
        rotation(0f, 90f, true)
        shape(ParticleType.TRIANGLE)
    }
    
    /**
     * Fire - огонь (костёр, факел).
     */
    val Fire: ParticleConfig = FIRE
    
    /**
     * Smoke - дым.
     */
    val Smoke: ParticleConfig = SMOKE
    
    /**
     * Sparks - искры (общие).
     */
    val Sparks: ParticleConfig = SPARKS
    
    /**
     * MagicGlow - магическое свечение.
     */
    val MagicGlow: ParticleConfig = MAGIC_GLOW
    
    // ============================================================================
    // ЭФФЕКТЫ ВРАГОВ
    // ============================================================================
    
    /**
     * EnemyHit - попадание по врагу.
     */
    val EnemyHit: ParticleConfig = particleConfig {
        lifetime(0.3f, 0.6f)
        speed(80f, 150f)
        size(6f, 15f)
        colors(
            Color.parseColor("#FFFFFF"),
            Color.TRANSPARENT
        )
        colorVariation(
            Color.parseColor("#FFFFFF"),
            Color.parseColor("#FF0000"),
            Color.parseColor("#FFA500")
        )
        emission(40f, 30)
        spread(180f, 0f)
        gravity(100f)
        shape(ParticleType.STAR)
    }
    
    /**
     * EnemyDeath - смерть врага.
     */
    val EnemyDeath: ParticleConfig = particleConfig {
        lifetime(0.6f, 1.2f)
        speed(100f, 200f)
        size(8f, 25f)
        colors(
            Color.parseColor("#8B0000"),
            Color.TRANSPARENT
        )
        colorVariation(
            Color.parseColor("#8B0000"),
            Color.parseColor("#FF0000"),
            Color.parseColor("#4A4A4A"),
            Color.parseColor("#2F4F4F")
        )
        emission(60f, 60)
        spread(360f)
        gravity(150f)
        shape(ParticleType.CIRCLE)
    }
    
    /**
     * PoisonCloud - облако яда.
     */
    val PoisonCloud: ParticleConfig = particleConfig {
        lifetime(1.5f, 3.0f)
        speed(10f, 30f)
        size(20f, 50f)
        colors(
            Color.parseColor("#8000FF00"),
            Color.parseColor("#0000FF00")
        )
        emission(5f, 30)
        spread(30f, -90f)
        drag(0.3f)
        shape(ParticleType.CIRCLE)
    }
    
    // ============================================================================
    // ЭФФЕКТЫ ПРЕПЯТСТВИЙ
    // ============================================================================
    
    /**
     * ObstacleHit - удар о препятствие.
     */
    val ObstacleHit: ParticleConfig = particleConfig {
        lifetime(0.3f, 0.6f)
        speed(100f, 180f)
        size(5f, 12f)
        colors(
            Color.parseColor("#808080"),
            Color.TRANSPARENT
        )
        colorVariation(
            Color.parseColor("#808080"),
            Color.parseColor("#A9A9A9"),
            Color.parseColor("#696969")
        )
        emission(40f, 30)
        spread(180f, 0f)
        gravity(200f)
        shape(ParticleType.SQUARE)
    }
    
    /**
     * Dust - обычная пыль.
     */
    val Dust: ParticleConfig = particleConfig {
        lifetime(0.8f, 1.5f)
        speed(20f, 50f)
        size(5f, 15f)
        colors(
            Color.parseColor("#D2B48C"),
            Color.TRANSPARENT
        )
        emission(10f, 40)
        spread(60f, -90f)
        gravity(30f)
        drag(0.4f)
        shape(ParticleType.CIRCLE)
    }
    
    // ============================================================================
    // УТИЛИТЫ
    // ============================================================================
    
    /**
     * Создание эмиттера с пресетом.
     *
     * @param preset Конфигурация пресета
     * @param x Позиция X
     * @param y Позиция Y
     * @return Настроенный эмиттер
     */
    fun createEmitter(
        preset: ParticleConfig,
        x: Float = 0f,
        y: Float = 0f
    ): ParticleEmitter {
        return ParticleEmitter(preset).apply {
            this.x = x
            this.y = y
        }
    }
    
    /**
     * Создание эмиттера в системе частиц.
     */
    fun createInSystem(
        preset: ParticleConfig,
        x: Float = 0f,
        y: Float = 0f
    ): ParticleEmitter {
        return ParticleSystem.getInstance().createEmitter(preset).apply {
            this.x = x
            this.y = y
        }
    }
    
    /**
     * Мгновенный выброс частиц в системе.
     */
    fun burstInSystem(
        preset: ParticleConfig,
        x: Float,
        y: Float,
        count: Int = 20
    ) {
        ParticleSystem.getInstance().createEmitter(preset).apply {
            this.x = x
            this.y = y
            burst(count)
        }
    }
    
    /**
     * Кастомизация пресета.
     *
     * @param preset Базовый пресет
     * @param block Блок настройки
     * @return Новая конфигурация
     */
    fun customize(
        preset: ParticleConfig,
        block: ParticleConfigBuilder.() -> Unit
    ): ParticleConfig {
        // Копируем параметры из пресета в builder
        val builder = ParticleConfigBuilder().apply {
            lifetime(preset.minLifetime, preset.maxLifetime)
            speed(preset.minSpeed, preset.maxSpeed)
            acceleration(preset.ax, preset.ay)
            drag(preset.drag)
            size(preset.minSize, preset.maxSize)
            colors(preset.startColor, preset.endColor)
            emission(preset.emissionRate, preset.maxParticles)
            spread(preset.spreadAngle, preset.baseAngle)
            rotation(preset.initialRotation, preset.rotationSpeed, preset.randomRotation)
            shape(preset.particleType)
            area(preset.emissionAreaWidth, preset.emissionAreaHeight, preset.emissionShape)
        }
        
        // Применяем кастомизацию
        block(builder)
        
        return builder.build()
    }
    
    /**
     * Смешивание двух пресетов.
     */
    fun blend(
        preset1: ParticleConfig,
        preset2: ParticleConfig,
        ratio: Float = 0.5f
    ): ParticleConfig {
        val t = ratio.coerceIn(0f, 1f)
        
        return particleConfig {
            lifetime(
                preset1.minLifetime + (preset2.minLifetime - preset1.minLifetime) * t,
                preset1.maxLifetime + (preset2.maxLifetime - preset1.maxLifetime) * t
            )
            speed(
                preset1.minSpeed + (preset2.minSpeed - preset1.minSpeed) * t,
                preset1.maxSpeed + (preset2.maxSpeed - preset1.maxSpeed) * t
            )
            acceleration(
                preset1.ax + (preset2.ax - preset1.ax) * t,
                preset1.ay + (preset2.ay - preset1.ay) * t
            )
            drag(preset1.drag + (preset2.drag - preset1.drag) * t)
            size(
                preset1.minSize + (preset2.minSize - preset1.minSize) * t,
                preset1.maxSize + (preset2.maxSize - preset1.maxSize) * t
            )
            colors(
                blendColor(preset1.startColor, preset2.startColor, t),
                blendColor(preset1.endColor, preset2.endColor, t)
            )
            emission(
                preset1.emissionRate + (preset2.emissionRate - preset1.emissionRate) * t,
                (preset1.maxParticles + (preset2.maxParticles - preset1.maxParticles) * t).toInt()
            )
            spread(
                preset1.spreadAngle + (preset2.spreadAngle - preset1.spreadAngle) * t,
                preset1.baseAngle + (preset2.baseAngle - preset1.baseAngle) * t
            )
            shape(if (t < 0.5f) preset1.particleType else preset2.particleType)
        }
    }
    
    /**
     * Смешивание цветов.
     */
    private fun blendColor(color1: Int, color2: Int, t: Float): Int {
        val a1 = Color.alpha(color1)
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)
        
        val a2 = Color.alpha(color2)
        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)
        
        return Color.argb(
            (a1 + (a2 - a1) * t).toInt(),
            (r1 + (r2 - r1) * t).toInt(),
            (g1 + (g2 - g1) * t).toInt(),
            (b1 + (b2 - b1) * t).toInt()
        )
    }
    
    /**
     * Создание кругового паттерна частиц.
     */
    fun createCircularPattern(
        centerX: Float,
        centerY: Float,
        radius: Float,
        particleCount: Int,
        config: ParticleConfig
    ): List<Triple<Float, Float, Pair<Float, Float>>> {
        return List(particleCount) { index ->
            val angle = (2 * PI * index / particleCount).toFloat()
            val x = centerX + cos(angle) * radius
            val y = centerY + sin(angle) * radius
            
            // Направление к центру
            val vx = -cos(angle) * config.minSpeed
            val vy = -sin(angle) * config.minSpeed
            
            Triple(x, y, Pair(vx, vy))
        }
    }
    
    /**
     * Создание спирального паттерна.
     */
    fun createSpiralPattern(
        centerX: Float,
        centerY: Float,
        turns: Int,
        particleCount: Int,
        config: ParticleConfig
    ): List<Triple<Float, Float, Pair<Float, Float>>> {
        return List(particleCount) { index ->
            val progress = index.toFloat() / particleCount
            val angle = progress * turns * 2 * PI
            val radius = progress * 100f
            
            val x = centerX + cos(angle) * radius
            val y = centerY + sin(angle) * radius
            
            val vx = -cos(angle) * config.minSpeed
            val vy = -sin(angle) * config.minSpeed
            
            Triple(x, y, Pair(vx, vy))
        }
    }
}

/**
 * Extension функция для быстрого создания эффекта в системе.
 */
fun ParticleConfig.emit(
    x: Float = 0f,
    y: Float = 0f,
    count: Int = 1
): ParticleEmitter {
    return ParticleSystem.getInstance().createEmitter(this).apply {
        start(x = x, y = y, oneShot = true, count = count)
    }
}

/**
 * Extension функция для burst эффекта.
 */
fun ParticleConfig.burst(
    x: Float = 0f,
    y: Float = 0f,
    count: Int = 20
): ParticleEmitter {
    return ParticleSystem.getInstance().createEmitter(this).apply {
        this.x = x
        this.y = y
        burst(count)
    }
}

/**
 * Extension функция для постоянного эффекта.
 */
fun ParticleConfig.continuous(
    x: Float = 0f,
    y: Float = 0f
): ParticleEmitter {
    return ParticleSystem.getInstance().createEmitter(this).apply {
        this.x = x
        this.y = y
        start()
    }
}
