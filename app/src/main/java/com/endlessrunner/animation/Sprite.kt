package com.endlessrunner.animation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF

/**
 * Спрайт с поддержкой анимации.
 * Управляет отображением анимированных объектов на canvas.
 */
class Sprite {
    /** Карта анимаций по состояниям */
    private val animations: MutableMap<AnimationState, Animation> = mutableMapOf()
    
    /** Текущее состояние анимации */
    var currentState: AnimationState = AnimationState.Idle
        private set
    
    /** Машина состояний для управления переходами */
    private val stateMachine: AnimationStateMachine = AnimationStateMachine()
    
    /** Масштаб спрайта */
    var scale: Float = 1f
    
    /** Дополнительный масштаб по X */
    var scaleX: Float = 1f
    
    /** Дополнительный масштаб по Y */
    var scaleY: Float = 1f
    
    /** Вращение спрайта (градусы) */
    var rotation: Float = 0f
    
    /** Отражение по горизонтали */
    var flipX: Boolean = false
    
    /** Отражение по вертикали */
    var flipY: Boolean = false
    
    /** Прозрачность (0-255) */
    var alpha: Int = 255
        set(value) {
            field = value.coerceIn(0, 255)
        }
    
    /** Цветовой фильтр */
    var colorFilter: ColorFilter? = null
    
    /** Смещение по X для отрисовки */
    var offsetX: Float = 0f
    
    /** Смещение по Y для отрисовки */
    var offsetY: Float = 0f
    
    /** Ширина спрайта */
    var width: Float = 64f
    
    /** Высота спрайта */
    var height: Float = 64f
    
    /** Точка привязки (pivot) относительно центра (0..1) */
    var pivotX: Float = 0.5f
    var pivotY: Float = 0.5f
    
    /** Paint для отрисовки */
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
    
    /** Matrix для трансформаций */
    private val transformMatrix = Matrix()
    
    /** Rect для отрисовки кадра */
    private val destRect = RectF()
    
    /** Callback на завершение анимации */
    var onAnimationComplete: ((AnimationState) -> Unit)? = null
    
    /**
     * Добавление анимации для состояния.
     *
     * @param state Состояние
     * @param animation Анимация
     */
    fun addAnimation(state: AnimationState, animation: Animation) {
        animations[state] = animation
    }
    
    /**
     * Добавление анимации по имени состояния.
     */
    fun addAnimation(name: String, animation: Animation) {
        AnimationState.fromName(name)?.let { state ->
            animations[state] = animation
        }
    }
    
    /**
     * Получение анимации для состояния.
     */
    fun getAnimation(state: AnimationState): Animation? = animations[state]
    
    /**
     * Получение текущей анимации.
     */
    fun getCurrentAnimation(): Animation? = animations[currentState]
    
    /**
     * Установка состояния анимации.
     *
     * @param state Новое состояние
     * @param force Принудительная смена
     * @return true если состояние изменено
     */
    fun setState(state: AnimationState, force: Boolean = false): Boolean {
        if (stateMachine.setState(state, force)) {
            currentState = state
            
            // Автоматический запуск анимации
            animations[state]?.let { anim ->
                if (!anim.isLooping || !anim.isPlaying) {
                    anim.start()
                }
            }
            
            return true
        }
        return false
    }
    
    /**
     * Установка состояния по имени.
     */
    fun setState(name: String, force: Boolean = false): Boolean {
        return AnimationState.fromName(name)?.let { setState(it, force) } ?: false
    }
    
    /**
     * Обновление спрайта и анимации.
     *
     * @param deltaTime Время с последнего кадра (секунды)
     */
    fun update(deltaTime: Float) {
        // Обновление машины состояний
        stateMachine.update(deltaTime)
        
        // Обновление текущей анимации
        val currentAnim = animations[currentState]
        currentAnim?.update(deltaTime)
        
        // Проверка завершения анимации
        if (currentAnim?.isFinished() == true) {
            onAnimationComplete?.invoke(currentState)
        }
    }
    
    /**
     * Отрисовка спрайта.
     *
     * @param canvas Canvas для отрисовки
     * @param x Позиция X (центр спрайта)
     * @param y Позиция Y (центр спрайта)
     */
    fun render(canvas: Canvas, x: Float, y: Float) {
        val frame = getCurrentFrame() ?: return
        
        canvas.save()
        
        try {
            // Настройка трансформации
            setupTransform(canvas, x, y)
            
            // Отрисовка кадра
            paint.alpha = alpha
            paint.colorFilter = colorFilter
            
            destRect.set(0f, 0f, width, height)
            canvas.drawBitmap(frame, null, destRect, paint)
            
        } finally {
            canvas.restore()
        }
    }
    
