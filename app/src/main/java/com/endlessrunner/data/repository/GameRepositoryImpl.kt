package com.endlessrunner.data.repository

import android.util.Log
import com.endlessrunner.data.local.dao.AchievementDao
import com.endlessrunner.data.local.dao.GameSaveDao
import com.endlessrunner.data.local.dao.LeaderboardDao
import com.endlessrunner.data.local.dao.PlayerProgressDao
import com.endlessrunner.data.local.datastore.PlayerPreferencesDataStore
import com.endlessrunner.data.local.datastore.SettingsDataStore
import com.endlessrunner.data.mapper.toDomain
import com.endlessrunner.data.mapper.toDomainList
import com.endlessrunner.data.mapper.toEntity
import com.endlessrunner.data.mapper.toEntityList
import com.endlessrunner.domain.model.Achievement
import com.endlessrunner.domain.model.GameSave
import com.endlessrunner.domain.model.LeaderboardEntry
import com.endlessrunner.domain.model.PlayerProgress
import com.endlessrunner.domain.model.SettingsData
import com.endlessrunner.domain.model.VolumeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Реализация GameRepository.
 *
 * @param playerProgressDao DAO для прогресса игрока
 * @param gameSaveDao DAO для сохранений
 * @param achievementDao DAO для достижений
 * @param leaderboardDao DAO для таблицы лидеров
 * @param settingsDataStore DataStore для настроек
 * @param playerPreferencesDataStore DataStore для предпочтений игрока
 */
