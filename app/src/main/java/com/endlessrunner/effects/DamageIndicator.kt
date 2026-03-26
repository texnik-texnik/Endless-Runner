package com.endlessrunner.effects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.endlessrunner.components.PositionComponent
import com.endlessrunner.core.PooledObject
import com.endlessrunner.entities.Entity

/**
 * Индикатор урона.
 * Отображает всплывающие цифры урона.
 *
 * @param damage Количество урона
 * @param x Позиция X
 * @param y Позиция Y
 * @param color Цвет текста
 */
class DamageIndicator(
    /** Количество урона */
    var damage: Int = 0,
    
    x: Float = 0f,
    y: Float = 0f,
    
    /** Цвет текста */
    var color: Int = Color.RED,
    
    /** Размер шрифта */
    var textSize: Float = 40f,
    
    /** Время жизни (секунды) */
    private val lifetime: Float = 1f,
    
    /** Скорость всплытия */
    private val floatSpeed: Float = 100f
) : Entity(tag = "damage_indicator") {
    
    companion object {
        /** Пул для переиспользования */
        private val pool: com.endlessrunner.core.ObjectPool<DamageIndicator> =
            com.endlessrunner.core.ObjectPool(
                initialSize = 20,
                maxSize = 100,
                factory = { DamageIndicator() }
            )
        
        /** Получение индикатора из пула */
        fun acquire(
            damage: Int = 0,
            x: Float = 0f,
            y: Float = 0f,
            color: Int = Color.RED,
            textSize: Float = 40f
        ): DamageIndicator {
            val indicator = pool.acquire()
            indicator.damage = damage
            indicator.color = color
            indicator.textSize = textSize
            indicator.positionComponent?.setPosition(x, y)
            indicator.lifetimeRemaining = indicator.lifetime
            return indicator
        }
        
        /** Возврат в пул */
        fun release(indicator: DamageIndicator) {
            pool.release(indicator)
        }
        
        /** Paint для отрисовки (общий для всех индикаторов) */
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
    }
    
    /** Компонент позиции */
    val positionComponent: PositionComponent?
        get() = getComponent()
    
    /** Оставшееся время жизни */
    var lifetimeRemaining: Float = lifetime
        private set
    
    /** Начальная позиция Y */
    private var startY: Float = 0f
    
    /** Альфа-канал для прозрачности */
    private var alpha: Int = 255
    
    /** Флаг завершения анимации */
    var isFinished: Boolean = false
        private set
    
    init {
        addComponent(PositionComponent(x = 0f, y = 0f))
    }
    
    override fun onActivate() {
        super.onActivate()
        
        val position = positionComponent
        startY = position?.y ?: 0f
        lifetimeRemaining = lifetime
        alpha = 255
        isFinished = false
        textPaint.textSize = textSize
    }
    
    override fun update(deltaTime: Float) {
        if (!isActive || isFinished) return
        
        lifetimeRemaining -= deltaTime
        
        // Обновление позиции (всплытие)
        val position = positionComponent
        position?.y = startY - (lifetime - lifetimeRemaining) * floatSpeed
        
        // Обновление прозрачности
        alpha = (255 * (lifetimeRemaining / lifetime)).toInt()
        
        // Проверка завершения
        if (lifetimeRemaining <= 0f) {
            isFinished = true
            markForDestroy()
        }
        
        super.update(deltaTime)
    }
    
    override fun render(canvas: Canvas) {
        if (!isActive || isFinished) return
        
        val position = positionComponent ?: return
        
        // Сохранение состояния
        canvas.save()
        
        // Установка прозрачности
        textPaint.color = color
        textPaint.alpha = alpha
        
        // Отрисовка текста
        canvas.drawText(
            "-$damage",
            position.x,
            position.y,
            textPaint
        )
        
        // Тень для лучшей видимости
        textPaint.color = Color.BLACK
        textPaint.alpha = alpha / 2
        canvas.drawText(
            "-$damage",
            position.x + 2f,
            position.y + 2f,
            textPaint
        )
        
        // Восстановление состояния
        canvas.restore()
    }
    
    override fun reset() {
        super.reset()
        damage = 0
        color = Color.RED
        lifetimeRemaining = lifetime
        startY = 0f
        alpha = 255
        isFinished = false
    }
    
    /**
     * Уничтожение и возврат в пул.
     */
    fun destroy() {
        markForDestroy()
        pool.release(this)
    }
}

