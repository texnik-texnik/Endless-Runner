package com.endlessrunner.core

/**
 * Универсальный Object Pool для повторного использования объектов.
 * Уменьшает количество аллокаций памяти и нагрузку на GC.
 * 
 * @param T Тип объектов в пуле (должен реализовывать PooledObject)
 * @param initialSize Начальный размер пула
 * @param maxSize Максимальный размер пула (0 = без ограничений)
 * @param factory Фабрика для создания новых объектов
 */
class ObjectPool<T : PooledObject>(
    private val initialSize: Int = 10,
    private val maxSize: Int = 0,
    private val factory: (Int) -> T
) {
    
    /** Пул неактивных объектов */
    private val pool: ArrayDeque<T> = ArrayDeque(initialSize)
    
    /** Все созданные объекты (для статистики и управления) */
    private val allObjects: MutableList<T> = mutableListOf()
    
    /** Счётчик созданных объектов */
    private var createdCount: Int = 0
    
    /** Счётчик активных объектов */
    var activeCount: Int = 0
        private set
    
    /** Счётчик объектов в пуле */
    val pooledCount: Int
        get() = pool.size
    
    /** Общее количество объектов */
    val totalCount: Int
        get() = allObjects.size
    
    /** Статистика использования пула */
    val stats: PoolStats
        get() = PoolStats(
            activeCount = activeCount,
            pooledCount = pooledCount,
            totalCount = totalCount,
            createdCount = createdCount,
            maxSize = maxSize
        )
    
    init {
        // Предварительное создание объектов
        expand(initialSize)
    }
    
    /**
     * Получение объекта из пула.
     * Если пул пуст, создаётся новый объект (если не достигнут maxSize).
     * 
     * @return Объект из пула или новый объект
     */
    fun acquire(): T {
        val obj: T
        
        if (pool.isNotEmpty()) {
            // Берём из пула
            obj = pool.removeLast()
        } else {
            // Создаём новый, если можно
            if (maxSize == 0 || totalCount < maxSize) {
                obj = createNewObject()
            } else {
                // Пул достиг максимума, возвращаем null или бросаем исключение
                // Для гибкости возвращаем новый объект с предупреждением
                obj = createNewObject()
            }
        }
        
        // Активируем объект
        @Suppress("UNCHECKED_CAST")
        (obj as BasePooledObject<*>).setActive(true)
        activeCount++
        
        return obj
    }
    
    /**
     * Возврат объекта в пул.
     * 
     * @param obj Объект для возврата
     * @return true если объект успешно возвращён
     */
    fun release(obj: T): Boolean {
        // Проверяем, что объект активен и принадлежит этому пулу
        if (!obj.isActive || !allObjects.contains(obj)) {
            return false
        }
        
        // Деактивируем объект
        @Suppress("UNCHECKED_CAST")
        (obj as BasePooledObject<*>).setActive(false)
        activeCount--
        
        // Возвращаем в пул
        pool.addLast(obj)
        
        return true
    }
    
    /**
     * Расширение пула на указанное количество объектов.
     * 
     * @param count Количество объектов для добавления
     * @return Количество фактически созданных объектов
     */
    fun expand(count: Int = 1): Int {
        var created = 0
        
        repeat(count) {
            if (maxSize > 0 && totalCount >= maxSize) {
                return created
            }
            
            val obj = createNewObject()
            @Suppress("UNCHECKED_CAST")
            (obj as BasePooledObject<*>).setActive(false)
            pool.addLast(obj)
            created++
        }
        
        return created
    }
    
    /**
     * Расширение пула до указанного размера.
     * 
     * @param targetSize Целевой размер пула
     * @return Количество фактически созданных объектов
     */
    fun expandTo(targetSize: Int): Int {
        if (targetSize <= totalCount) return 0
        return expand(targetSize - totalCount)
    }
    
    /**
     * Очистка пула.
     * Все объекты деактивируются и возвращаются в пул.
     */
    fun clear() {
        allObjects.forEach { obj ->
            if (obj.isActive) {
                @Suppress("UNCHECKED_CAST")
                (obj as BasePooledObject<*>).setActive(false)
            }
            if (!pool.contains(obj)) {
                pool.addLast(obj)
            }
        }
        activeCount = 0
    }
    
    /**
     * Полное освобождение пула.
     * Все объекты удаляются.
     */
    fun dispose() {
        pool.clear()
        allObjects.clear()
        createdCount = 0
        activeCount = 0
    }
    
    /**
     * Принудительное создание нового объекта.
     */
    private fun createNewObject(): T {
        val obj = factory(createdCount)
        @Suppress("UNCHECKED_CAST")
        (obj as BasePooledObject<*>).setPoolId(createdCount)
        allObjects.add(obj)
        createdCount++
        return obj
    }
    
    /**
     * Получение всех активных объектов.
     */
    fun getActiveObjects(): List<T> = allObjects.filter { it.isActive }
    
    /**
     * Получение всех объектов пула.
     */
    fun getAllObjects(): List<T> = allObjects.toList()
    
    /**
     * Проверка, принадлежит ли объект этому пулу.
     */
    fun contains(obj: T): Boolean = allObjects.contains(obj)
    
    /**
     * Data class для статистики пула.
     */
    data class PoolStats(
        val activeCount: Int,
        val pooledCount: Int,
        val totalCount: Int,
        val createdCount: Int,
        val maxSize: Int
    ) {
        /** Процент использования пула */
        val utilizationRate: Float
            get() = if (totalCount == 0) 0f else activeCount.toFloat() / totalCount
        
        /** Доступно места до максимума */
        val availableSpace: Int
            get() = if (maxSize == 0) Int.MAX_VALUE else maxSize - totalCount
    }
}

/**
 * Builder для удобного создания ObjectPool.
 */
class ObjectPoolBuilder<T : PooledObject> {
    private var initialSize: Int = 10
    private var maxSize: Int = 0
    private lateinit var factory: (Int) -> T
    
    fun initialSize(size: Int) = apply { initialSize = size }
    fun maxSize(size: Int) = apply { maxSize = size }
    fun factory(f: (Int) -> T) = apply { factory = f }
    
    fun build(): ObjectPool<T> {
        require(::factory.isInitialized) { "Factory must be specified" }
        return ObjectPool(initialSize, maxSize, factory)
    }
}

/**
 * DSL функция для создания ObjectPool.
 */
inline fun <reified T : PooledObject> objectPool(
    initialSize: Int = 10,
    maxSize: Int = 0,
    noinline factory: (Int) -> T
): ObjectPool<T> = ObjectPool(initialSize, maxSize, factory)

/**
 * DSL функция для создания ObjectPool с builder.
 */
inline fun <reified T : PooledObject> buildObjectPool(
    block: ObjectPoolBuilder<T>.() -> Unit
): ObjectPool<T> = ObjectPoolBuilder<T>().apply(block).build()
