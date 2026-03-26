package com.endlessrunner.entities

import com.endlessrunner.config.FullConfig
import com.endlessrunner.config.ItemConfig
import com.endlessrunner.config.LevelConfig
import com.endlessrunner.config.PlayerConfig
import com.endlessrunner.entities.components.SpriteComponent
import com.endlessrunner.entities.components.TransformComponent
import com.endlessrunner.entities.components.VelocityComponent
import com.endlessrunner.entities.interfaces.EntityType
import com.endlessrunner.entities.interfaces.Vector2
import com.endlessrunner.managers.EntityManager
import kotlin.random.Random

/**
 * Фабрика для создания игровых сущностей.
 *
 * Использует паттерн Factory Method для создания различных типов сущностей
 * с предустановленными компонентами и параметрами.
 */
class EntityFactory(
    private val entityManager: EntityManager,
    private val config: FullConfig
) {
    private var nextId: Long = 0

    /**
     * Генерация следующего уникального ID.
     */
    private fun nextId(): Long = nextId++

    /**
     * Создание сущности игрока.
     */
    fun createPlayer(position: Vector2 = Vector2(0f, 0f)): PlayerEntity {
        return PlayerEntity(
            id = nextId(),
            config = config.player,
            position = position
        ).apply {
            activate()
        }
    }

    /**
     * Создание монеты.
     */
    fun createCoin(position: Vector2, value: Int = 1): CoinEntity {
        return CoinEntity(
            id = nextId(),
            position = position,
            value = value,
            config = config.items.coin
        ).apply {
            activate()
        }
    }

    /**
     * Создание препятствия.
     */
    fun createObstacle(
        position: Vector2,
        type: ObstacleType = ObstacleType.STATIC,
        width: Float = 1f,
        height: Float = 1f
    ): ObstacleEntity {
        return ObstacleEntity(
            id = nextId(),
            position = position,
            type = type,
            width = width,
            height = height,
            damage = config.items.obstacle.damage
        ).apply {
            activate()
        }
    }

    /**
     * Создание врага.
     */
    fun createEnemy(
        position: Vector2,
        type: EnemyType = EnemyType.BASIC,
        speed: Float = 2f
    ): EnemyEntity {
        return EnemyEntity(
            id = nextId(),
            position = position,
            type = type,
            speed = speed,
            health = when (type) {
                EnemyType.BASIC -> 1
                EnemyType.FLYING -> 1
                EnemyType.BIG -> 3
            }
        ).apply {
            activate()
        }
    }

    /**
     * Создание бонуса (power-up).
     */
    fun createPowerUp(
        position: Vector2,
        type: PowerUpType
    ): PowerUpEntity {
        return PowerUpEntity(
            id = nextId(),
            position = position,
            type = type,
            duration = config.items.powerup.duration
        ).apply {
            activate()
        }
    }

    /**
     * Создание платформы.
     */
    fun createPlatform(
        position: Vector2,
        width: Float,
        height: Float = 0.5f,
        isMoving: Boolean = false,
        moveRange: Float = 0f,
        moveSpeed: Float = 0f
    ): PlatformEntity {
        return PlatformEntity(
            id = nextId(),
            position = position,
            width = width,
            height = height,
            isMoving = isMoving,
            moveRange = moveRange,
            moveSpeed = moveSpeed
        ).apply {
            activate()
        }
    }

    /**
     * Создание частицы для эффектов.
     */
    fun createParticle(
        position: Vector2,
        velocity: Vector2,
        lifetime: Float = 1f,
        color: Int = android.graphics.Color.WHITE,
        size: Float = 0.2f
    ): ParticleEntity {
        return ParticleEntity(
            id = nextId(),
            position = position,
            velocity = velocity,
            lifetime = lifetime,
            color = color,
            size = size
        ).apply {
            activate()
        }
    }

    /**
     * Создание эффекта частиц (взрыв).
     */
    fun createExplosion(
        position: Vector2,
        count: Int = 10,
        color: Int = android.graphics.Color.YELLOW,
        spread: Float = 5f
    ): List<ParticleEntity> {
        val particles = mutableListOf<ParticleEntity>()
        repeat(count) {
            val angle = (Random.nextFloat() * Math.PI * 2).toFloat()
            val speed = Random.nextFloat() * spread
            val vel = Vector2(
                kotlin.math.cos(angle) * speed,
                kotlin.math.sin(angle) * speed
            )
            particles.add(
                createParticle(
                    position = position,
                    velocity = vel,
                    lifetime = 0.5f + Random.nextFloat() * 0.5f,
                    color = color,
                    size = 0.1f + Random.nextFloat() * 0.2f
                )
            )
        }
        return particles
    }

    /**
     * Случайный бонус на основе конфигурации.
     */
    fun createRandomPowerUp(position: Vector2): PowerUpEntity? {
        val types = config.items.powerup.types
        if (types.isEmpty()) return null

        val randomType = types.random()
        val powerUpType = when (randomType) {
            "shield" -> PowerUpType.SHIELD
            "speed_boost" -> PowerUpType.SPEED_BOOST
            "coin_magnet" -> PowerUpType.COIN_MAGNET
            "extra_jump" -> PowerUpType.EXTRA_JUMP
            else -> PowerUpType.SHIELD
        }
        return createPowerUp(position, powerUpType)
    }

    /**
     * Очистка фабрики.
     */
    fun dispose() {
        nextId = 0
    }
}

