package com.endlessrunner.enemies

import android.graphics.Canvas
import com.endlessrunner.components.PhysicsComponent
import com.endlessrunner.components.PositionComponent
import com.endlessrunner.components.RenderComponent
import com.endlessrunner.core.GameConstants
import com.endlessrunner.core.PooledObject
import com.endlessrunner.entities.Entity
import com.endlessrunner.entities.EntityManager
import com.endlessrunner.player.Player

/**
 * Базовый класс врага.
 * Наследуется от Entity и использует Object Pool для производительности.
 *
 * @param type Тип врага
 * @param behavior Поведение врага
 * @param damage Урон, наносимый врагом
 * @param config Конфигурация врага
 */
open class Enemy(
    /** Тип врага */
    var type: EnemyType,
    
    /** Поведение врага (Strategy Pattern) */
    var behavior: EnemyBehavior? = null,
    
    /** Урон, наносимый игроку */
    var damage: Int = 1,
    
    /** Конфигурация врага */
    private val config: EnemyConfig = EnemyConfig.DEFAULT
) : Entity(tag = GameConstants.TAG_ENEMY) {

    companion object {
        /** Пул для переиспользования врагов */
        private val pool: com.endlessrunner.core.ObjectPool<Enemy> = 
            com.endlessrunner.core.ObjectPool(
                initialSize = GameConstants.ENEMY_POOL_INITIAL_SIZE,
                maxSize = GameConstants.ENEMY_POOL_MAX_SIZE,
                factory = { id ->
                    Enemy(
                        type = EnemyType.STATIC,
                        damage = 1
                    ).apply { poolId = id }
                }
            )
        
        /** Получение врага из пула */
        fun acquire(
            type: EnemyType = EnemyType.STATIC,
            behavior: EnemyBehavior? = null,
            damage: Int = 1,
            config: EnemyConfig = EnemyConfig.DEFAULT
        ): Enemy {
            val enemy = pool.acquire()
            enemy.type = type
            enemy.behavior = behavior
            enemy.damage = damage.coerceAtLeast(1)
            enemy.config = config
            return enemy
        }
        
        /** Возврат врага в пул */
        fun release(enemy: Enemy) {
            pool.release(enemy)
        }
        
        /** Статистика пула */
        val poolStats: com.endlessrunner.core.ObjectPool.PoolStats
            get() = pool.stats
    }

    // ============================================================================
    // КОМПОНЕНТЫ (кэшированные ссылки для производительности)
    // ============================================================================

    /** Компонент позиции */
    val positionComponent: PositionComponent?
        get() = getComponent()

    /** Компонент рендеринга */
    val renderComponent: RenderComponent?
        get() = getComponent()

    /** Компонент физики */
    val physicsComponent: PhysicsComponent?
        get() = getComponent()

    // ============================================================================
    // СВОЙСТВА ВРАГА
    // ============================================================================

    /** Уничтожен ли враг */
    var isDestroyed: Boolean = false
        private set

    /** Время жизни врага */
    var spawnTime: Float = 0f

    /** Расстояние, которое прошёл враг */
    var distanceTraveled: Float = 0f

    /** Текущая скорость врага */
    var currentSpeed: Float = type.baseSpeed

    /** Направление движения */
    var direction: MoveDirection = MoveDirection.LEFT

    /** Флаг активности поведения */
    var behaviorEnabled: Boolean = true

    // ============================================================================
    // КОЛЛИЗИИ
    // ============================================================================

    /** Игрок, с которым произошло столкновение */
    private var collidedPlayer: Player? = null

    /** Время последней коллизии с игроком */
    private var lastCollisionTime: Float = 0f

    /** Минимальное время между коллизиями (защита от многократного урона) */
    private val collisionCooldown: Float = 0.5f

    // ============================================================================
    // ИНИЦИАЛИЗАЦИЯ
    // ============================================================================

    init {
        setupComponents()
    }

    /**
     * Настройка компонентов врага.
     */
    private fun setupComponents() {
        // Позиция
        addComponent(PositionComponent(x = 0f, y = 0f))

        // Рендеринг
        addComponent(
            RenderComponent(
                color = type.debugColor,
                width = type.width,
                height = type.height
            )
        )

        // Физика
        addComponent(
            PhysicsComponent(
                width = type.width,
                height = type.height,
                collisionLayer = GameConstants.LAYER_OBSTACLE,
                isTrigger = false
            )
        )
    }

    // ============================================================================
    // ЖИЗНЕННЫЙ ЦИКЛ
    // ============================================================================

    override fun onActivate() {
        super.onActivate()
        spawnTime = 0f
        distanceTraveled = 0f
        isDestroyed = false
        collidedPlayer = null
        lastCollisionTime = -collisionCooldown
        
        // Инициализация поведения
        behavior?.onSpawn(this)
        
        // Установка скорости
        currentSpeed = type.baseSpeed * config.speedMultiplier
    }

    override fun onDeactivate() {
        behavior?.onDestroy(this)
        super.onDeactivate()
    }

    override fun update(deltaTime: Float) {
        if (!isActive || isDestroyed) return

        spawnTime += deltaTime

        // Обновление поведения
        if (behaviorEnabled) {
            behavior?.update(this, deltaTime)
        }

        // Обновление позиции на основе скорости
        updatePosition(deltaTime)

        // Проверка выхода за границы экрана
        checkBounds()

        super.update(deltaTime)
    }

    override fun render(canvas: Canvas) {
        if (!isActive || isDestroyed) return

        // Рендеринг через компоненты
        super.render(canvas)

        // Отладочная отрисовка
        if (config.showDebugInfo) {
            renderDebugInfo(canvas)
        }
    }

    override fun reset() {
        super.reset()
        type = EnemyType.STATIC
        damage = 1
        spawnTime = 0f
        distanceTraveled = 0f
        currentSpeed = 0f
        direction = MoveDirection.LEFT
        isDestroyed = false
        collidedPlayer = null
        lastCollisionTime = -collisionCooldown
        behaviorEnabled = true
        behavior?.reset()
    }

    // ============================================================================
    // ДВИЖЕНИЕ
    // ============================================================================

    /**
     * Обновление позиции на основе скорости и направления.
     */
    private fun updatePosition(deltaTime: Float) {
        val position = positionComponent ?: return

        val (dx, dy) = direction.vector
        
        if (dx != 0f || dy != 0f) {
            val deltaX = dx * currentSpeed * deltaTime
            val deltaY = dy * currentSpeed * deltaTime
            
            position.x += deltaX
            position.y += deltaY
            
            // Подсчёт пройденного расстояния
            distanceTraveled += kotlin.math.sqrt(deltaX * deltaX + deltaY * deltaY)
        }
    }

    /**
     * Проверка выхода за границы экрана.
     */
    private fun checkBounds() {
        val position = positionComponent ?: return
        val physics = physicsComponent ?: return
        
        val bounds = physics.getBounds()
        
        // Удаление если ушёл далеко за левую границу
        if (bounds.right < -config.destroyMargin) {
            markForDestroy()
        }
    }

    // ============================================================================
    // КОЛЛИЗИИ
    // ============================================================================

    /**
     * Обработка столкновения с игроком.
     *
     * @param player Игрок
     * @return true если урон нанесён
     */
    fun onCollideWithPlayer(player: Player): Boolean {
        // Проверка кулдауна
        if (spawnTime - lastCollisionTime < collisionCooldown) {
            return false
        }

        // Проверка неуязвимости игрока
        if (player.isInvincible || player.isDead) {
            return false
        }

        lastCollisionTime = spawnTime
        collidedPlayer = player

        // Нанесение урона
        val damageDealt = player.takeDamage(damage)
        
        if (damageDealt) {
            onPlayerHit(player)
        }

        return damageDealt
    }

    /**
     * Вызывается при нанесении урона игроку.
     */
    protected open fun onPlayerHit(player: Player) {
        // Переопределяется в наследниках
        // TODO: Добавить эффекты удара, тряску экрана
    }

    /**
     * Уничтожение врага.
     */
    open fun destroy() {
        if (isDestroyed) return
        
        isDestroyed = true
        behavior?.onDestroy(this)
        markForDestroy()
    }

    // ============================================================================
    // НАСТРОЙКИ
    // ============================================================================

    /**
     * Установка типа врага.
     */
    fun setType(newType: EnemyType) {
        type = newType
        damage = newType.baseDamage
        currentSpeed = newType.baseSpeed * config.speedMultiplier
        
        // Обновление хитбокса
        physicsComponent?.setSize(newType.width, newType.height)
        renderComponent?.setSize(newType.width, newType.height)
    }

    /**
     * Установка поведения.
     */
    fun setBehavior(newBehavior: EnemyBehavior) {
        behavior?.onDestroy(this)
        behavior = newBehavior
        behavior?.onSpawn(this)
    }

    /**
     * Установка скорости.
     */
    fun setSpeed(speed: Float) {
        currentSpeed = speed.coerceAtLeast(0f)
    }

    /**
     * Увеличение скорости на множитель.
     */
    fun multiplySpeed(multiplier: Float) {
        currentSpeed *= multiplier
    }

    // ============================================================================
    // УТИЛИТЫ
    // ============================================================================

    /**
     * Отрисовка отладочной информации.
     */
    private fun renderDebugInfo(canvas: Canvas) {
        val position = positionComponent ?: return
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 24f
        }
        
        canvas.drawText(
            "Enemy: ${type.id}",
            position.x - 50f,
            position.y - type.height / 2 - 10f,
            paint
        )
    }

    /**
     * Проверка, может ли враг быть уничтожен.
     */
    fun canBeDestroyed(): Boolean = isDestroyed || positionComponent?.x ?: 0f < -config.destroyMargin

    override fun toString(): String {
        return "Enemy(type=${type.id}, damage=$damage, active=$isActive, destroyed=$isDestroyed)"
    }
}

/**
 * Extension функция для получения всех активных врагов.
 */
fun EntityManager.getAllEnemies(): List<Enemy> {
    return getEntitiesByTag(GameConstants.TAG_ENEMY)
        .filterIsInstance<Enemy>()
        .filter { it.isActive && !it.isDestroyed }
}

/**
 * Extension функция для уничтожения всех врагов.
 */
fun EntityManager.destroyAllEnemies() {
    getAllEnemies().forEach { it.destroy() }
}

/**
 * Extension функция для применения урона всем врагам в радиусе.
 */
fun EntityManager.damageEnemiesInRadius(
    centerX: Float,
    centerY: Float,
    radius: Float,
    damage: Int
): Int {
    var count = 0
    getAllEnemies().forEach { enemy ->
        val pos = enemy.positionComponent ?: return@forEach
        val dx = pos.x - centerX
        val dy = pos.y - centerY
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        
        if (distance <= radius) {
            // TODO: Реализовать систему здоровья врагов
            enemy.destroy()
            count++
        }
    }
    return count
}
