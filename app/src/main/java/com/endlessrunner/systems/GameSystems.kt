package com.endlessrunner.systems

import android.view.MotionEvent
import com.endlessrunner.entities.interfaces.IEntity
import com.endlessrunner.entities.interfaces.IUpdatable
import com.endlessrunner.entities.interfaces.Vector2

/**
 * Базовый класс для игровых систем.
 *
 * Системы обрабатывают сущности с определёнными компонентами.
 * Следует паттерну Entity-Component-System (ECS).
 */
abstract class GameSystem : IUpdatable {
    /**
     * Приоритет системы (системы с меньшим приоритетом обновляются первыми).
     */
    open val priority: Int = 0

    /**
     * Активна ли система.
     */
    var enabled: Boolean = true

    /**
     * Инициализация системы.
     * Вызывается один раз при создании.
     */
    open fun init() {}

    /**
     * Вызывается при каждом обновлении.
     */
    override fun update(deltaTime: Float) {
        if (!enabled) return
        onUpdate(deltaTime)
    }

    /**
     * Фиксированное обновление (для физики).
     */
    open fun fixedUpdate(fixedDeltaTime: Float) {
        if (!enabled) return
        onFixedUpdate(fixedDeltaTime)
    }

    /**
     * Позднее обновление (после всех систем).
     */
    open fun lateUpdate(deltaTime: Float) {
        if (!enabled) return
        onLateUpdate(deltaTime)
    }

    /**
     * Обновление системы.
     */
    protected open fun onUpdate(deltaTime: Float) {}

    /**
     * Фиксированное обновление системы.
     */
    protected open fun onFixedUpdate(fixedDeltaTime: Float) {}

    /**
     * Позднее обновление системы.
     */
    protected open fun onLateUpdate(deltaTime: Float) {}

    /**
     * Вызывается при добавлении сущности в систему.
     */
    open fun onEntityAdded(entity: IEntity) {}

    /**
     * Вызывается при удалении сущности из системы.
     */
    open fun onEntityRemoved(entity: IEntity) {}

    /**
     * Очистка ресурсов системы.
     */
    open fun dispose() {}
}

/**
 * Данные о касании.
 */
data class TouchEvent(
    val x: Float,
    val y: Float,
    val deltaX: Float = 0f,
    val deltaY: Float = 0f,
    val action: TouchAction,
    val pointerId: Int = 0,
    val timestamp: Long = 0L
)

enum class TouchAction {
    DOWN,
    MOVE,
    UP,
    CANCEL
}

/**
 * Система физики.
 * Обрабатывает движение, коллизии и гравитацию.
 */
class PhysicsSystem(
    private val gravity: Float = -30f,
    private val collisionMargin: Float = 0.05f
) : GameSystem() {
    override val priority: Int = 0

    private val entities = mutableListOf<IEntity>()

    fun addEntity(entity: IEntity) {
        if (!entities.contains(entity)) {
            entities.add(entity)
            onEntityAdded(entity)
        }
    }

    fun removeEntity(entity: IEntity) {
        entities.remove(entity)
        onEntityRemoved(entity)
    }

    fun getEntities(): List<IEntity> = entities.toList()

    override fun onFixedUpdate(fixedDeltaTime: Float) {
        // Применение гравитации ко всем активным сущностям
        entities.filter { it.isActive }.forEach { entity ->
            // Гравитация применяется через VelocityComponent в сущности
        }
    }

    /**
     * Проверка коллизий между всеми сущностями.
     */
    fun checkCollisions(): List<Pair<IEntity, IEntity>> {
        val collisions = mutableListOf<Pair<IEntity, IEntity>>()
        
        for (i in entities.indices) {
            for (j in (i + 1) until entities.size) {
                val a = entities[i]
                val b = entities[j]

                if (a.isActive && b.isActive && a.collidesWith(b)) {
                    collisions.add(Pair(a, b))
                    a.onCollision(b)
                    b.onCollision(a)
                }
            }
        }
        
        return collisions
    }

    /**
     * AABB коллизия с учётом margin.
     */
    fun checkCollision(a: IEntity, b: IEntity): Boolean {
        return a.position.x - collisionMargin < b.position.x + b.size.x &&
               a.position.x + a.size.x + collisionMargin > b.position.x &&
               a.position.y - collisionMargin < b.position.y + b.size.y &&
               a.position.y + a.size.y + collisionMargin > b.position.y
    }

    override fun dispose() {
        entities.clear()
    }
}

