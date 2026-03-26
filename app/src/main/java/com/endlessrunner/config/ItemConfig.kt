package com.endlessrunner.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Конфигурация предметов и препятствий.
 */
@Serializable
data class ItemConfig(
    @SerialName("coin")
    val coin: CoinConfig = CoinConfig(),
    
    @SerialName("powerup")
    val powerup: PowerupConfig = PowerupConfig(),
    
    @SerialName("obstacle")
    val obstacle: ObstacleConfig = ObstacleConfig()
)

/**
 * Конфигурация монет.
 */
@Serializable
data class CoinConfig(
    @SerialName("spawnChance")
    val spawnChance: Float = 0.3f,  // Шанс появления монеты
    
    @SerialName("value")
    val value: Int = 1,  // Стоимость монеты
    
    @SerialName("magnetRange")
    val magnetRange: Float = 5.0f,  // Радиус действия магнита
    
    @SerialName("rotationSpeed")
    val rotationSpeed: Float = 2.0f  // Скорость вращения монеты
)

/**
 * Конфигурация усилений (power-ups).
 */
@Serializable
data class PowerupConfig(
    @SerialName("spawnChance")
    val spawnChance: Float = 0.05f,  // Шанс появления усилителя
    
    @SerialName("duration")
    val duration: Float = 10.0f,  // Длительность действия усилителя
    
    @SerialName("types")
    val types: List<String> = listOf("shield", "speed_boost", "coin_magnet", "extra_jump")
)

/**
 * Конфигурация препятствий.
 */
@Serializable
data class ObstacleConfig(
    @SerialName("minSpawnDistance")
    val minSpawnDistance: Float = 10.0f,  // Минимальная дистанция между препятствиями
    
    @SerialName("types")
    val types: List<String> = listOf("static", "moving", "falling", "rotating"),
    
    @SerialName("damage")
    val damage: Int = 1  // Урон от препятствия
)