/**
 * Типы препятствий.
 */
enum class ObstacleType {
    STATIC,
    MOVING,
    FALLING,
    ROTATING
}

/**
 * Типы врагов.
 */
enum class EnemyType {
    BASIC,
    FLYING,
    BIG
}

/**
 * Типы бонусов.
 */
enum class PowerUpType {
    SHIELD,
    SPEED_BOOST,
    COIN_MAGNET,
    EXTRA_JUMP
}

/**
 * Сущность монеты.
 */
class CoinEntity(
    override val id: Long,
    position: Vector2,
    val value: Int,
    private val config: ItemConfig.CoinConfig
) : BaseEntity(id, EntityType.COIN, position) {

    private var rotation: Float = 0f

    init {
        size = Vector2(0.5f, 0.5f)
        addComponent(TransformComponent(position))
        addComponent(SpriteComponent(color = android.graphics.Color.parseColor("#FFD700")))
    }

    override fun onUpdate(deltaTime: Float) {
        rotation += config.rotationSpeed * deltaTime
    }
}

/**
 * Сущность препятствия.
 */
class ObstacleEntity(
    override val id: Long,
    position: Vector2,
    val type: ObstacleType,
    width: Float,
    height: Float,
    val damage: Int
) : BaseEntity(id, EntityType.OBSTACLE, position) {

    private var moveOffset: Float = 0f
    private var moveDirection: Float = 1f

    init {
        size = Vector2(width, height)
        addComponent(TransformComponent(position))
        addComponent(SpriteComponent(
            color = when (type) {
                ObstacleType.STATIC -> android.graphics.Color.parseColor("#8B4513")
                ObstacleType.MOVING -> android.graphics.Color.parseColor("#A0522D")
                ObstacleType.FALLING -> android.graphics.Color.parseColor("#CD853F")
                ObstacleType.ROTATING -> android.graphics.Color.parseColor("#D2691E")
            }
        ))
    }

    override fun onUpdate(deltaTime: Float) {
        when (type) {
            ObstacleType.MOVING -> {
                moveOffset += moveDirection * 2f * deltaTime
                if (moveOffset > 3f || moveOffset < -3f) {
                    moveDirection *= -1
                }
                position = position.copy(x = position.x + moveOffset * deltaTime)
            }
            ObstacleType.FALLING -> {
                // Падает при приближении игрока
            }
            ObstacleType.ROTATING -> {
                // Вращается
            }
            ObstacleType.STATIC -> {
                // Не двигается
            }
        }
    }
}