/**
 * Система рендеринга.
 * Управляет отрисовкой всех видимых сущностей.
 */
class RenderSystem : GameSystem() {
    override val priority: Int = 100

    private val renderables = mutableListOf<IEntity>()

    fun addRenderable(entity: IEntity) {
        if (!renderables.contains(entity)) {
            renderables.add(entity)
            onEntityAdded(entity)
        }
    }

    fun removeRenderable(entity: IEntity) {
        renderables.remove(entity)
        onEntityRemoved(entity)
    }

    fun getRenderables(): List<IEntity> = renderables.toList()

    override fun onUpdate(deltaTime: Float) {
        // Обновление анимаций и визуальных эффектов
    }

    /**
     * Отрисовка всех сущностей.
     * Вызывается из GameView.
     */
    fun renderAll(
        canvas: android.graphics.Canvas,
        cameraPosition: Vector2,
        viewportSize: Vector2
    ) {
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
        }

        renderables
            .filter { it.isActive }
            .sortedBy { getRenderPriority(it) }
            .forEach { entity ->
                val screenX = (entity.position.x - cameraPosition.x) * viewportSize.x
                val screenY = (entity.position.y - cameraPosition.y) * viewportSize.y
                val screenWidth = entity.size.x * viewportSize.x
                val screenHeight = entity.size.y * viewportSize.y

                val color = when (entity) {
                    is com.endlessrunner.entities.CoinEntity -> android.graphics.Color.parseColor("#FFD700")
                    is com.endlessrunner.entities.ObstacleEntity -> android.graphics.Color.parseColor("#8B4513")
                    is com.endlessrunner.entities.EnemyEntity -> android.graphics.Color.parseColor("#DC143C")
                    is com.endlessrunner.entities.PowerUpEntity -> android.graphics.Color.parseColor("#32CD32")
                    is com.endlessrunner.entities.PlatformEntity -> android.graphics.Color.parseColor("#696969")
                    is com.endlessrunner.entities.ParticleEntity -> entity.color
                    is com.endlessrunner.entities.PlayerEntity -> android.graphics.Color.parseColor("#4CAF50")
                    else -> android.graphics.Color.WHITE
                }

                paint.color = color
                canvas.drawRect(
                    screenX,
                    screenY,
                    screenX + screenWidth,
                    screenY + screenHeight,
                    paint
                )
            }
    }

    private fun getRenderPriority(entity: IEntity): Int {
        return when (entity.type) {
            com.endlessrunner.entities.interfaces.EntityType.PLATFORM -> 0
            com.endlessrunner.entities.interfaces.EntityType.COIN,
            com.endlessrunner.entities.interfaces.EntityType.POWERUP -> 1
            com.endlessrunner.entities.interfaces.EntityType.PLAYER,
            com.endlessrunner.entities.interfaces.EntityType.ENEMY,
            com.endlessrunner.entities.interfaces.EntityType.OBSTACLE -> 2
            com.endlessrunner.entities.interfaces.EntityType.PARTICLE -> 3
            else -> 1
        }
    }

    override fun dispose() {
        renderables.clear()
    }
}

/**
 * Система управления вводом.
 * Обрабатывает касания и жесты.
 */
class InputSystem : GameSystem() {
    override val priority: Int = 1

    /**
     * Очередь событий касания.
     */
    private val touchQueue = ArrayDeque<TouchEvent>()

    /**
     * Последняя позиция касания.
     */
    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f

    /**
     * Флаг прыжка.
     */
    private var jumpRequested: Boolean = false

