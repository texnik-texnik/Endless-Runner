package com.endlessrunner.ui.screens.gameover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.endlessrunner.managers.AchievementManager
import com.endlessrunner.managers.GameManager
import com.endlessrunner.managers.LeaderboardManager
import com.endlessrunner.managers.ProgressManager
import com.endlessrunner.managers.ScoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Статистика забега.
 */
data class RunStatistics(
    val distance: Float = 0f,
    val enemiesAvoided: Int = 0,
    val enemiesDefeated: Int = 0,
    val coinsCollected: Int = 0,
    val timePlayed: String = "00:00",
    val maxCombo: Int = 0,
    val damageTaken: Int = 0
)

/**
 * Состояние экрана Game Over.
 */
data class GameOverState(
    val finalScore: Int = 0,
    val bestScore: Int = 0,
    val coinsCollected: Int = 0,
    val isNewRecord: Boolean = false,
    val statistics: RunStatistics = RunStatistics(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val leaderboardRank: Int = 0,
    val playerName: String = "Игрок"
)

/**
 * ViewModel для экрана Game Over.
 * Интегрирована с ProgressManager, AchievementManager и LeaderboardManager.
 */
class GameOverViewModel(
    private val gameManager: GameManager,
    private val scoreManager: ScoreManager,
    private val progressManager: ProgressManager,
    private val achievementManager: AchievementManager,
    private val leaderboardManager: LeaderboardManager
) : ViewModel() {

    private val _state = MutableStateFlow(GameOverState())
    val state: StateFlow<GameOverState> = _state.asStateFlow()

    /**
     * Инициализация состояния с финальными данными.
     */
    fun initialize(finalScore: Int, isNewRecord: Boolean) {
        viewModelScope.launch {
            val playerName = progressManager.getCurrentProgress()?.let { "Игрок" } ?: "Игрок"
            
            _state.value = _state.value.copy(
                finalScore = finalScore,
                bestScore = scoreManager.highScore.value,
                coinsCollected = scoreManager.totalCoins,
                isNewRecord = isNewRecord,
                statistics = collectStatistics(),
                playerName = playerName
            )

            // Сохранение результата
            saveResult(finalScore, isNewRecord)
        }
    }

    /**
     * Сбор статистики забега.
     */
    private fun collectStatistics(): RunStatistics {
        val enemyStats = gameManager.getEnemyStats()
        val gameStats = gameManager.getCurrentGameStats()

        return RunStatistics(
            distance = gameStats.distance,
            enemiesAvoided = enemyStats.activeEnemies,
            enemiesDefeated = enemyStats.enemiesDefeated,
            coinsCollected = gameStats.coins,
            timePlayed = gameManager.getGameTimeFormatted(),
            maxCombo = scoreManager.maxCombo,
            damageTaken = enemyStats.damageTaken
        )
    }

    /**
     * Сохранение результата.
     */
    private fun saveResult(finalScore: Int, isNewRecord: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)

            try {
                // Получение статистики текущей игры
                val gameStats = gameManager.getCurrentGameStats()

                // Обновление прогресса через ProgressManager
                progressManager.recordGameCompleted(
                    isWin = false,
                    finalScore = finalScore,
                    finalCoins = gameStats.coins,
                    finalDistance = gameStats.distance,
                    playTime = gameStats.playTimeSeconds
                )

                // Добавление в таблицу лидеров
                val entryId = leaderboardManager.addEntry(
                    score = finalScore,
                    coins = gameStats.coins,
                    distance = gameStats.distance,
                    playerName = _state.value.playerName
                )

                // Получение ранга
                val rank = leaderboardManager.getPlayerRank(finalScore)

                // Проверка достижений
                progressManager.getCurrentProgress()?.let { progress ->
                    achievementManager.checkAchievements(progress)
                }

                // Проверка достижения "Идеальная игра"
                if (gameStats.damageTaken == 0) {
                    achievementManager.checkPerfectGame(0)
                }

                // Проверка достижения "Спидраннер"
                if (gameStats.distance >= 1000 && gameStats.playTimeSeconds <= 60) {
                    achievementManager.checkSpeedrun(gameStats.distance, gameStats.playTimeSeconds)
                }

                // Проверка достижения "Мастер комбо"
                achievementManager.checkComboMaster(scoreManager.maxCombo)

                _state.value = _state.value.copy(
                    isSaving = false,
                    isSaved = true,
                    bestScore = scoreManager.highScore.value,
                    leaderboardRank = rank
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false
                )
            }
        }
    }

    /**
     * Перезапуск игры.
     */
    fun restart() {
        scoreManager.reset()
        gameManager.restartGame()
    }

    /**
     * Поделиться счётом (заглушка).
     */
    fun shareScore() {
        // TODO: Реализовать share функционал
        // val shareText = "Я набрал ${_state.value.finalScore} очков в Endless Runner!"
    }

    /**
     * Сохранить в таблицу лидеров.
     */
    fun saveToLeaderboard() {
        viewModelScope.launch {
            val gameStats = gameManager.getCurrentGameStats()
            leaderboardManager.addEntry(
                score = _state.value.finalScore,
                coins = gameStats.coins,
                distance = gameStats.distance,
                playerName = _state.value.playerName
            )
        }
    }

    /**
     * Просмотр рекламы для возрождения (заглушка).
     */
    fun watchAdForRevive() {
        // TODO: Интеграция с Ads SDK
    }

    override fun onCleared() {
        super.onCleared()
    }
}
