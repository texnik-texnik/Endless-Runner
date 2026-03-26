package com.endlessrunner.data.repository

import com.endlessrunner.domain.model.Achievement
import com.endlessrunner.domain.model.GameSave
import com.endlessrunner.domain.model.LeaderboardEntry
import com.endlessrunner.domain.model.PlayerProgress
import com.endlessrunner.domain.model.SettingsData
import kotlinx.coroutines.flow.Flow

/**
 * Результат операции с данными.
 * Используется для обработки ошибок в репозитории.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun exceptionOrNull(): Throwable? = when (this) {
        is Success -> null
        is Error -> exception
    }

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun error(exception: Throwable, message: String? = null): Result<Nothing> = Error(exception, message)
        fun <T> error(message: String): Result<T> = Error(Exception(message), message)
    }
}

/**
 * Extension функции для работы с Result.
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> = when (this) {
    is Result.Success -> {
        action(data)
        this
    }
    is Result.Error -> this
}

inline fun <T> Result<T>.onError(action: (Throwable) -> Unit): Result<T> = when (this) {
    is Result.Success -> this
    is Result.Error -> {
        action(exception)
        this
    }
}

/**
 * Интерфейс репозитория для работы с игровыми данными.
 * Предоставляет единый источник правды для всего приложения.
 */
interface GameRepository {

    // ============================================================================
    // PLAYER PROGRESS
    // ============================================================================

    /**
     * Получение прогресса игрока по ID.
     */
    fun getPlayerProgress(playerId: String): Flow<PlayerProgress?>

    /**
     * Сохранение прогресса игрока.
     */
    suspend fun savePlayerProgress(progress: PlayerProgress): Result<Unit>

    /**
     * Обновление отдельных полей прогресса.
     */
    suspend fun updatePlayerProgress(
        playerId: String,
        coins: Int = 0,
        bestScore: Int = 0,
        distance: Float = 0f,
        gamesPlayed: Int = 0,
        gamesWon: Int = 0,
        enemiesDefeated: Int = 0,
        coinsCollected: Int = 0,
        playTime: Long = 0L,
        currentSkin: String? = null,
        unlockedSkins: Set<String>? = null
    ): Result<Unit>

    /**
     * Удаление прогресса игрока.
     */
    suspend fun deletePlayerProgress(playerId: String): Result<Unit>

    // ============================================================================
    // GAME SAVES
    // ============================================================================

    /**
     * Получение всех сохранений.
     */
    fun getGameSaves(): Flow<List<GameSave>>

    /**
     * Получение сохранения по ID.
     */
    suspend fun getGameSave(saveId: String): GameSave?

    /**
     * Сохранение игры.
     */
    suspend fun saveGame(game: GameSave): Result<Unit>

    /**
     * Удаление сохранения.
     */
    suspend fun deleteGame(saveId: String): Result<Unit>

    /**
     * Удаление всех сохранений.
     */
    suspend fun deleteAllSaves(): Result<Unit>

    // ============================================================================
    // ACHIEVEMENTS
    // ============================================================================

    /**
     * Получение всех достижений.
     */
    fun getAchievements(): Flow<List<Achievement>>

    /**
     * Получение разблокированных достижений.
     */
    fun getUnlockedAchievements(): Flow<List<Achievement>>

    /**
     * Получение достижения по ID.
     */
    suspend fun getAchievement(id: String): Achievement?

    /**
     * Разблокировка достижения.
     */
    suspend fun unlockAchievement(id: String): Result<Unit>

    /**
     * Обновление прогресса достижения.
     */
    suspend fun updateAchievementProgress(id: String, progress: Int): Result<Unit>

    /**
     * Инициализация достижений (создание записей по умолчанию).
     */
    suspend fun initializeAchievements(): Result<Unit>

    // ============================================================================
    // LEADERBOARD
    // ============================================================================

    /**
     * Получение таблицы лидеров.
     */
    fun getLeaderboard(limit: Int = 10): Flow<List<LeaderboardEntry>>

    /**
     * Добавление записи в таблицу лидеров.
     */
    suspend fun addToLeaderboard(entry: LeaderboardEntry): Result<Unit>

    /**
     * Удаление записи из таблицы лидеров.
     */
    suspend fun deleteFromLeaderboard(entryId: String): Result<Unit>

    /**
     * Очистка таблицы лидеров.
     */
    suspend fun clearLeaderboard(): Result<Unit>

    /**
     * Получение ранга игрока по очкам.
     */
    suspend fun getPlayerRank(score: Int): Int

    // ============================================================================
    // SETTINGS
    // ============================================================================

    /**
     * Получение настроек.
     */
    fun getSettings(): Flow<SettingsData>

    /**
     * Сохранение настроек.
     */
    suspend fun saveSettings(settings: SettingsData): Result<Unit>

    /**
     * Обновление громкости.
     */
    suspend fun updateVolume(type: VolumeType, value: Float): Result<Unit>

    /**
     * Сброс настроек.
     */
    suspend fun resetSettings(): Result<Unit>

    // ============================================================================
    // PLAYER PREFERENCES
    // ============================================================================

    /**
     * Получение имени игрока.
     */
    suspend fun getPlayerName(): String

    /**
     * Сохранение имени игрока.
     */
    suspend fun setPlayerName(name: String): Result<Unit>

    /**
     * Получение текущего скина.
     */
    suspend fun getCurrentSkin(): String

    /**
     * Установка текущего скина.
     */
    suspend fun setCurrentSkin(skinId: String): Result<Unit>

    /**
     * Проверка, пройден ли туториал.
     */
    suspend fun isTutorialCompleted(): Boolean

    /**
     * Установка статуса прохождения туториала.
     */
    suspend fun setTutorialCompleted(completed: Boolean): Result<Unit>
}
