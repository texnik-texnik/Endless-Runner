package com.endlessrunner.collectibles

import android.graphics.Color
import com.endlessrunner.animation.Animation
import com.endlessrunner.animation.AnimationState
import com.endlessrunner.animation.Sprite
import com.endlessrunner.particles.Effects
import com.endlessrunner.particles.ParticleSystem
import com.endlessrunner.particles.burst

/**
 * Менеджер эффектов монеты.
 */
class CoinEffectsManager(
    private val coin: Coin
) {
    /** Система частиц */
    private val particleSystem: ParticleSystem = ParticleSystem.getInstance()
    
    /** Включены ли эффекты */
    var effectsEnabled: Boolean = true
    
    /** Таймер блеска */
    private var shineTimer: Float = 0f
    
    /** Частота блеска (секунды) */
    private val shineInterval: Float = 0.5f
    
    /**
     * Обновление эффектов.
     */
    fun update(deltaTime: Float) {
        if (!effectsEnabled) return
        
        // Обновление таймера блеска
        updateShine(deltaTime)
    }
    
    /**
     * Обновление блеска монеты.
     */
    private fun updateShine(deltaTime: Float) {
        shineTimer += deltaTime
        
        if (shineTimer >= shineInterval) {
            shineTimer = 0f
            
            // Небольшая частица блеска
            val x = coin.positionComponent?.x ?: 0f
            val y = coin.positionComponent?.y ?: 0f
            
            // Создаём эффект блеска
            particleSystem.emit(
                x = x,
                y = y - 10f,
                config = Effects.CoinSpark.copy(
                    maxParticles = 3,
                    emissionRate = 5f,
                    minLifetime = 0.2f,
                    maxLifetime = 0.4f
                ),
                count = 2
            )
        }
    }
    
    /**
     * Эффект сбора монеты.
     */
    fun onCollect() {
        if (!effectsEnabled) return
        
        val x = coin.positionComponent?.x ?: 0f
        val y = coin.positionComponent?.y ?: 0f
        
        // Искры при сборе
        Effects.CoinSpark.burst(x, y, 15)
    }
    
    /**
     * Эффект появления.
     */
    fun onSpawn() {
        if (!effectsEnabled) return
        
        val x = coin.positionComponent?.x ?: 0f
        val y = coin.positionComponent?.y ?: 0f
        
        // Небольшая вспышка при появлении
        particleSystem.emit(
            x = x,
            y = y,
            config = Effects.CoinSpark.copy(
                maxParticles = 5,
                minLifetime = 0.3f,
                maxLifetime = 0.5f
            ),
            count = 5
        )
    }
    
    /**
     * Очистка эффектов.
     */
    fun clear() {
        // Нет активных эмиттеров для очистки
    }
}

/**
 * Менеджер анимаций монеты.
 */
class CoinAnimationManager(
    private val coin: Coin
) {
    /** Спрайт монеты */
    private var sprite: Sprite? = null
    
    /** Текущий кадр вращения (для процедурной анимации) */
    private var rotationFrame: Int = 0
    
    /** Скорость вращения */
    private var rotationSpeed: Float = 2f
    
    /** Включены ли анимации */
    var animationsEnabled: Boolean = true
    
    /**
     * Установка спрайта монеты.
     */
    fun setSprite(sprite: Sprite) {
        this.sprite = sprite
        coin.renderComponent?.setSprite(sprite)
    }
    
    /**
     * Получение спрайта.
     */
    fun getSprite(): Sprite? = sprite
    
    /**
     * Обновление анимации.
     */
    fun update(deltaTime: Float) {
        if (!animationsEnabled) return
        
        sprite?.update(deltaTime)
        
        // Обновление компонента рендеринга
        if (coin.renderComponent?.useSpriteAnimation == true) {
            coin.renderComponent?.updateAnimation(deltaTime)
        }
        
        // Процедурное вращение
        updateRotation(deltaTime)
    }
    
    /**
     * Обновление вращения.
     */
    private fun updateRotation(deltaTime: Float) {
        if (coin.renderComponent == null) return
        
        rotationFrame += rotationSpeed * deltaTime * 60
        
        // Синусоидальное вращение для симуляции 3D
        val rotation = Math.sin(Math.toRadians((rotationFrame % 360).toDouble())).toFloat()
        coin.renderComponent?.scaleX = kotlin.math.abs(rotation)
    }
    
    /**
     * Установка скорости вращения.
     */
    fun setRotationSpeed(speed: Float) {
        rotationSpeed = speed
    }
    
    /**
     * Создание анимации вращения из спрайт-листа.
     */
    fun setupRotationAnimation(frames: List<android.graphics.Bitmap>, fps: Float = 15f) {
        if (frames.isEmpty()) return
        
        val animation = Animation(frames, frameDuration = 1f / fps, isLooping = true)
        
        if (sprite == null) {
            sprite = Sprite().apply {
                width = frames.first().width.toFloat()
                height = frames.first().height.toFloat()
            }
        }
        
        sprite?.addAnimation(AnimationState.Idle, animation)
        coin.renderComponent?.setSprite(sprite!!)
    }
    
    /**
     * Сброс анимации.
     */
    fun reset() {
        rotationFrame = 0
        sprite?.reset()
        coin.renderComponent?.scaleX = 1f
    }
}

