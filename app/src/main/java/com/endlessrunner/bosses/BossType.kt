package com.endlessrunner.bosses

import android.graphics.Color
import com.endlessrunner.bosses.config.BossBalanceConfig

/**
 * Типы боссов в игре.
 * Каждый тип имеет уникальные характеристики и паттерны атак.
 *
 * @property id Уникальный идентификатор
 * @property baseHealth Базовое здоровье
 * @property baseDamage Базовый урон
 * @property moveSpeed Скорость движения
 * @property width Ширина хитбокса
 * @property height Высота хитбокса
 * @property debugColor Цвет для отладки
 * @property arenaWidth Ширина арены
 * @property phases Список фаз босса
 * @property difficulty Сложность (1-5)
 */
sealed class BossType(
    val id: String,
    val baseHealth: Int,
    val baseDamage: Int,
    val moveSpeed: Float,
    val width: Float,
    val height: Float,
    val debugColor: Int,
    val arenaWidth: Float,
    val phases: List<BossPhase>,
    val difficulty: Int
) {
    /**
     * Гигантский слизень - первый босс (легкий).
     * Механика: делится на мелких слизней, ядовитые облака.
     */
    object GIANT_SLIME : BossType(
        id = "giant_slime",
        baseHealth = 500,
        baseDamage = 10,
        moveSpeed = 150f,
        width = 200f,
        height = 180f,
        debugColor = Color.rgb(76, 175, 80), // Green
        arenaWidth = 2000f,
        phases = listOf(
            BossPhase.Phase1(
                healthThreshold = 1.0f,
                attackPatterns = listOf(PatternType.JUMP_ATTACK, PatternType.SLIME_SPLIT),
                moveSpeedMultiplier = 1.0f,
                damageMultiplier = 1.0f
            ),
            BossPhase.Phase2(
                healthThreshold = 0.7f,
                attackPatterns = listOf(PatternType.JUMP_ATTACK, PatternType.SLIME_SPLIT, PatternType.POISON_CLOUD),
                moveSpeedMultiplier = 1.2f,
                damageMultiplier = 1.1f
            ),
            BossPhase.Phase3(
                healthThreshold = 0.4f,
                attackPatterns = listOf(PatternType.JUMP_ATTACK, PatternType.SLIME_SPLIT, PatternType.POISON_CLOUD, PatternType.GROUND_POUND),
                moveSpeedMultiplier = 1.4f,
                damageMultiplier = 1.2f
            )
        ),
        difficulty = 1
    )

    /**
     * Механический дракон - второй босс (средний).
     * Механика: летает, периодически приземляется, ракетный залп.
     */
    object MECH_DRAGON : BossType(
        id = "mech_dragon",
        baseHealth = 800,
        baseDamage = 15,
        moveSpeed = 200f,
        width = 280f,
        height = 220f,
        debugColor = Color.rgb(255, 87, 34), // Deep Orange
        arenaWidth = 2500f,
        phases = listOf(
            BossPhase.Phase1(
                healthThreshold = 1.0f,
                attackPatterns = listOf(PatternType.FIRE_BREATH, PatternType.TAIL_SWIPE),
                moveSpeedMultiplier = 1.0f,
                damageMultiplier = 1.0f
            ),
            BossPhase.Phase2(
                healthThreshold = 0.7f,
                attackPatterns = listOf(PatternType.FIRE_BREATH, PatternType.TAIL_SWIPE, PatternType.MISSILE_STORM),
                moveSpeedMultiplier = 1.1f,
                damageMultiplier = 1.2f
            ),
            BossPhase.Phase3(
                healthThreshold = 0.4f,
                attackPatterns = listOf(PatternType.FIRE_BREATH, PatternType.TAIL_SWIPE, PatternType.MISSILE_STORM, PatternType.LASER_BEAM),
                moveSpeedMultiplier = 1.3f,
                damageMultiplier = 1.3f
            )
        ),
        difficulty = 2
    )

    /**
     * Тёмный рыцарь - третий босс (сложный).
     * Механика: щит (нужно ломать), телепортация, комбо атаки.
     */
    object DARK_KNIGHT : BossType(
        id = "dark_knight",
        baseHealth = 1000,
        baseDamage = 20,
        moveSpeed = 250f,
        width = 180f,
        height = 250f,
        debugColor = Color.rgb(156, 39, 176), // Purple
        arenaWidth = 2200f,
        phases = listOf(
            BossPhase.Phase1(
                healthThreshold = 1.0f,
                attackPatterns = listOf(PatternType.SWORD_COMBO, PatternType.SHIELD_BASH),
                moveSpeedMultiplier = 1.0f,
                damageMultiplier = 1.0f,
                hasShield = true
            ),
            BossPhase.Phase2(
                healthThreshold = 0.7f,
                attackPatterns = listOf(PatternType.SWORD_COMBO, PatternType.SHIELD_BASH, PatternType.DARK_ORBS),
                moveSpeedMultiplier = 1.2f,
                damageMultiplier = 1.2f,
                hasShield = true
            ),
            BossPhase.Phase3(
                healthThreshold = 0.4f,
                attackPatterns = listOf(PatternType.SWORD_COMBO, PatternType.TELEPORT_STRIKE, PatternType.DARK_ORBS),
                moveSpeedMultiplier = 1.5f,
                damageMultiplier = 1.4f,
                hasShield = false
            )
        ),
        difficulty = 3
    )

    /**
     * Страж пустоты - четвертый босс (очень сложный).
     * Механика: порталы, телепортация, гравитация, чёрные дыры.
     */
    object VOID_GUARDIAN : BossType(
        id = "void_guardian",
        baseHealth = 1200,
        baseDamage = 25,
        moveSpeed = 180f,
        width = 220f,
        height = 280f,
        debugColor = Color.rgb(103, 58, 183), // Deep Purple
        arenaWidth = 2800f,
        phases = listOf(
            BossPhase.Phase1(
                healthThreshold = 1.0f,
                attackPatterns = listOf(PatternType.VOID_BEAM, PatternType.TELEPORT_STRIKE),
                moveSpeedMultiplier = 1.0f,
                damageMultiplier = 1.0f
            ),
            BossPhase.Phase2(
                healthThreshold = 0.7f,
                attackPatterns = listOf(PatternType.VOID_BEAM, PatternType.TELEPORT_STRIKE, PatternType.BLACK_HOLE),
                moveSpeedMultiplier = 1.2f,
                damageMultiplier = 1.3f
            ),
            BossPhase.Phase3(
                healthThreshold = 0.4f,
                attackPatterns = listOf(PatternType.VOID_BEAM, PatternType.TELEPORT_STRIKE, PatternType.BLACK_HOLE, PatternType.METEOR_STRIKE),
                moveSpeedMultiplier = 1.4f,
                damageMultiplier = 1.5f
            )
        ),
        difficulty = 4
    )

    /**
     * Финальный босс (хардкор).
     * Механика: меняет форму, комбинирует все атаки, 4 фазы.
     */
    object FINAL_BOSS : BossType(
        id = "final_boss",
        baseHealth = 2000,
        baseDamage = 30,
        moveSpeed = 220f,
        width = 250f,
        height = 300f,
        debugColor = Color.rgb(216, 27, 96), // Pink
        arenaWidth = 3500f,
        phases = listOf(
            BossPhase.Phase1(
                healthThreshold = 1.0f,
                attackPatterns = listOf(PatternType.SWORD_COMBO, PatternType.FIRE_BREATH, PatternType.JUMP_ATTACK),
                moveSpeedMultiplier = 1.0f,
                damageMultiplier = 1.0f
            ),
            BossPhase.Phase2(
                healthThreshold = 0.75f,
                attackPatterns = listOf(PatternType.VOID_BEAM, PatternType.BLACK_HOLE, PatternType.MISSILE_STORM),
                moveSpeedMultiplier = 1.2f,
                damageMultiplier = 1.3f
            ),
            BossPhase.Phase3(
                healthThreshold = 0.5f,
                attackPatterns = listOf(PatternType.LASER_BEAM, PatternType.METEOR_STRIKE, PatternType.TIME_SLOW),
                moveSpeedMultiplier = 1.4f,
                damageMultiplier = 1.5f
            ),
            BossPhase.Phase4(
                healthThreshold = 0.25f,
                attackPatterns = listOf(PatternType.ALL_ATTACKS),
                moveSpeedMultiplier = 1.6f,
                damageMultiplier = 1.7f
            )
        ),
        difficulty = 5
    )

    companion object {
        /** Все типы боссов */
        val ALL_TYPES: List<BossType> = listOf(
            GIANT_SLIME,
            MECH_DRAGON,
            DARK_KNIGHT,
            VOID_GUARDIAN,
            FINAL_BOSS
        )

        /** Получение типа по ID */
        fun fromId(id: String): BossType? = ALL_TYPES.find { it.id == id }

        /** Получение босса по индексу сложности */
        fun getByDifficulty(difficulty: Int): BossType? = ALL_TYPES.find { it.difficulty == difficulty }

        /** Расчёт конфигурации баланса для босса */
        fun getBalanceConfig(type: BossType): BossBalanceConfig {
            return BossBalanceConfig.forBoss(type)
        }
    }

    /**
     * Получение текущей фазы по проценту здоровья.
     */
    fun getPhaseForHealth(healthPercent: Float): BossPhase {
        return phases.find { healthPercent <= it.healthThreshold } ?: phases.last()
    }

    /**
     * Проверка, является ли этот босс финальным.
     */
    fun isFinalBoss(): Boolean = this == FINAL_BOSS
}
