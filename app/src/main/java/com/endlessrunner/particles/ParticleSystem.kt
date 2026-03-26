package com.endlessrunner.particles

import android.graphics.Canvas
import android.util.Log

/**
 * Глобальная система частиц.
 * Singleton для управления всеми эмиттерами частиц в игре.
 */
class ParticleSystem private constructor() {
    companion object {
        private const val TAG = "ParticleSystem"
        
        /** Singleton instance */
        @Volatile
        private var instance: ParticleSystem? = null
        
        /** Получение singleton экземпляра */
        fun getInstance(): ParticleSystem {
            return instance ?: synchronized(this) {
                instance ?: ParticleSystem().also { instance = it }
            }
        }
        
        /** Сброс singleton (для тестов) */
        fun resetInstance() {
            instance = null
        }
    }
    
    /** Все активные эмиттеры */
    private val emitters: MutableList<ParticleEmitter> = mutableListOf()
    
    /** Глобальный пул частиц */
    private val globalPool: ParticlePool by lazy {
        ParticlePool(initialSize = 500, maxSize = maxParticles)
    }
    
    /** Максимальное количество частиц */
    var maxParticles: Int = 2000
    
    /** Максимальное количество эмиттеров */
    var maxEmitters: Int = 50
    
    /** Пауза системы */
    var isPaused: Boolean = false
    
    /** Отладочный режим */
    var debugMode: Boolean = false
    
    /** Статистика */
    var totalParticlesRendered: Int = 0
        private set
    
    /**
     * Создание нового эмиттера.
     *
     * @param config Конфигурация эмиттера
     * @return Созданный эмиттер
     */
    fun createEmitter(config: ParticleConfig = ParticleConfig.DEFAULT): ParticleEmitter {
        if (emitters.size >= maxEmitters) {
            Log.w(TAG, "Достигнут лимит эмиттеров ($maxEmitters)")
            // Удаляем самый старый неактивный эмиттер
            val oldEmitter = emitters.find { !it.isActive }
            if (oldEmitter != null) {
                removeEmitter(oldEmitter)
            } else {
                Log.w(TAG, "Нет неактивных эмиттеров для удаления")
            }
        }
        
        val emitter = ParticleEmitter(config)
        emitter.setParticlePool(globalPool)
        emitter.debugMode = debugMode
        emitters.add(emitter)
        
        if (debugMode) {
            Log.d(TAG, "Создан эмиттер. Всего: ${emitters.size}")
        }
        
        return emitter
    }
    
    /**
     * Создание эмиттера в точке.
     */
    fun createEmitterAt(
        x: Float,
        y: Float,
        config: ParticleConfig = ParticleConfig.DEFAULT
    ): ParticleEmitter {
        return createEmitter(config).apply {
            this.x = x
            this.y = y
        }
    }
    
    /**
     * Удаление эмиттера.
     *
     * @param emitter Эмиттер для удаления
     * @return true если удалён успешно
     */
    fun removeEmitter(emitter: ParticleEmitter): Boolean {
        if (emitters.remove(emitter)) {
            emitter.dispose()
            
            if (debugMode) {
                Log.d(TAG, "Удалён эмиттер. Всего: ${emitters.size}")
            }
            
            return true
        }
        return false
    }
    
    /**
     * Удаление всех эмиттеров.
     */
    fun removeAllEmitters() {
        emitters.forEach { it.dispose() }
        emitters.clear()
        
        if (debugMode) {
            Log.d(TAG, "Все эмиттеры удалены")
        }
    }
    
    /**
     * Обновление системы частиц.
     *
     * @param deltaTime Время с последнего кадра
     */
    fun update(deltaTime: Float) {
        if (isPaused) return
        
        // Обновление эмиттеров
        val iterator = emitters.iterator()
        
        while (iterator.hasNext()) {
            val emitter = iterator.next()
            
            if (emitter.isActive || emitter.hasActiveParticles()) {
                emitter.update(deltaTime)
                
                // Авто-удаление завершённых эмиттеров
                if (!emitter.isActive && !emitter.hasActiveParticles()) {
                    // Можно оставить эмиттер для переиспользования
                    // или удалить если их слишком много
                    if (emitters.size > maxEmitters / 2) {
                        iterator.remove()
                        emitter.dispose()
                    }
                }
            } else {
                // Неактивный эмиттер без частиц - кандидат на удаление
                if (emitters.size > maxEmitters / 2) {
                    iterator.remove()
                    emitter.dispose()
                }
            }
        }
        
        // Обновление статистики
        totalParticlesRendered = emitters.sumOf { it.getParticleCount() }
    }
    
