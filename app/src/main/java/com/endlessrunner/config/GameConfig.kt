package com.endlessrunner.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Основная конфигурация игры.
 * Содержит все настраиваемые параметры в одном месте.
 * Загружается из JSON файла в assets.
 */
@Serializable
data class GameConfig(
    /** Конфигурация игрока */
    @SerialName("player")
    val player: PlayerConfig = PlayerConfig(),
    
    /** Конфигурация спавна объектов */
    @SerialName("spawn")
    val spawn: SpawnConfig = SpawnConfig(),
    
    /** Конфигурация монет */
    @SerialName("coin")
    val coin: CoinConfig = CoinConfig(),
    
    /** Физические настройки */
    @SerialName("physics")
    val physics: PhysicsConfig = PhysicsConfig(),
    
    /** Настройки камеры */
    @SerialName("camera")
    val camera: CameraConfig = CameraConfig(),
    
    /** Настройки сложности */
    @SerialName("difficulty")
    val difficulty: DifficultyConfig = DifficultyConfig(),
    
    /** Общие настройки игры */
    @SerialName("game")
    val game: GameGeneralConfig = GameGeneralConfig()
) {
    companion object {
        /** Конфиг по умолчанию */
        val DEFAULT = GameConfig()
    }
}

/**
 * Конфигурация игрока.
 */
@Serializable
data class PlayerConfig(
    /** Скорость движения (пикселей/сек) */
    @SerialName("speed")
    val speed: Float = 800f,
    
    /** Сила прыжка (пикселей/сек) */
    @SerialName("jumpForce")
    val jumpForce: Float = -1200f,
    
    /** Гравитация (пикселей/сек²) */
    @SerialName("gravity")
    val gravity: Float = 2000f,
    
    /** Максимальная скорость падения */
    @SerialName("terminalVelocity")
    val terminalVelocity: Float = 1500f,
    
    /** Ширина хитбокса */
    @SerialName("width")
    val width: Float = 100f,
    
    /** Высота хитбокса */
    @SerialName("height")
    val height: Float = 150f,
    
    /** Начальное здоровье */
    @SerialName("maxHealth")
    val maxHealth: Int = 3,
    
    /** Время неуязвимости (мс) */
    @SerialName("invincibilityTimeMs")
    val invincibilityTimeMs: Long = 1500L,
    
    /** Цвет игрока (ARGB) */
    @SerialName("color")
    val color: Int = 0xFFFF5722.toInt(),
    
    /** Путь к спрайту */
    @SerialName("spritePath")
    val spritePath: String = "sprites/player.png"
)

/**
 * Конфигурация спавна объектов.
 */
@Serializable
data class SpawnConfig(
    /** Минимальное расстояние между монетами */
    @SerialName("minCoinDistance")
    val minCoinDistance: Float = 200f,

    /** Максимальное расстояние между монетами */
    @SerialName("maxCoinDistance")
    val maxCoinDistance: Float = 600f,

    /** Минимальное расстояние между препятствиями */
    @SerialName("minObstacleDistance")
    val minObstacleDistance: Float = 1000f,

    /** Максимальное расстояние между препятствиями */
    @SerialName("maxObstacleDistance")
    val maxObstacleDistance: Float = 2000f,

    /** Минимальное расстояние между врагами */
    @SerialName("minEnemyDistance")
    val minEnemyDistance: Float = 400f,

    /** Максимальное расстояние между врагами */
    @SerialName("maxEnemyDistance")
    val maxEnemyDistance: Float = 1200f,

    /** Вероятность спавна монеты (0-1) */
    @SerialName("coinSpawnChance")
    val coinSpawnChance: Float = 0.7f,

    /** Вероятность спавна препятствия (0-1) */
    @SerialName("obstacleSpawnChance")
    val obstacleSpawnChance: Float = 0.3f,

    /** Вероятность спавна врага (0-1) */
    @SerialName("enemySpawnChance")
    val enemySpawnChance: Float = 0.5f,

    /** Начальная скорость спавна (пикселей/сек) */
    @SerialName("initialSpawnSpeed")
    val initialSpawnSpeed: Float = 500f,

    /** Максимальная скорость спавна */
    @SerialName("maxSpawnSpeed")
    val maxSpawnSpeed: Float = 1500f,

    /** Увеличение скорости спавна за секунду игры */
    @SerialName("speedIncreasePerSecond")
    val speedIncreasePerSecond: Float = 10f,

    /** Минимальная дистанция между врагами */
    @SerialName("enemySpawnMinDistance")
    val enemySpawnMinDistance: Float = 200f,

    /** Веса для спавна типов врагов */
    @SerialName("enemyWeights")
    val enemyWeights: Map<String, Float> = mapOf(
        "static" to 3f,
        "moving" to 2f,
        "flying" to 1.5f,
        "jumping" to 1.5f
    ),

    /** Кривая сложности */
    @SerialName("difficultyCurve")
    val difficultyCurve: DifficultyCurve = DifficultyCurve.LINEAR,

    /** Максимальное количество активных врагов */
    @SerialName("maxActiveEnemies")
    val maxActiveEnemies: Int = 20
)

/**
 * Типы кривых сложности.
 */
