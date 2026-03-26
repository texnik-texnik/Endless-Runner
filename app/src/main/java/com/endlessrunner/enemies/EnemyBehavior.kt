package com.endlessrunner.enemies

import com.endlessrunner.components.PhysicsComponent
import com.endlessrunner.components.PositionComponent

/**
 * Абстрактный класс поведения врага.
 * Реализует Strategy Pattern для гибкого изменения AI.
 */
abstract class EnemyBehavior(
    /** Скорость движения */
    protected var speed: Float = 300f,
    
    /** Направление движения */
    protected var direction: MoveDirection = MoveDirection.LEFT,
    
    /** Паттерн движения */
    protected var movementPattern: MovementPattern = MovementPattern.LINEAR
) {
    
    /** Время с момента спавна */
    protected var timeSinceSpawn: Float = 0f
    
    /** Флаг инициализации */
    protected var isInitialized: Boolean = false
    
    /**
     * Вызывается при спавне врага.
     * Используется для инициализации поведения.
     *
     * @param enemy Враг, к которому применяется поведение
     */
    open fun onSpawn(enemy: Enemy) {
        timeSinceSpawn = 0f
        isInitialized = true
        enemy.direction = direction
        enemy.currentSpeed = speed
    }
    
    /**
     * Обновление поведения.
     * Вызывается каждый кадр.
     *
     * @param enemy Враг
     * @param deltaTime Время с последнего кадра
     */
    abstract fun update(enemy: Enemy, deltaTime: Float)
    
    /**
     * Вызывается при уничтожении врага.
     * Используется для очистки ресурсов.
     *
     * @param enemy Враг
     */
    open fun onDestroy(enemy: Enemy) {
        isInitialized = false
    }
    
    /**
     * Сброс состояния поведения.
     */
    open fun reset() {
        timeSinceSpawn = 0f
    }
    
    /**
     * Обновление направления врага.
     */
    protected fun updateDirection(enemy: Enemy, newDirection: MoveDirection) {
        enemy.direction = newDirection
    }
    
    /**
     * Обновление скорости врага.
     */
    protected fun updateSpeed(enemy: Enemy, newSpeed: Float) {
        enemy.currentSpeed = newSpeed.coerceAtLeast(0f)
    }
    
    /**
     * Получение позиции врага.
     */
    protected fun getPosition(enemy: Enemy): PositionComponent? {
        return enemy.positionComponent
    }
    
    /**
     * Получение физики врага.
     */
    protected fun getPhysics(enemy: Enemy): PhysicsComponent? {
        return enemy.physicsComponent
    }
    
    /**
     * Проверка, находится ли враг в пределах экрана.
     */
    protected fun isInScreenBounds(
        enemy: Enemy,
        screenWidth: Float,
        screenHeight: Float,
        margin: Float = 100f
    ): Boolean {
        val position = getPosition(enemy) ?: return false
        val physics = getPhysics(enemy) ?: return false
        
        val bounds = physics.getBounds()
        return bounds.right > -margin && 
               bounds.left < screenWidth + margin &&
               bounds.bottom > -margin && 
               bounds.top < screenHeight + margin
    }
    
    /**
     * Разворот направления на противоположное.
     */
    protected fun reverseHorizontalDirection(enemy: Enemy) {
        enemy.direction = when (enemy.direction) {
            MoveDirection.LEFT -> MoveDirection.RIGHT
            MoveDirection.RIGHT -> MoveDirection.LEFT
            MoveDirection.UP_LEFT -> MoveDirection.UP_RIGHT
            MoveDirection.UP_RIGHT -> MoveDirection.UP_LEFT
            MoveDirection.DOWN_LEFT -> MoveDirection.DOWN_RIGHT
            MoveDirection.DOWN_RIGHT -> MoveDirection.DOWN_LEFT
            else -> MoveDirection.RIGHT
        }
    }
    
    /**
     * Вычисление синуса без аллокаций.
     */
    protected fun sin(value: Float): Float = kotlin.math.sin(value)
    
    /**
     * Вычисление косинуса без аллокаций.
     */
    protected fun cos(value: Float): Float = kotlin.math.cos(value)
}

/**
 * Статичное поведение.
 * Враг не двигается, только отрисовывается.
 */
class StaticBehavior : EnemyBehavior(
    speed = 0f,
    direction = MoveDirection.NONE,
    movementPattern = MovementPattern.STATIC
) {
    
    override fun update(enemy: Enemy, deltaTime: Float) {
        // Статичные враги не двигаются
        // Только обновляем время
        timeSinceSpawn += deltaTime
    }
}

/**
 * Поведение движущегося врага.
 * Поддерживает различные паттерны движения.
 *
 * @param speed Скорость движения
 * @param pattern Паттерн движения
 * @param amplitude Амплитуда для осциллирующего движения
 * @param frequency Частота для осциллирующего движения
 */
