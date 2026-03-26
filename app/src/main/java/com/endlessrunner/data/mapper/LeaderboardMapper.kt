package com.endlessrunner.data.mapper

import com.endlessrunner.data.local.entity.LeaderboardEntryEntity
import com.endlessrunner.domain.model.LeaderboardEntry

/**
 * Маппер для преобразования LeaderboardEntry между Entity и Domain.
 */
object LeaderboardMapper {

    /**
     * Преобразование Entity в Domain модель.
     */
    fun LeaderboardEntryEntity.toDomain(): LeaderboardEntry {
        return LeaderboardEntry(
            entryId = entryId,
            playerName = playerName,
            score = score,
            coins = coins,
            distance = distance,
            timestamp = timestamp,
            rank = rank
        )
    }

    /**
     * Преобразование Domain модели в Entity.
     */
    fun LeaderboardEntry.toEntity(): LeaderboardEntryEntity {
        return LeaderboardEntryEntity(
            entryId = entryId,
            playerName = playerName,
            score = score,
            coins = coins,
            distance = distance,
            timestamp = timestamp,
            rank = rank
        )
    }

    /**
     * Преобразование списка Entity в список Domain.
     */
    fun List<LeaderboardEntryEntity>.toDomainList(): List<LeaderboardEntry> = map { it.toDomain() }

    /**
     * Преобразование списка Domain в список Entity.
     */
    fun List<LeaderboardEntry>.toEntityList(): List<LeaderboardEntryEntity> = map { it.toEntity() }

    /**
     * Преобразование списка Entity с назначением рангов.
     */
    fun List<LeaderboardEntryEntity>.toDomainListWithRanks(): List<LeaderboardEntry> {
        return mapIndexed { index, entity ->
            entity.toDomain().copy(rank = index + 1)
        }
    }
}

/**
 * Extension функции для удобного использования.
 */
fun LeaderboardEntryEntity.toDomain() = LeaderboardMapper.toDomain(this)
fun LeaderboardEntry.toEntity() = LeaderboardMapper.toEntity(this)
fun List<LeaderboardEntryEntity>.toDomainList() = LeaderboardMapper.toDomainList(this)
fun List<LeaderboardEntry>.toEntityList() = LeaderboardMapper.toEntityList(this)
fun List<LeaderboardEntryEntity>.toDomainListWithRanks() = LeaderboardMapper.toDomainListWithRanks(this)
