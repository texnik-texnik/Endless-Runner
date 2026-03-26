package com.endlessrunner.data.mapper

import com.endlessrunner.data.local.entity.PlayerProgressEntity
import com.endlessrunner.domain.model.PlayerProgress

/**
 * Маппер для преобразования PlayerProgress между Entity и Domain.
 */
object PlayerProgressMapper {

    /**
     * Преобразование Entity в Domain модель.
     */
    fun PlayerProgressEntity.toDomain(): PlayerProgress {
        return PlayerProgress(
            playerId = playerId,
            totalCoins = totalCoins,
            bestScore = bestScore,
            totalDistance = totalDistance,
            totalGamesPlayed = totalGamesPlayed,
            totalGamesWon = totalGamesWon,
            enemiesDefeated = enemiesDefeated,
            coinsCollected = coinsCollected,
            playTimeSeconds = playTimeSeconds,
            currentSkin = currentSkin,
            unlockedSkins = unlockedSkins.split(",").filter { it.isNotBlank() }.toSet(),
            createdAt = createdAt,
            lastPlayedAt = lastPlayedAt
        )
    }

    /**
     * Преобразование Domain модели в Entity.
     */
    fun PlayerProgress.toEntity(): PlayerProgressEntity {
        return PlayerProgressEntity(
            playerId = playerId,
            totalCoins = totalCoins,
            bestScore = bestScore,
            totalDistance = totalDistance,
            totalGamesPlayed = totalGamesPlayed,
            totalGamesWon = totalGamesWon,
            enemiesDefeated = enemiesDefeated,
            coinsCollected = coinsCollected,
            playTimeSeconds = playTimeSeconds,
            currentSkin = currentSkin,
            unlockedSkins = unlockedSkins.joinToString(separator = ","),
            createdAt = createdAt,
            lastPlayedAt = lastPlayedAt
        )
    }

    /**
     * Преобразование списка Entity в список Domain.
     */
    fun List<PlayerProgressEntity>.toDomainList(): List<PlayerProgress> = map { it.toDomain() }

    /**
     * Преобразование списка Domain в список Entity.
     */
    fun List<PlayerProgress>.toEntityList(): List<PlayerProgressEntity> = map { it.toEntity() }
}

/**
 * Extension функции для удобного использования.
 */
fun PlayerProgressEntity.toDomain() = PlayerProgressMapper.toDomain(this)
fun PlayerProgress.toEntity() = PlayerProgressMapper.toEntity(this)
fun List<PlayerProgressEntity>.toDomainList() = PlayerProgressMapper.toDomainList(this)
fun List<PlayerProgress>.toEntityList() = PlayerProgressMapper.toEntityList(this)
