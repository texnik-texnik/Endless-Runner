package com.endlessrunner.player

import android.graphics.Color
import com.endlessrunner.animation.Animation
import com.endlessrunner.animation.AnimationState
import com.endlessrunner.animation.Sprite
import com.endlessrunner.particles.Effects
import com.endlessrunner.particles.ParticleEmitter
import com.endlessrunner.particles.ParticleSystem
import com.endlessrunner.particles.burst
import com.endlessrunner.particles.continuous
import com.endlessrunner.particles.emit

/**
 * Менеджер эффектов игрока.
 * Управляет частицами и визуальными эффектами.
 */
class PlayerEffectsManager(
    private val player: Player
) {
    /** Эмиттер для следа игрока */
    private var trailEmitter: ParticleEmitter? = null
    
    /** Система частиц */
    private val particleSystem: ParticleSystem = ParticleSystem.getInstance()
    
    /** Включены ли эффекты */
    var effectsEnabled: Boolean = true
    
    /**
     * Инициализация эффектов.
     */
    fun initialize() {
        // Создание эмиттера для следа
        trailEmitter = particleSystem.createEmitter(Effects.PlayerTrail)
        trailEmitter?.isActive = false
    }
    
    /**
     * Обновление эффектов.
     */
    fun update(deltaTime: Float) {
        if (!effectsEnabled) return
        
        // Обновление следа при движении
        updateTrail(deltaTime)
    }
    
    /**
     * Обновление следа игрока.
     */
    private fun updateTrail(deltaTime: Float) {
        val trail = trailEmitter ?: return
        
        if (player.currentState is AnimationState.Running && effectsEnabled) {
            if (!trail.isActive) {
                trail.start()
            }
            trail.x = player.positionComponent?.x ?: 0f
            trail.y = (player.positionComponent?.y ?: 0f) + 20f
        } else {
            if (trail.isActive) {
                trail.stop()
            }
        }
    }
    
    /**
     * Эффект прыжка.
     */
    fun onJump() {
        if (!effectsEnabled) return
        
        val x = player.positionComponent?.x ?: 0f
        val y = (player.positionComponent?.y ?: 0f) + 30f
        
        Effects.JumpDust.emit(x, y, 15)
    }
    
    /**
     * Эффект приземления.
     */
    fun onLand() {
        if (!effectsEnabled) return
        
        val x = player.positionComponent?.x ?: 0f
        val y = (player.positionComponent?.y ?: 0f) + 30f
        
        Effects.LandingDust.burst(x, y, 25)
    }
    
    /**
     * Эффект получения урона.
     */
    fun onHit() {
        if (!effectsEnabled) return
        
        val x = player.positionComponent?.x ?: 0f
        val y = (player.positionComponent?.y ?: 0f)
        
        Effects.HitSpark.burst(x, y, 30)
    }
    
    /**
     * Эффект смерти.
     */
    fun onDeath() {
        if (!effectsEnabled) return
        
        val x = player.positionComponent?.x ?: 0f
        val y = (player.positionComponent?.y ?: 0f)
        
        Effects.DeathExplosion.burst(x, y, 60)
    }
    
    /**
     * Эффект сбора монеты.
     */
    fun onCoinCollect(x: Float, y: Float) {
        if (!effectsEnabled) return
        Effects.CoinSpark.burst(x, y, 15)
    }
    
    /**
     * Эффект щита.
     */
    fun enableShield() {
        if (!effectsEnabled) return
        
        val shield = particleSystem.createEmitter(Effects.ShieldBubble)
        shield.isActive = true
    }
    
    /**
     * Эффект ускорения.
     */
    fun enableSpeedBoost() {
        if (!effectsEnabled) return
        
        val boost = particleSystem.createEmitter(Effects.SpeedBoost)
        boost.isActive = true
        boost.x = player.positionComponent?.x ?: 0f
        boost.y = player.positionComponent?.y ?: 0f
    }
    
    /**
     * Очистка всех эффектов.
     */
    fun clear() {
        trailEmitter?.stop()
        trailEmitter = null
    }
    
    /**
     * Освобождение ресурсов.
     */
    fun dispose() {
        clear()
    }
}

/**
 * Менеджер анимаций игрока.
 * Управляет спрайтами и переходами между состояниями.
 */