    /**
     * Отрисовка с дополнительным масштабированием.
     */
    fun render(canvas: Canvas, x: Float, y: Float, customScale: Float) {
        val savedScale = scale
        scale = customScale
        render(canvas, x, y)
        scale = savedScale
    }
    
    /**
     * Настройка трансформации canvas.
     */
    private fun setupTransform(canvas: Canvas, x: Float, y: Float) {
        // Перемещение в позицию
        canvas.translate(x + offsetX, y + offsetY)
        
        // Вращение
        if (rotation != 0f) {
            canvas.rotate(rotation)
        }
        
        // Масштабирование и отражение
        val totalScaleX = scale * scaleX * if (flipX) -1f else 1f
        val totalScaleY = scale * scaleY * if (flipY) -1f else 1f
        
        if (totalScaleX != 1f || totalScaleY != 1f) {
            canvas.scale(totalScaleX, totalScaleY)
        }
        
        // Центрирование относительно pivot
        val pivotOffsetX = width * pivotX
        val pivotOffsetY = height * pivotY
        canvas.translate(-pivotOffsetX, -pivotOffsetY)
    }
    
    /**
     * Получение текущего кадра.
     */
    fun getCurrentFrame(): Bitmap? {
        return animations[currentState]?.getCurrentFrame()
    }
    
    /**
     * Проверка, завершена ли текущая анимация.
     */
    fun isAnimationFinished(): Boolean {
        return animations[currentState]?.isFinished() ?: false
    }
    
    /**
     * Сброс спрайта.
     */
    fun reset() {
        stateMachine.reset()
        currentState = AnimationState.Idle
        scale = 1f
        scaleX = 1f
        scaleY = 1f
        rotation = 0f
        flipX = false
        flipY = false
        alpha = 255
        colorFilter = null
        offsetX = 0f
        offsetY = 0f
        pivotX = 0.5f
        pivotY = 0.5f
        
        // Сброс всех анимаций
        animations.values.forEach { it.reset() }
    }
    
    /**
     * Клонирование спрайта.
     *
     * @return Новая копия спрайта
     */
    fun copy(): Sprite {
        return Sprite().apply {
            // Копирование анимаций
            animations.putAll(this@Sprite.animations)
            
            // Копирование свойств
            currentState = this@Sprite.currentState
            scale = this@Sprite.scale
            scaleX = this@Sprite.scaleX
            scaleY = this@Sprite.scaleY
            rotation = this@Sprite.rotation
            flipX = this@Sprite.flipX
            flipY = this@Sprite.flipY
            alpha = this@Sprite.alpha
            width = this@Sprite.width
            height = this@Sprite.height
            offsetX = this@Sprite.offsetX
            offsetY = this@Sprite.offsetY
            pivotX = this@Sprite.pivotX
            pivotY = this@Sprite.pivotY
        }
    }
    
    /**
     * Получение bounds спрайта в мировых координатах.
     */
    fun getBounds(x: Float, y: Float): RectF {
        val halfWidth = (width * scale * scaleX) / 2f
        val halfHeight = (height * scale * scaleY) / 2f
        
        return RectF(
            x - halfWidth + offsetX,
            y - halfHeight + offsetY,
            x + halfWidth + offsetX,
            y + halfHeight + offsetY
        )
    }
}

/**
 * Builder для удобного создания Sprite.
 */
class SpriteBuilder {
    private val sprite = Sprite()
    private val animations: MutableMap<AnimationState, Animation> = mutableMapOf()
    
    fun size(width: Float, height: Float) = apply {
        sprite.width = width
        sprite.height = height
    }
    
    fun scale(scale: Float) = apply { sprite.scale = scale }
    fun scaleX(scale: Float) = apply { sprite.scaleX = scale }
    fun scaleY(scale: Float) = apply { sprite.scaleY = scale }
    fun rotation(rotation: Float) = apply { sprite.rotation = rotation }
    fun flipX(flip: Boolean) = apply { sprite.flipX = flip }
    fun flipY(flip: Boolean) = apply { sprite.flipY = flip }
    fun alpha(alpha: Int) = apply { sprite.alpha = alpha }
    fun offset(x: Float, y: Float) = apply {
        sprite.offsetX = x
        sprite.offsetY = y
    }
    fun pivot(x: Float, y: Float) = apply {
        sprite.pivotX = x
        sprite.pivotY = y
    }
    
    fun animation(state: AnimationState, anim: Animation) = apply {
        animations[state] = anim
    }
    
    fun build(): Sprite {
        sprite.animations.putAll(animations)
        return sprite
    }
}

/**
 * DSL функция для создания Sprite.
 */
fun sprite(block: SpriteBuilder.() -> Unit): Sprite {
    return SpriteBuilder().apply(block).build()
}

/**
 * Extension функция для быстрой настройки спрайта.
 */
inline fun Sprite.configure(block: Sprite.() -> Unit): Sprite {
    block()
    return this
}