    /**
     * Минимальное расстояние для свайпа.
     */
    private val minSwipeDistance: Float = 50f

    /**
     * Время для тапа.
     */
    private val tapTimeout: Long = 200L

    private var touchDownTime: Long = 0L

    /**
     * Обработка MotionEvent.
     */
    fun handleMotionEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val pointerIndex = event.actionIndex
        val x = event.getX(pointerIndex)
        val y = event.getY(pointerIndex)

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = x
                lastTouchY = y
                touchDownTime = System.currentTimeMillis()
                touchQueue.addLast(
                    TouchEvent(
                        x = x,
                        y = y,
                        action = TouchAction.DOWN,
                        pointerId = event.getPointerId(pointerIndex),
                        timestamp = System.currentTimeMillis()
                    )
                )
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - lastTouchX
                val deltaY = y - lastTouchY

                if (kotlin.math.abs(deltaX) > 1f || kotlin.math.abs(deltaY) > 1f) {
                    touchQueue.addLast(
                        TouchEvent(
                            x = x,
                            y = y,
                            deltaX = deltaX,
                            deltaY = deltaY,
                            action = TouchAction.MOVE,
                            pointerId = event.getPointerId(pointerIndex),
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    lastTouchX = x
                    lastTouchY = y
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                val touchDuration = System.currentTimeMillis() - touchDownTime
                touchQueue.addLast(
                    TouchEvent(
                        x = x,
                        y = y,
                        action = TouchAction.UP,
                        pointerId = event.getPointerId(pointerIndex),
                        timestamp = System.currentTimeMillis()
                    )
                )

                // Проверка на тап (короткое касание без перемещения)
                if (touchDuration < tapTimeout) {
                    jumpRequested = true
                }
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                touchQueue.addLast(
                    TouchEvent(
                        x = x,
                        y = y,
                        action = TouchAction.CANCEL,
                        timestamp = System.currentTimeMillis()
                    )
                )
                return true
            }
        }

        return false
    }

    /**
     * Получение следующего события касания.
     */
    fun pollTouchEvent(): TouchEvent? {
        return if (touchQueue.isNotEmpty()) touchQueue.removeFirst() else null
    }

    /**
     * Проверка, был ли запрошен прыжок.
     */
    fun isJumpRequested(): Boolean {
        val result = jumpRequested
        jumpRequested = false
        return result
    }

    /**
     * Запрос прыжка (для клавиатуры).
     */
    fun requestJump() {
        jumpRequested = true
    }

    /**
     * Проверка свайпа вверх.
     */
    fun isSwipeUp(threshold: Float = minSwipeDistance): Boolean {
        val events = touchQueue.toList()
        if (events.size < 2) return false

        val first = events.firstOrNull { it.action == TouchAction.DOWN } ?: return false
        val last = events.lastOrNull { it.action == TouchAction.UP } ?: return false

        return (last.y - first.y) < -threshold
    }

    override fun update(deltaTime: Float) {
        // Обработка событий ввода
    }

    fun clearQueue() {
        touchQueue.clear()
    }
}

/**
 * Система управления состоянием игры.
 */
class GameStateSystem : GameSystem() {
    override val priority: Int = 50

    var score: Int = 0
        private set

    var coins: Int = 0
        private set

    var level: Int = 1
        private set

    var highScore: Int = 0
        private set

    /**
     * Добавление очков.
     */
    fun addScore(points: Int) {
        score += points
        if (score > highScore) {
            highScore = score
        }
    }

    /**
     * Добавление монет.
     */
    fun addCoins(amount: Int) {
        coins += amount
        addScore(amount * 10)  // Монеты дают очки
    }

    /**
     * Переход на следующий уровень.
     */
    fun nextLevel() {
        level++
    }

    /**
     * Сброс прогресса.
     */
    fun reset() {
        score = 0
        coins = 0
        level = 1
    }

    /**
     * Загрузка рекорда.
     */
    fun loadHighScore(score: Int) {
        highScore = score
    }
}
