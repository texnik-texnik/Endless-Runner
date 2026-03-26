package com.endlessrunner.audio

/**
 * Библиотека звуковых эффектов.
 *
 * Содержит пресеты звуков для различных игровых событий.
 * Все пути указаны относительно папки assets/sfx/.
 */
object SoundLibrary {

    /**
     * Категории звуковых эффектов.
     */
    enum class SoundCategory(val description: String) {
        UI("Звуки интерфейса"),
        PLAYER("Звуки игрока"),
        COINS("Звуки монет и коллекционных предметов"),
        ENEMIES("Звуки врагов"),
        ENVIRONMENT("Звуки окружения"),
        POWERUPS("Звуки бонусов"),
        ALL("Все звуки");

        companion object {
            val ALL_CATEGORIES: List<SoundCategory> = entries.toList()
        }
    }

    // ==================== UI ЗВУКИ ====================

    /**
     * Клик по кнопке.
     */
    val BUTTON_CLICK = SoundEffect(
        id = "ui_button_click",
        name = "Button Click",
        assetPath = "sfx/ui/button_click.ogg",
        priority = 6,
        volume = 0.8f
    )

    /**
     * Наведение на кнопку.
     */
    val BUTTON_HOVER = SoundEffect(
        id = "ui_button_hover",
        name = "Button Hover",
        assetPath = "sfx/ui/button_hover.ogg",
        priority = 4,
        volume = 0.5f
    )

    /**
     * Открытие меню.
     */
    val MENU_OPEN = SoundEffect(
        id = "ui_menu_open",
        name = "Menu Open",
        assetPath = "sfx/ui/menu_open.ogg",
        priority = 5,
        volume = 0.7f
    )

    /**
     * Закрытие меню.
     */
    val MENU_CLOSE = SoundEffect(
        id = "ui_menu_close",
        name = "Menu Close",
        assetPath = "sfx/ui/menu_close.ogg",
        priority = 5,
        volume = 0.7f
    )

    /**
     * Уведомление.
     */
    val NOTIFICATION = SoundEffect(
        id = "ui_notification",
        name = "Notification",
        assetPath = "sfx/ui/notification.ogg",
        priority = 7,
        volume = 0.6f
    )

    /**
     * Разблокировка достижения.
     */
    val ACHIEVEMENT_UNLOCK = SoundEffect(
        id = "ui_achievement_unlock",
        name = "Achievement Unlocked",
        assetPath = "sfx/ui/achievement_unlock.ogg",
        priority = 8,
        volume = 1.0f
    )

    // ==================== ЗВУКИ ИГРОКА ====================

    /**
     * Прыжок игрока.
     */
    val PLAYER_JUMP = SoundEffect(
        id = "player_jump",
        name = "Player Jump",
        assetPath = "sfx/player/jump.ogg",
        priority = 7,
        volume = 0.9f
    )

    /**
     * Приземление игрока.
     */
    val PLAYER_LAND = SoundEffect(
        id = "player_land",
        name = "Player Land",
        assetPath = "sfx/player/land.ogg",
        priority = 6,
        volume = 0.7f
    )

    /**
     * Получение урона игроком.
     */
    val PLAYER_HIT = SoundEffect(
        id = "player_hit",
        name = "Player Hit",
        assetPath = "sfx/player/hit.ogg",
        priority = 8,
        volume = 1.0f
    )

    /**
     * Смерть игрока.
     */
    val PLAYER_DEATH = SoundEffect(
        id = "player_death",
        name = "Player Death",
        assetPath = "sfx/player/death.ogg",
        priority = 9,
        volume = 1.0f
    )

    /**
     * Получение усиления игроком.
     */
    val PLAYER_POWERUP = SoundEffect(
        id = "player_powerup",
        name = "Player Powerup",
        assetPath = "sfx/player/powerup.ogg",
        priority = 8,
        volume = 0.9f
    )

    // ==================== ЗВУКИ МОНЕТ ====================

    /**
     * Сбор монеты.
     */
    val COIN_COLLECT = SoundEffect(
        id = "coin_collect",
        name = "Coin Collect",
        assetPath = "sfx/coins/coin_collect.ogg",
        priority = 7,
        volume = 0.8f
    )

