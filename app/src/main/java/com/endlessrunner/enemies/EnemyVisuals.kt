package com.endlessrunner.enemies

import android.graphics.Color
import com.endlessrunner.animation.Animation
import com.endlessrunner.animation.AnimationState
import com.endlessrunner.animation.Sprite
import com.endlessrunner.particles.Effects
import com.endlessrunner.particles.ParticleSystem
import com.endlessrunner.particles.burst
import com.endlessrunner.particles.continuous

/**
 * Менеджер эффектов врага.
 */
class EnemyEffectsManager(
    private val enemy: Enemy
) {
    /** Система частиц */
    private val particleSystem: ParticleSystem = ParticleSystem.getInstance()
    
    /** Включены ли эффекты */
    var effectsEnabled: Boolean = true
    
    /** Эмиттер для постоянного эффекта (огонь, яд и т.д.) */
    private var continuousEmitter: com.endlessrunner.particles.ParticleEmitter? = null
    
    /**
     * Обновление эффектов.
     */
    fun update(deltaTime: Float) {
        if (!effectsEnabled) return
        
        // Обновление постоянного эмиттера
        continuousEmitter?.let { emitter ->
            if (emitter.isActive) {
                emitter.x = enemy.positionComponent?.x ?: 0f
                emitter.y = (enemy.positionComponent?.y ?: 0f) - 20f
            }
        }
    }
    
    /**
     * Эффект получения урона.
     */
    fun onHit() {
        if (!effectsEnabled) return
        
        val x = enemy.positionComponent?.x ?: 0f
        val y = enemy.positionComponent?.y ?: 0f
        
        Effects.EnemyHit.burst(x, y, 20)
    }
    
    /**
     * Эффект смерти.
     */
    fun onDeath() {
        if (!effectsEnabled) return
        
        val x = enemy.positionComponent?.x ?: 0f
        val y = enemy.positionComponent?.y ?: 0f
        
        Effects.EnemyDeath.burst(x, y, 40)
        
        // Тряска камеры при смерти крупного врага
        if (enemy.type.baseDamage >= 2) {
            // TODO: Интеграция с CameraSystem
        }
    }
    
    /**
     * Эффект появления.
     */
    fun onSpawn() {
        if (!effectsEnabled) return
        
        val x = enemy.positionComponent?.x ?: 0f
        val y = enemy.positionComponent?.y ?: 0f
        
        // Небольшая вспышка при появлении
        Effects.Smoke.burst(x, y, 10)
    }
    
    /**
     * Запуск постоянного эффекта.
     */
    fun startContinuousEffect(config: com.endlessrunner.particles.ParticleConfig) {
        if (!effectsEnabled) return
        
        stopContinuousEffect()
        
        continuousEmitter = particleSystem.createEmitter(config)
        continuousEmitter?.start()
    }
    
    /**
     * Остановка постоянного эффекта.
     */
    fun stopContinuousEffect() {
        continuousEmitter?.stop()
        continuousEmitter = null
    }
    
    /**
     * Эффект для конкретного типа врага.
     */
    fun onTypeEffect() {
        when (enemy.type) {
            EnemyType.FLYING -> {
                // След для летающих врагов
                startContinuousEffect(Effects.Smoke.copy(
                    emissionRate = 5f,
                    maxParticles = 10
                ))
            }
            EnemyType.FIRE -> {
                // Огонь для огненных врагов
                startContinuousEffect(Effects.Fire.copy(
                    emissionRate = 15f,
                    maxParticles = 20
                ))
            }
            EnemyType.POISON -> {
                // Ядовитое облако
                startContinuousEffect(Effects.PoisonCloud.copy(
                    emissionRate = 3f,
                    maxParticles = 15
                ))
            }
            else -> {
                // Нет постоянного эффекта
            }
        }
    }
    
    /**
     * Очистка эффектов.
     */
    fun clear() {
        stopContinuousEffect()
    }
}

/**
 * Менеджер анимаций врага.
 */
