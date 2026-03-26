package com.endlessrunner.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Экраны приложения.
 * Sealed class для типобезопасной навигации.
 */
sealed class Screen {
    
    /**
     * Экран загрузки (Splash).
     * Показывается при старте приложения.
     */
    @Serializable
    data object Splash : Screen()
    
    /**
     * Главное меню.
     * Основной экран навигации по приложению.
     */
    @Serializable
    data object MainMenu : Screen()
    
    /**
     * Игровой экран.
     * SurfaceView с игрой и HUD overlay.
     * 
     * @param difficulty Сложность (опционально)
     */
    @Serializable
    data class Game(val difficulty: String = "normal") : Screen()
    
    /**
     * Экран паузы.
     * Показывается поверх игрового экрана.
     * 
     * @param score Текущий счёт
     */
    @Serializable
    data class Pause(val score: Int = 0) : Screen()
    
    /**
     * Экран конца игры.
     * Показывает результаты и статистику.
     * 
     * @param finalScore Финальный счёт
     * @param isNewRecord Новый рекорд
     */
    @Serializable
    data class GameOver(
        val finalScore: Int = 0,
        val isNewRecord: Boolean = false
    ) : Screen()
    
    /**
     * Магазин.
     * Покупка улучшений и скинов.
     */
    @Serializable
    data object Shop : Screen()
    
    /**
     * Настройки.
     * Настройки звука, графики, геймплея.
     */
    @Serializable
    data object Settings : Screen()
    
    /**
     * Таблица лидеров.
     * Локальные рекорды.
     */
    @Serializable
    data object Leaderboard : Screen()
}

/**
 * Анимации переходов между экранами.
 */
object ScreenTransitions {
    
    /**
     * Длительность анимации в миллисекундах.
     */
    const val DURATION_MS = 300
    
    /**
     * Получить анимацию перехода для конкретного экрана.
     */
    fun getTransitionForScreen(screen: Screen): TransitionType {
        return when (screen) {
            is Screen.Splash -> TransitionType.Fade
            is Screen.MainMenu -> TransitionType.SlideLeft
            is Screen.Game -> TransitionType.Fade
            is Screen.Pause -> TransitionType.Fade
            is Screen.GameOver -> TransitionType.SlideUp
            is Screen.Shop -> TransitionType.SlideLeft
            is Screen.Settings -> TransitionType.SlideLeft
            is Screen.Leaderboard -> TransitionType.SlideLeft
        }
    }
    
    /**
     * Типы переходов.
     */
    enum class TransitionType {
        Fade,
        SlideLeft,
        SlideRight,
        SlideUp,
        SlideDown,
        Scale
    }
}
