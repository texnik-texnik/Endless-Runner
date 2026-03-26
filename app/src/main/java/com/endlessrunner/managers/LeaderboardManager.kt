package com.endlessrunner.managers

import android.util.Log
import com.endlessrunner.data.repository.GameRepository
import com.endlessrunner.domain.model.LeaderboardEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Менеджер таблицы лидеров.
 * Отвечает за добавление, получение и управление записями лидеров.
 *
 * @param repository Репозиторий для работы с данными
 */
class LeaderboardManager(
    private val repository: GameRepository
) {
    companion object {
        private const val TAG = "LeaderboardManager"
        private const val DEFAULT_LIMIT = 10
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    /**
     * Поток таблицы лидеров (топ записей).
     *
     * @param limit Количество записей (по умолчанию 10)
     */
    fun getLeaderboard(limit: Int = DEFAULT_LIMIT): Flow<List<LeaderboardEntry>> =
        repository.getLeaderboard(limit)

    /**
     * Получение таблицы лидеров (suspend версия).
     */
    suspend fun getLeaderboardOnce(limit: Int = DEFAULT_LIMIT): List<LeaderboardEntry> {
        return try {
            repository.getLeaderboard(limit).first()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении таблицы лидеров", e)
            emptyList()
        }
    }

    /**
     * Добавление записи в таблицу лидеров.
     *
     * @param score Набранные очки
     * @param coins Собранные монеты
     * @param distance Пройденная дистанция
     * @param playerName Имя игрока
     * @return ID записи или null при ошибке
     */
    suspend fun addEntry(
        score: Int,
        coins: Int = 0,
        distance: Float = 0f,
        playerName: String
    ): String? {
        return try {
            // Проверяем, qualifies ли счёт для попадания в таблицу
            val qualifies = checkScoreQualifies(score)
            
            if (qualifies) {
                val entry = LeaderboardEntry(
                    entryId = LeaderboardEntry.generateId(),
                    playerName = playerName,
                    score = score,
                    coins = coins,
                    distance = distance,
                    timestamp = System.currentTimeMillis()
                )

                repository.addToLeaderboard(entry)
                Log.d(TAG, "Запись добавлена в таблицу лидеров: $playerName - $score очков")
                entry.entryId
            } else {
                Log.d(TAG, "Счёт не прошёл в таблицу лидеров: $score")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при добавлении записи", e)
            null
        }
    }

    /**
     * Добавление записи из готового объекта.
     */
    suspend fun addEntry(entry: LeaderboardEntry): Boolean {
        return try {
            repository.addToLeaderboard(entry)
            Log.d(TAG, "Запись добавлена: ${entry.playerName} - ${entry.score}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при добавлении записи", e)
            false
        }
    }

    /**
     * Получение ранга игрока по очкам.
     *
     * @param score Очки игрока
     * @return Ранг или -1 при ошибке
     */
    suspend fun getPlayerRank(score: Int): Int {
        return try {
            repository.getPlayerRank(score)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении ранга", e)
            -1
        }
    }

    /**
     * Получение записей конкретного игрока.
     */
    fun getPlayerEntries(playerName: String): Flow<List<LeaderboardEntry>> =
        kotlinx.coroutines.flow.flow {
            emit(repository.getLeaderboard(100).first().filter { it.playerName == playerName })
        }

    /**
     * Получение лучшей записи игрока.
     */
    suspend fun getPlayerBestEntry(playerName: String): LeaderboardEntry? {
        return try {
            val entries = getPlayerEntries(playerName).first()
            entries.maxByOrNull { it.score }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении лучшей записи", e)
            null
        }
    }

    /**
     * Проверка, проходит ли счёт в таблицу лидеров.
     */
    private suspend fun checkScoreQualifies(score: Int): Boolean {
        return try {
            val leaderboard = repository.getLeaderboard(100).first()
            
            // Если меньше 100 записей, то проходит
            if (leaderboard.size < 100) return true
            
            // Иначе проверяем, больше ли счёт минимального в топ-100
            val minScore = leaderboard.minOfOrNull { it.score } ?: 0
            score > minScore
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при проверке счёта", e)
            true // При ошибке разрешаем добавление
        }
    }

    /**
     * Удаление записи из таблицы лидеров.
     */
    suspend fun deleteEntry(entryId: String): Boolean {
        return try {
            repository.deleteFromLeaderboard(entryId)
            Log.d(TAG, "Запись удалена: $entryId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при удалении записи", e)
            false
        }
    }

    /**
     * Очистка таблицы лидеров.
     */
    suspend fun clearLeaderboard(): Boolean {
        return try {
            repository.clearLeaderboard()
            Log.d(TAG, "Таблица лидеров очищена")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при очистке таблицы лидеров", e)
            false
        }
    }

    /**
     * Получение количества записей в таблице лидеров.
     */
    suspend fun getEntryCount(): Int {
        return try {
            // Room не предоставляет прямой метод для count, используем размер списка
            repository.getLeaderboard(1000).first().size
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении количества записей", e)
            0
        }
    }

    /**
     * Получение минимального счёта в топ-N.
     */
    suspend fun getMinScoreInTop(top: Int = 10): Int {
        return try {
            val leaderboard = repository.getLeaderboard(top).first()
            leaderboard.minOfOrNull { it.score } ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении минимального счёта", e)
            0
        }
    }

    /**
     * Проверка, является ли запись личной лучшей.
     */
    suspend fun isPersonalBest(score: Int, playerName: String): Boolean {
        return try {
            val bestEntry = getPlayerBestEntry(playerName)
            bestEntry == null || score > bestEntry.score
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при проверке личного рекорда", e)
            true
        }
    }

    /**
     * Освобождение ресурсов.
     */
    fun dispose() {
        scope.cancel()
        Log.d(TAG, "LeaderboardManager освобождён")
    }
}

/**
 * Extension функция для создания Flow.
 */
private inline fun <T> flow(crossinline builder: suspend kotlinx.coroutines.flow.FlowCollector<T>.() -> Unit): Flow<T> =
    kotlinx.coroutines.flow.flow(builder)
