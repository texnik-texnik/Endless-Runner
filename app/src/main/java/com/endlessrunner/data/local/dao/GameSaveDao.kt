package com.endlessrunner.data.local.dao

import androidx.room.*
import com.endlessrunner.data.local.entity.GameSaveEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object для работы с сохранениями игр.
 */
@Dao
interface GameSaveDao {

    /**
     * Получение всех сохранений, отсортированных по времени.
     */
    @Query("SELECT * FROM game_saves ORDER BY timestamp DESC")
    fun getAllSaves(): Flow<List<GameSaveEntity>>

    /**
     * Получение сохранения по ID.
     */
    @Query("SELECT * FROM game_saves WHERE saveId = :saveId LIMIT 1")
    suspend fun getSaveById(saveId: String): GameSaveEntity?

    /**
     * Получение сохранения по ID (Flow версия).
     */
    @Query("SELECT * FROM game_saves WHERE saveId = :saveId")
    fun getSaveByIdFlow(saveId: String): Flow<GameSaveEntity?>

    /**
     * Получение активных (незавершённых) сохранений.
     */
    @Query("SELECT * FROM game_saves WHERE isCompleted = 0 ORDER BY timestamp DESC")
    fun getActiveSaves(): Flow<List<GameSaveEntity>>

    /**
     * Получение завершённых сохранений.
     */
    @Query("SELECT * FROM game_saves WHERE isCompleted = 1 ORDER BY timestamp DESC")
    fun getCompletedSaves(): Flow<List<GameSaveEntity>>

    /**
     * Вставка нового сохранения.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(save: GameSaveEntity)

    /**
     * Вставка списка сохранений.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(saves: List<GameSaveEntity>)

    /**
     * Обновление сохранения.
     */
    @Update
    suspend fun update(save: GameSaveEntity)

    /**
     * Удаление сохранения.
     */
    @Delete
    suspend fun delete(save: GameSaveEntity)

    /**
     * Удаление сохранения по ID.
     */
    @Query("DELETE FROM game_saves WHERE saveId = :saveId")
    suspend fun deleteById(saveId: String)

    /**
     * Удаление всех сохранений.
     */
    @Query("DELETE FROM game_saves")
    suspend fun deleteAll()

    /**
     * Удаление завершённых сохранений.
     */
    @Query("DELETE FROM game_saves WHERE isCompleted = 1")
    suspend fun deleteCompleted()

    /**
     * Получение количества сохранений.
     */
    @Query("SELECT COUNT(*) FROM game_saves")
    suspend fun getCount(): Int

    /**
     * Получение последнего сохранения.
     */
    @Query("SELECT * FROM game_saves ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSave(): GameSaveEntity?

    /**
     * Проверка существования сохранения.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM game_saves WHERE saveId = :saveId)")
    suspend fun exists(saveId: String): Boolean
}
