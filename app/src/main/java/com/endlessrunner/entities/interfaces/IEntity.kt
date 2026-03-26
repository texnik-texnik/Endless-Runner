package com.endlessrunner.entities.interfaces

/**
 * Базовый интерфейс для всех игровых сущностей.
 * 
 * Определяет общий контракт для объектов игрового мира,
 * таких как игрок, враги, предметы, препятствия и т.д.
 */
interface IEntity {
    /**
     * Уникальный идентификатор сущности.
     * Генерируется при создании и не изменяется.
     */
    val id: Long

    /**
     * Тип сущности.
     * Используется для категоризации и фильтрации.
     */
    val type: EntityType

    /**
     * Проверка, активна ли сущность.
     * Неактивные сущности не обновляются и не рендерятся.
     */
    val isActive: Boolean

    /**
     * Позиция сущности в игровом мире.
     */
    var position: Vector2

    /**
     * Скорость сущности.
     */
    var velocity: Vector2

    /**
     * Размеры сущности (хитбокс).
     */
    var size: Vector2

    /**
     * Активация сущности.
     * Вызывается при создании или возрождении.
     */
    fun activate()

    /**
     * Деактивация сущности.
     * Вызывается при уничтожении или скрытии.
     */
    fun deactivate()

    /**
     * Полное уничтожение сущности.
     * Освобождает ресурсы и удаляет из менеджера сущностей.
     */
    fun destroy()

    /**
     * Проверка столкновения с другой сущностью.
     * 
     * @param other Другая сущность для проверки
     * @return true если есть столкновение
     */
    fun collidesWith(other: IEntity): Boolean

    /**
     * Обработка столкновения с другой сущностью.
     * 
     * @param other Сущность, с которой произошло столкновение
     */
    fun onCollision(other: IEntity)
}

/**
 * Типы игровых сущностей.
 */
enum class EntityType {
    PLAYER,
    ENEMY,
    OBSTACLE,
    COIN,
    POWERUP,
    PLATFORM,
    PARTICLE,
    DECORATION,
    TRIGGER,
    OTHER
}

/**
 * Простой 2D вектор для позиции и скорости.
 */
data class Vector2(
    var x: Float = 0f,
    var y: Float = 0f
) {
    /**
     * Длина вектора (модуль).
     */
    val length: Float
        get() = kotlin.math.sqrt(x * x + y * y)

    /**
     * Квадрат длины вектора (быстрее для сравнений).
     */
    val lengthSquared: Float
        get() = x * x + y * y

    /**
     * Нормализация вектора (единичная длина).
     */
    fun normalize(): Vector2 {
        val len = length
        return if (len > 0) Vector2(x / len, y / len) else Vector2(0f, 0f)
    }

    /**
     * Сложение векторов.
     */
    operator fun plus(other: Vector2): Vector2 = Vector2(x + other.x, y + other.y)

    /**
     * Вычитание векторов.
     */
    operator fun minus(other: Vector2): Vector2 = Vector2(x - other.x, y - other.y)

    /**
     * Умножение на скаляр.
     */
    operator fun times(scalar: Float): Vector2 = Vector2(x * scalar, y * scalar)

    /**
     * Деление на скаляр.
     */
    operator fun div(scalar: Float): Vector2 = Vector2(x / scalar, y / scalar)

    /**
     * Расстояние до другой точки.
     */
    fun distanceTo(other: Vector2): Float = kotlin.math.sqrt(
        (x - other.x) * (x - other.x) + (y - other.y) * (y - other.y)
    )

    /**
     * Квадрат расстояния (быстрее для сравнений).
     */
    fun distanceSquaredTo(other: Vector2): Float =
        (x - other.x) * (x - other.x) + (y - other.y) * (y - other.y)

    companion object {
        val Zero = Vector2(0f, 0f)
        val Up = Vector2(0f, 1f)
        val Down = Vector2(0f, -1f)
        val Left = Vector2(-1f, 0f)
        val Right = Vector2(1f, 0f)
    }
}
