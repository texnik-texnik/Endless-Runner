package com.endlessrunner.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.endlessrunner.animation.Sprite
import com.endlessrunner.animation.AnimationState

/**
 * Компонент рендеринга.
 * Отвечает за отображение сущности на экране.
 * Поддерживает как статические спрайты, так и анимированные.
 *
 * @param sprite Bitmap спрайта (может быть null для цветной отрисовки)
 * @param color Цвет для отрисовки (если sprite null)
 * @param width Ширина отображения
 * @param height Высота отображения
 */
open class RenderComponent(
    var sprite: Bitmap? = null,
    var color: Int = 0xFFFFFFFF.toInt(),
    var width: Float = 64f,
    var height: Float = 64f
) : Component() {
    
    // ============================================================================
    // СПРАЙТ И АНИМАЦИЯ
    // ============================================================================
    
    /** Спрайт с анимацией (опционально) */
    private var spriteObject: Sprite? = null
    
    /** Текущее состояние анимации */
    private var animationState: AnimationState = AnimationState.Idle
    
    /** Флаг использования спрайта вместо статичного bitmap */
    var useSpriteAnimation: Boolean = false
        private set
    
    companion object {
        /** Paint для отрисовки цвета (кэшируется) */
        private val colorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        
        /** Paint для отрисовки спрайта (кэшируется) */
        private val spritePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            isDither = true
        }
        
        /** Matrix для трансформаций (кэшируется, но создаётся новый для каждого рендера) */
        private val transformMatrix = Matrix()
    }
    
    // ============================================================================
    // СВОЙСТВА ОТОБРАЖЕНИЯ
    // ============================================================================
    
    /** Масштаб по X */
    var scaleX: Float = 1f
    
    /** Масштаб по Y */
    var scaleY: Float = 1f
    
    /** Отражение по горизонтали */
    var flipHorizontal: Boolean = false
    
    /** Отражение по вертикали */
    var flipVertical: Boolean = false
    
    /** Прозрачность (0-255) */
    var alpha: Int = 255
        set(value) {
            field = value.coerceIn(0, 255)
        }
    
    /** Цветовой фильтр */
    var colorFilter: ColorFilter? = null
    
    // ============================================================================
    // ОБРЕЗКА СПРАЙТА
    // ============================================================================
    
    /** Область обрезки спрайта (source rect) */
    var sourceRect: Rect? = null
    
    /** Область отображения (destination rect, вычисляется автоматически) */
    val destRect: RectF
        get() = RectF(0f, 0f, width * scaleX, height * scaleY)
    
    // ============================================================================
    // СЛОИ И ПРИОРИТЕТЫ
    // ============================================================================
    
    /** Слой рендеринга (для сортировки) */
    var renderLayer: Int = 0
    
    /** Смещение по Z (для псевдо-3D) */
    var zOffset: Float = 0f
    
    init {
        renderPriority = renderLayer
    }
    
    // ============================================================================
    // РЕНДЕРИНГ
    // ============================================================================
    
    override fun onRender(canvas: Canvas) {
        val position = entity?.getComponent<PositionComponent>() ?: return

        canvas.save()

        try {
            // Трансформация
            setupTransform(canvas, position)

            // Отрисовка
            if (useSpriteAnimation && spriteObject != null) {
                renderSpriteObject(canvas, position)
            } else if (sprite != null) {
                renderSprite(canvas)
            } else {
                renderColor(canvas)
            }
        } finally {
            canvas.restore()
        }
    }
    
    /**
     * Настройка трансформации canvas.
     */
    private fun setupTransform(canvas: Canvas, position: PositionComponent) {
        // Перемещение в позицию
        canvas.translate(position.x, position.y)
        
        // Вращение
        if (position.rotation != 0f) {
            canvas.rotate(position.rotation)
        }
        
        // Масштабирование и отражение
        val sx = if (flipHorizontal) -scaleX else scaleX
        val sy = if (flipVertical) -scaleY else scaleY
        
        if (sx != 1f || sy != 1f) {
            canvas.scale(sx, sy)
        }
        
        // Центрирование спрайта
        canvas.translate(-width / 2f, -height / 2f)
    }
    
    /**
     * Отрисовка спрайта.
     */
    private fun renderSprite(canvas: Canvas) {
        spritePaint.alpha = alpha
        spritePaint.colorFilter = colorFilter
        
        val src = sourceRect
        val dst = destRect
        
        if (src != null) {
            canvas.drawBitmap(sprite!!, src, dst, spritePaint)
        } else {
            canvas.drawBitmap(sprite!!, null, dst, spritePaint)
        }
    }
    
    /**
     * Отрисовка цветом (прямоугольник).
     */
    private fun renderColor(canvas: Canvas) {
        colorPaint.color = color
        colorPaint.alpha = alpha
        colorPaint.colorFilter = colorFilter
        
        canvas.drawRect(destRect, colorPaint)
    }
    
    // ============================================================================
    // МЕТОДЫ УПРАВЛЕНИЯ
    // ============================================================================
    
    /**
     * Установка спрайта.
     */
    fun setSprite(bitmap: Bitmap?) {
        sprite = bitmap
    }
    
    /**
     * Установка цвета.
     */
    fun setColor(color: Int) {
        this.color = color
    }
    
    /**
     * Установка размера.
     */
    fun setSize(width: Float, height: Float) {
        this.width = width
        this.height = height
    }
    
    /**
     * Установка масштаба.
     */
    fun setScale(scaleX: Float, scaleY: Float = scaleX) {
        this.scaleX = scaleX
        this.scaleY = scaleY
    }
    
    /**
     * Установка единого масштаба.
     */
    fun setScale(scale: Float) {
        setScale(scale, scale)
    }
    
    /**
     * Переключение горизонтального отражения.
     */
    fun toggleFlipHorizontal() {
        flipHorizontal = !flipHorizontal
    }
    
    /**
     * Переключение вертикального отражения.
     */
    fun toggleFlipVertical() {
        flipVertical = !flipVertical
    }
    
    /**
     * Установка направления спрайта.
     * 
     * @param facingRight true = вправо, false = влево
     */
    fun setFacingRight(facingRight: Boolean) {
        flipHorizontal = !facingRight
    }
    
    /**
     * Проверка, смотрит ли сущность вправо.
     */
    fun isFacingRight(): Boolean = !flipHorizontal
    
    /**
     * Применение цветовой тональности.
     */
    fun setTint(tintColor: Int) {
        colorFilter = android.graphics.PorterDuffColorFilter(
            tintColor,
            android.graphics.PorterDuff.Mode.SRC_ATOP
        )
    }
    
    /**
     * Очистка цветового фильтра.
     */
    fun clearColorFilter() {
        colorFilter = null
    }
    
    /**
     * Плавное изменение прозрачности.
     */
    fun fadeTo(targetAlpha: Int, deltaAlpha: Int) {
        alpha = if (alpha < targetAlpha) {
            (alpha + deltaAlpha).coerceAtMost(targetAlpha)
        } else {
            (alpha - deltaAlpha).coerceAtLeast(targetAlpha)
        }
    }
    
    override fun reset() {
        super.reset()
        scaleX = 1f
        scaleY = 1f
        flipHorizontal = false
        flipVertical = false
        alpha = 255
        colorFilter = null
        sourceRect = null
        renderLayer = 0
        zOffset = 0f
        renderPriority = 0
    }
    
    /**
     * Получение bounds компонента.
     */
    fun getBounds(): RectF {
        val position = entity?.getComponent<PositionComponent>() ?: return RectF()

        return RectF(
            position.x - width * scaleX / 2f,
            position.y - height * scaleY / 2f,
            position.x + width * scaleX / 2f,
            position.y + height * scaleY / 2f
        )
    }
    
    // ============================================================================
    // МЕТОДЫ ДЛЯ СПРАЙТА И АНИМАЦИИ
    // ============================================================================
    
    /**
     * Установка спрайта с анимацией.
     *
     * @param sprite Объект Sprite
     */
    fun setSprite(sprite: Sprite) {
        spriteObject = sprite
        useSpriteAnimation = true
        width = sprite.width
        height = sprite.height
    }
    
    /**
     * Получение спрайта.
     */
    fun getSprite(): Sprite? = spriteObject
    
    /**
     * Обновление анимации спрайта.
     *
     * @param deltaTime Время с последнего кадра
     */
    fun updateAnimation(deltaTime: Float) {
        if (useSpriteAnimation) {
            spriteObject?.update(deltaTime)
        }
    }
    
    /**
     * Установка состояния анимации.
     *
     * @param state Состояние анимации
     */
    fun setAnimationState(state: AnimationState) {
        animationState = state
        spriteObject?.setState(state)
    }
    
    /**
     * Получение текущего состояния анимации.
     */
    fun getAnimationState(): AnimationState = animationState
    
    /**
     * Отрисовка спрайта вместо обычного bitmap.
     */
    private fun renderSpriteObject(canvas: Canvas, position: PositionComponent) {
        spriteObject?.let { sprite ->
            // Синхронизация свойств
            sprite.alpha = alpha
            sprite.colorFilter = colorFilter
            sprite.scaleX = scaleX
            sprite.scaleY = scaleY
            sprite.flipX = flipHorizontal
            sprite.flipY = flipVertical
            
            // Отрисовка
            sprite.render(canvas, position.x, position.y)
        }
    }
    
    /**
     * Переключение на статичный спрайт.
     */
    fun disableSpriteAnimation() {
        useSpriteAnimation = false
    }
    
    /**
     * Переключение на анимированный спрайт.
     */
    fun enableSpriteAnimation() {
        useSpriteAnimation = spriteObject != null
    }
}

/**
 * Extension функция для отрисовки с обводкой.
 */
fun RenderComponent.drawWithStroke(
    canvas: Canvas,
    strokeColor: Int,
    strokeWidth: Float
) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = strokeColor
        strokeWidth = strokeWidth
    }
    
    canvas.drawRect(destRect, paint)
}

/**
 * Extension функция для отрисовки хитбокса (для отладки).
 */
fun RenderComponent.drawHitbox(canvas: Canvas, color: Int = 0xFFFF0000.toInt()) {
    val bounds = getBounds()
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = color
        strokeWidth = 2f
    }
    canvas.drawRect(bounds, paint)
}
