package com.endlessrunner.config

/**
 * Конфигурация графики и визуальных эффектов.
 */
data class VisualConfig(
    // ============================================================================
    // КАЧЕСТВО ГРАФИКИ
    // ============================================================================
    
    /** Качество частиц */
    val particleQuality: Quality = Quality.MEDIUM,
    
    /** Максимальное количество частиц */
    val maxParticles: Int = 2000,
    
    /** Максимальное количество эмиттеров */
    val maxEmitters: Int = 50,
    
    // ============================================================================
    // ЭФФЕКТЫ КАМЕРЫ
    // ============================================================================
    
    /** Включить тряску камеры */
    val enableShake: Boolean = true,
    
    /** Интенсивность тряски по умолчанию */
    val defaultShakeIntensity: Float = 10f,
    
    /** Длительность тряски по умолчанию */
    val defaultShakeDuration: Float = 0.3f,
    
    /** Включить flash эффект */
    val enableFlash: Boolean = true,
    
    /** Включить freeze frame */
    val enableFreezeFrame: Boolean = true,
    
    // ============================================================================
    // ЭФФЕКТЫ ПЕРСОНАЖА
    // ============================================================================
    
    /** Включить следы от движения */
    val enableTrails: Boolean = true,
    
    /** Включить эффекты прыжка/приземления */
    val enableJumpEffects: Boolean = true,
    
    /** Включить эффекты урона */
    val enableHitEffects: Boolean = true,
    
    /** Включить эффекты смерти */
    val enableDeathEffects: Boolean = true,
    
    // ============================================================================
    // ЭФФЕКТЫ ОКРУЖЕНИЯ
    // ============================================================================
    
    /** Включить погодные эффекты */
    val enableWeather: Boolean = true,
    
    /** Тип погоды (none, rain, snow) */
    val weatherType: WeatherType = WeatherType.NONE,
    
    /** Включить параллакс фон */
    val enableParallax: Boolean = true,
    
    /** Количество слоёв параллакса */
    val parallaxLayers: Int = 3,
    
    // ============================================================================
    // ТЕНИ И ОСВЕЩЕНИЕ
    // ============================================================================
    
    /** Качество теней */
    val shadowQuality: ShadowQuality = ShadowQuality.MEDIUM,
    
    /** Включить динамические тени */
    val enableDynamicShadows: Boolean = false,
    
    /** Включить свечение */
    val enableGlow: Boolean = true,
    
    // ============================================================================
    // UI ЭФФЕКТЫ
    // ============================================================================
    
    /** Включить анимацию UI */
    val enableUIAnimations: Boolean = true,
    
    /** Включить всплывающий текст */
    val enableFloatingText: Boolean = true,
    
    /** Включить анимацию полосок прогресса */
    val enableBarAnimations: Boolean = true,
    
    // ============================================================================
    // ПРОИЗВОДИТЕЛЬНОСТЬ
    // ============================================================================
    
    /** Ограничение FPS */
    val maxFPS: Int = 60,
    
    /** Включить VSync */
    val enableVSync: Boolean = true,
    
    /** Использовать аппаратное ускорение */
    val enableHardwareAcceleration: Boolean = true,
    
    /** Уровень детализации (LOD) */
    val lodLevel: LOD = LOD.MEDIUM
) {
    companion object {
        /** Конфигурация по умолчанию */
        val DEFAULT = VisualConfig()
        
        /** Низкое качество (для слабых устройств) */
        val LOW = VisualConfig(
            particleQuality = Quality.LOW,
            maxParticles = 500,
            maxEmitters = 20,
            enableShake = false,
            enableFlash = true,
            enableTrails = false,
            enableWeather = false,
            enableParallax = false,
            shadowQuality = ShadowQuality.NONE,
            enableGlow = false,
            enableUIAnimations = true,
            maxFPS = 30,
            lodLevel = LOD.LOW
        )
        
        /** Высокое качество (для мощных устройств) */
        val HIGH = VisualConfig(
            particleQuality = Quality.HIGH,
            maxParticles = 4000,
            maxEmitters = 80,
            enableShake = true,
            defaultShakeIntensity = 15f,
            defaultShakeDuration = 0.5f,
            enableFlash = true,
            enableFreezeFrame = true,
            enableTrails = true,
            enableJumpEffects = true,
            enableHitEffects = true,
            enableDeathEffects = true,
            enableWeather = true,
            enableParallax = true,
            parallaxLayers = 5,
            shadowQuality = ShadowQuality.HIGH,
            enableDynamicShadows = true,
            enableGlow = true,
            enableUIAnimations = true,
            enableFloatingText = true,
            enableBarAnimations = true,
            maxFPS = 60,
            lodLevel = LOD.HIGH
        )
    }
    
    /**
     * Проверка, включены ли частицы.
     */
    fun particlesEnabled(): Boolean = maxParticles > 0
    
    /**
     * Проверка, включены ли эффекты.
     */
    fun effectsEnabled(): Boolean = enableShake || enableFlash || enableTrails
}