    /**
     * Отрисовка всех частиц.
     */
    fun render(canvas: Canvas) {
        if (isPaused) return
        
        // Сортировка эмиттеров по приоритету (опционально)
        // emitters.sortBy { it.config.zIndex }
        
        // Отрисовка всех эмиттеров
        for (emitter in emitters) {
            emitter.render(canvas)
        }
    }
    
    /**
     * Очистка всех частиц.
     */
    fun clear() {
        emitters.forEach { emitter ->
            emitter.clearAllParticles()
            emitter.stop()
        }
        globalPool.clear()
        
        if (debugMode) {
            Log.d(TAG, "Система частиц очищена")
        }
    }
    
    /**
     * Получение эмиттера по индексу.
     */
    fun getEmitter(index: Int): ParticleEmitter? {
        return emitters.getOrNull(index)
    }
    
    /**
     * Получение всех эмиттеров.
     */
    fun getAllEmitters(): List<ParticleEmitter> = emitters.toList()
    
    /**
     * Получение количества эмиттеров.
     */
    fun getEmitterCount(): Int = emitters.size
    
    /**
     * Получение количества активных частиц.
     */
    fun getActiveParticleCount(): Int {
        return emitters.sumOf { it.getParticleCount() }
    }
    
    /**
     * Получение статистики системы.
     */
    fun getStats(): SystemStats {
        return SystemStats(
            emitterCount = emitters.size,
            activeParticles = getActiveParticleCount(),
            pooledParticles = globalPool.pooledCount,
            totalParticles = globalPool.totalCount,
            maxParticles = maxParticles,
            maxEmitters = maxEmitters
        )
    }
    
    /**
     * Сброс системы.
     */
    fun reset() {
        removeAllEmitters()
        globalPool.clear()
        isPaused = false
        totalParticlesRendered = 0
    }
    
    /**
     * Освобождение ресурсов.
     */
    fun dispose() {
        reset()
        globalPool.dispose()
    }
    
    /**
     * Настройка качества частиц.
     *
     * @param quality Уровень качества
     */
    fun setQuality(quality: ParticleQuality) {
        when (quality) {
            ParticleQuality.LOW -> {
                maxParticles = 500
                maxEmitters = 20
            }
            ParticleQuality.MEDIUM -> {
                maxParticles = 1500
                maxEmitters = 40
            }
            ParticleQuality.HIGH -> {
                maxParticles = 3000
                maxEmitters = 60
            }
        }
    }
    
    /**
     * Data class для статистики.
     */
    data class SystemStats(
        val emitterCount: Int,
        val activeParticles: Int,
        val pooledParticles: Int,
        val totalParticles: Int,
        val maxParticles: Int,
        val maxEmitters: Int
    ) {
        /** Процент использования пула */
        val poolUtilization: Float
            get() = if (totalParticles == 0) 0f else activeParticles.toFloat() / totalParticles
        
        /** Процент использования лимита эмиттеров */
        val emitterUtilization: Float
            get() = emitterCount.toFloat() / maxEmitters
    }
}

/**
 * Уровни качества частиц.
 */
enum class ParticleQuality {
    LOW,      // Низкое (для слабых устройств)
    MEDIUM,   // Среднее (баланс)
    HIGH      // Высокое (для мощных устройств)
}

/**
 * Extension свойство для получения singleton экземпляра.
 */
val ParticleSystem.Companion.instance: ParticleSystem
    get() = ParticleSystem.getInstance()

/**
 * Extension функция для быстрого создания эффекта в точке.
 */
fun ParticleSystem.emit(
    x: Float,
    y: Float,
    config: ParticleConfig,
    count: Int = 1
): ParticleEmitter {
    return createEmitter(config).apply {
        start(x = x, y = y, oneShot = true, count = count)
    }
}

/**
 * Extension функция для создания burst эффекта.
 */
fun ParticleSystem.burst(
    x: Float,
    y: Float,
    config: ParticleConfig,
    count: Int = 20
): ParticleEmitter {
    return createEmitter(config).apply {
        this.x = x
        this.y = y
        burst(count)
    }
}

/**
 * Extension функция для создания постоянного эффекта.
 */
fun ParticleSystem.continuous(
    x: Float,
    y: Float,
    config: ParticleConfig
): ParticleEmitter {
    return createEmitter(config).apply {
        this.x = x
        this.y = y
        start()
    }
}
