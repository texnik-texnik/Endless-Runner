package com.endlessrunner.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Конфигурация физики.
 * Определяет параметры физической системы игры.
 */
@Serializable
data class PhysicsConfig(
    @SerialName("gravity")
    val gravity: Float = -30.0f,  // Сила гравитации (отрицательная - вниз)
    
    @SerialName("terminalVelocity")
    val terminalVelocity: Float = -50.0f,  // Максимальная скорость падения
    
    @SerialName("collisionMargin")
    val collisionMargin: Float = 0.05f,  // Запас для коллизий
    
    @SerialName("minBounceVelocity")
    val minBounceVelocity: Float = 5.0f,  // Минимальная скорость для отскока
    
    @SerialName("bounceDamping")
    val bounceDamping: Float = 0.3f,  // Коэффициент затухания отскока
    
    @SerialName("slopeLimit")
    val slopeLimit: Float = 45.0f,  // Максимальный угол наклона поверхности
    
    @SerialName("stepHeight")
    val stepHeight: Float = 0.5f  // Максимальная высота ступеньки для автоподъёма
)
