package com.endlessrunner.enemies

import android.util.Log
import com.endlessrunner.config.EnemyConfig
import com.endlessrunner.config.SpawnConfig
import com.endlessrunner.core.GameConstants
import com.endlessrunner.entities.EntityManager
import com.endlessrunner.obstacles.ObstacleFactory
import com.endlessrunner.obstacles.PlatformType
import com.endlessrunner.player.Player
import kotlin.random.Random

/**
 * Спавнер врагов.
 * Управляет созданием и размещением врагов в игровом мире.
 *
 * @param entityManager Менеджер сущностей
 * @param spawnConfig Конфигурация спавна
 * @param enemyConfig Конфигурация врагов
 */
class EnemySpawner(
    private val entityManager: EntityManager,
    private val spawnConfig: SpawnConfig = SpawnConfig(),
    private val enemyConfig: EnemyConfig = EnemyConfig.DEFAULT
) {
    
    companion object {
        private const val TAG = "EnemySpawner"
        
        /** Минимальная дистанция между врагами */
        private const val MIN_ENEMY_DISTANCE = 200f
        
        /** Максимальное количество врагов одновременно */
        private const val MAX_ACTIVE_ENEMIES = 20
    }
    
    /** Текущий уровень сложности */
    var difficultyLevel: Int = 1
    
    /** Множитель сложности */
    var difficultyMultiplier: Float = 1f
    
    /** Таймер спавна врагов */
    private var enemySpawnTimer: Float = 0f
    
    /** Позиция последнего спавна врага */
    private var lastEnemySpawnX: Float = 0f
    
    /** Количество заспавненных врагов */
    var enemiesSpawned: Int = 0
        private set
    
    /** Количество убитых/избежанных врагов */
    var enemiesAvoided: Int = 0
        private set
    
    /** Правая граница экрана для спавна */
    var screenRightEdge: Float = 1920f
    
    /** Границы уровня по Y */
    var groundY: Float = 800f
    var skyY: Float = 200f
    
    /** Включить спавн врагов */
    var enableSpawning: Boolean = true
    
    /** Доступные типы врагов для текущего уровня */
    private var availableTypes: List<EnemyType> = EnemyType.ALL_TYPES
    
    /**
     * Обновление спавнера.
     *
     * @param deltaTime Время с последнего кадра
     * @param playerPositionX Позиция игрока по X
     */
    fun update(deltaTime: Float, playerPositionX: Float) {
        if (!enableSpawning) return
        
        // Обновление таймера
        enemySpawnTimer -= deltaTime
        
        // Спавн врага
        if (enemySpawnTimer <= 0f) {
            trySpawnEnemy(playerPositionX)
            enemySpawnTimer = calculateSpawnInterval()
        }
        
        // Очистка старых врагов
        cleanupOldEnemies()
    }
    
    /**
     * Попытка спавна врага.
     */
    private fun trySpawnEnemy(playerX: Float) {
        // Проверка лимита активных врагов
        val activeEnemies = entityManager.getAllEnemies()
        if (activeEnemies.size >= MAX_ACTIVE_ENEMIES) {
            return
        }
        
        // Расчёт позиции спавна
        val spawnX = screenRightEdge + Random.nextFloat() * 200f
        val spawnY = calculateSpawnY()
        
        // Проверка минимальной дистанции
        if (spawnX - lastEnemySpawnX < MIN_ENEMY_DISTANCE) {
            return
        }
        
        // Проверка дистанции от игрока
        if (spawnX - playerX < getMinSpawnDistance()) {
            return
        }
        
        // Создание врага
        spawnEnemy(spawnX, spawnY)
        
        lastEnemySpawnX = spawnX
    }
    
    /**
     * Расчёт позиции Y для спавна.
     */
    private fun calculateSpawnY(): Float {
        val type = EnemyType.getRandomWeighted(availableTypes)
        
        return when (type) {
            EnemyType.STATIC, EnemyType.HAZARD -> groundY - type.height / 2
            EnemyType.MOVING -> groundY - type.height / 2
            EnemyType.FLYING -> Random.nextFloat() * (groundY - skyY) + skyY
            EnemyType.JUMPING -> groundY - type.height / 2
            else -> groundY
        }
    }
    
    /**
     * Спавн врага.
     */
    private fun spawnEnemy(x: Float, y: Float) {
        val type = EnemyType.getRandomWeighted(availableTypes)
        
        val enemy = EnemyFactory.createEnemy(
            type = type,
            x = x,
            y = y,
            config = enemyConfig
        )
        
        // Применение множителя сложности
        enemy.damage = (type.baseDamage * difficultyMultiplier).toInt()
        enemy.currentSpeed = type.baseSpeed * difficultyMultiplier
        
        // Обновление статистики
        enemiesSpawned++
        
        Log.d(TAG, "Спавн врага: type=${type.id}, x=$x, y=$y, damage=${enemy.damage}")
    }
    
    /**
     * Спавн конкретного типа врага.
     */
    fun spawnEnemy(
        type: EnemyType,
        x: Float,
        y: Float,
        damage: Int? = null,
        speed: Float? = null
    ): Enemy {
        val enemy = EnemyFactory.createEnemy(
            type = type,
            x = x,
            y = y,
            config = enemyConfig
        )
        
        damage?.let { enemy.damage = it }
        speed?.let { enemy.currentSpeed = it }
        
        enemiesSpawned++
        return enemy
    }
    
    /**
     * Спавн волны врагов.
     */
    fun spawnEnemyWave(
        count: Int,
        startX: Float,
        y: Float,
        spacing: Float = 150f,
        types: List<EnemyType>? = null
    ): List<Enemy> {
        val enemyTypes = types ?: availableTypes
        
        return EnemyFactory.createEnemyWave(
            count = count,
            startX = startX,
            y = y,
            spacing = spacing,
            types = enemyTypes,
            entityManager = entityManager,
            config = enemyConfig
        ).also {
            enemiesSpawned += it.size
        }
    }
    
    /**
     * Спавн врагов в формации.
     */
    fun spawnEnemyFormation(
        formation: Formation,
        centerX: Float,
        centerY: Float,
        types: List<EnemyType>? = null
    ): List<Enemy> {
        val enemyTypes = types ?: listOf(EnemyType.FLYING)
        
        return EnemyFactory.createEnemyFormation(
            formation = formation,
            centerX = centerX,
            centerY = centerY,
            types = enemyTypes,
            entityManager = entityManager,
            config = enemyConfig
        ).also {
            enemiesSpawned += it.size
        }
    }
    
    /**
     * Спавн препятствия.
     */
    fun spawnObstacle(
        x: Float,
        y: Float,
        type: com.endlessrunner.obstacles.ObstacleType = com.endlessrunner.obstacles.ObstacleType.BLOCK
    ) {
        val obstacle = ObstacleFactory.createObstacle(
            type = type,
            x = x,
            y = y
        )
        
        Log.d(TAG, "Спавн препятствия: type=${type.name}, x=$x, y=$y")
    }
    
    /**
     * Спавн платформы.
     */
    fun spawnPlatform(
        x: Float,
        y: Float,
        width: Float = 200f,
        height: Float = 40f,
        type: PlatformType = PlatformType.STATIC
    ) {
        val platform = ObstacleFactory.createPlatform(
            x = x,
            y = y,
            width = width,
            height = height,
            type = type
        )
        
        Log.d(TAG, "Спавн платформы: type=${type.name}, x=$x, y=$y")
    }
    
    /**
     * Расчёт интервала спавна.
     */
    private fun calculateSpawnInterval(): Float {
        val baseInterval = spawnConfig.minObstacleDistance / 
            (spawnConfig.initialSpawnSpeed * difficultyMultiplier)
        
        // Случайная вариация
        val variation = Random.nextFloat() * 0.5f + 0.75f
        
        return baseInterval * variation
    }
    
    /**
     * Получение минимальной дистанции спавна от игрока.
     */
    private fun getMinSpawnDistance(): Float {
        return availableTypes.maxOfOrNull { it.minSpawnDistance } ?: 500f
    }
    
    /**
     * Очистка старых врагов.
     */
    private fun cleanupOldEnemies() {
        val enemies = entityManager.getAllEnemies()
        
        for (enemy in enemies) {
            if (enemy.isDestroyed || enemy.canBeDestroyed()) {
                enemy.destroy()
                enemiesAvoided++
            }
        }
    }
    
    /**
     * Обновление доступных типов врагов.
     */
    fun updateAvailableTypes(level: Int = difficultyLevel) {
        availableTypes = EnemyType.getAvailableTypes(level)
    }
    
    /**
     * Обновление сложности.
     */
    fun updateDifficulty(level: Int) {
        difficultyLevel = level
        difficultyMultiplier = 1f + (level - 1) * 0.2f
        
        updateAvailableTypes(level)
        
        Log.i(TAG, "Обновление сложности: level=$level, multiplier=$difficultyMultiplier")
    }
    
    /**
     * Уведомление об уничтожении врага.
     */
    fun onEnemyDestroyed(enemy: Enemy) {
        enemiesAvoided++
    }
    
    /**
     * Уведомление о столкновении с врагом.
     */
    fun onEnemyCollision(enemy: Enemy, player: Player) {
        // Логика при столкновении
    }
    
    /**
     * Сброс спавнера.
     */
    fun reset() {
        enemySpawnTimer = 0f
        lastEnemySpawnX = screenRightEdge
        enemiesSpawned = 0
        enemiesAvoided = 0
        difficultyLevel = 1
        difficultyMultiplier = 1f
        updateAvailableTypes(1)
    }
    
    /**
     * Получение статистики.
     */
    fun getStats(): SpawnStats {
        val activeEnemies = entityManager.getAllEnemies()
        
        return SpawnStats(
            enemiesSpawned = enemiesSpawned,
            enemiesAvoided = enemiesAvoided,
            activeEnemies = activeEnemies.size,
            difficultyLevel = difficultyLevel,
            difficultyMultiplier = difficultyMultiplier,
            spawnTimer = enemySpawnTimer
        )
    }
    
    /**
     * Data class для статистики спавна.
     */
    data class SpawnStats(
        val enemiesSpawned: Int,
        val enemiesAvoided: Int,
        val activeEnemies: Int,
        val difficultyLevel: Int,
        val difficultyMultiplier: Float,
        val spawnTimer: Float
    )
}

/**
 * Extension функция для создания EnemySpawner.
 */
fun createEnemySpawner(
    entityManager: EntityManager,
    spawnConfig: SpawnConfig = SpawnConfig(),
    enemyConfig: EnemyConfig = EnemyConfig.DEFAULT
): EnemySpawner {
    return EnemySpawner(entityManager, spawnConfig, enemyConfig)
}

/**
 * Extension функция для получения всех врагов в радиусе.
 */
fun EntityManager.getEnemiesInRadius(
    centerX: Float,
    centerY: Float,
    radius: Float
): List<Enemy> {
    return getAllEnemies().filter { enemy ->
        val pos = enemy.positionComponent ?: return@filter false
        val dx = pos.x - centerX
        val dy = pos.y - centerY
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        distance <= radius
    }
}

/**
 * Extension функция для уничтожения врагов в радиусе.
 */
fun EntityManager.destroyEnemiesInRadius(
    centerX: Float,
    centerY: Float,
    radius: Float
): Int {
    var count = 0
    getEnemiesInRadius(centerX, centerY, radius).forEach { enemy ->
        enemy.destroy()
        count++
    }
    return count
}
