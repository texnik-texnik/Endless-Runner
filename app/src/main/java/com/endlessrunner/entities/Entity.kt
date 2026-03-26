package com.endlessrunner.entities

import android.graphics.Canvas
import android.graphics.RectF
import com.endlessrunner.core.PooledObject
import com.endlessrunner.components.Component
import kotlin.reflect.KClass

/**
 * Базовый класс игровой сущности.
 * Реализует паттерн Entity-Component.
 * 
 * Сущность содержит компоненты, которые определяют её поведение и свойства.
 * Компоненты обновляются и рендерятся через сущность.
 */
abstract class Entity(
    /** Уникальный ID сущности */
    val id: Long = generateId(),
    
    /** Тег для группировки и поиска */
    val tag: String = ""
) : PooledObject {
    
    companion object {
        private var idCounter: Long = 0L
        
        @Synchronized
        private fun generateId(): Long = ++idCounter
    }
    
    /** Флаг активности сущности */
    final override var isActive: Boolean = false
        protected set
    
    /** Флаг готовности к удалению */
    var isPendingDestroy: Boolean = false
        protected set
    
    /** Компоненты сущности (Map для быстрого доступа по типу) */
    private val components: MutableMap<KClass<out Component>, Component> = mutableMapOf()
    
    /** Список компонентов для итерации (без аллокаций) */
    private val componentList: MutableList<Component> = mutableListOf()
    
    /** Родительская сущность (для иерархии) */
    var parent: Entity? = null
        protected set
    
    /** Дочерние сущности */
    private val children: MutableList<Entity> = mutableListOf()
    
    /** Время жизни сущности (секунды) */
    var lifetime: Float = 0f
        protected set
    
    /** Ссылка на EntityManager */
    var entityManager: EntityManager? = null
        internal set
    
    override val poolId: Int = -1
    
    // ============================================================================
    // УПРАВЛЕНИЕ КОМПОНЕНТАМИ
    // ============================================================================
    
    /**
     * Добавление компонента к сущности.
     * 
     * @param T Тип компонента
     * @param component Экземпляр компонента
     * @return Эта сущность для цепочки вызовов
     */
    fun <T : Component> addComponent(component: T): Entity {
        val kClass = component::class
        
        // Удаляем старый компонент того же типа из списка
        components[kClass]?.let { oldComponent ->
            componentList.remove(oldComponent)
            oldComponent.onRemove()
        }
        
        // Добавляем новый компонент
        components[kClass] = component
        componentList.add(component)
        
        // Устанавливаем ссылку на сущность
        component.entity = this
        component.onAdd()
        
        return this
    }
    
    /**
     * Получение компонента по типу.
     * 
     * @param T Тип компонента
     * @return Компонент или null если не найден
     */
    inline fun <reified T : Component> getComponent(): T? {
        @Suppress("UNCHECKED_CAST")
        return components[T::class] as? T
    }
    
    /**
     * Получение или создание компонента по типу.
     * 
     * @param T Тип компонента
     * @param factory Фабрика для создания компонента если не найден
     * @return Компонент
     */
    inline fun <reified T : Component> getOrCreateComponent(factory: () -> T): T {
        return getComponent<T>() ?: factory().also { addComponent(it) }
    }
    
    /**
     * Проверка наличия компонента.
     * 
     * @param T Тип компонента
     * @return true если компонент есть
     */
    inline fun <reified T : Component> hasComponent(): Boolean {
        return components.contains(T::class)
    }
    
    /**
     * Удаление компонента по типу.
     * 
     * @param T Тип компонента
     * @return true если компонент был удалён
     */
    inline fun <reified T : Component> removeComponent(): Boolean {
        val component = components.remove(T::class)
        if (component != null) {
            componentList.remove(component)
            component.onRemove()
            return true
        }
        return false
    }
    
    /**
     * Удаление конкретного компонента.
     */
    fun removeComponent(component: Component): Boolean {
        val removed = components.remove(component::class)
        if (removed != null) {
            componentList.remove(component)
            component.onRemove()
            return true
        }
        return false
    }
    
    /**
     * Получение всех компонентов.
     */
    fun getAllComponents(): List<Component> = componentList.toList()
    
    /**
     * Получение количества компонентов.
     */
    fun getComponentCount(): Int = components.size
    
    // ============================================================================
    // УПРАВЛЕНИЕ ИЕРАРХИЕЙ
    // ============================================================================
    
    /**
     * Добавление дочерней сущности.
     */
    fun addChild(child: Entity) {
        if (!children.contains(child)) {
            children.add(child)
            child.parent = this
        }
    }
    
    /**
     * Удаление дочерней сущности.
     */
    fun removeChild(child: Entity) {
        children.remove(child)
        child.parent = null
    }
    
    /**
     * Получение дочерних сущностей.
     */
    fun getChildren(): List<Entity> = children.toList()
    
    // ============================================================================
    // ОБНОВЛЕНИЕ И РЕНДЕРИНГ
    // ============================================================================
    
    /**
     * Обновление сущности и всех компонентов.
     * Вызывается каждый кадр.
     * 
     * @param deltaTime Время в секундах с последнего обновления
     */
    open fun update(deltaTime: Float) {
        if (!isActive) return
        
        lifetime += deltaTime
        
        // Обновление компонентов
        for (component in componentList) {
            if (component.isEnabled) {
                component.onUpdate(deltaTime)
            }
        }
        
        // Обновление дочерних сущностей
        for (child in children) {
            if (child.isActive) {
                child.update(deltaTime)
            }
        }
    }
    
    /**
     * Рендеринг сущности и всех компонентов.
     * 
     * @param canvas Canvas для рисования
     */
    open fun render(canvas: Canvas) {
        if (!isActive) return
        
        // Рендеринг компонентов
        for (component in componentList) {
            if (component.isVisible) {
                component.onRender(canvas)
            }
        }
        
        // Рендеринг дочерних сущностей
        for (child in children) {
            if (child.isActive) {
                child.render(canvas)
            }
        }
    }
    
    // ============================================================================
    // ЖИЗНЕННЫЙ ЦИКЛ
    // ============================================================================
    
    /**
     * Вызывается при активации сущности.
     */
    open fun onActivate() {
        isActive = true
        lifetime = 0f
        components.values.forEach { it.onEntityActivate() }
    }
    
    /**
     * Вызывается при деактивации сущности.
     */
    open fun onDeactivate() {
        isActive = false
        components.values.forEach { it.onEntityDeactivate() }
    }
    
    /**
     * Пометка сущности на удаление.
     */
    fun markForDestroy() {
        isPendingDestroy = true
    }
    
    /**
     * Сброс состояния сущности.
     */
    open fun reset() {
        lifetime = 0f
        isPendingDestroy = false
        children.clear()
        parent = null
    }
    
    // ============================================================================
    // POOLED OBJECT
    // ============================================================================
    
    override fun onAcquire() {
        onActivate()
    }
    
    override fun onRelease() {
        onDeactivate()
        reset()
    }
    
    // ============================================================================
    // УТИЛИТЫ
    // ============================================================================
    
    /**
     * Получение bounds сущности (если есть PhysicsComponent).
     */
    fun getBounds(): RectF? {
        return getComponent<com.endlessrunner.components.PhysicsComponent>()?.getBounds()
    }
    
    /**
     * Проверка столкновения с другой сущностью.
     */
    fun overlaps(other: Entity): Boolean {
        val thisBounds = getBounds() ?: return false
        val otherBounds = other.getBounds() ?: return false
        
        return RectF.intersects(thisBounds, otherBounds)
    }
    
    override fun toString(): String {
        return "Entity(id=$id, tag=$tag, active=$isActive, components=${components.size})"
    }
}

/**
 * Extension функция для получения компонента с проверкой.
 */
inline fun <reified T : Component> Entity.requireComponent(): T {
    return getComponent<T>() ?: throw IllegalStateException(
        "Компонент ${T::class.simpleName} не найден в сущности $this"
    )
}

/**
 * Extension функция для безопасного выполнения действия над компонентом.
 */
inline fun <reified T : Component> Entity.withComponent(action: (T) -> Unit) {
    getComponent<T>()?.let(action)
}

/**
 * Extension функция для проверки наличия любого из компонентов.
 */
fun Entity.hasAnyComponents(vararg componentTypes: KClass<out Component>): Boolean {
    return componentTypes.any { components.contains(it) }
}

/**
 * Extension функция для проверки наличия всех компонентов.
 */
fun Entity.hasAllComponents(vararg componentTypes: KClass<out Component>): Boolean {
    return componentTypes.all { components.contains(it) }
}
