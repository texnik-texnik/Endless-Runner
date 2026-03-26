package com.endlessrunner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity для хранения достижений.
 *
 * @property id Уникальный идентификатор достижения
 * @property title Заголовок
 * @property description Описание
 * @property iconResId ID ресурса иконки
 * @property isUnlocked Флаг разблокировки
 * @property unlockedAt Время разблокировки
 * @property progress Текущий прогресс
 * @property maxProgress Максимальный прогресс
 */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val iconResId: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val progress: Int = 0,
    val maxProgress: Int = 1
)