    /**
     * Сбор множителя монет.
     */
    val COIN_MULTIPLIER = SoundEffect(
        id = "coin_multiplier",
        name = "Coin Multiplier",
        assetPath = "sfx/coins/coin_multiplier.ogg",
        priority = 8,
        volume = 0.9f
    )

    /**
     * Сбор драгоценного камня.
     */
    val GEM_COLLECT = SoundEffect(
        id = "gem_collect",
        name = "Gem Collect",
        assetPath = "sfx/coins/gem_collect.ogg",
        priority = 8,
        volume = 0.9f
    )

    /**
     * Открытие сундука с сокровищами.
     */
    val TREASURE_CHEST = SoundEffect(
        id = "treasure_chest",
        name = "Treasure Chest",
        assetPath = "sfx/coins/treasure_chest.ogg",
        priority = 9,
        volume = 1.0f
    )

    // ==================== ЗВУКИ ВРАГОВ ====================

    /**
     * Попадание по врагу.
     */
    val ENEMY_HIT = SoundEffect(
        id = "enemy_hit",
        name = "Enemy Hit",
        assetPath = "sfx/enemies/enemy_hit.ogg",
        priority = 6,
        volume = 0.8f
    )

    /**
     * Смерть врага.
     */
    val ENEMY_DEATH = SoundEffect(
        id = "enemy_death",
        name = "Enemy Death",
        assetPath = "sfx/enemies/enemy_death.ogg",
        priority = 7,
        volume = 0.9f
    )

    /**
     * Атака врага.
     */
    val ENEMY_ATTACK = SoundEffect(
        id = "enemy_attack",
        name = "Enemy Attack",
        assetPath = "sfx/enemies/enemy_attack.ogg",
        priority = 8,
        volume = 0.9f
    )

    /**
     * Рёв босса.
     */
    val BOSS_ROAR = SoundEffect(
        id = "boss_roar",
        name = "Boss Roar",
        assetPath = "sfx/enemies/boss_roar.ogg",
        priority = 10,
        volume = 1.0f
    )

    /**
     * Попадание по боссу.
     */
    val BOSS_HIT = SoundEffect(
        id = "boss_hit",
        name = "Boss Hit",
        assetPath = "sfx/enemies/boss_hit.ogg",
        priority = 9,
        volume = 1.0f
    )

    // ==================== ЗВУКИ ОКРУЖЕНИЯ ====================

    /**
     * Столкновение с препятствием.
     */
    val OBSTACLE_HIT = SoundEffect(
        id = "obstacle_hit",
        name = "Obstacle Hit",
        assetPath = "sfx/environment/obstacle_hit.ogg",
        priority = 8,
        volume = 1.0f
    )

    /**
     * Разрушение платформы.
     */
    val PLATFORM_BREAK = SoundEffect(
        id = "platform_break",
        name = "Platform Break",
        assetPath = "sfx/environment/platform_break.ogg",
        priority = 7,
        volume = 0.8f
    )

    /**
     * Открытие двери.
     */
    val DOOR_OPEN = SoundEffect(
        id = "door_open",
        name = "Door Open",
        assetPath = "sfx/environment/door_open.ogg",
        priority = 6,
        volume = 0.7f
    )

    /**
     * Срабатывание шипов.
     */
    val SPIKE_TRAP = SoundEffect(
        id = "spike_trap",
        name = "Spike Trap",
        assetPath = "sfx/environment/spike_trap.ogg",
        priority = 8,
        volume = 0.9f
    )

    // ==================== ЗВУКИ БОНУСОВ ====================

    /**
     * Активация бонуса.
     */
    val POWERUP_ACTIVATE = SoundEffect(
        id = "powerup_activate",
        name = "Powerup Activate",
        assetPath = "sfx/powerups/powerup_activate.ogg",
        priority = 8,
        volume = 0.9f
    )

    /**
     * Активация щита.
     */
    val SHIELD_ACTIVATE = SoundEffect(
        id = "shield_activate",
        name = "Shield Activate",
        assetPath = "sfx/powerups/shield_activate.ogg",
        priority = 8,
        volume = 0.9f
    )

