package com.endlessrunner.data.mapper

import com.endlessrunner.data.local.entity.AchievementEntity
import com.endlessrunner.domain.model.Achievement

/**
 * Маппер для преобразования Achievement между Entity и Domain.
 */
object AchievementMapper {

    /**
     * Преобразование Entity в Domain модель.
     */
    fun AchievementEntity.toDomain(): Achievement {
        return Achievement(
            id = id,
            title = title,
            description = description,
            iconResId = iconResId,
            isUnlocked = isUnlocked,
            unlockedAt = unlockedAt,
            progress = progress,
            maxProgress = maxProgress
        )
    }

    /**
     * Преобразование Domain модели в Entity.
     */
    fun Achievement.toEntity(): AchievementEntity {
        return AchievementEntity(
            id = id,
            title = title,
            description = description,
            iconResId = iconResId,
            isUnlocked = isUnlocked,
            unlockedAt = unlockedAt,
            progress = progress,
            maxProgress = maxProgress
        )
    }

    /**
     * Преобразование списка Entity в список Domain.
     */
    fun List<AchievementEntity>.toDomainList(): List<Achievement> = map { it.toDomain() }

    /**
     * Преобразование списка Domain в список Entity.
     */
    fun List<Achievement>.toEntityList(): List<AchievementEntity> = map { it.toEntity() }

    /**
     * Создание Entity из Domain модели с обновлённым прогрессом.
     */
    fun Achievement.toEntityWithProgress(newProgress: Int, unlockedAt: Long?): AchievementEntity {
        return AchievementEntity(
            id = id,
            title = title,
            description = description,
            iconResId = iconResId,
            isUnlocked = newProgress >= maxProgress,
            unlockedAt = if (newProgress >= maxProgress) unlockedAt ?: System.currentTimeMillis() else null,
            progress = newProgress.coerceAtMost(maxProgress),
            maxProgress = maxProgress
        )
    }
}

/**
 * Extension функции для удобного использования.
 */
fun AchievementEntity.toDomain() = AchievementMapper.toDomain(this)
fun Achievement.toEntity() = AchievementMapper.toEntity(this)
fun List<AchievementEntity>.toDomainList() = AchievementMapper.toDomainList(this)
fun List<Achievement>.toEntityList() = AchievementMapper.toEntityList(this)
