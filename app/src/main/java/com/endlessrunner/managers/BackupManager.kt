package com.endlessrunner.managers

import android.content.Context
import android.util.Log
import com.endlessrunner.data.local.dao.AchievementDao
import com.endlessrunner.data.local.dao.GameSaveDao
import com.endlessrunner.data.local.dao.LeaderboardDao
import com.endlessrunner.data.local.dao.PlayerProgressDao
import com.endlessrunner.data.mapper.toDomain
import com.endlessrunner.data.mapper.toDomainList
import com.endlessrunner.data.mapper.toEntity
import com.endlessrunner.domain.model.Achievement
import com.endlessrunner.domain.model.GameSave
import com.endlessrunner.domain.model.LeaderboardEntry
import com.endlessrunner.domain.model.PlayerProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Менеджер для экспорта и импорта сохранений.
 * Позволяет создавать резервные копии и восстанавливать данные.
 *
 * @param context Context приложения
 * @param playerProgressDao DAO для прогресса игрока
 * @param gameSaveDao DAO для сохранений
 * @param achievementDao DAO для достижений
 * @param leaderboardDao DAO для таблицы лидеров
 */
class BackupManager(
    private val context: Context,
    private val playerProgressDao: PlayerProgressDao,
    private val gameSaveDao: GameSaveDao,
    private val achievementDao: AchievementDao,
    private val leaderboardDao: LeaderboardDao
) {
    companion object {
        private const val TAG = "BackupManager"
        private const val BACKUP_FILE_NAME = "game_backup.json"
        private const val BACKUP_VERSION = 1
    }

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    /**
     * Экспорт всех сохранений в JSON строку.
     *
     * @return JSON строка с данными или null при ошибке
     */
    suspend fun exportSaves(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val playerProgress = playerProgressDao.getAllProgress()
                    .firstOrNull()?.map { it.toDomain() } ?: emptyList()
                val gameSaves = gameSaveDao.getAllSaves()
                    .firstOrNull()?.map { it.toDomain() } ?: emptyList()
                val achievements = achievementDao.getAllAchievementsOnce().map { it.toDomain() }
                val leaderboard = leaderboardDao.getAllEntries()
                    .firstOrNull()?.map { it.toDomain() } ?: emptyList()

                val backupData = BackupData(
                    version = BACKUP_VERSION,
                    timestamp = System.currentTimeMillis(),
                    playerProgress = playerProgress,
                    gameSaves = gameSaves,
                    achievements = achievements,
                    leaderboard = leaderboard
                )

                json.encodeToString(backupData)
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при экспорте сохранений", e)
                null
            }
        }
    }

    /**
     * Экспорт сохранений в файл.
     *
     * @param fileName Имя файла (по умолчанию "game_backup.json")
     * @return Путь к файлу или null при ошибке
     */
    suspend fun exportToFile(fileName: String = BACKUP_FILE_NAME): String? {
        return withContext(Dispatchers.IO) {
            try {
                val json = exportSaves() ?: return@withContext null

                val file = context.filesDir.resolve(fileName)
                file.writeText(json)

                Log.d(TAG, "Сохранения экспортированы в файл: ${file.absolutePath}")
                file.absolutePath
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при экспорте в файл", e)
                null
            }
        }
    }

    /**
     * Импорт сохранений из JSON строки.
     *
     * @param json JSON строка с данными
     * @return Result с количеством восстановленных записей
     */
    suspend fun importSaves(json: String): Result<ImportResult> {
        return withContext(Dispatchers.IO) {
            try {
                val backupData = Json.decodeFromString<BackupData>(json)

                // Проверка версии
                if (backupData.version != BACKUP_VERSION) {
                    Log.w(TAG, "Несовместимая версия бэкапа: ${backupData.version}")
                    // Продолжаем импорт, но логируем предупреждение
                }

                var importedProgress = 0
                var importedSaves = 0
                var importedAchievements = 0
                var importedLeaderboard = 0

                // Импорт прогресса игрока
                backupData.playerProgress.forEach { progress ->
                    playerProgressDao.insert(progress.toEntity())
                    importedProgress++
                }

                // Импорт сохранений
                backupData.gameSaves.forEach { save ->
                    gameSaveDao.insert(save.toEntity())
                    importedSaves++
                }

                // Импорт достижений
                backupData.achievements.forEach { achievement ->
                    achievementDao.insert(achievement.toEntity())
                    importedAchievements++
                }

                // Импорт таблицы лидеров
                backupData.leaderboard.forEach { entry ->
                    leaderboardDao.insert(entry.toEntity())
                    importedLeaderboard++
                }

                Log.i(
                    TAG,
                    "Импорт завершён: прогресс=$importedProgress, сохранения=$importedSaves, " +
                            "достижения=$importedAchievements, лидеры=$importedLeaderboard"
                )

                Result.success(
                    ImportResult(
                        playerProgressCount = importedProgress,
                        gameSavesCount = importedSaves,
                        achievementsCount = importedAchievements,
                        leaderboardCount = importedLeaderboard
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при импорте сохранений", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Импорт сохранений из файла.
     *
     * @param fileName Имя файла
     * @return Result с количеством восстановленных записей
     */
    suspend fun importFromFile(fileName: String = BACKUP_FILE_NAME): Result<ImportResult> {
        return withContext(Dispatchers.IO) {
            try {
                val file = context.filesDir.resolve(fileName)
                if (!file.exists()) {
                    Log.e(TAG, "Файл бэкапа не найден: ${file.absolutePath}")
                    Result.failure(Exception("Файл бэкапа не найден"))
                } else {
                    val json = file.readText()
                    importSaves(json)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при импорте из файла", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Получение размера файла сохранений.
     *
     * @return Размер в байтах
     */
    suspend fun getSaveFileSize(): Long {
        return withContext(Dispatchers.IO) {
            try {
                val file = context.filesDir.resolve(BACKUP_FILE_NAME)
                if (file.exists()) file.length() else 0L
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при получении размера файла", e)
                0L
            }
        }
    }

    /**
     * Проверка существования файла бэкапа.
     */
    fun hasBackupFile(fileName: String = BACKUP_FILE_NAME): Boolean {
        return context.filesDir.resolve(fileName).exists()
    }

    /**
     * Удаление файла бэкапа.
     */
    fun deleteBackupFile(fileName: String = BACKUP_FILE_NAME): Boolean {
        return try {
            val file = context.filesDir.resolve(fileName)
            if (file.exists()) {
                file.delete()
                Log.d(TAG, "Файл бэкапа удалён: $fileName")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при удалении файла бэкапа", e)
            false
        }
    }

    /**
     * Создание бэкапа перед важным действием (например, обновлением).
     */
    suspend fun createBackupBeforeAction(actionName: String): String? {
        val fileName = "backup_${actionName}_${System.currentTimeMillis()}.json"
        return exportToFile(fileName)
    }

    /**
     * Получение списка всех файлов бэкапов.
     */
    fun getBackupFiles(): List<BackupFileInfo> {
        return try {
            context.filesDir.listFiles { file ->
                file.name.endsWith(".json") && file.name.startsWith("backup_")
            }?.map { file ->
                BackupFileInfo(
                    name = file.name,
                    size = file.length(),
                    timestamp = file.lastModified()
                )
            }?.sortedByDescending { it.timestamp } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении списка бэкапов", e)
            emptyList()
        }
    }

    /**
     * Очистка старых бэкапов (оставляет только последние N).
     *
     * @param keepCount Количество бэкапов для хранения
     * @return Количество удалённых файлов
     */
    fun cleanupOldBackups(keepCount: Int = 5): Int {
        return try {
            val backups = getBackupFiles()
            val toDelete = backups.drop(keepCount)
            
            toDelete.forEach { backup ->
                context.filesDir.resolve(backup.name).delete()
                Log.d(TAG, "Удалён старый бэкап: ${backup.name}")
            }
            
            toDelete.size
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при очистке старых бэкапов", e)
            0
        }
    }

    /**
     * Data class для структуры бэкапа.
     */
    @Serializable
    private data class BackupData(
        val version: Int,
        val timestamp: Long,
        val playerProgress: List<PlayerProgress>,
        val gameSaves: List<GameSave>,
        val achievements: List<Achievement>,
        val leaderboard: List<LeaderboardEntry>
    )

    /**
     * Data class для информации о файле бэкапа.
     */
    data class BackupFileInfo(
        val name: String,
        val size: Long,
        val timestamp: Long
    ) {
        val formattedSize: String
            get() = when {
                size < 1024 -> "$size B"
                size < 1024 * 1024 -> "${size / 1024} KB"
                else -> "${size / (1024 * 1024)} MB"
            }

        val formattedDate: String
            get() {
                val date = java.util.Date(timestamp)
                val format = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                return format.format(date)
            }
    }

    /**
     * Data class для результата импорта.
     */
    data class ImportResult(
        val playerProgressCount: Int,
        val gameSavesCount: Int,
        val achievementsCount: Int,
        val leaderboardCount: Int
    ) {
        val totalCount: Int
            get() = playerProgressCount + gameSavesCount + achievementsCount + leaderboardCount
    }
}
