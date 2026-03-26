package com.endlessrunner.ui.screens.shop

import androidx.compose.ui.graphics.Color

/**
 * Базовый элемент магазина.
 */
sealed class ShopItem {
    abstract val id: String
    abstract val title: String
    abstract val description: String
    abstract val price: Int
    abstract val icon: String
}

/**
 * Элемент улучшения.
 *
 * @param id Уникальный идентификатор
 * @param title Название
 * @param description Описание эффекта
 * @param price Цена
 * @param icon Иконка
 * @param level Текущий уровень
 * @param maxLevel Максимальный уровень
 * @param effectValue Значение эффекта
 * @param upgradeType Тип улучшения
 */
data class UpgradeItem(
    override val id: String,
    override val title: String,
    override val description: String,
    override val price: Int,
    override val icon: String = "⬆️",
    val level: Int = 0,
    val maxLevel: Int = 5,
    val effectValue: Float = 0f,
    val upgradeType: UpgradeType
) : ShopItem() {
    
    /**
     * Типы улучшений.
     */
    enum class UpgradeType {
        SPEED,          // Скорость бега
        JUMP,           // Высота прыжка
        MAGNET,         // Радиус магнита
        COIN_MULTIPLIER,// Множитель монет
        HEALTH,         // Максимальное здоровье
        SHIELD,         // Время щита
        DOUBLE_JUMP     // Двойной прыжок
    }
    
    /**
     * Цена следующего уровня.
     */
    fun getNextLevelPrice(): Int {
        return (price * 1.5f * (level + 1)).toInt()
    }
    
    /**
     * Можно ли улучшить.
     */
    fun canUpgrade(): Boolean = level < maxLevel
    
    /**
     * Процент прогресса.
     */
    fun getProgressPercent(): Float = level.toFloat() / maxLevel.toFloat()
}

/**
 * Элемент скина.
 *
 * @param id Уникальный идентификатор
 * @param title Название
 * @param description Описание
 * @param price Цена
 * @param icon Иконка
 * @param color Цвет скина
 * @param isPurchased Куплен ли
 * @param isEquipped Экипирован ли
 * @param rarity Редкость
 */
data class SkinItem(
    override val id: String,
    override val title: String,
    override val description: String,
    override val price: Int,
    override val icon: String = "🎨",
    val color: Color = Color.White,
    val isPurchased: Boolean = false,
    val isEquipped: Boolean = false,
    val rarity: Rarity = Rarity.COMMON
) : ShopItem() {
    
    /**
     * Редкость предмета.
     */
    enum class Rarity(val color: Color) {
        COMMON(Color.Gray),
        RARE(Color.Blue),
        EPIC(Color(0xFF9C27B0)),
        LEGENDARY(Color(0xFFFFD700))
    }
}

/**
 * Элемент бонуса (power-up).
 *
 * @param id Уникальный идентификатор
 * @param title Название
 * @param description Описание эффекта
 * @param price Цена
 * @param icon Иконка
 * @param quantity Количество
 * @param duration Длительность в секундах
 * @param powerUpType Тип бонуса
 */
data class PowerUpItem(
    override val id: String,
    override val title: String,
    override val description: String,
    override val price: Int,
    override val icon: String = "⚡",
    val quantity: Int = 0,
    val duration: Int = 30,
    val powerUpType: PowerUpType
) : ShopItem() {
    
    /**
     * Типы бонусов.
     */
    enum class PowerUpType {
        SHIELD,         // Щит на 30 секунд
        MAGNET,         // Магнит на 30 секунд
        DOUBLE_COINS,   // 2x монеты на 30 секунд
        INVINCIBILITY,  // Непобедимость на 10 секунд
        SCORE_BOOST,    // 2x очки на 30 секунд
        JETPACK         // Джетпак на 15 секунд
    }
}

/**
 * Категории магазина.
 */
enum class ShopCategory {
    UPGRADES,   // Улучшения
    SKINS,      // Скины
    POWERUPS    // Бонусы
}

/**
 * Предустановленные улучшения.
 */
object DefaultUpgrades {
    
    val SPEED = UpgradeItem(
        id = "upgrade_speed",
        title = "Скорость",
        description = "Увеличивает максимальную скорость бега",
        price = 100,
        icon = "🏃",
        upgradeType = UpgradeItem.UpgradeType.SPEED
    )
    
