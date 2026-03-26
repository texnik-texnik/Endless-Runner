package com.endlessrunner.ui.screens.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.endlessrunner.domain.model.LeaderboardEntry as DomainLeaderboardEntry
import com.endlessrunner.managers.LeaderboardManager
import com.endlessrunner.managers.ProgressManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Запись в таблице лидеров для UI.
 */
data class LeaderboardEntry(
    val rank: Int,
    val playerName: String,
    val score: Int,
    val coins: Int = 0,
    val distance: Float = 0f,
    val date: Long,
    val isCurrentPlayer: Boolean = false
) {
    /**
     * Форматированная дата.
     */
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(date))
        }

    /**
     * Дистанция в километрах.
     */
    val distanceKm: Float
        get() = distance / 1000f
    }

/**
 * Тип таблицы лидеров.
 */
enum class LeaderboardType {
    LOCAL,    // Локальные рекорды
    GLOBAL    // Глобальные (онлайн)
}

/**
 * Состояние таблицы лидеров.
 */
data class LeaderboardState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val playerRank: Int = 0,
    val playerBestScore: Int = 0,
    val selectedType: LeaderboardType = LeaderboardType.LOCAL,
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalEntries: Int = 0
)

/**
 * ViewModel для таблицы лидеров.
 * Интегрирована с LeaderboardManager.
 */
class LeaderboardViewModel(
    private val leaderboardManager: LeaderboardManager,
    private val progressManager: ProgressManager
) : ViewModel() {

    private val _state = MutableStateFlow(LeaderboardState())
    val state: StateFlow<LeaderboardState> = _state.asStateFlow()

    init {
        loadLeaderboard()
        observeLeaderboard()
    }

    /**
     * Наблюдение за таблицей лидеров.
     */
    private fun observeLeaderboard() {
        viewModelScope.launch {
            leaderboardManager.getLeaderboard(100).collect { entries ->
                val uiEntries = entries.mapIndexed { index, entry ->
                    LeaderboardEntry(
                        rank = index + 1,
                        playerName = entry.playerName,
                        score = entry.score,
                        coins = entry.coins,
                        distance = entry.distance,
                        date = entry.timestamp,
                        isCurrentPlayer = false // TODO: Определить текущего игрока
                    )
                }

                _state.value = _state.value.copy(
                    entries = uiEntries,
                    totalEntries = entries.size,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Загрузка таблицы лидеров.
     */
    fun loadLeaderboard() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                // Получение лучшего результата игрока
                val progress = progressManager.getCurrentProgress()
                val playerBestScore = progress?.bestScore ?: 0

                _state.value = _state.value.copy(
                    playerBestScore = playerBestScore
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Ошибка загрузки: ${e.message}"
                )
            }
        }
    }

    /**
     * Выбор типа таблицы лидеров.
     */
    fun selectType(type: LeaderboardType) {
        _state.value = _state.value.copy(selectedType = type)

        if (type == LeaderboardType.GLOBAL) {
            // TODO: Загрузка глобальной таблицы лидеров
            // loadGlobalLeaderboard()
        } else {
            loadLeaderboard()
        }
    }

    /**
     * Обновление таблицы.
     */
    fun refresh() {
        loadLeaderboard()
    }

    /**
     * Сохранение рекорда в таблицу.
     */
    fun saveRecord(score: Int, coins: Int = 0, distance: Float = 0f, playerName: String) {
        viewModelScope.launch {
            try {
                leaderboardManager.addEntry(
                    score = score,
                    coins = coins,
                    distance = distance,
                    playerName = playerName
                )
                // Перезагрузка таблицы
                loadLeaderboard()
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }

    /**
     * Получение ранга игрока.
     */
    fun getPlayerRank(score: Int) {
        viewModelScope.launch {
            val rank = leaderboardManager.getPlayerRank(score)
            _state.value = _state.value.copy(playerRank = rank)
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}
