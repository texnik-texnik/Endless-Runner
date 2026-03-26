package com.endlessrunner.data.local.dao

import androidx.room.*
import com.endlessrunner.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object для работы с достижениями.
 */
@Dao
interface AchievementDao {

    /**
     * Получение всех достижений.
     */
    @Query("SELECT * FROM achievements ORDER BY id")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    /**
     * Получение всех достижений (suspend версия).
     */
    @Query("SELECT * FROM achievements ORDER BY id")
    suspend fun getAllAchievementsOnce(): List<AchievementEntity>

    /**
     * Получение разблокированных достижений.
     */
    @Query("SELECT * FROM achievements WHERE isUnlocked = 1 ORDER BY unlockedAt DESC")
    fun getUnlockedAchievements(): Flow<List<AchievementEntity>>

    /**
     * Получение заблокированных достижений.
     */
    @Query("SELECT * FROM achievements WHERE isUnlocked = 0 ORDER BY id")
    fun getLockedAchievements(): Flow<List<AchievementEntity>>

    /**
     * Получение достижения по ID.
     */
    @Query("SELECT * FROM achievements WHERE id = :id LIMIT 1")
    suspend fun getAchievementById(id: String): AchievementEntity?

    /**
     * Получение достижения по ID (Flow версия).
     */
    @Query("SELECT * FROM achievements WHERE id = :id")
    fun getAchievementByIdFlow(id: String): Flow<AchievementEntity?>

    /**
     * Вставка или обновление достижения.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(achievement: AchievementEntity)

    /**
     * Вставка списка достижений.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<AchievementEntity>)

    /**
     * Обновление достижения.
     */
    @Update
    suspend fun update(achievement: AchievementEntity)

    /**
     * Обновление прогресса достижения.
     */
    @Query("""
        UPDATE achievements 
        SET progress = :progress,
            isUnlocked = CASE WHEN :progress >= maxProgress THEN 1 ELSE isUnlocked END,
            unlockedAt = CASE WHEN :progress >= maxProgress THEN :unlockedAt ELSE unlockedAt END
        WHERE id = :id
    """)
    suspend fun updateProgress(id: String, progress: Int, unlockedAt: Long?)

    /**
     * Разблокировка достижения.
     */
    @Query("""
        UPDATE achievements 
        SET isUnlocked = 1,
            unlockedAt = :unlockedAt,
            progress = maxProgress
        WHERE id = :id
    """)
    suspend fun unlockAchievement(id: String, unlockedAt: Long)

    /**
     * Сброс всех достижений.
     */
    @Query("UPDATE achievements SET isUnlocked = 0, unlockedAt = NULL, progress = 0")
    suspend fun resetAll()

    /**
     * Удаление достижения.
     */
    @Delete
    suspend fun delete(achievement: AchievementEntity)

    /**
     * Удаление достижения по ID.
     */
    @Query("DELETE FROM achievements WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Получение количества разблокированных достижений.
     */
    @Query("SELECT COUNT(*) FROM achievements WHERE isUnlocked = 1")
    suspend fun getUnlockedCount(): Int

    /**
     * Получение общего количества достижений.
     */
    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getTotalCount(): Int

    /**
     * Получение процента разблокированных достижений.
     */
    @Query("""
        SELECT CAST(SUM(CASE WHEN isUnlocked THEN 1 ELSE 0 END) AS FLOAT) * 100 / COUNT(*) 
        FROM achievements
    """)
    suspend fun getUnlockPercentage(): Float?
}
