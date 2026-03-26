package com.endlessrunner.player

import android.content.Context
import android.graphics.Canvas
import com.endlessrunner.audio.AudioManager
import com.endlessrunner.audio.GameAudioIntegration
import com.endlessrunner.audio.SoundLibrary
import com.endlessrunner.components.MovementComponent
import com.endlessrunner.components.PhysicsComponent
import com.endlessrunner.components.PositionComponent
import com.endlessrunner.components.RenderComponent
import com.endlessrunner.config.GameConfig
import com.endlessrunner.core.GameConstants
import com.endlessrunner.entities.Entity

/**
 * Класс игрока.
 * Наследуется от Entity и содержит все необходимые компоненты.
 *
 * @param config Конфигурация игрока
 * @param context Контекст для аудио (опционально)
 */
class Player(
    private val config: com.endlessrunner.config.PlayerConfig = com.endlessrunner.config.PlayerConfig(),
    private val context: Context? = null
) : Entity(tag = GameConstants.TAG_PLAYER) {

    /**
     * Аудио менеджер для воспроизведения звуков.
     */
    private val audioManager: AudioManager? by lazy {
        context?.let { AudioManager.getInstance(it) }
    }

    /**
     * Интеграция аудио с игровыми событиями.
     */
    private val audioIntegration: GameAudioIntegration? by lazy {
        context?.let { GameAudioIntegration.getInstance(it, audioManager!!) }
    }
    
    // ============================================================================
    // КОМПОНЕНТЫ (кэшированные ссылки для производительности)
    // ============================================================================
    
    /** Компонент позиции */
    val positionComponent: PositionComponent by lazy { getComponent() }
    
    /** Компонент рендеринга */
    val renderComponent: RenderComponent by lazy { getComponent() }
    
    /** Компонент физики */
    val physicsComponent: PhysicsComponent by lazy { getComponent() }
    
    /** Компонент движения */
    val movementComponent: MovementComponent by lazy { getComponent() }
    
    // ============================================================================
    // СВОЙСТВА ИГРОКА
    // ============================================================================
    
    /** Текущее состояние */
    var currentState: PlayerState = PlayerState.Idle
        private set
    
    /** Предыдущее состояние */
    var previousState: PlayerState? = null
        private set
    
    /** Здоровье */
    var health: Int = config.maxHealth
        private set
    
    /** Максимальное здоровье */
    val maxHealth: Int = config.maxHealth
    
    /** Счёт */
    var score: Int = 0
        private set
    
    /** Количество монет */
    var coins: Int = 0
        private set
    
    /** Комбо (последовательные монеты без падения) */
    var combo: Int = 0
        private set
    
    /** Неуязвим ли игрок */
    var isInvincible: Boolean = false
        internal set
    
    /** Мёртв ли игрок */
    val isDead: Boolean
        get() = currentState is PlayerState.Dead
    
    /** На земле ли игрок */
    val isGrounded: Boolean
        get() = movementComponent?.isGrounded == true
    
    /** В воздухе ли игрок */
    val isAirborne: Boolean
        get() = !isGrounded
    
    // ============================================================================
    // ТАЙМЕРЫ
    // ============================================================================
    
    /** Время неуязвимости */
    private var invincibleTimer: Float = 0f
    
    /** Время respawn'а */
    private var respawnTimer: Float = 0f
    
    // ============================================================================
    // ИНИЦИАЛИЗАЦИЯ
    // ============================================================================
    
    init {
        setupComponents()
    }
    
    /**
     * Настройка компонентов.
     */
    private fun setupComponents() {
        // Позиция
        addComponent(
            PositionComponent(
                x = 200f,
                y = 500f
            )
        )
        
        // Рендеринг
        addComponent(
            RenderComponent(
                color = config.color,
                width = config.width,
                height = config.height
            )
        )
        
        // Физика
        addComponent(
            PhysicsComponent(
                width = config.width,
                height = config.height,
                collisionLayer = GameConstants.LAYER_PLAYER
            )
        )
        
        // Движение
        addComponent(
            MovementComponent(
                speed = config.speed,
                jumpForce = config.jumpForce,
                gravity = config.gravity
            )
        )
    }
    
    // ============================================================================
    // ОБНОВЛЕНИЕ
    // ============================================================================
    
    override fun update(deltaTime: Float) {
        if (isDead) {
            updateDead(deltaTime)
            return
        }
        
        // Обновление состояния
        currentState.onUpdate(this, deltaTime)
        
        // Обновление таймера неуязвимости
        if (isInvincible) {
            invincibleTimer -= deltaTime
            if (invincibleTimer <= 0) {
                isInvincible = false
            }
        }
        
        // Обновление рендеринга (мигание при неуязвимости)
        updateInvincibilityVisuals()
        
        // Обновление комбо (сброс при падении)
        if (isAirborne && combo > 0) {
            // Комбо не сбрасывается в воздухе, только при получении урона
        }
        
        super.update(deltaTime)
    }
    
    /**
     * Обновление в состоянии смерти.
     */
    private fun updateDead(deltaTime: Float) {
        respawnTimer -= deltaTime
        // Игрок мёртв, ничего не делаем
    }
    
    /**
     * Обновление визуальных эффектов неуязвимости.
     */
    private fun updateInvincibilityVisuals() {
        if (isInvincible) {
            // Мигание (видимость меняется 4 раза в секунду)
            val blinkInterval = 0.125f
            val shouldShow = (invincibleTimer / blinkInterval) % 2 < 1
            renderComponent?.isVisible = shouldShow
        } else {
            renderComponent?.isVisible = true
        }
    }
    
    // ============================================================================
    // УПРАВЛЕНИЕ СОСТОЯНИЕМ
    // ============================================================================
    
    /**
     * Изменение состояния игрока.
     * 
     * @param newState Новое состояние
     * @return true если переход успешен
     */
    fun changeState(newState: PlayerState): Boolean {
        if (currentState == newState) return false
        if (isDead && newState !is PlayerState.Dead) return false
        if (!currentState.canTransitionTo(newState)) return false
        
        // Сохранение предыдущего состояния
        if (newState !is PlayerState.Invincible) {
            previousState = currentState
        }
        
        // Выход из текущего состояния
        currentState.onExit(this)
        
        // Вход в новое состояние
        currentState = newState
        currentState.reset()
        currentState.onEnter(this)
        
        return true
    }
    
    /**
     * Обновление состояния на основе движения.
     */
    fun updateStateFromMovement() {
        if (isDead || isInvincible) return
        
        val movement = movementComponent ?: return
        
        when {
            !movement.isGrounded && movement.isMovingUp() -> {
                changeState(PlayerState.Jumping)
            }
            !movement.isGrounded -> {
                changeState(PlayerState.Falling)
            }
            movement.isMoving() -> {
                changeState(PlayerState.Running)
            }
            else -> {
                changeState(PlayerState.Idle)
            }
        }
    }
    
    // ============================================================================
    // ДЕЙСТВИЯ
    // ============================================================================
    
    /**
     * Прыжок.
     *
     * @return true если прыжок выполнен
     */
    fun jump(): Boolean {
        if (isDead || isInvincible) return false

        val jumped = movementComponent?.jump() == true
        if (jumped) {
            changeState(PlayerState.Jumping)
            // Воспроизведение звука прыжка
            audioManager?.playSfx(SoundLibrary.PLAYER_JUMP)
        }
        return jumped
    }
    
    /**
     * Движение влево.
     */
    fun moveLeft() {
        if (isDead || isInvincible) return
        movementComponent?.moveLeft()
        renderComponent?.setFacingRight(false)
    }
    
    /**
     * Движение вправо.
     */
    fun moveRight() {
        if (isDead || isInvincible) return
        movementComponent?.moveRight()
        renderComponent?.setFacingRight(true)
    }
    
    /**
     * Остановка.
     */
    fun stop() {
        movementComponent?.stop()
    }
    
    /**
     * Получение урона.
     *
     * @param damage Количество урона
     * @return true если урон применён
     */
    fun takeDamage(damage: Int = 1): Boolean {
        if (isDead || isInvincible) return false

        health -= damage

        if (health <= 0) {
            die()
            return true
        }

        // Воспроизведение звука получения урона
        audioManager?.playSfx(SoundLibrary.PLAYER_HIT)

        // Включение неуязвимости
        isInvincible = true
        invincibleTimer = config.invincibilityTimeMs / 1000f
        changeState(PlayerState.Invincible(config.invincibilityTimeMs / 1000f))

        // Сброс комбо
        combo = 0

        return true
    }
    
    /**
     * Смерть игрока.
     */
    fun die() {
        if (isDead) return

        health = 0
        changeState(PlayerState.Dead)
        
        // Воспроизведение звука смерти
        audioManager?.playSfx(SoundLibrary.PLAYER_DEATH)
        
        onDeath()
    }
    
    /**
     * Вызывается при смерти.
     */
    protected open fun onDeath() {
        // Переопределяется в наследниках или через callback
    }
    
    /**
     * Добавление очков.
     * 
     * @param points Количество очков
     * @param isCombo Является ли частью комбо
     */
    fun addScore(points: Int, isCombo: Boolean = false) {
        val multiplier = if (isCombo) {
            1 + (combo / 10) * 0.5f
        } else {
            1f
        }
        score += (points * multiplier).toInt()
        
        if (isCombo) {
            combo++
        }
    }
    
    /**
     * Сбор монеты.
     * 
     * @param value Стоимость монеты
     */
    fun collectCoin(value: Int = GameConstants.BASE_COIN_SCORE) {
        coins++
        addScore(value, isCombo = true)
    }
    
    /**
     * Сброс комбо.
     */
    fun resetCombo() {
        combo = 0
    }
    
    /**
     * Лечение.
     * 
     * @param amount Количество здоровья
     */
    fun heal(amount: Int = 1) {
        health = (health + amount).coerceAtMost(maxHealth)
    }
    
    /**
     * Полное восстановление.
     */
    fun fullHeal() {
        health = maxHealth
    }
    
    /**
     * Респаун игрока.
     * 
     * @param x Позиция X
     * @param y Позиция Y
     */
    fun respawn(x: Float = 200f, y: Float = 500f) {
        positionComponent?.setPosition(x, y)
        positionComponent?.stop()
        health = maxHealth
        isInvincible = false
        combo = 0
        changeState(PlayerState.Idle)
    }
    
    // ============================================================================
    // УТИЛИТЫ
    // ============================================================================
    
    /**
     * Получение процента здоровья.
     */
    fun getHealthPercent(): Float = health.toFloat() / maxHealth
    
    /**
     * Проверка, может ли игрок двигаться.
     */
    fun canMove(): Boolean = !isDead && !isInvincible
    
    /**
     * Проверка, может ли игрок прыгать.
     */
    fun canJump(): Boolean = canMove() && movementComponent?.isJumpAvailable() == true
    
    override fun reset() {
        super.reset()
        health = maxHealth
        score = 0
        coins = 0
        combo = 0
        isInvincible = false
        invincibleTimer = 0f
        respawnTimer = 0f
        changeState(PlayerState.Idle)
    }
    
    override fun toString(): String {
        return "Player(health=$health, score=$score, state=${currentState::class.simpleName})"
    }
}

/**
 * Extension property для получения текущего состояния как Invincible.
 */
val Player.invincibleState: PlayerState.Invincible?
    get() = currentState as? PlayerState.Invincible

/**
 * Extension property для получения времени до конца неуязвимости.
 */
val Player.invincibleTimeRemaining: Float
    get() = invincibleState?.let { it.duration - it.stateTime } ?: 0f
