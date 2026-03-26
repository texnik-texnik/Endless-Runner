# Система Боссов для Endless Runner

Полная реализация системы боссов с уникальными паттернами атак, фазами, поведением и визуальными эффектами.

## Структура Пакетов

```
bosses/
├── Boss.kt                      # Базовый класс босса
├── BossType.kt                  # Типы боссов (enum)
├── BossPhase.kt                 # Фазы босса (sealed class)
├── PatternType.kt               # Типы паттернов атак (enum)
├── ConcreteBosses.kt            # Конкретные реализации боссов
├── attacks/
│   ├── AttackPattern.kt         # Абстрактный класс паттерна
│   ├── AttackPatternFactory.kt  # Фабрика паттернов
│   ├── ProjectileAttacks.kt     # Атаки снарядами
│   ├── MeleeAttacks.kt          # Ближние атаки
│   └── SpecialAttacks.kt        # Специальные атаки
├── projectiles/
│   └── BossProjectile.kt        # Снаряды босса + Object Pool
├── minions/
│   ├── Minion.kt                # Класс миньона + Object Pool
│   └── MinionSpawner.kt         # Спавнер миньонов
├── arena/
│   └── BossArena.kt             # Арена + опасности
├── managers/
│   └── BossManager.kt           # Менеджер боссов (Singleton)
├── visuals/
│   ├── BossVisuals.kt           # Визуальные эффекты
│   └── BossHealthBar.kt         # Health bar + Warning overlay
├── config/
│   └── BossBalanceConfig.kt     # Конфигурация баланса
├── achievements/
│   └── BossAchievements.kt      # Достижения боссов
└── systems/
    └── BossSystem.kt            # Интеграция с игровыми системами
```

## Типы Боссов

### 1. GiantSlimeBoss (Гигантский Слизень)
- **Сложность:** 1/5 (Легкий)
- **HP:** 500
- **Механика:** Делится на мелких слизней, ядовитые облака
- **Атаки:** Jump, Split, PoisonCloud
- **Фаза 2:** Больше прыжков, ядовитые облака

### 2. MechDragonBoss (Механический Дракон)
- **Сложность:** 2/5 (Средний)
- **HP:** 800
- **Механика:** Летает, периодически приземляется
- **Атаки:** FireBreath, TailSwipe, MissileStorm
- **Фаза 2:** Больше ракет, лазерный луч

### 3. DarkKnightBoss (Тёмный Рыцарь)
- **Сложность:** 3/5 (Сложный)
- **HP:** 1000
- **Механика:** Щит (нужно ломать), телепортация
- **Атаки:** SwordCombo, ShieldBash, DarkOrbs
- **Фаза 2:** Без щита, но быстрее и агрессивнее

### 4. VoidGuardianBoss (Страж Пустоты)
- **Сложность:** 4/5 (Очень Сложный)
- **HP:** 1200
- **Механика:** Порталы, телепортация, гравитация
- **Атаки:** BlackHole, VoidBeam, TeleportStrike
- **Фаза 2:** Больше порталов, метеоры

### 5. FinalBossBoss (Финальный Босс)
- **Сложность:** 5/5 (Хардкор)
- **HP:** 2000
- **Механика:** Меняет форму (слим → дракон → рыцарь → истинная форма)
- **Атаки:** Комбинирует все атаки предыдущих боссов
- **4 фазы** вместо 3

## Паттерны Атак

### Projectile Attacks
- **BulletHell** - Множество снарядов по спирали
- **AimedShot** - Снаряд в игрока
- **SpreadShot** - Веер снарядов
- **RainProjectiles** - Снаряды сверху
- **OrbitProjectiles** - Снаряды по орбите
- **MeteorStrike** - Метеоры с неба
- **MissileStorm** - Ракетный залп

### Melee Attacks
- **Charge** - Рывок на игрока
- **Slam** - Удар по земле с волной
- **Swipe** - Удар лапой/мечом
- **TailSpin** - Вращение с хвостом
- **TailSwipe** - Удар хвостом
- **SwordCombo** - Комбо мечом
- **ShieldBash** - Удар щитом
- **GroundPound** - Удар по земле
- **JumpAttack** - Прыжок с атакой

### Special Attacks
- **SummonMinions** - Призыв миньонов
- **LaserBeam** - Луч через всю арену
- **TeleportStrike** - Телепортация + удар
- **Shockwave** - Волна вокруг босса
- **BlackHole** - Притягивание игрока
- **TimeSlow** - Замедление времени
- **FireBreath** - Огненное дыхание
- **VoidBeam** - Луч пустоты
- **DarkOrbs** - Тёмные сферы
- **PoisonCloud** - Ядовитое облако
- **SlimeSplit** - Разделение слизня
- **AllAttacks** - Все атаки одновременно (финальный босс)

## Фазы Босса

### Phase1 (100%-70% HP)
- Базовые атаки
- Стандартная скорость
- Нормальный урон

### Phase2 (70%-40% HP)
- Добавляются новые атаки
- Скорость +20%
- Урон +20%

### Phase3 (40%-0% HP)
- Все атаки доступны
- Скорость +40%
- Урон +40%

### Phase4 (25%-0% HP, только финальный босс)
- Истинная форма
- Все атаки одновременно
- Скорость +60%
- Урон +70%

### Enraged (<10% HP или по таймеру)
- Максимальная скорость (+100%)
- Максимальный урон (+100%)
- Игнорирует некоторые кулдауны
- Таймер: 30 секунд

## Миньоны