/**
 * Менеджер индикаторов урона.
 * Управляет созданием и обновлением индикаторов.
 */
class DamageIndicatorManager {
    
    /** Активные индикаторы */
    private val activeIndicators: MutableList<DamageIndicator> = mutableListOf()
    
    /**
     * Создание индикатора урона.
     */
    fun showDamage(
        damage: Int,
        x: Float,
        y: Float,
        color: Int = Color.RED
    ): DamageIndicator {
        val indicator = DamageIndicator.acquire(
            damage = damage,
            x = x,
            y = y,
            color = color
        )
        activeIndicators.add(indicator)
        return indicator
    }
    
    /**
     * Создание индикатора урона для критического удара.
     */
    fun showCriticalDamage(
        damage: Int,
        x: Float,
        y: Float
    ): DamageIndicator {
        return showDamage(
            damage = damage,
            x = x,
            y = y,
            color = Color.rgb(255, 87, 34) // Deep Orange для крита
        ).apply {
            textSize = 60f
        }
    }
    
    /**
     * Создание индикатора лечения.
     */
    fun showHeal(
        amount: Int,
        x: Float,
        y: Float
    ): DamageIndicator {
        val indicator = DamageIndicator.acquire(
            damage = -amount, // Отрицательный урон = лечение
            x = x,
            y = y,
            color = Color.GREEN
        )
        activeIndicators.add(indicator)
        return indicator
    }
    
    /**
     * Обновление всех индикаторов.
     */
    fun update(deltaTime: Float) {
        // Обновление активных индикаторов
        val iterator = activeIndicators.iterator()
        while (iterator.hasNext()) {
            val indicator = iterator.next()
            
            if (indicator.isFinished || !indicator.isActive) {
                indicator.destroy()
                iterator.remove()
            } else {
                indicator.update(deltaTime)
            }
        }
    }
    
    /**
     * Отрисовка всех индикаторов.
     */
    fun render(canvas: Canvas) {
        for (indicator in activeIndicators) {
            if (indicator.isActive && !indicator.isFinished) {
                indicator.render(canvas)
            }
        }
    }
    
    /**
     * Очистка всех индикаторов.
     */
    fun clear() {
        activeIndicators.forEach { it.destroy() }
        activeIndicators.clear()
    }
    
    /**
     * Количество активных индикаторов.
     */
    fun getActiveCount(): Int = activeIndicators.size
}

/**
 * Типы урона для визуальных эффектов.
 */
enum class DamageType {
    /** Обычный урон */
    NORMAL,
    
    /** Критический урон */
    CRITICAL,
    
    /** Лечение */
    HEAL,
    
    /** Блокированный урон */
    BLOCKED,
    
    /** Поглощённый урон */
    ABSORBED
}

/**
 * Extension функция для показа индикатора урона на сущности.
 */
fun Entity.showDamage(
    damage: Int,
    manager: DamageIndicatorManager,
    type: DamageType = DamageType.NORMAL
) {
    val position = getComponent<PositionComponent>() ?: return
    
    val x = position.x
    val y = position.y - 50f
    
    when (type) {
        DamageType.NORMAL -> manager.showDamage(damage, x, y)
        DamageType.CRITICAL -> manager.showCriticalDamage(damage, x, y)
        DamageType.HEAL -> manager.showHeal(damage, x, y)
        DamageType.BLOCKED -> manager.showDamage(0, x, y, Color.GRAY)
        DamageType.ABSORBED -> manager.showDamage(0, x, y, Color.BLUE)
    }
}

/**
 * Extension функция для показа урона игроку.
 */
fun com.endlessrunner.player.Player.showDamage(
    damage: Int,
    manager: DamageIndicatorManager,
    isCritical: Boolean = false
) {
    val position = positionComponent ?: return
    
    val x = position.x
    val y = position.y - 80f
    
    if (isCritical) {
        manager.showCriticalDamage(damage, x, y)
    } else {
        manager.showDamage(damage, x, y, Color.RED)
    }
}
