package com.endlessrunner.level

import com.endlessrunner.config.FullConfig
import com.endlessrunner.config.LevelConfig
import com.endlessrunner.entities.EntityFactory
import com.endlessrunner.entities.ObstacleType
import com.endlessrunner.entities.PlatformEntity
import com.endlessrunner.entities.PowerUpType
import com.endlessrunner.entities.interfaces.Vector2
import com.endlessrunner.systems.GameStateSystem
import com.endlessrunner.systems.PhysicsSystem
import com.endlessrunner.systems.RenderSystem
import kotlin.random.Random

/**
 * Генератор уровней для endless runner.
 *
 * Процедурно генерирует платформы, препятствия, монеты и бонусы
 * по мере продвижения игрока.
 */
class LevelGenerator(
    private val config: FullConfig,
    private val entityFactory: EntityFactory,
    private val physicsSystem: PhysicsSystem,
    private val renderSystem: RenderSystem,
    private val gameStateSystem: GameStateSystem
) {
    private val levelConfig: LevelConfig = config.level

    /**
     * Текущая позиция генерации по оси X.
     */
    private var currentX: Float = 0f

    /**
     * Последняя сгенерированная позиция Y для платформы.
     */
    private var lastY: Float = 0f

    /**
     * Пройденное расстояние.
     */
    private var distanceTraveled: Float = 0f

    /**
     * Текущая скорость уровня.
     */
    private var currentSpeed: Float = levelConfig.baseSpeed

    /**
     * Позиция спавна (где появляются новые объекты).
     */
    private var spawnX: Float = 0f

    /**
     * Позиция удаления (где объекты удаляются).
     */
    private var despawnX: Float = 0f

    /**
     * Сложность (увеличивается с прогрессом).
     */
    private var difficulty: Float = 1f

    /**
     * Инициализация генератора.
     */
    fun init(startX: Float = 0f) {
        currentX = startX
        lastY = 0f
        distanceTraveled = 0f
        currentSpeed = levelConfig.baseSpeed
        difficulty = 1f

        // Генерация стартовой платформы
        generateStartingPlatform()
    }

    /**
     * Генерация стартовой платформы (безопасная зона).
     */
    private fun generateStartingPlatform() {
        val platform = entityFactory.createPlatform(
            position = Vector2(0f, -2f),
            width = 20f,
            height = 1f
        )
        addEntity(platform)
        currentX = 10f
        lastY = -2f
    }

    /**
     * Обновление генератора.
     * Вызывается каждый кадр для генерации новых сегментов.
     */
    fun update(deltaTime: Float, playerX: Float) {
        distanceTraveled += currentSpeed * deltaTime

        // Обновление позиции спавна относительно игрока
        spawnX = playerX + levelConfig.spawnDistance
        despawnX = playerX - levelConfig.despawnDistance

        // Генерация новых сегментов
        while (currentX < spawnX) {
            generateSegment()
        }

        // Удаление старых объектов
        cleanupDespawnedEntities(playerX)

        // Увеличение сложности
        updateDifficulty()
    }

    /**
     * Генерация одного сегмента уровня.
     */
    private fun generateSegment() {
        val segmentLength = levelConfig.segmentLength + Random.nextFloat() * 10f

        // Определение типа сегмента
        val segmentType = getSegmentType()

        when (segmentType) {
            SegmentType.NORMAL -> generateNormalSegment(segmentLength)
            SegmentType.GAP -> generateGapSegment(segmentLength)
            SegmentType.STAIRS -> generateStairsSegment(segmentLength)
            SegmentType.OBSTACLES -> generateObstacleSegment(segmentLength)
            SegmentType.COINS -> generateCoinSegment(segmentLength)
        }

        currentX += segmentLength
    }

    /**
     * Определение типа сегмента на основе сложности.
     */
    private fun getSegmentType(): SegmentType {
        val rand = Random.nextFloat()

        return when {
            // Увеличиваем вероятность препятствий с ростом сложности
            rand < 0.1f * difficulty -> SegmentType.OBSTACLES
            rand < 0.25f * difficulty -> SegmentType.GAP
            rand < 0.35f -> SegmentType.STAIRS
            rand < 0.5f -> SegmentType.COINS
            else -> SegmentType.NORMAL
        }
    }

    /**
     * Генерация нормального сегмента с платформой и возможными монетами.
     */
    private fun generateNormalSegment(segmentLength: Float) {
        val width = Random.nextFloat() * (levelConfig.maxPlatformWidth - levelConfig.minPlatformWidth) +
                levelConfig.minPlatformWidth

        // Небольшое изменение высоты
        val heightChange = (Random.nextFloat() - 0.5f) * 2f
        lastY = (lastY + heightChange).coerceIn(-5f, 2f)

        val platform = entityFactory.createPlatform(
            position = Vector2(currentX + segmentLength / 2, lastY),
            width = width,
            height = 0.5f
        )
        addEntity(platform)

        // Шанс спавна монет
        if (Random.nextFloat() < config.items.coin.spawnChance * difficulty) {
            spawnCoins(currentX + segmentLength / 2, lastY + 1f)
        }

        // Шанс спавна бонуса
        if (Random.nextFloat() < config.items.powerup.spawnChance) {
            spawnPowerUp(currentX + segmentLength / 2, lastY + 1f)
        }
    }

    /**
     * Генерация сегмента с разрывом (яма).
     */
    private fun generateGapSegment(segmentLength: Float) {
        val gapWidth = Random.nextFloat() * (levelConfig.maxGapWidth - levelConfig.minGapWidth) +
                levelConfig.minGapWidth

        // Платформа перед разрывом
        val platform1Width = segmentLength / 2 - gapWidth / 2
        if (platform1Width > levelConfig.minPlatformWidth) {
            val platform1 = entityFactory.createPlatform(
                position = Vector2(currentX + platform1Width / 2, lastY),
                width = platform1Width,
                height = 0.5f
            )
            addEntity(platform1)
        }

        // Платформа после разрыва
        val platform2Width = segmentLength / 2 - gapWidth / 2
        if (platform2Width > levelConfig.minPlatformWidth) {
            // Высота может измениться после разрыва
            val heightChange = (Random.nextFloat() - 0.5f) * 3f
            lastY = (lastY + heightChange).coerceIn(-5f, 2f)

            val platform2 = entityFactory.createPlatform(
                position = Vector2(currentX + segmentLength / 2 + gapWidth / 2 + platform2Width / 2, lastY),
                width = platform2Width,
                height = 0.5f
            )
            addEntity(platform2)
        }
    }

    /**
     * Генерация сегмента с лестницей.
     */
    private fun generateStairsSegment(segmentLength: Float) {
        val steps = 3
        val stepWidth = segmentLength / steps
        val stepHeight = 0.8f

        for (i in 0 until steps) {
            val y = lastY + i * stepHeight
            val platform = entityFactory.createPlatform(
                position = Vector2(currentX + i * stepWidth + stepWidth / 2, y),
                width = stepWidth * 0.9f,
                height = 0.5f
            )
            addEntity(platform)

            // Монеты на лестнице
            if (i > 0) {
                val coin = entityFactory.createCoin(
                    position = Vector2(currentX + i * stepWidth + stepWidth / 2, y + 1f)
                )
                addEntity(coin)
            }
        }

        lastY += steps * stepHeight
    }

    /**
     * Генерация сегмента с препятствиями.
     */
    private fun generateObstacleSegment(segmentLength: Float) {
        generateNormalSegment(segmentLength)

        val obstacleCount = (1 + difficulty * 2).toInt().coerceAtMost(3)

        for (i in 0 until obstacleCount) {
            val obstacleX = currentX + Random.nextFloat() * segmentLength
            val obstacleType = when (Random.nextInt(4)) {
                0 -> ObstacleType.STATIC
                1 -> ObstacleType.MOVING
                2 -> ObstacleType.FALLING
                else -> ObstacleType.ROTATING
            }

            val obstacle = entityFactory.createObstacle(
                position = Vector2(obstacleX, lastY + 0.5f),
                type = obstacleType,
                width = 0.8f,
                height = 0.8f
            )
            addEntity(obstacle)
        }
    }

    /**
     * Генерация сегмента с монетами.
     */
    private fun generateCoinSegment(segmentLength: Float) {
        generateNormalSegment(segmentLength)

        // Линия монет
        val coinCount = 5
        for (i in 0 until coinCount) {
            val coin = entityFactory.createCoin(
                position = Vector2(
                    currentX + (i + 1) * segmentLength / (coinCount + 1),
                    lastY + 1f + kotlin.math.sin(i * 0.5f) * 0.5f
                )
            )
            addEntity(coin)
        }
    }

    /**
     * Спавн монет в точке.
     */
    private fun spawnCoins(x: Float, y: Float) {
        val count = Random.nextInt(1, 4)
        for (i in 0 until count) {
            val coin = entityFactory.createCoin(
                position = Vector2(x + i * 0.8f, y)
            )
            addEntity(coin)
        }
    }

    /**
     * Спавн бонуса в точке.
     */
    private fun spawnPowerUp(x: Float, y: Float) {
        entityFactory.createRandomPowerUp(
            position = Vector2(x, y)
        )?.let { addEntity(it) }
    }

    /**
     * Удаление объектов за пределами видимости.
     */
    private fun cleanupDespawnedEntities(playerX: Float) {
        val despawnX = playerX - levelConfig.despawnDistance

        physicsSystem.getEntities().filter { entity ->
            entity.position.x < despawnX && !entity.isActive
        }.forEach { entity ->
            physicsSystem.removeEntity(entity)
            renderSystem.removeRenderable(entity)
        }
    }

    /**
     * Обновление сложности на основе прогресса.
     */
    private fun updateDifficulty() {
        // Увеличение сложности каждые 100 метров
        difficulty = 1f + (distanceTraveled / 100f) * 0.2f

        // Увеличение скорости
        val speedIncrease = (difficulty - 1f) * levelConfig.speedIncrement
        currentSpeed = (levelConfig.baseSpeed + speedIncrease)
            .coerceAtMost(levelConfig.maxSpeed)
    }

    /**
     * Добавление сущности в системы.
     */
    private fun addEntity(entity: Any) {
        if (entity is com.endlessrunner.entities.interfaces.IEntity) {
            physicsSystem.addEntity(entity)
            renderSystem.addRenderable(entity)
        }
    }

    /**
     * Сброс генератора.
     */
    fun reset() {
        currentX = 0f
        lastY = 0f
        distanceTraveled = 0f
        currentSpeed = levelConfig.baseSpeed
        difficulty = 1f
    }

    /**
     * Получение текущего расстояния.
     */
    fun getDistanceTraveled(): Float = distanceTraveled

    /**
     * Получение текущей скорости.
     */
    fun getCurrentSpeed(): Float = currentSpeed

    /**
     * Получение текущей сложности.
     */
    fun getDifficulty(): Float = difficulty
}

/**
 * Типы сегментов уровня.
 */
enum class SegmentType {
    NORMAL,
    GAP,
    STAIRS,
    OBSTACLES,
    COINS
}
