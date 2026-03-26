package com.endlessrunner.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Конфигурация уровня.
 * Определяет параметры генерации и поведения уровня.
 */
@Serializable
data class LevelConfig(
    @SerialName("baseSpeed")
    val baseSpeed: Float = 5.0f,  // Базовая скорость движения уровня
    
    @SerialName("speedIncrement")
    val speedIncrement: Float = 0.5f,  // Увеличение скорости за уровень
    
    @SerialName("maxSpeed")
    val maxSpeed: Float = 20.0f,  // Максимальная скорость уровня
    
    @SerialName("segmentLength")
    val segmentLength: Float = 50.0f,  // Длина одного сегмента уровня
    
    @SerialName("minPlatformWidth")
    val minPlatformWidth: Float = 3.0f,  // Минимальная ширина платформы
    
    @SerialName("maxPlatformWidth")
    val maxPlatformWidth: Float = 10.0f,  // Максимальная ширина платформы
    
    @SerialName("minGapWidth")
    val minGapWidth: Float = 2.0f,  // Минимальная ширина разрыва между платформами
    
    @SerialName("maxGapWidth")
    val maxGapWidth: Float = 5.0f,  // Максимальная ширина разрыва
    
    @SerialName("spawnDistance")
    val spawnDistance: Float = 100.0f,  // Дистанция спавна объектов впереди игрока
    
    @SerialName("despawnDistance")
    val despawnDistance: Float = 20.0f,  // Дистанция удаления объектов позади игрока
    
    @SerialName("backgroundScrollSpeed")
    val backgroundScrollSpeed: Float = 0.5f,  // Скорость прокрутки фона
    
    @SerialName("parallaxLayers")
    val parallaxLayers: Int = 3  // Количество слоёв параллакса
)
