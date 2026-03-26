package com.endlessrunner.animation

/**
 * Sealed class состояний анимации персонажа.
 * Используется для управления переключением между анимациями.
 */
sealed class AnimationState(
    /** Уникальное имя состояния */
    val name: String,
    
    /** Приоритет состояния (более высокий перебивает низкий) */
    val priority: Int = 0,
    
    /** Может ли это состояние прервать другое */
    val canInterrupt: Boolean = true
) {
    /**
     * Покой. Базовое состояние без движения.
     */
    object Idle : AnimationState("idle", priority = 0, canInterrupt = true)
    
    /**
     * Бег. Движение по горизонтали.
     */
    object Running : AnimationState("running", priority = 1, canInterrupt = true)
    
    /**
     * Прыжок. Движение вверх.
     */
    object Jumping : AnimationState("jumping", priority = 2, canInterrupt = true)
    
    /**
     * Падение. Движение вниз после прыжка.
     */
    object Falling : AnimationState("falling", priority = 2, canInterrupt = true)
    
    /**
     * Получение урона. Кратковременное состояние.
     */
    object Hit : AnimationState("hit", priority = 3, canInterrupt = false)
    
    /**
     * Смерть. Конечное состояние.
     */
    object Dead : AnimationState("dead", priority = 4, canInterrupt = false)
    
    /**
     * Атака. Для врагов или игрока с оружием.
     */
    object Attacking : AnimationState("attacking", priority = 3, canInterrupt = false)
    
    /**
     * Блок. Защита от атаки.
     */
    object Blocking : AnimationState("blocking", priority = 3, canInterrupt = false)
    
    /**
     * Скольжение. Для специальных способностей.
     */
    object Sliding : AnimationState("sliding", priority = 2, canInterrupt = true)
    
    /**
     * Победа. Для экранов победы.
     */
    object Victory : AnimationState("victory", priority = 4, canInterrupt = false)
    
    /**
     * Кастомное состояние с заданным именем.
     */
    data class Custom(
        val customName: String,
        override val priority: Int = 0,
        override val canInterrupt: Boolean = true
    ) : AnimationState(customName, priority, canInterrupt)
    
    /**
     * Проверка возможности перехода в другое состояние.
     *
     * @param target Целевое состояние
     * @return true если переход возможен
     */
    fun canTransitionTo(target: AnimationState): Boolean {
        // Нельзя перейти из Dead никуда кроме respawn
        if (this is Dead) return target is Idle
        
        // Hit и Dead нельзя прервать (если они сами не разрешили)
        if (!this.canInterrupt && this.priority >= target.priority) {
            return false
        }
        
        // Целевое состояние может иметь свои ограничения
        if (!target.canInterrupt && target.priority > this.priority) {
            return false
        }
        
        return true
    }
    
    companion object {
        /** Все стандартные состояния */
        val values: List<AnimationState> = listOf(
            Idle,
            Running,
            Jumping,
            Falling,
            Hit,
            Dead,
            Attacking,
            Blocking,
            Sliding,
            Victory
        )
        
        /** Получение состояния по имени */
        fun fromName(name: String): AnimationState? {
            return values.find { it.name.equals(name, ignoreCase = true) }
        }
        
        /** Состояние по умолчанию */
        val DEFAULT: AnimationState = Idle
    }
}

/**
 * Менеджер переходов между состояниями анимации.
 * Отслеживает текущее и предыдущее состояние.
 */
class AnimationStateMachine {
    /** Текущее состояние */
    var currentState: AnimationState = AnimationState.Idle
        private set
    
    /** Предыдущее состояние */
    var previousState: AnimationState? = null
        private set
    
    /** Время в текущем состоянии */
    var stateTime: Float = 0f
        private set
    
    /** Количество переходов */
    var transitionCount: Int = 0
        private set
    
    /** Callback на смену состояния */
    var onStateChanged: ((AnimationState, AnimationState?) -> Unit)? = null
    
    /**
     * Попытка перехода в новое состояние.
     *
     * @param newState Целевое состояние
     * @param force Принудительный переход (игнорируя правила)
     * @return true если переход успешен
     */
    fun setState(newState: AnimationState, force: Boolean = false): Boolean {
        if (currentState == newState) return true
        
        // Проверка возможности перехода
        if (!force && !currentState.canTransitionTo(newState)) {
            return false
        }
        
        // Сохранение предыдущего состояния
        previousState = currentState
        
        // Переход
        currentState = newState
        stateTime = 0f
        transitionCount++
        
        // Уведомление
        onStateChanged?.invoke(newState, previousState)
        
        return true
    }
    
    /**
     * Обновление таймера состояния.
     *
     * @param deltaTime Время с последнего кадра
     */
    fun update(deltaTime: Float) {
        stateTime += deltaTime
    }
    
    /**
     * Сброс к состоянию по умолчанию.
     */
    fun reset() {
        previousState = null
        currentState = AnimationState.Idle
        stateTime = 0f
        transitionCount = 0
    }
    
    /**
     * Проверка, находится ли в заданном состоянии.
     */
    fun isState(state: AnimationState): Boolean = currentState == state
    
    /**
     * Проверка, находится ли в любом из заданных состояний.
     */
    fun isAnyState(vararg states: AnimationState): Boolean {
        return states.contains(currentState)
    }
    
    /**
     * Получение времени в текущем состоянии.
     */
    fun getStateTime(): Float = stateTime
    
    /**
     * Проверка, только что вошли в состояние.
     *
     * @param threshold Порог времени (секунды)
     */
    fun justEnteredState(threshold: Float = 0.1f): Boolean {
        return stateTime < threshold
    }
}

/**
 * Extension property для получения строкового имени состояния.
 */
val AnimationState.stateName: String
    get() = name

/**
 * Extension property для проверки, является ли состояние активным (не покой).
 */
val AnimationState.isActiveState: Boolean
    get() = this !is AnimationState.Idle && this !is AnimationState.Dead

/**
 * Extension property для проверки, является ли состояние воздушным.
 */
val AnimationState.isAirborneState: Boolean
    get() = this is AnimationState.Jumping || this is AnimationState.Falling
