package com.endlessrunner.particles

import android.graphics.Canvas
import android.util.Log

/**
 * Эмиттер частиц.
 * Генерирует и управляет частицами согласно конфигурации.
 *
 * @param config Конфигурация эмиттера
 */
class ParticleEmitter(
    /** Конфигурация эмиттера */
    var config: ParticleConfig = ParticleConfig.DEFAULT
) {
    companion object {
        private const val TAG = "ParticleEmitter"
    }
    
    /** Позиция эмиттера X */
    var x: Float = 0f
    
    /** Позиция эмиттера Y */
    var y: Float = 0f
    
    /** Смещение позиции X */
    var offsetX: Float = 0f
    
    /** Смещение позиции Y */
    var offsetY: Float = 0f
    
    /** Активен ли эмиттер */
    var isActive: Boolean = false
        private set
    
    /** Пул частиц для этого эмиттера */
    private var particlePool: ParticlePool? = null
    
    /** Активные частицы */
    private val particles: MutableList<Particle> = mutableListOf()
    
    /** Таймер эмиссии */
    private var emissionTimer: Float = 0f
    
    /** Количество испущенных частиц */
    private var emittedCount: Long = 0
    
    /** Общее количество частиц для одноразовой эмиссии */
    private var totalEmitCount: Int = 0
    
    /** Флаг одноразовой эмиссии */
    private var isOneShot: Boolean = false
    
    /** Счётчик для burst режима */
    private var burstRemaining: Int = 0
    
    /** Интервал между burst частицами */
    private var burstInterval: Float = 0f
    
    /** Таймер для burst */
    private var burstTimer: Float = 0f
    
    /** Callback на завершение эмиссии */
    var onEmissionComplete: (() -> Unit)? = null
    
    /** Флаг отладки */
    var debugMode: Boolean = false
    
    /**
     * Установка пула частиц.
     */
    fun setParticlePool(pool: ParticlePool) {
        particlePool = pool
    }
    
    /**
     * Старт эмиссии.
     *
     * @param x Позиция X
     * @param y Позиция Y
     * @param oneShot Одноразовая эмиссия
     * @param count Количество частиц (для oneShot)
     */
    fun start(x: Float = this.x, y: Float = this.y, oneShot: Boolean = false, count: Int = 0) {
        this.x = x
        this.y = y
        isActive = true
        emissionTimer = 0f
        isOneShot = oneShot
        totalEmitCount = if (oneShot) count else 0
        emittedCount = 0
        
        if (debugMode) {
            Log.d(TAG, "Эмиттер запущен: oneShot=$oneShot, count=$count")
        }
    }
    
    /**
     * Старт эмиссии в точке.
     */
    fun startAt(x: Float, y: Float) {
        start(x = x, y = y)
    }
    
    /**
     * Остановка эмиссии.
     *
     * @param clearParticles Очистить ли частицы
     */
    fun stop(clearParticles: Boolean = false) {
        isActive = false
        emissionTimer = 0f
        burstRemaining = 0
        
        if (clearParticles) {
            clearAllParticles()
        }
        
        if (debugMode) {
            Log.d(TAG, "Эмиттер остановлен")
        }
    }
    
    /**
     * Мгновенный выброс частиц (burst).
     *
     * @param count Количество частиц
     * @param interval Интервал между частицами (0 = мгновенно)
     */
    fun burst(count: Int, interval: Float = 0f) {
        if (!isActive) {
            start()
        }
        
        burstRemaining = count
        burstInterval = interval
        burstTimer = 0f
        isOneShot = true
        
        if (interval <= 0) {
            // Мгновенный выброс
            repeat(count) {
                emitParticle()
            }
        }
    }
    
    /**
     * Обновление эмиттера.
     *
     * @param deltaTime Время с последнего кадра
     */
    fun update(deltaTime: Float) {
        if (!isActive) return
        
        // Обновление burst режима
        if (burstRemaining > 0) {
            burstTimer += deltaTime
            
            if (burstInterval > 0) {
                while (burstTimer >= burstInterval && burstRemaining > 0) {
                    emitParticle()
                    burstRemaining--
                    burstTimer -= burstInterval
                }
            } else {
                // Уже обработано в burst()
                burstRemaining = 0
            }
        }
        
        // Обычная эмиссия
        if (!isOneShot || emittedCount < totalEmitCount) {
            emissionTimer += deltaTime
            
            // Проверка частоты эмиссии
            val emissionInterval = 1f / config.emissionRate
            
            while (emissionTimer >= emissionInterval) {
                emitParticle()
                emissionTimer -= emissionInterval
                emittedCount++
                
                // Проверка завершения одноразовой эмиссии
                if (isOneShot && emittedCount >= totalEmitCount) {
                    isActive = false
                    onEmissionComplete?.invoke()
                    break
                }
            }
        }
        
        // Обновление частиц
        updateParticles(deltaTime)
    }
    
    /**
     * Обновление частиц.
     */
    private fun updateParticles(deltaTime: Float) {
        val iterator = particles.iterator()
        
        while (iterator.hasNext()) {
            val particle = iterator.next()
            
            if (particle.isActive) {
                particle.update(deltaTime)
            } else {
                // Частица мертва - возвращаем в пул
                particlePool?.release(particle)
                iterator.remove()
            }
        }
    }
    
    /**
     * Отрисовка частиц.
     */
    fun render(canvas: Canvas) {
        for (particle in particles) {
            if (particle.isActive) {
                particle.render(canvas)
            }
        }
        
        // Отладочная отрисовка
        if (debugMode) {
            renderDebug(canvas)
        }
    }
    
    /**
     * Отрисовка отладочной информации.
     */
    private fun renderDebug(canvas: Canvas) {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.GREEN
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2f
        }
        
        // Рисуем точку эмиттера
        canvas.drawCircle(x + offsetX, y + offsetY, 5f, paint)
        
        // Текст со статистикой
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 20f
        }
        
        canvas.drawText(
            "Particles: ${particles.size}",
            x + offsetX + 10f,
            y + offsetY - 10f,
            textPaint
        )
    }
    
    /**
     * Создание и испускание частицы.
     */
    private fun emitParticle() {
        // Получение частицы из пула
        val particle = particlePool?.acquire() ?: Particle()
        
        // Получение позиции в области эмиссии
        val (emitX, emitY) = config.getEmissionPosition(x + offsetX, y + offsetY)
        
        // Получение случайного угла и скорости
        val angle = config.getRandomAngle()
        val (vx, vy) = config.getVelocity(angle)
        
        // Инициализация частицы
        particle.emit(emitX, emitY, vx, vy, config)
        
        // Применение случайных вариаций
        applyParticleVariations(particle)
        
        particles.add(particle)
    }
    
    /**
     * Применение случайных вариаций к частице.
     */
    private fun applyParticleVariations(particle: Particle) {
        // Случайный цвет из вариаций
        if (config.colorVariations.isNotEmpty()) {
            particle.startColor = config.getRandomColor()
        }
        
        // Случайный размер
        particle.startSize = config.getRandomSize()
        particle.endSize = config.getRandomSize() * 0.5f
        
        // Случайное вращение
        if (config.randomRotation) {
            particle.rotation = (Math.random() * 360).toFloat()
        }
        
        // Случайное время жизни
        particle.lifetime = config.getRandomLifetime()
    }
    
    /**
     * Очистка всех частиц.
     */
    fun clearAllParticles() {
        particles.forEach { particle ->
            particlePool?.release(particle)
        }
        particles.clear()
    }
    
    /**
     * Получение количества активных частиц.
     */
    fun getParticleCount(): Int = particles.count { it.isActive }
    
    /**
     * Получение количества испущенных частиц.
     */
    fun getEmittedCount(): Long = emittedCount
    
    /**
     * Проверка, завершена ли эмиссия.
     */
    fun isEmissionComplete(): Boolean {
        return !isActive && particles.isEmpty()
    }
    
    /**
     * Проверка, есть ли активные частицы.
     */
    fun hasActiveParticles(): Boolean = particles.any { it.isActive }
    
    /**
     * Сброс эмиттера.
     */
    fun reset() {
        stop(clearParticles = true)
        x = 0f
        y = 0f
        offsetX = 0f
        offsetY = 0f
        emittedCount = 0
        totalEmitCount = 0
        isOneShot = false
        burstRemaining = 0
        burstTimer = 0f
        emissionTimer = 0f
    }
    
    /**
     * Освобождение ресурсов.
     */
    fun dispose() {
        reset()
        particlePool = null
        onEmissionComplete = null
    }
}