/**
 * Сущность врага.
 */
class EnemyEntity(
    override val id: Long,
    position: Vector2,
    val type: EnemyType,
    val speed: Float,
    health: Int
) : BaseEntity(id, EntityType.ENEMY, position) {

    var currentHealth: Int = health
        private set

    private var moveDirection: Float = -1f  // Движется влево к игроку

    init {
        size = when (type) {
            EnemyType.BASIC -> Vector2(1f, 1f)
            EnemyType.FLYING -> Vector2(0.8f, 0.8f)
            EnemyType.BIG -> Vector2(2f, 2f)
        }
        addComponent(TransformComponent(position))
        addComponent(VelocityComponent())
        addComponent(SpriteComponent(
            color = when (type) {
                EnemyType.BASIC -> android.graphics.Color.parseColor("#DC143C")
                EnemyType.FLYING -> android.graphics.Color.parseColor("#8B0000")
                EnemyType.BIG -> android.graphics.Color.parseColor("#B22222")
            }
        ))
    }

    override fun onUpdate(deltaTime: Float) {
        // Простое движение к игроку
        position = position.copy(x = position.x + moveDirection * speed * deltaTime)
    }

    fun takeDamage(amount: Int) {
        currentHealth -= amount
    }
}

/**
 * Сущность бонуса.
 */
class PowerUpEntity(
    override val id: Long,
    position: Vector2,
    val type: PowerUpType,
    val duration: Float
) : BaseEntity(id, EntityType.POWERUP, position) {

    private var lifetime: Float = 10f  // Время жизни на земле

    init {
        size = Vector2(0.6f, 0.6f)
        addComponent(TransformComponent(position))
        addComponent(SpriteComponent(
            color = when (type) {
                PowerUpType.SHIELD -> android.graphics.Color.parseColor("#4169E1")
                PowerUpType.SPEED_BOOST -> android.graphics.Color.parseColor("#FF4500")
                PowerUpType.COIN_MAGNET -> android.graphics.Color.parseColor("#32CD32")
                PowerUpType.EXTRA_JUMP -> android.graphics.Color.parseColor("#9370DB")
            }
        ))
    }

    override fun onUpdate(deltaTime: Float) {
        lifetime -= deltaTime
        if (lifetime <= 0) {
            deactivate()
        }
    }
}

/**
 * Сущность платформы.
 */
class PlatformEntity(
    override val id: Long,
    position: Vector2,
    width: Float,
    height: Float,
    val isMoving: Boolean,
    val moveRange: Float,
    val moveSpeed: Float
) : BaseEntity(id, EntityType.PLATFORM, position) {

    private var initialX: Float = position.x
    private var time: Float = 0f

    init {
        size = Vector2(width, height)
        addComponent(TransformComponent(position))
        addComponent(SpriteComponent(
            color = android.graphics.Color.parseColor("#696969")
        ))
    }

    override fun onUpdate(deltaTime: Float) {
        if (isMoving) {
            time += deltaTime
            position = position.copy(
                x = initialX + kotlin.math.sin(time * moveSpeed) * moveRange
            )
        }
    }
}

/**
 * Сущность частицы.
 */
class ParticleEntity(
    override val id: Long,
    position: Vector2,
    velocity: Vector2,
    val lifetime: Float,
    val color: Int,
    size: Float
) : BaseEntity(id, EntityType.PARTICLE, position) {

    private var remainingLifetime: Float = lifetime

    init {
        this.velocity = velocity
        this.size = Vector2(size, size)
        addComponent(TransformComponent(position))
        addComponent(SpriteComponent(color = color))
    }

    override fun onUpdate(deltaTime: Float) {
        remainingLifetime -= deltaTime
        position = position + velocity * deltaTime

        // Гравитация для частиц
        velocity = velocity.copy(y = velocity.y - 9.8f * deltaTime)

        if (remainingLifetime <= 0) {
            deactivate()
        }
    }
}
