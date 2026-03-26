package com.endlessrunner.enemies

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.endlessrunner.components.RenderComponent
import com.endlessrunner.core.GameConstants

/**
 * Статичные шипы.
 * Низкий урон, частый спавн.
 * Визуально: треугольники.
 */
class SpikeEnemy(
    damage: Int = EnemyType.STATIC.baseDamage,
    config: EnemyConfig = EnemyConfig.DEFAULT
) : Enemy(
    type = EnemyType.STATIC,
    behavior = StaticBehavior(),
    damage = damage,
    config = config
) {
    
    companion object {
        /** Пул для переиспользования */
        private val pool: com.endlessrunner.core.ObjectPool<SpikeEnemy> = 
            com.endlessrunner.core.ObjectPool(
                initialSize = 30,
                maxSize = 100,
                factory = { SpikeEnemy() }
            )
        
        /** Получение шипа из пула */
        fun acquire(
            damage: Int = EnemyType.STATIC.baseDamage,
            config: EnemyConfig = EnemyConfig.DEFAULT
        ): SpikeEnemy {
            val enemy = pool.acquire()
            enemy.damage = damage
            enemy.config = config
            return enemy
        }
        
        /** Возврат шипа в пул */
        fun release(enemy: SpikeEnemy) {
            pool.release(enemy)
        }
    }
    
    /** Путь для отрисовки треугольника */
    private val spikePath = Path()
    
    /** Paint для отрисовки */
    private val spikePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = type.debugColor
        style = Paint.Style.FILL
    }
    
    /** Paint для обводки */
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    
    /** Флаг направления шипов (вверх/вниз) */
    var isUpsideDown: Boolean = false
    
    /** Количество шипов */
    private var spikeCount: Int = 3
    
    override fun onActivate() {
        super.onActivate()
        updateSpikePath()
    }
    
    override fun render(canvas: Canvas) {
        if (!isActive || isDestroyed) return
        
        val position = positionComponent ?: return
        val physics = physicsComponent ?: return
        
        val bounds = physics.getBounds()
        
        // Сохранение состояния canvas
        canvas.save()
        
        // Отрисовка шипов
        canvas.translate(bounds.centerX(), bounds.centerY())
        
        if (isUpsideDown) {
            canvas.rotate(180f)
        }
        
        canvas.drawPath(spikePath, spikePaint)
        canvas.drawPath(spikePath, outlinePaint)
        
        // Восстановление состояния canvas
        canvas.restore()
        
        // Базовый рендеринг (для компонентов)
        super.render(canvas)
    }
    
    /**
     * Обновление пути шипов.
     */
    private fun updateSpikePath() {
        val physics = physicsComponent ?: return
        val bounds = physics.getBounds()
        
        val width = bounds.width()
        val height = bounds.height()
        
        spikePath.reset()
        
        val spikeWidth = width / spikeCount
        val spikeHeight = height * 0.8f
        
        // Создание зигзагообразного пути
        spikePath.moveTo(-width / 2, height / 2)
        
        for (i in 0 until spikeCount) {
            val x = -width / 2 + i * spikeWidth + spikeWidth / 2
            spikePath.lineTo(x, -height / 2 + height * 0.2f)
            spikePath.lineTo(x + spikeWidth, height / 2)
        }
        
        spikePath.close()
    }
    
    /**
     * Установка количества шипов.
     */
    fun setSpikeCount(count: Int) {
        spikeCount = count.coerceAtLeast(1)
        updateSpikePath()
    }
    
    /**
     * Установка направления шипов.
     */
    fun setUpsideDown(upsideDown: Boolean) {
        isUpsideDown = upsideDown
    }
    
    override fun reset() {
        super.reset()
        isUpsideDown = false
        spikeCount = 3
        updateSpikePath()
    }
    
    override fun destroy() {
        super.destroy()
        pool.release(this)
    }
}

/**
 * Движущиеся блоки.
 * Средний урон, движутся по заданному паттерну.
 * Визуально: квадраты/прямоугольники.
 */
