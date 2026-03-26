package com.endlessrunner.enemies

import android.graphics.Color
import com.endlessrunner.core.GameConstants

/**
 * Типы врагов в игре.
 * Sealed class для типобезопасности и исчерпывающей проверки.
 */
sealed class EnemyType(
    /** Уникальный идентификатор типа */
    val id: String,
    
    /** Базовый урон */
    val baseDamage: Int,
    
    /** Базовая скорость движения (если применимо) */
    val baseSpeed: Float,
    
    /** Ширина хитбокса */
    val width: Float,
    
    /** Высота хитбокса */
    val height: Float,
    
    /** Цвет для отладочной отрисовки */
    val debugColor: Int,
    
    /** Вес для рандомного спавна (чем больше, тем чаще появляется) */
    val spawnWeight: Float = 1f,
    
    /** Минимальная дистанция спавна от игрока */
    val minSpawnDistance: Float = 500f
) {
    
    /**
     * Статичные враги (шипы, столбы).
     * Не двигаются, наносят урон при касании.
     */
    object STATIC : EnemyType(
        id = "static",
        baseDamage = 1,
        baseSpeed = 0f,
        width = 80f,
        height = 80f,
        debugColor = Color.rgb(255, 87, 34), // Deep Orange
        spawnWeight = 3f,
        minSpawnDistance = 400f
    )
    
    /**
     * Движущиеся враги (блоки, платформы).
     * Двигаются по линейной траектории.
     */
    object MOVING : EnemyType(
        id = "moving",
        baseDamage = 2,
        baseSpeed = 300f,
        width = 100f,
        height = 100f,
        debugColor = Color.rgb(156, 39, 176), // Purple
        spawnWeight = 2f,
        minSpawnDistance = 600f
    )
    
    /**
     * Летающие враги (птицы, дроны).
     * Двигаются по синусоиде.
     */
    object FLYING : EnemyType(
        id = "flying",
        baseDamage = 1,
        baseSpeed = 400f,
        width = 70f,
        height = 50f,
        debugColor = Color.rgb(33, 150, 243), // Blue
        spawnWeight = 1.5f,
        minSpawnDistance = 700f
    )
    
    /**
     * Прыгающие враги (слизь, кролики).
     * Периодически прыгают.
     */
    object JUMPING : EnemyType(
        id = "jumping",
        baseDamage = 2,
        baseSpeed = 200f,
        width = 60f,
        height = 60f,
        debugColor = Color.rgb(76, 175, 80), // Green
        spawnWeight = 1.5f,
        minSpawnDistance = 500f
    )
    
    /**
     * Опасные зоны (шипы на полу/потолке).
     * Могут быть перевёрнуты.
     */
    object HAZARD : EnemyType(
        id = "hazard",
        baseDamage = 1,
        baseSpeed = 0f,
        width = 100f,
        height = 40f,
        debugColor = Color.rgb(244, 67, 54), // Red
        spawnWeight = 2.5f,
        minSpawnDistance = 400f
    )
    
    companion object {
        /** Все типы врагов для итерации */
        val ALL_TYPES: List<EnemyType> = listOf(STATIC, MOVING, FLYING, JUMPING, HAZARD)
        
        /** Типы врагов, доступные для спавна на текущем уровне сложности */
        fun getAvailableTypes(difficultyLevel: Int): List<EnemyType> {
            return when {
                difficultyLevel < 2 -> listOf(STATIC, HAZARD)
                difficultyLevel < 4 -> listOf(STATIC, MOVING, HAZARD)
                difficultyLevel < 6 -> listOf(STATIC, MOVING, FLYING, HAZARD)
                else -> ALL_TYPES
            }
        }
        
        /** Получение типа по ID */
        fun fromId(id: String): EnemyType? {
            return ALL_TYPES.find { it.id == id }
        }
        
        /** Расчёт общего веса для рандомизации */
        fun getTotalWeight(types: List<EnemyType>): Float {
            return types.sumOf { it.spawnWeight }.toFloat()
        }
        
        /** Выбор случайного типа с учётом весов */
        fun getRandomWeighted(types: List<EnemyType>): EnemyType {
            val totalWeight = getTotalWeight(types)
            var random = (Math.random() * totalWeight).toFloat()
            
            for (type in types) {
                random -= type.spawnWeight
                if (random <= 0f) return type
            }
            
            return types.last()
        }
    }
}

/**
 * Паттерны движения для врагов.
 */
enum class MovementPattern {
    /** Линейное движение в одном направлении */
    LINEAR,
    
    /** Движение туда-обратно */
    OSCILLATING,
    
    /** Отскок от границ */
    BOUNCING,
    
    /** Синусоидальное движение */
    SINUSOIDAL,
    
    /** Круговое движение */
    CIRCULAR,
    
    /** Статичное положение */
    STATIC
}

/**
 * Направления движения.
 */
enum class MoveDirection {
    NONE,
    LEFT,
    RIGHT,
    UP,
    DOWN,
    UP_LEFT,
    UP_RIGHT,
    DOWN_LEFT,
    DOWN_RIGHT
}

/**
 * Extension property для получения вектора направления.
 */
val MoveDirection.vector: Pair<Float, Float>
    get() = when (this) {
        MoveDirection.NONE -> Pair(0f, 0f)
        MoveDirection.LEFT -> Pair(-1f, 0f)
        MoveDirection.RIGHT -> Pair(1f, 0f)
        MoveDirection.UP -> Pair(0f, -1f)
        MoveDirection.DOWN -> Pair(0f, 1f)
        MoveDirection.UP_LEFT -> Pair(-1f, -1f)
        MoveDirection.UP_RIGHT -> Pair(1f, -1f)
        MoveDirection.DOWN_LEFT -> Pair(-1f, 1f)
        MoveDirection.DOWN_RIGHT -> Pair(1f, 1f)
    }
