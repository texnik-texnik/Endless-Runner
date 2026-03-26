package com.endlessrunner.components

import android.graphics.Canvas
import com.endlessrunner.entities.Entity

/**
 * Базовый абстрактный класс компонента.
 * Компоненты определяют поведение и свойства сущностей.
 * 
 * Компоненты следуют паттерну Component из Entity-Component System.
 */
abstract class Component {
    
    /** Ссылка на родительскую сущность */
    var entity: Entity? = null
        internal set
    
    /** Флаг включения компонента */
    var isEnabled: Boolean = true
    
    /** Флаг видимости компонента (для рендеринга) */
    var isVisible: Boolean = true
    
    /** Приоритет обновления (меньше = раньше) */
    var updatePriority: Int = 0
    
    /** Приоритет рендеринга (меньше = раньше) */
    var renderPriority: Int = 0
    
    /** Время жизни компонента (секунды) */
    var lifetime: Float = 0f
        protected set
    
    /** Флаг инициализации */
    private var isInitialized: Boolean = false
    
    // ============================================================================
    // ЖИЗНЕННЫЙ ЦИКЛ
    // ============================================================================
    
    /**
     * Вызывается при добавлении компонента к сущности.
     * Здесь происходит инициализация.
     */
    open fun onAdd() {
        isInitialized = true
    }
    
    /**
     * Вызывается при удалении компонента из сущности.
     * Здесь происходит очистка ресурсов.
     */
    open fun onRemove() {
        isInitialized = false
        entity = null
    }
    
    /**
     * Вызывается при активации сущности.
     */
    open fun onEntityActivate() {
        lifetime = 0f
    }
    
    /**
     * Вызывается при деактивации сущности.
     */
    open fun onEntityDeactivate() {
        lifetime = 0f
    }
    
    /**
     * Обновление компонента.
     * Вызывается каждый кадр сущностью.
     * 
     * @param deltaTime Время в секундах с последнего обновления
     */
    open fun onUpdate(deltaTime: Float) {
        lifetime += deltaTime
    }
    
    /**
     * Рендеринг компонента.
     * Вызывается каждый кадр сущностью.
     * 
     * @param canvas Canvas для рисования
     */
    open fun onRender(canvas: Canvas) {
        // По умолчанию ничего не рендерит
    }
    
    // ============================================================================
    // УТИЛИТЫ
    // ============================================================================
    
    /**
     * Получение родительской сущности с проверкой.
     * 
     * @throws IllegalStateException если сущность не установлена
     */
    fun requireEntity(): Entity {
        return entity ?: throw IllegalStateException(
            "Компонент ${javaClass.simpleName} не прикреплён к сущности"
        )
    }
    
    /**
     * Безопасное выполнение действия над сущностью.
     */
    inline fun withEntity(action: (Entity) -> Unit) {
        entity?.let(action)
    }
    
    /**
     * Проверка, прикреплён ли компонент к сущности.
     */
    fun isAttached(): Boolean = entity != null
    
    /**
     * Проверка, активна ли сущность.
     */
    fun isEntityActive(): Boolean = entity?.isActive == true
    
    /**
     * Отключение компонента.
     */
    fun disable() {
        isEnabled = false
    }
    
    /**
     * Включение компонента.
     */
    fun enable() {
        isEnabled = true
    }
    
    /**
     * Переключение видимости.
     */
    fun toggleVisibility() {
        isVisible = !isVisible
    }
    
    /**
     * Сброс компонента.
     */
    open fun reset() {
        lifetime = 0f
        isEnabled = true
        isVisible = true
    }
    
    override fun toString(): String {
        return "${javaClass.simpleName}(enabled=$isEnabled, visible=$isVisible, lifetime=${String.format("%.2f", lifetime)}s)"
    }
}

/**
 * Компонент с фиксированным обновлением.
 * Использует фиксированный шаг времени вместо deltaTime.
 */
abstract class FixedUpdateComponent : Component() {
    
    /** Накопитель времени для фиксированного шага */
    private var accumulator: Float = 0f
    
    /** Фиксированный шаг времени */
    protected open val fixedTimeStep: Float = 1f / 60f
    
    final override fun onUpdate(deltaTime: Float) {
        accumulator += deltaTime
        
        while (accumulator >= fixedTimeStep) {
            onFixedUpdate(fixedTimeStep)
            accumulator -= fixedTimeStep
        }
    }
    
    /**
     * Обновление с фиксированным шагом.
     * Переопределяется в наследниках.
     */
    abstract fun onFixedUpdate(deltaTime: Float)
}

/**
 * Компонент с задержкой активации.
 */
abstract class DelayedComponent(
    /** Задержка перед активацией (секунды) */
    private val delaySeconds: Float = 0f
) : Component() {
    
    private var isDelayElapsed: Boolean = delaySeconds <= 0f
    
    final override fun onEntityActivate() {
        super.onEntityActivate()
        isDelayElapsed = delaySeconds <= 0f
    }
    
    final override fun onUpdate(deltaTime: Float) {
        super.onUpdate(deltaTime)
        
        if (!isDelayElapsed) {
            if (lifetime >= delaySeconds) {
                isDelayElapsed = true
                onDelayElapsed()
            }
            return
        }
        
        onDelayedUpdate(deltaTime)
    }
    
    /**
     * Вызывается когда задержка истекла.
     */
    open fun onDelayElapsed() {}
    
    /**
     * Обновление после истечения задержки.
     */
    open fun onDelayedUpdate(deltaTime: Float) {}
}

/**
 * Компонент с ограниченным временем жизни.
 */
abstract class LifetimeComponent(
    /** Время жизни (секунды) */
    private val maxLifetime: Float = 1f,
    
    /** Действие при истечении времени жизни */
    private val onExpired: () -> Unit = {}
) : Component() {
    
    private var isExpired: Boolean = false
    
    final override fun onUpdate(deltaTime: Float) {
        super.onUpdate(deltaTime)
        
        if (!isExpired && lifetime >= maxLifetime) {
            isExpired = true
            onExpired()
            onLifeExpired()
        }
        
        if (!isExpired) {
            onLifetimeUpdate(deltaTime)
        }
    }
    
    /**
     * Проверка, истекло ли время жизни.
     */
    fun isLifeExpired(): Boolean = isExpired
    
    /**
     * Процент оставшегося времени жизни (0-1).
     */
    fun getLifeRemainingPercent(): Float {
        return 1f - (lifetime / maxLifetime).coerceIn(0f, 1f)
    }
    
    /**
     * Вызывается при истечении времени жизни.
     */
    open fun onLifeExpired() {}
    
    /**
     * Обновление пока время жизни не истекло.
     */
    open fun onLifetimeUpdate(deltaTime: Float) {}
}

/**
 * Extension функция для получения всех компонентов определённого типа.
 */
inline fun <reified T : Component> Entity.getComponentsOfType(): List<T> {
    return getAllComponents().filterIsInstance<T>()
}

/**
 * Extension функция для удаления всех компонентов определённого типа.
 */
inline fun <reified T : Component> Entity.removeComponentsOfType(): Int {
    var count = 0
    getAllComponents().filterIsInstance<T>().forEach {
        if (removeComponent(it)) count++
    }
    return count
}
