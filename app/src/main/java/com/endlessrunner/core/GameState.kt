package com.endlessrunner.core

/**
 * Состояния игры.
 * 
 * Определяет текущее состояние игрового приложения.
 * Используется sealed class для исчерпывающей проверки состояний.
 */
sealed class GameState {
    /**
     * Начальное состояние - загрузка ресурсов и конфигурации.
     */
    data object Initializing : GameState()

    /**
     * Главное меню - игрок может начать игру, изменить настройки и т.д.
     */
    data object Menu : GameState()

    /**
     * Активная игра - игровой цикл запущен, игрок управляет персонажем.
     */
    data object Playing : GameState()

    /**
     * Пауза - игра приостановлена, игровой цикл остановлен.
     */
    data object Paused : GameState()

    /**
     * Игра окончена - игрок проиграл, показывается экран результатов.
     */
    data object GameOver : GameState()

    /**
     * Состояние ошибки - произошла критическая ошибка при загрузке или выполнении.
     * @param message Сообщение об ошибке
     */
    data class Error(val message: String) : GameState()

    /**
     * Проверка, запущена ли игра в данный момент.
     */
    val isPlaying: Boolean
        get() = this is Playing

    /**
     * Проверка, можно ли начать игру.
     */
    val canStartGame: Boolean
        get() = this is Menu || this is GameOver || this is Paused

    /**
     * Проверка, показывает ли игра UI меню.
     */
    val isShowingMenu: Boolean
        get() = this is Menu || this is Paused || this is GameOver
}