class EnemyAnimationManager(
    private val enemy: Enemy
) {
    /** Спрайт врага */
    private var sprite: Sprite? = null
    
    /** Кэш анимаций */
    private val animations: MutableMap<AnimationState, Animation> = mutableMapOf()
    
    /** Включены ли анимации */
    var animationsEnabled: Boolean = true
    
    /** Текущее состояние */
    private var currentState: AnimationState = AnimationState.Idle
    
    /**
     * Регистрация анимации.
     */
    fun registerAnimation(state: AnimationState, animation: Animation) {
        animations[state] = animation
        
        if (sprite == null) {
            sprite = Sprite().apply {
                width = animation.frames.firstOrNull()?.width?.toFloat() ?: 64f
                height = animation.frames.firstOrNull()?.height?.toFloat() ?: 64f
            }
        }
        
        sprite?.addAnimation(state, animation)
    }
    
    /**
     * Установка спрайта.
     */
    fun setSprite(sprite: Sprite) {
        this.sprite = sprite
        enemy.renderComponent?.setSprite(sprite)
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
        
        // Определение текущего состояния
        val targetState = getCurrentState()
        
        // Смена состояния при необходимости
        if (currentState != targetState) {
            currentState = targetState
            sprite?.setState(targetState)
        }
        
        // Обновление спрайта
        sprite?.update(deltaTime)
        
        // Обновление компонента рендеринга
        if (enemy.renderComponent?.useSpriteAnimation == true) {
            enemy.renderComponent?.updateAnimation(deltaTime)
        }
    }
    
    /**
     * Определение текущего состояния.
     */
    private fun getCurrentState(): AnimationState {
        return when {
            enemy.isDestroyed -> AnimationState.Dead
            enemy.currentSpeed > 0 -> AnimationState.Running
            else -> AnimationState.Idle
        }
    }
    
    /**
     * Установка состояния.
     */
    fun setState(state: AnimationState) {
        currentState = state
        sprite?.setState(state)
    }
    
    /**
     * Сброс анимации.
     */
    fun reset() {
        currentState = AnimationState.Idle
        sprite?.reset()
    }
    
    /**
     * Создание анимаций для типа врага.
     */
    fun setupAnimationsForType() {
        // Создаём процедурные анимации-заглушки
        // В реальном проекте загружаются из спрайт-листов
        
        val color = enemy.type.debugColor
        
        // Idle
        val idleAnim = createPlaceholderAnimation(color, 4, 0.12f)
        registerAnimation(AnimationState.Idle, idleAnim)
        
        // Running
        val runAnim = createPlaceholderAnimation(color, 6, 0.08f)
        registerAnimation(AnimationState.Running, runAnim)
        
        // Hit
        val hitAnim = createPlaceholderAnimation(Color.RED, 3, 0.1f, false)
        registerAnimation(AnimationState.Hit, hitAnim)
        
        // Dead
        val deadAnim = createPlaceholderAnimation(Color.GRAY, 1)
        registerAnimation(AnimationState.Dead, deadAnim)
    }
    
    /**
     * Создание простой анимации-заглушки.
     */
    private fun createPlaceholderAnimation(
        color: Int,
        frameCount: Int,
        frameDuration: Float = 0.1f,
        isLooping: Boolean = true
    ): Animation {
        val frames = List(frameCount) {
            android.graphics.Bitmap.createBitmap(32, 32, android.graphics.Bitmap.Config.ARGB_8888)
                .apply {
                    val canvas = android.graphics.Canvas(this)
                    val paint = android.graphics.Paint().apply {
                        this.color = color
                    }
                    
                    // Рисуем форму в зависимости от типа врага
                    when (enemy.type) {
                        EnemyType.FLYING -> canvas.drawCircle(16f, 16f, 14f, paint)
                        EnemyType.JUMPER -> {
                            val path = android.graphics.Path().apply {
                                moveTo(16f, 2f)
                                lineTo(30f, 30f)
                                lineTo(2f, 30f)
                                close()
                            }
                            canvas.drawPath(path, paint)
                        }
                        else -> canvas.drawRect(4f, 4f, 28f, 28f, paint)
                    }
                }
        }
        
        return Animation(frames, frameDuration, isLooping)
    }
}

/**
 * Extension свойства для Enemy.
 */

/**
 * Эффекты врага.
 */
val Enemy.effectsManager: EnemyEffectsManager by lazy {
    EnemyEffectsManager(this)
}

/**
 * Анимации врага.
 */
val Enemy.animationManager: EnemyAnimationManager by lazy {
    EnemyAnimationManager(this)
}

/**
 * Инициализация визуальных эффектов врага.
 */
fun Enemy.setupVisuals() {
    effectsManager.onSpawn()
    effectsManager.onTypeEffect()
    animationManager.setupAnimationsForType()
}