class MovingBehavior(
    speed: Float = 300f,
    pattern: MovementPattern = MovementPattern.LINEAR,
    private val amplitude: Float = 200f,
    private val frequency: Float = 2f,
    private val boundsMinX: Float = 0f,
    private val boundsMaxX: Float = 1920f,
    private val boundsMinY: Float = 0f,
    private val boundsMaxY: Float = 1080f
) : EnemyBehavior(
    speed = speed,
    direction = MoveDirection.LEFT,
    movementPattern = pattern
) {
    
    /** Начальная позиция */
    private var startX: Float = 0f
    private var startY: Float = 0f
    
    override fun onSpawn(enemy: Enemy) {
        super.onSpawn(enemy)
        
        val position = getPosition(enemy)
        startX = position?.x ?: 0f
        startY = position?.y ?: 0f
    }
    
    override fun update(enemy: Enemy, deltaTime: Float) {
        timeSinceSpawn += deltaTime
        
        when (movementPattern) {
            MovementPattern.LINEAR -> updateLinear(enemy, deltaTime)
            MovementPattern.OSCILLATING -> updateOscillating(enemy, deltaTime)
            MovementPattern.BOUNCING -> updateBouncing(enemy, deltaTime)
            MovementPattern.SINUSOIDAL -> updateSinusoidal(enemy, deltaTime)
            MovementPattern.CIRCULAR -> updateCircular(enemy, deltaTime)
            else -> {} // STATIC и другие не поддерживаются
        }
    }
    
    /**
     * Линейное движение.
     */
    private fun updateLinear(enemy: Enemy, deltaTime: Float) {
        // Движение в заданном направлении без изменений
        // Проверка границ
        val position = getPosition(enemy) ?: return
        val physics = getPhysics(enemy) ?: return
        
        val bounds = physics.getBounds()
        
        // Отскок от границ
        if (bounds.left < boundsMinX && enemy.direction == MoveDirection.LEFT) {
            reverseHorizontalDirection(enemy)
        } else if (bounds.right > boundsMaxX && enemy.direction == MoveDirection.RIGHT) {
            reverseHorizontalDirection(enemy)
        }
    }
    
    /**
     * Осциллирующее движение (туда-обратно).
     */
    private fun updateOscillating(enemy: Enemy, deltaTime: Float) {
        val position = getPosition(enemy) ?: return
        
        // Движение по синусоиде вдоль оси X
        val offset = sin(timeSinceSpawn * frequency * 2f * kotlin.math.PI.toFloat()) * amplitude
        position.x = startX + offset
    }
    
    /**
     * Движение с отскоком от границ.
     */
    private fun updateBouncing(enemy: Enemy, deltaTime: Float) {
        val position = getPosition(enemy) ?: return
        val physics = getPhysics(enemy) ?: return
        
        val bounds = physics.getBounds()
        
        // Отскок от левой/правой границы
        if (bounds.left <= boundsMinX) {
            position.x = boundsMinX + bounds.width() / 2
            reverseHorizontalDirection(enemy)
        } else if (bounds.right >= boundsMaxX) {
            position.x = boundsMaxX - bounds.width() / 2
            reverseHorizontalDirection(enemy)
        }
        
        // Отскок от верхней/нижней границы
        if (bounds.top <= boundsMinY) {
            position.y = boundsMinY + bounds.height() / 2
            // Разворот по Y
            enemy.direction = when (enemy.direction) {
                MoveDirection.UP -> MoveDirection.DOWN
                MoveDirection.DOWN -> MoveDirection.UP
                MoveDirection.UP_LEFT -> MoveDirection.DOWN_LEFT
                MoveDirection.UP_RIGHT -> MoveDirection.DOWN_RIGHT
                MoveDirection.DOWN_LEFT -> MoveDirection.UP_LEFT
                MoveDirection.DOWN_RIGHT -> MoveDirection.UP_RIGHT
                else -> MoveDirection.DOWN
            }
        } else if (bounds.bottom >= boundsMaxY) {
            position.y = boundsMaxY - bounds.height() / 2
            enemy.direction = when (enemy.direction) {
                MoveDirection.UP -> MoveDirection.DOWN
                MoveDirection.DOWN -> MoveDirection.UP
                MoveDirection.UP_LEFT -> MoveDirection.DOWN_LEFT
                MoveDirection.UP_RIGHT -> MoveDirection.DOWN_RIGHT
                MoveDirection.DOWN_LEFT -> MoveDirection.UP_LEFT
                MoveDirection.DOWN_RIGHT -> MoveDirection.UP_RIGHT
                else -> MoveDirection.UP
            }
        }
    }
    
    /**
     * Синусоидальное движение (волна).
     */
    private fun updateSinusoidal(enemy: Enemy, deltaTime: Float) {
        val position = getPosition(enemy) ?: return
        
        // Движение влево/вправо
        val (dx, _) = direction.vector
        position.x += dx * speed * deltaTime
        
        // Вертикальное движение по синусоиде
        val yOffset = sin(timeSinceSpawn * frequency * 2f * kotlin.math.PI.toFloat()) * amplitude
        position.y = startY + yOffset
    }
    
    /**
     * Круговое движение.
     */
    private fun updateCircular(enemy: Enemy, deltaTime: Float) {
        val position = getPosition(enemy) ?: return
        
        // Движение по кругу
        val angle = timeSinceSpawn * frequency
        position.x = startX + cos(angle) * amplitude
        position.y = startY + sin(angle) * amplitude
    }
    
    override fun reset() {
        super.reset()
        startX = 0f
        startY = 0f
    }
}

