package com.endlessrunner.systems

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.endlessrunner.components.PhysicsComponent
import com.endlessrunner.components.PositionComponent
import com.endlessrunner.config.GameConfig
import com.endlessrunner.core.GameConstants
import com.endlessrunner.entities.EntityManager
import com.endlessrunner.entities.Entity
import com.endlessrunner.player.Player
import com.endlessrunner.collectibles.Coin

/**
 * Система коллизий.
 * Обрабатывает AABB коллизии между сущностями.
 * 
 * @param entityManager Менеджер сущностей
 * @param config Конфигурация игры
 */
class CollisionSystem(
    entityManager: EntityManager,
    config: GameConfig = GameConfig.DEFAULT
) : BaseSystem(entityManager, config), CollisionHandler {
    
    companion object {
        /** Paint для отладочной отрисовки хитбоксов */
        private val debugPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
    }
    
    /** Список коллизий для обработки */
    private val collisionQueue: MutableList<CollisionPair> = mutableListOf()
    
    /** Флаг включения broad phase */
    var useBroadPhase: Boolean = true
    
    /** Размер ячейки для spatial hashing */
    var cellSize: Float = 100f
    
    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        
        // Очистка предыдущих коллизий
        clearPreviousCollisions()
        
        // Сбор потенциальных коллизий
        findCollisions()
        
        // Обработка коллизий
        processCollisions()
    }
    
    /**
     * Очистка предыдущих коллизий.
     */
    private fun clearPreviousCollisions() {
        val entities = entityManager.getAll()
        for (entity in entities) {
            entity.getComponent<PhysicsComponent>()?.clearCollisions()
        }
    }
    
    /**
     * Поиск коллизий (broad phase + narrow phase).
     */
    private fun findCollisions() {
        val entities = entityManager.getAll()
        val physicsEntities = entities.filter { 
            it.isActive && 
            !it.isPendingDestroy && 
            it.hasComponent<PhysicsComponent>() 
        }
        
        if (useBroadPhase) {
            // Broad phase - быстрая проверка
            broadPhase(physicsEntities)
        } else {
            // Naive approach - проверка всех пар
            naiveBroadPhase(physicsEntities)
        }
    }
    
    /**
     * Broad phase с использованием spatial hashing.
     */
    private fun broadPhase(entities: List<Entity>) {
        // Создание spatial hash map
        val spatialMap = mutableMapOf<Pair<Int, Int>, MutableList<Entity>>()
        
        // Распределение сущностей по ячейкам
        for (entity in entities) {
            val physics = entity.getComponent<PhysicsComponent>() ?: continue
            val position = entity.getComponent<PositionComponent>() ?: continue
            
            val bounds = physics.getBounds()
            
            // Определение ячеек, которые занимает сущность
            val minX = (bounds.left / cellSize).toInt()
            val maxX = (bounds.right / cellSize).toInt()
            val minY = (bounds.top / cellSize).toInt()
            val maxY = (bounds.bottom / cellSize).toInt()
            
            // Добавление в ячейки
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    val cell = Pair(x, y)
                    spatialMap.getOrPut(cell) { mutableListOf() }.add(entity)
                }
            }
        }
        
        // Проверка коллизий в каждой ячейке
        val checkedPairs = mutableSetOf<Pair<Long, Long>>()
        
        for ((_, cellEntities) in spatialMap) {
            if (cellEntities.size < 2) continue
            
            for (i in cellEntities.indices) {
                for (j in i + 1 until cellEntities.size) {
                    val entity1 = cellEntities[i]
                    val entity2 = cellEntities[j]
                    
                    // Проверка, что пара ещё не проверялась
                    val pairId = Pair(
                        minOf(entity1.id, entity2.id),
                        maxOf(entity1.id, entity2.id)
                    )
                    
                    if (checkedPairs.contains(pairId)) continue
                    checkedPairs.add(pairId)
                    
                    // Narrow phase
                    if (checkNarrowPhase(entity1, entity2)) {
                        collisionQueue.add(CollisionPair(entity1, entity2))
                    }
                }
            }
        }
    }
    
    /**
     * Naive broad phase - проверка всех пар.
     */
    private fun naiveBroadPhase(entities: List<Entity>) {
        for (i in entities.indices) {
            for (j in i + 1 until entities.size) {
                val entity1 = entities[i]
                val entity2 = entities[j]
                
                if (checkNarrowPhase(entity1, entity2)) {
                    collisionQueue.add(CollisionPair(entity1, entity2))
                }
            }
        }
    }
    
    /**
     * Narrow phase - точная проверка коллизии.
     */
    private fun checkNarrowPhase(entity1: Entity, entity2: Entity): Boolean {
        val physics1 = entity1.getComponent<PhysicsComponent>() ?: return false
        val physics2 = entity2.getComponent<PhysicsComponent>() ?: return false
        
        // Проверка слоёв коллизий
        if (!checkCollisionLayers(physics1, physics2)) return false
        
        // Проверка перекрытия
        return physics1.overlaps(physics2)
    }
    
    /**
     * Проверка слоёв коллизий.
     */
    private fun checkCollisionLayers(physics1: PhysicsComponent, physics2: PhysicsComponent): Boolean {
        val mask1 = physics1.getCollisionMask()
        val mask2 = physics2.getCollisionMask()
        
        // Проверка, совместимы ли слои
        return (mask1 and physics2.collisionLayer) != 0 ||
               (mask2 and physics1.collisionLayer) != 0
    }
    
    /**
     * Обработка найденных коллизий.
     */
    private fun processCollisions() {
        for (collision in collisionQueue) {
            val entity1 = collision.entity1
            val entity2 = collision.entity2
            
            val physics1 = entity1.getComponent<PhysicsComponent>()
            val physics2 = entity2.getComponent<PhysicsComponent>()
            
            if (physics1 == null || physics2 == null) continue
            
            // Уведомление компонентов о коллизии
            physics1.onCollisionEnter(physics2)
            physics2.onCollisionEnter(physics1)
            
            // Обработка коллизии
            handleCollision(entity1, entity2)
        }
        
        collisionQueue.clear()
    }
    
    /**
     * Обработка конкретной коллизии.
     */
    private fun handleCollision(entity1: Entity, entity2: Entity) {
        // Игрок + Монета
        if (entity1 is Player && entity2 is Coin) {
            entity2.collect(entity1)
            return
        }
        if (entity2 is Player && entity1 is Coin) {
            entity1.collect(entity2)
            return
        }

        // Игрок + Враг
        if (entity1 is Player && entity2.tag == GameConstants.TAG_ENEMY) {
            handleEnemyPlayerCollision(entity2, entity1)
            return
        }
        if (entity2 is Player && entity1.tag == GameConstants.TAG_ENEMY) {
            handleEnemyPlayerCollision(entity1, entity2)
            return
        }

        // Игрок + Препятствие
        if (entity1 is Player && entity2.tag == GameConstants.TAG_OBSTACLE) {
            if (!entity1.isInvincible) {
                entity1.takeDamage(1)
            }
            return
        }
        if (entity2 is Player && entity1.tag == GameConstants.TAG_OBSTACLE) {
            if (!entity2.isInvincible) {
                entity2.takeDamage(1)
            }
            return
        }

        // Игрок + Земля
        if (entity1 is Player && entity2.tag == GameConstants.TAG_GROUND) {
            resolvePlayerGroundCollision(entity1, entity2)
            return
        }
        if (entity2 is Player && entity1.tag == GameConstants.TAG_GROUND) {
            resolvePlayerGroundCollision(entity2, entity1)
            return
        }

        // Вызов callback
        onCollision(entity1, entity2)
    }

    /**
     * Обработка коллизии врага с игроком.
     */
    private fun handleEnemyPlayerCollision(enemy: Entity, player: Player) {
        if (player.isInvincible || player.isDead) return

        // Получение урона от врага
        val enemyDamage = when {
            enemy is com.endlessrunner.enemies.Enemy -> enemy.damage
            else -> 1
        }

        player.takeDamage(enemyDamage)

        // Отталкивание игрока
        applyKnockback(player, enemy)

        // Уведомление врага о коллизии
        if (enemy is com.endlessrunner.enemies.Enemy) {
            enemy.onCollideWithPlayer(player)
        }

        Log.d(TAG, "Коллизия с врагом: урон=$enemyDamage")
    }

    /**
     * Применение отталкивания игроку при коллизии.
     */
    private fun applyKnockback(player: Player, enemy: Entity) {
        val playerPos = player.positionComponent ?: return
        val enemyPos = enemy.getComponent<com.endlessrunner.components.PositionComponent>() ?: return

        // Определение направления отталкивания
        val dx = playerPos.x - enemyPos.x
        val knockbackForce = 300f

        if (dx != 0f) {
            playerPos.vx = kotlin.math.sign(dx) * knockbackForce
            playerPos.vy = -200f // Небольшой подброс вверх
        }
    }
    
    /**
     * Разрешение коллизии игрока с землёй.
     */
    private fun resolvePlayerGroundCollision(player: Player, ground: Entity) {
        val playerPhysics = player.physicsComponent ?: return
        val playerPosition = player.positionComponent ?: return
        val groundPhysics = ground.getComponent<PhysicsComponent>() ?: return
        
        val playerBounds = playerPhysics.getBounds()
        val groundBounds = groundPhysics.getBounds()
        
        // Проверка, что игрок падает на землю
        if (playerPosition.vy >= 0 && 
            playerBounds.bottom <= groundBounds.top + 10f) {
            
            // Приземление
            playerPosition.y = groundBounds.top - playerBounds.height / 2f
            playerPosition.vy = 0f
            player.movementComponent?.onLand()
        }
    }
    
    override fun onCollision(entity1: Entity, entity2: Entity) {
        // Переопределяется в наследниках или через callback
    }
    
    override fun render(canvas: Canvas) {
        super.render(canvas)
        
        // Отладочная отрисовка хитбоксов
        if (config.game.showHitboxes) {
            renderHitboxes(canvas)
        }
    }
    
    /**
     * Отрисовка хитбоксов.
     */
    private fun renderHitboxes(canvas: Canvas) {
        val entities = entityManager.getAll()

        for (entity in entities) {
            if (!entity.isActive) continue

            val physics = entity.getComponent<PhysicsComponent>() ?: continue
            val bounds = physics.getBounds()

            // Цвет в зависимости от типа
            debugPaint.color = when {
                entity is Player -> Color.RED
                entity is Coin -> Color.YELLOW
                entity.tag == GameConstants.TAG_ENEMY -> Color.rgb(255, 87, 34) // Deep Orange для врагов
                entity.tag == GameConstants.TAG_OBSTACLE -> Color.GREEN
                else -> Color.WHITE
            }

            canvas.drawRect(bounds, debugPaint)
        }
    }
    
    override fun reset() {
        super.reset()
        collisionQueue.clear()
    }
    
    /**
     * Data class для пары коллизии.
     */
    private data class CollisionPair(
        val entity1: Entity,
        val entity2: Entity
    )
}

/**
 * Extension функция для проверки коллизии между двумя сущностями.
 */
fun checkCollision(entity1: Entity, entity2: Entity): Boolean {
    val physics1 = entity1.getComponent<PhysicsComponent>() ?: return false
    val physics2 = entity2.getComponent<PhysicsComponent>() ?: return false
    
    return physics1.overlaps(physics2)
}

/**
 * Extension функция для получения всех коллизий сущности.
 */
fun Entity.getCollisions(): List<Entity> {
    return getComponent<PhysicsComponent>()?.getCollidingEntities() ?: emptyList()
}
