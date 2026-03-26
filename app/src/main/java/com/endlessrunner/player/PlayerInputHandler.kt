package com.endlessrunner.player

import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.abs

/**
 * Обработчик тач-событий для управления игроком.
 * Поддерживает multi-touch и жесты.
 * 
 * @param view View для получения ViewConfiguration
 */
class PlayerInputHandler(
    private val view: View? = null
) {
    
    companion object {
        /** Тег для логирования */
        private const val TAG = "PlayerInputHandler"
        
        /** Минимальное расстояние для свайпа (пиксели) */
        private const val MIN_SWIPE_DISTANCE = 50
        
        /** Минимальная скорость для свайпа (пикселей/сек) */
        private const val MIN_SWIPE_VELOCITY = 100
    }
    
    // ============================================================================
    // СОСТОЯНИЕ ВВОДА
    // ============================================================================
    
    /** Запрошен прыжок */
    var isJumpPressed: Boolean = false
        private set
    
    /** Запрошено движение влево */
    var isMoveLeftPressed: Boolean = false
        private set
    
    /** Запрошено движение вправо */
    var isMoveRightPressed: Boolean = false
        private set
    
    /** Текущий pointer ID для движения */
    private var movePointerId: Int = MotionEvent.INVALID_POINTER_ID
    
    /** Текущий pointer ID для прыжка */
    private var jumpPointerId: Int = MotionEvent.INVALID_POINTER_ID
    
    // ============================================================================
    // ЖЕСТЫ
    // ============================================================================
    
    /** Начальная позиция касания */
    private var touchStartX: Float = 0f
    
    /** Начальная позиция Y касания */
    private var touchStartY: Float = 0f
    
    /** Текущая позиция X */
    private var touchCurrentX: Float = 0f
    
    /** Текущая позиция Y */
    private var touchCurrentY: Float = 0f
    
    /** Время начала касания */
    private var touchStartTime: Long = 0L
    
    /** VelocityTracker для свайпов */
    private var velocityTracker: VelocityTracker? = null
    
    // ============================================================================
    // НАСТРОЙКИ
    // ============================================================================
    
    /** Включить свайпы для прыжка */
    var enableSwipeJump: Boolean = true
    
    /** Включить виртуальный джойстик */
    var enableVirtualJoystick: Boolean = true
    
    /** Зона для прыжка (правая половина экрана) */
    var jumpZoneX: Float = 0f
        set(value) {
            field = value
        }
    
    /** Минимальное время для long press */
    var longPressTimeout: Long = ViewConfiguration.getLongPressTimeout()
    
    /** Флаг long press */
    private var isLongPress: Boolean = false
    
    // ============================================================================
    // CALLBACKS
    // ============================================================================
    
    /** Callback на прыжок */
    var onJump: (() -> Unit)? = null
    
    /** Callback на движение влево */
    var onMoveLeft: (() -> Unit)? = null
    
    /** Callback на движение вправо */
    var onMoveRight: (() -> Unit)? = null
    
    /** Callback на остановку */
    var onStop: (() -> Unit)? = null
    
    /** Callback на свайп вверх */
    var onSwipeUp: (() -> Unit)? = null
    
    /** Callback на свайп вниз */
    var onSwipeDown: (() -> Unit)? = null
    
    /** Callback на свайп влево */
    var onSwipeLeft: (() -> Unit)? = null
    
    /** Callback на свайп вправо */
    var onSwipeRight: (() -> Unit)? = null
    
    // ============================================================================
    // ОБРАБОТКА СОБЫТИЙ
    // ============================================================================
    
    /**
     * Обработка MotionEvent.
     * Вызывайте из View.onTouchEvent().
     * 
     * @param event MotionEvent
     * @return true если событие обработано
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val actionIndex = event.actionIndex
        
        // Инициализация VelocityTracker
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker?.addMovement(event)
        
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                handleActionDown(event, actionIndex)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                handleActionPointerDown(event, actionIndex)
            }
            MotionEvent.ACTION_MOVE -> {
                handleActionMove(event)
            }
            MotionEvent.ACTION_UP -> {
                handleActionUp(event, actionIndex)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                handleActionPointerUp(event, actionIndex)
            }
            MotionEvent.ACTION_CANCEL -> {
                handleActionCancel()
            }
        }
        
        return true
    }
    
    /**
     * Обработка ACTION_DOWN.
     */
    private fun handleActionDown(event: MotionEvent, index: Int) {
        val x = event.getX(index)
        val y = event.getY(index)
        
        touchStartX = x
        touchStartY = y
        touchCurrentX = x
        touchCurrentY = y
        touchStartTime = System.currentTimeMillis()
        isLongPress = false
        
        // Определение зоны касания
        val screenWidth = view?.width?.toFloat() ?: 1080f
        jumpZoneX = screenWidth * 0.5f
        
        if (x > jumpZoneX) {
            // Правая зона - прыжок
            jumpPointerId = event.getPointerId(index)
            isJumpPressed = true
            onJump?.invoke()
        } else {
            // Левая зона - движение
            movePointerId = event.getPointerId(index)
            
            // Проверка на свайп
            if (enableSwipeJump) {
                // Отслеживание для свайпа
            }
        }
    }
    
    /**
     * Обработка ACTION_POINTER_DOWN.
     */
    private fun handleActionPointerDown(event: MotionEvent, index: Int) {
        val x = event.getX(index)
        val pointerId = event.getPointerId(index)
        
        if (x > jumpZoneX && jumpPointerId == MotionEvent.INVALID_POINTER_ID) {
            jumpPointerId = pointerId
            isJumpPressed = true
            onJump?.invoke()
        } else if (movePointerId == MotionEvent.INVALID_POINTER_ID) {
            movePointerId = pointerId
        }
    }
    
    /**
     * Обработка ACTION_MOVE.
     */
    private fun handleActionMove(event: MotionEvent) {
        // Поиск индекса для movePointerId
        val moveIndex = event.findPointerIndex(movePointerId)
        
        if (moveIndex >= 0) {
            val x = event.getX(moveIndex)
            val y = event.getY(moveIndex)
            
            val deltaX = x - touchStartX
            val deltaY = y - touchStartY
            
            touchCurrentX = x
            touchCurrentY = y
            
            // Определение направления движения
            if (enableVirtualJoystick) {
                val deadZone = 20f
                
                if (deltaX > deadZone) {
                    isMoveLeftPressed = false
                    isMoveRightPressed = true
                    onMoveRight?.invoke()
                } else if (deltaX < -deadZone) {
                    isMoveLeftPressed = true
                    isMoveRightPressed = false
                    onMoveLeft?.invoke()
                } else {
                    stopMovement()
                }
            }
            
            // Проверка на свайп
            if (enableSwipeJump && abs(deltaY) > MIN_SWIPE_DISTANCE) {
                velocityTracker?.computeCurrentVelocity(1000)
                val velocityY = velocityTracker?.yVelocity ?: 0f
                
                if (deltaY < 0 && velocityY < -MIN_SWIPE_VELOCITY) {
                    // Свайп вверх
                    if (!isJumpPressed) {
                        isJumpPressed = true
                        onJump?.invoke()
                        onSwipeUp?.invoke()
                    }
                }
            }
        }
    }
    
    /**
     * Обработка ACTION_UP.
     */
    private fun handleActionUp(event: MotionEvent, index: Int) {
        val pointerId = event.getPointerId(index)
        
        if (pointerId == jumpPointerId) {
            isJumpPressed = false
            jumpPointerId = MotionEvent.INVALID_POINTER_ID
        }
        
        if (pointerId == movePointerId) {
            stopMovement()
            movePointerId = MotionEvent.INVALID_POINTER_ID
        }
        
        // Проверка на tap
        val deltaTime = System.currentTimeMillis() - touchStartTime
        val deltaX = abs(touchCurrentX - touchStartX)
        val deltaY = abs(touchCurrentY - touchStartY)
        
        if (deltaTime < 200 && deltaX < 10 && deltaY < 10) {
            onTap(touchStartX, touchStartY)
        }
        
        // Очистка VelocityTracker
        velocityTracker?.recycle()
        velocityTracker = null
    }
    
    /**
     * Обработка ACTION_POINTER_UP.
     */
    private fun handleActionPointerUp(event: MotionEvent, index: Int) {
        val pointerId = event.getPointerId(index)
        
        if (pointerId == jumpPointerId) {
            isJumpPressed = false
            jumpPointerId = MotionEvent.INVALID_POINTER_ID
        }
        
        if (pointerId == movePointerId) {
            stopMovement()
            movePointerId = MotionEvent.INVALID_POINTER_ID
        }
    }
    
    /**
     * Обработка ACTION_CANCEL.
     */
    private fun handleActionCancel() {
        reset()
        velocityTracker?.recycle()
        velocityTracker = null
    }
    
    // ============================================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================================
    
    /**
     * Остановка движения.
     */
    private fun stopMovement() {
        isMoveLeftPressed = false
        isMoveRightPressed = false
        onStop?.invoke()
    }
    
    /**
     * Обработка tap.
     */
    private fun onTap(x: Float, y: Float) {
        // Можно добавить дополнительную логику для tap
    }
    
    /**
     * Сброс состояния.
     */
    fun reset() {
        isJumpPressed = false
        isMoveLeftPressed = false
        isMoveRightPressed = false
        movePointerId = MotionEvent.INVALID_POINTER_ID
        jumpPointerId = MotionEvent.INVALID_POINTER_ID
        velocityTracker?.recycle()
        velocityTracker = null
    }
    
    /**
     * Освобождение ресурсов.
     */
    fun dispose() {
        reset()
        onJump = null
        onMoveLeft = null
        onMoveRight = null
        onStop = null
        onSwipeUp = null
        onSwipeDown = null
        onSwipeLeft = null
        onSwipeRight = null
    }
}

/**
 * Extension функция для подключения к View.
 */
fun PlayerInputHandler.attachToView(view: View) {
    view.setOnTouchListener { _, event ->
        onTouchEvent(event)
    }
}

/**
 * Extension функция для создания PlayerInputHandler с View.
 */
fun View.createPlayerInputHandler(): PlayerInputHandler {
    return PlayerInputHandler(this).apply {
        attachToView(this@createPlayerInputHandler)
    }
}
