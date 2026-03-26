package com.endlessrunner.data.local.dao

import androidx.room.*
import com.endlessrunner.data.local.entity.LeaderboardEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object для работы с таблицей лидеров.
 */
@Dao
interface LeaderboardDao {

    /**
     * Получение топ записей таблицы лидеров.
     */
    @Query("SELECT * FROM leaderboard ORDER BY score DESC LIMIT :limit")
    fun getTopEntries(limit: Int = 10): Flow<List<LeaderboardEntryEntity>>

    /**
     * Получение топ записей (suspend версия).
     */
    @Query("SELECT * FROM leaderboard ORDER BY score DESC LIMIT :limit")
    suspend fun getTopEntriesOnce(limit: Int = 10): List<LeaderboardEntryEntity>

    /**
     * Получение записи по ID.
     */
    @Query("SELECT * FROM leaderboard WHERE entryId = :entryId LIMIT 1")
    suspend fun getEntryById(entryId: String): LeaderboardEntryEntity?

    /**
     * Получение записи по ID (Flow версия).
     */
    @Query("SELECT * FROM leaderboard WHERE entryId = :entryId")
    fun getEntryByIdFlow(entryId: String): Flow<LeaderboardEntryEntity?>

    /**
     * Получение всех записей.
     */
    @Query("SELECT * FROM leaderboard ORDER BY score DESC")
    fun getAllEntries(): Flow<List<LeaderboardEntryEntity>>

    /**
     * Вставка или обновление записи.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: LeaderboardEntryEntity)

    /**
     * Вставка списка записей.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<LeaderboardEntryEntity>)

    /**
     * Обновление записи.
     */
    @Update
    suspend fun update(entry: LeaderboardEntryEntity)

    /**
     * Удаление записи.
     */
    @Delete
    suspend fun delete(entry: LeaderboardEntryEntity)

    /**
     * Удаление записи по ID.
     */
    @Query("DELETE FROM leaderboard WHERE entryId = :entryId")
    suspend fun deleteById(entryId: String)

    /**
     * Удаление всех записей.
     */
    @Query("DELETE FROM leaderboard")
    suspend fun deleteAll()

    /**
     * Удаление старых записей (кроме топ N).
     */
    @Query("""
        DELETE FROM leaderboard 
        WHERE entryId NOT IN (
            SELECT entryId FROM leaderboard 
            ORDER BY score DESC 
            LIMIT :keepCount
        )
    """)
    suspend fun deleteOldEntries(keepCount: Int = 100)

    /**
     * Получение количества записей.
     */
    @Query("SELECT COUNT(*) FROM leaderboard")
    suspend fun getCount(): Int

    /**
     * Получение минимального счёта в топ N.
     */
    @Query("SELECT MIN(score) FROM leaderboard ORDER BY score DESC LIMIT 1 OFFSET :offset")
    suspend fun getMinScoreInTop(offset: Int): Int?

    /**
     * Получение ранга игрока по очкам.
     */
    @Query("""
        SELECT COUNT(*) + 1 FROM leaderboard 
        WHERE score > :score
    """)
    suspend fun getRankByScore(score: Int): Int

    /**
     * Проверка, является ли счёт рекордным для попадания в топ.
     */
    @Query("""
        SELECT CASE 
            WHEN (SELECT COUNT(*) FROM leaderboard) < :limit THEN 1
            WHEN :score > (SELECT MIN(score) FROM (SELECT score FROM leaderboard ORDER BY score DESC LIMIT :limit)) THEN 1
            ELSE 0 
        END
    """)
    suspend fun isScoreQualifying(score: Int, limit: Int = 10): Boolean

    /**
     * Получение записей игрока по имени.
     */
    @Query("SELECT * FROM leaderboard WHERE playerName = :playerName ORDER BY score DESC")
    fun getPlayerEntries(playerName: String): Flow<List<LeaderboardEntryEntity>>

    /**
     * Получение лучшей записи игрока.
     */
    @Query("SELECT * FROM leaderboard WHERE playerName = :playerName ORDER BY score DESC LIMIT 1")
    suspend fun getPlayerBestEntry(playerName: String): LeaderboardEntryEntity?
}
