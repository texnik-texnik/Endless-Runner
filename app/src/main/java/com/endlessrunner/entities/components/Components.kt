package com.endlessrunner.entities.components

import com.endlessrunner.entities.interfaces.IUpdatable
import com.endlessrunner.entities.interfaces.Vector2

/**
 * Базовый класс для всех компонентов сущности.
 * 
 * Компоненты - это части сущности, которые определяют её поведение и свойства.
 * Следует паттерну Entity-Component-System (ECS).
 */
abstract class Component : IUpdatable {
    /**
     * Владелец компонента (сущность).
     */
    var owner: IEntityBase? = null
    
    /**
     * Активен ли компонент.
     */
    var enabled: Boolean = true
        set(value) {
            field = value
            if (value) onEnable() else onDisable()
        }

    /**
     * Вызывается при добавлении компонента к сущности.
     */
    open fun onAdded() {}

    /**
     * Вызывается при удалении компонента из сущности.
     */
    open fun onRemoved() {}

    /**
     * Вызывается при включении компонента.
     */
    open fun onEnable() {}

    /**
     * Вызывается при отключении компонента.
     */
    open fun onDisable() {}

    override fun update(deltaTime: Float) {
        if (!enabled) return
        onUpdate(deltaTime)
    }

    /**
     * Обновление компонента.
     * Переопределяется в наследниках.
     */
    open fun onUpdate(deltaTime: Float) {}
}

/**
 * Базовый интерфейс для сущности (упрощённая версия).
 */
interface IEntityBase {
    val id: Long
    val isActive: Boolean
    var position: Vector2
    var velocity: Vector2
    var size: Vector2
}

/**
 * Компонент трансформации (позиция, вращение, масштаб).
 */
class TransformComponent(
    position: Vector2 = Vector2(),
    rotation: Float = 0f,
    scale: Vector2 = Vector2(1f, 1f)
) : Component() {
    var position: Vector2 = position
        set(value) {
            field = value
            owner?.position = value
        }

    var rotation: Float = rotation
    var scale: Vector2 = scale

    /**
     * Получение направления вперёд на основе вращения.
     */
    fun forward(): Vector2 = Vector2(
        kotlin.math.cos(kotlin.math.toRadians(rotation)),
        kotlin.math.sin(kotlin.math.toRadians(rotation))
    )

    /**
     * Перемещение вперёд на заданное расстояние.
     */
    fun translateForward(distance: Float) {
        val dir = forward()
        position = position + dir * distance
    }
}

/**
 * Компонент скорости (velocity).
 */
class VelocityComponent(
    initialVelocity: Vector2 = Vector2()
) : Component() {
    var velocity: Vector2 = initialVelocity
    var acceleration: Vector2 = Vector2()
    var maxSpeed: Float = Float.MAX_VALUE

    override fun onUpdate(deltaTime: Float) {
        // Применение ускорения
        velocity = velocity + acceleration * deltaTime
        
        // Ограничение максимальной скорости
        if (velocity.length > maxSpeed) {
            velocity = velocity.normalize() * maxSpeed
        }

        // Обновление позиции владельца
        owner?.let {
            it.position = it.position + velocity * deltaTime
        }
    }

    /**
     * Добавление импульса к скорости.
     */
    fun addImpulse(impulse: Vector2) {
        velocity = velocity + impulse
    }

    /**
     * Применение трения.
     */
    fun applyFriction(friction: Float, deltaTime: Float) {
        velocity = velocity * kotlin.math.pow(friction, deltaTime)
    }
}

/**
 * Компонент здоровья.
 */
class HealthComponent(
    maxHealth: Int = 100,
    startHealth: Int = maxHealth
) : Component() {
    var maxHealth: Int = maxHealth
    var currentHealth: Int = startHealth
        private set

    var isDead: Boolean = false
        private set

    var isInvincible: Boolean = false
    private var invincibilityTimer: Float = 0f

    /**
     * Получение урона.
     */
    fun takeDamage(amount: Int): Boolean {
        if (isInvincible || isDead) return false
        
        currentHealth = (currentHealth - amount).coerceAtLeast(0)
        
        if (currentHealth <= 0) {
            isDead = true
            onDeath()
        }
        
        onDamageTaken(amount)
        return true
    }

    /**
     * Лечение.
     */
    fun heal(amount: Int): Int {
        if (isDead) return 0
        
        val oldHealth = currentHealth
        currentHealth = (currentHealth + amount).coerceAtMost(maxHealth)
        
        return currentHealth - oldHealth
    }

    /**
     * Включение неуязвимости на время.
     */
    fun setInvincible(duration: Float) {
        isInvincible = true
        invincibilityTimer = duration
    }

    override fun onUpdate(deltaTime: Float) {
        if (isInvincible) {
            invincibilityTimer -= deltaTime
            if (invincibilityTimer <= 0) {
                isInvincible = false
            }
        }
    }

    /**
     * Сброс здоровья.
     */
    fun reset() {
        currentHealth = maxHealth
        isDead = false
        isInvincible = false
        invincibilityTimer = 0f
    }

    /**
     * Процент здоровья.
     */
    fun healthPercent(): Float = currentHealth.toFloat() / maxHealth.toFloat()

    /**
     * Вызывается при получении урона.
     */
    open fun onDamageTaken(amount: Int) {}

    /**
     * Вызывается при смерти.
     */
    open fun onDeath() {}
}

/**
 * Компонент спрайта (визуальное представление).
 */
class SpriteComponent(
    var color: Int = android.graphics.Color.WHITE,
    var width: Float = 1f,
    var height: Float = 1f
) : Component() {
    var isVisible: Boolean = true
    var flipHorizontal: Boolean = false
    var flipVertical: Boolean = false
    var tint: Int? = null
}
