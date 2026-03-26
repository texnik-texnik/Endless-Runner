package com.endlessrunner.core

import android.graphics.Point

/**
 * Глобальные константы игры.
 * Содержит настройки FPS, физические константы и размеры экрана.
 */
object GameConstants {
    
    // ============================================================================
    // ИГРОВОЙ ЦИКЛ И ВРЕМЯ
    // ============================================================================
    
    /** Целевое количество кадров в секунду */
    const val TARGET_FPS: Int = 60
    
    /** Время одного кадра в миллисекундах (16.67ms для 60 FPS) */
    const val TARGET_FRAME_TIME_MS: Long = 1000L / TARGET_FPS
    
    /** Время одного кадра в наносекундах */
    const val TARGET_FRAME_TIME_NS: Long = 1_000_000_000L / TARGET_FPS
    
    /** Минимальный deltaTime для предотвращения скачков при лагах */
    const val MIN_DELTA_TIME: Float = 0.0f
    
    /** Максимальный deltaTime для предотвращения "телепортации" при больших лагах */
    const val MAX_DELTA_TIME: Float = 0.25f // 250ms
    
    /** Фиксированный шаг времени для физики (в секундах) */
    const val FIXED_TIME_STEP: Float = 1f / 60f
    
    // ============================================================================
    // ФИЗИЧЕСКИЕ КОНСТАНТЫ (по умолчанию, могут быть переопределены в конфиге)
    // ============================================================================
    
    /** Гравитация по умолчанию (пикселей за секунду²) */
    const val DEFAULT_GRAVITY: Float = 2000f
    
    /** Скорость игрока по умолчанию (пикселей за секунду) */
    const val DEFAULT_PLAYER_SPEED: Float = 800f
    
    /** Сила прыжка по умолчанию (пикселей за секунду) */
    const val DEFAULT_JUMP_FORCE: Float = -1200f
    
    /** Максимальная скорость падения (пикселей за секунду) */
    const val TERMINAL_VELOCITY: Float = 1500f
    
    // ============================================================================
    // РАЗМЕРЫ ИГРЫ
    // ============================================================================
    
    /** Базовая ширина экрана для расчётов (будет масштабироваться) */
    const val BASE_SCREEN_WIDTH: Int = 1920
    
    /** Базовая высота экрана для расчётов (будет масштабироваться) */
    const val BASE_SCREEN_HEIGHT: Int = 1080
    
    /** Минимальная ширина экрана */
    const val MIN_SCREEN_WIDTH: Int = 320
    
    /** Минимальная высота экрана */
    const val MIN_SCREEN_HEIGHT: Int = 480
    
    // ============================================================================
    // СЛОИ КОЛЛИЗИЙ (битовые маски)
    // ============================================================================
    
    /** Слой для игрока */
    const val LAYER_PLAYER: Int = 0b0001
    
    /** Слой для земли и платформ */
    const val LAYER_GROUND: Int = 0b0010
    
    /** Слой для монет и предметов */
    const val LAYER_COLLECTIBLE: Int = 0b0100
    
    /** Слой для препятствий и врагов */
    const val LAYER_OBSTACLE: Int = 0b1000
    
    /** Маска коллизий для игрока (с чем игрок сталкивается) */
    const val PLAYER_COLLISION_MASK: Int = LAYER_GROUND or LAYER_OBSTACLE
    
    /** Маска коллизий для монет */
    const val COLLECTIBLE_COLLISION_MASK: Int = LAYER_PLAYER
    
    // ============================================================================
    // ИГРОВЫЕ ЗНАЧЕНИЯ
    // ============================================================================
    
    /** Начальное количество жизней */
    const val DEFAULT_LIVES: Int = 3
    
    /** Время неуязвимости после получения урона (мс) */
    const val INVINCIBILITY_TIME_MS: Long = 1500L
    
    /** Базовые очки за монету */
    const val BASE_COIN_SCORE: Int = 10
    
    /** Множитель комбо за каждые 10 монет */
    const val COMBO_MULTIPLIER_STEP: Int = 10
    
    /** Максимальный множитель комбо */
    const val MAX_COMBO_MULTIPLIER: Int = 5
    
    // ============================================================================
    // СПАВН ОБЪЕКТОВ
    // ============================================================================
    