class PlayerAnimationManager(
    private val player: Player
) {
    /** Спрайт игрока */
    private var sprite: Sprite? = null
    
    /** Машина состояний анимаций */
    private val stateMachine: AnimationStateMachine = AnimationStateMachine()
    
    /** Кэш анимаций */
    private val animations: MutableMap<AnimationState, Animation> = mutableMapOf()
    
    /** Включены ли анимации */
    var animationsEnabled: Boolean = true
    
    /** Callback на смену анимации */
    var onAnimationChanged: ((AnimationState) -> Unit)? = null
    
    /**
     * Регистрация анимации для состояния.
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
        player.renderComponent?.setSprite(sprite)
    }
    
    /**
     * Получение спрайта.
     */
    fun getSprite(): Sprite? = sprite
    
    /**
     * Обновление анимаций.
     */
    fun update(deltaTime: Float) {
        if (!animationsEnabled) return
        
        // Обновление машины состояний
        stateMachine.update(deltaTime)
        
        // Определение текущего состояния
        val targetState = getCurrentAnimationState()
        
        // Смена состояния при необходимости
        if (stateMachine.currentState != targetState) {
            stateMachine.setState(targetState)
            sprite?.setState(targetState)
            onAnimationChanged?.invoke(targetState)
        }
        
        // Обновление спрайта
        sprite?.update(deltaTime)
        
        // Обновление компонента рендеринга
        if (player.renderComponent?.useSpriteAnimation == true) {
            player.renderComponent?.updateAnimation(deltaTime)
        }
    }
    
    /**
     * Определение текущего состояния анимации.
     */
    private fun getCurrentAnimationState(): AnimationState {
        return when {
            player.isDead -> AnimationState.Dead
            player.currentState is PlayerState.Hit -> AnimationState.Hit
            !player.isGrounded -> {
                val velocity = player.movementComponent?.velocityY ?: 0f
                if (velocity < 0) AnimationState.Jumping else AnimationState.Falling
            }
            player.movementComponent?.isMoving() == true -> AnimationState.Running
            else -> AnimationState.Idle
        }
    }
    
    /**
     * Принудительная установка состояния.
     */
    fun setState(state: AnimationState) {
        stateMachine.setState(state)
        sprite?.setState(state)
    }
    
    /**
     * Сброс анимаций.
     */
    fun reset() {
        stateMachine.reset()
        sprite?.reset()
    }
    
    /**
     * Проверка, завершена ли анимация.
     */
    fun isAnimationFinished(): Boolean {
        return sprite?.isAnimationFinished() ?: false
    }
}

/**
 * Extension свойства и функции для Player.
 */

/**
 * Эффекты игрока (ленивая инициализация).
 */
val Player.effectsManager: PlayerEffectsManager by lazy {
    PlayerEffectsManager(this)
}

/**
 * Анимации игрока (ленивая инициализация).
 */
val Player.animationManager: PlayerAnimationManager by lazy {
    PlayerAnimationManager(this)
}

/**
 * Инициализация эффектов и анимаций игрока.
 */
fun Player.setupVisuals() {
    effectsManager.initialize()
    
    // Создание процедурных анимаций (заглушки для примера)
    setupPlaceholderAnimations()
}

/**
 * Создание процедурных анимаций-заглушек.
 * В реальном проекте загружаются из спрайт-листов.
 */
private fun Player.setupPlaceholderAnimations() {
    val config = this.config
    
    // Создаём простые "анимации" из одного кадра (цветные прямоугольники)
    // В реальном проекте здесь будут загруженные Bitmap
    
    // Idle анимация
    val idleAnim = createPlaceholderAnimation(config.color, 4)
    animationManager.registerAnimation(AnimationState.Idle, idleAnim)
    
    // Running анимация
    val runAnim = createPlaceholderAnimation(config.color, 6, 0.08f)
    animationManager.registerAnimation(AnimationState.Running, runAnim)
    
    // Jumping анимация
    val jumpAnim = createPlaceholderAnimation(config.color, 1)
    animationManager.registerAnimation(AnimationState.Jumping, jumpAnim)
    
    // Falling анимация
    val fallAnim = createPlaceholderAnimation(config.color, 1)
    animationManager.registerAnimation(AnimationState.Falling, fallAnim)
    
    // Hit анимация
    val hitAnim = createPlaceholderAnimation(Color.RED, 3, 0.1f, false)
    animationManager.registerAnimation(AnimationState.Hit, hitAnim)
    
    // Dead анимация
    val deadAnim = createPlaceholderAnimation(Color.GRAY, 1)
    animationManager.registerAnimation(AnimationState.Dead, deadAnim)
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
    // В реальном проекте здесь будут загруженные Bitmap
    // Создаём пустые Bitmap для демонстрации
    val frames = List(frameCount) {
        android.graphics.Bitmap.createBitmap(32, 32, android.graphics.Bitmap.Config.ARGB_8888)
            .apply {
                val canvas = android.graphics.Canvas(this)
                val paint = android.graphics.Paint().apply {
                    this.color = color
                }
                canvas.drawCircle(16f, 16f, 14f, paint)
            }
    }
    
    return Animation(frames, frameDuration, isLooping)
}

/**
 * Обновление визуальных эффектов игрока.
 */
fun Player.updateVisuals(deltaTime: Float) {
    animationManager.update(deltaTime)
    effectsManager.update(deltaTime)
}

/**
 * Очистка визуальных эффектов.
 */
fun Player.clearVisuals() {
    effectsManager.clear()
    animationManager.reset()
}

/**
 * Освобождение ресурсов визуальных эффектов.
 */
fun Player.disposeVisuals() {
    effectsManager.dispose()
}
