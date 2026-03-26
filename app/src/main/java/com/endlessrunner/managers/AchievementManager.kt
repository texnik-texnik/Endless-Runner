package com.endlessrunner.managers

import android.content.Context
import android.util.Log
import com.endlessrunner.audio.AudioManager
import com.endlessrunner.audio.SoundLibrary
import com.endlessrunner.data.repository.GameRepository
import com.endlessrunner.domain.model.Achievement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Менеджер достижений.
 * Отвечает за отслеживание и разблокировку достижений.
 *
 * @param context Контекст приложения
 * @param repository Репозиторий для работы с данными
 * @param progressManager Менеджер прогресса для проверки условий
 */
class AchievementManager(
    private val context: Context,
    private val repository: GameRepository,
    private val progressManager: ProgressManager
) {
    companion object {
        private const val TAG = "AchievementManager"
    }

    /**
     * AudioManager для воспроизведения звуков.
     */
    private val audioManager: AudioManager by lazy {
        AudioManager.getInstance(context)
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val unlockedAchievements = mutableSetOf<String>()

    /**
     * Поток всех достижений.
     */
    fun getAchievements(): Flow<List<Achievement>> = repository.getAchievements()

    /**
     * Поток разблокированных достижений.
     */
    fun getUnlockedAchievements(): Flow<List<Achievement>> = repository.getUnlockedAchievements()

    /**
     * Поток конкретного достижения.
     */
    fun getAchievement(id: String): Flow<Achievement?> =
        kotlinx.coroutines.flow.flow {
            emit(repository.getAchievement(id))
        }

    /**
     * Инициализация достижений.
     */
    suspend fun initialize() {
        repository.initializeAchievements()
        loadUnlockedAchievements()
        Log.d(TAG, "AchievementManager инициализирован")
    }

    /**
     * Загрузка разблокированных достижений.
     */
    private suspend fun loadUnlockedAchievements() {
        try {
            val unlocked = repository.getUnlockedAchievements().first()
            unlockedAchievements.clear()
            unlockedAchievements.addAll(unlocked.map { it.id })
            Log.d(TAG, "Загружено разблокированных достижений: ${unlockedAchievements.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при загрузке достижений", e)
        }
    }

    /**
     * Проверка достижений на основе прогресса игрока.
     * Вызывается после значимых событий в игре.
     *
     * @param progress Текущий прогресс игрока
     */
    suspend fun checkAchievements(progress: PlayerProgress) {
        scope.launch {
            try {
                // Монеты
                checkAchievement("FIRST_COIN", progress.coinsCollected >= 1, progress.coinsCollected)
                checkAchievement("COIN_COLLECTOR", progress.coinsCollected >= 100, progress.coinsCollected, 100)
                checkAchievement("RICH", progress.coinsCollected >= 1000, progress.coinsCollected, 1000)

                // Игры
                checkAchievement("FIRST_GAME", progress.totalGamesPlayed >= 1, progress.totalGamesPlayed)
                checkAchievement("VETERAN", progress.totalGamesPlayed >= 100, progress.totalGamesPlayed, 100)

                // Очки
                checkAchievement("HIGH_SCORE", progress.bestScore >= 1000, progress.bestScore, 1000)
                checkAchievement("LEGENDARY_SCORE", progress.bestScore >= 10000, progress.bestScore, 10000)

                // Дистанция
                checkAchievement("MARATHON", progress.totalDistance >= 10000, progress.totalDistance.toInt(), 10000)
                checkAchievement("ULTRA_MARATHON", progress.totalDistance >= 50000, progress.totalDistance.toInt(), 50000)

                // Враги
                checkAchievement("ENEMY_SLAYER", progress.enemiesDefeated >= 100, progress.enemiesDefeated, 100)
                checkAchievement("ENEMY_HUNTER", progress.enemiesDefeated >= 500, progress.enemiesDefeated, 500)

                // Комбо (проверяется через ScoreManager)
                // PERFECT и SPEEDRUNER проверяются отдельно
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при проверке достижений", e)
            }
        }
    }

    /**
     * Проверка конкретного достижения.
     *
     * @param achievementId ID достижения
     * @param isUnlocked Флаг разблокировки
     * @param progress Текущий прогресс
     * @param maxProgress Максимальный прогресс (если известен)
     */
    private suspend fun checkAchievement(
        achievementId: String,
        isUnlocked: Boolean,
        progress: Int,
        maxProgress: Int? = null
    ) {
        // Если уже разблокировано, пропускаем
        if (unlockedAchievements.contains(achievementId)) return

        val achievement = repository.getAchievement(achievementId) ?: return

        // Обновляем прогресс
        val targetProgress = maxProgress ?: achievement.maxProgress
        val normalizedProgress = if (targetProgress > 0) {
            (progress * achievement.maxProgress / targetProgress).coerceAtMost(achievement.maxProgress)
        } else {
            progress
        }

        repository.updateAchievementProgress(achievementId, normalizedProgress)

        // Разблокируем если условие выполнено
        if (isUnlocked && achievementId !in unlockedAchievements) {
            unlockAchievement(achievementId)
        }
    }

    /**
     * Разблокировка достижения.
     *
     * @param achievementId ID достижения
     */
    suspend fun unlockAchievement(achievementId: String) {
        if (unlockedAchievements.contains(achievementId)) {
            Log.d(TAG, "Достижение уже разблокировано: $achievementId")
            return
        }

        try {
            repository.unlockAchievement(achievementId)
            unlockedAchievements.add(achievementId)

            val achievement = repository.getAchievement(achievementId)
            Log.i(TAG, "🏆 Достижение разблокировано: ${achievement?.title ?: achievementId}")

            // Воспроизведение звука разблокировки достижения
            audioManager.playSfx(SoundLibrary.ACHIEVEMENT_UNLOCK)

            // Здесь можно вызвать callback для показа уведомления
            onAchievementUnlocked?.invoke(achievementId)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при разблокировке достижения", e)
        }
    }

    /**
     * Обновление прогресса достижения.
     *
     * @param achievementId ID достижения
     * @param progress Новый прогресс
     */
    suspend fun updateProgress(achievementId: String, progress: Int) {
        try {
            repository.updateAchievementProgress(achievementId, progress)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при обновлении прогресса достижения", e)
        }
    }

    /**
     * Проверка достижения "Идеальная игра".
     *
     * @param damageTaken Полученный урон
     */
    suspend fun checkPerfectGame(damageTaken: Int) {
        if (damageTaken == 0 && !unlockedAchievements.contains("PERFECT")) {
            unlockAchievement("PERFECT")
        }
    }

    /**
     * Проверка достижения "Спидраннер".
     *
     * @param distance Пройденная дистанция в метрах
     * @param timeSeconds Время в секундах
     */
    suspend fun checkSpeedrun(distance: Float, timeSeconds: Long) {
        if (distance >= 1000 && timeSeconds <= 60 && !unlockedAchievements.contains("SPEEDRUNER")) {
            unlockAchievement("SPEEDRUNER")
        }
    }

    /**
     * Проверка достижения "Мастер комбо".
     *
     * @param maxCombo Максимальное комбо
     */
    suspend fun checkComboMaster(maxCombo: Int) {
        if (maxCombo >= 50 && !unlockedAchievements.contains("COMBO_MASTER")) {
            unlockAchievement("COMBO_MASTER")
        }
    }

    /**
     * Получение прогресса конкретного достижения.
     */
    suspend fun getAchievementProgress(id: String): Int {
        return repository.getAchievement(id)?.progress ?: 0
    }

    /**
     * Проверка, разблокировано ли достижение.
     */
    fun isUnlocked(achievementId: String): Boolean = unlockedAchievements.contains(achievementId)

    /**
     * Получение процента разблокированных достижений.
     */
    suspend fun getUnlockPercentage(): Float {
        return try {
            repository.getAchievements().first().let { all ->
                if (all.isEmpty()) 0f
                else unlockedAchievements.size * 100f / all.size
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении процента разблокировки", e)
            0f
        }
    }

    /**
     * Получение количества разблокированных достижений.
     */
    fun getUnlockedCount(): Int = unlockedAchievements.size

    /**
     * Получение общего количества достижений.
     */
    suspend fun getTotalCount(): Int {
        return repository.getAchievements().first().size
    }

    /**
     * Callback на разблокировку достижения.
     */
    var onAchievementUnlocked: ((String) -> Unit)? = null

    /**
     * Сброс всех достижений (для отладки).
     */
    suspend fun resetAllAchievements() {
        try {
            unlockedAchievements.clear()
            // Пересоздаём достижения с начальным прогрессом
            repository.initializeAchievements()
            Log.d(TAG, "Все достижения сброшены")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при сбросе достижений", e)
        }
    }

    /**
     * Освобождение ресурсов.
     */
    fun dispose() {
        scope.cancel()
        unlockedAchievements.clear()
        onAchievementUnlocked = null
        Log.d(TAG, "AchievementManager освобождён")
    }
}
