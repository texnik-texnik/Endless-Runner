package com.endlessrunner.bosses.projectiles

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.endlessrunner.core.PooledObject

/**
 * Снаряд босса.
 * Использует Object Pool для производительности.
 *
 * @property damage Урон снаряда
 * @property lifetime Время жизни
 */
class BossProjectile(
    var damage: Int = 10,
    var lifetime: Float = 5f
) : PooledObject {

    override var id: Long = 0
    override var isActive: Boolean = false
    override var poolId: Int = -1

    /** Позиция снаряда */
    val position = Position2D(0f, 0f)

    /** Скорость снаряда */
    val velocity = Velocity2D(0f, 0f)

    /** Радиус снаряда */
    var radius: Float = 15f

    /** Время жизни снаряда */
    var timeAlive: Float = 0f
        private set

    /** Тип снаряда */
    var type: ProjectileType = ProjectileType.NORMAL

    /** Флаг взрыва */
    var isExploding: Boolean = false

    /** Проверка, истёк ли снаряд */
    val isExpired: Boolean
        get() = timeAlive >= lifetime || position.y > 1500f || position.y < -500f || position.x < -500f

    /**
     * Инициализация снаряда.
     */
    fun initialize(
        startX: Float,
        startY: Float,
        velocityX: Float,
        velocityY: Float,
        damage: Int = 10,
        lifetime: Float = 5f,
        type: ProjectileType = ProjectileType.NORMAL
    ) {
        position.x = startX
        position.y = startY
        velocity.x = velocityX
        velocity.y = velocityY
        this.damage = damage
        this.lifetime = lifetime
        this.type = type
        timeAlive = 0f
        isExploding = false
        isActive = true
    }

    /**
     * Обновление снаряда.
     */
    fun update(deltaTime: Float) {
        if (!isActive) return

        timeAlive += deltaTime

        // Обновление позиции
        position.x += velocity.x * deltaTime
        position.y += velocity.y * deltaTime

        // Гравитация для некоторых типов
        if (type == ProjectileType.METEOR || type == ProjectileType.RAIN) {
            velocity.y += 500f * deltaTime
        }

        // Проверка выхода за границы
        if (isExpired) {
            isActive = false
        }
    }

    /**
     * Проверка коллизии с точкой.
     */
    fun checkCollision(x: Float, y: Float, hitRadius: Float): Boolean {
        val dx = position.x - x
        val dy = position.y - y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        return distance < radius + hitRadius
    }

    /**
     * Проверка коллизии с прямоугольником.
     */
    fun checkCollision(rect: RectF): Boolean {
        val closestX = position.x.coerceIn(rect.left, rect.right)
        val closestY = position.y.coerceIn(rect.top, rect.bottom)

        val dx = position.x - closestX
        val dy = position.y - closestY
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        return distance < radius
    }

    /**
     * Отрисовка снаряда.
     */
    fun render(canvas: Canvas, paint: Paint) {
        if (!isActive) return

        when (type) {
            ProjectileType.NORMAL -> {
                canvas.drawCircle(position.x, position.y, radius, paint)
            }
            ProjectileType.METEOR -> {
                // Метеор со следом
                paint.alpha = 200
                canvas.drawCircle(position.x, position.y, radius, paint)
                paint.alpha = 100
                canvas.drawLine(
                    position.x,
                    position.y - 50f,
                    position.x,
                    position.y,
                    paint
                )
            }
            ProjectileType.HOMING -> {
                // Самонаводящийся снаряд
                canvas.drawCircle(position.x, position.y, radius + 5f, paint)
                canvas.drawCircle(position.x, position.y, radius - 5f, Paint().apply {
                    color = android.graphics.Color.WHITE
                    style = Paint.Style.FILL
                })
            }
            else -> {
                canvas.drawCircle(position.x, position.y, radius, paint)
            }
        }
    }

    /**
     * Сброс снаряда.
     */
    override fun reset() {
        position.x = 0f
        position.y = 0f
        velocity.x = 0f
        velocity.y = 0f
        damage = 10
        lifetime = 5f
        timeAlive = 0f
        type = ProjectileType.NORMAL
        isExploding = false
        isActive = false
    }

    override fun toString(): String {
        return "BossProjectile(type=$type, active=$isActive, timeAlive=$timeAlive)"
    }
}

/**
 * Позиция в 2D.
 */
data class Position2D(
    var x: Float = 0f,
    var y: Float = 0f
)

/**
 * Скорость в 2D.
 */
data class Velocity2D(
    var x: Float = 0f,
    var y: Float = 0f
)

/**
 * Тип снаряда.
 */
enum class ProjectileType {
    /** Обычный снаряд */
    NORMAL,

    /** Метеор */
    METEOR,

    /** Дождь снарядов */
    RAIN,

    /** Самонаводящийся */
    HOMING,

    /** Спиральный */
    SPIRAL,

    /** Орбитальный */
    ORBIT
}

/**
 * Пул снарядов для переиспользования.
 */
class ProjectilePool private constructor() {

    companion object {
        @Volatile
        private var instance: ProjectilePool? = null

        fun getInstance(): ProjectilePool {
            return instance ?: synchronized(this) {
                instance ?: ProjectilePool().also { instance = it }
            }
        }
    }

    private val pool = ArrayDeque<BossProjectile>(INITIAL_SIZE)
    private var createdCount: Int = 0

    init {
        // Предварительное создание снарядов
        repeat(INITIAL_SIZE) {
            pool.addLast(createProjectile())
        }
    }

    private fun createProjectile(): BossProjectile {
        return BossProjectile().apply {
            poolId = createdCount++
        }
    }

    /**
     * Получение снаряда из пула.
     */
    fun acquire(
        startX: Float,
        startY: Float,
        velocityX: Float,
        velocityY: Float,
        damage: Int = 10,
        lifetime: Float = 5f,
        type: ProjectileType = ProjectileType.NORMAL
    ): BossProjectile {
        val projectile = if (pool.isEmpty()) {
            createProjectile()
        } else {
            pool.removeFirst()
        }

        projectile.initialize(
            startX = startX,
            startY = startY,
            velocityX = velocityX,
            velocityY = velocityY,
            damage = damage,
            lifetime = lifetime,
            type = type
        )

        return projectile
    }

    /**
     * Возврат снаряда в пул.
     */
    fun release(projectile: BossProjectile?) {
        if (projectile == null) return

        projectile.reset()
        if (pool.size < MAX_SIZE) {
            pool.addLast(projectile)
        }
    }

    /**
     * Возврат всех снарядов в пул.
     */
    fun releaseAll(projectiles: Collection<BossProjectile>) {
        projectiles.forEach { release(it) }
    }

    /**
     * Статистика пула.
     */
    val stats: PoolStats
        get() = PoolStats(
            activeCount = createdCount - pool.size,
            pooledCount = pool.size,
            totalCount = createdCount
        )

    /**
     * Очистка пула.
     */
    fun clear() {
        pool.clear()
        createdCount = 0
    }

    data class PoolStats(
        val activeCount: Int,
        val pooledCount: Int,
        val totalCount: Int
    )

    companion object {
        private const val INITIAL_SIZE = 100
        private const val MAX_SIZE = 500
    }
}
