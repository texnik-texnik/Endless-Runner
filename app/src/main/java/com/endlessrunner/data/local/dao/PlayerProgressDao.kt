package com.endlessrunner.data.local.dao

import androidx.room.*
import com.endlessrunner.data.local.entity.PlayerProgressEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object для работы с прогрессом игрока.
 * Все методы suspend или возвращают Flow для асинхронной работы.
 */
@Dao
interface PlayerProgressDao {

    /**
     * Получение прогресса игрока по ID.
     * Возвращает Flow для реактивного обновления.
     */
    @Query("SELECT * FROM player_progress WHERE playerId = :playerId")
    fun getPlayerProgress(playerId: String): Flow<PlayerProgressEntity?>

    /**
     * Получение прогресса игрока (suspend версия).
     */
    @Query("SELECT * FROM player_progress WHERE playerId = :playerId LIMIT 1")
    suspend fun getPlayerProgressOnce(playerId: String): PlayerProgressEntity?

    /**
     * Вставка или обновление прогресса.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: PlayerProgressEntity)

    /**
     * Обновление существующего прогресса.
     */
    @Update
    suspend fun update(progress: PlayerProgressEntity)

    /**
     * Удаление прогресса.
     */
    @Delete
    suspend fun delete(progress: PlayerProgressEntity)

    /**
     * Удаление прогресса по ID.
     */
    @Query("DELETE FROM player_progress WHERE playerId = :playerId")
    suspend fun deleteByPlayerId(playerId: String)

    /**
     * Получение всех записей прогресса.
     */
    @Query("SELECT * FROM player_progress ORDER BY lastPlayedAt DESC")
    fun getAllProgress(): Flow<List<PlayerProgressEntity>>

    /**
     * Обновление отдельных полей прогресса.
     */
    @Query("""
        UPDATE player_progress 
        SET totalCoins = :totalCoins,
            bestScore = MAX(bestScore, :bestScore),
            totalDistance = totalDistance + :distanceToAdd,
            totalGamesPlayed = totalGamesPlayed + :gamesPlayedToAdd,
            totalGamesWon = totalGamesWon + :gamesWonToAdd,
            enemiesDefeated = enemiesDefeated + :enemiesDefeatedToAdd,
            coinsCollected = coinsCollected + :coinsCollectedToAdd,
            playTimeSeconds = playTimeSeconds + :playTimeToAdd,
            currentSkin = :currentSkin,
            unlockedSkins = :unlockedSkins,
            lastPlayedAt = :lastPlayedAt
        WHERE playerId = :playerId
    """)
    suspend fun updateProgress(
        playerId: String,
        totalCoins: Int,
        bestScore: Int,
        distanceToAdd: Float,
        gamesPlayedToAdd: Int,
        gamesWonToAdd: Int,
        enemiesDefeatedToAdd: Int,
        coinsCollectedToAdd: Int,
        playTimeToAdd: Long,
        currentSkin: String,
        unlockedSkins: String,
        lastPlayedAt: Long
    )

    /**
     * Проверка существования прогресса.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM player_progress WHERE playerId = :playerId)")
    suspend fun exists(playerId: String): Boolean

    /**
     * Получение общего количества монет всех игроков.
     */
    @Query("SELECT SUM(totalCoins) FROM player_progress")
    suspend fun getTotalCoins(): Int?

    /**
     * Получение лучшего рекорда.
     */
    @Query("SELECT MAX(bestScore) FROM player_progress")
    suspend fun getBestScore(): Int?
}
