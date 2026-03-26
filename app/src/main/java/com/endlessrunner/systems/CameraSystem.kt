package com.endlessrunner.systems

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.endlessrunner.components.PositionComponent
import com.endlessrunner.config.CameraConfig
import com.endlessrunner.config.GameConfig
import com.endlessrunner.core.GameConstants
import com.endlessrunner.entities.EntityManager
import com.endlessrunner.entities.Entity
import com.endlessrunner.player.Player

/**
 * Система камеры.
 * Следит за игроком и управляет видом.
 * Поддерживает эффекты: shake, flash, freeze frame.
 *
 * @param entityManager Менеджер сущностей
 * @param config Конфигурация игры
 */
class CameraSystem(
    entityManager: EntityManager,
    config: GameConfig = GameConfig.DEFAULT
) : BaseSystem(entityManager, config) {

    companion object {
        /** Пустой Rect для сброса */
        private val EMPTY_RECT = Rect(0, 0, 0, 0)
        
        /** Paint для flash эффекта */
        private val flashPaint = Paint().apply {
            style = Paint.Style.FILL
        }
    }
    
    /** Конфигурация камеры */
    private val cameraConfig: CameraConfig = config.camera
    
    /** Позиция камеры X */
    var cameraX: Float = 0f
        private set
    
    /** Позиция камеры Y */
    var cameraY: Float = 0f
        private set
    
    /** Целевая позиция X */
    private var targetX: Float = 0f
    
    /** Целевая позиция Y */
    private var targetY: Float = 0f
    
    /** Цель для следования */
    private var followTarget: Entity? = null
    
    /** Размеры вьюпорта */
    var viewportWidth: Float = 1920f
    var viewportHeight: Float = 1080f
    
    /** Границы мира */
    var worldMinX: Float = 0f
    var worldMaxX: Float = Float.MAX_VALUE
    var worldMinY: Float = 0f
    var worldMaxY: Float = Float.MAX_VALUE
    
    // ============================================================================
    // ЭФФЕКТЫ КАМЕРЫ
    // ============================================================================
    
    /** Дрожание камеры (shake) */
    private var shakeDuration: Float = 0f
    private var shakeIntensity: Float = 0f
    private var shakeTimer: Float = 0f
    private var shakeDecay: Float = 1f
    
    /** Текущее смещение shake */
    private var shakeOffsetX: Float = 0f
    private var shakeOffsetY: Float = 0f
    
    /** Flash эффект */
    private var flashDuration: Float = 0f
    private var flashTimer: Float = 0f
    private var flashColor: Int = Color.WHITE
    private var flashAlpha: Int = 0
    
    /** Freeze frame */
    private var freezeDuration: Float = 0f
    private var freezeTimer: Float = 0f
    private var isFrozen: Boolean = false
    
    /** Zoom */
    private var zoomLevel: Float = 1f
    private var targetZoom: Float = 1f
    private var zoomSpeed: Float = 2f
    
    /** Слои параллакса */
    private val parallaxLayers: MutableList<ParallaxLayer> = mutableListOf()
    
    init {
        updatePriority = 5 // До MovementSystem
    }
    
    override fun init() {
        super.init()
        
        // Инициализация позиции камеры
        cameraX = 0f
        cameraY = 0f
        targetX = 0f
        targetY = 0f
    }
    
    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        
        // Пропуск обновления при freeze
        if (isFrozen) {
            freezeTimer -= deltaTime
            if (freezeTimer <= 0) {
                isFrozen = false
            }
            return
        }

        // Обновление цели следования
        updateFollowTarget(deltaTime)

        // Плавное движение к цели
        smoothFollow(deltaTime)

        // Ограничение границами мира
        clampToBounds()

        // Обновление дрожания
        updateShake(deltaTime)
        
        // Обновление flash
        updateFlash(deltaTime)
        
        // Обновление zoom
        updateZoom(deltaTime)

        // Обновление слоёв параллакса
        updateParallaxLayers(deltaTime)
    }
    
    /**
     * Обновление цели следования.
     */
    private fun updateFollowTarget(deltaTime: Float) {
        val target = followTarget ?: return
        
        val position = target.getComponent<PositionComponent>() ?: return
        
        // Установка целевой позиции с оффсетами
        targetX = position.x + cameraConfig.offsetX
        targetY = position.y + cameraConfig.offsetY
    }
    
    /**
     * Плавное следование за целью.
     */
    private fun smoothFollow(deltaTime: Float) {
        // Lerp для плавного движения
        val lerpFactor = cameraConfig.followSpeed.coerceIn(0.01f, 1f)
        
        cameraX += (targetX - cameraX) * lerpFactor
        cameraY += (targetY - cameraY) * lerpFactor
    }
    
    /**
     * Ограничение камеры границами мира.
     */
    private fun clampToBounds() {
        // Половина вьюпорта
        val halfWidth = viewportWidth / 2f
        val halfHeight = viewportHeight / 2f
        
        // Ограничение по X
        if (cameraConfig.minX != 0f || cameraConfig.maxX != 0f) {
            val minX = if (worldMinX == 0f) cameraConfig.minX else worldMinX + halfWidth
            val maxX = if (worldMaxX == Float.MAX_VALUE) {
                if (cameraConfig.maxX == 0f) Float.MAX_VALUE else cameraConfig.maxX - halfWidth
            } else {
                worldMaxX - halfWidth
            }
            
            if (maxX != Float.MAX_VALUE) {
                cameraX = cameraX.coerceIn(minX, maxX)
            } else {
                cameraX = cameraX.coerceAtLeast(minX)
            }
        }
        
        // Ограничение по Y
        if (cameraConfig.minY != 0f || cameraConfig.maxY != 0f) {
            val minY = if (worldMinY == 0f) cameraConfig.minY else worldMinY + halfHeight
            val maxY = if (cameraConfig.maxY == 0f) worldMaxY - halfHeight else cameraConfig.maxY - halfHeight
            
            if (maxY != Float.MAX_VALUE) {
                cameraY = cameraY.coerceIn(minY, maxY)
            } else {
                cameraY = cameraY.coerceAtLeast(minY)
            }
        }
    }
    
    /**
     * Обновление дрожания камеры.
     */
    private fun updateShake(deltaTime: Float) {
        if (shakeTimer > 0) {
            shakeTimer -= deltaTime
            
            // Вычисление текущего смещения
            if (shakeTimer > 0) {
                val progress = shakeTimer / shakeDuration
                val currentIntensity = shakeIntensity * progress
                
                shakeOffsetX = (Math.random().toFloat() * 2 - 1) * currentIntensity
                shakeOffsetY = (Math.random().toFloat() * 2 - 1) * currentIntensity
            } else {
                shakeDuration = 0f
                shakeIntensity = 0f
                shakeOffsetX = 0f
                shakeOffsetY = 0f
            }
        } else {
            shakeOffsetX = 0f
            shakeOffsetY = 0f
        }
    }
    
    /**
     * Обновление flash эффекта.
     */
    private fun updateFlash(deltaTime: Float) {
        if (flashTimer > 0) {
            flashTimer -= deltaTime
            
            // Вычисление альфа-канала
            val progress = flashTimer / flashDuration
            flashAlpha = (255 * progress).toInt().coerceIn(0, 255)
            
            if (flashTimer <= 0) {
                flashDuration = 0f
                flashAlpha = 0
            }
        } else {
            flashAlpha = 0
        }
    }
    
    /**
     * Обновление zoom.
     */
    private fun updateZoom(deltaTime: Float) {
        if (zoomLevel != targetZoom) {
            val delta = (targetZoom - zoomLevel) * zoomSpeed * deltaTime
            zoomLevel += delta
            
            // Проверка достижения цели
            if (kotlin.math.abs(zoomLevel - targetZoom) < 0.01f) {
                zoomLevel = targetZoom
            }
        }
    }
    
    /**
     * Обновление слоёв параллакса.
     */
    private fun updateParallaxLayers(deltaTime: Float) {
        for (layer in parallaxLayers) {
            layer.update(cameraX, deltaTime)
        }
    }
    
    override fun render(canvas: Canvas) {
        super.render(canvas)

        // Применение трансформации камеры
        canvas.save()
        
        // Применение shake эффекта
        canvas.translate(shakeOffsetX, shakeOffsetY)
        
        // Применение zoom
        if (zoomLevel != 1f) {
            canvas.scale(zoomLevel, zoomLevel, viewportWidth / 2f, viewportHeight / 2f)
        }
        
        canvas.translate(-cameraX + viewportWidth / 2f, -cameraY + viewportHeight / 2f)

        // Отрисовка слоёв параллакса
        renderParallaxLayers(canvas)

        canvas.restore()
        
        // Отрисовка flash эффекта (поверх всего)
        renderFlash(canvas)
    }
    
    /**
     * Отрисовка слоёв параллакса.
     */
    private fun renderParallaxLayers(canvas: Canvas) {
        for (layer in parallaxLayers) {
            layer.render(canvas, cameraX, viewportWidth, viewportHeight)
        }
    }
    
    /**
     * Отрисовка flash эффекта.
     */
    private fun renderFlash(canvas: Canvas) {
        if (flashAlpha > 0) {
            flashPaint.color = flashColor
            flashPaint.alpha = flashAlpha
            canvas.drawRect(0f, 0f, viewportWidth, viewportHeight, flashPaint)
        }
    }
    
    // ============================================================================
    // УПРАВЛЕНИЕ КАМЕРОЙ
    // ============================================================================
    
    /**
     * Начало следования за сущностью.
     * 
     * @param target Сущность для следования
     */
    fun follow(target: Entity?) {
        followTarget = target
        
        if (target != null) {
            val position = target.getComponent<PositionComponent>()
            if (position != null) {
                targetX = position.x + cameraConfig.offsetX
                targetY = position.y + cameraConfig.offsetY
                cameraX = targetX
                cameraY = targetY
            }
        }
    }
    
    /**
     * Мгновенное перемещение камеры.
     * 
     * @param x Позиция X
     * @param y Позиция Y
     */
    fun setPosition(x: Float, y: Float) {
        cameraX = x
        cameraY = y
        targetX = x
        targetY = y
    }
    
    /**
     * Перемещение камеры.
     * 
     * @param deltaX Смещение X
     * @param deltaY Смещение Y
     */
    fun move(deltaX: Float, deltaY: Float) {
        cameraX += deltaX
        cameraY += deltaY
        targetX = cameraX
        targetY = cameraY
    }
    
    /**
     * Дрожание камеры.
     *
     * @param duration Длительность в секундах
     * @param intensity Интенсивность
     * @param decay Скорость затухания (1 = линейное)
     */
    fun shake(duration: Float, intensity: Float, decay: Float = 1f) {
        shakeDuration = duration
        shakeIntensity = intensity
        shakeTimer = duration
        shakeDecay = decay
    }
    
    /**
     * Flash эффект (вспышка).
     *
     * @param color Цвет вспышки
     * @param duration Длительность в секундах
     */
    fun flash(color: Int = Color.WHITE, duration: Float = 0.3f) {
        flashColor = color
        flashDuration = duration
        flashTimer = duration
        flashAlpha = 255
    }
    
    /**
     * Freeze frame (заморозка кадра).
     *
     * @param duration Длительность заморозки в секундах
     */
    fun freezeFrame(duration: Float = 0.2f) {
        freezeDuration = duration
        freezeTimer = duration
        isFrozen = true
    }
    
    /**
     * Zoom камеры.
     *
     * @param zoom Целевой уровень зума
     * @param speed Скорость зумирования
     */
    fun zoom(zoom: Float, speed: Float = 2f) {
        targetZoom = zoom.coerceIn(0.5f, 2f)
        zoomSpeed = speed
    }
    
    /**
     * Мгновенный zoom.
     */
    fun setZoom(zoom: Float) {
        zoomLevel = zoom.coerceIn(0.5f, 2f)
        targetZoom = zoomLevel
    }
    
    /**
     * Сброс zoom.
     */
    fun resetZoom() {
        targetZoom = 1f
    }
    
    /**
     * Проверка, активен ли shake.
     */
    fun isShaking(): Boolean = shakeTimer > 0
    
    /**
     * Проверка, активен ли flash.
     */
    fun isFlashing(): Boolean = flashTimer > 0
    
    /**
     * Проверка, активен ли freeze.
     */
    fun isFrozen(): Boolean = isFrozen
    
    /**
     * Принудительная остановка всех эффектов.
     */
    fun stopAllEffects() {
        shakeTimer = 0f
        shakeIntensity = 0f
        shakeOffsetX = 0f
        shakeOffsetY = 0f
        
        flashTimer = 0f
        flashAlpha = 0
        
        freezeTimer = 0f
        isFrozen = false
        
        resetZoom()
    }
    
    /**
     * Получение позиции камеры.
     */
    fun getCameraPosition(): Pair<Float, Float> = Pair(cameraX, cameraY)
    
    /**
     * Получение границ видимой области.
     */
    fun getVisibleBounds(): RectF {
        val halfWidth = viewportWidth / 2f
        val halfHeight = viewportHeight / 2f
        
        return RectF(
            cameraX - halfWidth,
            cameraY - halfHeight,
            cameraX + halfWidth,
            cameraY + halfHeight
        )
    }
    
    /**
     * Проверка видимости точки.
     * 
     * @param x Позиция X
     * @param y Позиция Y
     * @param margin Запас
     * @return true если точка видима
     */
    fun isVisible(x: Float, y: Float, margin: Float = 0f): Boolean {
        val bounds = getVisibleBounds()
        return x >= bounds.left - margin && 
               x <= bounds.right + margin &&
               y >= bounds.top - margin && 
               y <= bounds.bottom + margin
    }
    
    /**
     * Проверка видимости сущности.
     */
    fun isVisible(entity: Entity, margin: Float = 0f): Boolean {
        val position = entity.getComponent<PositionComponent>() ?: return false
        return isVisible(position.x, position.y, margin)
    }
    
    // ============================================================================
    // ПАРАЛЛАКС
    // ============================================================================
    
    /**
     * Добавление слоя параллакса.
     * 
     * @param speed Скорость параллакса (0-1)
     * @param bitmap Bitmap фона
     */
    fun addParallaxLayer(speed: Float, bitmap: android.graphics.Bitmap? = null): ParallaxLayer {
        val layer = ParallaxLayer(speed, bitmap)
        parallaxLayers.add(layer)
        return layer
    }
    
    /**
     * Удаление слоя параллакса.
     */
    fun removeParallaxLayer(layer: ParallaxLayer) {
        parallaxLayers.remove(layer)
    }
    
    /**
     * Очистка слоёв параллакса.
     */
    fun clearParallaxLayers() {
        parallaxLayers.clear()
    }
    
    override fun reset() {
        super.reset()
        cameraX = 0f
        cameraY = 0f
        targetX = 0f
        targetY = 0f
        followTarget = null
        shakeDuration = 0f
        shakeIntensity = 0f
        shakeTimer = 0f
    }
}

