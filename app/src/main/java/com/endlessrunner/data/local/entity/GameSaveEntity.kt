package com.endlessrunner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity для хранения сохранений игры.
 *
 * @property saveId Уникальный идентификатор сохранения
 * @property score Счёт в забеге
 * @property coins Монеты в забеге
 * @property distance Пройденная дистанция
 * @property timestamp Время сохранения
 * @property isCompleted Флаг завершения
 */
@Entity(tableName = "game_saves")
data class GameSaveEntity(
    @PrimaryKey
    val saveId: String,
    val score: Int = 0,
    val coins: Int = 0,
    val distance: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)
