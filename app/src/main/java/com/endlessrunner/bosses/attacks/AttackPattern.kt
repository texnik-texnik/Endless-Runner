package com.endlessrunner.bosses.attacks

import android.graphics.Canvas
import com.endlessrunner.bosses.Boss
import com.endlessrunner.bosses.PatternType

/**
 * Абстрактный класс паттерна атаки босса.
 * Strategy Pattern для гибкой смены атак.
 *
 * @property type Тип паттерна
 * @property damage Урон атаки
 * @property duration Длительность атаки
 * @property cooldown Время перезарядки
 * @property difficulty Сложность (1-5)
 */
abstract class AttackPattern(
    val type: PatternType,
    var damage: Int,
    var duration: Float,
    var cooldown: Float,
    val difficulty: Int
) {
    /** Ссылка на босса */
    protected var boss: Boss? = null

    /** Время, прошедшее с начала атаки */
    protected var timeElapsed: Float = 0f

    /** Состояние атаки */
    protected var state: AttackState = AttackState.IDLE

    /** Таймер кулдауна */
    protected var cooldownTimer: Float = 0f

    /** Флаг завершения атаки */
    protected var isFinished: Boolean = false

    /** Callback на завершение атаки */
    var onComplete: (() -> Unit)? = null

    /**
     * Инициализация паттерна с боссом.
     */
    open fun initialize(boss: Boss) {
        this.boss = boss
    }

    /**
     * Вызывается при начале атаки.
     */
    open fun onStart() {
        state = AttackState.ACTIVE
        timeElapsed = 0f
        isFinished = false
        onAttackStart()
    }

    /**
     * Вызывается при начале атаки (для переопределения).
     */
    protected abstract fun onAttackStart()

    /**
     * Обновление паттерна.
     * @param deltaTime Время с последнего кадра
     * @return true если атака активна
     */
    open fun update(deltaTime: Float): Boolean {
        if (state != AttackState.ACTIVE) return false

        timeElapsed += deltaTime

        // Проверка завершения по времени
        if (timeElapsed >= duration) {
            finish()
            return false
        }

        // Обновление логики атаки
        onAttackUpdate(deltaTime)

        return true
    }

    /**
     * Обновление логики атаки (для переопределения).
     */
    protected abstract fun onAttackUpdate(deltaTime: Float)

    /**
     * Вызывается при завершении атаки.
     */
    open fun onEnd() {
        state = AttackState.COOLDOWN
        cooldownTimer = cooldown
        onAttackEnd()
    }

    /**
     * Вызывается при завершении атаки (для переопределения).
     */
    protected abstract fun onAttackEnd()

    /**
     * Обновление кулдауна.
     */
    open fun updateCooldown(deltaTime: Float) {
        if (state == AttackState.COOLDOWN) {
            cooldownTimer -= deltaTime
            if (cooldownTimer <= 0) {
                state = AttackState.IDLE
            }
        }
    }

    /**
     * Завершение атаки.
     */
    protected fun finish() {
        isFinished = true
        onEnd()
        onComplete?.invoke()
    }

    /**
     * Проверка, можно ли прервать атаку.
     */
    open fun canInterrupt(): Boolean = false

    /**
     * Принудительное прерывание атаки.
     */
    open fun interrupt() {
        if (state == AttackState.ACTIVE) {
            state = AttackState.INTERRUPTED
            onAttackEnd()
        }
    }

    /**
     * Сброс паттерна.
     */
    open fun reset() {
        timeElapsed = 0f
        isFinished = false
        cooldownTimer = 0f
        state = AttackState.IDLE
    }

    /**
     * Отрисовка паттерна (для визуализации).
     */
    open fun render(canvas: Canvas) {
        // Переопределяется в наследниках для отладки
    }

    /**
     * Проверка, готова ли атака к использованию.
     */
    fun isReady(): Boolean = state == AttackState.IDLE

    /**
     * Проверка, активна ли атака.
     */
    fun isActive(): Boolean = state == AttackState.ACTIVE

    /**
     * Проверка, на кулдауне ли атака.
     */
    fun isOnCooldown(): Boolean = state == AttackState.COOLDOWN

    /**
     * Получение прогресса кулдауна (0-1).
     */
    fun getCooldownProgress(): Float {
        return if (state == AttackState.COOLDOWN) {
            1f - (cooldownTimer / cooldown)
        } else 0f
    }

    /**
     * Получение прогресса атаки (0-1).
     */
    fun getAttackProgress(): Float {
        return if (state == AttackState.ACTIVE) {
            timeElapsed / duration
        } else 0f
    }

    /**
     * Получение босса.
     */
    fun getBoss(): Boss? = boss

    /**
     * Состояние атаки.
     */
    enum class AttackState {
        /** Готова к использованию */
        IDLE,

        /** Активно выполняется */
        ACTIVE,

        /** На перезарядке */
        COOLDOWN,

        /** Прервана */
        INTERRUPTED
    }
}

/**
 * Data class для конфигурации паттерна.
 */
data class AttackPatternConfig(
    val damageMultiplier: Float = 1f,
    val durationMultiplier: Float = 1f,
    val cooldownMultiplier: Float = 1f,
    val enabled: Boolean = true
) {
    companion object {
        val DEFAULT = AttackPatternConfig()
    }
}
