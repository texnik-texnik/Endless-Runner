package com.endlessrunner.bosses.config

import com.endlessrunner.bosses.BossType
import com.endlessrunner.bosses.PatternType

/**
 * Конфигурация баланса для боссов.
 * Определяет параметры для каждого босса.
 */
data class BossBalanceConfig(
    val healthMultiplier: Float = 1f,
    val damageMultiplier: Float = 1f,
    val speedMultiplier: Float = 1f,
    val attackCooldownMultiplier: Float = 1f,
    val phaseThresholds: List<Float> = listOf(1.0f, 0.7f, 0.4f),
    val patternWeights: Map<PatternType, Float> = emptyMap(),
    val minionSpawnRate: Float = 1f,
    val enrageTime: Float = 300f // секунд до ярости
) {
    companion object {
        /**
         * Конфигурация по умолчанию.
         */
        val DEFAULT = BossBalanceConfig()

        /**
         * Получение конфигурации для конкретного босса.
         */
        fun forBoss(bossType: BossType): BossBalanceConfig {
            return when (bossType) {
                BossType.GIANT_SLIME -> slimeConfig()
                BossType.MECH_DRAGON -> dragonConfig()
                BossType.DARK_KNIGHT -> knightConfig()
                BossType.VOID_GUARDIAN -> voidConfig()
                BossType.FINAL_BOSS -> finalBossConfig()
            }
        }

        /**
         * Конфигурация для слизня.
         */
        private fun slimeConfig() = BossBalanceConfig(
            healthMultiplier = 1f,
            damageMultiplier = 1f,
            speedMultiplier = 1f,
            phaseThresholds = listOf(1.0f, 0.7f, 0.4f),
            patternWeights = mapOf(
                PatternType.JUMP_ATTACK to 0.4f,
                PatternType.SLIME_SPLIT to 0.3f,
                PatternType.POISON_CLOUD to 0.3f
            ),
            minionSpawnRate = 0.5f,
            enrageTime = 600f
        )

        /**
         * Конфигурация для дракона.
         */
        private fun dragonConfig() = BossBalanceConfig(
            healthMultiplier = 1.2f,
            damageMultiplier = 1.3f,
            speedMultiplier = 1.1f,
            phaseThresholds = listOf(1.0f, 0.7f, 0.4f),
            patternWeights = mapOf(
                PatternType.FIRE_BREATH to 0.3f,
                PatternType.TAIL_SWIPE to 0.3f,
                PatternType.MISSILE_STORM to 0.25f,
                PatternType.LASER_BEAM to 0.15f
            ),
            minionSpawnRate = 0.7f,
            enrageTime = 480f
        )

        /**
         * Конфигурация для рыцаря.
         */
        private fun knightConfig() = BossBalanceConfig(
            healthMultiplier = 1.4f,
            damageMultiplier = 1.5f,
            speedMultiplier = 1.2f,
            phaseThresholds = listOf(1.0f, 0.7f, 0.4f),
            patternWeights = mapOf(
                PatternType.SWORD_COMBO to 0.35f,
                PatternType.SHIELD_BASH to 0.25f,
                PatternType.DARK_ORBS to 0.25f,
                PatternType.TELEPORT_STRIKE to 0.15f
            ),
            minionSpawnRate = 0.6f,
            enrageTime = 420f
        )

        /**
         * Конфигурация для стража пустоты.
         */
        private fun voidConfig() = BossBalanceConfig(
            healthMultiplier = 1.6f,
            damageMultiplier = 1.7f,
            speedMultiplier = 1.0f,
            phaseThresholds = listOf(1.0f, 0.7f, 0.4f),
            patternWeights = mapOf(
                PatternType.VOID_BEAM to 0.3f,
                PatternType.TELEPORT_STRIKE to 0.25f,
                PatternType.BLACK_HOLE to 0.25f,
                PatternType.METEOR_STRIKE to 0.2f
            ),
            minionSpawnRate = 0.8f,
            enrageTime = 360f
        )

        /**
         * Конфигурация для финального босса.
         */
        private fun finalBossConfig() = BossBalanceConfig(
            healthMultiplier = 2.5f,
            damageMultiplier = 2f,
            speedMultiplier = 1.3f,
            phaseThresholds = listOf(1.0f, 0.75f, 0.5f, 0.25f),
            patternWeights = mapOf(
                PatternType.ALL_ATTACKS to 0.1f,
                PatternType.LASER_BEAM to 0.15f,
                PatternType.METEOR_STRIKE to 0.15f,
                PatternType.BLACK_HOLE to 0.15f,
                PatternType.TIME_SLOW to 0.1f,
                PatternType.FIRE_BREATH to 0.15f,
                PatternType.SWORD_COMBO to 0.2f
            ),
            minionSpawnRate = 1f,
            enrageTime = 300f
        )
    }
}

