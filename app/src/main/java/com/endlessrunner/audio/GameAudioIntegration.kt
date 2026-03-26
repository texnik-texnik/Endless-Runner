package com.endlessrunner.audio

import android.content.Context
import com.endlessrunner.collectibles.Coin
import com.endlessrunner.player.Player
import com.endlessrunner.powerups.PowerUp
import com.endlessrunner.powerups.PowerUpType

/**
 * Интеграция аудио системы с игровыми событиями.
 *
 * Объект для привязки аудио событий к игровым действиям.
 * Автоматически воспроизводит нужные звуки при событиях игры.
 *
 * @param context Контекст приложения
 * @param audioManager Менеджер аудио
 */
class GameAudioIntegration(
    private val context: Context,
    private val audioManager: AudioManager
) {
    /**
     * DynamicMusic система для адаптивной музыки.
     */
    private val dynamicMusic: DynamicMusic

    /**
     * Флаг подписки на события.
     */
    private var isSubscribed = false

    /**
     * Текущее состояние игры.
     */
    private var gameState = GameState.MENU

    /**
     * Текущий комбо счёт.
     */
    private var currentCombo = 0

    /**
     * Флаг босс файла.
     */
    private var isBossFight = false

    init {
        dynamicMusic = DynamicMusic.getInstance(audioManager)
    }

    /**
     * Подписаться на игровые события.
     * Вызывается при создании GameManager.
     */
    fun subscribeToGameEvents() {
        if (isSubscribed) return
        isSubscribed = true

        // Предзагрузка звуков
        audioManager.preloadSfx(SoundLibrary.SoundCategory.PLAYER)
        audioManager.preloadSfx(SoundLibrary.SoundCategory.COINS)
        audioManager.preloadSfx(SoundLibrary.SoundCategory.UI)
    }

    /**
     * Отписаться от игровых событий.
     */
    fun unsubscribeFromGameEvents() {
        isSubscribed = false
        currentCombo = 0
        isBossFight = false
    }

    // ==================== СОБЫТИЯ ИГРОКА ====================

    /**
     * Игрок прыгнул.
     */
    fun onPlayerJump() {
        audioManager.playSfx(SoundLibrary.PLAYER_JUMP)
    }

    /**
     * Игрок приземлился.
     *
     * @param isHard true если жёсткое приземление
     */
    fun onPlayerLand(isHard: Boolean = false) {
        val volume = if (isHard) 1.0f else 0.7f
        audioManager.playSfx(SoundLibrary.PLAYER_LAND, volume)
    }

    /**
     * Игрок получил урон.
     *
     * @param damageAmount Количество урона
     */
    fun onPlayerHit(damageAmount: Int = 1) {
        audioManager.playSfx(SoundLibrary.PLAYER_HIT)
        dynamicMusic.onLowHealth()
    }

    /**
     * Игрок умер.
     */
    fun onPlayerDeath() {
        audioManager.playSfx(SoundLibrary.PLAYER_DEATH)
    }

    /**
     * Игрок получил усиление.
     *
     * @param powerUpType Тип усиления
     */
    fun onPlayerPowerUp(powerUpType: PowerUpType) {
        audioManager.playSfx(SoundLibrary.PLAYER_POWERUP)
    }

    // ==================== СОБЫТИЯ МОНЕТ ====================

    /**
     * Собрана монета.
     *
     * @param value Номинал монеты
     * @param isMultiplier true если монета с множителем
     */
    fun onCoinCollected(value: Int = 1, isMultiplier: Boolean = false) {
        when {
            isMultiplier -> {
                audioManager.playSfx(SoundLibrary.COIN_MULTIPLIER)
            }
            value >= 5 -> {
                audioManager.playSfx(SoundLibrary.GEM_COLLECT)
            }
            else -> {
                audioManager.playSfx(SoundLibrary.COIN_COLLECT)
            }
        }
    }

    /**
     * Открыт сундук с сокровищами.
     *
     * @param rarity Редкость сундука
     */
    fun onTreasureChestOpened(rarity: TreasureRarity = TreasureRarity.COMMON) {
        audioManager.playSfx(SoundLibrary.TREASURE_CHEST)
    }

    // ==================== СОБЫТИЯ ВРАГОВ ====================

    /**
     * Враг получил урон.
     */
    fun onEnemyHit() {
        audioManager.playSfx(SoundLibrary.ENEMY_HIT)
    }

    /**
     * Враг умер.
     *
     * @param isBoss true если это был босс
     */
    fun onEnemyDeath(isBoss: Boolean = false) {
        if (isBoss) {
            audioManager.playSfx(SoundLibrary.BOSS_HIT)
            isBossFight = false
            dynamicMusic.onBossFightEnd()
        } else {
            audioManager.playSfx(SoundLibrary.ENEMY_DEATH)
        }
    }

    /**
     * Враг атаковал.
     */
    fun onEnemyAttack() {
        audioManager.playSfx(SoundLibrary.ENEMY_ATTACK)
    }

    /**
     * Босс издал рёв.
     */
    fun onBossRoar() {
        audioManager.playSfx(SoundLibrary.BOSS_ROAR)
        isBossFight = true
        dynamicMusic.onBossFight()
    }

    // ==================== СОБЫТИЯ ОКРУЖЕНИЯ ====================

    /**
     * Столкновение с препятствием.
     */
    fun onObstacleHit() {
        audioManager.playSfx(SoundLibrary.OBSTACLE_HIT)
    }

    /**
     * Разрушение платформы.
     */
    fun onPlatformBreak() {
        audioManager.playSfx(SoundLibrary.PLATFORM_BREAK)
    }

    /**
     * Открытие двери.
     */
    fun onDoorOpen() {
        audioManager.playSfx(SoundLibrary.DOOR_OPEN)
    }

    /**
     * Срабатывание шипов.
     */
    fun onSpikeTrap() {
        audioManager.playSfx(SoundLibrary.SPIKE_TRAP)
    }

    // ==================== СОБЫТИЯ БОНУСОВ ====================

    /**
     * Собран бонус.
     *
     * @param type Тип бонуса
     */
    fun onPowerUpCollected(type: PowerUpType) {
        when (type) {
            PowerUpType.SHIELD -> {
                audioManager.playSfx(SoundLibrary.SHIELD_ACTIVATE)
            }
            PowerUpType.MAGNET -> {
                audioManager.playSfx(SoundLibrary.MAGNET_ACTIVATE)
            }
            PowerUpType.SPEED -> {
                audioManager.playSfx(SoundLibrary.SPEED_BOOST)
            }
            PowerUpType.INVINCIBILITY -> {
                audioManager.playSfx(SoundLibrary.INVINCIBILITY)
            }
            else -> {
                audioManager.playSfx(SoundLibrary.POWERUP_ACTIVATE)
            }
        }
        dynamicMusic.onPowerUpCollected(type)
    }

    // ==================== СОБЫТИЯ UI ====================

    /**
     * Клик по кнопке.
     */
    fun onButtonClick() {
        audioManager.playSfx(SoundLibrary.BUTTON_CLICK)
    }

    /**
     * Наведение на кнопку.
     */
    fun onButtonHover() {
        audioManager.playSfx(SoundLibrary.BUTTON_HOVER)
    }

    /**
     * Открытие меню.
     */
    fun onMenuOpen() {
        audioManager.playSfx(SoundLibrary.MENU_OPEN)
    }

    /**
     * Закрытие меню.
     */
    fun onMenuClose() {
        audioManager.playSfx(SoundLibrary.MENU_CLOSE)
    }

    /**
     * Разблокировка достижения.
     *
     * @param achievementName Название достижения
     */
    fun onAchievementUnlocked(achievementName: String) {
        audioManager.playSfx(SoundLibrary.ACHIEVEMENT_UNLOCK)
        audioManager.playSfx(SoundLibrary.NOTIFICATION)
    }

    // ==================== СОБЫТИЯ ИГРЫ ====================

    /**
     * Игра началась.
     */
    fun onGameStart() {
        gameState = GameState.PLAYING
        currentCombo = 0
        dynamicMusic.reset()
        
        // Запуск музыки геймплея
        audioManager.playMusic(MusicLibrary.GAMEPLAY_1.id)
    }

    /**
     * Игра окончена.
     *
     * @param isVictory true если победа
     * @param score Финальный счёт
     */
    fun onGameOver(isVictory: Boolean = false, score: Int = 0) {
        gameState = GameState.GAME_OVER
        currentCombo = 0
        isBossFight = false

        if (isVictory) {
            audioManager.playMusic(MusicLibrary.VICTORY.id, fade = true)
        } else {
            audioManager.playMusic(MusicLibrary.GAME_OVER.id, fade = true)
        }
    }

    /**
     * Игра на паузе.
     */
    fun onPause() {
        gameState = GameState.PAUSED
        audioManager.pauseMusic()
        audioManager.playMusic(MusicLibrary.PAUSE.id, fade = true)
    }

    /**
     * Игра возобновлена.
     */
    fun onResume() {
        if (gameState == GameState.PAUSED) {
            audioManager.stopMusic(fade = false)
            audioManager.resumeMusic()
        }
        gameState = GameState.PLAYING
    }

    /**
     * Возврат в главное меню.
     */
    fun onReturnToMenu() {
        gameState = GameState.MENU
        currentCombo = 0
        isBossFight = false
        dynamicMusic.reset()
        
        audioManager.playMusic(MusicLibrary.MAIN_THEME.id, fade = true)
    }

    /**
     * Открыт магазин.
     */
    fun onShopOpen() {
        audioManager.playMusic(MusicLibrary.SHOP.id, fade = true)
    }

    /**
     * Закрыт магазин.
     */
    fun onShopClose() {
        audioManager.playMusic(MusicLibrary.MAIN_THEME.id, fade = true)
    }

    // ==================== КОМБО СИСТЕМА ====================

    /**
     * Увеличение комбо.
     *
     * @param combo Текущее комбо
     */
    fun onComboIncreased(combo: Int) {
        currentCombo = combo

        // Звуковое сопровождение на определённых этапах
        when (combo) {
            5, 10, 25, 50, 100 -> {
                audioManager.playSfx(SoundLibrary.NOTIFICATION)
                dynamicMusic.onCombo(combo)
            }
        }
    }

    /**
     * Сброс комбо.
     */
    fun onComboReset() {
        currentCombo = 0
    }

    /**
     * Получить текущее комбо.
     */
    fun getCurrentCombo(): Int = currentCombo

    // ==================== УРОВЕНЬ ИНТЕНСИВНОСТИ ====================

    /**
     * Установить интенсивность музыки.
     *
     * @param level Уровень интенсивности (0-3)
     */
    fun setMusicIntensity(level: Int) {
        dynamicMusic.setIntensity(level)
    }

    /**
     * Получить состояние игры.
     */
    fun getGameState(): GameState = gameState

    /**
     * Освободить ресурсы.
     */
    fun release() {
        unsubscribeFromGameEvents()
    }

    companion object {
        @Volatile
        private var instance: GameAudioIntegration? = null

        /**
         * Получить singleton экземпляр.
         */
        fun getInstance(context: Context, audioManager: AudioManager): GameAudioIntegration {
            return instance ?: synchronized(this) {
                instance ?: GameAudioIntegration(context.applicationContext, audioManager)
                    .also { instance = it }
            }
        }

        /**
         * Очистить singleton (для тестов).
         */
        fun clearInstance() {
            instance?.release()
            instance = null
        }
    }
}

/**
 * Состояния игры.
 */
enum class GameState {
    MENU,
    PLAYING,
    PAUSED,
    GAME_OVER,
    SHOP,
    SETTINGS
}

/**
 * Редкость сундука с сокровищами.
 */
enum class TreasureRarity {
    COMMON,
    RARE,
    EPIC,
    LEGENDARY
}
