package com.endlessrunner.systems

import android.view.MotionEvent
import com.endlessrunner.entities.EntityManager
import com.endlessrunner.config.GameConfig
import com.endlessrunner.player.Player
import com.endlessrunner.player.PlayerInputHandler

/**
 * Система обработки ввода.
 * Интегрирует PlayerInputHandler с игровым циклом.
 * 
 * @param entityManager Менеджер сущностей
 * @param config Конфигурация игры
 * @param inputHandler Обработчик ввода
 */
class InputSystem(
    entityManager: EntityManager,
    config: GameConfig = GameConfig.DEFAULT,
    private val inputHandler: PlayerInputHandler
) : BaseSystem(entityManager, config) {
    
    init {
        updatePriority = 0 // Самый высокий приоритет
        setupCallbacks()
    }
    
    /**
     * Настройка callback'ов input handler.
     */
    private fun setupCallbacks() {
        inputHandler.onJump = {
            // Обработка прыжка будет в update
        }
        
        inputHandler.onMoveLeft = {
            // Обработка движения влево будет в update
        }
        
        inputHandler.onMoveRight = {
            // Обработка движения вправо будет в update
        }
        
        inputHandler.onStop = {
            // Обработка остановки будет в update
        }
    }
    
    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        
        // Получение игрока
        val player = entityManager.getFirstByType<Player>() ?: return
        
        if (!player.canMove()) return
        
        // Обработка ввода
        processInput(player)
    }
    
    /**
     * Обработка ввода для игрока.
     */
    private fun processInput(player: Player) {
        // Прыжок
        if (inputHandler.isJumpPressed) {
            player.jump()
        }
        
        // Движение влево
        if (inputHandler.isMoveLeftPressed) {
            player.moveLeft()
        }
        // Движение вправо
        else if (inputHandler.isMoveRightPressed) {
            player.moveRight()
        }
        // Остановка
        else {
            player.stop()
        }
        
        // Обновление состояния на основе движения
        player.updateStateFromMovement()
    }
    
    /**
     * Обработка MotionEvent.
     * Вызывается из Activity.onTouchEvent().
     * 
     * @param event MotionEvent
     * @return true если событие обработано
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        return inputHandler.onTouchEvent(event)
    }
    
    override fun reset() {
        super.reset()
        inputHandler.reset()
    }
    
    override fun dispose() {
        inputHandler.dispose()
    }
}

/**
 * Система ввода для тестов (без реального ввода).
 */
class TestInputSystem(
    entityManager: EntityManager,
    config: GameConfig = GameConfig.DEFAULT
) : BaseSystem(entityManager, config) {
    
    /** Эмуляция нажатия прыжка */
    var emulateJump: Boolean = false
    
    /** Эмуляция движения влево */
    var emulateMoveLeft: Boolean = false
    
    /** Эмуляция движения вправо */
    var emulateMoveRight: Boolean = false
    
    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        
        val player = entityManager.getFirstByType<Player>() ?: return
        
        if (!player.canMove()) return
        
        // Эмуляция ввода
        if (emulateJump) {
            player.jump()
            emulateJump = false
        }
        
        if (emulateMoveLeft) {
            player.moveLeft()
        } else if (emulateMoveRight) {
            player.moveRight()
        } else {
            player.stop()
        }
        
        player.updateStateFromMovement()
    }
}

/**
 * Extension функция для создания InputSystem.
 */
fun createInputSystem(
    entityManager: EntityManager,
    config: GameConfig,
    inputHandler: PlayerInputHandler
): InputSystem {
    return InputSystem(entityManager, config, inputHandler)
}
