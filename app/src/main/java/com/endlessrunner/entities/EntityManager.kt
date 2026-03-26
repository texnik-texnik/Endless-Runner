package com.endlessrunner.entities

import android.util.Log
import com.endlessrunner.core.GameConstants
import com.endlessrunner.core.ObjectPool
import com.endlessrunner.core.PooledObject
import kotlin.reflect.KClass

/**
 * Менеджер сущностей.
 * Singleton для создания, обновления и удаления сущностей.
 * Использует Object Pool для производительности.
 */
class EntityManager private constructor() {
    
    companion object {
        private const val TAG = "EntityManager"
        
        @Volatile
        private var instance: EntityManager? = null
        
        /**
         * Получение экземпляра EntityManager.
         */
        fun getInstance(): EntityManager {
            return instance ?: synchronized(this) {
                instance ?: EntityManager().also { instance = it }
            }
        }
        
        /**
         * Сброс экземпляра (для тестов).
         */
        fun resetInstance() {
            instance?.dispose()
            instance = null
        }
    }
    
    /** Пул для сущностей */
    private val entityPool: ObjectPool<Entity> = ObjectPool(
        initialSize = 100,
        maxSize = 500,
        factory = { Entity(id = it.toLong()) }
    )
    
    /** Все активные сущности */
    private val activeEntities: MutableList<Entity> = mutableListOf()
    
    /** Сущности по тегам (для быстрого поиска) */
    private val entitiesByTag: MutableMap<String, MutableList<Entity>> = mutableMapOf()
    
    /** Сущности по типам (для систем) */
    private val entitiesByType: MutableMap<KClass<out Entity>, MutableList<Entity>> = mutableMapOf()
    
    /** Очередь на удаление (для безопасного удаления во время обновления) */
    private val destroyQueue: MutableList<Entity> = mutableListOf()
    
    /** Очередь на добавление (для безопасного добавления во время обновления) */
    private val createQueue: MutableList<Entity> = mutableListOf()
    
    /** Флаг блокировки изменений (во время обновления) */
    private var isUpdating: Boolean = false
    
    /** Статистика */
    var activeCount: Int = 0
        private set
    
    /**
     * Создание новой сущности.
     * 
     * @param tag Тег сущности
     * @param initializer Блок инициализации сущности
     * @return Новая сущность
     */
    fun create(
        tag: String = "",
        initializer: Entity.() -> Unit = {}
    ): Entity {
        val entity = entityPool.acquire()
        entity.tag = tag
        entity.entityManager = this
        entity.initializer()
        
        // Добавляем в очередь на создание
        if (isUpdating) {
            createQueue.add(entity)
        } else {
            addEntity(entity)
        }
        
        return entity
    }
    
    /**
     * Создание сущности определённого типа.
     * 
     * @param T Тип сущности
     * @param tag Тег сущности
     * @param initializer Блок инициализации
     * @return Новая сущность типа T
     */
    inline fun <reified T : Entity> createTyped(
        tag: String = "",
        noinline initializer: T.() -> Unit = {}
    ): T {
        @Suppress("UNCHECKED_CAST")
        val entity = create(tag) as T
        entity.initializer()
        return entity
    }
    
    /**
     * Уничтожение сущности.
     * 
     * @param entity Сущность для уничтожения
     * @param immediate Немедленное удаление (не рекомендуется во время update)
     */
    fun destroy(entity: Entity, immediate: Boolean = false) {
        if (!entity.isActive) return
        
        entity.markForDestroy()
        
        if (immediate && !isUpdating) {
            removeEntity(entity)
        } else {
            destroyQueue.add(entity)
        }
    }
    
    /**
     * Уничтожение всех сущностей с указанным тегом.
     */
    fun destroyByTag(tag: String) {
        getEntitiesByTag(tag).forEach { destroy(it) }
    }
    
    /**
     * Уничтожение всех сущностей указанного типа.
     */
    inline fun <reified T : Entity> destroyByType() {
        getEntitiesByType<T>().forEach { destroy(it) }
    }
    
    /**
     * Уничтожение всех сущностей.
     */
    fun destroyAll() {
        activeEntities.forEach { it.markForDestroy() }
        destroyQueue.addAll(activeEntities)
    }
    
    /**
     * Обновление всех сущностей.
     * Вызывается из игрового цикла.
     * 
     * @param deltaTime Время в секундах
     */
    fun update(deltaTime: Float) {
        isUpdating = true
        
        try {
            // Обработка очереди создания
            processCreateQueue()
            
            // Обновление сущностей
            for (entity in activeEntities) {
                if (entity.isActive && !entity.isPendingDestroy) {
                    entity.update(deltaTime)
                }
            }
            
            // Обработка очереди уничтожения
            processDestroyQueue()
        } finally {
            isUpdating = false
        }
    }
    
    /**
     * Рендеринг всех сущностей.
     *
     * @param canvas Canvas для рисования
     */
    fun render(canvas: Canvas) {
        // Сортировка по приоритету рендеринга (опционально)
        for (entity in activeEntities) {
            if (entity.isActive && !entity.isPendingDestroy) {
                entity.render(canvas)
            }
        }
    }
    
    /**
     * Получение всех активных сущностей.
     */
    fun getAll(): List<Entity> = activeEntities.toList()
    