    val JUMP = UpgradeItem(
        id = "upgrade_jump",
        title = "Прыжок",
        description = "Увеличивает высоту прыжка",
        price = 100,
        icon = "🦘",
        upgradeType = UpgradeItem.UpgradeType.JUMP
    )
    
    val MAGNET = UpgradeItem(
        id = "upgrade_magnet",
        title = "Магнит",
        description = "Увеличивает радиус сбора монет",
        price = 150,
        icon = "🧲",
        upgradeType = UpgradeItem.UpgradeType.MAGNET
    )
    
    val COIN_MULTIPLIER = UpgradeItem(
        id = "upgrade_coin_multiplier",
        title = "Множитель монет",
        description = "Увеличивает количество получаемых монет",
        price = 200,
        icon = "💰",
        upgradeType = UpgradeItem.UpgradeType.COIN_MULTIPLIER
    )
    
    val HEALTH = UpgradeItem(
        id = "upgrade_health",
        title = "Здоровье",
        description = "Увеличивает максимальное здоровье",
        price = 150,
        icon = "❤️",
        upgradeType = UpgradeItem.UpgradeType.HEALTH
    )
    
    val ALL = listOf(SPEED, JUMP, MAGNET, COIN_MULTIPLIER, HEALTH)
}

/**
 * Предустановленные скины.
 */
object DefaultSkins {
    
    val DEFAULT = SkinItem(
        id = "skin_default",
        title = "Классический",
        description = "Стандартный внешний вид",
        price = 0,
        icon = "👤",
        color = Color(0xFFFF6B35),
        isPurchased = true,
        isEquipped = true,
        rarity = SkinItem.Rarity.COMMON
    )
    
    val NINJA = SkinItem(
        id = "skin_ninja",
        title = "Ниндзя",
        description = "Тёмный воин ночи",
        price = 500,
        icon = "🥷",
        color = Color(0xFF1A1A1A),
        rarity = SkinItem.Rarity.RARE
    )
    
    val GOLDEN = SkinItem(
        id = "skin_golden",
        title = "Золотой",
        description = "Сияющий золотом",
        price = 1000,
        icon = "👑",
        color = Color(0xFFFFD700),
        rarity = SkinItem.Rarity.LEGENDARY
    )
    
    val ROBOT = SkinItem(
        id = "skin_robot",
        title = "Робот",
        description = "Механический бегун",
        price = 750,
        icon = "🤖",
        color = Color(0xFF90A4AE),
        rarity = SkinItem.Rarity.EPIC
    )
    
    val GHOST = SkinItem(
        id = "skin_ghost",
        title = "Призрак",
        description = "Неуловимый дух",
        price = 600,
        icon = "👻",
        color = Color(0xFFE0E0E0).copy(alpha = 0.7f),
        rarity = SkinItem.Rarity.EPIC
    )
    
    val ALL = listOf(DEFAULT, NINJA, GOLDEN, ROBOT, GHOST)
}

/**
 * Предустановленные бонусы.
 */
object DefaultPowerUps {
    
    val SHIELD = PowerUpItem(
        id = "powerup_shield",
        title = "Щит",
        description = "Защищает от одного удара",
        price = 50,
        icon = "🛡️",
        duration = 30,
        powerUpType = PowerUpItem.PowerUpType.SHIELD
    )
    
    val MAGNET = PowerUpItem(
        id = "powerup_magnet",
        title = "Магнит",
        description = "Притягивает монеты",
        price = 50,
        icon = "🧲",
        duration = 30,
        powerUpType = PowerUpItem.PowerUpType.MAGNET
    )
    
    val DOUBLE_COINS = PowerUpItem(
        id = "powerup_double_coins",
        title = "2x Монеты",
        description = "Удваивает все монеты",
        price = 75,
        icon = "💎",
        duration = 30,
        powerUpType = PowerUpItem.PowerUpType.DOUBLE_COINS
    )
    
    val INVINCIBILITY = PowerUpItem(
        id = "powerup_invincibility",
        title = "Непобедимость",
        description = "Полная неуязвимость",
        price = 100,
        icon = "⭐",
        duration = 10,
        powerUpType = PowerUpItem.PowerUpType.INVINCIBILITY
    )
    
    val ALL = listOf(SHIELD, MAGNET, DOUBLE_COINS, INVINCIBILITY)
}