    /**
     * Активация магнита.
     */
    val MAGNET_ACTIVATE = SoundEffect(
        id = "magnet_activate",
        name = "Magnet Activate",
        assetPath = "sfx/powerups/magnet_activate.ogg",
        priority = 7,
        volume = 0.8f
    )

    /**
     * Ускорение.
     */
    val SPEED_BOOST = SoundEffect(
        id = "speed_boost",
        name = "Speed Boost",
        assetPath = "sfx/powerups/speed_boost.ogg",
        priority = 8,
        volume = 0.9f
    )

    /**
     * Непобедимость.
     */
    val INVINCIBILITY = SoundEffect(
        id = "invincibility",
        name = "Invincibility",
        assetPath = "sfx/powerups/invincibility.ogg",
        priority = 9,
        volume = 1.0f
    )

    // ==================== КОЛЛЕКЦИИ ЗВУКОВ ====================

    /**
     * Все UI звуки.
     */
    val UI_SOUNDS: List<SoundEffect> = listOf(
        BUTTON_CLICK,
        BUTTON_HOVER,
        MENU_OPEN,
        MENU_CLOSE,
        NOTIFICATION,
        ACHIEVEMENT_UNLOCK
    )

    /**
     * Все звуки игрока.
     */
    val PLAYER_SOUNDS: List<SoundEffect> = listOf(
        PLAYER_JUMP,
        PLAYER_LAND,
        PLAYER_HIT,
        PLAYER_DEATH,
        PLAYER_POWERUP
    )

    /**
     * Все звуки монет.
     */
    val COIN_SOUNDS: List<SoundEffect> = listOf(
        COIN_COLLECT,
        COIN_MULTIPLIER,
        GEM_COLLECT,
        TREASURE_CHEST
    )

    /**
     * Все звуки врагов.
     */
    val ENEMY_SOUNDS: List<SoundEffect> = listOf(
        ENEMY_HIT,
        ENEMY_DEATH,
        ENEMY_ATTACK,
        BOSS_ROAR,
        BOSS_HIT
    )

    /**
     * Все звуки окружения.
     */
    val ENVIRONMENT_SOUNDS: List<SoundEffect> = listOf(
        OBSTACLE_HIT,
        PLATFORM_BREAK,
        DOOR_OPEN,
        SPIKE_TRAP
    )

    /**
     * Все звуки бонусов.
     */
    val POWERUP_SOUNDS: List<SoundEffect> = listOf(
        POWERUP_ACTIVATE,
        SHIELD_ACTIVATE,
        MAGNET_ACTIVATE,
        SPEED_BOOST,
        INVINCIBILITY
    )

    /**
     * Все доступные звуки.
     */
    val ALL_SOUNDS: List<SoundEffect> = listOf(
        UI_SOUNDS,
        PLAYER_SOUNDS,
        COIN_SOUNDS,
        ENEMY_SOUNDS,
        ENVIRONMENT_SOUNDS,
        POWERUP_SOUNDS
    ).flatten()

    /**
     * Звуки по категориям.
     */
    fun getSoundsByCategory(category: SoundCategory): List<SoundEffect> = when (category) {
        SoundCategory.UI -> UI_SOUNDS
        SoundCategory.PLAYER -> PLAYER_SOUNDS
        SoundCategory.COINS -> COIN_SOUNDS
        SoundCategory.ENEMIES -> ENEMY_SOUNDS
        SoundCategory.ENVIRONMENT -> ENVIRONMENT_SOUNDS
        SoundCategory.POWERUPS -> POWERUP_SOUNDS
        SoundCategory.ALL -> ALL_SOUNDS
    }

    /**
     * Получить звук по ID.
     *
     * @param id Уникальный идентификатор звука
     * @return SoundEffect или null если не найден
     */
    fun getSoundById(id: String): SoundEffect? = ALL_SOUNDS.find { it.id == id }

    /**
     * Получить звук по имени.
     *
     * @param name Название звука
     * @return SoundEffect или null если не найден
     */
    fun getSoundByName(name: String): SoundEffect? = ALL_SOUNDS.find { it.name == name }
}
