# 🎮 AndroGame - Бесконечный 2D Раннер

[![Android Build](https://github.com/YOUR_USERNAME/AndroGame/actions/workflows/build.yml/badge.svg)](https://github.com/YOUR_USERNAME/AndroGame/actions/workflows/build.yml)
[![Platform](https://img.shields.io/badge/platform-Android-blue.svg)](https://developer.android.com/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

Нативная Android 2D игра на Kotlin с современной архитектурой и полноценным геймплеем.

---

## 📸 Скриншоты

<!-- Добавьте скриншоты после первой сборки -->

---

## ✨ Особенности

### 🎮 Геймплей
- ♾️ **Бесконечный раннер** с процедурной генерацией
- 🪙 **Сбор монет** и бонусов
- 👹 **5 уникальных боссов** с разными паттернами атак
- 🎯 **27 паттернов атак** для разнообразия
- 🏆 **Таблица лидеров** и рекорды
- 🏅 **13 достижений** для разблокировки

### 🎨 Графика
- 🖼️ **Спрайтовая анимация**
- ✨ **Система частиц** (20+ эффектов)
- 🌄 **Параллакс фон**
- 📸 **Camera effects** (shake, flash, freeze)

### 🎵 Аудио
- 🎶 **8 музыкальных треков**
- 🔊 **30+ звуковых эффектов**
- 🎼 **Адаптивная музыка** (меняется от ситуации)

### 💾 Прогрессия
- 💿 **Сохранения** (Room Database + DataStore)
- 🛒 **Магазин** с апгрейдами и скинами
- 📊 **Статистика** игрока
- 🎖️ **Достижения**

### 📱 UI
- 🎨 **Jetpack Compose** для всех экранов
- 🔄 **8 экранов** (Меню, Игра, Магазин, Настройки...)
- ✨ **Плавные анимации** переходов
- 📊 **StateFlow** для реактивности

---

## 🏗️ Архитектура

```
┌─────────────────────────────────────────────────────────┐
│                    GameActivity                          │
└─────────────────────────────────────────────────────────┘
                            │
            ┌───────────────┴───────────────┐
            ▼                               ▼
┌───────────────────────┐       ┌───────────────────────┐
│    UI (Jetpack Compose)      │   Game Loop (SurfaceView)│
│  • MVVM Pattern               │  • 60 FPS               │
│  • StateFlow                  │  • Update/Render        │
│  • Navigation                 │  • Delta Time           │
└───────────────────────┘       └───────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                    Entity-Component System               │
│  • Entities: Player, Enemy, Coin, Boss                  │
│  • Components: Position, Render, Physics, Movement      │
│  • Systems: Input, Movement, Collision, Spawn, Camera   │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                    Data Layer                            │
│  • Room Database (прогресс, достижения)                 │
│  • DataStore (настройки)                                │
│  • Repository Pattern                                   │
└─────────────────────────────────────────────────────────┘
```

### Технологии
- **Kotlin** 1.9.22
- **Jetpack Compose** для UI
- **Room** 2.6.1 для БД
- **DataStore** для настроек
- **Koin** для DI
- **Kotlinx Serialization** для JSON
- **Coroutines & Flow** для асинхронности

---

## 🚀 Быстрый старт

### 1. Скачать APK (быстро)

Перейдите на [GitHub Actions](https://github.com/YOUR_USERNAME/AndroGame/actions) и скачайте последний APK из артефактов.

### 2. Собрать самостоятельно

```bash
# Клонирование
git clone https://github.com/YOUR_USERNAME/AndroGame.git
cd AndroGame

# Сборка
./gradlew assembleDebug

# Установка
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 3. Требования
- **Android SDK** 34
- **JDK** 17
- **Gradle** 8.5

---

## 📁 Структура проекта

```
app/src/main/java/com/endlessrunner/
├── core/           # Ядро движка (GameLoop, Time, ObjectPool)
├── config/         # Конфигурация игры (JSON, data classes)
├── entities/       # Сущности (Entity, Components)
├── systems/        # Системы (Input, Movement, Collision...)
├── managers/       # Менеджеры (Game, Score, Save, Audio...)
├── player/         # Игрок и компоненты
├── enemies/        # Враги и AI
├── bosses/         # Боссы и паттерны атак
├── collectibles/   # Монеты и бонусы
├── obstacles/      # Препятствия
├── powerups/       # Силы и улучшения
├── animation/      # Спрайты и анимации
├── particles/      # Система частиц
├── visual/         # Визуальные эффекты
├── audio/          # Аудио система
├── ui/             # Jetpack Compose UI
│   ├── screens/   # Экраны (Menu, Game, Shop...)
│   ├── components/# UI компоненты
│   ├── theme/     # Тема и стили
│   └── navigation/# Навигация
├── data/           # Data layer (Room, DataStore, Repository)
└── di/             # Dependency Injection (Koin)
```

---

## 🎮 Как играть

### Управление
- **Тап** - Прыжок
- **Двойной тап** - Двойной прыжок (после апгрейда)

### Цель
- Бежать как можно дальше
- Собирать монеты
- Избегать врагов и препятствий
- Побеждать боссов

### Магазин
- **Апгрейды:** Скорость, прыжок, магнит, множитель монет, здоровье
- **Скины:** Разные визуальные стили
- **Бонусы:** Щит, магнит, x2 монеты

---

## 🏆 Достижения

| Достижение | Условие |
|------------|---------|
| 🪙 Первая монета | Собрать 1 монету |
| 💰 Коллекционер | Собрать 100 монет |
| 💎 Богач | Собрать 1000 монет |
| 🎮 Первая игра | Сыграть 1 игру |
| 🎯 Ветеран | Сыграть 100 игр |
| 📈 Рекордсмен | 1000 очков |
| 🏃 Марафон | 10000 метров |
| ⚔️ Победитель босса | Победить любого босса |
| 👑 Все боссы | Победить всех 5 боссов |

---

## 🛠️ Разработка

### Настройка окружения

1. Установите **Android Studio** Arctic Fox или новее
2. Откройте проект
3. Sync Gradle
4. Запустите на эмуляторе или устройстве

### Сборка

```bash
# Debug
./gradlew assembleDebug

# Release
./gradlew assembleRelease

# Тесты
./gradlew test

# Линтинг
./gradlew lint
```

### Архитектурные паттерны
- **Entity-Component System** для игровых объектов
- **MVVM** для UI
- **Repository** для данных
- **Strategy** для AI и атак
- **State** для состояний
- **Observer** для событий
- **Object Pool** для производительности

---

## 📊 Статистика проекта

- **Язык:** Kotlin 98%, Java 2%
- **Строк кода:** ~15,000+
- **Файлов:** ~150+
- **Зависимостей:** ~30+

---

## 🤝 Вклад

Pull requests приветствуются! Для крупных изменений сначала откройте issue.

---

## 📝 License

MIT License - см. [LICENSE](LICENSE) файл

---

## 🙏 Благодарности

- **AndroidX** - библиотеки для Android
- **Jetpack Compose** - современный UI
- **Kotlin** - прекрасный язык
- **Koin** - легковесный DI

---

## 📞 Контакты

- **Email:** your.email@example.com
- **GitHub:** [@YOUR_USERNAME](https://github.com/YOUR_USERNAME)

---

**Сделано с ❤️ на Kotlin**
