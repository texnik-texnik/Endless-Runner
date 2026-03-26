package com.endlessrunner.managers

import android.util.Log
import com.endlessrunner.data.repository.GameRepository
import com.endlessrunner.domain.model.GameSave
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Менеджер управлений сохранениями игр.
 * Отвечает за создание, загрузку, удаление и автосохранение игр.
 *
 * @param repository Репозиторий для работы с данными
 * @param autoSaveInterval Интервал автосохранения в секундах (по умолчанию 30)
 */
class SaveManager(
    private val repository: GameRepository,
    private val autoSaveInterval: Long = 30_000L // 30 секунд
) {
    companion object {
        private const val TAG = "SaveManager"
        private const val MAX_SAVE_SLOTS = 5
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var autoSaveJob: Job? = null
    private var currentGameSave: GameSave? = null

    /**
     * Поток всех сохранений.
     */
    fun getSaveSlots(): Flow<List<GameSave>> = repository.getGameSaves()

    /**
     * Получение активных (незавершённых) сохранений.
     */
    suspend fun getActiveSaves(): List<GameSave> {
        return repository.getGameSaves().first().filter { it.isActive }
    }

    /**
     * Сохранение текущей игры.
     *
     * @param score Текущий счёт
     * @param coins Собранные монеты
     * @param distance Пройденная дистанция
     * @param isCompleted Флаг завершения игры
     * @return ID сохранения или null при ошибке
     */
    suspend fun saveGame(
        score: Int,
        coins: Int,
        distance: Float,
        isCompleted: Boolean = false
    ): String? {
        return try {
            val saveId = generateSaveId()
            val gameSave = GameSave(
                saveId = saveId,
                score = score,
                coins = coins,
                distance = distance,
                timestamp = System.currentTimeMillis(),
                isCompleted = isCompleted
            )

            repository.saveGame(gameSave)
            currentGameSave = gameSave

            Log.d(TAG, "Игра сохранена: $saveId (score=$score, coins=$coins, distance=$distance)")
            saveId
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при сохранении игры", e)
            null
        }
    }

    /**
     * Сохранение текущей игры с использованием существующего ID.
     */
    suspend fun updateCurrentGame(
        score: Int,
        coins: Int,
        distance: Float
    ): Boolean {
        val currentSave = currentGameSave ?: return false

        return try {
            val updatedSave = currentSave.copy(
                score = score,
                coins = coins,
                distance = distance,
                timestamp = System.currentTimeMillis()
            )
            repository.saveGame(updatedSave)
            currentGameSave = updatedSave
            Log.d(TAG, "Текущая игра обновлена: score=$score, coins=$coins, distance=$distance")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при обновлении игры", e)
            false
        }
    }

    /**
     * Загрузка сохранения по ID.
     */
    suspend fun loadGame(saveId: String): GameSave? {
        return try {
            val save = repository.getGameSave(saveId)
            if (save != null) {
                currentGameSave = save
                Log.d(TAG, "Игра загружена: $saveId")
            }
            save
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при загрузке игры", e)
            null
        }
    }

    /**
     * Загрузка последнего активного сохранения.
     */
    suspend fun loadLastActiveGame(): GameSave? {
        return try {
            val saves = getActiveSaves()
            val lastSave = saves.maxByOrNull { it.timestamp }
            if (lastSave != null) {
                currentGameSave = lastSave
                Log.d(TAG, "Последняя активная игра загружена: ${lastSave.saveId}")
            }
            lastSave
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при загрузке последней игры", e)
            null
        }
    }

    /**
     * Удаление сохранения.
     */
    suspend fun deleteGame(saveId: String): Boolean {
        return try {
            repository.deleteGame(saveId)
            if (currentGameSave?.saveId == saveId) {
                currentGameSave = null
            }
            Log.d(TAG, "Игра удалена: $saveId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при удалении игры", e)
            false
        }
    }

    /**
     * Удаление всех завершённых сохранений.
     */
    suspend fun deleteCompletedSaves(): Int {
        return try {
            val saves = repository.getGameSaves().first()
            val completedCount = saves.count { it.isCompleted }
            
            for (save in saves.filter { it.isCompleted }) {
                repository.deleteGame(save.saveId)
            }
            
            Log.d(TAG, "Удалено завершённых сохранений: $completedCount")
            completedCount
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при удалении завершённых сохранений", e)
            0
        }
    }

    /**
     * Очистка всех сохранений.
     */
    suspend fun clearAllSaves(): Boolean {
        return try {
            repository.deleteAllSaves()
            currentGameSave = null
            stopAutoSave()
            Log.d(TAG, "Все сохранения очищены")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при очистке сохранений", e)
            false
        }
    }

    /**
     * Запуск автосохранения.
     */
    fun startAutoSave() {
        stopAutoSave()
        autoSaveJob = scope.launch {
            while (true) {
                delay(autoSaveInterval)
                autoSave()
            }
        }
        Log.d(TAG, "Автосохранение запущено (интервал: ${autoSaveInterval / 1000}с)")
    }

    /**
     * Остановка автосохранения.
     */
    fun stopAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = null
        Log.d(TAG, "Автосохранение остановлено")
    }

    /**
     * Автосохранение текущей игры.
     */
    private suspend fun autoSave() {
        val save = currentGameSave ?: return
        if (save.isCompleted) return

        try {
            // Автосохранение только если есть прогресс
            if (save.score > 0 || save.distance > 0) {
                val updatedSave = save.copy(timestamp = System.currentTimeMillis())
                repository.saveGame(updatedSave)
                currentGameSave = updatedSave
                Log.d(TAG, "Автосохранение: score=${save.score}, distance=${save.distance}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка автосохранения", e)
        }
    }

    /**
     * Завершение текущей игры.
     */
    suspend fun completeCurrentGame(): Boolean {
        val save = currentGameSave ?: return false
        return try {
            val completedSave = save.copy(
                isCompleted = true,
                timestamp = System.currentTimeMillis()
            )
            repository.saveGame(completedSave)
            currentGameSave = null
            Log.d(TAG, "Игра завершена: ${save.saveId}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при завершении игры", e)
            false
        }
    }

    /**
     * Получение текущего сохранения.
     */
    fun getCurrentSave(): GameSave? = currentGameSave

    /**
     * Проверка, есть ли активное сохранение.
     */
    fun hasActiveSave(): Boolean = currentGameSave?.isActive == true

    /**
     * Получение количества слотов сохранений.
     */
    suspend fun getSaveCount(): Int {
        return try {
            repository.getGameSaves().first().size
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении количества сохранений", e)
            0
        }
    }

    /**
     * Проверка, заполнены ли все слоты сохранений.
     */
    suspend fun isSaveSlotsFull(): Boolean {
        return getSaveCount() >= MAX_SAVE_SLOTS
    }

    /**
     * Освобождение ресурсов.
     */
    fun dispose() {
        stopAutoSave()
        scope.cancel()
        currentGameSave = null
        Log.d(TAG, "SaveManager освобождён")
    }

    // ============================================================================
    // МЕТОДЫ ДЛЯ SOUND MANAGER (совместимость с data.SaveManager)
    // ============================================================================

    /**
     * Получение статуса включения звуков.
     */
    suspend fun isSfxEnabled(): Boolean {
        return true // Заглушка, реальная реализация через SettingsDataStore
    }

    /**
     * Получение статуса включения музыки.
     */
    suspend fun isMusicEnabled(): Boolean {
        return true // Заглушка, реальная реализация через SettingsDataStore
    }

    /**
     * Получение статуса включения вибрации.
     */
    suspend fun isVibrationEnabled(): Boolean {
        return true // Заглушка, реальная реализация через SettingsDataStore
    }

    /**
     * Сохранение настройки звуков.
     */
    suspend fun setSfxEnabled(enabled: Boolean) {
        // Заглушка, реальная реализация через SettingsDataStore
    }

    /**
     * Сохранение настройки музыки.
     */
    suspend fun setMusicEnabled(enabled: Boolean) {
        // Заглушка, реальная реализация через SettingsDataStore
    }

    /**
     * Сохранение настройки вибрации.
     */
    suspend fun setVibrationEnabled(enabled: Boolean) {
        // Заглушка, реальная реализация через SettingsDataStore
    }

    // ============================================================================
    // МЕТОДЫ ДЛЯ VIEWMODELS (совместимость с data.SaveManager)
    // ============================================================================

    /**
     * Загрузка всех данных игрока.
     * Заглушка - возвращает данные по умолчанию.
     */
    suspend fun loadPlayerData(): com.endlessrunner.data.PlayerData {
        return com.endlessrunner.data.PlayerData()
    }

    /**
     * Сохранение всех данных игрока.
     * Заглушка - не делает ничего.
     */
    suspend fun savePlayerData(playerData: com.endlessrunner.data.PlayerData) {
        // Заглушка, реальная реализация через SettingsDataStore
    }

    /**
     * Генерация уникального ID для сохранения.
     */
    private fun generateSaveId(): String {
        return "save_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
}