/**
 * Поведение летающего врага.
 * Движение по синусоиде с постоянной горизонтальной скоростью.
 *
 * @param speed Горизонтальная скорость
 * @param amplitude Амплитуда волны
 * @param frequency Частота волны
 * @param yOffset Смещение по Y от базовой позиции
 */
class FlyingBehavior(
    speed: Float = 400f,
    private val amplitude: Float = 100f,
    private val frequency: Float = 1.5f,
    private val yOffset: Float = 0f
) : EnemyBehavior(
    speed = speed,
    direction = MoveDirection.LEFT,
    movementPattern = MovementPattern.SINUSOIDAL
) {
    
    /** Базовая позиция Y */
    private var baseY: Float = 0f
    
    override fun onSpawn(enemy: Enemy) {
        super.onSpawn(enemy)
        
        val position = getPosition(enemy)
        baseY = (position?.y ?: 0f) + yOffset
    }
    
    override fun update(enemy: Enemy, deltaTime: Float) {
        timeSinceSpawn += deltaTime
        
        val position = getPosition(enemy) ?: return
        
        // Горизонтальное движение
        val (dx, _) = direction.vector
        position.x += dx * speed * deltaTime
        
        // Вертикальное движение по синусоиде
        position.y = baseY + sin(timeSinceSpawn * frequency) * amplitude
    }
    
    override fun reset() {
        super.reset()
        baseY = 0f
    }
}

/**
 * Поведение прыгающего врага.
 * Периодические прыжки с заданным интервалом.
 *
 * @param speed Горизонтальная скорость
 * @param jumpInterval Интервал между прыжками (секунды)
 * @param jumpForce Сила прыжка
 * @param gravity Гравитация
 */
class JumpingBehavior(
    speed: Float = 200f,
    private val jumpInterval: Float = 2f,
    private val jumpForce: Float = -800f,
    private val gravity: Float = 2000f
) : EnemyBehavior(
    speed = speed,
    direction = MoveDirection.LEFT,
    movementPattern = MovementPattern.LINEAR
) {
    
    /** Таймер до следующего прыжка */
    private var jumpTimer: Float = 0f
    
    /** Находится ли в воздухе */
    var isAirborne: Boolean = false
        private set
    
    /** Вертикальная скорость */
    private var velocityY: Float = 0f
    
    /** Базовая позиция Y (земля) */
    private var groundY: Float = 0f
    
    override fun onSpawn(enemy: Enemy) {
        super.onSpawn(enemy)
        
        val position = getPosition(enemy)
        groundY = position?.y ?: 0f
        jumpTimer = jumpInterval * 0.5f // Случайный старт
    }
    
    override fun update(enemy: Enemy, deltaTime: Float) {
        timeSinceSpawn += deltaTime
        
        val position = getPosition(enemy) ?: return
        
        // Горизонтальное движение
        val (dx, _) = direction.vector
        position.x += dx * speed * deltaTime
        
        // Обновление таймера прыжка
        if (!isAirborne) {
            jumpTimer -= deltaTime
            
            if (jumpTimer <= 0f) {
                jump()
                jumpTimer = jumpInterval
            }
        }
        
        // Применение гравитации
        if (isAirborne) {
            velocityY += gravity * deltaTime
            position.y += velocityY * deltaTime
            
            // Проверка приземления
            if (position.y >= groundY) {
                position.y = groundY
                velocityY = 0f
                isAirborne = false
                onLand(enemy)
            }
        }
    }
    
    /**
     * Выполнение прыжка.
     */
    private fun jump() {
        isAirborne = true
        velocityY = jumpForce
    }
    
    /**
     * Вызывается при приземлении.
     */
    protected open fun onLand(enemy: Enemy) {
        // Переопределяется в наследниках
        // TODO: Добавить эффекты приземления (тряска, частицы)
    }
    
    override fun reset() {
        super.reset()
        jumpTimer = 0f
        isAirborne = false
        velocityY = 0f
        groundY = 0f
    }
}
