package com.endlessrunner.bosses

/**
 * Sealed class для фаз босса.
 * Каждая фаза имеет свои паттерны атак и модификаторы.
 *
 * @property healthThreshold Порог здоровья для перехода (0.0-1.0)
 * @property attackPatterns Список доступных паттернов атак
 * @property moveSpeedMultiplier Множитель скорости движения
 * @property damageMultiplier Множитель урона
 * @property hasShield Есть ли щит у босса
 */
sealed class BossPhase(
    open val healthThreshold: Float,
    open val attackPatterns: List<PatternType>,
    open val moveSpeedMultiplier: Float = 1.0f,
    open val damageMultiplier: Float = 1.0f,
    open val hasShield: Boolean = false,
    open val shieldHealth: Int = 0
) {
    /**
     * Фаза 1: 100% - 70% HP
     * Базовые атаки, стандартная скорость.
     */
    data class Phase1(
        override val healthThreshold: Float = 1.0f,
        override val attackPatterns: List<PatternType>,
        override val moveSpeedMultiplier: Float = 1.0f,
        override val damageMultiplier: Float = 1.0f,
        override val hasShield: Boolean = false,
        override val shieldHealth: Int = 0
    ) : BossPhase(healthThreshold, attackPatterns, moveSpeedMultiplier, damageMultiplier, hasShield, shieldHealth)

    /**
     * Фаза 2: 70% - 40% HP
     * Добавляются новые атаки, увеличивается скорость и урон.
     */
    data class Phase2(
        override val healthThreshold: Float = 0.7f,
        override val attackPatterns: List<PatternType>,
        override val moveSpeedMultiplier: Float = 1.2f,
        override val damageMultiplier: Float = 1.2f,
        override val hasShield: Boolean = false,
        override val shieldHealth: Int = 0
    ) : BossPhase(healthThreshold, attackPatterns, moveSpeedMultiplier, damageMultiplier, hasShield, shieldHealth)

    /**
     * Фаза 3: 40% - 0% HP
     * Все атаки доступны, максимальная агрессия.
     */
    data class Phase3(
        override val healthThreshold: Float = 0.4f,
        override val attackPatterns: List<PatternType>,
        override val moveSpeedMultiplier: Float = 1.4f,
        override val damageMultiplier: Float = 1.4f,
        override val hasShield: Boolean = false,
        override val shieldHealth: Int = 0
    ) : BossPhase(healthThreshold, attackPatterns, moveSpeedMultiplier, damageMultiplier, hasShield, shieldHealth)

    /**
     * Фаза 4: только для финального босса (25% - 0% HP)
     * Истинная форма, все атаки одновременно.
     */
    data class Phase4(
        override val healthThreshold: Float = 0.25f,
        override val attackPatterns: List<PatternType>,
        override val moveSpeedMultiplier: Float = 1.6f,
        override val damageMultiplier: Float = 1.7f,
        override val hasShield: Boolean = false,
        override val shieldHealth: Int = 0
    ) : BossPhase(healthThreshold, attackPatterns, moveSpeedMultiplier, damageMultiplier, hasShield, shieldHealth)

    /**
     * Режим ярости (Enraged): активируется по таймеру или при < 10% HP
     * Максимальная скорость и урон, игнорирует некоторые кулдауны.
     */
    data class Enraged(
        val parentPhase: BossPhase,
        val enragedDuration: Float = 30f, // секунд до конца ярости
        override val healthThreshold: Float = 0.1f,
        override val attackPatterns: List<PatternType> = parentPhase.attackPatterns,
        override val moveSpeedMultiplier: Float = 2.0f,
        override val damageMultiplier: Float = 2.0f,
        override val hasShield: Boolean = false,
        override val shieldHealth: Int = 0
    ) : BossPhase(healthThreshold, attackPatterns, moveSpeedMultiplier, damageMultiplier, hasShield, shieldHealth) {

        /** Время, прошедшее с начала ярости */
        var timeInEnrage: Float = 0f
            private set

        /** Обновление таймера ярости */
        fun updateEnrageTimer(deltaTime: Float) {
            timeInEnrage += deltaTime
        }

        /** Проверка, активна ли ещё ярость */
        fun isEnrageActive(): Boolean = timeInEnrage < enragedDuration

        /** Процент оставшегося времени ярости */
        fun getEnrageProgress(): Float = timeInEnrage / enragedDuration
    }

    companion object {
        /** Индекс фазы для UI */
        fun getPhaseIndex(phase: BossPhase): Int {
            return when (phase) {
                is Phase1 -> 1
                is Phase2 -> 2
                is Phase3 -> 3
                is Phase4 -> 4
                is Enraged -> getPhaseIndex(phase.parentPhase)
            }
        }

        /** Проверка, является ли фаза яростью */
        fun isEnraged(phase: BossPhase): Boolean = phase is Enraged

        /** Получение базовой фазы (без учёта ярости) */
        fun getBasePhase(phase: BossPhase): BossPhase {
            return if (phase is Enraged) phase.parentPhase else phase
        }
    }
}

/**
 * Extension property для получения названия фазы.
 */
val BossPhase.phaseName: String
    get() = when (this) {
        is BossPhase.Phase1 -> "Phase 1"
        is BossPhase.Phase2 -> "Phase 2"
        is BossPhase.Phase3 -> "Phase 3"
        is BossPhase.Phase4 -> "Phase 4"
        is BossPhase.Enraged -> "ENRAGED!"
    }

/**
 * Extension property для проверки, последняя ли это фаза.
 */
val BossPhase.isFinalPhase: Boolean
    get() = this is BossPhase.Phase3 || this is BossPhase.Phase4