class GameRepositoryImpl(
    private val playerProgressDao: PlayerProgressDao,
    private val gameSaveDao: GameSaveDao,
    private val achievementDao: AchievementDao,
    private val leaderboardDao: LeaderboardDao,
    private val settingsDataStore: SettingsDataStore,
    private val playerPreferencesDataStore: PlayerPreferencesDataStore
) : GameRepository {

    companion object {
        private const val TAG = "GameRepositoryImpl"
        private const val DEFAULT_PLAYER_ID = "default_player"
    }

    // ============================================================================
    // PLAYER PROGRESS
    // ============================================================================

    override fun getPlayerProgress(playerId: String): Flow<PlayerProgress?> {
        return playerProgressDao.getPlayerProgress(playerId)
            .map { it?.toDomain() }
            .catch { e ->
                Log.e(TAG, "Error getting player progress", e)
                emit(null)
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun savePlayerProgress(progress: PlayerProgress): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                playerProgressDao.insert(progress.toEntity())
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving player progress", e)
                Result.error(e, "Failed to save player progress")
            }
        }
    }

    override suspend fun updatePlayerProgress(
        playerId: String,
        coins: Int,
        bestScore: Int,
        distance: Float,
        gamesPlayed: Int,
        gamesWon: Int,
        enemiesDefeated: Int,
        coinsCollected: Int,
        playTime: Long,
        currentSkin: String?,
        unlockedSkins: Set<String>?
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Получаем текущий прогресс
                val current = playerProgressDao.getPlayerProgressOnce(playerId)
                
                if (current == null) {
                    // Если прогресса нет, создаём новый
                    val newProgress = PlayerProgress(
                        playerId = playerId,
                        totalCoins = coins,
                        bestScore = bestScore,
                        totalDistance = distance,
                        totalGamesPlayed = gamesPlayed,
                        totalGamesWon = gamesWon,
                        enemiesDefeated = enemiesDefeated,
                        coinsCollected = coinsCollected,
                        playTimeSeconds = playTime,
                        currentSkin = currentSkin ?: "skin_default",
                        unlockedSkins = unlockedSkins ?: emptySet()
                    )
                    playerProgressDao.insert(newProgress.toEntity())
                } else {
                    // Обновляем существующий
                    playerProgressDao.updateProgress(
                        playerId = playerId,
                        totalCoins = coins,
                        bestScore = bestScore,
                        distanceToAdd = distance,
                        gamesPlayedToAdd = gamesPlayed,
                        gamesWonToAdd = gamesWon,
                        enemiesDefeatedToAdd = enemiesDefeated,
                        coinsCollectedToAdd = coinsCollected,
                        playTimeToAdd = playTime,
                        currentSkin = currentSkin ?: current.currentSkin,
                        unlockedSkins = unlockedSkins?.joinToString(",") ?: current.unlockedSkins,
                        lastPlayedAt = System.currentTimeMillis()
                    )
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating player progress", e)
                Result.error(e, "Failed to update player progress")
            }
        }
    }

    override suspend fun deletePlayerProgress(playerId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                playerProgressDao.deleteByPlayerId(playerId)
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting player progress", e)
                Result.error(e, "Failed to delete player progress")
            }
        }
    }

    // ============================================================================
    // GAME SAVES
    // ============================================================================

    override fun getGameSaves(): Flow<List<GameSave>> {
        return gameSaveDao.getAllSaves()
            .map { it.toDomainList() }
            .catch { e ->
                Log.e(TAG, "Error getting game saves", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getGameSave(saveId: String): GameSave? {
        return withContext(Dispatchers.IO) {
            try {
                gameSaveDao.getSaveById(saveId)?.toDomain()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting game save", e)
                null
            }
        }
    }

    override suspend fun saveGame(game: GameSave): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                gameSaveDao.insert(game.toEntity())
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving game", e)
                Result.error(e, "Failed to save game")
            }
        }
    }

    override suspend fun deleteGame(saveId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                gameSaveDao.deleteById(saveId)
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting game save", e)
                Result.error(e, "Failed to delete game save")
            }
        }
    }

    override suspend fun deleteAllSaves(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                gameSaveDao.deleteAll()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting all saves", e)
                Result.error(e, "Failed to delete all saves")
            }
        }
    }

    // ============================================================================
    // ACHIEVEMENTS
    // ============================================================================

    override fun getAchievements(): Flow<List<Achievement>> {
        return achievementDao.getAllAchievements()
            .map { it.toDomainList() }
            .catch { e ->
                Log.e(TAG, "Error getting achievements", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getUnlockedAchievements(): Flow<List<Achievement>> {
        return achievementDao.getUnlockedAchievements()
            .map { it.toDomainList() }
            .catch { e ->
                Log.e(TAG, "Error getting unlocked achievements", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getAchievement(id: String): Achievement? {
        return withContext(Dispatchers.IO) {
            try {
                achievementDao.getAchievementById(id)?.toDomain()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting achievement", e)
                null
            }
        }
    }

    override suspend fun unlockAchievement(id: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                achievementDao.unlockAchievement(id, System.currentTimeMillis())
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error unlocking achievement", e)
                Result.error(e, "Failed to unlock achievement")
            }
        }
    }

    override suspend fun updateAchievementProgress(id: String, progress: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val achievement = achievementDao.getAchievementById(id)
                if (achievement != null) {
                    val newProgress = progress.coerceAtMost(achievement.maxProgress)
                    val unlockedAt = if (newProgress >= achievement.maxProgress) {
                        System.currentTimeMillis()
                    } else null
                    achievementDao.updateProgress(id, newProgress, unlockedAt)
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating achievement progress", e)
                Result.error(e, "Failed to update achievement progress")
            }
        }
    }

    override suspend fun initializeAchievements(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val existingIds = achievementDao.getAllAchievementsOnce().map { it.id }
                val newAchievements = Achievement.ALL_ACHIEVEMENTS
                    .filter { it.id !in existingIds }
                    .map { it.toEntity() }
                
                if (newAchievements.isNotEmpty()) {
                    achievementDao.insertAll(newAchievements)
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing achievements", e)
                Result.error(e, "Failed to initialize achievements")
            }
        }
    }

    // ============================================================================
    // LEADERBOARD
    // ============================================================================

    override fun getLeaderboard(limit: Int): Flow<List<LeaderboardEntry>> {
        return leaderboardDao.getTopEntries(limit)
            .map { it.toDomainListWithRanks() }
            .catch { e ->
                Log.e(TAG, "Error getting leaderboard", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun addToLeaderboard(entry: LeaderboardEntry): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                leaderboardDao.insert(entry.toEntity())
                // Удаляем старые записи, оставляя топ 100
                leaderboardDao.deleteOldEntries(100)
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding to leaderboard", e)
                Result.error(e, "Failed to add to leaderboard")
            }
        }
    }

    override suspend fun deleteFromLeaderboard(entryId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                leaderboardDao.deleteById(entryId)
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting from leaderboard", e)
                Result.error(e, "Failed to delete from leaderboard")
            }
        }
    }

    override suspend fun clearLeaderboard(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                leaderboardDao.deleteAll()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing leaderboard", e)
                Result.error(e, "Failed to clear leaderboard")
            }
        }
    }

    override suspend fun getPlayerRank(score: Int): Int {
        return withContext(Dispatchers.IO) {
            try {
                leaderboardDao.getRankByScore(score)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting player rank", e)
                -1
            }
        }
    }

    // ============================================================================
    // SETTINGS
    // ============================================================================

    override fun getSettings(): Flow<SettingsData> {
        return settingsDataStore.settingsFlow
            .catch { e ->
                Log.e(TAG, "Error getting settings", e)
                emit(SettingsData.DEFAULT)
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun saveSettings(settings: SettingsData): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                settingsDataStore.saveSettings(settings)
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving settings", e)
                Result.error(e, "Failed to save settings")
            }
        }
    }

    override suspend fun updateVolume(type: VolumeType, value: Float): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                settingsDataStore.updateVolume(type, value)
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating volume", e)
                Result.error(e, "Failed to update volume")
            }
        }
    }

    override suspend fun resetSettings(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                settingsDataStore.resetToDefault()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting settings", e)
                Result.error(e, "Failed to reset settings")
            }
        }
    }

    // ============================================================================
    // PLAYER PREFERENCES
    // ============================================================================

    override suspend fun getPlayerName(): String {
        return withContext(Dispatchers.IO) {
            try {
                playerPreferencesDataStore.getPlayerName()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting player name", e)
                "Игрок"
            }
        }
    }

    override suspend fun setPlayerName(name: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                playerPreferencesDataStore.setPlayerName(name)
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting player name", e)
                Result.error(e, "Failed to set player name")
            }
        }
    }

    override suspend fun getCurrentSkin(): String {
        return withContext(Dispatchers.IO) {
            try {
                playerPreferencesDataStore.getCurrentSkin()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting current skin", e)
                "skin_default"
            }
        }
    }

    override suspend fun setCurrentSkin(skinId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                playerPreferencesDataStore.setCurrentSkin(skinId)
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting current skin", e)
                Result.error(e, "Failed to set current skin")
            }
        }
    }

    override suspend fun isTutorialCompleted(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                playerPreferencesDataStore.isTutorialCompleted()
            } catch (e: Exception) {
                Log.e(TAG, "Error checking tutorial completion", e)
                false
            }
        }
    }

    override suspend fun setTutorialCompleted(completed: Boolean): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                playerPreferencesDataStore.setTutorialCompleted(completed)
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting tutorial completion", e)
                Result.error(e, "Failed to set tutorial completion")
            }
        }
    }
}