class MovingBlockEnemy(
    damage: Int = EnemyType.MOVING.baseDamage,
    speed: Float = EnemyType.MOVING.baseSpeed,
    pattern: MovementPattern = MovementPattern.OSCILLATING,
    config: EnemyConfig = EnemyConfig.DEFAULT
) : Enemy(
    type = EnemyType.MOVING,
    behavior = MovingBehavior(
        speed = speed,
        pattern = pattern,
        amplitude = 150f,
        frequency = 1f
    ),
    damage = damage,
    config = config
) {
    
    companion object {
        /** Пул для переиспользования */
        private val pool: com.endlessrunner.core.ObjectPool<MovingBlockEnemy> = 
            com.endlessrunner.core.ObjectPool(
                initialSize = 20,
                maxSize = 60,
                factory = { MovingBlockEnemy() }
            )
        
        /** Получение блока из пула */
        fun acquire(
            damage: Int = EnemyType.MOVING.baseDamage,
            speed: Float = EnemyType.MOVING.baseSpeed,
            pattern: MovementPattern = MovementPattern.OSCILLATING,
            config: EnemyConfig = EnemyConfig.DEFAULT
        ): MovingBlockEnemy {
            val enemy = pool.acquire()
            enemy.damage = damage
            enemy.config = config
            (enemy.behavior as? MovingBehavior)?.let {
                it.speed = speed
                it.movementPattern = pattern
            }
            return enemy
        }
        
        /** Возврат блока в пул */
        fun release(enemy: MovingBlockEnemy) {
            pool.release(enemy)
        }
    }
    
    /** Paint для отрисовки блока */
    private val blockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = type.debugColor
        style = Paint.Style.FILL
    }
    
    /** Paint для обводки */
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    
    /** Анимация пульсации */
    private var pulseScale: Float = 1f
    private var pulseTime: Float = 0f
    
    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        
        // Обновление пульсации
        pulseTime += deltaTime * 5f
        pulseScale = 1f + kotlin.math.sin(pulseTime) * 0.05f
    }
    
    override fun render(canvas: Canvas) {
        if (!isActive || isDestroyed) return
        
        val physics = physicsComponent ?: return
        val bounds = physics.getBounds()
        
        // Сохранение состояния canvas
        canvas.save()
        
        // Масштабирование для пульсации
        val scale = pulseScale
        canvas.scale(scale, scale, bounds.centerX(), bounds.centerY())
        
        // Отрисовка блока
        canvas.drawRect(bounds, blockPaint)
        canvas.drawRect(bounds, outlinePaint)
        
        // Декоративные элементы (крест внутри)
        canvas.drawLine(
            bounds.left, bounds.top,
            bounds.right, bounds.bottom,
            outlinePaint
        )
        canvas.drawLine(
            bounds.right, bounds.top,
            bounds.left, bounds.bottom,
            outlinePaint
        )
        
        // Восстановление состояния canvas
        canvas.restore()
        
        super.render(canvas)
    }
    
    override fun destroy() {
        super.destroy()
        pool.release(this)
    }
}

/**
 * Летающий враг.
 * Движение по волне.
 * Визуально: со спрайтом (пока простая форма).
 */
