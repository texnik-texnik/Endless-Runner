package com.endlessrunner.systems

import android.graphics.Canvas
import com.endlessrunner.config.GameConfig
import com.endlessrunner.entities.EntityManager

/**
 * Базовый абстрактный класс системы.
 * Системы обрабатывают логику игры, работая с сущностями и компонентами.
 * 
 * @param entityManager Менеджер сущностей
 * @param config Конфигурация игры
 */
abstract class BaseSystem(
    protected val entityManager: EntityManager,
    protected val config: GameConfig = GameConfig.DEFAULT
) {
    
    /** Флаг включения системы */
    var isEnabled: Boolean = true
    
    /** Приоритет обновления (меньше = раньше) */
    var updatePriority: Int = 0
    
    /** Приоритет рендеринга (меньше = раньше) */
    var renderPriority: Int = 0
    
    /** Время работы системы */
    protected var systemTime: Float = 0f
    
    /**
     * Инициализация системы.
     * Вызывается один раз при создании.
     */
    open fun init() {
        // Переопределяется в наследниках
    }
    
    /**
     * Обновление системы.
     * Вызывается каждый кадр.
     * 
     * @param deltaTime Время в секундах с последнего обновления
     */
    open fun update(deltaTime: Float) {
        if (!isEnabled) return
        systemTime += deltaTime
    }
    
    /**
     * Рендеринг системы.
     * Вызывается каждый кадр после update.
     * 
     * @param canvas Canvas для рисования
     */
    open fun render(canvas: Canvas) {
        if (!isEnabled) return
    }
    
    /**
     * Вызывается при паузе игры.
     */
    open fun onPause() {
        // Переопределяется в наследниках
    }
    
    /**
     * Вызывается при возобновлении игры.
     */
    open fun onResume() {
        // Переопределяется в наследниках
    }
    
    /**
     * Вызывается при окончании игры.
     */
    open fun onGameOver() {
        // Переопределяется в наследниках
    }
    
    /**
     * Сброс системы.
     */
    open fun reset() {
        systemTime = 0f
    }
    
    /**
     * Освобождение ресурсов.
     */
    open fun dispose() {
        // Переопределяется в наследниках
    }
}

/**
 * Интерфейс для систем, обрабатывающих коллизии.
 */
interface CollisionHandler {
    /**
     * Обработка коллизии между сущностями.
     * 
     * @param entity1 Первая сущность
     * @param entity2 Вторая сущность
     */
    fun onCollision(entity1: com.endlessrunner.entities.Entity, entity2: com.endlessrunner.entities.Entity)
}

/**
 * Интерфейс для систем, создающих сущности.
 */
interface EntitySpawner {
    /**
     * Спавн сущности.
     * 
     * @param x Позиция X
     * @param y Позиция Y
     * @return Созданная сущность
     */
    fun spawn(x: Float, y: Float): com.endlessrunner.entities.Entity?
}

/**
 * Extension функция для получения всех систем определённого типа.
 */
inline fun <reified T : BaseSystem> List<BaseSystem>.getSystem(): T? {
    return firstOrNull { it is T } as? T
}

/**
 * Extension функция для включения/выключения системы.
 */
inline fun <reified T : BaseSystem> List<BaseSystem>.setSystemEnabled(enabled: Boolean) {
    firstOrNull { it is T }?.isEnabled = enabled
}
