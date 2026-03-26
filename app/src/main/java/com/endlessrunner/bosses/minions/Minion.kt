package com.endlessrunner.bosses.minions

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.endlessrunner.bosses.Boss
import com.endlessrunner.bosses.PatternType
import com.endlessrunner.components.PhysicsComponent
import com.endlessrunner.components.PositionComponent
import com.endlessrunner.components.RenderComponent
import com.endlessrunner.core.GameConstants
import com.endlessrunner.core.PooledObject
import com.endlessrunner.entities.Entity
import com.endlessrunner.entities.EntityManager
import kotlin.math.sqrt

/**
 * Тип миньона.
 */
enum class MinionType(
    val id: String,
    val baseHealth: Int,
    val baseDamage: Int,
    val moveSpeed: Float,
    val width: Float,
    val height: Float,
    val color: Int
) {
    /** Осколок слизня (от разделения) */
    SLIME_SPLIT(
        id = "slime_split",
        baseHealth = 50,
        baseDamage = 5,
        moveSpeed = 100f,
        width = 60f,
        height = 50f,
        color = Color.rgb(129, 199, 132)
    ),

    /** Яйцо дракона (вылупляется в мини-дрона) */
    DRAGON_EGG(
        id = "dragon_egg",
        baseHealth = 80,
        baseDamage = 8,
        moveSpeed = 80f,
        width = 50f,
        height = 70f,
        color = Color.rgb(255, 138, 101)
    ),

    /** Тёмное порождение */
    DARK_SPAWN(
        id = "dark_spawn",
        baseHealth = 100,
        baseDamage = 10,
        moveSpeed = 120f,
        width = 70f,
        height = 90f,
        color = Color.rgb(179, 136, 255)
    ),

    /** Сфера пустоты */
    VOID_ORB(
        id = "void_orb",
        baseHealth = 60,
        baseDamage = 12,
        moveSpeed = 150f,
        width = 40f,
        height = 40f,
        color = Color.rgb(206, 147, 216)
    )
}

/**
 * Миньон босса.
 * Наследуется от Entity, использует Object Pool.
 *
 * @property minionType Тип миньона
 * @property masterBoss Босс-хозяин
 */