class FlyingEnemy(
    damage: Int = EnemyType.FLYING.baseDamage,
    speed: Float = EnemyType.FLYING.baseSpeed,
    amplitude: Float = 100f,
    frequency: Float = 1.5f,
    config: EnemyConfig = EnemyConfig.DEFAULT
) : Enemy(
    type = EnemyType.FLYING,
    behavior = FlyingBehavior(
        speed = speed,
        amplitude = amplitude,
        frequency = frequency
    ),
    damage = damage,
    config = config
) {
    
    companion object {
        /** Пул для переиспользования */
        private val pool: com.endlessrunner.core.ObjectPool<FlyingEnemy> = 
            com.endlessrunner.core.ObjectPool(
                initialSize = 15,
                maxSize = 50,
                factory = { FlyingEnemy() }
            )
        
        /** Получение летающего врага из пула */
        fun acquire(
            damage: Int = EnemyType.FLYING.baseDamage,
            speed: Float = EnemyType.FLYING.baseSpeed,
            amplitude: Float = 100f,
            frequency: Float = 1.5f,
            config: EnemyConfig = EnemyConfig.DEFAULT
        ): FlyingEnemy {
            val enemy = pool.acquire()
            enemy.damage = damage
            enemy.config = config
            (enemy.behavior as? FlyingBehavior)?.let {
                // Обновление параметров поведения
            }
            return enemy
        }
        
        /** Возврат в пул */
        fun release(enemy: FlyingEnemy) {
            pool.release(enemy)
        }
    }
    
    /** Paint для отрисовки */
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = type.debugColor
        style = Paint.Style.FILL
    }
    
    /** Paint для крыльев */
    private val wingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    
    /** Анимация взмаха крыльев */
    private var wingAngle: Float = 0f
    
    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        
        // Обновление анимации крыльев
        wingAngle += deltaTime * 15f
    }
    
    override fun render(canvas: Canvas) {
        if (!isActive || isDestroyed) return
        
        val position = positionComponent ?: return
        val physics = physicsComponent ?: return
        
        val bounds = physics.getBounds()
        val centerX = bounds.centerX()
        val centerY = bounds.centerY()
        
        canvas.save()
        
        // Отрисовка тела (эллипс)
        val bodyWidth = bounds.width() * 0.6f
        val bodyHeight = bounds.height() * 0.5f
        canvas.drawOval(
            centerX - bodyWidth / 2,
            centerY - bodyHeight / 2,
            centerX + bodyWidth / 2,
            centerY + bodyHeight / 2,
            bodyPaint
        )
        
        // Отрисовка крыльев
        val wingSpan = bounds.width() * 0.8f
        val wingHeight = bounds.height() * 0.4f
        
        val wingFlap = kotlin.math.sin(wingAngle) * 30f
        
        // Левое крыло
        canvas.save()
        canvas.translate(centerX - bodyWidth / 4, centerY)
        canvas.rotate(-wingFlap)
        canvas.drawOval(
            -wingSpan / 2,
            -wingHeight / 2,
            0f,
            wingHeight / 2,
            wingPaint
        )
        canvas.restore()
        
        // Правое крыло
        canvas.save()
        canvas.translate(centerX + bodyWidth / 4, centerY)
        canvas.rotate(wingFlap)
        canvas.drawOval(
            0f,
            -wingHeight / 2,
            wingSpan / 2,
            wingHeight / 2,
            wingPaint
        )
        canvas.restore()
        
        // Глаза
        val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX + 10f, centerY - 5f, 8f, eyePaint)
        
        canvas.restore()
        
        super.render(canvas)
    }
    
    override fun destroy() {
        super.destroy()
        pool.release(this)
    }
}

/**
 * Прыгающий враг.
 * Периодические прыжки.
 * Визуально: анимация сжатия/растяжения.
 */
