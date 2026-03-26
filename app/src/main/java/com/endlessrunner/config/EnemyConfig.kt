package com.endlessrunner.config

import android.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Конфигурация врагов.
 * Содержит все настраиваемые параметры для врагов.
 */
@Serializable
data class EnemyConfig(
    /** Базовый урон врагов */
    @SerialName("baseDamage")
    val baseDamage: Int = 1,
    
    /** Базовая скорость врагов */
    @SerialName("baseSpeed")
    val baseSpeed: Float = 300f,
    
    /** Множитель скорости (для увеличения сложности) */
    @SerialName("speedMultiplier")
    val speedMultiplier: Float = 1f,
    
    /** Множитель урона (для увеличения сложности) */
    @SerialName("damageMultiplier")
    val damageMultiplier: Float = 1f,
    
    /** Максимальное количество активных врагов */
    @SerialName("maxActiveEnemies")
    val maxActiveEnemies: Int = 20,
    
    /** Минимальная дистанция между врагами */
    @SerialName("minDistanceBetweenEnemies")
    val minDistanceBetweenEnemies: Float = 200f,
    
    /** Граница уничтожения врагов (за экраном) */
    @SerialName("destroyMargin")
    val destroyMargin: Float = 200f,
    
    /** Показывать отладочную информацию */
    @SerialName("showDebugInfo")
    val showDebugInfo: Boolean = false,
    
    /** Веса для спавна типов врагов */
    @SerialName("spawnWeights")
    val spawnWeights: Map<String, Float> = mapOf(
        "static" to 3f,
        "moving" to 2f,
        "flying" to 1.5f,
        "jumping" to 1.5f,
        "hazard" to 2.5f
    ),
    
    /** Визуальная конфигурация */
    @SerialName("visual")
    val visualConfig: EnemyVisualConfig = EnemyVisualConfig()
) {
    companion object {
        /** Конфигурация по умолчанию */
        val DEFAULT = EnemyConfig()
        
        /** Конфигурация для лёгкого уровня */
        val EASY = EnemyConfig(
            baseDamage = 1,
            baseSpeed = 250f,
            maxActiveEnemies = 10,
            minDistanceBetweenEnemies = 300f
        )
        
        /** Конфигурация для нормального уровня */
        val NORMAL = EnemyConfig()
        
        /** Конфигурация для сложного уровня */
        val HARD = EnemyConfig(
            baseDamage = 2,
            baseSpeed = 400f,
            maxActiveEnemies = 30,
            minDistanceBetweenEnemies = 150f,
            damageMultiplier = 1.5f,
            speedMultiplier = 1.3f
        )
    }
    
    /**
     * Получение веса для типа врага.
     */
    fun getSpawnWeight(typeId: String): Float {
        return spawnWeights[typeId] ?: 1f
    }
    
    /**
     * Получение множителя сложности для уровня.
     */
    fun getDifficultyMultiplier(level: Int): Float {
        return 1f + (level - 1) * 0.2f
    }
}

/**
 * Визуальная конфигурация врагов.
 */
@Serializable
data class EnemyVisualConfig(
    /** Цвет для статичных врагов (ARGB) */
    @SerialName("staticColor")
    val staticColor: Int = Color.rgb(255, 87, 34),
    
    /** Цвет для движущихся врагов */
    @SerialName("movingColor")
    val movingColor: Int = Color.rgb(156, 39, 176),
    
    /** Цвет для летающих врагов */
    @SerialName("flyingColor")
    val flyingColor: Int = Color.rgb(33, 150, 243),
    
    /** Цвет для прыгающих врагов */
    @SerialName("jumpingColor")
    val jumpingColor: Int = Color.rgb(76, 175, 80),
    
    /** Цвет для опасностей */
    @SerialName("hazardColor")
    val hazardColor: Int = Color.rgb(244, 67, 54),
    
    /** Размер хитбокса для статичных врагов */
    @SerialName("staticHitboxWidth")
    val staticHitboxWidth: Float = 80f,
    
    @SerialName("staticHitboxHeight")
    val staticHitboxHeight: Float = 80f,
    
    /** Размер хитбокса для движущихся врагов */
    @SerialName("movingHitboxWidth")
    val movingHitboxWidth: Float = 100f,
    
    @SerialName("movingHitboxHeight")
    val movingHitboxHeight: Float = 100f,
    
    /** Размер хитбокса для летающих врагов */
    @SerialName("flyingHitboxWidth")
    val flyingHitboxWidth: Float = 70f,
    
    @SerialName("flyingHitboxHeight")
    val flyingHitboxHeight: Float = 50f,
    
    /** Размер хитбокса для прыгающих врагов */
    @SerialName("jumpingHitboxWidth")
    val jumpingHitboxWidth: Float = 60f,
    
    @SerialName("jumpingHitboxHeight")
    val jumpingHitboxHeight: Float = 60f,
    
    /** Путь к спрайту для статичных врагов */
    @SerialName("staticSpritePath")
    val staticSpritePath: String = "sprites/enemies/spike.png",
    
    /** Путь к спрайту для движущихся врагов */
    @SerialName("movingSpritePath")
    val movingSpritePath: String = "sprites/enemies/block.png",
    
    /** Путь к спрайту для летающих врагов */
    @SerialName("flyingSpritePath")
    val flyingSpritePath: String = "sprites/enemies/fly.png",
    
    /** Путь к спрайту для прыгающих врагов */
    @SerialName("jumpingSpritePath")
    val jumpingSpritePath: String = "sprites/enemies/jump.png"
) {
    companion object {
        val DEFAULT = EnemyVisualConfig()
    }
    
    /**
     * Получение цвета для типа врага.
     */
    fun getColorForType(typeId: String): Int {
        return when (typeId) {
            "static" -> staticColor
            "moving" -> movingColor
            "flying" -> flyingColor
            "jumping" -> jumpingColor
            "hazard" -> hazardColor
            else -> Color.GRAY
        }
    }
    
    /**
     * Получение размера хитбокса для типа врага.
     */
    fun getHitboxSizeForType(typeId: String): Pair<Float, Float> {
        return when (typeId) {
            "static" -> Pair(staticHitboxWidth, staticHitboxHeight)
            "moving" -> Pair(movingHitboxWidth, movingHitboxHeight)
            "flying" -> Pair(flyingHitboxWidth, flyingHitboxHeight)
            "jumping" -> Pair(jumpingHitboxWidth, jumpingHitboxHeight)
            else -> Pair(80f, 80f)
        }
    }
    
    /**
     * Получение пути к спрайту для типа врага.
     */
    fun getSpritePathForType(typeId: String): String {
        return when (typeId) {
            "static" -> staticSpritePath
            "moving" -> movingSpritePath
            "flying" -> flyingSpritePath
            "jumping" -> jumpingSpritePath
            else -> ""
        }
    }
}