class Minion(
    val minionType: MinionType,
    val masterBoss: Boss?
) : Entity(tag = "minion"), PooledObject {

    override var id: Long = 0
    override var isActive: Boolean = false
    override var poolId: Int = -1

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

    var health: Int = minionType.baseHealth
        private set

    val maxHealth: Int = minionType.baseHealth

    var damage: Int = minionType.baseDamage

    var spawnTimer: Float = 0f

    var state: MinionState = MinionState.IDLE
        private set

    var targetX: Float = 0f
    var targetY: Float = 0f

    var isDestroyed: Boolean = false
        private set

    private val paint = Paint().apply {
        color = minionType.color
        style = Paint.Style.FILL
    }

    // ============================================================================
    // ИНИЦИАЛИЗАЦИЯ
    // ============================================================================

    init {
        setupComponents()
    }

    private fun setupComponents() {
        addComponent(PositionComponent(0f, 0f))

        addComponent(
            RenderComponent(
                color = minionType.color,
                width = minionType.width,
                height = minionType.height
            )
        )

        addComponent(
            PhysicsComponent(
                width = minionType.width,
                height = minionType.height,
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
        health = maxHealth
        spawnTimer = 0f
        isDestroyed = false
        state = MinionState.IDLE
    }

    override fun update(deltaTime: Float) {
        if (!isActive || isDestroyed) return

        spawnTimer += deltaTime

        when (state) {
            MinionState.IDLE -> updateIdle(deltaTime)
            MinionState.FOLLOWING -> updateFollowing(deltaTime)
            MinionState.ATTACKING -> updateAttacking(deltaTime)
            MinionState.RETURNING -> updateReturning(deltaTime)
        }

        super.update(deltaTime)
    }

    override fun render(canvas: Canvas) {
        if (!isActive || isDestroyed) return

        val pos = positionComponent ?: return

        // Отрисовка миньона
        when (minionType) {
            MinionType.SLIME_SPLIT -> renderSlime(canvas, pos)
            MinionType.DRAGON_EGG -> renderEgg(canvas, pos)
            MinionType.DARK_SPAWN -> renderDarkSpawn(canvas, pos)
            MinionType.VOID_ORB -> renderVoidOrb(canvas, pos)
        }

        super.render(canvas)
    }

    override fun reset() {
        super.reset()
        health = maxHealth
        spawnTimer = 0f
        isDestroyed = false
        state = MinionState.IDLE
        targetX = 0f
        targetY = 0f
    }

    // ============================================================================
    // ПОВЕДЕНИЕ
    // ============================================================================

    private fun updateIdle(deltaTime: Float) {
        // Простое парение на месте
        val pos = positionComponent ?: return
        pos.y += kotlin.math.sin(spawnTimer * 3f) * 0.5f
    }

    private fun updateFollowing(deltaTime: Float) {
        val pos = positionComponent ?: return

        val dx = targetX - pos.x
        val dy = targetY - pos.y
        val distance = sqrt(dx * dx + dy * dy)

        if (distance > 50f) {
            // Движение к цели
            val speed = minionType.moveSpeed
            pos.x += (dx / distance) * speed * deltaTime
            pos.y += (dy / distance) * speed * deltaTime
        } else {
            // Достиг цели
            state = MinionState.ATTACKING
        }
    }

    private fun updateAttacking(deltaTime: Float) {
        // Атака игрока
        val playerPos = getPlayerPosition() ?: return

        targetX = playerPos.first
        targetY = playerPos.second

        val pos = positionComponent ?: return
        val dx = targetX - pos.x
        val dy = targetY - pos.y
        val distance = sqrt(dx * dx + dy * dy)

        if (distance < 100f) {
            // В радиусе атаки
            dealDamageToPlayer()
        } else {
            state = MinionState.FOLLOWING
        }
    }

    private fun updateReturning(deltaTime: Float) {
        val bossPos = masterBoss?.positionComponent ?: return

        val pos = positionComponent ?: return
        val dx = bossPos.x - pos.x
        val dy = bossPos.y - pos.y
        val distance = sqrt(dx * dx + dy * dy)

        if (distance > 50f) {
            pos.x += (dx / distance) * minionType.moveSpeed * deltaTime
            pos.y += (dy / distance) * minionType.moveSpeed * deltaTime
        } else {
            state = MinionState.FOLLOWING
        }
    }

    // ============================================================================
    // БОЕВЫЕ ДЕЙСТВИЯ
    // ============================================================================

    fun takeDamage(amount: Int): Boolean {
        if (isDestroyed) return false

        health -= amount

        if (health <= 0) {
            health = 0
            onDestroyed()
            return true
        }

        return false
    }

    private fun onDestroyed() {
        isDestroyed = true
        markForDestroy()
    }

    private fun dealDamageToPlayer() {
        // Делегируется GameManager
        // TODO: gameManager.onDamageTaken(damage)
    }

    // ============================================================================
    // УПРАВЛЕНИЕ СОСТОЯНИЕМ
    // ============================================================================

    fun followTarget(x: Float, y: Float) {
        targetX = x
        targetY = y
        state = MinionState.FOLLOWING
    }

    fun attackPlayer() {
        state = MinionState.ATTACKING
    }

    fun returnToMaster() {
        state = MinionState.RETURNING
    }

    fun onMasterDeath() {
        // Миньон становится агрессивным или исчезает
        state = MinionState.ATTACKING
        // Или: markForDestroy()
    }

    // ============================================================================
    // ОТРИСОВКА
    // ============================================================================

    private fun renderSlime(canvas: Canvas, pos: PositionComponent) {
        canvas.drawOval(
            pos.x - minionType.width / 2,
            pos.y - minionType.height / 2,
            pos.x + minionType.width / 2,
            pos.y + minionType.height / 2,
            paint
        )

        // Глаза
        val eyePaint = Paint().apply { color = Color.WHITE }
        val pupilPaint = Paint().apply { color = Color.BLACK }
        canvas.drawCircle(pos.x - 10f, pos.y - 5f, 8f, eyePaint)
        canvas.drawCircle(pos.x + 10f, pos.y - 5f, 8f, eyePaint)
        canvas.drawCircle(pos.x - 10f, pos.y - 5f, 4f, pupilPaint)
        canvas.drawCircle(pos.x + 10f, pos.y - 5f, 4f, pupilPaint)
    }

    private fun renderEgg(canvas: Canvas, pos: PositionComponent) {
        canvas.drawOval(
            pos.x - minionType.width / 2,
            pos.y - minionType.height / 2,
            pos.x + minionType.width / 2,
            pos.y + minionType.height / 2,
            paint
        )

        // Трещины
        val crackPaint = Paint().apply {
            color = Color.rgb(100, 50, 50)
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(pos.x - 10f, pos.y - 20f, pos.x + 5f, pos.y - 10f, crackPaint)
        canvas.drawLine(pos.x + 5f, pos.y - 10f, pos.x - 5f, pos.y, crackPaint)
    }

    private fun renderDarkSpawn(canvas: Canvas, pos: PositionComponent) {
        canvas.drawRect(
            pos.x - minionType.width / 2,
            pos.y - minionType.height / 2,
            pos.x + minionType.width / 2,
            pos.y + minionType.height / 2,
            paint
        )

        // Глаза
        val eyePaint = Paint().apply { color = Color.rgb(255, 255, 0) }
        canvas.drawCircle(pos.x - 15f, pos.y - 20f, 6f, eyePaint)
        canvas.drawCircle(pos.x + 15f, pos.y - 20f, 6f, eyePaint)
    }

    private fun renderVoidOrb(canvas: Canvas, pos: PositionComponent) {
        // Свечение
        val glowPaint = Paint().apply {
            color = minionType.color
            alpha = 80
        }
        canvas.drawCircle(pos.x, pos.y, minionType.width, glowPaint)

        // Ядро
        canvas.drawCircle(pos.x, pos.y, minionType.width / 2, paint)
    }

    // ============================================================================
    // УТИЛИТЫ
    // ============================================================================

    private fun getPlayerPosition(): Pair<Float, Float>? {
        // Получение позиции игрока
        return null // TODO: Получить из EntityManager
    }

    override fun toString(): String {
        return "Minion(type=${minionType.id}, health=$health/$maxHealth, state=$state)"
    }
}

/**
 * Состояние миньона.
 */
enum class MinionState {
    /** Бездействие */
    IDLE,

    /** Следование за целью */
    FOLLOWING,

    /** Атака */
    ATTACKING,

    /** Возврат к боссу */
    RETURNING
}

/**
 * Пул миньонов для переиспользования.
 */
class MinionPool private constructor() {

    companion object {
        @Volatile
        private var instance: MinionPool? = null

        fun getInstance(): MinionPool {
            return instance ?: synchronized(this) {
                instance ?: MinionPool().also { instance = it }
            }
        }
    }

    private val pool = mutableMapOf<MinionType, ArrayDeque<Minion>>()
    private var createdCount: Int = 0

    init {
        MinionType.entries.forEach { type ->
            pool[type] = ArrayDeque(INITIAL_SIZE)
            repeat(INITIAL_SIZE) {
                pool[type]?.addLast(createMinion(type))
            }
        }
    }

    private fun createMinion(type: MinionType): Minion {
        return Minion(type, null).apply { poolId = createdCount++ }
    }

    fun acquire(type: MinionType, masterBoss: Boss? = null): Minion {
        val minionList = pool[type] ?: return Minion(type, masterBoss)

        val minion = if (minionList.isEmpty()) {
            createMinion(type)
        } else {
            minionList.removeFirst()
        }

        minion.masterBoss = masterBoss
        return minion
    }

    fun release(minion: Minion?) {
        if (minion == null) return

        minion.reset()
        val minionList = pool[minion.minionType] ?: return

        if (minionList.size < MAX_SIZE) {
            minionList.addLast(minion)
        }
    }

    fun clear() {
        pool.values.forEach { it.clear() }
        createdCount = 0
    }

    companion object {
        private const val INITIAL_SIZE = 20
        private const val MAX_SIZE = 100
    }
}
