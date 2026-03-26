package com.endlessrunner.powerups

/**
 * Типы бонусов (Power-Ups).
 *
 * Определяет различные типы усиливающих предметов в игре.
 */
enum class PowerUpType(
    val id: String,
    val displayName: String,
    val description: String,
    val durationSeconds: Float,
    val rarity: PowerUpRarity,
    val scoreValue: Int
) {
    /**
     * Щит - защищает от одного удара.
     */
    SHIELD(
        id = "shield",
        displayName = "Щит",
        description = "Защищает от одного удара",
        durationSeconds = 0f, // Мгновенный эффект
        rarity = PowerUpRarity.COMMON,
        scoreValue = 100
    ),

    /**
     * Магнит - притягивает монеты.
     */
    MAGNET(
        id = "magnet",
        displayName = "Магнит",
        description = "Притягивает nearby монеты",
        durationSeconds = 10f,
        rarity = PowerUpRarity.COMMON,
        scoreValue = 150
    ),

    /**
     * Ускорение - увеличивает скорость бега.
     */
    SPEED(
        id = "speed",
        displayName = "Ускорение",
        description = "Увеличивает скорость движения",
        durationSeconds = 8f,
        rarity = PowerUpRarity.UNCOMMON,
        scoreValue = 200
    ),

    /**
     * Непобедимость - полная защита от урона.
     */
    INVINCIBILITY(
        id = "invincibility",
        displayName = "Непобедимость",
        description = "Полная защита от урона",
        durationSeconds = 5f,
        rarity = PowerUpRarity.RARE,
        scoreValue = 500
    ),

    /**
     * Двойные очки - удваивает получаемые очки.
     */
    DOUBLE_SCORES(
        id = "double_scores",
        displayName = "Двойные очки",
        description = "Удваивает все получаемые очки",
        durationSeconds = 15f,
        rarity = PowerUpRarity.UNCOMMON,
        scoreValue = 250
    ),

    /**
     * Замедление времени - замедляет игру.
     */
    SLOW_TIME(
        id = "slow_time",
        displayName = "Замедление",
        description = "Замедляет течение времени",
        durationSeconds = 6f,
        rarity = PowerUpRarity.RARE,
        scoreValue = 400
    ),

    /**
     * Телепорт - мгновенно перемещает вперёд.
     */
    TELEPORT(
        id = "teleport",
        displayName = "Телепорт",
        description = "Мгновенное перемещение вперёд",
        durationSeconds = 0f, // Мгновенный эффект
        rarity = PowerUpRarity.EPIC,
        scoreValue = 600
    ),

    /**
     * Бомба - уничтожает всех nearby врагов.
     */
    BOMB(
        id = "bomb",
        displayName = "Бомба",
        description = "Уничтожает всех nearby врагов",
        durationSeconds = 0f, // Мгновенный эффект
        rarity = PowerUpRarity.EPIC,
        scoreValue = 700
    ),

    /**
     * Супер прыжок - увеличивает высоту прыжка.
     */
    SUPER_JUMP(
        id = "super_jump",
        displayName = "Супер прыжок",
        description = "Увеличивает высоту прыжка",
        durationSeconds = 12f,
        rarity = PowerUpRarity.UNCOMMON,
        scoreValue = 180
    ),

    /**
     * Монетный дождь - спавнит монеты на пути.
     */
    COIN_RAIN(
        id = "coin_rain",
        displayName = "Монетный дождь",
        description = "Спавнит монеты на пути",
        durationSeconds = 10f,
        rarity = PowerUpRarity.RARE,
        scoreValue = 350
    );

    companion object {
        /**
         * Все типы бонусов.
         */
        val ALL: List<PowerUpType> = entries.toList()

        /**
         * Общие бонусы.
         */
        val COMMON: List<PowerUpType> = ALL.filter { it.rarity == PowerUpRarity.COMMON }

        /**
         * Необычные бонусы.
         */
        val UNCOMMON: List<PowerUpType> = ALL.filter { it.rarity == PowerUpRarity.UNCOMMON }

        /**
         * Редкие бонусы.
         */
        val RARE: List<PowerUpType> = ALL.filter { it.rarity == PowerUpRarity.RARE }

        /**
         * Эпические бонусы.
         */
        val EPIC: List<PowerUpType> = ALL.filter { it.rarity == PowerUpRarity.EPIC }

        /**
         * Получить тип по ID.
         */
        fun fromId(id: String): PowerUpType? = ALL.find { it.id == id }

        /**
         * Получить случайный бонус указанной редкости.
         */
        fun getRandom(rarity: PowerUpRarity): PowerUpType {
            val filtered = ALL.filter { it.rarity == rarity }
            return if (filtered.isNotEmpty()) filtered.random() else ALL.random()
        }

        /**
         * Получить случайный бонус с учётом весов редкости.
         */
        fun getRandomWeighted(): PowerUpType {
            val rand = (1..100).random()
            return when {
                rand <= 40 -> getRandom(PowerUpRarity.COMMON)      // 40%
                rand <= 70 -> getRandom(PowerUpRarity.UNCOMMON)    // 30%
                rand <= 90 -> getRandom(PowerUpRarity.RARE)        // 20%
                else -> getRandom(PowerUpRarity.EPIC)              // 10%
            }
        }

        /**
         * Проверка, является ли бонус мгновенным.
         */
        fun isInstant(type: PowerUpType): Boolean = type.durationSeconds <= 0f
    }
}

/**
 * Редкость бонуса.
 */
enum class PowerUpRarity(
    val displayName: String,
    val color: Int,
    val weight: Int
) {
    COMMON("Обычный", 0xFFA0A0A0.toInt(), 40),      // Серый
    UNCOMMON("Необычный", 0xFF00FF00.toInt(), 30),  // Зелёный
    RARE("Редкий", 0xFF0080FF.toInt(), 20),         // Синий
    EPIC("Эпический", 0xFFFF8000.toInt(), 10);      // Оранжевый

    companion object {
        /**
         * Все редкости.
         */
        val ALL: List<PowerUpRarity> = entries.toList()
    }
}
