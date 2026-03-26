package com.endlessrunner.enemies

import com.endlessrunner.config.EnemyConfig
import com.endlessrunner.entities.EntityManager

/**
 * Фабрика для создания врагов.
 * Использует Factory Method и Object Pool для производительности.
 */
object EnemyFactory {
    
    /**
     * Создание врага указанного типа.
     *
     * @param type Тип врага
     * @param x Позиция X
     * @param y Позиция Y
     * @param config Конфигурация
     * @return Созданный враг
     */
    fun createEnemy(
        type: EnemyType,
        x: Float = 0f,
        y: Float = 0f,
        config: EnemyConfig = EnemyConfig.DEFAULT
    ): Enemy {
        val enemy = when (type) {
            EnemyType.STATIC -> createSpike(x, y, config)
            EnemyType.MOVING -> createMovingBlock(x, y, config)
            EnemyType.FLYING -> createFlying(x, y, config)
            EnemyType.JUMPING -> createJumping(x, y, config)
            EnemyType.HAZARD -> createHazard(x, y, config)
        }
        
        enemy.positionComponent?.setPosition(x, y)
        return enemy
    }
    
    /**
     * Создание врага с кастомным поведением.
     *
     * @param type Тип врага
     * @param behavior Поведение
     * @param x Позиция X
     * @param y Позиция Y
     * @param damage Урон
     * @param config Конфигурация
     * @return Созданный враг
     */
    fun createEnemy(
        type: EnemyType,
        behavior: EnemyBehavior,
        x: Float = 0f,
        y: Float = 0f,
        damage: Int? = null,
        config: EnemyConfig = EnemyConfig.DEFAULT
    ): Enemy {
        val enemy = Enemy.acquire(
            type = type,
            behavior = behavior,
            damage = damage ?: type.baseDamage,
            config = config
        )
        
        enemy.positionComponent?.setPosition(x, y)
        return enemy
    }
    
    /**
     * Создание шипа.
     */
    fun createSpike(
        x: Float = 0f,
        y: Float = 0f,
        config: EnemyConfig = EnemyConfig.DEFAULT
    ): SpikeEnemy {
        return SpikeEnemy.acquire(
            damage = EnemyType.STATIC.baseDamage,
            config = config
        ).apply {
            positionComponent?.setPosition(x, y)
        }
    }
    
    /**
     * Создание шипа с направлением.
     */
    fun createSpike(
        x: Float,
        y: Float,
        upsideDown: Boolean,
        config: EnemyConfig = EnemyConfig.DEFAULT
    ): SpikeEnemy {
        return createSpike(x, y, config).apply {
            isUpsideDown = upsideDown
        }
    }
    
    /**
     * Создание движущегося блока.
     */
    fun createMovingBlock(
        x: Float = 0f,
        y: Float = 0f,
        speed: Float = EnemyType.MOVING.baseSpeed,
        pattern: MovementPattern = MovementPattern.OSCILLATING,
        config: EnemyConfig = EnemyConfig.DEFAULT
    ): MovingBlockEnemy {
        return MovingBlockEnemy.acquire(
            damage = EnemyType.MOVING.baseDamage,
            speed = speed,
            pattern = pattern,
            config = config
        ).apply {
            positionComponent?.setPosition(x, y)
        }
    }
    
    /**
     * Создание летающего врага.
     */
    fun createFlying(
        x: Float = 0f,
        y: Float = 0f,
        speed: Float = EnemyType.FLYING.baseSpeed,
        amplitude: Float = 100f,
        frequency: Float = 1.5f,
        config: EnemyConfig = EnemyConfig.DEFAULT
    ): FlyingEnemy {
        return FlyingEnemy.acquire(
            damage = EnemyType.FLYING.baseDamage,
            speed = speed,
            amplitude = amplitude,
            frequency = frequency,
            config = config
        ).apply {
            positionComponent?.setPosition(x, y)
        }
    }
    
    /**
     * Создание прыгающего врага.
     */
    fun createJumping(
        x: Float = 0f,
        y: Float = 0f,
        speed: Float = EnemyType.JUMPING.baseSpeed,
        jumpInterval: Float = 2f,
        jumpForce: Float = -800f,
        config: EnemyConfig = EnemyConfig.DEFAULT
    ): JumpingEnemy {
        return JumpingEnemy.acquire(
            damage = EnemyType.JUMPING.baseDamage,
            speed = speed,
            jumpInterval = jumpInterval,
            jumpForce = jumpForce,
            config = config
        ).apply {
            positionComponent?.setPosition(x, y)
        }
    }
    
    /**
     * Создание опасности (шипы на потолке).
     */
    fun createHazard(
        x: Float = 0f,
        y: Float = 0f,
        config: EnemyConfig = EnemyConfig.DEFAULT
    ): SpikeEnemy {
        return createSpike(x, y, config).apply {
            isUpsideDown = true
        }
    }
    
    /**
     * Создание случайного врага.
     *
     * @param x Позиция X
     * @param y Позиция Y
     * @param availableTypes Доступные типы врагов
     * @param difficultyMultiplier Множитель сложности
     * @param config Конфигурация
     * @return Созданный враг
     */
    fun createRandomEnemy(
        x: Float = 0f,
        y: Float = 0f,
        availableTypes: List<EnemyType> = EnemyType.ALL_TYPES,
        difficultyMultiplier: Float = 1f,
        config: EnemyConfig = EnemyConfig.DEFAULT
    ): Enemy {
        val type = EnemyType.getRandomWeighted(availableTypes)
        return createEnemy(type, x, y, config).apply {
            damage = (type.baseDamage * difficultyMultiplier).toInt()
            currentSpeed = type.baseSpeed * difficultyMultiplier
        }
    }
    