/**
 * Уровни качества.
 */
enum class Quality {
    LOW,      // Низкое
    MEDIUM,   // Среднее
    HIGH      // Высокое
}

/**
 * Типы погоды.
 */
enum class WeatherType {
    NONE,     // Без погоды
    RAIN,     // Дождь
    SNOW,     // Снег
    LEAVES    // Листопад
}

/**
 * Качество теней.
 */
enum class ShadowQuality {
    NONE,     // Без теней
    LOW,      // Простые тени
    MEDIUM,   // Среднее качество
    HIGH      // Высокое качество
}

/**
 * Уровень детализации (LOD).
 */
enum class LOD {
    LOW,      // Низкая детализация
    MEDIUM,   // Средняя
    HIGH      // Высокая
}

/**
 * Конфигурация визуальных эффектов.
 * Содержит параметры для каждого типа эффекта.
 */
data class EffectsConfig(
    // ============================================================================
    // ЭФФЕКТЫ МОНЕТ
    // ============================================================================
    
    /** Количество частиц для сбора монеты */
    val coinSparkCount: Int = 15,
    
    /** Длительность эффекта монеты */
    val coinSparkDuration: Float = 0.5f,
    
    // ============================================================================
    // ЭФФЕКТЫ ИГРОКА
    // ============================================================================
    
    /** Количество частиц для следа игрока */
    val playerTrailCount: Int = 5,
    
    /** Частота эмиссии следа (частиц в секунду) */
    val playerTrailRate: Float = 20f,
    
    /** Количество частиц для прыжка */
    val jumpDustCount: Int = 10,
    
    /** Количество частиц для приземления */
    val landingDustCount: Int = 20,
    
    // ============================================================================
    // ЭФФЕКТЫ УРОНА
    // ============================================================================
    
    /** Количество частиц для удара */
    val hitSparkCount: Int = 20,
    
    /** Длительность эффекта удара */
    val hitSparkDuration: Float = 0.4f,
    
    /** Количество частиц для смерти */
    val deathExplosionCount: Int = 50,
    
    /** Длительность эффекта смерти */
    val deathExplosionDuration: Float = 1.0f,
    
    // ============================================================================
    // ЭФФЕКТЫ БОНУСОВ
    // ============================================================================
    
    /** Количество частиц для бонуса */
    val powerUpGlowCount: Int = 10,
    
    /** Частота эмиссии бонуса */
    val powerUpGlowRate: Float = 15f,
    
    /** Количество частиц для щита */
    val shieldBubbleCount: Int = 20,
    
    /** Частота эмиссии щита */
    val shieldBubbleRate: Float = 10f,
    
    // ============================================================================
    // ЭФФЕКТЫ ВРАГОВ
    // ============================================================================
    
    /** Количество частиц для попадания во врага */
    val enemyHitCount: Int = 15,
    
    /** Количество частиц для смерти врага */
    val enemyDeathCount: Int = 30,
    
    // ============================================================================
    // ЭФФЕКТЫ ОКРУЖЕНИЯ
    // ============================================================================
    
    /** Количество капель дождя */
    val rainCount: Int = 100,
    
    /** Количество снежинок */
    val snowCount: Int = 80,
    
    /** Количество листьев */
    val leavesCount: Int = 50,
    
    // ============================================================================
    // UI ЭФФЕКТЫ
    // ============================================================================
    
    /** Длительность всплывающего текста */
    val floatingTextDuration: Float = 1.5f,
    
    /** Скорость всплытия текста */
    val floatingTextSpeed: Float = 50f,
    
    /** Длительность анимации полоски здоровья */
    val healthBarAnimationDuration: Float = 0.3f,
    
    // ============================================================================
    // ЭФФЕКТЫ КАМЕРЫ
    // ============================================================================
    
    /** Интенсивность тряски при ударе */
    val hitShakeIntensity: Float = 8f,
    
    /** Длительность тряски при ударе */
    val hitShakeDuration: Float = 0.2f,
    
    /** Интенсивность тряски при смерти */
    val deathShakeIntensity: Float = 20f,
    
    /** Длительность тряски при смерти */
    val deathShakeDuration: Float = 0.5f,
    
    /** Длительность flash при ударе */
    val hitFlashDuration: Float = 0.1f,
    
    /** Длительность freeze frame при ударе */
    val hitFreezeDuration: Float = 0.1f
) {
    companion object {
        /** Конфигурация по умолчанию */
        val DEFAULT = EffectsConfig()
        
        /** Упрощённая конфигурация */
        val MINIMAL = EffectsConfig(
            coinSparkCount = 8,
            playerTrailCount = 2,
            jumpDustCount = 5,
            landingDustCount = 10,
            hitSparkCount = 10,
            deathExplosionCount = 25,
            powerUpGlowCount = 5,
            shieldBubbleCount = 10,
            enemyHitCount = 8,
            enemyDeathCount = 15,
            rainCount = 50,
            snowCount = 40,
            leavesCount = 25
        )
        
        /** Максимальная конфигурация */
        val MAXIMUM = EffectsConfig(
            coinSparkCount = 30,
            playerTrailCount = 10,
            playerTrailRate = 30f,
            jumpDustCount = 20,
            landingDustCount = 40,
            hitSparkCount = 40,
            hitSparkDuration = 0.6f,
            deathExplosionCount = 100,
            deathExplosionDuration = 1.5f,
            powerUpGlowCount = 20,
            powerUpGlowRate = 25f,
            shieldBubbleCount = 40,
            shieldBubbleRate = 15f,
            enemyHitCount = 25,
            enemyDeathCount = 50,
            rainCount = 200,
            snowCount = 150,
            leavesCount = 100,
            hitShakeIntensity = 12f,
            hitShakeDuration = 0.3f,
            deathShakeIntensity = 30f,
            deathShakeDuration = 0.8f
        )
    }
    
    /**
     * Получение конфигурации для уровня качества.
     */
    fun forQuality(quality: Quality): EffectsConfig {
        return when (quality) {
            Quality.LOW -> MINIMAL
            Quality.MEDIUM -> this
            Quality.HIGH -> MAXIMUM
        }
    }
}