/**
 * Extension функция для создания и запуска эмиттера.
 */
fun ParticleEmitter.emitAt(x: Float, y: Float, count: Int = 1) {
    start(x = x, y = y, oneShot = true, count = count)
}

/**
 * Extension функция для создания burst эффекта.
 */
fun ParticleEmitter.explode(count: Int = 20, spread: Float = 360f) {
    val oldSpread = config.spreadAngle
    config.copy(spreadAngle = spread)
    burst(count)
    config.copy(spreadAngle = oldSpread)
}

/**
 * Builder для ParticleEmitter.
 */
class ParticleEmitterBuilder {
    private var config: ParticleConfig = ParticleConfig.DEFAULT
    private var x: Float = 0f
    private var y: Float = 0f
    private var particlePool: ParticlePool? = null
    
    fun config(config: ParticleConfig) = apply { this.config = config }
    fun position(x: Float, y: Float) = apply { this.x = x; this.y = y }
    fun pool(pool: ParticlePool) = apply { particlePool = pool }
    
    fun build(): ParticleEmitter {
        return ParticleEmitter(config).apply {
            this.x = this@ParticleEmitterBuilder.x
            this.y = this@ParticleEmitterBuilder.y
            particlePool?.let { setParticlePool(it) }
        }
    }
}

/**
 * DSL функция для создания ParticleEmitter.
 */
fun particleEmitter(block: ParticleEmitterBuilder.() -> Unit): ParticleEmitter {
    return ParticleEmitterBuilder().apply(block).build()
}