/**
 * Обновление визуальных эффектов.
 */
fun Enemy.updateVisuals(deltaTime: Float) {
    if (isDestroyed) return
    
    animationManager.update(deltaTime)
    effectsManager.update(deltaTime)
}

/**
 * Обработка получения урона с эффектами.
 */
fun Enemy.hitWithEffects() {
    effectsManager.onHit()
}

/**
 * Обработка смерти с эффектами.
 */
fun Enemy.deathWithEffects() {
    effectsManager.onDeath()
    isDestroyed = true
}

/**
 * Очистка визуальных эффектов.
 */
fun Enemy.clearVisuals() {
    effectsManager.clear()
    animationManager.reset()
}

/**
 * Освобождение ресурсов.
 */
fun Enemy.disposeVisuals() {
    effectsManager.clear()
}

/**
 * Создание врага с визуальными эффектами.
 */
fun createEnemyWithEffects(
    type: EnemyType = EnemyType.STATIC,
    x: Float = 0f,
    y: Float = 0f,
    behavior: EnemyBehavior? = null
): Enemy {
    return Enemy.acquire(type, behavior).apply {
        positionComponent?.setPosition(x, y)
        setupVisuals()
    }
}

/**
 * Factory для создания врагов с эффектами.
 */
object EnemyFactoryWithEffects {
    
    /**
     * Создание летающего врага.
     */
    fun createFlying(x: Float, y: Float): Enemy {
        return createEnemyWithEffects(
            type = EnemyType.FLYING,
            x = x,
            y = y,
            behavior = PatrolBehavior(
                startX = x - 100f,
                endX = x + 100f,
                speed = 80f
            )
        )
    }
    
    /**
     * Создание прыгающего врага.
     */
    fun createJumper(x: Float, y: Float): Enemy {
        return createEnemyWithEffects(
            type = EnemyType.JUMPER,
            x = x,
            y = y,
            behavior = JumpBehavior()
        )
    }
    
    /**
     * Создание огненного врага.
     */
    fun createFire(x: Float, y: Float): Enemy {
        return createEnemyWithEffects(
            type = EnemyType.FIRE,
            x = x,
            y = y,
            behavior = PatrolBehavior(
                startX = x - 50f,
                endX = x + 50f,
                speed = 50f
            )
        ).apply {
            effectsManager.startContinuousEffect(Effects.Fire)
        }
    }
    
    /**
     * Создание ядовитого врага.
     */
    fun createPoison(x: Float, y: Float): Enemy {
        return createEnemyWithEffects(
            type = EnemyType.POISON,
            x = x,
            y = y,
            behavior = StaticBehavior()
        ).apply {
            effectsManager.startContinuousEffect(Effects.PoisonCloud)
        }
    }
}

/**
 * Простые поведения для врагов.
 */

class PatrolBehavior(
    private val startX: Float,
    private val endX: Float,
    private val speed: Float
) : EnemyBehavior {
    
    private var direction: Float = 1f
    
    override fun update(enemy: Enemy, deltaTime: Float) {
        val x = enemy.positionComponent?.x ?: 0f
        
        if (x >= endX) {
            direction = -1f
            enemy.renderComponent?.flipHorizontal = true
        } else if (x <= startX) {
            direction = 1f
            enemy.renderComponent?.flipHorizontal = false
        }
        
        enemy.positionComponent?.x = x + direction * speed * deltaTime
    }
    
    override fun onSpawn(enemy: Enemy) {
        enemy.currentSpeed = speed
    }
    
    override fun onDestroy(enemy: Enemy) {}
    override fun reset() {}
}

class JumpBehavior : EnemyBehavior {
    
    private var jumpTimer: Float = 0f
    private val jumpInterval: Float = 2f
    
    override fun update(enemy: Enemy, deltaTime: Float) {
        jumpTimer += deltaTime
        
        if (jumpTimer >= jumpInterval) {
            jumpTimer = 0f
            enemy.movementComponent?.jump()
        }
    }
    
    override fun onSpawn(enemy: Enemy) {}
    override fun onDestroy(enemy: Enemy) {}
    override fun reset() { jumpTimer = 0f }
}

class StaticBehavior : EnemyBehavior {
    override fun update(enemy: Enemy, deltaTime: Float) {}
    override fun onSpawn(enemy: Enemy) {}
    override fun onDestroy(enemy: Enemy) {}
    override fun reset() {}
}
