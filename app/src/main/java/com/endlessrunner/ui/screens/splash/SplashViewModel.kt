package com.endlessrunner.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.endlessrunner.config.ConfigManager
import com.endlessrunner.managers.SaveManager
import com.endlessrunner.managers.GameManager
import com.endlessrunner.managers.ScoreManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Состояние Splash экрана.
 */
data class SplashState(
    val isLoading: Boolean = true,
    val progress: Float = 0f,
    val statusMessage: String = "Загрузка...",
    val error: String? = null
)

/**
 * ViewModel для Splash экрана.
 * Отвечает за инициализацию игры и загрузку ресурсов.
 */
class SplashViewModel(
    private val configManager: ConfigManager,
    private val saveManager: SaveManager,
    private val gameManager: GameManager,
    private val scoreManager: ScoreManager
) : ViewModel() {

    companion object {
        /** Время показа splash экрана (минимум) */
        private const val SPLASH_DISPLAY_TIME_MS = 2000L
        
        /** Время на шаг загрузки */
        private const val LOAD_STEP_DELAY_MS = 300L
    }

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()

    /**
     * Инициализация игры.
     * Загружает конфигурацию, данные игрока, ресурсы.
     */
    fun initialize() {
        viewModelScope.launch {
            try {
                // Шаг 1: Загрузка конфигурации
                updateProgress(0.1f, "Загрузка конфигурации...")
                delay(LOAD_STEP_DELAY_MS)
                
                configManager.load()
                
                // Шаг 2: Загрузка данных игрока
                updateProgress(0.3f, "Загрузка данных игрока...")
                delay(LOAD_STEP_DELAY_MS)
                
                val playerData = saveManager.loadPlayerData()
                scoreManager.loadHighScore(playerData.highScore)
                
                // Шаг 3: Загрузка ресурсов
                updateProgress(0.5f, "Загрузка ресурсов...")
                delay(LOAD_STEP_DELAY_MS)
                
                // TODO: Загрузка спрайтов и звуков
                // resourceManager.loadAll()
                
                // Шаг 4: Инициализация систем
                updateProgress(0.7f, "Инициализация систем...")
                delay(LOAD_STEP_DELAY_MS)
                
                // Шаг 5: Финализация
                updateProgress(0.9f, "Подготовка...")
                delay(LOAD_STEP_DELAY_MS)
                
                // Убедимся, что splash показывается минимум SPLASH_DISPLAY_TIME_MS
                val elapsedTime = 5 * LOAD_STEP_DELAY_MS
                val remainingTime = SPLASH_DISPLAY_TIME_MS - elapsedTime
                if (remainingTime > 0) {
                    delay(remainingTime)
                }
                
                updateProgress(1.0f, "Готово!")
                delay(200)
                
                // Навигация на главное меню
                _state.value = _state.value.copy(isLoading = false)
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Ошибка загрузки: ${e.message}"
                )
            }
        }
    }

    /**
     * Обновление прогресса загрузки.
     */
    private fun updateProgress(progress: Float, message: String) {
        _state.value = _state.value.copy(
            progress = progress,
            statusMessage = message
        )
    }

    /**
     * Повторная попытка загрузки при ошибке.
     */
    fun retry() {
        _state.value = SplashState()
        initialize()
    }

    override fun onCleared() {
        super.onCleared()
        // Очистка ресурсов при необходимости
    }
}
