package com.endlessrunner.managers

import android.content.Context
import android.util.Log
import com.endlessrunner.audio.AudioManager
import com.endlessrunner.audio.GameAudioIntegration
import com.endlessrunner.config.GameConfig
import com.endlessrunner.core.GameConstants
import com.endlessrunner.core.GameLoop
import com.endlessrunner.core.GameState
import com.endlessrunner.core.TimeProvider
import com.endlessrunner.entities.EntityManager
import com.endlessrunner.player.Player
import com.endlessrunner.systems.BaseSystem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Главный менеджер игры.
 * Оркестрирует все компоненты игры.
 *
 * @param context Контекст приложения
 * @param config Конфигурация игры
 * @param entityManager Менеджер сущностей
 * @param timeProvider Провайдер времени
 * @param saveManager Менеджер сохранений
 * @param progressManager Менеджер прогресса
 * @param achievementManager Менеджер достижений
 * @param leaderboardManager Менеджер таблицы лидеров
 */
class GameManager(
    private val context: Context,
    private val config: GameConfig = GameConfig.DEFAULT,
    private val entityManager: EntityManager = EntityManager.getInstance(),
    private val timeProvider: TimeProvider = TimeProvider(),
    private val saveManager: SaveManager? = null,
    private val progressManager: ProgressManager? = null,
    private val achievementManager: AchievementManager? = null,
    private val leaderboardManager: LeaderboardManager? = null,
    private val playerName: String = "Игрок"
) {
    companion object {
        private const val TAG = "GameManager"

        @Volatile
        private var instance: GameManager? = null

        /**
         * Получение экземпляра GameManager.
         */
        fun getInstance(
            context: Context,
            config: GameConfig = GameConfig.DEFAULT,
            entityManager: EntityManager = EntityManager.getInstance(),
            saveManager: SaveManager? = null,
            progressManager: ProgressManager? = null,
            achievementManager: AchievementManager? = null,
            leaderboardManager: LeaderboardManager? = null,
            playerName: String = "Игрок"
        ): GameManager {
            return instance ?: synchronized(this) {
                instance ?: GameManager(
                    context = context,
                    config = config,
                    entityManager = entityManager,
                    saveManager = saveManager,
                    progressManager = progressManager,
                    achievementManager = achievementManager,
                    leaderboardManager = leaderboardManager,
                    playerName = playerName
                ).also { instance = it }
            }
        }

        /**
         * Сброс экземпляра.
         */
        fun resetInstance() {
            instance?.dispose()
            instance = null
        }
    }

    /**
     * AudioManager для воспроизведения звуков.
     */
    private val audioManager: AudioManager by lazy {
        AudioManager.getInstance(context)
    }

    /**
     * GameAudioIntegration для привязки аудио к событиям игры.
     */
    private val audioIntegration: GameAudioIntegration by lazy {
        GameAudioIntegration.getInstance(context, audioManager)
    }

    /** Игровой цикл */
    var gameLoop: GameLoop? = null
        private set

    /** Список систем */
    private val systems: MutableList<BaseSystem> = mutableListOf()

    /** Состояние игры (используем GameState из core) */
    private val _gameState = MutableStateFlow<GameState>(GameState.Menu)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    /** Флаг работы игры */
    var isPlaying: Boolean = false
        private set

    /** Флаг паузы */
    var isPaused: Boolean = false
        private set

    /** Текущий счёт */
    var score: Int = 0
        private set

    /** Рекорд */
    var highScore: Int = 0
        private set

    /** Игрок */
    val player: Player?
        get() = entityManager.getFirstByType()

    /** Callback на изменение счёта */
    var onScoreChanged: ((Int) -> Unit)? = null

    /** Callback на конец игры */
    var onGameOver: ((Int) -> Unit)? = null

    /** Callback на паузу */
    var onPauseChanged: ((Boolean) -> Unit)? = null

    /** Статистика врагов */
    var enemiesDefeated: Int = 0
        private set

    /** Статистика урона */
    var damageTaken: Int = 0
        private set

    /** Время начала текущей игры */
    private var gameStartTime: Long = 0L

    /** Собранные монеты в текущей игре */
    private var currentGameCoins: Int = 0

    /** Пройденная дистанция в текущей игре */
    private var currentGameDistance: Float = 0f

    /** CoroutineScope для операций сохранения */
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    init {
        Log.d(TAG, "GameManager инициализирован")
    }

    // ============================================================================
    // УПРАВЛЕНИЕ ИГРОЙ
    // ============================================================================

    /**
     * Старт новой игры.
     */
    fun startGame() {
        if (isPlaying) {
            Log.w(TAG, "Игра уже запущена")
            return
        }

        Log.d(TAG, "Старт новой игры")

        // Сброс состояния
        resetGame()

        // Создание игрока
        createPlayer()

        // Инициализация систем
        initSystems()

        // Подписка на аудио события
        audioIntegration.subscribeToGameEvents()

        // Запуск игрового цикла
        gameLoop?.start()

        isPlaying = true
        isPaused = false
        gameStartTime = System.currentTimeMillis()
        _gameState.value = GameState.Playing

        // Запуск автосохранения
        saveManager?.startAutoSave()

        // Запуск музыки геймплея
        audioIntegration.onGameStart()

        Log.i(TAG, "Игра запущена")
    }

    /**
     * Пауза игры.
     */
    fun pauseGame() {
        if (!isPlaying || isPaused) return

        Log.d(TAG, "Пауза игры")

        isPaused = true
        timeProvider.pause()
        gameLoop?.pause()

        systems.forEach { it.onPause() }

        _gameState.value = GameState.Paused
        onPauseChanged?.invoke(true)

        // Аудио событие паузы
        audioIntegration.onPause()
    }

    /**
     * Возобновление игры.
     */
    fun resumeGame() {
        if (!isPlaying || !isPaused) return

        Log.d(TAG, "Возобновление игры")

        isPaused = false
        timeProvider.resume()
        gameLoop?.resume()

        systems.forEach { it.onResume() }

        _gameState.value = GameState.Playing
        onPauseChanged?.invoke(false)

        // Аудио событие возобновления
        audioIntegration.onResume()
    }

    /**
     * Конец игры.
     */
    fun gameOver() {
        if (!isPlaying) return

        Log.d(TAG, "Конец игры. Счёт: $score")

        isPlaying = false
        isPaused = false

        // Вычисление времени игры
        val playTimeSeconds = (System.currentTimeMillis() - gameStartTime) / 1000L

        // Проверка рекорда
        if (score > highScore) {
            highScore = score
            Log.i(TAG, "Новый рекорд: $highScore")
        }

        _gameState.value = GameState.GameOver

        systems.forEach { it.onGameOver() }

        // Аудио событие конца игры
        audioIntegration.onGameOver(isVictory = false, score = score)

        // Сохранение результата
        saveGameResult(playTimeSeconds)

        onGameOver?.invoke(score)
    }

    /**
     * Сохранение результата игры.
     */
    private fun saveGameResult(playTimeSeconds: Long) {
        scope.launch {
            try {
                // Завершение текущего сохранения
                saveManager?.completeCurrentGame()

                // Обновление прогресса
                progressManager?.recordGameCompleted(
                    isWin = false,
                    finalScore = score,
                    finalCoins = currentGameCoins,
                    finalDistance = currentGameDistance,
                    playTime = playTimeSeconds
                )

                // Добавление в таблицу лидеров
                leaderboardManager?.addEntry(
                    score = score,
                    coins = currentGameCoins,
                    distance = currentGameDistance,
                    playerName = playerName
                )

                // Проверка достижений
                progressManager?.getCurrentProgress()?.let { progress ->
                    achievementManager?.checkAchievements(progress)
                }

                // Проверка достижения "Идеальная игра"
                achievementManager?.checkPerfectGame(damageTaken)

                Log.d(TAG, "Результат игры сохранён: score=$score, coins=$currentGameCoins, distance=$currentGameDistance")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при сохранении результата игры", e)
            }
        }
    }

    /**
     * Перезапуск игры.
     */
    fun restartGame() {
        Log.d(TAG, "Перезапуск игры")
        gameOver()
        startGame()
    }

    /**
     * Завершение игры.
     */
    fun stopGame() {
        Log.d(TAG, "Завершение игры")

        isPlaying = false
        isPaused = false

        gameLoop?.stop()
        saveManager?.stopAutoSave()

        _gameState.value = GameState.Menu
    }

    // ============================================================================
    // ВНУТРЕННИЕ МЕТОДЫ
    // ============================================================================

    /**
     * Сброс игры.
     */
    private fun resetGame() {
        score = 0
        enemiesDefeated = 0
        damageTaken = 0
        currentGameCoins = 0
        currentGameDistance = 0f
        entityManager.clear()
        systems.forEach { it.reset() }
        timeProvider.reset()
    }

    /**
     * Создание игрока.
     */
    private fun createPlayer() {
        val player = Player(config.player)
        entityManager.create(tag = GameConstants.TAG_PLAYER) {
            addComponent(player.positionComponent)
            addComponent(player.renderComponent)
            addComponent(player.physicsComponent)
            addComponent(player.movementComponent)
        }
    }

    /**
     * Инициализация систем.
     */
    private fun initSystems() {
        systems.forEach { it.init() }
    }

    // ============================================================================
    // УПРАВЛЕНИЕ СИСТЕМАМИ
    // ============================================================================

    /**
     * Добавление системы.
     */
    fun addSystem(system: BaseSystem) {
        systems.add(system)
        system.init()
        Log.d(TAG, "Добавлена система: ${system::class.simpleName}")
    }

    /**
     * Удаление системы.
     */
    fun removeSystem(system: BaseSystem) {
        system.dispose()
        systems.remove(system)
        Log.d(TAG, "Удалена система: ${system::class.simpleName}")
    }

    /**
     * Получение системы по типу.
     */
    inline fun <reified T : BaseSystem> getSystem(): T? {
        return systems.firstOrNull { it is T } as? T
    }

    /**
     * Обновление всех систем.
     */
    fun updateSystems(deltaTime: Float) {
        // Сортировка по приоритету
        systems.sortBy { it.updatePriority }

        systems.forEach { it.update(deltaTime) }
    }

    /**
     * Рендеринг всех систем.
     */
    fun renderSystems(canvas: android.graphics.Canvas) {
        // Сортировка по приоритету
        systems.sortBy { it.renderPriority }

        systems.forEach { it.render(canvas) }
    }

    // ============================================================================
    // СЧЁТ
    // ============================================================================

    /**
     * Добавление очков.
     */
    fun addScore(points: Int) {
        score += points
        onScoreChanged?.invoke(score)
    }

    /**
     * Получение текущего счёта.
     */
    fun getScore(): Int = score

    /**
     * Получение рекорда.
     */
    fun getHighScore(): Int = highScore

    // ============================================================================
    // СТАТИСТИКА ВРАГОВ
    // ============================================================================

    /**
     * Уведомление об уничтожении врага.
     */
    fun onEnemyDefeated() {
        enemiesDefeated++
        // Бонусные очки за уничтожение врага
        addScore(50)

        // Аудио событие
        audioIntegration.onEnemyDeath()

        // Запись в прогресс
        scope.launch {
            progressManager?.recordEnemyDefeated()
        }
    }

    /**
     * Уведомление о получении урона.
     */
    fun onDamageTaken(amount: Int) {
        damageTaken += amount

        // Аудио событие
        audioIntegration.onPlayerHit(amount)
    }

    /**
     * Получение статистики врагов.
     */
    fun getEnemyStats(): EnemyStats {
        val activeEnemies = entityManager.getAllEnemies()
        return EnemyStats(
            enemiesDefeated = enemiesDefeated,
            damageTaken = damageTaken,
            activeEnemies = activeEnemies.size
        )
    }

    // ============================================================================
    // МОНЕТЫ И ДИСТАНЦИЯ
    // ============================================================================

    /**
     * Уведомление о сборе монеты.
     */
    fun onCoinCollected(value: Int = 10) {
        currentGameCoins += value
        addScore(value)

        // Аудио событие
        audioIntegration.onCoinCollected(value)

        scope.launch {
            progressManager?.recordCoin(value)
        }
    }

    /**
     * Уведомление о пройденной дистанции.
     */
    fun onDistanceTraveled(amount: Float) {
        currentGameDistance += amount
        
        scope.launch {
            progressManager?.recordDistance(amount)
        }
    }

    // ============================================================================
    // УТИЛИТЫ
    // ============================================================================

    /**
     * Получение времени игры.
     */
    fun getGameTime(): Float = timeProvider.getElapsedTime()

    /**
     * Получение времени игры в секундах.
     */
    fun getGameTimeSeconds(): Int = getGameTime().toInt()

    /**
     * Получение времени игры в формате MM:SS.
     */
    fun getGameTimeFormatted(): String {
        val totalSeconds = getGameTimeSeconds()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * Освобождение ресурсов.
     */
    fun dispose() {
        Log.d(TAG, "Освобождение ресурсов GameManager")

        stopGame()
        systems.forEach { it.dispose() }
        systems.clear()
        entityManager.dispose()
        scope.cancel()

        // Отписка от аудио событий
        audioIntegration.unsubscribeFromGameEvents()

        onScoreChanged = null
        onGameOver = null
        onPauseChanged = null
    }

    /**
     * Data class для статистики врагов.
     */
    data class EnemyStats(
        val enemiesDefeated: Int,
        val damageTaken: Int,
        val activeEnemies: Int
    )

    /**
     * Получение статистики текущей игры.
     */
    fun getCurrentGameStats(): CurrentGameStats {
        return CurrentGameStats(
            score = score,
            coins = currentGameCoins,
            distance = currentGameDistance,
            enemiesDefeated = enemiesDefeated,
            damageTaken = damageTaken,
            playTimeSeconds = (System.currentTimeMillis() - gameStartTime) / 1000L
        )
    }

    /**
     * Data class для статистики текущей игры.
     */
    data class CurrentGameStats(
        val score: Int,
        val coins: Int,
        val distance: Float,
        val enemiesDefeated: Int,
        val damageTaken: Int,
        val playTimeSeconds: Long
    )
}

/**
 * Extension property для проверки состояния.
 */
val GameManager.isMenuState: Boolean
    get() = gameState.value == GameState.Menu

val GameManager.isPlayingState: Boolean
    get() = gameState.value == GameState.Playing

val GameManager.isPausedState: Boolean
    get() = gameState.value == GameState.Paused

val GameManager.isGameOverState: Boolean
    get() = gameState.value == GameState.GameOver
