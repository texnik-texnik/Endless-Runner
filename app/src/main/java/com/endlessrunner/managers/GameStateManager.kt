package com.endlessrunner.managers

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Менеджер состояний игры.
 * Управляет машиной состояний игры.
 */
class GameStateManager {
    
    companion object {
        private const val TAG = "GameStateManager"
    }
    
    /** Текущее состояние */
    private val _currentState = MutableStateFlow<GameState>(GameState.Menu)
    val currentState: StateFlow<GameState> = _currentState.asStateFlow()
    
    /** Предыдущее состояние */
    private var previousState: GameState? = null
    
    /** Стек состояний (для вложенных состояний) */
    private val stateStack: ArrayDeque<GameState> = ArrayDeque()
    
    /** Флаг перехода */
    private var isTransitioning: Boolean = false
    
    /**
     * Изменение состояния.
     * 
     * @param newState Новое состояние
     * @param pushInStack Добавить в стек (для вложенных состояний)
     */
    fun changeState(newState: GameState, pushInStack: Boolean = false) {
        if (isTransitioning) {
            Log.w(TAG, "Переход уже выполняется")
            return
        }
        
        if (_currentState.value == newState && !pushInStack) {
            Log.w(TAG, "Уже в состоянии $newState")
            return
        }
        
        isTransitioning = true
        
        Log.d(TAG, "Переход из ${_currentState.value} в $newState")
        
        // Выход из текущего состояния
        _currentState.value.onExit()
        
        // Сохранение предыдущего состояния
        if (!pushInStack) {
            previousState = _currentState.value
        } else {
            stateStack.addLast(_currentState.value)
        }
        
        // Вход в новое состояние
        _currentState.value = newState
        newState.onEnter()
        
        isTransitioning = false
    }
    
    /**
     * Возврат к предыдущему состоянию.
     */
    fun popState() {
        if (stateStack.isEmpty()) {
            Log.w(TAG, "Стек состояний пуст")
            return
        }
        
        val previous = stateStack.removeLast()
        changeState(previous)
    }
    
    /**
     * Получение текущего состояния.
     */
    fun getCurrentState(): GameState = _currentState.value
    
    /**
     * Получение предыдущего состояния.
     */
    fun getPreviousState(): GameState? = previousState
    
    /**
     * Проверка текущего состояния.
     * 
     * @param state Состояние для проверки
     * @return true если текущее состояние совпадает
     */
    fun isState(state: GameState): Boolean = _currentState.value == state
    
    /**
     * Проверка, находится ли в меню.
     */
    fun isInMenu(): Boolean = _currentState.value is GameState.Menu
    
    /**
     * Проверка, идёт ли игра.
     */
    fun isPlaying(): Boolean = _currentState.value is GameState.Playing
    
    /**
     * Проверка, на паузе ли.
     */
    fun isPaused(): Boolean = _currentState.value is GameState.Paused
    
    /**
     * Проверка, конец игры ли.
     */
    fun isGameOver(): Boolean = _currentState.value is GameState.GameOver
    
    /**
     * Проверка, загрузка ли.
     */
    fun isLoading(): Boolean = _currentState.value is GameState.Loading
    
    /**
     * Сброс менеджера.
     */
    fun reset() {
        _currentState.value.onExit()
        _currentState.value = GameState.Menu
        GameState.Menu.onEnter()
        previousState = null
        stateStack.clear()
        isTransitioning = false
    }
    
    /**
     * Освобождение ресурсов.
     */
    fun dispose() {
        reset()
    }
}

/**
 * Sealed class для состояний игры.
 */
sealed class GameState {
    
    /**
     * Вызывается при входе в состояние.
     */
    open fun onEnter() {
        Log.d("GameState", "Enter: ${this::class.simpleName}")
    }
    
    /**
     * Вызывается при выходе из состояния.
     */
    open fun onExit() {
        Log.d("GameState", "Exit: ${this::class.simpleName}")
    }
    
    /**
     * Главное меню.
     */
    object Menu : GameState() {
        override fun onEnter() {
            super.onEnter()
            // TODO: Показать UI меню
        }
        
        override fun onExit() {
            super.onExit()
            // TODO: Скрыть UI меню
        }
    }
    
    /**
     * Игра идёт.
     */
    object Playing : GameState() {
        override fun onEnter() {
            super.onEnter()
            // TODO: Запустить игровой цикл
        }
        
        override fun onExit() {
            super.onExit()
            // TODO: Остановить игровой цикл
        }
    }
    
    /**
     * Пауза.
     */
    object Paused : GameState() {
        override fun onEnter() {
            super.onEnter()
            // TODO: Показать UI паузы
        }
        
        override fun onExit() {
            super.onExit()
            // TODO: Скрыть UI паузы
        }
    }
    
    /**
     * Конец игры.
     */
    object GameOver : GameState() {
        override fun onEnter() {
            super.onEnter()
            // TODO: Показать UI конца игры
        }
        
        override fun onExit() {
            super.onExit()
            // TODO: Скрыть UI конца игры
        }
    }
    
    /**
     * Экран загрузки.
     */
    object Loading : GameState() {
        override fun onEnter() {
            super.onEnter()
            // TODO: Показать экран загрузки
        }
        
        override fun onExit() {
            super.onExit()
            // TODO: Скрыть экран загрузки
        }
    }
    
    /**
     * Магазин.
     */
    object Shop : GameState() {
        override fun onEnter() {
            super.onEnter()
            // TODO: Показать магазин
        }
        
        override fun onExit() {
            super.onExit()
            // TODO: Скрыть магазин
        }
    }
    
    /**
     * Настройки.
     */
    object Settings : GameState() {
        override fun onEnter() {
            super.onEnter()
            // TODO: Показать настройки
        }
        
        override fun onExit() {
            super.onExit()
            // TODO: Скрыть настройки
        }
    }
}

/**
 * Extension property для получения названия состояния.
 */
val GameState.name: String
    get() = this::class.simpleName ?: "Unknown"

/**
 * Extension property для проверки, является ли состояние игровым.
 */
val GameState.isInGame: Boolean
    get() = this is GameState.Playing || this is GameState.Paused
