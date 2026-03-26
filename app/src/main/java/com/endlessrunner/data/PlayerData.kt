package com.endlessrunner.data

import com.endlessrunner.ui.screens.settings.GameSettings
import com.endlessrunner.ui.screens.settings.GameplaySettings
import com.endlessrunner.ui.screens.settings.GraphicsSettings
import com.endlessrunner.ui.screens.settings.SoundSettings

/**
 * Запись в таблице лидеров.
 */
data class LeaderboardRecord(
    val playerName: String,
    val score: Int,
    val date: Long,
    val isCurrentPlayer: Boolean = false
)

/**
 * Данные игрока.
 * Содержит всю сохраняемую информацию о прогрессе.
 */
data class PlayerData(
    val highScore: Int = 0,
    val coins: Int = 0,
    val playerName: String = "Игрок",
    val totalGamesPlayed: Int = 0,
    val totalWins: Int = 0,
    val totalDistance: Long = 0,
    val totalCoinsCollected: Int = 0,
    val lastDailyRewardTime: Long = 0,
    val tutorialCompleted: Boolean = false,
    val musicEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val difficulty: String = "normal",
    
    // Уровни улучшений (id улучшения -> уровень)
    val upgradeLevels: Map<String, Int> = emptyMap(),
    
    // Купленные скины (id скинов)
    val purchasedSkins: Set<String> = emptySet(),
    
    // Экипированный скин (id)
    val equippedSkinId: String = "skin_default",
    
    // Количество бонусов (id бонуса -> количество)
    val powerUpQuantities: Map<String, Int> = emptyMap(),
    
    // Записи в таблице лидеров
    val leaderboardRecords: List<LeaderboardRecord> = emptyList(),
    
    // Настройки игры
    val gameSettings: GameSettings = GameSettings()
) {
    /**
     * Проверка доступности ежедневной награды.
     * Награда доступна если прошло больше 24 часов с последнего получения.
     */
    val isDailyRewardAvailable: Boolean
        get() {
            if (lastDailyRewardTime == 0L) return true
            val hoursSinceLastReward = (System.currentTimeMillis() - lastDailyRewardTime) / (1000 * 60 * 60)
            return hoursSinceLastReward >= 24
        }
}

/**
 * Результат забега для сохранения.
 */
data class RunResult(
    val score: Int,
    val coins: Int,
    val distance: Float,
    val enemiesDefeated: Int,
    val damageTaken: Int,
    val timePlayed: Long,
    val maxCombo: Int
)
