package com.endlessrunner.components

import com.endlessrunner.core.GameConstants
import kotlin.math.abs

/**
 * Компонент движения и управления.
 * Обрабатывает гравитацию, прыжки и перемещение.
 * 
 * @param speed Скорость движения
 * @param jumpForce Сила прыжка
 * @param gravity Гравитация
 */
open class MovementComponent(
    var speed: Float = GameConstants.DEFAULT_PLAYER_SPEED,
    var jumpForce: Float = GameConstants.DEFAULT_JUMP_FORCE,
    var gravity: Float = GameConstants.DEFAULT_GRAVITY
) : Component() {
    
    // ============================================================================
    // СВОЙСТВА ДВИЖЕНИЯ
    // ============================================================================
    
    /** Направление движения (-1 = влево, 0 = нет, 1 = вправо) */
    var moveDirection: Int = 0
        set(value) {
            field = value.coerceIn(-1, 1)
        }
    
    /** Целевая скорость по X */
    var targetVelocityX: Float = 0f
    
    // ============================================================================
    // СОСТОЯНИЕ
    // ============================================================================
    
    /** Находится ли на земле */
    var isGrounded: Boolean = false
        private set
    
    /** Может ли прыгать */
    var canJump: Boolean = true
        private set
    
    /** Количество прыжков осталось (для двойных прыжков) */
    var jumpsRemaining: Int = 1
        private set
    
    /** Максимальное количество прыжков */
    var maxJumps: Int = 1
    
    /** Время последнего прыжка */
    private var lastJumpTime: Float = 0f
    
    /** Время на земле (для коyот-тайма) */
    private var timeOnGround: Float = 0f
    
    // ============================================================================
    // ТАЙМЕРЫ
    // ============================================================================
    
    /** Coyote time - время после схода с платформы, когда ещё можно прыгнуть */
    var coyoteTime: Float = 0.1f
    
    /** Время буфера прыжка - запоминание нажатия прыжка перед приземлением */
    var jumpBufferTime: Float = 0.1f
    
    /** Таймер coyote time */
    private var coyoteTimer: Float = 0f
    
    /** Таймер буфера прыжка */
    private var jumpBufferTimer: Float = 0f
    
    // ============================================================================
    // ОГРАНИЧЕНИЯ
    // ============================================================================
    
    /** Минимальная скорость движения */
    var minSpeed: Float = 0f
    
    /** Максимальная скорость движения */
    var maxSpeed: Float = Float.MAX_VALUE
    
    /** Торможение на земле */
    var groundDeceleration: Float = 2000f
    
    /** Торможение в воздухе */
    var airDeceleration: Float = 500f
    
    /** Ускорение */
    var acceleration: Float = 3000f
    
    // ============================================================================
    // ВВОД
    // ============================================================================
    
    /** Запрошен прыжок */
    var jumpRequested: Boolean = false
        set(value) {
            if (value && !field) {
                jumpBufferTimer = jumpBufferTime
            }
            field = value
        }
    
    /** Запрошено движение влево */
    var moveLeftRequested: Boolean = false
    
    /** Запрошено движение вправо */
    var moveRightRequested: Boolean = false
    
    // ============================================================================
    // ОБНОВЛЕНИЕ
    // ============================================================================
    
    override fun onUpdate(deltaTime: Float) {
        super.onUpdate(deltaTime)
        
        updateTimers(deltaTime)
        updateMovement(deltaTime)
        updateGravity(deltaTime)
        updateJump()
    }
    
    /**
     * Обновление таймеров.
     */
    private fun updateTimers(deltaTime: Float) {
        // Coyote time
        if (!isGrounded && coyoteTimer > 0) {
            coyoteTimer -= deltaTime
        }
        
        // Jump buffer
        if (jumpBufferTimer > 0) {
            jumpBufferTimer -= deltaTime
        }
        
        // Время на земле
        if (isGrounded) {
            timeOnGround += deltaTime
        } else {
            timeOnGround = 0f
        }
    }
    
    /**
     * Обновление движения.
     */
    private fun updateMovement(deltaTime: Float) {
        val position = entity?.getComponent<PositionComponent>() ?: return
        
        // Определение направления
        moveDirection = when {
            moveLeftRequested && !moveRightRequested -> -1
            moveRightRequested && !moveLeftRequested -> 1
            else -> 0
        }
        
        // Целевая скорость
        targetVelocityX = moveDirection * speed
        
        // Применение ускорения или торможения
        val currentVelX = position.vx
        val deceleration = if (isGrounded) groundDeceleration else airDeceleration
        
        if (moveDirection == 0) {
            // Торможение
            if (abs(currentVelX) > 0.1f) {
                val newVelX = currentVelX - kotlin.math.sign(currentVelX) * deceleration * deltaTime
                position.vx = if (abs(newVelX) < abs(currentVelX)) newVelX else 0f
            } else {
                position.vx = 0f
            }
        } else {
            // Ускорение к целевой скорости
            if (currentVelX < targetVelocityX) {
                position.vx = (currentVelX + acceleration * deltaTime).coerceAtMost(targetVelocityX)
            } else if (currentVelX > targetVelocityX) {
                position.vx = (currentVelX - acceleration * deltaTime).coerceAtLeast(targetVelocityX)
            }
        }
        
        // Ограничение скорости
        position.vx = position.vx.coerceIn(-maxSpeed, maxSpeed)
    }
    
    /**
     * Применение гравитации.
     */
    private fun updateGravity(deltaTime: Float) {
        val position = entity?.getComponent<PositionComponent>() ?: return
        
        // Применение гравитации
        position.vy += gravity * deltaTime
        
        // Ограничение терминальной скорости
        if (position.vy > GameConstants.TERMINAL_VELOCITY) {
            position.vy = GameConstants.TERMINAL_VELOCITY
        }
    }
    
    /**
     * Обновление прыжка.
     */
    private fun updateJump() {
        // Выполнение прыжка из буфера
        if (jumpBufferTimer > 0 && canJump) {
            if (isGrounded || coyoteTimer > 0) {
                jump()
                jumpBufferTimer = 0f
            }
        }
    }
    
    // ============================================================================
    // ПРЫЖОК
    // ============================================================================
    
    /**
     * Выполнение прыжка.
     * 
     * @return true если прыжок выполнен
     */
    fun jump(): Boolean {
        if (!canJump) return false
        
        val position = entity?.getComponent<PositionComponent>() ?: return false
        
        // Выполнение прыжка
        position.vy = jumpForce
        isGrounded = false
        jumpsRemaining--
        lastJumpTime = lifetime
        jumpRequested = false
        
        onJump()
        
        return true
    }
    
    /**
     * Выполнение двойного прыжка.
     * 
     * @return true если двойной прыжок выполнен
     */
    fun doubleJump(): Boolean {
        if (maxJumps < 2 || jumpsRemaining < 1) return false
        
        val position = entity?.getComponent<PositionComponent>() ?: return false
        
        // Сброс вертикальной скорости для двойного прыжка
        position.vy = 0f
        
        // Выполнение прыжка
        position.vy = jumpForce * 0.8f // Чуть слабее обычного
        jumpsRemaining--
        
        onDoubleJump()
        
        return true
    }
    
    /**
     * Вызывается при прыжке.
     */
    open fun onJump() {
        // Переопределяется в наследниках
    }
    
    /**
     * Вызывается при двойном прыжке.
     */
    open fun onDoubleJump() {
        // Переопределяется в наследниках
    }
    
    // ============================================================================
    // ПРИЗЕМЛЕНИЕ
    // ============================================================================
    
    /**
     * Вызывается при приземлении.
     */
    fun onLand() {
        if (!isGrounded) {
            isGrounded = true
            canJump = true
            jumpsRemaining = maxJumps
            coyoteTimer = coyoteTime
            
            onLandInternal()
        }
    }
    
    /**
     * Внутренний метод приземления.
     */
    private fun onLandInternal() {
        val position = entity?.getComponent<PositionComponent>()
        position?.vy = 0f
    }
    
    /**
     * Отрыв от земли.
     */
    fun onTakeOff() {
        isGrounded = false
        canJump = jumpsRemaining > 0
    }
    
    // ============================================================================
    // УПРАВЛЕНИЕ
    // ============================================================================
    
    /**
     * Установка направления движения.
     */
    fun setDirection(direction: Int) {
        moveDirection = direction.coerceIn(-1, 1)
        moveLeftRequested = direction < 0
        moveRightRequested = direction > 0
    }
    
    /**
     * Остановка движения.
     */
    fun stop() {
        moveDirection = 0
        moveLeftRequested = false
        moveRightRequested = false
        targetVelocityX = 0f
    }
    
    /**
     * Движение влево.
     */
    fun moveLeft() {
        setDirection(-1)
    }
    
    /**
     * Движение вправо.
     */
    fun moveRight() {
        setDirection(1)
    }
    
    /**
     * Запрос прыжка.
     */
    fun requestJump() {
        jumpRequested = true
    }
    
    /**
     * Отмена прыжка.
     */
    fun cancelJump() {
        jumpRequested = false
    }
    
    /**
     * Проверка, запрошен ли прыжок.
     */
    fun isJumpPressed(): Boolean = jumpRequested || jumpBufferTimer > 0
    
    // ============================================================================
    // СОСТОЯНИЕ
    // ============================================================================
    
    /**
     * Проверка, движется ли сущность.
     */
    fun isMoving(): Boolean = moveDirection != 0
    
    /**
     * Проверка, движется ли влево.
     */
    fun isMovingLeft(): Boolean = moveDirection < 0
    
    /**
     * Проверка, движется ли вправо.
     */
    fun isMovingRight(): Boolean = moveDirection > 0
    
    /**
     * Проверка, находится ли в воздухе.
     */
    fun isAirborne(): Boolean = !isGrounded
    
    /**
     * Проверка, доступен ли прыжок.
     */
    fun isJumpAvailable(): Boolean = canJump && jumpsRemaining > 0
    
    /**
     * Получение состояния движения.
     */
    fun getMovementState(): MovementState {
        return when {
            !isGrounded && entity?.getComponent<PositionComponent>()?.vy ?: 0f < 0 -> 
                MovementState.JUMPING
            !isGrounded -> 
                MovementState.FALLING
            isMoving() -> 
                MovementState.RUNNING
            else -> 
                MovementState.IDLE
        }
    }
    
    override fun reset() {
        super.reset()
        moveDirection = 0
        isGrounded = false
        canJump = true
        jumpsRemaining = maxJumps
        jumpRequested = false
        moveLeftRequested = false
        moveRightRequested = false
        coyoteTimer = 0f
        jumpBufferTimer = 0f
        timeOnGround = 0f
    }
    
    // ============================================================================
    // ENUM
    // ============================================================================
    
    /**
     * Состояния движения.
     */
    enum class MovementState {
        IDLE,
        RUNNING,
        JUMPING,
        FALLING
    }
}

/**
 * Extension функция для мгновенной установки скорости.
 */
fun MovementComponent.setVelocityX(velocity: Float) {
    entity?.getComponent<PositionComponent>()?.vx = velocity
}

/**
 * Extension функция для добавления импульса.
 */
fun MovementComponent.addImpulse(impulseX: Float, impulseY: Float) {
    val position = entity?.getComponent<PositionComponent>() ?: return
    position.vx += impulseX
    position.vy += impulseY
}