    /**
     * Создание волны врагов.
     *
     * @param count Количество врагов
     * @param startX Начальная позиция X
     * @param y Позиция Y
     * @param spacing Расстояние между врагами
     * @param types Доступные типы
     * @param entityManager Менеджер сущностей для регистрации
     * @param config Конфигурация
     * @return Список созданных врагов
     */
    fun createEnemyWave(
        count: Int,
        startX: Float,
        y: Float,
        spacing: Float = 150f,
        types: List<EnemyType> = EnemyType.ALL_TYPES,
        entityManager: EntityManager? = null,
        config: EnemyConfig = EnemyConfig.DEFAULT
    ): List<Enemy> {
        val enemies = mutableListOf<Enemy>()
        
        repeat(count) { index ->
            val x = startX + index * spacing
            val enemy = createRandomEnemy(x, y, types, config = config)
            enemies.add(enemy)
            
            // Регистрация в EntityManager
            entityManager?.let { em ->
                // EntityManager будет управлять врагом через тег
            }
        }
        
        return enemies
    }
    
    /**
     * Создание группы врагов в формации.
     *
     * @param formation Тип формации
     * @param centerX Центральная позиция X
     * @param centerY Центральная позиция Y
     * @param types Доступные типы
     * @param entityManager Менеджер сущностей
     * @param config Конфигурация
     * @return Список созданных врагов
     */
    fun createEnemyFormation(
        formation: Formation,
        centerX: Float,
        centerY: Float,
        types: List<EnemyType> = listOf(EnemyType.FLYING),
        entityManager: EntityManager? = null,
        config: EnemyConfig = EnemyConfig.DEFAULT
    ): List<Enemy> {
        val enemies = mutableListOf<Enemy>()
        val positions = formation.getPositions(centerX, centerY)
        
        for ((index, pos) in positions.withIndex()) {
            val type = types.getOrElse(index % types.size) { EnemyType.FLYING }
            val enemy = createEnemy(type, pos.first, pos.second, config)
            enemies.add(enemy)
        }
        
        return enemies
    }
    
    /**
     * Очистка всех врагов из пулов.
     */
    fun clearAllPools() {
        SpikeEnemy.poolStats.let {} // TODO: Очистка пулов
    }
    
    /**
     * Статистика фабрики.
     */
    fun getFactoryStats(): FactoryStats {
        return FactoryStats(
            spikePoolStats = SpikeEnemy.poolStats,
            movingBlockPoolStats = MovingBlockEnemy.poolStats,
            flyingPoolStats = FlyingEnemy.poolStats,
            jumpingPoolStats = JumpingEnemy.poolStats
        )
    }
    
    /**
     * Data class для статистики фабрики.
     */
    data class FactoryStats(
        val spikePoolStats: com.endlessrunner.core.ObjectPool.PoolStats,
        val movingBlockPoolStats: com.endlessrunner.core.ObjectPool.PoolStats,
        val flyingPoolStats: com.endlessrunner.core.ObjectPool.PoolStats,
        val jumpingPoolStats: com.endlessrunner.core.ObjectPool.PoolStats
    )
}

/**
 * Типы формаций для врагов.
 */
sealed class Formation {
    
    /**
     * Получение позиций для формации.
     */
    abstract fun getPositions(centerX: Float, centerY: Float): List<Pair<Float, Float>>
    
    /**
     * Формация "V" (клин).
     */
    data class VFormation(
        val count: Int = 5,
        val spacing: Float = 80f
    ) : Formation() {
        
        override fun getPositions(centerX: Float, centerY: Float): List<Pair<Float, Float>> {
            val positions = mutableListOf<Pair<Float, Float>>()
            
            for (i in 0 until count) {
                val row = i / 2
                val offset = if (i % 2 == 0) -1 else 1
                val x = centerX - row * spacing * 0.5f
                val y = centerY + (row * spacing * offset)
                positions.add(Pair(x, y))
            }
            
            return positions
        }
    }
    
    /**
     * Линейная формация.
     */
    data class LineFormation(
        val count: Int = 3,
        val spacing: Float = 100f,
        val vertical: Boolean = false
    ) : Formation() {
        
        override fun getPositions(centerX: Float, centerY: Float): List<Pair<Float, Float>> {
            val positions = mutableListOf<Pair<Float, Float>>()
            
            for (i in 0 until count) {
                val offset = (i - count / 2) * spacing
                val x = if (vertical) centerX else centerX + offset
                val y = if (vertical) centerY + offset else centerY
                positions.add(Pair(x, y))
            }
            
            return positions
        }
    }
    
    /**
     * Круговая формация.
     */
    data class CircleFormation(
        val count: Int = 6,
        val radius: Float = 100f
    ) : Formation() {
        
        override fun getPositions(centerX: Float, centerY: Float): List<Pair<Float, Float>> {
            val positions = mutableListOf<Pair<Float, Float>>()
            val angleStep = 360f / count
            
            for (i in 0 until count) {
                val angle = Math.toRadians((i * angleStep).toDouble())
                val x = (centerX + kotlin.math.cos(angle) * radius).toFloat()
                val y = (centerY + kotlin.math.sin(angle) * radius).toFloat()
                positions.add(Pair(x, y))
            }
            
            return positions
        }
    }
    
    companion object {
        /** Стандартные формации */
        val V_SMALL = VFormation(count = 3)
        val V_LARGE = VFormation(count = 7)
        val LINE_HORIZONTAL = LineFormation(count = 5)
        val LINE_VERTICAL = LineFormation(count = 5, vertical = true)
        val CIRCLE_SMALL = CircleFormation(count = 4)
        val CIRCLE_LARGE = CircleFormation(count = 8)
    }
}
