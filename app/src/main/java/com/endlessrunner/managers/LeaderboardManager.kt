package com.endlessrunner.managers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Менеджер таблицы лидеров (локальный).
 */
class LeaderboardManager {

    data class LeaderboardEntry(
        val playerName: String,
        val score: Int,
        val coins: Int,
        val distance: Float,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    fun addEntry(playerName: String, score: Int, coins: Int, distance: Float) {
        val entry = LeaderboardEntry(playerName, score, coins, distance)
        val current = _leaderboard.value.toMutableList()
        current.add(entry)
        current.sortByDescending { it.score }
        _leaderboard.value = current.take(100) // Храним топ-100
    }

    fun getTopEntries(limit: Int = 10): List<LeaderboardEntry> =
        _leaderboard.value.take(limit)

    fun clearLeaderboard() {
        _leaderboard.value = emptyList()
    }
}