class JumpingEnemy(
    damage: Int = EnemyType.JUMPING.baseDamage,
    speed: Float = EnemyType.JUMPING.baseSpeed,
    jumpInterval: Float = 2f,
    jumpForce: Float = -800f,
    config: EnemyConfig = EnemyConfig.DEFAULT
) : Enemy(
    type = EnemyType.JUMPING,
    behavior = JumpingBehavior(
        speed = speed,
        jumpInterval = jumpInterval,
        jumpForce = jumpForce
    ),
    damage = damage,
    config = config
) {
    
    companion object {
        /** Пул для переиспользования */
        private val pool: com.endlessrunner.core.ObjectPool<JumpingEnemy> = 
            com.endlessrunner.core.ObjectPool(
                initialSize = 15,
                maxSize = 50,
                factory = { JumpingEnemy() }
            )
        
        /** Получение прыгающего врага из пула */
        fun acquire(
            damage: Int = EnemyType.JUMPING.baseDamage,
            speed: Float = EnemyType.JUMPING.baseSpeed,
            jumpInterval: Float = 2f,
            jumpForce: Float = -800f,
            config: EnemyConfig = EnemyConfig.DEFAULT
        ): JumpingEnemy {
            val enemy = pool.acquire()
            enemy.damage = damage
            enemy.config = config
            return enemy
        }
        
        /** Возврат в пул */
        fun release(enemy: JumpingEnemy) {
            pool.release(enemy)
        }
    }
    
    /** Paint для отрисовки */
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = type.debugColor
        style = Paint.Style.FILL
    }
    
    /** Paint для глаз */
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    
    /** Paint для зрачков */
    private val pupilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.BLACK
        style = Paint.Style.FILL
    }
    
    /** Коэффициент сжатия/растяжения */
    private var squashStretch: Float = 1f
    
    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        
        // Анимация сжатия/растяжения
        val jumpingBehavior = behavior as? JumpingBehavior
        if (jumpingBehavior?.isAirborne == true) {
            // Растяжение в прыжке
            squashStretch = 1.2f
        } else {
            // Возврат к норме
            squashStretch = 1f
        }
    }
    
    override fun render(canvas: Canvas) {
        if (!isActive || isDestroyed) return
        
        val physics = physicsComponent ?: return
        val bounds = physics.getBounds()
        val centerX = bounds.centerX()
        val centerY = bounds.centerY()
        
        canvas.save()
        
        // Трансформация для сжатия/растяжения
        val scaleX = 1f / squashStretch
        val scaleY = squashStretch
        canvas.scale(scaleX, scaleY, centerX, centerY)
        
        // Отрисовка тела (круг/эллипс)
        canvas.drawCircle(centerX, centerY, bounds.width() / 2 * 0.8f, bodyPaint)
        
        // Глаза
        val eyeOffsetX = bounds.width() * 0.2f
        val eyeOffsetY = bounds.height() * 0.1f
        val eyeRadius = bounds.width() * 0.12f
        
        // Левый глаз
        canvas.drawCircle(centerX - eyeOffsetX, centerY - eyeOffsetY, eyeRadius, eyePaint)
        canvas.drawCircle(centerX - eyeOffsetX + 2f, centerY - eyeOffsetY, eyeRadius * 0.5f, pupilPaint)
        
        // Правый глаз
        canvas.drawCircle(centerX + eyeOffsetX, centerY - eyeOffsetY, eyeRadius, eyePaint)
        canvas.drawCircle(centerX + eyeOffsetX + 2f, centerY - eyeOffsetY, eyeRadius * 0.5f, pupilPaint)
        
        // Улыбка
        val mouthPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawArc(
            centerX - bounds.width() * 0.3f,
            centerY,
            centerX + bounds.width() * 0.3f,
            centerY + bounds.height() * 0.3f,
            0f,
            180f,
            false,
            mouthPaint
        )
        
        canvas.restore()
        
        super.render(canvas)
    }
    
    override fun destroy() {
        super.destroy()
        pool.release(this)
    }
}

/**
 * Extension функция для создания врага нужного типа.
 */
fun createEnemyByType(
    type: EnemyType,
    x: Float,
    y: Float,
    difficultyMultiplier: Float = 1f
): Enemy {
    return when (type) {
        EnemyType.STATIC -> SpikeEnemy.acquire(
            damage = (type.baseDamage * difficultyMultiplier).toInt()
        ).apply {
            positionComponent?.setPosition(x, y)
        }
        EnemyType.MOVING -> MovingBlockEnemy.acquire(
            damage = (type.baseDamage * difficultyMultiplier).toInt(),
            speed = type.baseSpeed * difficultyMultiplier
        ).apply {
            positionComponent?.setPosition(x, y)
        }
        EnemyType.FLYING -> FlyingEnemy.acquire(
            damage = (type.baseDamage * difficultyMultiplier).toInt(),
            speed = type.baseSpeed * difficultyMultiplier
        ).apply {
            positionComponent?.setPosition(x, y)
        }
        EnemyType.JUMPING -> JumpingEnemy.acquire(
            damage = (type.baseDamage * difficultyMultiplier).toInt(),
            speed = type.baseSpeed * difficultyMultiplier
        ).apply {
            positionComponent?.setPosition(x, y)
        }
        EnemyType.HAZARD -> SpikeEnemy.acquire(
            damage = (type.baseDamage * difficultyMultiplier).toInt()
        ).apply {
            isUpsideDown = true
            positionComponent?.setPosition(x, y)
        }
    }
}
