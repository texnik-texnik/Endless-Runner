# 📦 Инструкция по сборке AndroGame

## 🚀 Облачная сборка через GitHub Actions (Рекомендуется)

### Шаг 1: Загрузите проект на GitHub

```bash
# Инициализация Git
cd /storage/emulated/0/AndroGame
git init
git add .
git commit -m "Initial commit"

# Создайте репозиторий на GitHub и добавьте remote
git remote add origin https://github.com/YOUR_USERNAME/AndroGame.git
git branch -M main
git push -u origin main
```

### Шаг 2: Сборка автоматически запустится

После пуша в репозиторий:
1. Перейдите на GitHub → ваш репозиторий
2. Кликните на вкладку **Actions**
3. Вы увидите запущенный workflow "Android Build"
4. Дождитесь завершения (обычно 5-10 минут)

### Шаг 3: Скачайте APK

1. Кликните на завершённый workflow (зелёная галочка)
2. Внизу страницы найдите секцию **Artifacts**
3. Кликните на **app-debug** для скачивания
4. Распакуйте ZIP файл → получите `app-debug.apk`

### Ручной запуск сборки

Для запуска сборки без пуша:
1. GitHub → репозиторий → Actions
2. Выберите "Android Build" слева
3. Кнопка **Run workflow** → **Run workflow**
4. Дождитесь завершения

---

## 💻 Локальная сборка на ПК

### Требования
- **OS:** Linux (Ubuntu 20.04+), Windows 10+, macOS 11+
- **JDK:** 17 или выше
- **Android SDK:** Platform 34, Build Tools 34.0.0
- **RAM:** 8GB минимум (рекомендуется 16GB)

### Установка JDK

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openjdk-17-jdk
java -version
```

**Windows:**
1. Скачайте JDK 17 с https://adoptium.net/
2. Установите
3. Добавьте в PATH

**macOS:**
```bash
brew install openjdk@17
```

### Сборка проекта

```bash
# Клонирование репозитория
git clone https://github.com/YOUR_USERNAME/AndroGame.git
cd AndroGame

# На Linux/Mac
chmod +x gradlew
./gradlew assembleDebug

# На Windows
gradlew.bat assembleDebug
```

### Результат

APK файл будет в:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 Установка APK на устройство

### Через ADB
```bash
adb install app-debug.apk
```

### Через файловый менеджер
1. Скопируйте APK на устройство
2. Откройте файловый менеджер
3. Кликните на APK файл
4. Разрешите установку из неизвестных источников
5. Нажмите "Установить"

---

## 🔧 Решение проблем

### Ошибка: "SDK not found"
```bash
# Создайте local.properties
echo "sdk.dir=/path/to/Android/sdk" >> local.properties
```

### Ошибка: "License not accepted"
```bash
# Linux/Mac
mkdir -p $ANDROID_HOME/licenses
echo "24333f8a63b6825ea9c5514f83c2829b004d1fee" > $ANDROID_HOME/licenses/android-sdk-license

# Windows (PowerShell)
New-Item -ItemType Directory -Force -Path "$env:ANDROID_HOME\licenses"
"24333f8a63b6825ea9c5514f83c2829b004d1fee" | Out-File -FilePath "$env:ANDROID_HOME\licenses\android-sdk-license"
```

### Ошибка: "Out of memory"
В `gradle.properties` добавьте:
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m
```

### Ошибка: "Build failed with AAPT2"
Убедитесь что установлены:
- Android SDK Platform 34
- Android SDK Build-Tools 34.0.0

Через sdkmanager:
```bash
sdkmanager "platforms;android-34" "build-tools;34.0.0"
```

---

## 📊 Статус сборки

[![Android Build](https://github.com/YOUR_USERNAME/AndroGame/actions/workflows/build.yml/badge.svg)](https://github.com/YOUR_USERNAME/AndroGame/actions/workflows/build.yml)

---

## 🎮 Быстрый старт

1. **Скачайте APK** из GitHub Actions или соберите локально
2. **Установите** на Android устройство (Android 7.0+)
3. **Запустите** игру
4. **Наслаждайтесь!** 🎉

---

## 📝 Примечания

- **Debug APK** содержит отладочную информацию и не оптимизирован
- Для production сборки используйте `./gradlew assembleRelease`
- Release APK подписывается автоматически на GitHub Actions
- APK файл работает на Android 7.0 (API 24) и выше
