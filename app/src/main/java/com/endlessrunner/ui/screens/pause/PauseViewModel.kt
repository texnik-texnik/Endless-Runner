package com.endlessrunner.ui.screens.pause

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.endlessrunner.managers.SaveManager
import com.endlessrunner.managers.GameManager
import com.endlessrunner.managers.ScoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Состояние экрана паузы.
 */
data class PauseState(
    val currentScore: Int = 0,
    val bestScore: Int = 0,
    val gameTime: String = "00:00",
    val isPlaying: Boolean = false
)

/**
 * ViewModel для экрана паузы.
 */
class PauseViewModel(
    private val gameManager: GameManager,
    private val scoreManager: ScoreManager,
    private val saveManager: SaveManager
) : ViewModel() {

    private val _state = MutableStateFlow(PauseState())
    val state: StateFlow<PauseState> = _state.asStateFlow()

    init {
        loadState()
    }

    /**
     * Загрузка текущего состояния.
     */
    private fun loadState() {
        viewModelScope.launch {
            _state.value = PauseState(
                currentScore = scoreManager.score.value,
                bestScore = scoreManager.highScore.value,
                gameTime = gameManager.getGameTimeFormatted(),
                isPlaying = gameManager.isPlaying
            )
        }
    }

    /**
     * Возобновление игры.
     */
    fun resume() {
        gameManager.resumeGame()
    }

    /**
     * Перезапуск игры.
     */
    fun restart() {
        scoreManager.reset()
        gameManager.restartGame()
    }

    /**
     * Выход в главное меню.
     */
    fun quit() {
        gameManager.stopGame()
    }

    /**
     * Сохранение прогресса.
     */
    fun saveProgress() {
        viewModelScope.launch {
            val playerData = saveManager.loadPlayerData()
            // TODO: Сохранение текущего прогресса
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}