/**
 * Extension свойства для Coin.
 */

/**
 * Эффекты монеты.
 */
val Coin.effectsManager: CoinEffectsManager by lazy {
    CoinEffectsManager(this)
}

/**
 * Анимации монеты.
 */
val Coin.animationManager: CoinAnimationManager by lazy {
    CoinAnimationManager(this)
}

/**
 * Инициализация визуальных эффектов монеты.
 */
fun Coin.setupVisuals() {
    effectsManager.onSpawn()
}

/**
 * Обновление визуальных эффектов.
 */
fun Coin.updateVisuals(deltaTime: Float) {
    if (isCollected) return
    
    animationManager.update(deltaTime)
    effectsManager.update(deltaTime)
}

/**
 * Обработка сбора с эффектами.
 */
fun Coin.collectWithEffects() {
    if (isCollected) return
    
    // Эффекты сбора
    effectsManager.onCollect()
    
    // Стандартный сбор
    isCollected = true
    renderComponent?.isVisible = false
}

/**
 * Очистка визуальных эффектов.
 */
fun Coin.clearVisuals() {
    effectsManager.clear()
    animationManager.reset()
}

/**
 * Создание монеты с визуальными эффектами.
 */
fun createCoinWithEffects(
    x: Float,
    y: Float,
    value: Int = 10
): Coin {
    return Coin(value).apply {
        positionComponent?.setPosition(x, y)
        setupVisuals()
    }
}

/**
 * Создание линии монет с эффектами.
 */
fun createCoinLineWithEffects(
    startX: Float,
    startY: Float,
    count: Int,
    spacing: Float = 80f,
    value: Int = 10
): List<Coin> {
    return List(count) { index ->
        createCoinWithEffects(
            x = startX + index * spacing,
            y = startY,
            value = value
        )
    }
}

/**
 * Создание дуги монет с эффектами.
 */
fun createCoinArcWithEffects(
    centerX: Float,
    centerY: Float,
    radius: Float,
    startAngle: Float,
    endAngle: Float,
    count: Int,
    value: Int = 10
): List<Coin> {
    return List(count) { index ->
        val progress = index.toFloat() / (count - 1)
        val angle = startAngle + (endAngle - startAngle) * progress
        val radian = Math.toRadians(angle.toDouble()).toFloat()
        
        createCoinWithEffects(
            x = centerX + kotlin.math.cos(radian) * radius,
            y = centerY + kotlin.math.sin(radian) * radius,
            value = value
        )
    }
}

/**
 * Special монета с эффектами.
 */
class BonusCoinWithEffects(
    value: Int = 50,
    private val bonusType: BonusCoin.BonusType = BonusCoin.BonusType.EXTRA_POINTS
) : BonusCoin(value, bonusType) {
    
    private val effectsManager = CoinEffectsManager(this)
    private val animationManager = CoinAnimationManager(this)
    
    init {
        setupVisuals()
        
        // Усиленный эффект для бонусной монеты
        effectsManager.effectsEnabled = true
    }
    
    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        
        if (!isCollected) {
            animationManager.update(deltaTime)
            effectsManager.update(deltaTime)
        }
    }
    
    override fun onCollect(collector: com.endlessrunner.entities.Entity?) {
        effectsManager.onCollect()
        
        // Дополнительный эффект для бонуса
        if (collector is com.endlessrunner.player.Player) {
            val x = positionComponent?.x ?: 0f
            val y = positionComponent?.y ?: 0f
            
            when (bonusType) {
                BonusCoin.BonusType.SHIELD -> {
                    Effects.ShieldBubble.burst(x, y, 30)
                }
                BonusCoin.BonusType.SPEED_BOOST -> {
                    Effects.SpeedBoost.burst(x, y, 20)
                }
                BonusCoin.BonusType.MAGNET -> {
                    Effects.MagnetField.burst(x, y, 25)
                }
                BonusCoin.BonusType.INVINCIBILITY -> {
                    Effects.MagicGlow.burst(x, y, 30)
                }
                else -> {}
            }
        }
        
        super.onCollect(collector)
    }
}