    /** Минимальное расстояние между монетами (в пикселях) */
    const val MIN_COIN_SPAWN_DISTANCE: Float = 200f
    
    /** Максимальное расстояние между монетами (в пикселях) */
    const val MAX_COIN_SPAWN_DISTANCE: Float = 600f
    
    /** Минимальное расстояние между препятствиями (в пикселях) */
    const val MIN_OBSTACLE_SPAWN_DISTANCE: Float = 1000f

    /** Максимальное расстояние между препятствиями (в пикселях) */
    const val MAX_OBSTACLE_SPAWN_DISTANCE: Float = 2000f

    /** Минимальное расстояние между врагами (в пикселях) */
    const val MIN_ENEMY_SPAWN_DISTANCE: Float = 400f

    /** Максимальное расстояние между врагами (в пикселях) */
    const val MAX_ENEMY_SPAWN_DISTANCE: Float = 1200f

    // ============================================================================
    // ПУЛЫ ОБЪЕКТОВ
    // ============================================================================

    /** Начальный размер пула монет */
    const val COIN_POOL_INITIAL_SIZE: Int = 50

    /** Максимальный размер пула монет */
    const val COIN_POOL_MAX_SIZE: Int = 200

    /** Начальный размер пула препятствий */
    const val OBSTACLE_POOL_INITIAL_SIZE: Int = 20

    /** Максимальный размер пула препятствий */
    const val OBSTACLE_POOL_MAX_SIZE: Int = 100

    /** Начальный размер пула врагов */
    const val ENEMY_POOL_INITIAL_SIZE: Int = 30

    /** Максимальный размер пула врагов */
    const val ENEMY_POOL_MAX_SIZE: Int = 150

    /** Начальный размер пула шипов */
    const val SPIKE_POOL_INITIAL_SIZE: Int = 30

    /** Максимальный размер пула шипов */
    const val SPIKE_POOL_MAX_SIZE: Int = 100

    /** Начальный размер пула летающих врагов */
    const val FLYING_ENEMY_POOL_INITIAL_SIZE: Int = 15

    /** Максимальный размер пула летающих врагов */
    const val FLYING_ENEMY_POOL_MAX_SIZE: Int = 50
    
    // ============================================================================
    // РАЗМЕРЫ СУЩНОСТЕЙ (по умолчанию)
    // ============================================================================
    
    /** Ширина игрока (пиксели) */
    const val PLAYER_WIDTH: Float = 100f
    
    /** Высота игрока (пиксели) */
    const val PLAYER_HEIGHT: Float = 150f
    
    /** Ширина монеты (пиксели) */
    const val COIN_WIDTH: Float = 60f
    
    /** Высота монеты (пиксели) */
    const val COIN_HEIGHT: Float = 60f
    
    /** Ширина стандартного препятствия (пиксели) */
    const val OBSTACLE_WIDTH: Float = 100f
    
    /** Высота стандартного препятствия (пиксели) */
    const val OBSTACLE_HEIGHT: Float = 100f
    
    // ============================================================================
    // ТАГИ И ИДЕНТИФИКАТОРЫ
    // ============================================================================
    
    /** Тег для игрока */
    const val TAG_PLAYER: String = "player"
    
    /** Тег для монет */
    const val TAG_COIN: String = "coin"
    
    /** Тег для препятствий */
    const val TAG_OBSTACLE: String = "obstacle"

    /** Тег для врагов */
    const val TAG_ENEMY: String = "enemy"

    /** Тег для земли */
    const val TAG_GROUND: String = "ground"

    /** Тег для фона */
    const val TAG_BACKGROUND: String = "background"
}

/**
 * Data class для хранения размеров экрана.
 * Используется value class для избежания overhead.
 */
@JvmInline
value class ScreenSize(val width: Int, val height: Int) {
    val center: Pair<Float, Float>
        get() = Pair(width / 2f, height / 2f)
    
    val aspectRatio: Float
        get() = width.toFloat() / height.toFloat()
    
    companion object {
        val ZERO = ScreenSize(0, 0)
    }
}

/**
 * Расширение для безопасного расчёта deltaTime с ограничением.
 */
fun Long.clampDeltaTime(maxDelta: Float = GameConstants.MAX_DELTA_TIME): Float {
    val delta = this / 1_000_000_000f
    return delta.coerceIn(GameConstants.MIN_DELTA_TIME, maxDelta)
}
