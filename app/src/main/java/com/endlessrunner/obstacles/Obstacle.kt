package com.endlessrunner.obstacles

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.endlessrunner.components.PhysicsComponent
import com.endlessrunner.components.PositionComponent
import com.endlessrunner.components.RenderComponent
import com.endlessrunner.core.GameConstants
import com.endlessrunner.entities.Entity
import com.endlessrunner.player.Player

/**
 * Типы препятствий.
 */
enum class ObstacleType(
    /** Урон от препятствия */
    val damage: Int,
    
    /** Масштаб хитбокса */
    val scale: Float = 1f,
    
    /** Цвет для отладки */
    val debugColor: Int = Color.GRAY,
    
    /** Разрушаемо ли препятствие */
    val isDestructible: Boolean = false,
    
    /** Здоровье разрушаемого препятствия */
    val health: Int = 0
) {
    /** Шипы на полу */
    SPIKE(
        damage = 1,
        scale = 0.8f,
        debugColor = Color.rgb(255, 87, 34),
        isDestructible = false
    ),
    
    /** Твёрдый блок */
    BLOCK(
        damage = 2,
        scale = 1f,
        debugColor = Color.rgb(96, 96, 96),
        isDestructible = false
    ),
    
    /** Барьер (прозрачный щит) */
    BARRIER(
        damage = 1,
        scale = 1.2f,
        debugColor = Color.rgb(33, 150, 243),
        isDestructible = true,
        health = 3
    ),
    
    /** Падающий камень */
    FALLING_ROCK(
        damage = 3,
        scale = 1.5f,
        debugColor = Color.rgb(121, 85, 72),
        isDestructible = true,
        health = 5
    ),
    
    /** Разрушаемая стена */
    BREAKABLE_WALL(
        damage = 0,
        scale = 1f,
        debugColor = Color.rgb(158, 158, 158),
        isDestructible = true,
        health = 10
    ),
    
    /** Движущаяся платформа-препятствие */
    MOVING_OBSTACLE(
        damage = 2,
        scale = 1f,
        debugColor = Color.rgb(156, 39, 176),
        isDestructible = false
    );
    
    companion object {
        val ALL_TYPES = values().toList()
    }
}

/**
 * Базовый класс препятствия.
 * Наследуется от Entity.
 *
 * @param type Тип препятствия
 * @param x Позиция X
 * @param y Позиция Y
 * @param width Ширина
 * @param height Высота
 */
