# Аудио Система

Полная система воспроизведения музыки и звуковых эффектов для бесконечного 2D-раннера на Android.

## Архитектура

```
audio/
├── AudioConstants.kt       # Константы и лимиты
├── AudioState.kt           # Состояния аудио компонентов
├── AudioChannel.kt         # Каналы микшера (MASTER, MUSIC, SFX, etc.)
├── AudioConfig.kt          # Конфигурация аудио системы
├── AudioManager.kt         # Главный контроллер (Singleton)
├── AudioMixer.kt           # Микшер каналов
├── AudioLoader.kt          # Загрузчик аудио файлов
├── AudioCache.kt           # LRU кэш для аудио
├── AudioAssetManager.kt    # Управление путями к ассетам
├── AudioUtils.kt           # Утилиты и extension функции
├── AudioDebugOverlay.kt    # Debug overlay для отладки
├── DefaultVolumes.kt       # Громкости по умолчанию
│
├── MusicTrack.kt           # Data class музыкального трека
├── MusicPlayer.kt          # Плеер для фоновой музыки (MediaPlayer)
├── MusicLibrary.kt         # Библиотека музыкальных треков
│
├── SoundEffect.kt          # Класс звукового эффекта
├── SoundEffectPool.kt      # Object pool для звуков
├── SoundLibrary.kt         # Библиотека звуковых эффектов
├── SoundManager.kt         # Менеджер звуков (для совместимости)
│
├── DynamicMusic.kt         # Адаптивная музыка
└── GameAudioIntegration.kt # Интеграция с игровыми событиями
```

## Компоненты

### AudioConstants

Основные константы системы:
- `MAX_AUDIO_CHANNELS = 16` - Максимум каналов в SoundPool
- `MAX_SIMULTANEOUS_SFX = 8` - Максимум одновременных SFX
- `MUSIC_FADE_DURATION = 1000ms` - Длительность fade эффекта
- `AUDIO_POOL_SIZE = 32` - Размер пула объектов

### AudioManager

Главный singleton контроллер:

```kotlin
val audioManager = AudioManager.getInstance(context)

// Инициализация
audioManager.initialize()

// Управление громкостью
audioManager.setMasterVolume(0.8f)
audioManager.setMusicVolume(0.7f)
audioManager.setSfxVolume(0.9f)
audioManager.mute()
audioManager.unmute()

// Музыка
audioManager.playMusic(MusicLibrary.GAMEPLAY_1)
audioManager.stopMusic()
audioManager.pauseMusic()
audioManager.resumeMusic()

// Звуки
audioManager.playSfx(SoundLibrary.PLAYER_JUMP)
audioManager.stopAllSfx()

// Предзагрузка
audioManager.preloadAllSfx()
audioManager.preloadSfx(SoundLibrary.SoundCategory.PLAYER)

// Lifecycle
audioManager.onPause()
audioManager.onResume()
audioManager.release()
```

### MusicPlayer

Использует Android MediaPlayer для фоновой музыки:
- Fade in/out для плавных переходов
- StateFlow для реактивного UI
- Зацикливание треков
- Асинхронная загрузка

### SoundEffectPool

Object pool для звуковых эффектов:
- Минимизация аллокаций в runtime
- SoundPool для воспроизведения
- Приоритеты для важных звуков
- Автоматическое управление памятью

### SoundLibrary

Библиотека звуков по категориям:

**UI:** BUTTON_CLICK, BUTTON_HOVER, MENU_OPEN, ACHIEVEMENT_UNLOCK

**Игрок:** PLAYER_JUMP, PLAYER_LAND, PLAYER_HIT, PLAYER_DEATH, PLAYER_POWERUP

**Монеты:** COIN_COLLECT, COIN_MULTIPLIER, GEM_COLLECT

**Враги:** ENEMY_HIT, ENEMY_DEATH, ENEMY_ATTACK, BOSS_ROAR

**Окружение:** OBSTACLE_HIT, PLATFORM_BREAK

**Бонусы:** POWERUP_ACTIVATE, SHIELD_ACTIVATE, MAGNET_ACTIVATE, SPEED_BOOST

### DynamicMusic

Адаптивная музыка изменяется в зависимости от событий:
- Уровень интенсивности (0-3)
- Низкое здоровье → напряжённая музыка
- Комбо → увеличение интенсивности
- Битва с боссом → специальная тема

### GameAudioIntegration

Привязка аудио к игровым событиям:

```kotlin
val audioIntegration = GameAudioIntegration.getInstance(context, audioManager)

// Подписка на события
audioIntegration.subscribeToGameEvents()

// События игрока
audioIntegration.onPlayerJump()
audioIntegration.onPlayerLand()
audioIntegration.onPlayerHit()
audioIntegration.onPlayerDeath()

// События монет
audioIntegration.onCoinCollected(value = 10)

// События врагов
audioIntegration.onEnemyHit()
audioIntegration.onEnemyDeath(isBoss = true)
audioIntegration.onBossRoar()

// События бонусов
audioIntegration.onPowerUpCollected(PowerUpType.SHIELD)

// События игры
audioIntegration.onGameStart()
audioIntegration.onGameOver()
audioIntegration.onPause()
audioIntegration.onResume()
audioIntegration.onReturnToMenu()
```

## Интеграция

### SettingsViewModel

Управление громкостью через UI:

```kotlin
class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val context: Context
) : ViewModel() {
    private val audioManager = AudioManager.getInstance(context)
    
    fun setMasterVolume(value: Float) {
        audioManager.setMasterVolume(value)
        // Сохранение в DataStore
    }
    
    fun toggleMute() {
        audioManager.toggleMute()
    }
}
```

### GameManager

Интеграция с игровыми событиями:

