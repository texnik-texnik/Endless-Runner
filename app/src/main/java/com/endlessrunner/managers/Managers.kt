package com.endlessrunner.managers

import com.endlessrunner.entities.interfaces.IEntity
import com.endlessrunner.entities.interfaces.IUpdatable
import com.endlessrunner.entities.interfaces.Vector2

/**
 * Менеджер сущностей.
 * 
 * Управляет созданием, обновлением и удалением игровых сущностей.
 * Реализует паттерн Object Pool для оптимизации производительности.
 */
class EntityManager : IUpdatable {
    /**
     * Все активные сущности.
     */
    private val entities = mutableListOf<IEntity>()

    /**
     * Пул неактивных сущностей для повторного использования.
     */
    private val entityPool = mutableListOf<IEntity>()

    /**
     * Счётчик для генерации уникальных ID.
     */
    private var nextId: Long = 0

    /**
     * Максимальный размер пула.
     */
    private val maxPoolSize = 100

    /**
     * Получение следующей сущности из пула или создание новой.
     */
    fun acquire(): IEntity? {
        return if (entityPool.isNotEmpty()) {
            entityPool.removeAt(entityPool.lastIndex).apply {
                activate()
            }
        } else {
            null  // Возвращаем null, создание сущности делегируется фабрике
        }
    }

    /**
     * Добавление сущности в менеджер.
     */
    fun addEntity(entity: IEntity) {
        if (!entities.contains(entity)) {
            entities.add(entity)
        }
    }

    /**
     * Удаление сущности из менеджера.
     * Сущность перемещается в пул для повторного использования.
     */
    fun removeEntity(entity: IEntity) {
        entities.remove(entity)
        
        if (entityPool.size < maxPoolSize) {
            entity.deactivate()
            entityPool.add(entity)
        } else {
            entity.destroy()
        }
    }

    /**
     * Удаление сущности без возврата в пул.
     */
    fun destroyEntity(entity: IEntity) {
        entities.remove(entity)
        entityPool.remove(entity)
        entity.destroy()
    }

    /**
     * Получение сущности по ID.
     */
    fun getEntityById(id: Long): IEntity? = entities.find { it.id == id }

    /**
     * Получение всех сущностей определённого типа.
     */
    fun <T : IEntity> getEntitiesByType(type: Class<T>): List<T> {
        return entities.filter { type.isInstance(it) } as List<T>
    }

    /**
     * Получение всех сущностей в радиусе.
     */
    fun getEntitiesInRadius(position: Vector2, radius: Float): List<IEntity> {
        val radiusSquared = radius * radius
        return entities.filter { entity ->
            entity.isActive && entity.position.distanceSquaredTo(position) <= radiusSquared
        }
    }

    /**
     * Проверка столкновений между сущностями.
     */
    fun checkCollisions(): List<Pair<IEntity, IEntity>> {
        val collisions = mutableListOf<Pair<IEntity, IEntity>>()
        
        for (i in entities.indices) {
            for (j in (i + 1) until entities.size) {
                val a = entities[i]
                val b = entities[j]
                
                if (a.isActive && b.isActive && a.collidesWith(b)) {
                    collisions.add(Pair(a, b))
                }
            }
        }
        
        return collisions
    }

    override fun update(deltaTime: Float) {
        // Обновление всех активных сущностей
        entities.filter { it.isActive }.forEach { entity ->
            if (entity is IUpdatable) {
                entity.update(deltaTime)
            }
        }
    }

    /**
     * Очистка всех сущностей.
     */
    fun clear() {
        entities.forEach { it.destroy() }
        entities.clear()
        entityPool.forEach { it.destroy() }
        entityPool.clear()
        nextId = 0
    }

    /**
     * Количество активных сущностей.
     */
    fun activeEntityCount(): Int = entities.count { it.isActive }

    /**
     * Количество сущностей в пуле.
     */
    fun pooledEntityCount(): Int = entityPool.size

    /**
     * Генерация уникального ID.
     */
    fun generateId(): Long = nextId++
}

/**
 * Менеджер сцен.
 * Управляет загрузкой и переключением между игровыми сценами.
 */
class SceneManager {
    /**
     * Текущая активная сцена.
     */
    private var currentScene: GameScene? = null

    /**
     * Загрузка сцены.
     */
    fun loadScene(scene: GameScene) {
        currentScene?.unload()
        currentScene = scene
        scene.load()
    }

    /**
     * Получение текущей сцены.
     */
    fun getCurrentScene(): GameScene? = currentScene

    /**
     * Перезагрузка текущей сцены.
     */
    fun reloadCurrentScene() {
        currentScene?.let { scene ->
            currentScene = null
            loadScene(scene)
        }
    }

    /**
     * Выгрузка текущей сцены.
     */
    fun unloadCurrentScene() {
        currentScene?.unload()
        currentScene = null
    }
}

/**
 * Базовый класс для игровой сцены.
 */
abstract class GameScene {
    /**
     * Название сцены.
     */
    abstract val name: String

    /**
     * Загрузка сцены.
     */
    open fun load() {}

    /**
     * Выгрузка сцены.
     */
    open fun unload() {}

    /**
     * Обновление сцены.
     */
    open fun update(deltaTime: Float) {}

    /**
     * Пауза сцены.
     */
    open fun onPause() {}

    /**
     * Возобновление сцены.
     */
    open fun onResume() {}
}
