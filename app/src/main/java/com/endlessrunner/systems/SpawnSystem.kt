package com.endlessrunner.systems

import android.util.Log
import com.endlessrunner.collectibles.Coin
import com.endlessrunner.config.EnemyConfig
import com.endlessrunner.config.GameConfig
import com.endlessrunner.config.SpawnConfig
import com.endlessrunner.core.GameConstants
import com.endlessrunner.enemies.EnemySpawner
import com.endlessrunner.enemies.createEnemySpawner
import com.endlessrunner.entities.EntityManager
import com.endlessrunner.entities.Entity
import com.endlessrunner.obstacles.ObstacleFactory
import com.endlessrunner.obstacles.ObstacleType
import com.endlessrunner.obstacles.PlatformType
import kotlin.random.Random

/**
 * Система спавна объектов.
 * Создаёт монеты, препятствия и врагов во время игры.
 *
 * @param entityManager Менеджер сущностей
 * @param config Конфигурация игры
 */
class SpawnSystem(
    entityManager: EntityManager,
    config: GameConfig = GameConfig.DEFAULT
) : BaseSystem(entityManager, config), EntitySpawner {

    companion object {
        private const val TAG = "SpawnSystem"
    }

    /** Конфигурация спавна */
    private val spawnConfig: SpawnConfig = config.spawn

    /** Конфигурация врагов */
    private val enemyConfig: EnemyConfig = EnemyConfig.DEFAULT

    /** Спавнер врагов */
    private lateinit var enemySpawner: EnemySpawner

    /** Текущая скорость спавна */
    private var currentSpawnSpeed: Float = spawnConfig.initialSpawnSpeed

    /** Таймер спавна монет */
    private var coinSpawnTimer: Float = 0f

    /** Таймер спавна препятствий */
    private var obstacleSpawnTimer: Float = 0f

    /** Позиция последнего спавна монеты */
    private var lastCoinSpawnX: Float = 0f

    /** Позиция последнего спавна препятствия */
    private var lastObstacleSpawnX: Float = 0f

    /** Уровень сложности */
    var difficultyLevel: Int = config.difficulty.initialLevel
        private set

    /** Количество собранных монет (для повышения сложности) */
    var coinsCollected: Int = 0
        private set

    /** Флаг включения спавна монет */
    var enableCoinSpawn: Boolean = true

    /** Флаг включения спавна препятствий */
    var enableObstacleSpawn: Boolean = true

    /** Флаг включения спавна врагов */
    var enableEnemySpawn: Boolean = true

    /** Граница экрана для спавна */
    var screenRightEdge: Float = 1920f

    init {
        updatePriority = 20
    }

    override fun init() {
        super.init()
        lastCoinSpawnX = screenRightEdge
        lastObstacleSpawnX = screenRightEdge
        
        // Инициализация спавнера врагов
        enemySpawner = createEnemySpawner(
            entityManager = entityManager,
            spawnConfig = spawnConfig,
            enemyConfig = enemyConfig
        )
        enemySpawner.screenRightEdge = screenRightEdge
        enemySpawner.groundY = 800f
        enemySpawner.skyY = 200f
    }
    
    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        // Увеличение скорости со временем
        increaseSpeedOverTime(deltaTime)

        // Спавн монет
        if (enableCoinSpawn) {
            updateCoinSpawn(deltaTime)
        }

        // Спавн препятствий
        if (enableObstacleSpawn) {
            updateObstacleSpawn(deltaTime)
        }

        // Спавн врагов
        if (enableEnemySpawn) {
            updateEnemySpawn(deltaTime)
        }
    }

    /**
     * Обновление спавна врагов.
     */
    private fun updateEnemySpawn(deltaTime: Float) {
        val playerX = entityManager.getFirstByTag(GameConstants.TAG_PLAYER)
            ?.getComponent<com.endlessrunner.components.PositionComponent>()
            ?.x ?: 0f
        
        enemySpawner.update(deltaTime, playerX)
    }
    
    /**
     * Увеличение скорости спавна со временем.
     */
    private fun increaseSpeedOverTime(deltaTime: Float) {
        currentSpawnSpeed += spawnConfig.speedIncreasePerSecond * deltaTime
        currentSpawnSpeed = currentSpawnSpeed.coerceAtMost(spawnConfig.maxSpawnSpeed)
    }
    
    /**
     * Обновление спавна монет.
     */
    private fun updateCoinSpawn(deltaTime: Float) {
        // Расчёт времени до следующего спавна
        val spawnInterval = calculateCoinSpawnInterval()
        
        coinSpawnTimer -= deltaTime
        
        if (coinSpawnTimer <= 0) {
            // Спавн монеты
            if (Random.nextFloat() < spawnConfig.coinSpawnChance) {
                spawnCoin()
            }
            
            // Сброс таймера
            coinSpawnTimer = spawnInterval
        }
    }
    
    /**
     * Расчёт интервала спавна монет.
     */
    private fun calculateCoinSpawnInterval(): Float {
        val distance = Random.nextFloat() * 
            (spawnConfig.maxCoinDistance - spawnConfig.minCoinDistance) + 
            spawnConfig.minCoinDistance
        
        return distance / currentSpawnSpeed
    }
    
    /**
     * Спавн монеты.
     */
    private fun spawnCoin() {
        val x = screenRightEdge + Random.nextFloat() * 200f
        val y = calculateCoinYPosition()
        
        val coin = Coin(
            value = GameConstants.BASE_COIN_SCORE * difficultyLevel,
            config = config.coin
        )
        
        coin.positionComponent?.setPosition(x, y)
        coin.setRotationAnimation(config.coin.rotationSpeed)
        coin.setBobAnimation(config.coin.bobAmplitude, config.coin.bobFrequency)
        
        entityManager.create(tag = GameConstants.TAG_COIN) {
            // Копирование компонентов из coin
            addComponent(coin.positionComponent!!)
            addComponent(coin.renderComponent!!)
            addComponent(coin.physicsComponent!!)
        }
        
        lastCoinSpawnX = x
        
        Log.d(TAG, "Спавн монеты: x=$x, y=$y")
    }
    
    /**
     * Расчёт позиции Y для монеты.
     */
    private fun calculateCoinYPosition(): Float {
        // Разные высоты для разнообразия
        val heights = listOf(300f, 450f, 600f, 750f)
        return heights[Random.nextInt(heights.size)]
    }
    
    /**
     * Обновление спавна препятствий.
     */
    private fun updateObstacleSpawn(deltaTime: Float) {
        val spawnInterval = calculateObstacleSpawnInterval()
        
        obstacleSpawnTimer -= deltaTime
        
        if (obstacleSpawnTimer <= 0) {
            if (Random.nextFloat() < spawnConfig.obstacleSpawnChance) {
                spawnObstacle()
            }
            
            obstacleSpawnTimer = spawnInterval
        }
    }
    
    /**
     * Расчёт интервала спавна препятствий.
     */
    private fun calculateObstacleSpawnInterval(): Float {
        val distance = Random.nextFloat() * 
            (spawnConfig.maxObstacleDistance - spawnConfig.minObstacleDistance) + 
            spawnConfig.minObstacleDistance
        
        return distance / currentSpawnSpeed
    }
    
    /**
     * Спавн препятствия.
     */
    private fun spawnObstacle() {
        val x = screenRightEdge + Random.nextFloat() * 100f
        val y = 800f // Земля
        
        // TODO: Создать препятствие (пока заглушка)
        val obstacle = entityManager.create(tag = GameConstants.TAG_OBSTACLE) {
            // Заглушка - будет реализовано позже
        }
        
        obstacle.getComponent<com.endlessrunner.components.PositionComponent>()?.setPosition(x, y)
        
        lastObstacleSpawnX = x
        
        Log.d(TAG, "Спавн препятствия: x=$x")
    }
    
    override fun spawn(x: Float, y: Float): Entity? {
        // Общий метод спавна
        return null
    }
    
    /**
     * Уведомление о сборе монеты.
     */
    fun onCoinCollected() {
        coinsCollected++
        
        // Проверка повышения уровня сложности
        val coinsForNextLevel = config.difficulty.levelUpEveryCoins * difficultyLevel
        if (coinsCollected >= coinsForNextLevel) {
            increaseDifficulty()
        }
    }
    
    /**
     * Повышение уровня сложности.
     */
    private fun increaseDifficulty() {
        if (difficultyLevel >= config.difficulty.maxLevel) return

        difficultyLevel++
        coinsCollected = 0
        
        // Обновление спавнера врагов
        enemySpawner.updateDifficulty(difficultyLevel)

        Log.i(TAG, "Повышение уровня сложности: $difficultyLevel")
    }

    /**
     * Увеличение скорости.
     */
    fun increaseSpeed(amount: Float) {
        currentSpawnSpeed = (currentSpawnSpeed + amount).coerceAtMost(spawnConfig.maxSpawnSpeed)
    }

    /**
     * Сброс скорости.
     */
    fun resetSpeed() {
        currentSpawnSpeed = spawnConfig.initialSpawnSpeed
    }

    override fun reset() {
        super.reset()
        coinSpawnTimer = 0f
        obstacleSpawnTimer = 0f
        lastCoinSpawnX = screenRightEdge
        lastObstacleSpawnX = screenRightEdge
        difficultyLevel = config.difficulty.initialLevel
        coinsCollected = 0
        resetSpeed()
        
        // Сброс спавнера врагов
        if (::enemySpawner.isInitialized) {
            enemySpawner.reset()
            enemySpawner.screenRightEdge = screenRightEdge
        }
    }
    
    /**
     * Получение текущей скорости спавна.
     */
    fun getCurrentSpawnSpeed(): Float = currentSpawnSpeed
    
    /**
     * Получение статистики спавна.
     */
    fun getSpawnStats(): SpawnStats {
        return SpawnStats(
            difficultyLevel = difficultyLevel,
            coinsCollected = coinsCollected,
            currentSpawnSpeed = currentSpawnSpeed,
            coinSpawnTimer = coinSpawnTimer,
            obstacleSpawnTimer = obstacleSpawnTimer
        )
    }
    
    /**
     * Data class для статистики спавна.
     */
    data class SpawnStats(
        val difficultyLevel: Int,
        val coinsCollected: Int,
        val currentSpawnSpeed: Float,
        val coinSpawnTimer: Float,
        val obstacleSpawnTimer: Float
    )
}

/**
 * Extension функция для создания SpawnSystem.
 */
fun createSpawnSystem(
    entityManager: EntityManager,
    config: GameConfig
): SpawnSystem {
    return SpawnSystem(entityManager, config)
}