/**
 * Builder для VisualConfig.
 */
class VisualConfigBuilder {
    private var particleQuality: Quality = Quality.MEDIUM
    private var maxParticles: Int = 2000
    private var maxEmitters: Int = 50
    private var enableShake: Boolean = true
    private var enableFlash: Boolean = true
    private var enableTrails: Boolean = true
    private var enableWeather: Boolean = true
    private var enableParallax: Boolean = true
    private var shadowQuality: ShadowQuality = ShadowQuality.MEDIUM
    private var enableGlow: Boolean = true
    private var maxFPS: Int = 60
    
    fun particleQuality(quality: Quality) = apply { particleQuality = quality }
    fun maxParticles(count: Int) = apply { maxParticles = count }
    fun maxEmitters(count: Int) = apply { maxEmitters = count }
    fun enableShake(enable: Boolean) = apply { enableShake = enable }
    fun enableFlash(enable: Boolean) = apply { enableFlash = enable }
    fun enableTrails(enable: Boolean) = apply { enableTrails = enable }
    fun enableWeather(enable: Boolean) = apply { enableWeather = enable }
    fun enableParallax(enable: Boolean) = apply { enableParallax = enable }
    fun shadowQuality(quality: ShadowQuality) = apply { shadowQuality = quality }
    fun enableGlow(enable: Boolean) = apply { enableGlow = enable }
    fun maxFPS(fps: Int) = apply { maxFPS = fps }
    
    fun build(): VisualConfig {
        return VisualConfig(
            particleQuality = particleQuality,
            maxParticles = maxParticles,
            maxEmitters = maxEmitters,
            enableShake = enableShake,
            enableFlash = enableFlash,
            enableTrails = enableTrails,
            enableWeather = enableWeather,
            enableParallax = enableParallax,
            shadowQuality = shadowQuality,
            enableGlow = enableGlow,
            maxFPS = maxFPS
        )
    }
}

/**
 * DSL функция для создания VisualConfig.
 */
fun visualConfig(block: VisualConfigBuilder.() -> Unit): VisualConfig {
    return VisualConfigBuilder().apply(block).build()
}

/**
 * DSL функция для создания EffectsConfig.
 */
fun effectsConfig(block: EffectsConfig.() -> Unit): EffectsConfig {
    val default = EffectsConfig.DEFAULT
    return default.copy(
        coinSparkCount = block.invoke(default).coinSparkCount,
        playerTrailCount = block.invoke(default).playerTrailCount,
        jumpDustCount = block.invoke(default).jumpDustCount
    )
}
