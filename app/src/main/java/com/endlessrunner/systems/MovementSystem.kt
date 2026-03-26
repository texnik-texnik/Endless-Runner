package com.endlessrunner.systems

import android.graphics.Canvas
import com.endlessrunner.components.MovementComponent
import com.endlessrunner.components.PhysicsComponent
import com.endlessrunner.components.PositionComponent
import com.endlessrunner.config.GameConfig
import com.endlessrunner.core.GameConstants
import com.endlessrunner.entities.EntityManager
import com.endlessrunner.entities.Entity
import com.endlessrunner.player.Player

/**
 * Система движения.
 * Обновляет позиции всех сущностей с компонентами Position и Movement.
 * 
 * @param entityManager Менеджер сущностей
 * @param config Конфигурация игры
 */
class MovementSystem(
    entityManager: EntityManager,
    config: GameConfig = GameConfig.DEFAULT
) : BaseSystem(entityManager, config) {
    
    init {
        updatePriority = 10 // После InputSystem
    }
    
    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        
        // Обновление всех сущностей с MovementComponent
        val entities = entityManager.getAll()
        
        for (entity in entities) {
            if (!entity.isActive || entity.isPendingDestroy) continue
            
            val movement = entity.getComponent<MovementComponent>() ?: continue
            val position = entity.getComponent<PositionComponent>() ?: continue
            
            // Обновление движения уже происходит в компоненте
            // Здесь дополнительная логика системы
            
            // Ограничение позиции в пределах мира
            clampToWorldBounds(position, deltaTime)
            
            // Специальная логика для игрока
            if (entity is Player) {
                updatePlayer(entity, movement, position, deltaTime)
            }
        }
    }
    
    /**
     * Обновление игрока.
     */
    private fun updatePlayer(
        player: Player,
        movement: MovementComponent,
        position: PositionComponent,
        deltaTime: Float
    ) {
        // Проверка приземления
        if (movement.isGrounded && position.vy == 0f) {
            movement.onLand()
        }
        
        // Проверка отрыва от земли
        if (!movement.isGrounded && position.vy != 0f) {
            movement.onTakeOff()
        }
    }
    
    /**
     * Ограничение позиции в пределах мира.
     */
    private fun clampToWorldBounds(position: PositionComponent, deltaTime: Float) {
        // Ограничение по Y (пол)
        // Будет переопределено в CollisionSystem
        
        // Ограничение по X (бесконечный раннер - нет ограничений)
    }
    
    override fun render(canvas: Canvas) {
        super.render(canvas)
        
        // Отладочная отрисовка
        if (config.game.debugMode) {
            renderDebug(canvas)
        }
    }
    
    /**
     * Отладочная отрисовка.
     */
    private fun renderDebug(canvas: Canvas) {
        val entities = entityManager.getAll()
        
        for (entity in entities) {
            if (!entity.isActive) continue
            
            val position = entity.getComponent<PositionComponent>() ?: continue
            val movement = entity.getComponent<MovementComponent>()
            
            // Отрисовка вектора скорости
            if (movement != null) {
                val vx = position.vx
                val vy = position.vy
                
                if (vx != 0f || vy != 0f) {
                    canvas.drawLine(
                        position.x,
                        position.y,
                        position.x + vx * 0.1f,
                        position.y + vy * 0.1f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GREEN
                            strokeWidth = 2f
                        }
                    )
                }
            }
        }
    }
}

/**
 * Система применения гравитации.
 * Отдельная система для гравитации для гибкости.
 */
class GravitySystem(
    entityManager: EntityManager,
    config: GameConfig = GameConfig.DEFAULT
) : BaseSystem(entityManager, config) {
    
    /** Гравитация */
    var gravity: Float = config.physics.gravity
    
    init {
        updatePriority = 5
    }
    
    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        
        val entities = entityManager.getAll()
        
        for (entity in entities) {
            if (!entity.isActive || entity.isPendingDestroy) continue
            
            val position = entity.getComponent<PositionComponent>() ?: continue
            val physics = entity.getComponent<PhysicsComponent>()
            
            // Не применять гравитацию к триггерам
            if (physics?.isTrigger == true) continue
            
            // Применение гравитации
            position.vy += gravity * deltaTime
            
            // Ограничение терминальной скорости
            if (position.vy > GameConstants.TERMINAL_VELOCITY) {
                position.vy = GameConstants.TERMINAL_VELOCITY
            }
        }
    }
}

/**
 * Система движения для препятствий.
 * Двигает препятствия влево (симуляция движения игрока вправо).
 */
class ObstacleMovementSystem(
    entityManager: EntityManager,
    config: GameConfig = GameConfig.DEFAULT
) : BaseSystem(entityManager, config) {
    
    /** Скорость движения препятствий */
    var obstacleSpeed: Float = config.spawn.initialSpawnSpeed
    
    init {
        updatePriority = 15
    }
    
    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        
        val entities = entityManager.getAll()
        
        for (entity in entities) {
            if (!entity.isActive || entity.isPendingDestroy) continue
            
            // Препятствия (не игрок, не монеты)
            if (entity.tag == GameConstants.TAG_OBSTACLE) {
                val position = entity.getComponent<PositionComponent>() ?: continue
                
                position.x -= obstacleSpeed * deltaTime
                
                // Удаление если ушло за экран
                if (position.x < -200f) {
                    entity.markForDestroy()
                }
            }
        }
    }
    
    /**
     * Увеличение скорости препятствий.
     */
    fun increaseSpeed(amount: Float) {
        obstacleSpeed = (obstacleSpeed + amount).coerceAtMost(config.spawn.maxSpawnSpeed)
    }
    
    /**
     * Сброс скорости.
     */
    fun resetSpeed() {
        obstacleSpeed = config.spawn.initialSpawnSpeed
    }
}

/**
 * Extension функция для применения силы к сущности.
 */
fun Entity.applyForce(forceX: Float, forceY: Float) {
    getComponent<PositionComponent>()?.apply {
        vx += forceX
        vy += forceY
    }
}

/**
 * Extension функция для применения импульса.
 */
fun Entity.applyImpulse(impulseX: Float, impulseY: Float) {
    getComponent<PositionComponent>()?.apply {
        x += impulseX
        y += impulseY
    }
}
