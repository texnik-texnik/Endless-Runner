package com.endlessrunner.data.mapper

import com.endlessrunner.data.local.entity.GameSaveEntity
import com.endlessrunner.domain.model.GameSave

/**
 * Маппер для преобразования GameSave между Entity и Domain.
 */
object GameSaveMapper {

    /**
     * Преобразование Entity в Domain модель.
     */
    fun GameSaveEntity.toDomain(): GameSave {
        return GameSave(
            saveId = saveId,
            score = score,
            coins = coins,
            distance = distance,
            timestamp = timestamp,
            isCompleted = isCompleted
        )
    }

    /**
     * Преобразование Domain модели в Entity.
     */
    fun GameSave.toEntity(): GameSaveEntity {
        return GameSaveEntity(
            saveId = saveId,
            score = score,
            coins = coins,
            distance = distance,
            timestamp = timestamp,
            isCompleted = isCompleted
        )
    }

    /**
     * Преобразование списка Entity в список Domain.
     */
    fun List<GameSaveEntity>.toDomainList(): List<GameSave> = map { it.toDomain() }

    /**
     * Преобразование списка Domain в список Entity.
     */
    fun List<GameSave>.toEntityList(): List<GameSaveEntity> = map { it.toEntity() }
}

/**
 * Extension функции для удобного использования.
 */
fun GameSaveEntity.toDomain() = GameSaveMapper.toDomain(this)
fun GameSave.toEntity() = GameSaveMapper.toEntity(this)
fun List<GameSaveEntity>.toDomainList() = GameSaveMapper.toDomainList(this)
fun List<GameSave>.toEntityList() = GameSaveMapper.toEntityList(this)
