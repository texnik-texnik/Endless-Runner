package com.endlessrunner.particles

import android.util.Log

/**
 * Object pool для частиц.
 * Эффективное переиспользование объектов частиц для минимизации GC.
 *
 * @param initialSize Начальный размер пула
 * @param maxSize Максимальный размер пула
 */
class ParticlePool(
    private val initialSize: Int = 100,
    private val maxSize: Int = 1000
) {
    companion object {
        private const val TAG = "ParticlePool"
    }
    
    /** Пул неактивных частиц */
    private val pool: ArrayDeque<Particle> = ArrayDeque(initialSize)
    
    /** Все созданные частицы */
    private val allParticles: MutableList<Particle> = mutableListOf()
    
    /** Количество созданных частиц */
    private var createdCount: Int = 0
    
    /** Количество активных частиц */
    var activeCount: Int = 0
        private set
    
    /** Количество частиц в пуле */
    val pooledCount: Int
        get() = pool.size
    
    /** Общее количество частиц */
    val totalCount: Int
        get() = allParticles.size
    
    /** Статистика пула */
    val stats: PoolStats
        get() = PoolStats(
            activeCount = activeCount,
            pooledCount = pooledCount,
            totalCount = totalCount,
            createdCount = createdCount
        )
    
    init {
        // Предварительное создание частиц
        expand(initialSize)
    }
    
    /**
     * Получение частицы из пула.
     *
     * @return Частица из пула или новая
     */
    fun acquire(): Particle {
        val particle: Particle
        
        if (pool.isNotEmpty()) {
            // Берём из пула
            particle = pool.removeLast()
        } else {
            // Создаём новую если можно
            if (maxSize == 0 || totalCount < maxSize) {
                particle = createNewParticle()
            } else {
                // Пул достиг максимума
                Log.w(TAG, "ParticlePool достиг максимума ($maxSize). Возвращаем null-подобную частицу.")
                // Возвращаем новую частицу вне пула (не оптимально, но работает)
                return Particle()
            }
        }
        
        // Активируем
        particle.isActive = true
        activeCount++
        
        return particle
    }
    
    /**
     * Возврат частицы в пул.
     *
     * @param particle Частица для возврата
     * @return true если успешно
     */
    fun release(particle: Particle): Boolean {
        // Проверяем что частица активна и принадлежит пулу
        if (!particle.isActive || !allParticles.contains(particle)) {
            return false
        }
        
        // Сбрасываем и деактивируем
        particle.reset()
        particle.isActive = false
        activeCount--
        
        // Возвращаем в пул
        pool.addLast(particle)
        
        return true
    }
    
    /**
     * Расширение пула.
     *
     * @param count Количество для добавления
     * @return Фактически создано
     */
    fun expand(count: Int = 1): Int {
        var created = 0
        
        repeat(count) {
            if (maxSize > 0 && totalCount >= maxSize) {
                return created
            }
            
            val particle = createNewParticle()
            particle.isActive = false
            pool.addLast(particle)
            created++
        }
        
        return created
    }
    
    /**
     * Расширение до указанного размера.
     */
    fun expandTo(targetSize: Int): Int {
        if (targetSize <= totalCount) return 0
        return expand(targetSize - totalCount)
    }
    
    /**
     * Очистка пула.
     * Все частицы деактивируются и возвращаются.
     */
    fun clear() {
        allParticles.forEach { particle ->
            if (particle.isActive) {
                particle.reset()
                particle.isActive = false
            }
            if (!pool.contains(particle)) {
                pool.addLast(particle)
            }
        }
        activeCount = 0
    }
    
    /**
     * Полное освобождение пула.
     */
    fun dispose() {
        pool.clear()
        allParticles.clear()
        createdCount = 0
        activeCount = 0
    }
    
    /**
     * Создание новой частицы.
     */
    private fun createNewParticle(): Particle {
        val particle = Particle()
        allParticles.add(particle)
        createdCount++
        return particle
    }
    
    /**
     * Получение всех активных частиц.
     */
    fun getActiveParticles(): List<Particle> = allParticles.filter { it.isActive }
    
    /**
     * Получение всех частиц.
     */
    fun getAllParticles(): List<Particle> = allParticles.toList()
    
    /**
     * Принудительная активация частицы (для внешних частиц).
     */
    fun activate(particle: Particle) {
        if (!particle.isActive && allParticles.contains(particle)) {
            particle.isActive = true
            activeCount++
        }
    }
    
    /**
     * Принудительная деактивация частицы.
     */
    fun deactivate(particle: Particle) {
        if (particle.isActive && allParticles.contains(particle)) {
            particle.reset()
            particle.isActive = false
            activeCount--
            if (!pool.contains(particle)) {
                pool.addLast(particle)
            }
        }
    }
    
    /**
     * Data class для статистики.
     */
    data class PoolStats(
        val activeCount: Int,
        val pooledCount: Int,
        val totalCount: Int,
        val createdCount: Int
    ) {
        /** Процент использования */
        val utilizationRate: Float
            get() = if (totalCount == 0) 0f else activeCount.toFloat() / totalCount
        
        /** Доступно места */
        val availableSpace: Int
            get() = if (totalCount < maxSize) maxSize - totalCount else 0
        
        /** Процент попаданий в пул */
        val poolHitRate: Float
            get() = if (createdCount == 0) 1f else 1f - (createdCount.toFloat() / (activeCount + createdCount))
    }
}

/**
 * Extension функция для получения или создания частицы.
 */
fun ParticlePool.acquireOrNew(): Particle {
    return if (totalCount < maxSize) acquire() else Particle()
}

/**
 * Extension функция для массового получения частиц.
 */
fun ParticlePool.acquireMultiple(count: Int): List<Particle> {
    return List(count) { acquire() }
}

/**
 * Extension функция для массового возврата частиц.
 */
fun ParticlePool.releaseMultiple(particles: Collection<Particle>) {
    particles.forEach { release(it) }
}
