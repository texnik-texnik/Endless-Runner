package com.endlessrunner.entities

import com.endlessrunner.config.PlayerConfig
import com.endlessrunner.entities.components.HealthComponent
import com.endlessrunner.entities.components.TransformComponent
import com.endlessrunner.entities.components.VelocityComponent
import com.endlessrunner.entities.interfaces.EntityType
import com.endlessrunner.entities.interfaces.IEntity
import com.endlessrunner.entities.interfaces.IUpdatable
import com.endlessrunner.entities.interfaces.Vector2

/**
 * Базовый класс для всех игровых сущностей.
 * 
 * Реализует интерфейс IEntity и предоставляет общую функциональность
 * для всех игровых объектов.
 */
abstract class BaseEntity(
    override val id: Long,
    override val type: EntityType,
    initialPosition: Vector2 = Vector2()
) : IEntity, IUpdatable {

    override var position: Vector2 = initialPosition
    override var velocity: Vector2 = Vector2()
    override var size: Vector2 = Vector2(1f, 1f)
    override var isActive: Boolean = false

    /**
     * Компоненты сущности.
     */
    private val components = mutableMapOf<Class<out Any>, Any>()

    /**
     * Добавление компонента.
     */
    inline fun <reified T : Any> addComponent(component: T) {
        components[T::class.java] = component
        if (component is com.endlessrunner.entities.components.Component) {
            component.owner = this
            component.onAdded()
        }
    }

    /**
     * Получение компонента.
     */
    inline fun <reified T : Any> getComponent(): T? {
        return components[T::class.java] as? T
    }

    /**
     * Удаление компонента.
     */
    inline fun <reified T : Any> removeComponent() {
        val component = components.remove(T::class.java)
        if (component is com.endlessrunner.entities.components.Component) {
            component.onRemoved()
            component.owner = null
        }
    }

    /**
     * Проверка наличия компонента.
     */
    inline fun <reified T : Any> hasComponent(): Boolean {
        return components.containsKey(T::class.java)
    }

    override fun activate() {
        isActive = true
        getComponent<com.endlessrunner.entities.components.Component>()?.onEnable()
    }

    override fun deactivate() {
        getComponent<com.endlessrunner.entities.components.Component>()?.onDisable()
        isActive = false
    }

    override fun destroy() {
        components.values.forEach { component ->
            if (component is com.endlessrunner.entities.components.Component) {
                component.onRemoved()
                component.owner = null
            }
        }
        components.clear()
    }

    override fun collidesWith(other: IEntity): Boolean {
        // Простая AABB коллизия (Axis-Aligned Bounding Box)
        return position.x < other.position.x + other.size.x &&
               position.x + size.x > other.position.x &&
               position.y < other.position.y + other.size.y &&
               position.y + size.y > other.position.y
    }

    override fun onCollision(other: IEntity) {
        // Базовая реализация - ничего не делает
        // Переопределяется в наследниках
    }

    override fun update(deltaTime: Float) {
        if (!isActive) return

        // Обновление всех компонентов
        components.values.forEach { component ->
            if (component is IUpdatable) {
                component.update(deltaTime)
            }
        }

        onUpdate(deltaTime)
    }

    /**
     * Обновление сущности.
     * Переопределяется в наследниках.
     */
    protected open fun onUpdate(deltaTime: Float) {}
}

/**
 * Сущность игрока.
 */
class PlayerEntity(
    id: Long,
    private val config: PlayerConfig,
    position: Vector2 = Vector2()
) : BaseEntity(id, EntityType.PLAYER, position) {

    /**
     * Количество жизней.
     */
    var lives: Int = config.startLives
        private set

    /**
     * Количество монет.
     */
    var coins: Int = 0
        private set

    /**
     * Текущее количество прыжков.
     */
    private var jumpCount: Int = 0

    /**
     * Находится ли игрок на земле.
     */
    var isGrounded: Boolean = false
        private set

    /**
     * Неуязвим ли игрок.
     */
    var isInvincible: Boolean = false
        private set
    private var invincibilityTimer: Float = 0f

    init {
        size = Vector2(config.width, config.height)

        // Добавление компонентов
        addComponent(TransformComponent(position))
        addComponent(VelocityComponent())
        addComponent(HealthComponent(startHealth = lives))
    }

    /**
     * Прыжок.
     * @return true если прыжок выполнен
     */
    fun jump(): Boolean {
        if (jumpCount >= config.maxJumps) return false

        val jumpForce = if (jumpCount == 0) config.jumpForce else config.doubleJumpForce
        velocity = velocity.copy(y = jumpForce)
        jumpCount++
        isGrounded = false

        return true
    }

    /**
     * Сброс прыжков (при приземлении).
     */
    fun resetJumps() {
        jumpCount = 0
        isGrounded = true
    }

    /**
     * Получение урона.
     */
    fun takeDamage(amount: Int): Boolean {
        if (isInvincible) return false

        lives--
        isInvincible = true
        invincibilityTimer = config.invincibilityDuration

        getComponent<HealthComponent>()?.takeDamage(amount)

        return lives > 0
    }

    /**
     * Добавление монет.
     */
    fun addCoins(amount: Int) {
        coins += amount
    }

    override fun onUpdate(deltaTime: Float) {
        // Обновление таймера неуязвимости
        if (isInvincible) {
            invincibilityTimer -= deltaTime
            if (invincibilityTimer <= 0) {
                isInvincible = false
            }
        }

        // Применение гравитации
        if (!isGrounded) {
            velocity = velocity.copy(y = velocity.y + config.gravity * deltaTime)
        }

        // Применение сопротивления воздуха
        if (!isGrounded) {
            velocity = velocity * config.airResistance
        }

        // Обновление позиции
        position = position + velocity * deltaTime
    }

    override fun onCollision(other: IEntity) {
        when (other.type) {
            EntityType.COIN -> {
                addCoins(1)
                // other.deactivate()  // Скрыть монету
            }
            EntityType.OBSTACLE, EntityType.ENEMY -> {
                takeDamage(1)
            }
            EntityType.POWERUP -> {
                // Применить эффект усилителя
            }
            else -> {}
        }
    }

    /**
     * Сброс состояния игрока.
     */
    fun reset() {
        lives = config.startLives
        coins = 0
        jumpCount = 0
        isGrounded = false
        isInvincible = false
        invincibilityTimer = 0f
        velocity = Vector2()
        getComponent<HealthComponent>()?.reset()
    }

    private val PlayerConfig.gravity: Float
        get() = -30.0f  // TODO: вынести в PhysicsConfig
}