/**
 * Scaling сложности для боссов.
 * Увеличивает параметры в зависимости от прогресса игрока.
 */
data class DifficultyScaling(
    val healthMultiplier: Float = 1f,
    val damageMultiplier: Float = 1f,
    val speedMultiplier: Float = 1f,
    val attackFrequencyMultiplier: Float = 1f
) {
    companion object {
        /**
         * Расчёт scaling на основе уровня игрока.
         */
        fun forPlayerLevel(playerLevel: Int): DifficultyScaling {
            // Увеличение на 5% за каждый уровень после 10
            val levelFactor = if (playerLevel > 10) {
                1f + (playerLevel - 10) * 0.05f
            } else {
                1f
            }

            return DifficultyScaling(
                healthMultiplier = levelFactor,
                damageMultiplier = levelFactor,
                speedMultiplier = 1f + (levelFactor - 1f) * 0.5f,
                attackFrequencyMultiplier = 1f + (levelFactor - 1f) * 0.3f
            )
        }

        /**
         * Расчёт scaling на основе количества побед.
         */
        fun forVictories(victories: Int): DifficultyScaling {
            val victoryFactor = 1f + victories * 0.1f

            return DifficultyScaling(
                healthMultiplier = victoryFactor,
                damageMultiplier = victoryFactor,
                speedMultiplier = 1f + victories * 0.02f,
                attackFrequencyMultiplier = 1f + victories * 0.03f
            )
        }

        /**
         * Комбинированный scaling.
         */
        fun combined(playerLevel: Int, victories: Int): DifficultyScaling {
            val levelScaling = forPlayerLevel(playerLevel)
            val victoryScaling = forVictories(victories)

            return DifficultyScaling(
                healthMultiplier = levelScaling.healthMultiplier * victoryScaling.healthMultiplier,
                damageMultiplier = levelScaling.damageMultiplier * victoryScaling.damageMultiplier,
                speedMultiplier = levelScaling.speedMultiplier * victoryScaling.speedMultiplier,
                attackFrequencyMultiplier = levelScaling.attackFrequencyMultiplier * victoryScaling.attackFrequencyMultiplier
            )
        }
    }
}

/**
 * Настройки для конкретной фазы босса.
 */
data class PhaseConfig(
    val healthThreshold: Float,
    val damageMultiplier: Float = 1f,
    val speedMultiplier: Float = 1f,
    val attackCooldownMultiplier: Float = 1f,
    val enabledPatterns: List<PatternType> = emptyList(),
    val minionSpawnChance: Float = 0f,
    val specialBehavior: SpecialBehavior? = null
)

/**
 * Специальное поведение для фазы.
 */
enum class SpecialBehavior {
    /** Увеличенная агрессия */
    AGGRESSIVE,

    /** Защитная позиция */
    DEFENSIVE,

    /** Призыв подкреплений */
    CALL_FOR_HELP,

    /** Случайные телепортации */
    RANDOM_TELEPORT,

    /** Неуязвимость до условия */
    INVULNERABLE_UNTIL,

    /** Отражение снарядов */
    REFLECT_PROJECTILES,

    /** Регенерация здоровья */
    REGENERATE_HEALTH,

    /** Разделение на части */
    SPLIT
}

/**
 * Глобальные настройки системы боссов.
 */
object BossGlobalConfig {

    /** Максимальное количество активных боссов */
    const val MAX_ACTIVE_BOSSES = 1

    /** Максимальное количество миньонов одновременно */
    const val MAX_MINIONS_PER_BOSS = 15

    /** Время до авто-ярости (если бой затянулся) */
    const val AUTO_ENRAGE_TIME = 600f // 10 минут

    /** Шанс спавна босса при достижении условия */
    const val BOSS_SPAWN_CHANCE = 1.0f // 100%

    /** Минимальный уровень игрока для спавна босса */
    const val MIN_PLAYER_LEVEL_FOR_BOSS = 5

    /** Дистанция агробосса */
    const val BOSS_AGGRO_DISTANCE = 1000f

    /** Дистанция де-агро */
    const val BOSS_DEAGGRO_DISTANCE = 2000f

    /** Время респавна босса после смерти (в секундах) */
    const val BOSS_RESPAWN_TIME = 300f // 5 минут

    /** Награда за победу (множитель) */
    const val VICTORY_REWARD_MULTIPLIER = 5f

    /** Опыт за победу (базовый) */
    const val BASE_XP_REWARD = 1000

    /** Золото за победу (базовое) */
    const val BASE_GOLD_REWARD = 500
}
