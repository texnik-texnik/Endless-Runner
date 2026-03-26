package com.endlessrunner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity для хранения прогресса игрока.
 */
@Entity(tableName = "player_progress")
data class PlayerProgressEntity(
    @PrimaryKey
    val playerId: String,
    val totalCoins: Int = 0,
    val bestScore: Int = 0,
    val totalDistance: Float = 0f,
    val totalGamesPlayed: Int = 0,
    val totalGamesWon: Int = 0,
    val enemiesDefeated: Int = 0,
    val coinsCollected: Int = 0,
    val playTimeSeconds: Long = 0L,
    val currentSkin: String = "skin_default",
    val unlockedSkins: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastPlayedAt: Long = System.currentTimeMillis()
)
