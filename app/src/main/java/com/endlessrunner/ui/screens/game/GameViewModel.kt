package com.endlessrunner.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.endlessrunner.core.GameState
import com.endlessrunner.managers.GameManager
import com.endlessrunner.managers.ScoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Состояние игрового экрана.
 */
data class GameStateUi(
    val score: Int = 0,
    val coins: Int = 0,
    val health: Float = 100f,
    val maxHealth: Float = 100f,
    val distance: Float = 0f,
    val totalDistance: Float = 10000f,
    val combo: Int = 0,
    val multiplier: Float = 1f,
    val isPaused: Boolean = false,
    val isPlaying: Boolean = false,
    val isGameOver: Boolean = false,
    val gameTime: String = "00:00",
    val enemiesDefeated: Int = 0,
    val damageTaken: Int = 0
)

/**
 * ViewModel для игрового экрана.
 * Подписывается на GameManager и ScoreManager.
 */
class GameViewModel(
    private val gameManager: GameManager,
    private val scoreManager: ScoreManager
) : ViewModel() {

    private val _state = MutableStateFlow(GameStateUi())
    val state: StateFlow<GameStateUi> = _state.asStateFlow()

    init {
        observeGameChanges()
    }

    /**
     * Наблюдение за изменениями в игре.
     */
    private fun observeGameChanges() {
        viewModelScope.launch {
            combine(
                scoreManager.score,
                scoreManager.combo,
                scoreManager.multiplier,
                gameManager.gameState
            ) { score, combo, multiplier, gameState ->
                val player = gameManager.player
                GameStateUi(
                    score = score,
                    coins = scoreManager.totalCoins,
                    health = player?.health?.toFloat() ?: 100f,
                    maxHealth = player?.maxHealth?.toFloat() ?: 100f,
                    distance = player?.positionComponent?.x ?: 0f,
                    combo = combo,
                    multiplier = multiplier,
                    isPaused = gameState == GameState.Paused,
                    isPlaying = gameState == GameState.Playing,
                    isGameOver = gameState == GameState.GameOver,
                    gameTime = gameManager.getGameTimeFormatted(),
                    enemiesDefeated = gameManager.enemiesDefeated,
                    damageTaken = gameManager.damageTaken
                )
            }.collect { newState ->
                _state.value = newState
                
                // Проверка конца игры
                if (newState.isGameOver) {
                    onGameOver()
                }
            }
        }
    }

    /**
     * Старт игры.
     */
    fun startGame() {
        scoreManager.reset()
        gameManager.startGame()
    }

    /**
     * Пауза игры.
     */
    fun pauseGame() {
        gameManager.pauseGame()
    }

    /**
     * Возобновление игры.
     */
    fun resumeGame() {
        gameManager.resumeGame()
    }

    /**
     * Перезапуск игры.
     */
    fun restartGame() {
        scoreManager.reset()
        gameManager.restartGame()
    }

    /**
     * Конец игры.
     */
    private fun onGameOver() {
        // Обработка конца игры
    }

    /**
     * Получение финального счёта.
     */
    fun getFinalScore(): Int = _state.value.score

    /**
     * Проверка нового рекорда.
     */
    fun isNewRecord(): Boolean {
        return _state.value.score > scoreManager.highScore.value
    }

    override fun onCleared() {
        super.onCleared()
        // Остановка игры при уничтожении ViewModel
        if (gameManager.isPlaying) {
            gameManager.stopGame()
        }
    }
}