### Типы Миньонов
- **SLIME_SPLIT** - Осколок слизня (от разделения)
- **DRAGON_EGG** - Яйцо дракона (вылупляется в мини-дрона)
- **DARK_SPAWN** - Тёмное порождение
- **VOID_ORB** - Сфера пустоты

### Паттерны Спавна
- **CIRCLE** - По кругу вокруг точки
- **LINE** - По линии
- **RANDOM** - Случайные позиции
- **SPIRAL** - По спирали

## Арена Босса

### Опасности
- **PoisonHazard** - Ядовитые лужи (урон 5/тик)
- **FireHazard** - Огненные зоны (урон 15/тик)
- **SpikeHazard** - Шипы (урон 20/тик)
- **GravityHazard** - Зоны гравитации (притягивание)

### Платформы
- Статичные платформы
- Движущиеся платформы

## Интеграция с Системами

### BossSystem
```kotlin
// Добавление системы в GameManager
val bossSystem = BossSystem(entityManager, config)
gameManager.addSystem(bossSystem)

// Спавн босса
bossSystem.spawnBoss(BossType.GIANT_SLIME, x = 5000f, y = 600f)

// Начало боя
bossSystem.startBossFight()
```

### BossManager (Singleton)
```kotlin
val bossManager = BossManager.getInstance()

// Callbacks
bossManager.onBossStart = { boss -> /* ... */ }
bossManager.onBossDefeated = { boss -> /* ... */ }
bossManager.onBossPhaseChange = { boss, phase -> /* ... */ }

// Статистика
val stats = bossManager.getStats()
val progress = bossManager.getBossProgress(BossType.GIANT_SLIME)
```

### Визуальные Эффекты
- **Аура** - Пульсирующее свечение вокруг босса
- **Hit Effect** - Вспышка при получении урона
- **Charge Effect** - Накопление атаки
- **Phase Transition** - Эффект перехода фазы
- **Death Animation** - Анимация смерти

### Boss Health Bar
- Большое отображение сверху экрана
- Анимация изменения HP
- Маркеры фаз (70%, 40%)
- Имя босса
- Индикатор текущей фазы

### Boss Warning Overlay
- Затемнение экрана
- Появление имени босса
- Анимация пульсации
- Звуковое предупреждение

## Достижения

- **FIRST_BOSS** - Победить первого босса
- **ALL_BOSSES** - Победить всех боссов
- **FINAL_BOSS** - Победить финального босса
- **SPEEDRUN_BOSS** - Быстрая победа
- **NO_DAMAGE_BOSS** - Без урона
- **SOLO_BOSS** - Без апгрейдов
- **FLAWLESS_RUN** - Без смертей
- **ENRAGE_VICTORY** - Победа в ярости
- **MINION_SLAYER** - Убийство миньонов
- **DODGE_MASTER** - Мастер уклонения
- **BOSS_COLLECTOR** - Коллекционер

## Баланс и Настройка

### BossBalanceConfig
```kotlin
val config = BossBalanceConfig.forBoss(BossType.GIANT_SLIME)
// healthMultiplier, damageMultiplier, speedMultiplier
// patternWeights, minionSpawnRate, enrageTime
```

### DifficultyScaling
```kotlin
val scaling = DifficultyScaling.combined(playerLevel = 20, victories = 5)
// healthMultiplier: 1.75
// damageMultiplier: 1.75
// speedMultiplier: 1.1
// attackFrequencyMultiplier: 1.15
```

## Производительность

### Object Pools
- **BossProjectile** - Пул снарядов (100 начальных, 500 макс)
- **Minion** - Пул миньонов (20 начальных, 100 макс)

### Оптимизации
- Нет аллокаций в update() цикле
- Кэширование позиций
- Spatial partitioning для коллизий
- Lazy инициализация компонентов

## Пример Использования

```kotlin
// В GameManager
class GameManager {
    private lateinit var bossSystem: BossSystem

    fun init() {
        bossSystem = BossSystem(entityManager, config)
        addSystem(bossSystem)
    }

    fun onBossTrigger() {
        bossSystem.spawnBoss(BossType.MECH_DRAGON, 5000f, 600f)
        bossSystem.startBossFight()
    }
}

// В Player
class Player {
    fun takeDamage(amount: Int): Boolean {
        if (BossManager.getInstance().isBossFight) {
            // Урон во время боя с боссом
        }
        // ...
    }
}

// В CameraSystem
class CameraSystem {
    fun onBossFightStart(boss: Boss) {
        zoom(1.2f)
        shake(1f, 20f)
        // Ограничение камеры ареной
    }
}
```

## Требования к Интеграции

1. **AudioSystem** - Добавить треки:
   - `BOSS_BATTLE` - Музыка боя
   - `BOSS_WARNING` - Предупреждение
   - `BOSS_PHASE_CHANGE` - Смена фазы
   - `BOSS_DEFEATED` - Победа
   - `BOSS_ATTACK_WARNING` - Предупреждение атаки

2. **GameManager** - Обновить состояния:
   - `GameState.BOSS_FIGHT`
   - `onBossStart()`, `onBossDefeated()`

3. **CollisionSystem** - Добавить слой:
   - `LAYER_BOSS = 0b10000`
   - Обработка коллизий с боссом

4. **UIManager** - Добавить:
   - Boss health bar overlay
   - Phase indicator
   - Attack warning indicator

## KDoc Комментарии

Все классы и публичные методы имеют KDoc комментарии с описанием:
- Назначения
- Параметров
- Возвращаемых значений
- Примеров использования

## Код Готов к Компиляции

Все файлы созданы с правильными package declaration и импортами. Система полностью интегрируется с существующей архитектурой игры.
