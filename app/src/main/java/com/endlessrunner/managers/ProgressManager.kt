package com.endlessrunner.managers

import android.util.Log
import com.endlessrunner.data.repository.GameRepository
import com.endlessrunner.domain.model.PlayerProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Менеджер отслеживания прогресса игрока.
 * Отвечает за накопление статистики и обновление достижений.
 *
 * @param repository Репозиторий для работы с данными
 * @param playerId ID игрока (по умолчанию "default_player")
 */
class ProgressManager(
    private val repository: GameRepository,
    private val playerId: String = "default_player"
) {
    companion object {
        private const val TAG = "ProgressManager"
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var currentProgress: PlayerProgress? = null

    // Временные счётчики для текущей сессии
    private var sessionCoins: Int = 0
    private var sessionScore: Int = 0
    private var sessionDistance: Float = 0f
    private var sessionEnemiesDefeated: Int = 0
    private var sessionPlayTime: Long = 0L

    /**
     * Поток прогресса игрока.
     */
    fun getPlayerProgress(): Flow<PlayerProgress?> = repository.getPlayerProgress(playerId)

    /**
     * Получение текущего прогресса (suspend версия).
     */
    suspend fun getCurrentProgress(): PlayerProgress? {
        return repository.getPlayerProgress(playerId).first().also {
            currentProgress = it
        }
    }

    /**
     * Поток общего количества монет.
     */
    fun getTotalCoins(): Flow<Int> = repository.getPlayerProgress(playerId)
        .mapNotNull { it?.totalCoins }

    /**
     * Поток лучшего результата.
     */
    fun getBestScore(): Flow<Int> = repository.getPlayerProgress(playerId)
        .mapNotNull { it?.bestScore }

    /**
     * Поток общей дистанции.
     */
    fun getTotalDistance(): Flow<Float> = repository.getPlayerProgress(playerId)
        .mapNotNull { it?.totalDistance }

    /**
     * Поток количества сыгранных игр.
     */
    fun getTotalGamesPlayed(): Flow<Int> = repository.getPlayerProgress(playerId)
        .mapNotNull { it?.totalGamesPlayed }

    /**
     * Инициализация прогресса (создание записи если не существует).
     */
    suspend fun initializeProgress() {
        val progress = getCurrentProgress()
        if (progress == null) {
            val newProgress = PlayerProgress(playerId = playerId)
            repository.savePlayerProgress(newProgress)
            currentProgress = newProgress
            Log.d(TAG, "Прогресс инициализирован для игрока: $playerId")
        } else {
            currentProgress = progress
            Log.d(TAG, "Прогресс загружен: ${progress.totalCoins} монет, рекорд: ${progress.bestScore}")
        }
    }

    /**
     * Запись собранной монеты.
     *
     * @param value Количество монет (по умолчанию 1)
     */
    suspend fun recordCoin(value: Int = 1) {
        sessionCoins += value
        updateProgress(coins = value, coinsCollected = value)
        Log.d(TAG, "Монета записана: +$value (сессия: $sessionCoins)")
    }

    /**
     * Запись очков.
     *
     * @param points Количество очков
     */
    suspend fun recordScore(points: Int) {
        sessionScore += points
        updateProgress(bestScore = points)
        Log.d(TAG, "Очки записаны: +$points (сессия: $sessionScore)")
    }

    /**
     * Запись дистанции.
     *
     * @param amount Пройденная дистанция в метрах
     */
    suspend fun recordDistance(amount: Float) {
        sessionDistance += amount
        updateProgress(distance = amount)
        Log.d(TAG, "Дистанция записана: +$amount (сессия: $sessionDistance)")
    }

    /**
     * Запись уничтоженного врага.
     */
    suspend fun recordEnemyDefeated() {
        sessionEnemiesDefeated++
        updateProgress(enemiesDefeated = 1)
        Log.d(TAG, "Враг уничтожен (сессия: $sessionEnemiesDefeated)")
    }

    /**
     * Запись завершённой игры.
     *
     * @param isWin Флаг победы
     * @param finalScore Финальный счёт
     * @param finalCoins Финальное количество монет
     * @param finalDistance Финальная дистанция
     * @param playTime Время игры в секундах
     */
    suspend fun recordGameCompleted(
        isWin: Boolean = false,
        finalScore: Int = 0,
        finalCoins: Int = 0,
        finalDistance: Float = 0f,
        playTime: Long = 0L
    ) {
        try {
            // Обновляем прогресс с финальными значениями
            repository.updatePlayerProgress(
                playerId = playerId,
                coins = 0,
                bestScore = finalScore,
                distance = finalDistance,
                gamesPlayed = 1,
                gamesWon = if (isWin) 1 else 0,
                enemiesDefeated = sessionEnemiesDefeated,
                coinsCollected = finalCoins,
                playTime = playTime
            )

            // Сброс сессионных счётчиков
            resetSessionCounters()

            Log.d(TAG, "Игра завершена: score=$finalScore, coins=$finalCoins, distance=$finalDistance, win=$isWin")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при записи завершённой игры", e)
        }
    }

    /**
     * Разблокировка скина.
     *
     * @param skinId ID скина
     */
    suspend fun unlockSkin(skinId: String) {
        try {
            val progress = getCurrentProgress()
            val currentSkins = progress?.unlockedSkins ?: emptySet()
            if (skinId !in currentSkins) {
                val newSkins = currentSkins + skinId
                repository.updatePlayerProgress(
                    playerId = playerId,
                    currentSkin = progress?.currentSkin,
                    unlockedSkins = newSkins
                )
                Log.d(TAG, "Скин разблокирован: $skinId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при разблокировке скина", e)
        }
    }

    /**
     * Установка текущего скина.
     *
     * @param skinId ID скина
     */
    suspend fun setCurrentSkin(skinId: String): Boolean {
        return try {
            val progress = getCurrentProgress()
            if (progress?.isSkinUnlocked(skinId) == true || skinId == "skin_default") {
                repository.updatePlayerProgress(
                    playerId = playerId,
                    currentSkin = skinId
                )
                Log.d(TAG, "Скин установлен: $skinId")
                true
            } else {
                Log.w(TAG, "Скин не разблокирован: $skinId")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при установке скина", e)
            false
        }
    }

    /**
     * Получение текущего скина.
     */
    suspend fun getCurrentSkin(): String {
        return repository.getCurrentSkin()
    }

    /**
     * Получение разблокированных скинов.
     */
    suspend fun getUnlockedSkins(): Set<String> {
        return getCurrentProgress()?.unlockedSkins ?: emptySet()
    }

    /**
     * Проверка, разблокирован ли скин.
     */
    suspend fun isSkinUnlocked(skinId: String): Boolean {
        return getCurrentProgress()?.isSkinUnlocked(skinId) ?: false
    }

    /**
     * Добавление времени игры.
     *
     * @param seconds Время в секундах
     */
    suspend fun addPlayTime(seconds: Long) {
        sessionPlayTime += seconds
        updateProgress(playTime = seconds)
    }

    /**
     * Обновление прогресса в репозитории.
     */
    private suspend fun updateProgress(
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
    ) {
        scope.launch {
            try {
                repository.updatePlayerProgress(
                    playerId = playerId,
                    coins = coins,
                    bestScore = bestScore,
                    distance = distance,
                    gamesPlayed = gamesPlayed,
                    gamesWon = gamesWon,
                    enemiesDefeated = enemiesDefeated,
                    coinsCollected = coinsCollected,
                    playTime = playTime,
                    currentSkin = currentSkin,
                    unlockedSkins = unlockedSkins
                )
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при обновлении прогресса", e)
            }
        }
    }

    /**
     * Сброс сессионных счётчиков.
     */
    private fun resetSessionCounters() {
        sessionCoins = 0
        sessionScore = 0
        sessionDistance = 0f
        sessionEnemiesDefeated = 0
        sessionPlayTime = 0
    }

    /**
     * Получение статистики сессии.
     */
    fun getSessionStats(): SessionStats {
        return SessionStats(
            coins = sessionCoins,
            score = sessionScore,
            distance = sessionDistance,
            enemiesDefeated = sessionEnemiesDefeated,
            playTime = sessionPlayTime
        )
    }

    /**
     * Освобождение ресурсов.
     */
    fun dispose() {
        scope.cancel()
        resetSessionCounters()
        currentProgress = null
        Log.d(TAG, "ProgressManager освобождён")
    }

    /**
     * Data class для статистики сессии.
     */
    data class SessionStats(
        val coins: Int,
        val score: Int,
        val distance: Float,
        val enemiesDefeated: Int,
        val playTime: Long
    )
}

/**
 * Extension функция для mapNotNull.
 */
private inline fun <T, R> Flow<T>.mapNotNull(crossinline transform: suspend (T) -> R?): Flow<R> {
    return kotlinx.coroutines.flow.map { value ->
        transform(value)
    }.kotlinx.coroutines.flow.filterNotNull()
}
