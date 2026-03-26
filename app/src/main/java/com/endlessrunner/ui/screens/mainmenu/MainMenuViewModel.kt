package com.endlessrunner.ui.screens.mainmenu

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
 * Состояние главного меню.
 */
data class MainMenuState(
    val highScore: Int = 0,
    val coins: Int = 0,
    val playerName: String = "Игрок",
    val isLoading: Boolean = false,
    val dailyRewardAvailable: Boolean = false,
    val totalGamesPlayed: Int = 0
)

/**
 * ViewModel для главного меню.
 * Управляет данными игрока и навигацией.
 */
class MainMenuViewModel(
    private val gameManager: GameManager,
    private val scoreManager: ScoreManager,
    private val saveManager: SaveManager
) : ViewModel() {

    private val _state = MutableStateFlow(MainMenuState())
    val state: StateFlow<MainMenuState> = _state.asStateFlow()

    init {
        loadPlayerData()
        observeScoreManager()
    }

    /**
     * Загрузка данных игрока.
     */
    private fun loadPlayerData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                val playerData = saveManager.loadPlayerData()
                
                _state.value = _state.value.copy(
                    highScore = playerData.highScore,
                    coins = playerData.coins,
                    playerName = playerData.playerName,
                    totalGamesPlayed = playerData.totalGamesPlayed,
                    dailyRewardAvailable = playerData.isDailyRewardAvailable,
                    isLoading = false
                )
            } catch (e: Exception) {
                // При ошибке используем данные по умолчанию
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Наблюдение за изменениями в ScoreManager.
     */
    private fun observeScoreManager() {
        viewModelScope.launch {
            scoreManager.highScore.collect { highScore ->
                _state.value = _state.value.copy(highScore = highScore)
            }
        }
    }

    /**
     * Начало новой игры.
     */
    fun startGame() {
        viewModelScope.launch {
            // Сохранение статистики
            val playerData = saveManager.loadPlayerData()
            saveManager.savePlayerData(
                playerData.copy(totalGamesPlayed = playerData.totalGamesPlayed + 1)
            )
        }
    }

    /**
     * Сбор ежедневной награды.
     */
    fun collectDailyReward() {
        viewModelScope.launch {
            val playerData = saveManager.loadPlayerData()
            val rewardCoins = 100 // Ежедневная награда
            
            saveManager.savePlayerData(
                playerData.copy(
                    coins = playerData.coins + rewardCoins,
                    lastDailyRewardTime = System.currentTimeMillis()
                )
            )
            
            _state.value = _state.value.copy(
                coins = _state.value.coins + rewardCoins,
                dailyRewardAvailable = false
            )
        }
    }

    /**
     * Обновление данных игрока.
     */
    fun refresh() {
        loadPlayerData()
    }

    override fun onCleared() {
        super.onCleared()
        // Очистка при необходимости
    }
}
