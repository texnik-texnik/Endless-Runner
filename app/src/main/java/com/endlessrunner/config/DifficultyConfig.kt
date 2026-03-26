package com.endlessrunner.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Конфигурация уровней сложности.
 */
@Serializable
data class DifficultyConfig(
    @SerialName("easy")
    val easy: DifficultyLevel = DifficultyLevel(),
    
    @SerialName("normal")
    val normal: DifficultyLevel = DifficultyLevel(),
    
    @SerialName("hard")
    val hard: DifficultyLevel = DifficultyLevel()
)

/**
 * Параметры уровня сложности.
 * Все множители применяются к базовым значениям.
 */
@Serializable
data class DifficultyLevel(
    @SerialName("speedMultiplier")
    val speedMultiplier: Float = 1.0f,  // Множитель скорости игры
    
    @SerialName("obstacleFrequency")
    val obstacleFrequency: Float = 1.0f,  // Частота появления препятствий
    
    @SerialName("itemFrequency")
    val itemFrequency: Float = 1.0f,  // Частота появления предметов
    
    @SerialName("gravityMultiplier")
    val gravityMultiplier: Float = 1.0f  // Множитель гравитации
)

/**
 * Перечисление доступных уровней сложности.
 */
enum class Difficulty {
    EASY,
    NORMAL,
    HARD
}