/**
 * Слой параллакса.
 */
class ParallaxLayer(
    /** Скорость параллакса (0 = не движется, 1 = как камера) */
    val speed: Float,
    /** Bitmap фона */
    var bitmap: android.graphics.Bitmap? = null
) {
    /** Смещение слоя */
    var offsetX: Float = 0f
    
    /** Повторять ли фон */
    var repeat: Boolean = true
    
    /**
     * Обновление слоя.
     */
    fun update(cameraX: Float, deltaTime: Float) {
        offsetX = cameraX * speed
    }
    
    /**
     * Отрисовка слоя.
     */
    fun render(canvas: Canvas, cameraX: Float, viewportWidth: Float, viewportHeight: Float) {
        val bmp = bitmap ?: return
        
        val drawX = -offsetX
        
        if (repeat) {
            // Повторение фона
            var x = drawX % bmp.width
            if (x > 0) x -= bmp.width
            
            while (x < viewportWidth) {
                canvas.drawBitmap(bmp, x, 0f, null)
                x += bmp.width
            }
        } else {
            // Одиночное изображение
            canvas.drawBitmap(bmp, drawX, 0f, null)
        }
    }
}

/**
 * Extension функция для получения камеры из систем.
 */
fun List<BaseSystem>.getCameraSystem(): CameraSystem? {
    return firstOrNull { it is CameraSystem } as? CameraSystem
}

/**
 * Extension функция для конвертации координат экрана в мировые.
 */
fun CameraSystem.screenToWorld(screenX: Float, screenY: Float): Pair<Float, Float> {
    return Pair(
        screenX + cameraX - viewportWidth / 2f,
        screenY + cameraY - viewportHeight / 2f
    )
}

/**
 * Extension функция для конвертации мировых координат в экранные.
 */
fun CameraSystem.worldToScreen(worldX: Float, worldY: Float): Pair<Float, Float> {
    return Pair(
        worldX - cameraX + viewportWidth / 2f,
        worldY - cameraY + viewportHeight / 2f
    )
}