open class Obstacle(
    /** Тип препятствия */
    var type: ObstacleType,
    
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 100f,
    height: Float = 100f
) : Entity(tag = GameConstants.TAG_OBSTACLE) {

    companion object {
        /** Пул для переиспользования */
        private val pool: com.endlessrunner.core.ObjectPool<Obstacle> =
            com.endlessrunner.core.ObjectPool(
                initialSize = 30,
                maxSize = 100,
                factory = { Obstacle(ObstacleType.BLOCK) }
            )
        
        /** Получение препятствия из пула */
        fun acquire(
            type: ObstacleType = ObstacleType.BLOCK,
            x: Float = 0f,
            y: Float = 0f,
            width: Float = 100f,
            height: Float = 100f
        ): Obstacle {
            val obstacle = pool.acquire()
            obstacle.type = type
            obstacle.positionComponent?.setPosition(x, y)
            obstacle.physicsComponent?.setSize(
                width * type.scale,
                height * type.scale
            )
            return obstacle
        }
        
        /** Возврат в пул */
        fun release(obstacle: Obstacle) {
            pool.release(obstacle)
        }
    }

    // ============================================================================
    // КОМПОНЕНТЫ
    // ============================================================================

    val positionComponent: PositionComponent?
        get() = getComponent()

    val renderComponent: RenderComponent?
        get() = getComponent()

    val physicsComponent: PhysicsComponent?
        get() = getComponent()

    // ============================================================================
    // СВОЙСТВА
    // ============================================================================

    /** Текущее здоровье (для разрушаемых препятствий) */
    var health: Int = type.health
        private set

    /** Максимальное здоровье */
    val maxHealth: Int = type.health

    /** Разрушено ли препятствие */
    var isDestroyed: Boolean = false
        protected set

    /** Время жизни */
    var spawnTime: Float = 0f

    // ============================================================================
    // ИНИЦИАЛИЗАЦИЯ
    // ============================================================================

    init {
        setupComponents(width, height)
    }

    private fun setupComponents(width: Float, height: Float) {
        addComponent(PositionComponent(x = 0f, y = 0f))
        
        addComponent(
            RenderComponent(
                color = type.debugColor,
                width = width * type.scale,
                height = height * type.scale
            )
        )
        
        addComponent(
            PhysicsComponent(
                width = width * type.scale,
                height = height * type.scale,
                collisionLayer = GameConstants.LAYER_OBSTACLE,
                isTrigger = type.damage == 0
            )
        )
    }

    // ============================================================================
    // ЖИЗНЕННЫЙ ЦИКЛ
    // ============================================================================

    override fun onActivate() {
        super.onActivate()
        spawnTime = 0f
        isDestroyed = false
        health = type.health
    }

    override fun update(deltaTime: Float) {
        if (!isActive || isDestroyed) return

        spawnTime += deltaTime

        // Обновление поведения препятствия
        updateObstacle(deltaTime)

        // Проверка выхода за границы
        checkBounds()

        super.update(deltaTime)
    }

    override fun render(canvas: Canvas) {
        if (!isActive || isDestroyed) return

        super.render(canvas)

        // Отрисовка здоровья для разрушаемых препятствий
        if (type.isDestructible && config.showDebugInfo) {
            renderHealthBar(canvas)
        }
    }

    override fun reset() {
        super.reset()
        type = ObstacleType.BLOCK
        health = type.health
        isDestroyed = false
        spawnTime = 0f
    }

    // ============================================================================
    // ПОВЕДЕНИЕ ПРЕПЯТСТВИЯ
    // ============================================================================

    /**
     * Обновление препятствия.
     * Переопределяется в наследниках.
     */
    protected open fun updateObstacle(deltaTime: Float) {
        // По умолчанию ничего не делаем
    }

    /**
     * Проверка выхода за границы.
     */
    protected fun checkBounds() {
        val physics = physicsComponent ?: return
        
        val bounds = physics.getBounds()
        if (bounds.right < -200f) {
            markForDestroy()
        }
    }

    // ============================================================================
    // КОЛЛИЗИИ
    // ============================================================================

    /**
     * Обработка столкновения с игроком.
     */
    open fun onCollide(player: Player) {
        if (type.damage > 0 && !player.isInvincible && !player.isDead) {
            player.takeDamage(type.damage)
        }
    }

    // ============================================================================
    // УПРАВЛЕНИЕ ЗДОРОВЬЕМ
    // ============================================================================

    /**
     * Получение урона.
     */
    open fun takeDamage(amount: Int): Boolean {
        if (!type.isDestructible) return false
        
        health -= amount
        
        if (health <= 0) {
            destroy()
            return true
        }
        
        return false
    }

    /**
     * Уничтожение препятствия.
     */
    open fun destroy() {
        if (isDestroyed) return
        
        isDestroyed = true
        markForDestroy()
        onDestroyed()
    }

    /**
     * Вызывается при уничтожении.
     */
    protected open fun onDestroyed() {
        // TODO: Добавить эффекты разрушения
    }

    // ============================================================================
    // ОТРИСОВКА
    // ============================================================================

    /**
     * Отрисовка полоски здоровья.
     */
    protected fun renderHealthBar(canvas: Canvas) {
        val physics = physicsComponent ?: return
        val bounds = physics.getBounds()
        
        val barWidth = bounds.width()
        val barHeight = 8f
        val barX = bounds.left
        val barY = bounds.top - barHeight - 5f
        
        // Фон
        val bgPaint = Paint().apply {
            color = Color.DKGRAY
            style = Paint.Style.FILL
        }
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, bgPaint)
        
        // Здоровье
        val healthPercent = health.toFloat() / maxHealth
        val healthPaint = Paint().apply {
            color = if (healthPercent > 0.5f) Color.GREEN else Color.RED
            style = Paint.Style.FILL
        }
        canvas.drawRect(
            barX, barY,
            barX + barWidth * healthPercent, barY + barHeight,
            healthPaint
        )
    }

    /**
     * Конфигурация для отладки.
     */
    private val config = DebugConfig()
    
    class DebugConfig {
        val showDebugInfo: Boolean = false // TODO: Интегрировать с GameConfig
    }
}

