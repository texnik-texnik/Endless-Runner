package com.endlessrunner.screens

import com.endlessrunner.core.GameState
import com.endlessrunner.entities.interfaces.IUpdatable

/**
 * Базовый класс для экранов игры.
 * 
 * Экраны представляют собой различные состояния UI:
 * главное меню, игра, пауза, настройки и т.д.
 */
abstract class GameScreen : IUpdatable {
    /**
     * Название экрана.
     */
    abstract val name: String

    /**
     * Приоритет экрана (для управления стеком экранов).
     */
    open val priority: Int = 0

    /**
     * Активен ли экран.
     */
    var isActive: Boolean = false
        protected set

    /**
     * Инициализация экрана.
     * Вызывается один раз при создании.
     */
    open fun init() {}

    /**
     * Показ экрана.
     */
    open fun show() {
        isActive = true
    }

    /**
     * Скрытие экрана.
     */
    open fun hide() {
        isActive = false
    }

    /**
     * Обновление экрана.
     */
    override fun update(deltaTime: Float) {
        if (!isActive) return
        onUpdate(deltaTime)
    }

    /**
     * Логика обновления экрана.
     */
    protected open fun onUpdate(deltaTime: Float) {}

    /**
     * Обработка касаний.
     */
    open fun onTouchDown(x: Float, y: Float) {}
    open fun onTouchUp(x: Float, y: Float) {}
    open fun onTouchMove(x: Float, y: Float, deltaX: Float, deltaY: Float) {}

    /**
     * Очистка ресурсов экрана.
     */
    open fun dispose() {}
}

/**
 * Экран главного меню.
 */
class MenuScreen : GameScreen() {
    override val name: String = "Menu"
    override val priority: Int = 0

    private var selectedIndex: Int = 0

    override fun show() {
        super.show()
        selectedIndex = 0
    }

    override fun onUpdate(deltaTime: Float) {
        // Анимация элементов меню
    }
}

/**
 * Экран игры.
 */
class GameScreen : GameScreen() {
    override val name: String = "Game"
    override val priority: Int = 1

    var score: Int = 0
        private set

    var lives: Int = 3
        private set

    override fun show() {
        super.show()
        score = 0
        lives = 3
    }

    override fun onUpdate(deltaTime: Float) {
        // Обновление игрового процесса
    }

    /**
     * Добавление очков.
     */
    fun addScore(points: Int) {
        score += points
    }

    /**
     * Потеря жизни.
     */
    fun loseLife(): Boolean {
        if (lives > 0) {
            lives--
            return lives > 0
        }
        return false
    }
}

/**
 * Экран паузы.
 */
class PauseScreen : GameScreen() {
    override val name: String = "Pause"
    override val priority: Int = 2

    override fun show() {
        super.show()
        // Затемнение фона
    }
}

/**
 * Экран завершения игры.
 */
class GameOverScreen : GameScreen() {
    override val name: String = "GameOver"
    override val priority: Int = 3

    var finalScore: Int = 0
    var highScore: Int = 0

    override fun show() {
        super.show()
        // Проверка нового рекорда
        if (finalScore > highScore) {
            highScore = finalScore
        }
    }
}

/**
 * Экран настроек.
 */
class SettingsScreen : GameScreen() {
    override val name: String = "Settings"
    override val priority: Int = 1

    var musicVolume: Float = 0.6f
    var sfxVolume: Float = 0.7f
    var vibrationEnabled: Boolean = true

    override fun show() {
        super.show()
        // Загрузка сохранённых настроек
    }

    override fun hide() {
        super.hide()
        // Сохранение настроек
    }
}

/**
 * Менеджер экранов.
 * Управляет стеком активных экранов.
 */
class ScreenManager {
    private val screens = mutableMap<String, GameScreen>()
    private val screenStack = ArrayDeque<GameScreen>()

    /**
     * Регистрация экрана.
     */
    fun registerScreen(screen: GameScreen) {
        screens[screen.name] = screen
        screen.init()
    }

    /**
     * Показ экрана.
     */
    fun showScreen(screenName: String) {
        val screen = screens[screenName] ?: return
        
        // Скрываем текущий экран
        screenStack.lastOrNull()?.hide()
        
        // Показываем новый экран
        screenStack.addLast(screen)
        screen.show()
    }

    /**
     * Возврат к предыдущему экрану.
     */
    fun goBack() {
        if (screenStack.size > 1) {
            val current = screenStack.removeLast()
            current.hide()
            screenStack.lastOrNull()?.show()
        }
    }

    /**
     * Получение текущего экрана.
     */
    fun getCurrentScreen(): GameScreen? = screenStack.lastOrNull()

    /**
     * Обновление всех активных экранов.
     */
    fun update(deltaTime: Float) {
        screenStack.forEach { it.update(deltaTime) }
    }

    /**
     * Очистка всех экранов.
     */
    fun dispose() {
        screens.values.forEach { it.dispose() }
        screens.clear()
        screenStack.clear()
    }
}
