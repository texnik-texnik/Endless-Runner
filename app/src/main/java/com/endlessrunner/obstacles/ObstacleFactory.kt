package com.endlessrunner.obstacles

import com.endlessrunner.entities.EntityManager

/**
 * Фабрика для создания препятствий.
 * Использует Factory Method и Object Pool.
 */
object ObstacleFactory {
    
    /**
     * Создание препятствия указанного типа.
     *
     * @param type Тип препятствия
     * @param x Позиция X
     * @param y Позиция Y
     * @param width Ширина
     * @param height Высота
     * @return Созданное препятствие
     */
    fun createObstacle(
        type: ObstacleType = ObstacleType.BLOCK,
        x: Float = 0f,
        y: Float = 0f,
        width: Float = 100f,
        height: Float = 100f
    ): Obstacle {
        return Obstacle.acquire(type, x, y, width, height)
    }
    
    /**
     * Создание шипов.
     */
    fun createSpike(
        x: Float = 0f,
        y: Float = 0f,
        width: Float = 80f,
        height: Float = 80f
    ): Obstacle {
        return createObstacle(ObstacleType.SPIKE, x, y, width, height)
    }
    
    /**
     * Создание блока.
     */
    fun createBlock(
        x: Float = 0f,
        y: Float = 0f,
        width: Float = 100f,
        height: Float = 100f
    ): Obstacle {
        return createObstacle(ObstacleType.BLOCK, x, y, width, height)
    }
    
    /**
     * Создание барьера.
     */
    fun createBarrier(
        x: Float = 0f,
        y: Float = 0f,
        width: Float = 50f,
        height: Float = 150f
    ): Obstacle {
        return createObstacle(ObstacleType.BARRIER, x, y, width, height)
    }
    
    /**
     * Создание падающего камня.
     */
    fun createFallingRock(
        x: Float = 0f,
        y: Float = 0f,
        size: Float = 80f
    ): Obstacle {
        return createObstacle(ObstacleType.FALLING_ROCK, x, y, size, size)
    }
    
    /**
     * Создание разрушаемой стены.
     */
    fun createBreakableWall(
        x: Float = 0f,
        y: Float = 0f,
        width: Float = 100f,
        height: Float = 200f
    ): Obstacle {
        return createObstacle(ObstacleType.BREAKABLE_WALL, x, y, width, height)
    }
    
    /**
     * Создание платформы.
     */
    fun createPlatform(
        x: Float = 0f,
        y: Float = 0f,
        width: Float = 200f,
        height: Float = 40f,
        type: PlatformType = PlatformType.STATIC
    ): Platform {
        return Platform.acquire(x, y, width, height, type)
    }
    
    /**
     * Создание движущейся платформы.
     */
    fun createMovingPlatform(
        x: Float,
        y: Float,
        width: Float = 200f,
        height: Float = 40f,
        pattern: MovementPattern = MovementPattern.LINEAR,
        speed: Float = 200f,
        amplitude: Float = 150f
    ): Platform {
        return createPlatform(x, y, width, height, PlatformType.MOVING).apply {
            movementPattern = pattern
            this.speed = speed
            this.amplitude = amplitude
        }
    }
    
    /**
     * Создание падающей платформы.
     */
    fun createFallingPlatform(
        x: Float,
        y: Float,
        width: Float = 150f,
        height: Float = 30f
    ): Platform {
        return createPlatform(x, y, width, height, PlatformType.FALLING)
    }
    
    /**
     * Создание ломающейся платформы.
     */
    fun createBreakablePlatform(
        x: Float,
        y: Float,
        width: Float = 150f,
        height: Float = 30f
    ): Platform {
        return createPlatform(x, y, width, height, PlatformType.BREAKABLE)
    }
    
    /**
     * Создание полосы препятствий.
     *
     * @param count Количество препятствий
     * @param startX Начальная позиция X
     * @param y Позиция Y
     * @param spacing Расстояние между препятствиями
     * @param types Доступные типы препятствий
     * @return Список созданных препятствий
     */
    fun createObstacleCourse(
        count: Int,
        startX: Float,
        y: Float,
        spacing: Float = 300f,
        types: List<ObstacleType> = listOf(ObstacleType.SPIKE, ObstacleType.BLOCK)
    ): List<Obstacle> {
        val obstacles = mutableListOf<Obstacle>()
        
        for (i in 0 until count) {
            val x = startX + i * spacing
            val type = types[i % types.size]
            val obstacle = createObstacle(type, x, y)
            obstacles.add(obstacle)
        }
        
        return obstacles
    }
    
    /**
     * Создание случайного препятствия.
     */
    fun createRandomObstacle(
        x: Float,
        y: Float,
        types: List<ObstacleType> = ObstacleType.ALL_TYPES.filter { !it.isDestructible }
    ): Obstacle {
        val type = types.random()
        return createObstacle(type, x, y)
    }
    
    /**
     * Статистика фабрики.
     */
    fun getFactoryStats(): FactoryStats {
        return FactoryStats(
            obstaclePoolActive = 0, // TODO: Интеграция с пулом
            platformPoolActive = 0
        )
    }
    
    /**
     * Data class для статистики.
     */
    data class FactoryStats(
        val obstaclePoolActive: Int,
        val platformPoolActive: Int
    )
}
