package com.endlessrunner.bosses

/**
 * Enum всех типов паттернов атак.
 * Используется для конфигурации и фабрики паттернов.
 */
enum class PatternType(
    val id: String,
    val category: AttackCategory,
    val difficulty: Int,
    val baseDamage: Int,
    val baseDuration: Float,
    val baseCooldown: Float
) {
    // ============================================================================
    // PROJECTILE ATTACKS (Снаряды)
    // ============================================================================

    /** Множество снарядов по спирали */
    BULLET_HELL(
        id = "bullet_hell",
        category = AttackCategory.PROJECTILE,
        difficulty = 3,
        baseDamage = 5,
        baseDuration = 5f,
        baseCooldown = 8f
    ),

    /** Снаряд в игрока */
    AIMED_SHOT(
        id = "aimed_shot",
        category = AttackCategory.PROJECTILE,
        difficulty = 1,
        baseDamage = 10,
        baseDuration = 1f,
        baseCooldown = 2f
    ),

    /** Веер снарядов */
    SPREAD_SHOT(
        id = "spread_shot",
        category = AttackCategory.PROJECTILE,
        difficulty = 2,
        baseDamage = 8,
        baseDuration = 2f,
        baseCooldown = 4f
    ),

    /** Снаряды сверху */
    RAIN_PROJECTILES(
        id = "rain_projectiles",
        category = AttackCategory.PROJECTILE,
        difficulty = 2,
        baseDamage = 7,
        baseDuration = 4f,
        baseCooldown = 6f
    ),

    /** Снаряды по орбите */
    ORBIT_PROJECTILES(
        id = "orbit_projectiles",
        category = AttackCategory.PROJECTILE,
        difficulty = 2,
        baseDamage = 6,
        baseDuration = 6f,
        baseCooldown = 8f
    ),

    /** Метеоры с неба */
    METEOR_STRIKE(
        id = "meteor_strike",
        category = AttackCategory.PROJECTILE,
        difficulty = 4,
        baseDamage = 15,
        baseDuration = 5f,
        baseCooldown = 10f
    ),

    /** Ракетный залп */
    MISSILE_STORM(
        id = "missile_storm",
        category = AttackCategory.PROJECTILE,
        difficulty = 3,
        baseDamage = 12,
        baseDuration = 4f,
        baseCooldown = 7f
    ),

    // ============================================================================
    // MELEE ATTACKS (Ближний бой)
    // ============================================================================

    /** Рывок на игрока */
    CHARGE(
        id = "charge",
        category = AttackCategory.MELEE,
        difficulty = 1,
        baseDamage = 15,
        baseDuration = 2f,
        baseCooldown = 4f
    ),

    /** Удар по земле с волной */
    SLAM(
        id = "slam",
        category = AttackCategory.MELEE,
        difficulty = 2,
        baseDamage = 20,
        baseDuration = 2f,
        baseCooldown = 5f
    ),

    /** Удар лапой/мечом */
    SWIPE(
        id = "swipe",
        category = AttackCategory.MELEE,
        difficulty = 1,
        baseDamage = 12,
        baseDuration = 1f,
        baseCooldown = 3f
    ),

    /** Вращение с хвостом */
    TAIL_SPIN(
        id = "tail_spin",
        category = AttackCategory.MELEE,
        difficulty = 2,
        baseDamage = 18,
        baseDuration = 3f,
        baseCooldown = 5f
    ),

    /** Удар хвостом */
    TAIL_SWIPE(
        id = "tail_swipe",
        category = AttackCategory.MELEE,
        difficulty = 2,
        baseDamage = 16,
        baseDuration = 2f,
        baseCooldown = 4f
    ),

    /** Комбо мечом */
    SWORD_COMBO(
        id = "sword_combo",
        category = AttackCategory.MELEE,
        difficulty = 3,
        baseDamage = 25,
        baseDuration = 3f,
        baseCooldown = 5f
    ),

    /** Удар щитом */
    SHIELD_BASH(
        id = "shield_bash",
        category = AttackCategory.MELEE,
        difficulty = 2,
        baseDamage = 20,
        baseDuration = 2f,
        baseCooldown = 6f
    ),

    /** Удар по земле */
    GROUND_POUND(
        id = "ground_pound",
        category = AttackCategory.MELEE,
        difficulty = 2,
        baseDamage = 18,
        baseDuration = 2f,
        baseCooldown = 5f
    ),

    /** Прыжок с атакой */
    JUMP_ATTACK(
        id = "jump_attack",
        category = AttackCategory.MELEE,
        difficulty = 1,
        baseDamage = 15,
        baseDuration = 3f,
        baseCooldown = 4f
    ),

    // ============================================================================
    // SPECIAL ATTACKS (Специальные)
    // ============================================================================

    /** Призыв миньонов */
    SUMMON_MINIONS(
        id = "summon_minions",
        category = AttackCategory.SPECIAL,
        difficulty = 2,
        baseDamage = 0,
        baseDuration = 3f,
        baseCooldown = 15f
    ),

    /** Луч через всю арену */
    LASER_BEAM(
        id = "laser_beam",
        category = AttackCategory.SPECIAL,
        difficulty = 4,
        baseDamage = 30,
        baseDuration = 4f,
        baseCooldown = 8f
    ),

    /** Телепортация + удар */
    TELEPORT_STRIKE(
        id = "teleport_strike",
        category = AttackCategory.SPECIAL,
        difficulty = 3,
        baseDamage = 20,
        baseDuration = 3f,
        baseCooldown = 6f
    ),

    /** Волна вокруг босса */
    SHOCKWAVE(
        id = "shockwave",
        category = AttackCategory.SPECIAL,
        difficulty = 2,
        baseDamage = 15,
        baseDuration = 2f,
        baseCooldown = 5f
    ),

    /** Притягивание игрока */
    BLACK_HOLE(
        id = "black_hole",
        category = AttackCategory.SPECIAL,
        difficulty = 4,
        baseDamage = 10,
        baseDuration = 5f,
        baseCooldown = 10f
    ),

    /** Замедление времени */
    TIME_SLOW(
        id = "time_slow",
        category = AttackCategory.SPECIAL,
        difficulty = 5,
        baseDamage = 0,
        baseDuration = 5f,
        baseCooldown = 20f
    ),

    /** Огненное дыхание */
    FIRE_BREATH(
        id = "fire_breath",
        category = AttackCategory.SPECIAL,
        difficulty = 3,
        baseDamage = 25,
        baseDuration = 4f,
        baseCooldown = 7f
    ),

    /** Луч пустоты */
    VOID_BEAM(
        id = "void_beam",
        category = AttackCategory.SPECIAL,
        difficulty = 4,
        baseDamage = 28,
        baseDuration = 5f,
        baseCooldown = 8f
    ),

    /** Тёмные сферы */
    DARK_ORBS(
        id = "dark_orbs",
        category = AttackCategory.SPECIAL,
        difficulty = 3,
        baseDamage = 18,
        baseDuration = 4f,
        baseCooldown = 6f
    ),

    /** Ядовитое облако */
    POISON_CLOUD(
        id = "poison_cloud",
        category = AttackCategory.SPECIAL,
        difficulty = 2,
        baseDamage = 8,
        baseDuration = 6f,
        baseCooldown = 8f
    ),

    /** Разделение слизня */
    SLIME_SPLIT(
        id = "slime_split",
        category = AttackCategory.SPECIAL,
        difficulty = 2,
        baseDamage = 5,
        baseDuration = 2f,
        baseCooldown = 10f
    ),

    /** Все атаки одновременно (финальный босс) */
    ALL_ATTACKS(
        id = "all_attacks",
        category = AttackCategory.SPECIAL,
        difficulty = 5,
        baseDamage = 0,
        baseDuration = 10f,
        baseCooldown = 30f
    );

    companion object {
        /** Получение паттерна по ID */
        fun fromId(id: String): PatternType? = entries.find { it.id == id }

        /** Все паттерны категории */
        fun getByCategory(category: AttackCategory): List<PatternType> =
            entries.filter { it.category == category }

        /** Паттерны по сложности */
        fun getByDifficulty(maxDifficulty: Int): List<PatternType> =
            entries.filter { it.difficulty <= maxDifficulty }
    }
}

/**
 * Категория атаки.
 */
enum class AttackCategory {
    /** Снаряды и проектайлы */
    PROJECTILE,

    /** Ближний бой */
    MELEE,

    /** Специальные атаки */
    SPECIAL
}

/**
 * Extension property для проверки, является ли атака снарядом.
 */
val PatternType.isProjectile: Boolean
    get() = category == AttackCategory.PROJECTILE

/**
 * Extension property для проверки, является ли атака ближним боем.
 */
val PatternType.isMelee: Boolean
    get() = category == AttackCategory.MELEE

/**
 * Extension property для проверки, является ли атака специальной.
 */
val PatternType.isSpecial: Boolean
    get() = category == AttackCategory.SPECIAL