/**
 * Движущаяся платформа.
 * Может быть использована как препятствие или как платформа для игрока.
 *
 * @param pattern Паттерн движения
 * @param speed Скорость движения
 * @param amplitude Амплитуда движения
 */
open class Platform(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 200f,
    height: Float = 40f,
    
    /** Тип платформы */
    var platformType: PlatformType = PlatformType.STATIC,
    
    /** Паттерн движения */
    var movementPattern: MovementPattern = MovementPattern.LINEAR,
    
    /** Скорость движения */
    var speed: Float = 200f,
    
    /** Амплитуда движения */
    var amplitude: Float = 150f,
    
    /** Частота движения */
    var frequency: Float = 1f
) : Obstacle(
    type = ObstacleType.MOVING_OBSTACLE,
    x = x,
    y = y,
    width = width,
    height = height
) {
    
    companion object {
        /** Пул для переиспользования */
        private val pool: com.endlessrunner.core.ObjectPool<Platform> =
            com.endlessrunner.core.ObjectPool(
                initialSize = 20,
                maxSize = 60,
                factory = { Platform() }
            )
        
        fun acquire(
            x: Float = 0f,
            y: Float = 0f,
            width: Float = 200f,
            height: Float = 40f,
            type: PlatformType = PlatformType.STATIC
        ): Platform {
            val platform = pool.acquire()
            platform.platformType = type
            platform.positionComponent?.setPosition(x, y)
            platform.physicsComponent?.setSize(width, height)
            return platform
        }
        
        fun release(platform: Platform) {
            pool.release(platform)
        }
    }
    
    /** Начальная позиция */
    private var startX: Float = 0f
    private var startY: Float = 0f
    
    /** Текущее смещение */
    private var offsetX: Float = 0f
    private var offsetY: Float = 0f
    
    /** Время движения */
    private var moveTime: Float = 0f
    
    /** Направление движения */
    private var direction: Int = 1
    
    override fun onActivate() {
        super.onActivate()
        
        val position = positionComponent
        startX = position?.x ?: 0f
        startY = position?.y ?: 0f
        moveTime = 0f
        offsetX = 0f
        offsetY = 0f
    }
    
    override fun updateObstacle(deltaTime: Float) {
        when (platformType) {
            PlatformType.STATIC -> {
                // Ничего не делаем
            }
            PlatformType.MOVING -> {
                updateMoving(deltaTime)
            }
            PlatformType.FALLING -> {
                updateFalling(deltaTime)
            }
            PlatformType.BREAKABLE -> {
                // Ломается при контакте с игроком
                updateBreakable(deltaTime)
            }
        }
    }
    
    /**
     * Обновление движущейся платформы.
     */
    private fun updateMoving(deltaTime: Float) {
        moveTime += deltaTime
        
        val position = positionComponent ?: return
        
        when (movementPattern) {
            MovementPattern.LINEAR -> {
                // Движение туда-обратно
                offsetX = direction * speed * deltaTime
                position.x += offsetX
                
                // Проверка границ
                if (kotlin.math.abs(position.x - startX) > amplitude) {
                    direction = -direction
                }
            }
            MovementPattern.OSCILLATING, MovementPattern.SINUSOIDAL -> {
                // Синусоидальное движение
                offsetX = kotlin.math.sin(moveTime * frequency * 2f * kotlin.math.PI.toFloat()) * amplitude
                position.x = startX + offsetX
            }
            MovementPattern.CIRCULAR -> {
                // Круговое движение
                offsetX = kotlin.math.cos(moveTime * frequency) * amplitude
                offsetY = kotlin.math.sin(moveTime * frequency) * amplitude
                position.x = startX + offsetX
                position.y = startY + offsetY
            }
            else -> {}
        }
    }
    
    /**
     * Обновление падающей платформы.
     */
    private fun updateFalling(deltaTime: Float) {
        val position = positionComponent ?: return
        val physics = physicsComponent ?: return
        
        // Падение под действием гравитации
        val velocity = physicsComponent?.let { 
            // Используем простую гравитацию
            500f * deltaTime 
        } ?: 0f
        
        position.y += velocity
        
        // Проверка на выход за нижнюю границу
        if (position.y > 2000f) {
            markForDestroy()
        }
    }
    
    /**
     * Обновление ломающейся платформы.
     */
    private fun updateBreakable(deltaTime: Float) {
        // Платформа ломается при контакте с игроком
        // Логика обрабатывается в onCollide
    }
    
    override fun onCollide(player: Player) {
        super.onCollide(player)
        
        if (platformType == PlatformType.BREAKABLE) {
            // Ломаем платформу при контакте
            destroy()
        }
    }
    
    /**
     * Получение текущей скорости платформы.
     * Используется для передачи скорости игроку.
     */
    fun getCurrentVelocity(): Pair<Float, Float> {
        return when (movementPattern) {
            MovementPattern.LINEAR -> Pair(direction * speed, 0f)
            MovementPattern.OSCILLATING, MovementPattern.SINUSOIDAL -> {
                val vx = speed * kotlin.math.cos(moveTime * frequency * 2f * kotlin.math.PI.toFloat())
                Pair(vx, 0f)
            }
            MovementPattern.CIRCULAR -> {
                val vx = -speed * kotlin.math.sin(moveTime * frequency) * frequency
                val vy = speed * kotlin.math.cos(moveTime * frequency) * frequency
                Pair(vx, vy)
            }
            else -> Pair(0f, 0f)
        }
    }
    
    override fun reset() {
        super.reset()
        platformType = PlatformType.STATIC
        movementPattern = MovementPattern.LINEAR
        speed = 200f
        amplitude = 150f
        frequency = 1f
        startX = 0f
        startY = 0f
        offsetX = 0f
        offsetY = 0f
        moveTime = 0f
        direction = 1
    }
    
    override fun destroy() {
        super.destroy()
        pool.release(this)
    }
}

/**
 * Типы платформ.
 */
enum class PlatformType {
    /** Статичная платформа */
    STATIC,
    
    /** Движущаяся платформа */
    MOVING,
    
    /** Падающая платформа (при контакте) */
    FALLING,
    
    /** Ломающаяся платформа */
    BREAKABLE
}

/**
 * Паттерны движения (дублирование из EnemyType для независимости).
 */
enum class MovementPattern {
    LINEAR,
    OSCILLATING,
    BOUNCING,
    SINUSOIDAL,
    CIRCULAR,
    STATIC
}

/**
 * Extension функция для получения всех препятствий.
 */
fun com.endlessrunner.entities.EntityManager.getAllObstacles(): List<Obstacle> {
    return getEntitiesByTag(GameConstants.TAG_OBSTACLE)
        .filterIsInstance<Obstacle>()
        .filter { it.isActive && !it.isDestroyed }
}

/**
 * Extension функция для получения всех платформ.
 */
fun com.endlessrunner.entities.EntityManager.getAllPlatforms(): List<Platform> {
    return getAllObstacles().filterIsInstance<Platform>()
}
