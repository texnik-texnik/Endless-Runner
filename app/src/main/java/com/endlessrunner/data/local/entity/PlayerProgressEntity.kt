package com.endlessrunner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.endlessrunner.data.local.converter.TypeConverters

/**
 * Entity для хранения прогресса игрока.
 *
 * @property playerId Уникальный идентификатор игрока
 * @property totalCoins Общее количество монет
 * @property bestScore Лучший результат
 * @property totalDistance Общая дистанция
 * @property totalGamesPlayed Всего игр
 * @property totalGamesWon Всего побед
 * @property enemiesDefeated Уничтожено врагов
 * @property coinsCollected Собрано монет
 * @property playTimeSeconds Время игры
 * @property currentSkin Текущий скин
 * @property unlockedSkins Разблокированные скины (JSON)
 * @property createdAt Время создания
 * @property lastPlayedAt Время последней игры
 */
@Entity(tableName = "player_progress")
@TypeConverters(TypeConverters::class)
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
