package com.endlessrunner.collectibles

import android.graphics.Canvas
import com.endlessrunner.components.PhysicsComponent
import com.endlessrunner.components.PositionComponent
import com.endlessrunner.components.RenderComponent
import com.endlessrunner.core.GameConstants
import com.endlessrunner.entities.Entity

/**
 * Базовый класс для собираемых предметов.
 * 
 * @param value Ценность предмета (очки)
 * @param effect Эффект при сборе (опционально)
 * @param width Ширина хитбокса
 * @param height Высота хитбокса
 */
abstract class Collectible(
    open val value: Int = 10,
    open val effect: CollectibleEffect? = null,
    width: Float = GameConstants.COIN_WIDTH,
    height: Float = GameConstants.COIN_HEIGHT
) : Entity(tag = GameConstants.TAG_COIN) {
    
    // ============================================================================
    // КОМПОНЕНТЫ
    // ============================================================================
    
    /** Компонент позиции */
    val positionComponent: PositionComponent by lazy { getComponent() }
    
    /** Компонент рендеринга */
    val renderComponent: RenderComponent by lazy { getComponent() }
    
    /** Компонент физики */
    val physicsComponent: PhysicsComponent by lazy { getComponent() }
    
    // ============================================================================
    // СВОЙСТВА
    // ============================================================================
    
    /** Собран ли предмет */
    var isCollected: Boolean = false
        private set
    
    /** Время до исчезновения (0 = никогда) */
    var despawnTime: Float = 0f
    
    /** Таймер до исчезновения */
    private var despawnTimer: Float = 0f
    
    /** Скорость вращения (градусов/сек) */
    var rotationSpeed: Float = 0f
    
    /** Амплитуда покачивания */
    var bobAmplitude: Float = 0f
    
    /** Частота покачивания (Гц) */
    var bobFrequency: Float = 0f
    
    /** Начальная позиция Y для покачивания */
    private var startY: Float = 0f
    
    // ============================================================================
    // ИНИЦИАЛИЗАЦИЯ
    // ============================================================================
    
    init {
        setupComponents(width, height)
    }
    
    /**
     * Настройка компонентов.
     */
    private fun setupComponents(width: Float, height: Float) {
        // Позиция
        addComponent(PositionComponent())
        
        // Рендеринг
        addComponent(RenderComponent(width = width, height = height))
        
        // Физика (триггер)
        addComponent(
            PhysicsComponent(
                width = width,
                height = height,
                collisionLayer = GameConstants.LAYER_COLLECTIBLE,
                isTrigger = true
            )
        )
    }
    
    // ============================================================================
    // ОБНОВЛЕНИЕ
    // ============================================================================
    
    override fun update(deltaTime: Float) {
        if (isCollected) return
        
        // Обновление таймера исчезновения
        if (despawnTime > 0) {
            despawnTimer -= deltaTime
            if (despawnTimer <= 0) {
                despawn()
                return
            }
        }
        
        // Анимация вращения
        if (rotationSpeed != 0f) {
            val position = positionComponent
            position.rotation += rotationSpeed * deltaTime
        }
        
        // Анимация покачивания
        if (bobAmplitude > 0 && bobFrequency > 0) {
            val position = positionComponent
            if (startY == 0f) {
                startY = position.y
            }
            position.y = startY + kotlin.math.sin(lifetime * bobFrequency * 2 * kotlin.math.PI.toFloat()) * bobAmplitude
        }
        
        // Проверка выхода за границы экрана
        checkOutOfBounds()
        
        super.update(deltaTime)
    }
    
    /**
     * Проверка выхода за границы.
     */
    protected open fun checkOutOfBounds() {
        val position = positionComponent
        if (position.x < -200f) {
            // Предмет ушёл за левую границу - можно удалить
            markForDestroy()
        }
    }
    
    // ============================================================================
    // СБОР
    // ============================================================================
    
    /**
     * Сбор предмета.
     * Вызывается когда игрок касается предмета.
     * 
     * @param collector Сущность, собравшая предмет
     * @return true если предмет успешно собран
     */
    open fun collect(collector: Entity? = null): Boolean {
        if (isCollected) return false
        
        isCollected = true
        
        // Применение эффекта
        effect?.apply(collector)
        
        // Callback
        onCollect(collector)
        
        // Анимация сбора
        onCollectAnimation()
        
        return true
    }
    
    /**
     * Вызывается при сборе предмета.
     * Переопределяется в наследниках.
     * 
     * @param collector Сущность, собравшая предмет
     */
    protected open fun onCollect(collector: Entity?) {
        // Переопределяется в наследниках
    }
    
    /**
     * Анимация сбора.
     */
    protected open fun onCollectAnimation() {
        // Проигрывание анимации сбора
    }
    
    /**
     * Исчезновение предмета.
     */
    protected open fun despawn() {
        markForDestroy()
    }
    
    // ============================================================================
    // УПРАВЛЕНИЕ
    // ============================================================================
    
    /**
     * Установка времени до исчезновения.
     * 
     * @param seconds Время в секундах
     */
    fun setDespawnTime(seconds: Float) {
        despawnTime = seconds
        despawnTimer = seconds
    }
    
    /**
     * Установка анимации вращения.
     * 
     * @param speed Скорость вращения (градусов/сек)
     */
    fun setRotationAnimation(speed: Float = 180f) {
        rotationSpeed = speed
    }
    
    /**
     * Установка анимации покачивания.
     * 
     * @param amplitude Амплитуда
     * @param frequency Частота (Гц)
     */
    fun setBobAnimation(amplitude: Float = 10f, frequency: Float = 2f) {
        bobAmplitude = amplitude
        bobFrequency = frequency
        startY = positionComponent?.y ?: 0f
    }
    
    override fun reset() {
        super.reset()
        isCollected = false
        despawnTimer = 0f
        startY = 0f
    }
    
    override fun toString(): String {
        return "${javaClass.simpleName}(value=$value, collected=$isCollected)"
    }
}

/**
 * Эффект собираемого предмета.
 */
interface CollectibleEffect {
    /**
     * Применение эффекта к сущности.
     * 
     * @param target Сущность, к которой применяется эффект
     */
    fun apply(target: Entity?)
}

/**
 * Extension функция для быстрой настройки collectible.
 */
fun <T : Collectible> T.withAnimation(
    rotationSpeed: Float = 0f,
    bobAmplitude: Float = 0f,
    bobFrequency: Float = 0f
): T = apply {
    this.rotationSpeed = rotationSpeed
    this.bobAmplitude = bobAmplitude
    this.bobFrequency = bobFrequency
}

/**
 * Extension функция для установки времени жизни.
 */
fun <T : Collectible> T.withDespawnTime(seconds: Float): T = apply {
    setDespawnTime(seconds)
}
