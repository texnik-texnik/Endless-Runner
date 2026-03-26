package com.endlessrunner.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Конфигурация игрока.
 * Определяет физические параметры и возможности персонажа.
 */
@Serializable
data class PlayerConfig(
    @SerialName("speed")
    val speed: Float = 5.0f,  // Базовая скорость движения
    
    @SerialName("jumpForce")
    val jumpForce: Float = 15.0f,  // Сила обычного прыжка
    
    @SerialName("doubleJumpForce")
    val doubleJumpForce: Float = 12.0f,  // Сила двойного прыжка
    
    @SerialName("maxJumps")
    val maxJumps: Int = 2,  // Максимальное количество прыжков в воздухе
    
    @SerialName("width")
    val width: Float = 1.0f,  // Ширина хитбокса
    
    @SerialName("height")
    val height: Float = 1.8f,  // Высота хитбокса
    
    @SerialName("acceleration")
    val acceleration: Float = 20.0f,  // Ускорение при разгоне
    
    @SerialName("deceleration")
    val deceleration: Float = 10.0f,  // Замедление при остановке
    
    @SerialName("airResistance")
    val airResistance: Float = 0.98f,  // Сопротивление воздуха
    
    @SerialName("groundFriction")
    val groundFriction: Float = 0.8f,  // Трение о землю
    
    @SerialName("invincibilityDuration")
    val invincibilityDuration: Float = 2.0f,  // Длительность неуязвимости после получения урона
    
    @SerialName("startLives")
    val startLives: Int = 3  // Начальное количество жизней
)
