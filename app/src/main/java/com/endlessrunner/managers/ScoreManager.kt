package com.endlessrunner.managers

import android.util.Log
import com.endlessrunner.data.repository.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Менеджер счёта и комбо.
 * Отвечает за подсчёт очков, комбо и множителей.
 * Интегрирован с ProgressManager для отслеживания прогресса.
 *
 * @param progressManager Менеджер прогресса для записи очков
 */
class ScoreManager(
    private val progressManager: ProgressManager? = null
) {

    companion object {
        private const val TAG = "ScoreManager"

        /** Базовые очки за монету */
        const val BASE_COIN_SCORE = 10

        /** Очки за препятствие */
        const val OBSTACLE_SCORE = 50

        /** Множитель за каждые 10 комбо */
        const val COMBO_MULTIPLIER_STEP = 10

        /** Максимальный множитель */
        const val MAX_COMBO_MULTIPLIER = 5
    }

    /** Текущий счёт */
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    /** Текущее комбо */
    private val _combo = MutableStateFlow(0)
    val combo: StateFlow<Int> = _combo.asStateFlow()

    /** Текущий множитель */
    private val _multiplier = MutableStateFlow(1f)
    val multiplier: StateFlow<Float> = _multiplier.asStateFlow()

    /** Рекорд */
    private val _highScore = MutableStateFlow(0)
    val highScore: StateFlow<Int> = _highScore.asStateFlow()

    /** Всего собрано монет */
    var totalCoins: Int = 0
        private set

    /** Всего пройдено препятствий */
    var totalObstacles: Int = 0
        private set

    /** Максимальное комбо за сессию */
    var maxCombo: Int = 0
        private set

    /** Callback на изменение счёта */
    var onScoreChanged: ((Int) -> Unit)? = null

    /** Callback на изменение комбо */
    var onComboChanged: ((Int) -> Unit)? = null

    /** Callback на новый рекорд */
    var onNewHighScore: ((Int) -> Unit)? = null

    /** CoroutineScope для операций */
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    init {
        Log.d(TAG, "ScoreManager инициализирован")
    }

    // ============================================================================
    // ДОБАВЛЕНИЕ ОЧКОВ
    // ============================================================================

    /**
     * Добавление очков.
     *
     * @param points Базовое количество очков
     * @param applyMultiplier Применить множитель комбо
     */
    fun addScore(points: Int, applyMultiplier: Boolean = true) {
        val finalPoints = if (applyMultiplier) {
            (points * _multiplier.value).toInt()
        } else {
            points
        }

        val newScore = _score.value + finalPoints
        _score.value = newScore

        Log.d(TAG, "Добавлено очков: $finalPoints (множитель=${_multiplier.value}), всего: $newScore")

        onScoreChanged?.invoke(newScore)

        // Проверка рекорда
        if (newScore > _highScore.value) {
            _highScore.value = newScore
            onNewHighScore?.invoke(newScore)
        }
    }

    /**
     * Добавление очков за монету.
     *
     * @param coinValue Стоимость монеты
     */
    fun addCoinScore(coinValue: Int = BASE_COIN_SCORE) {
        totalCoins++
        addScore(coinValue, applyMultiplier = true)

        // Увеличение комбо
        increaseCombo()
    }

    /**
     * Добавление очков за препятствие.
     */
    fun addObstacleScore() {
        totalObstacles++
        addScore(OBSTACLE_SCORE, applyMultiplier = false)
    }

    /**
     * Добавление очков за время.
     *
     * @param seconds Прошедшие секунды
     */
    fun addTimeScore(seconds: Int) {
        val points = seconds / 10 // 1 очко за 10 секунд
        if (points > 0) {
            addScore(points, applyMultiplier = false)
        }
    }

    // ============================================================================
    // КОМБО
    // ============================================================================

    /**
     * Увеличение комбо.
     */
    fun increaseCombo() {
        _combo.value++

        if (_combo.value > maxCombo) {
            maxCombo = _combo.value
        }

        // Обновление множителя
        updateMultiplier()

        Log.d(TAG, "Комбо: ${_combo.value}, множитель: ${_multiplier.value}x")

        onComboChanged?.invoke(_combo.value)
    }

    /**
     * Сброс комбо.
     */
    fun resetCombo() {
        if (_combo.value > 0) {
            Log.d(TAG, "Комбо сброшено: ${_combo.value}")
        }

        _combo.value = 0
        _multiplier.value = 1f

        onComboChanged?.invoke(0)
    }

    /**
     * Обновление множителя на основе комбо.
     */
    private fun updateMultiplier() {
        val comboValue = _combo.value
        val multiplierSteps = comboValue / COMBO_MULTIPLIER_STEP
        _multiplier.value = (1f + multiplierSteps * 0.5f).coerceAtMost(MAX_COMBO_MULTIPLIER.toFloat())
    }

    /**
     * Получение текущего множителя.
     */
    fun getCurrentMultiplier(): Float = _multiplier.value

    /**
     * Проверка, активно ли комбо.
     */
    fun hasCombo(): Boolean = _combo.value > 0

    // ============================================================================
    // РЕКОРДЫ
    // ============================================================================

    /**
     * Загрузка рекорда.
     *
     * @param score Рекорд для загрузки
     */
    fun loadHighScore(score: Int) {
        _highScore.value = score
        Log.d(TAG, "Рекорд загружен: $score")
    }

    /**
     * Сохранение рекорда в репозиторий.
     */
    fun saveHighScore() {
        scope.launch {
            try {
                progressManager?.let { manager ->
                    // Получаем текущий прогресс и обновляем bestScore
                    val currentProgress = manager.getCurrentProgress()
                    if (_highScore.value > (currentProgress?.bestScore ?: 0)) {
                        // ProgressManager автоматически обновит bestScore через recordScore
                        manager.recordScore(_highScore.value)
                    }
                }
                Log.d(TAG, "Рекорд сохранён: ${_highScore.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при сохранении рекорда", e)
            }
        }
    }

    // ============================================================================
    // СБРОС
    // ============================================================================

    /**
     * Сброс для новой игры.
     */
    fun reset() {
        Log.d(TAG, "Сброс ScoreManager")

        _score.value = 0
        _combo.value = 0
        _multiplier.value = 1f
        totalCoins = 0
        totalObstacles = 0
        // maxCombo не сбрасываем - это статистика сессии

        onScoreChanged?.invoke(0)
        onComboChanged?.invoke(0)
    }

    /**
     * Полная очистка.
     */
    fun clear() {
        reset()
        maxCombo = 0
        _highScore.value = 0
    }

    /**
     * Освобождение ресурсов.
     */
    fun dispose() {
        saveHighScore()
        scope.cancel()
        onScoreChanged = null
        onComboChanged = null
        onNewHighScore = null
    }

    // ============================================================================
    // СТАТИСТИКА
    // ============================================================================

    /**
     * Получение статистики.
     */
    fun getStats(): ScoreStats {
        return ScoreStats(
            score = _score.value,
            combo = _combo.value,
            multiplier = _multiplier.value,
            highScore = _highScore.value,
            totalCoins = totalCoins,
            totalObstacles = totalObstacles,
            maxCombo = maxCombo
        )
    }

    /**
     * Data class для статистики.
     */
    data class ScoreStats(
        val score: Int,
        val combo: Int,
        val multiplier: Float,
        val highScore: Int,
        val totalCoins: Int,
        val totalObstacles: Int,
        val maxCombo: Int
    ) {
        /** Очки без учёта множителя */
        val baseScore: Int
            get() = (score / multiplier).toInt()

        /** Очки от комбо */
        val comboBonus: Int
            get() = score - baseScore
    }
}

/**
 * Extension функция для получения строкового представления множителя.
 */
fun ScoreManager.getMultiplierString(): String {
    val mult = getCurrentMultiplier()
    return if (mult > 1f) {
        "${mult}x"
    } else {
        ""
    }
}

/**
 * Extension функция для проверки максимального комбо.
 */
fun ScoreManager.isMaxCombo(): Boolean {
    return combo.value >= 50
}
