package com.endlessrunner.config

import android.content.Context
import android.util.Log
import com.endlessrunner.data.local.datastore.SettingsDataStore
import com.endlessrunner.domain.model.GraphicsQuality
import com.endlessrunner.domain.model.Orientation
import com.endlessrunner.domain.model.SettingsData
import com.endlessrunner.domain.model.VolumeType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Менеджер конфигурации игры.
 * Singleton для управления конфигом с поддержкой горячей перезагрузки.
 * Интегрирован с SettingsDataStore для сохранения настроек.
 *
 * @param context Android Context
 * @param settingsDataStore DataStore для настроек
 */
class ConfigManager(
    private val context: Context,
    private val settingsDataStore: SettingsDataStore? = null
) {
    companion object {
        private const val TAG = "ConfigManager"

        @Volatile
        private var instance: ConfigManager? = null

        /**
         * Получение экземпляра ConfigManager.
         *
         * @param context Context для инициализации
         * @param settingsDataStore DataStore для настроек (опционально)
         * @return Singleton экземпляр
         */
        fun getInstance(
            context: Context,
            settingsDataStore: SettingsDataStore? = null
        ): ConfigManager {
            return instance ?: synchronized(this) {
                instance ?: ConfigManager(context.applicationContext, settingsDataStore).also {
                    instance = it
                }
            }
        }

        /**
         * Сброс экземпляра (для тестов).
         */
        fun resetInstance() {
            instance?.dispose()
            instance = null
        }
    }

    /** ConfigLoader для загрузки конфигов */
    private val configLoader: ConfigLoader = ConfigLoader(context)

    /** Текущая конфигурация */
    private val _configFlow = MutableStateFlow(GameConfig.DEFAULT)

    /** StateFlow с текущей конфигурацией (только для чтения) */
    val configFlow: StateFlow<GameConfig> = _configFlow.asStateFlow()

    /** Текущие настройки игры */
    private val _settingsFlow = MutableStateFlow(SettingsData.DEFAULT)
    val settingsFlow: StateFlow<SettingsData> = _settingsFlow.asStateFlow()

    /** Текущая конфигурация (синхронный доступ) */
    val config: GameConfig
        get() = _configFlow.value

    /** Текущие настройки (синхронный доступ) */
    val settings: SettingsData
        get() = _settingsFlow.value

    /** Флаг загруженной конфигурации */
    var isConfigLoaded: Boolean = false
        private set

    /** Путь к текущему конфигу */
    private var configPath: String = "config/game_config.json"

    /** Время последней загрузки */
    private var lastLoadTimeMs: Long = 0L

    /** CoroutineScope для операций */
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    init {
        // Загрузка настроек при инициализации
        loadSettings()
    }

    /**
     * Загрузка настроек из DataStore.
     */
    private fun loadSettings() {
        scope.launch {
            try {
                settingsDataStore?.let { store ->
                    _settingsFlow.value = store.getSettings()
                    Log.d(TAG, "Настройки загружены: ${_settingsFlow.value}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при загрузке настроек", e)
                _settingsFlow.value = SettingsData.DEFAULT
            }
        }

        // Подписка на изменения настроек
        scope.launch {
            settingsDataStore?.settingsFlow?.collect { settings ->
                _settingsFlow.value = settings
                Log.d(TAG, "Настройки обновлены: graphics=${settings.graphicsQuality}, fps=${settings.showFps}")
            }
        }
    }

    /**
     * Загрузка конфигурации из assets.
     *
     * @param path Путь к JSON файлу в assets
     * @return true если загрузка успешна
     */
    fun load(path: String = "config/game_config.json"): Boolean {
        return try {
            Log.d(TAG, "Загрузка конфигурации: $path")

            val newConfig = configLoader.loadConfigSafe(path)

            _configFlow.value = newConfig
            configPath = path
            isConfigLoaded = true
            lastLoadTimeMs = System.currentTimeMillis()

            Log.i(TAG, "Конфигурация успешно загружена")
            Log.d(TAG, "Player speed: ${newConfig.player.speed}")
            Log.d(TAG, "Gravity: ${newConfig.physics.gravity}")
            Log.d(TAG, "Spawn speed: ${newConfig.spawn.initialSpawnSpeed}")

            true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки конфигурации", e)
            isConfigLoaded = false
            false
        }
    }

    /**
     * Перезагрузка текущей конфигурации.
     */
    fun reload(): Boolean {
        Log.d(TAG, "Перезагрузка конфигурации")
        return load(configPath)
    }

    /**
     * Получение текущей конфигурации.
     */
    fun getConfig(): GameConfig = _configFlow.value

    /**
     * Получение текущих настроек.
     */
    fun getSettings(): SettingsData = _settingsFlow.value

    /**
     * Получение конфигурации игрока.
     */
    fun getPlayerConfig(): PlayerConfig = _configFlow.value.player

    /**
     * Получение конфигурации спавна.
     */
    fun getSpawnConfig(): SpawnConfig = _configFlow.value.spawn

    /**
     * Получение конфигурации монет.
     */
    fun getCoinConfig(): CoinConfig = _configFlow.value.coin

    /**
     * Получение физических настроек.
     */
    fun getPhysicsConfig(): PhysicsConfig = _configFlow.value.physics

    /**
     * Получение настроек камеры.
     */
    fun getCameraConfig(): CameraConfig = _configFlow.value.camera

    /**
     * Получение настроек сложности.
     */
    fun getDifficultyConfig(): DifficultyConfig = _configFlow.value.difficulty

    /**
     * Получение общих настроек.
     */
    fun getGameConfig(): GameGeneralConfig = _configFlow.value.game

    // ============================================================================
    // УПРАВЛЕНИЕ НАСТРОЙКАМИ
    // ============================================================================

    /**
     * Обновление громкости.
     */
    fun updateVolume(type: VolumeType, value: Float) {
        scope.launch {
            settingsDataStore?.updateVolume(type, value)
        }
    }

    /**
     * Установка качества графики.
     */
    fun setGraphicsQuality(quality: GraphicsQuality) {
        scope.launch {
            settingsDataStore?.setGraphicsQuality(quality)
        }
    }

    /**
     * Переключение показа FPS.
     */
    fun toggleShowFps() {
        scope.launch {
            settingsDataStore?.toggleShowFps()
        }
    }

    /**
     * Переключение эффектов частиц.
     */
    fun toggleParticleEffects() {
        scope.launch {
            settingsDataStore?.toggleParticleEffects()
        }
    }

    /**
     * Установка ориентации экрана.
     */
    fun setScreenOrientation(orientation: Orientation) {
        scope.launch {
            settingsDataStore?.setScreenOrientation(orientation)
        }
    }

    /**
     * Сброс настроек к значениям по умолчанию.
     */
    fun resetSettings() {
        scope.launch {
            settingsDataStore?.resetToDefault()
        }
    }

    /**
     * Применение настроек низкого качества.
     */
    fun applyLowEndSettings() {
        scope.launch {
            settingsDataStore?.saveSettings(SettingsData.LOW_END)
        }
    }

    /**
     * Применение настроек высокого качества.
     */
    fun applyHighEndSettings() {
        scope.launch {
            settingsDataStore?.saveSettings(SettingsData.HIGH_END)
        }
    }

    /**
     * Проверка, включены ли эффекты частиц.
     */
    fun areParticleEffectsEnabled(): Boolean = _settingsFlow.value.particleEffects

    /**
     * Проверка, показывается ли FPS.
     */
    fun isShowFpsEnabled(): Boolean = _settingsFlow.value.showFps

    /**
     * Получение качества графики.
     */
    fun getGraphicsQuality(): GraphicsQuality = _settingsFlow.value.graphicsQuality

    // ============================================================================
    // УТИЛИТЫ
    // ============================================================================

    /**
     * Проверка необходимости перезагрузки.
     */
    fun needsReload(): Boolean {
        return false
    }

    /**
     * Валидация текущей конфигурации.
     */
    fun validate(): List<String> {
        return configLoader.validateConfig(_configFlow.value)
    }

    /**
     * Получение статистики о конфигурации.
     */
    fun getStats(): ConfigStats {
        return ConfigStats(
            isLoaded = isConfigLoaded,
            configPath = configPath,
            lastLoadTimeMs = lastLoadTimeMs,
            validationErrors = validate(),
            settings = _settingsFlow.value
        )
    }

    /**
     * Очистка ресурсов.
     */
    fun dispose() {
        Log.d(TAG, "Очистка ConfigManager")
        scope.cancel()
    }

    /**
     * Data class для статистики конфигурации.
     */
    data class ConfigStats(
        val isLoaded: Boolean,
        val configPath: String,
        val lastLoadTimeMs: Long,
        val validationErrors: List<String>,
        val settings: SettingsData
    ) {
        val isValid: Boolean
            get() = validationErrors.isEmpty()

        val timeSinceLoadMs: Long
            get() = System.currentTimeMillis() - lastLoadTimeMs
    }
}

/**
 * Extension property для удобного доступа к ConfigManager.
 */
val Context.configManager: ConfigManager
    get() = ConfigManager.getInstance(this)

/**
 * Extension функция для загрузки конфигурации.
 */
fun Context.loadGameConfig(): Boolean {
    return configManager.load()
}

/**
 * Extension функция для получения конфигурации.
 */
fun Context.getGameConfig(): GameConfig {
    return configManager.getConfig()
}
