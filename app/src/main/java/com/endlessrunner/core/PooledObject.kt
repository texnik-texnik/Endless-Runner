package com.endlessrunner.core

/**
 * Интерфейс для объектов, поддерживающих пулинг.
 * Реализуется классами, которые будут использоваться в ObjectPool.
 */
interface PooledObject {
    
    /**
     * Вызывается при получении объекта из пула.
     * Здесь должна происходить инициализация объекта.
     */
    fun onAcquire()
    
    /**
     * Вызывается при возврате объекта в пул.
     * Здесь должно происходить освобождение ресурсов и сброс состояния.
     */
    fun onRelease()
    
    /**
     * Проверка, активен ли объект.
     * Активный объект находится в использовании, неактивный - в пуле.
     */
    val isActive: Boolean
    
    /**
     * Уникальный ID объекта в пуле (опционально, для отладки).
     */
    val poolId: Int get() = -1
}

/**
 * Базовая реализация PooledObject.
 * 
 * @param T Тип объекта (должен быть наследником BasePooledObject)
 */
abstract class BasePooledObject<T : BasePooledObject<T>> : PooledObject {
    
    /** Флаг активности объекта */
    final override var isActive: Boolean = false
        protected set
    
    /** Уникальный ID объекта */
    final override var poolId: Int = -1
        internal set
    
    /**
     * Внутренний метод для установки ID пула.
     * Вызывается ObjectPool при создании объекта.
     */
    internal fun setPoolId(id: Int) {
        poolId = id
    }
    
    /**
     * Внутренний метод для установки активности.
     * Вызывается ObjectPool.
     */
    internal fun setActive(active: Boolean) {
        isActive = active
        if (active) {
            onAcquire()
        } else {
            onRelease()
        }
    }
    
    override fun onAcquire() {
        // По умолчанию ничего не делает
        // Переопределяется в наследниках
    }
    
    override fun onRelease() {
        // По умолчанию ничего не делает
        // Переопределяется в наследниках
    }
    
    /**
     * Быстрая проверка и приведение типа.
     */
    @Suppress("UNCHECKED_CAST")
    fun asType(): T = this as T
}

/**
 * Расширение для безопасного получения объекта из пула.
 */
inline fun <reified T : PooledObject> ObjectPool<T>.acquireSafe(): T? {
    @Suppress("UNCHECKED_CAST")
    return acquire() as? T
}

/**
 * Расширение для массового возврата объектов в пул.
 */
fun <T : PooledObject> ObjectPool<T>.releaseAll(objects: Collection<T>) {
    objects.forEach { release(it) }
}

/**
 * Расширение для массового возврата активных объектов.
 */
fun <T : PooledObject> ObjectPool<T>.releaseActive(objects: Collection<T>) {
    objects.filter { it.isActive }.forEach { release(it) }
}