```kotlin
class GameManager(private val context: Context) {
    private val audioManager = AudioManager.getInstance(context)
    private val audioIntegration = GameAudioIntegration.getInstance(context, audioManager)
    
    fun startGame() {
        audioIntegration.subscribeToGameEvents()
        audioIntegration.onGameStart()
    }
    
    fun onCoinCollected(value: Int) {
        audioIntegration.onCoinCollected(value)
    }
    
    fun onEnemyDefeated() {
        audioIntegration.onEnemyDeath()
    }
    
    fun dispose() {
        audioIntegration.unsubscribeFromGameEvents()
    }
}
```

### Player

Звуки игрока:

```kotlin
class Player(private val context: Context?) {
    private val audioManager = context?.let { AudioManager.getInstance(it) }
    
    fun jump(): Boolean {
        val jumped = movementComponent.jump()
        if (jumped) {
            audioManager?.playSfx(SoundLibrary.PLAYER_JUMP)
        }
        return jumped
    }
    
    fun takeDamage(damage: Int): Boolean {
        audioManager?.playSfx(SoundLibrary.PLAYER_HIT)
        // ...
    }
    
    fun die() {
        audioManager?.playSfx(SoundLibrary.PLAYER_DEATH)
        // ...
    }
}
```

### Coin

Звук сбора монеты:

```kotlin
class Coin(private val context: Context?) {
    private val audioManager = context?.let { AudioManager.getInstance(it) }
    
    override fun onCollect(collector: Entity?) {
        if (collector is Player) {
            collector.collectCoin(value)
        }
        audioManager?.playSfx(SoundLibrary.COIN_COLLECT)
    }
}
```

### AchievementManager

Звук разблокировки достижения:

```kotlin
class AchievementManager(private val context: Context) {
    private val audioManager = AudioManager.getInstance(context)
    
    suspend fun unlockAchievement(achievementId: String) {
        // ...
        audioManager.playSfx(SoundLibrary.ACHIEVEMENT_UNLOCK)
        onAchievementUnlocked?.invoke(achievementId)
    }
}
```

## Lifecycle

### Инициализация (AndroGameApplication)

```kotlin
class AndroGameApplication : Application() {
    val audioManager: AudioManager by lazy { AudioManager.getInstance(this) }
    val audioLoader: AudioLoader by lazy { AudioLoader.getInstance(this, audioManager) }
    
    override fun onCreate() {
        audioManager.initialize()
        preloadAudio()
    }
    
    private fun preloadAudio() {
        appScope.launch {
            audioLoader.preloadCategory(SoundLibrary.SoundCategory.PLAYER)
            audioLoader.preloadCategory(SoundLibrary.SoundCategory.UI)
            audioLoader.preloadCategory(SoundLibrary.SoundCategory.COINS)
        }
    }
    
    override fun onTerminate() {
        audioManager.release()
    }
}
```

### Activity Lifecycle (GameActivity)

```kotlin
class GameActivity : ComponentActivity() {
    private lateinit var audioManager: AudioManager
    
    override fun onCreate() {
        audioManager = AudioManager.getInstance(this)
        audioManager.initialize()
    }
    
    override fun onResume() {
        audioManager.onResume()
    }
    
    override fun onPause() {
        audioManager.onPause()
    }
    
    override fun onDestroy() {
        audioManager.release()
    }
}
```

## Оптимизации

1. **Object Pooling** - SoundEffectPool минимизирует аллокации
2. **LRU Cache** - AudioCache кэширует часто используемые звуки
3. **StateFlow** - Реактивные обновления без polling
4. **Coroutine** - Асинхронная загрузка без блокировки UI
5. **Fade Effects** - Плавные переходы для музыки
6. **Priority System** - Важные звуки не обрезаются

## Структура Assets

```
assets/
├── music/
│   ├── main_theme.ogg
│   ├── gameplay_1.ogg
│   ├── gameplay_2.ogg
│   ├── boss_battle.ogg
│   ├── game_over.ogg
│   ├── victory.ogg
│   ├── shop.ogg
│   └── pause.ogg
│
└── sfx/
    ├── ui/
    │   ├── button_click.ogg
    │   ├── button_hover.ogg
    │   ├── menu_open.ogg
    │   └── achievement_unlock.ogg
    │
    ├── player/
    │   ├── jump.ogg
    │   ├── land.ogg
    │   ├── hit.ogg
    │   ├── death.ogg
    │   └── powerup.ogg
    │
    ├── coins/
    │   ├── coin_collect.ogg
    │   ├── coin_multiplier.ogg
    │   └── gem_collect.ogg
    │
    ├── enemies/
    │   ├── enemy_hit.ogg
    │   ├── enemy_death.ogg
    │   ├── enemy_attack.ogg
    │   └── boss_roar.ogg
    │
    ├── environment/
    │   ├── obstacle_hit.ogg
    │   ├── platform_break.ogg
    │   └── spike_trap.ogg
    │
    └── powerups/
        ├── powerup_activate.ogg
        ├── shield_activate.ogg
        ├── magnet_activate.ogg
        └── speed_boost.ogg
```

## Debug

AudioDebugOverlay для отладки:

```kotlin
val debugOverlay = AudioDebugOverlay.getInstance(audioManager)

// Переключить видимость
debugOverlay.toggleVisibility()

// Отрисовка в GameScreen
override fun render(canvas: Canvas) {
    debugOverlay.render(canvas, width, height)
}
```

## Требования

- Android API 24+
- MediaPlayer для музыки
- SoundPool для SFX
- Coroutines для асинхронности
- StateFlow для реактивности

## Производительность

- 60 FPS стабильно
- Минимальные аллокации в game loop
- Оптимизированное использование памяти
- Корректная обработка lifecycle