enum class DifficultyCurve {
    /** Линейное увеличение сложности */
    LINEAR,
    
    /** Экспоненциальное увеличение */
    EXPONENTIAL,
    
    /** Ступенчатое увеличение */
    STEP
}

/**
 * Конфигурация монет.
 */
@Serializable
data class CoinConfig(
    /** Ширина хитбокса */
    @SerialName("width")
    val width: Float = 60f,
    
    /** Высота хитбокса */
    @SerialName("height")
    val height: Float = 60f,
    
    /** Очки за монету */
    @SerialName("scoreValue")
    val scoreValue: Int = 10,
    
    /** Цвет монеты (ARGB) */
    @SerialName("color")
    val color: Int = 0xFFFFD700.toInt(),
    
    /** Путь к спрайту */
    @SerialName("spritePath")
    val spritePath: String = "sprites/coin.png",
    
    /** Скорость вращения (градусов/сек) */
    @SerialName("rotationSpeed")
    val rotationSpeed: Float = 180f,
    
    /** Амплитуда покачивания */
    @SerialName("bobAmplitude")
    val bobAmplitude: Float = 10f,
    
    /** Частота покачивания (Гц) */
    @SerialName("bobFrequency")
    val bobFrequency: Float = 3f
)

/**
 * Физические настройки.
 */
@Serializable
data class PhysicsConfig(
    /** Гравитация */
    @SerialName("gravity")
    val gravity: Float = 2000f,
    
    /** Трение воздуха */
    @SerialName("airFriction")
    val airFriction: Float = 0.99f,
    
    /** Трение земли */
    @SerialName("groundFriction")
    val groundFriction: Float = 0.9f,
    
    /** Упругость (коэффициент отскока) */
    @SerialName("bounciness")
    val bounciness: Float = 0.0f,
    
    /** Максимальная скорость */
    @SerialName("maxVelocity")
    val maxVelocity: Float = 2000f,
    
    /** Точность коллизий (пиксели) */
    @SerialName("collisionTolerance")
    val collisionTolerance: Float = 1f
)

/**
 * Настройки камеры.
 */
@Serializable
data class CameraConfig(
    /** Скорость следования за игроком (0-1) */
    @SerialName("followSpeed")
    val followSpeed: Float = 0.1f,
    
    /** Смещение по X относительно игрока */
    @SerialName("offsetX")
    val offsetX: Float = 0f,
    
    /** Смещение по Y относительно игрока */
    @SerialName("offsetY")
    val offsetY: Float = -200f,
    
    /** Минимальный X камеры */
    @SerialName("minX")
    val minX: Float = 0f,
    
    /** Максимальный X камеры (0 = без ограничений) */
    @SerialName("maxX")
    val maxX: Float = 0f,
    
    /** Минимальный Y камеры */
    @SerialName("minY")
    val minY: Float = 0f,
    
    /** Максимальный Y камеры */
    @SerialName("maxY")
    val maxY: Float = 0f,
    
    /** Скорость параллакса для слоя 1 */
    @SerialName("parallaxSpeed1")
    val parallaxSpeed1: Float = 0.2f,
    
    /** Скорость параллакса для слоя 2 */
    @SerialName("parallaxSpeed2")
    val parallaxSpeed2: Float = 0.5f,
    
    /** Скорость параллакса для слоя 3 */
    @SerialName("parallaxSpeed3")
    val parallaxSpeed3: Float = 0.8f
)

/**
 * Настройки сложности.
 */
@Serializable
data class DifficultyConfig(
    /** Начальный уровень сложности */
    @SerialName("initialLevel")
    val initialLevel: Int = 1,
    
    /** Увеличение сложности за каждые N монет */
    @SerialName("levelUpEveryCoins")
    val levelUpEveryCoins: Int = 20,
    
    /** Максимальный уровень сложности */
    @SerialName("maxLevel")
    val maxLevel: Int = 10,
    
    /** Множитель скорости на уровень */
    @SerialName("speedMultiplierPerLevel")
    val speedMultiplierPerLevel: Float = 0.1f,
    
    /** Множитель очков на уровень */
    @SerialName("scoreMultiplierPerLevel")
    val scoreMultiplierPerLevel: Float = 0.1f
)

/**
 * Общие настройки игры.
 */
@Serializable
data class GameGeneralConfig(
    /** Целевой FPS */
    @SerialName("targetFps")
    val targetFps: Int = 60,
    
    /** Использовать фиксированный шаг времени */
    @SerialName("useFixedTimeStep")
    val useFixedTimeStep: Boolean = true,
    
    /** Включить отладочную информацию */
    @SerialName("debugMode")
    val debugMode: Boolean = false,
    
    /** Показывать FPS */
    @SerialName("showFps")
    val showFps: Boolean = true,
    
    /** Показывать хитбоксы */
    @SerialName("showHitboxes")
    val showHitboxes: Boolean = false,
    
    /** Путь к конфигурации */
    @SerialName("configPath")
    val configPath: String = "config/game_config.json"
)

/**
 * Extension property для получения множителя сложности.
 */
val DifficultyConfig.currentMultiplier: Float
    get() = 1f + (initialLevel - 1) * speedMultiplierPerLevel
