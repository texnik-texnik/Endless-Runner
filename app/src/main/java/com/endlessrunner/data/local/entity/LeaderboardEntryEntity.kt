package com.endlessrunner.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity для таблицы лидеров.
 *
 * @property entryId Уникальный идентификатор записи
 * @property playerName Имя игрока
 * @property score Очки
 * @property coins Монеты
 * @property distance Дистанция
 * @property timestamp Время записи
 * @property rank Позиция в таблице
 */
@Entity(
    tableName = "leaderboard",
    indices = [Index(value = ["score"], order = androidx.room.Index.Order.DESCENDING)]
)
data class LeaderboardEntryEntity(
    @PrimaryKey
    val entryId: String,
    val playerName: String,
    val score: Int,
    val coins: Int = 0,
    val distance: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val rank: Int = 0
)