    /**
     * Получение сущностей по тегу.
     */
    fun getEntitiesByTag(tag: String): List<Entity> {
        return entitiesByTag[tag]?.toList() ?: emptyList()
    }
    
    /**
     * Получение сущностей по типу.
     */
    inline fun <reified T : Entity> getEntitiesByType(): List<T> {
        @Suppress("UNCHECKED_CAST")
        return entitiesByType[T::class]?.toList() as? List<T> ?: emptyList()
    }
    
    /**
     * Получение первой сущности по тегу.
     */
    fun getFirstByTag(tag: String): Entity? {
        return entitiesByTag[tag]?.firstOrNull()
    }
    
    /**
     * Получение первой сущности по типу.
     */
    inline fun <reified T : Entity> getFirstByType(): T? {
        return getEntitiesByType<T>().firstOrNull()
    }
    
    /**
     * Поиск сущности по ID.
     */
    fun getById(id: Long): Entity? {
        return activeEntities.find { it.id == id }
    }
    
    /**
     * Проверка наличия сущности.
     */
    fun contains(entity: Entity): Boolean = activeEntities.contains(entity)
    
    /**
     * Получение статистики.
     */
    fun getStats(): EntityStats {
        return EntityStats(
            activeCount = activeCount,
            pooledCount = entityPool.pooledCount,
            totalCount = entityPool.totalCount,
            destroyQueueSize = destroyQueue.size,
            createQueueSize = createQueue.size,
            tagCount = entitiesByTag.size,
            typeCount = entitiesByType.size
        )
    }
    
    /**
     * Очистка всех сущностей.
     */
    fun clear() {
        destroyAll()
        processDestroyQueue()
    }
    
    /**
     * Освобождение ресурсов.
     */
    fun dispose() {
        clear()
        entityPool.dispose()
        entitiesByTag.clear()
        entitiesByType.clear()
        activeCount = 0
    }
    
    // ============================================================================
    // ВНУТРЕННИЕ МЕТОДЫ
    // ============================================================================
    
    /**
     * Добавление сущности в менеджмент.
     */
    private fun addEntity(entity: Entity) {
        if (activeEntities.contains(entity)) return
        
        activeEntities.add(entity)
        entity.onActivate()
        activeCount++
        
        // Индексация по тегу
        if (entity.tag.isNotEmpty()) {
            entitiesByTag.getOrPut(entity.tag) { mutableListOf() }.add(entity)
        }
        
        // Индексация по типу
        val kClass = entity::class
        entitiesByType.getOrPut(kClass) { mutableListOf() }.add(entity)
        
        Log.d(TAG, "Сущность создана: ${entity.tag} (id=${entity.id})")
    }
    
    /**
     * Удаление сущности из менеджмента.
     */
    private fun removeEntity(entity: Entity) {
        if (!activeEntities.contains(entity)) return
        
        activeEntities.remove(entity)
        entity.onDeactivate()
        activeCount--
        
        // Удаление из индексов
        if (entity.tag.isNotEmpty()) {
            entitiesByTag[entity.tag]?.remove(entity)
        }
        entitiesByType[entity::class]?.remove(entity)
        
        // Возврат в пул
        entityPool.release(entity)
        
        Log.d(TAG, "Сущность удалена: ${entity.tag} (id=${entity.id})")
    }
    
    /**
     * Обработка очереди создания.
     */
    private fun processCreateQueue() {
        for (entity in createQueue) {
            addEntity(entity)
        }
        createQueue.clear()
    }
    
    /**
     * Обработка очереди уничтожения.
     */
    private fun processDestroyQueue() {
        for (entity in destroyQueue) {
            if (entity.isPendingDestroy) {
                removeEntity(entity)
            }
        }
        destroyQueue.clear()
    }
    
    /**
     * Data class для статистики.
     */
    data class EntityStats(
        val activeCount: Int,
        val pooledCount: Int,
        val totalCount: Int,
        val destroyQueueSize: Int,
        val createQueueSize: Int,
        val tagCount: Int,
        val typeCount: Int
    ) {
        val poolUtilization: Float
            get() = if (totalCount == 0) 0f else activeCount.toFloat() / totalCount
    }
}

/**
 * Extension property для удобного доступа.
 */
val entityManager: EntityManager
    get() = EntityManager.getInstance()

/**
 * Extension функция для создания сущности.
 */
inline fun createEntity(
    tag: String = "",
    noinline initializer: Entity.() -> Unit = {}
): Entity = EntityManager.getInstance().create(tag, initializer)

/**
 * Extension функция для уничтожения сущности.
 */
fun Entity.destroy(immediate: Boolean = false) {
    EntityManager.getInstance().destroy(this, immediate)
}

/**
 * Extension функция для получения всех сущностей.
 */
fun getAllEntities(): List<Entity> = EntityManager.getInstance().getAll()

/**
 * Extension функция для получения всех врагов.
 */
fun EntityManager.getAllEnemies(): List<com.endlessrunner.enemies.Enemy> {
    return getEntitiesByTag(com.endlessrunner.core.GameConstants.TAG_ENEMY)
        .filterIsInstance<com.endlessrunner.enemies.Enemy>()
        .filter { it.isActive && !it.isDestroyed }
}
