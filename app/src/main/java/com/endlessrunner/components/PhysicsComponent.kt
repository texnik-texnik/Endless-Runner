package com.endlessrunner.components

import android.graphics.RectF
import com.endlessrunner.core.GameConstants

/**
 * Компонент физики и коллизий.
 * Определяет хитбокс сущности и слой коллизий.
 * 
 * @param width Ширина хитбокса
 * @param height Высота хитбокса
 * @param collisionLayer Битовая маска слоя коллизий
 * @param isTrigger Является ли триггером (не блокирует движение)
 */
open class PhysicsComponent(
    var width: Float = 64f,
    var height: Float = 64f,
    var collisionLayer: Int = GameConstants.LAYER_PLAYER,
    var isTrigger: Boolean = false
) : Component() {
    
    // ============================================================================
    // СВОЙСТВА ХИТБОКСА
    // ============================================================================
    
    /** Смещение хитбокса относительно позиции сущности по X */
    var offsetX: Float = 0f
    
    /** Смещение хитбокса относительно позиции сущности по Y */
    var offsetY: Float = 0f
    
    /**
     * Получение центра хитбокса по X.
     */
    val centerX: Float
        get() {
            val position = entity?.getComponent<PositionComponent>() ?: return 0f
            return position.x + offsetX
        }
    
    /**
     * Получение центра хитбокса по Y.
     */
    val centerY: Float
        get() {
            val position = entity?.getComponent<PositionComponent>() ?: return 0f
            return position.y + offsetY
        }
    
    // ============================================================================
    // КОЛЛИЗИИ
    // ============================================================================
    
    /** Флаг столкновения с чем-либо в этом кадре */
    var isColliding: Boolean = false
        private set
    
    /** Список сущностей, с которыми произошло столкновение */
    private val collidingWith: MutableList<Entity> = mutableListOf()
    
    /**
     * Получение bounds хитбокса в мировых координатах.
     */
    fun getBounds(): RectF {
        val position = entity?.getComponent<PositionComponent>()
        
        if (position == null) {
            return RectF(0f, 0f, width, height)
        }
        
        val left = position.x + offsetX - width / 2f
        val top = position.y + offsetY - height / 2f
        
        return RectF(
            left,
            top,
            left + width,
            top + height
        )
    }
    
    /**
     * Получение bounds с учётом вращения (окружающий прямоугольник).
     */
    fun getRotatedBounds(): RectF {
        val bounds = getBounds()
        val position = entity?.getComponent<PositionComponent>() ?: return bounds
        
        val rotation = position.rotation
        if (rotation == 0f) return bounds
        
        // Для простоты возвращаем bounds без учёта вращения
        // Для точного расчёта нужно использовать Polygon
        return bounds
    }
    
    /**
     * Проверка перекрытия с другим PhysicsComponent.
     * 
     * @param other Другой PhysicsComponent
     * @return true если есть перекрытие
     */
    fun overlaps(other: PhysicsComponent): Boolean {
        return RectF.intersects(getBounds(), other.getBounds())
    }
    
    /**
     * Проверка перекрытия с RectF.
     */
    fun overlaps(rect: RectF): Boolean {
        return RectF.intersects(getBounds(), rect)
    }
    
    /**
     * Проверка, содержит ли точка внутри хитбокса.
     */
    fun containsPoint(x: Float, y: Float): Boolean {
        val bounds = getBounds()
        return bounds.contains(x, y)
    }
    
    /**
     * Проверка коллизии с учётом слоёв.
     * 
     * @param other Другой PhysicsComponent
     * @return true если есть коллизия и слои совместимы
     */
    fun collidesWith(other: PhysicsComponent): Boolean {
        // Проверка слоёв
        val myMask = getCollisionMask()
        if (myMask and other.collisionLayer == 0) return false
        
        val otherMask = other.getCollisionMask()
        if (otherMask and collisionLayer == 0) return false
        
        // Проверка перекрытия
        return overlaps(other)
    }
    
    /**
     * Получение маски коллизий для этого компонента.
     * Переопределяется в наследниках для кастомной логики.
     */
    open fun getCollisionMask(): Int {
        return when (collisionLayer) {
            GameConstants.LAYER_PLAYER -> GameConstants.PLAYER_COLLISION_MASK
            GameConstants.LAYER_COLLECTIBLE -> GameConstants.COLLECTIBLE_COLLISION_MASK
            else -> -1 // Все слои
        }
    }
    
    // ============================================================================
    // УПРАВЛЕНИЕ СТОЛКНОВЕНИЯМИ
    // ============================================================================
    
    /**
     * Вызывается при начале столкновения.
     */
    open fun onCollisionEnter(other: PhysicsComponent) {
        isColliding = true
        val otherEntity = other.entity
        if (otherEntity != null && !collidingWith.contains(otherEntity)) {
            collidingWith.add(otherEntity)
        }
    }
    
    /**
     * Вызывается каждый кадр во время столкновения.
     */
    open fun onCollisionStay(other: PhysicsComponent) {
        isColliding = true
    }
    
    /**
     * Вызывается при окончании столкновения.
     */
    open fun onCollisionExit(other: PhysicsComponent) {
        val otherEntity = other.entity
        if (otherEntity != null) {
            collidingWith.remove(otherEntity)
        }
        isColliding = collidingWith.isNotEmpty()
    }
    
    /**
     * Получение списка сущностей, с которыми есть столкновение.
     */
    fun getCollidingEntities(): List<Entity> = collidingWith.toList()
    
    /**
     * Очистка списка столкновений.
     */
    fun clearCollisions() {
        collidingWith.clear()
        isColliding = false
    }
    
    // ============================================================================
    // УТИЛИТЫ
    // ============================================================================
    
    /**
     * Установка размера хитбокса.
     */
    fun setSize(width: Float, height: Float) {
        this.width = width
        this.height = height
    }
    
    /**
     * Установка смещения хитбокса.
     */
    fun setOffset(offsetX: Float, offsetY: Float) {
        this.offsetX = offsetX
        this.offsetY = offsetY
    }
    
    /**
     * Проверка, является ли сущность триггером.
     */
    fun isTrigger(): Boolean = isTrigger
    
    /**
     * Проверка, является ли сущность твёрдой (не триггер).
     */
    fun isSolid(): Boolean = !isTrigger
    
    /**
     * Получение площади хитбокса.
     */
    fun getArea(): Float = width * height
    
    /**
     * Проверка, находится ли хитбокс в пределах экрана.
     */
    fun isInScreenBounds(screenWidth: Float, screenHeight: Float): Boolean {
        val bounds = getBounds()
        return bounds.right > 0 && bounds.left < screenWidth &&
               bounds.bottom > 0 && bounds.top < screenHeight
    }
    
    /**
     * Проверка, полностью ли хитбокс за пределами экрана.
     */
    fun isOutOfBounds(screenWidth: Float, screenHeight: Float, margin: Float = 100f): Boolean {
        val bounds = getBounds()
        return bounds.right < -margin || bounds.left > screenWidth + margin ||
               bounds.bottom < -margin || bounds.top > screenHeight + margin
    }
    
    override fun reset() {
        super.reset()
        width = 64f
        height = 64f
        collisionLayer = GameConstants.LAYER_PLAYER
        isTrigger = false
        offsetX = 0f
        offsetY = 0f
        clearCollisions()
    }
    
    override fun toString(): String {
        return "PhysicsComponent(${width}x${height}, layer=$collisionLayer, trigger=$isTrigger)"
    }
}

/**
 * Extension функция для получения расстояния между хитбоксами.
 */
fun PhysicsComponent.distanceTo(other: PhysicsComponent): Float {
    val thisBounds = getBounds()
    val otherBounds = other.getBounds()
    
    // Нахождение ближайших точек
    val closestX = thisBounds.centerX().coerceIn(otherBounds.left, otherBounds.right)
    val closestY = thisBounds.centerY().coerceIn(otherBounds.top, otherBounds.bottom)
    
    val dx = thisBounds.centerX() - closestX
    val dy = thisBounds.centerY() - closestY
    
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

/**
 * Extension функция для проверки коллизии с запасом.
 */
fun PhysicsComponent.overlapsWithMargin(other: PhysicsComponent, margin: Float): Boolean {
    val thisBounds = getBounds()
    val otherBounds = other.getBounds()
    
    val expandedThis = RectF(
        thisBounds.left - margin,
        thisBounds.top - margin,
        thisBounds.right + margin,
        thisBounds.bottom + margin
    )
    
    return RectF.intersects(expandedThis, otherBounds)
}
