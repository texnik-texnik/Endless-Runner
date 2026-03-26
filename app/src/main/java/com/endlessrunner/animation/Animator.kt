package com.endlessrunner.animation

import android.graphics.Bitmap
import android.util.Log

/**
 * Менеджер анимаций.
 * Централизованное хранилище и управление анимациями.
 */
class Animator {
    companion object {
        private const val TAG = "Animator"
    }
    
    /** Карта всех загруженных анимаций по имени */
    private val animations: MutableMap<String, Animation> = mutableMapOf()
    
    /** Текущая воспроизводимая анимация */
    private var currentAnimation: Animation? = null
    
    /** Имя текущей анимации */
    private var currentAnimationName: String? = null
    
    /** Callback на завершение анимации */
    var onAnimationComplete: ((String) -> Unit)? = null
    
    /** Флаг логгирования */
    var debugMode: Boolean = false
    
    /**
     * Загрузка анимации.
     *
     * @param name Уникальное имя анимации
     * @param frames Кадры анимации
     * @param duration Длительность одного кадра (секунды)
     * @param loop Зациклена ли анимация
     * @return true если загружено успешно
     */
    fun loadAnimation(
        name: String,
        frames: List<Bitmap>,
        duration: Float = 0.1f,
        loop: Boolean = true
    ): Boolean {
        if (frames.isEmpty()) {
            Log.w(TAG, "Попытка загрузки пустой анимации: $name")
            return false
        }
        
        val animation = Animation(frames, duration, loop)
        animations[name] = animation
        
        if (debugMode) {
            Log.d(TAG, "Загружена анимация: $name (${frames.size} кадров, ${animation.totalDuration}s)")
        }
        
        return true
    }
    
    /**
     * Загрузка анимации из массива.
     */
    fun loadAnimation(
        name: String,
        vararg frames: Bitmap,
        duration: Float = 0.1f,
        loop: Boolean = true
    ): Boolean {
        return loadAnimation(name, frames.toList(), duration, loop)
    }
    
    /**
     * Получение анимации по имени.
     *
     * @param name Имя анимации
     * @return Animation или null
     */
    fun getAnimation(name: String): Animation? {
        return animations[name]
    }
    
    /**
     * Получение или загрузка анимации.
     */
    fun getOrLoadAnimation(
        name: String,
        frames: List<Bitmap>,
        duration: Float = 0.1f,
        loop: Boolean = true
    ): Animation {
        return animations[name] ?: run {
            loadAnimation(name, frames, duration, loop)
            animations[name]!!
        }
    }
    
    /**
     * Воспроизведение анимации.
     *
     * @param name Имя анимации
     * @param force Принудительный перезапуск
     * @return true если анимация найдена и запущена
     */
    fun play(name: String, force: Boolean = false): Boolean {
        val animation = animations[name]
        
        if (animation == null) {
            Log.w(TAG, "Анимация не найдена: $name")
            return false
        }
        
        // Если уже воспроизводится эта анимация
        if (currentAnimation == animation && animation.isPlaying && !force) {
            return true
        }
        
        currentAnimation = animation
        currentAnimationName = name
        animation.start()
        
        if (debugMode) {
            Log.d(TAG, "Воспроизведение: $name")
        }
        
        return true
    }
    
    /**
     * Остановка текущей анимации.
     *
     * @param resetFrame Сбросить ли кадр
     */
    fun stop(resetFrame: Boolean = false) {
        currentAnimation?.stop(resetFrame)
        currentAnimation = null
        currentAnimationName = null
    }
    
    /**
     * Обновление текущей анимации.
     *
     * @param deltaTime Время с последнего кадра
     */
    fun update(deltaTime: Float) {
        val animation = currentAnimation
        animation?.update(deltaTime)
        
        // Проверка завершения
        if (animation?.isFinished() == true) {
            onAnimationComplete?.invoke(currentAnimationName ?: "")
            if (debugMode) {
                Log.d(TAG, "Анимация завершена: $currentAnimationName")
            }
        }
    }
    
    /**
     * Получение текущего кадра.
     */
    fun getCurrentFrame(): Bitmap? {
        return currentAnimation?.getCurrentFrame()
    }
    
    /**
     * Проверка, воспроизводится ли анимация.
     */
    fun isPlaying(): Boolean = currentAnimation?.isPlaying ?: false
    
    /**
     * Проверка, завершена ли текущая анимация.
     */
    fun isFinished(): Boolean = currentAnimation?.isFinished() ?: false
    
    /**
     * Получение имени текущей анимации.
     */
    fun getCurrentAnimationName(): String? = currentAnimationName
    
    /**
     * Проверка наличия анимации.
     */
    fun hasAnimation(name: String): Boolean = animations.containsKey(name)
    
    /**
     * Удаление анимации.
     */
    fun removeAnimation(name: String): Boolean {
        if (currentAnimationName == name) {
            stop()
        }
        return animations.remove(name) != null
    }
    
    /**
     * Очистка всех анимаций.
     */
    fun clear() {
        stop()
        animations.clear()
        if (debugMode) {
            Log.d(TAG, "Все анимации очищены")
        }
    }
    
    /**
     * Получение списка всех имён анимаций.
     */
    fun getAnimationNames(): Set<String> = animations.keys
    
    /**
     * Количество загруженных анимаций.
     */
    fun getAnimationCount(): Int = animations.size
    
    /**
     * Предзагрузка набора анимаций.
     */
    fun preloadAll(vararg names: String): Int {
        var loaded = 0
        names.forEach { name ->
            if (animations.containsKey(name)) {
                loaded++
            }
        }
        return loaded
    }
}

/**
 * Extension функция для создания Animator с начальными анимациями.
 */
fun animator(
    vararg animations: Pair<String, AnimationData>
): Animator {
    val animator = Animator()
    animations.forEach { (name, data) ->
        animator.loadAnimation(name, data.frames, data.frameDuration, data.isLooping)
    }
    return animator
}

/**
 * Data class для описания анимации.
 */
data class AnimationData(
    val frames: List<Bitmap>,
    val frameDuration: Float = 0.1f,
    val isLooping: Boolean = true
)

/**
 * Extension функция для копирования всех анимаций из другого Animator.
 */
fun Animator.copyFrom(other: Animator) {
    other.animations.forEach { (name, anim) ->
        this.loadAnimation(name, anim.frames, anim.frameDuration, anim.isLooping)
    }
}

/**
 * Extension функция для получения статистики Animator.
 */
fun Animator.getStats(): AnimatorStats {
    return AnimatorStats(
        totalAnimations = getAnimationCount(),
        isPlaying = isPlaying(),
        currentAnimation = getCurrentAnimationName(),
        animationNames = getAnimationNames().toList()
    )
}

/**
 * Data class для статистики Animator.
 */
data class AnimatorStats(
    val totalAnimations: Int,
    val isPlaying: Boolean,
    val currentAnimation: String?,
    val animationNames: List<String>
)
