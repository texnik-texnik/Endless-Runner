package com.endlessrunner.config

import android.content.Context
import android.util.Log
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Загрузчик конфигурации из JSON файлов.
 * Использует Kotlinx Serialization для парсинга.
 * 
 * @param context Android Context для доступа к assets
 */
class ConfigLoader(private val context: Context) {
    
    companion object {
        private const val TAG = "ConfigLoader"
        
        /** Настройки JSON парсера */
        private val jsonFormat = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
            prettyPrint = false
        }
    }
    
    /**
     * Загрузка конфигурации из assets.
     * 
     * @param path Путь к JSON файлу в assets
     * @return GameConfig или конфиг по умолчанию при ошибке
     */
    fun loadConfig(path: String = "config/game_config.json"): GameConfig {
        return try {
            Log.d(TAG, "Загрузка конфигурации из: $path")
            
            context.assets.open(path).use { inputStream ->
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                parseConfig(jsonString)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки конфигурации: ${e.message}", e)
            Log.w(TAG, "Использую конфигурацию по умолчанию")
            GameConfig.DEFAULT
        }
    }
    
    /**
     * Загрузка конфигурации из строки JSON.
     * 
     * @param jsonString JSON строка с конфигурацией
     * @return GameConfig
     * @throws SerializationException при ошибке парсинга
     */
    fun parseConfig(jsonString: String): GameConfig {
        return try {
            jsonFormat.decodeFromString(GameConfig.serializer(), jsonString)
        } catch (e: SerializationException) {
            Log.e(TAG, "Ошибка парсинга JSON: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Проверка существования конфигурационного файла.
     * 
     * @param path Путь к файлу в assets
     * @return true если файл существует
     */
    fun configExists(path: String): Boolean {
        return try {
            context.assets.open(path).close()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Валидация конфигурации.
     * Проверяет корректность значений.
     * 
     * @param config Конфигурация для проверки
     * @return Список ошибок валидации (пустой если всё ок)
     */
    fun validateConfig(config: GameConfig): List<String> {
        val errors = mutableListOf<String>()
        
        // Валидация игрока
        if (config.player.speed <= 0) {
            errors.add("player.speed должен быть > 0")
        }
        if (config.player.width <= 0 || config.player.height <= 0) {
            errors.add("player.width и player.height должны быть > 0")
        }
        if (config.player.maxHealth <= 0) {
            errors.add("player.maxHealth должен быть > 0")
        }
        
        // Валидация спавна
        if (config.spawn.minCoinDistance < 0) {
            errors.add("spawn.minCoinDistance должен быть >= 0")
        }
        if (config.spawn.maxCoinDistance < config.spawn.minCoinDistance) {
            errors.add("spawn.maxCoinDistance должен быть >= minCoinDistance")
        }
        if (config.spawn.coinSpawnChance !in 0f..1f) {
            errors.add("spawn.coinSpawnChance должен быть в диапазоне 0-1")
        }
        
        // Валидация физики
        if (config.physics.gravity < 0) {
            errors.add("physics.gravity должен быть >= 0")
        }
        if (config.physics.airFriction !in 0f..1f) {
            errors.add("physics.airFriction должен быть в диапазоне 0-1")
        }
        
        // Валидация камеры
        if (config.camera.followSpeed !in 0f..1f) {
            errors.add("camera.followSpeed должен быть в диапазоне 0-1")
        }
        
        // Валидация сложности
        if (config.difficulty.initialLevel < 1) {
            errors.add("difficulty.initialLevel должен быть >= 1")
        }
        if (config.difficulty.speedMultiplierPerLevel < 0) {
            errors.add("difficulty.speedMultiplierPerLevel должен быть >= 0")
        }
        
        if (errors.isNotEmpty()) {
            Log.w(TAG, "Ошибки валидации конфигурации: $errors")
        }
        
        return errors
    }
    
    /**
     * Загрузка конфигурации с валидацией и fallback.
     * 
     * @param path Путь к файлу
     * @return GameConfig (валидный)
     */
    fun loadConfigSafe(path: String = "config/game_config.json"): GameConfig {
        val config = loadConfig(path)
        val errors = validateConfig(config)
        
        if (errors.isNotEmpty()) {
            Log.w(TAG, "Конфигурация содержит ошибки, использую значения по умолчанию")
            return GameConfig.DEFAULT
        }
        
        return config
    }
}

/**
 * Extension функция для удобной загрузки конфига.
 */
fun Context.loadGameConfig(path: String = "config/game_config.json"): GameConfig {
    return ConfigLoader(this).loadConfigSafe(path)
}

/**
 * Extension функция для парсинга JSON строки.
 */
fun String.parseGameConfig(): GameConfig {
    return ConfigLoader(null as Context?).let { loader ->
        // Создаём временный loader без context для парсинга строки
        object {
            fun parseConfig(jsonString: String): GameConfig {
                return try {
                    ConfigLoader.jsonFormat.decodeFromString(
                        GameConfig.serializer(),
                        jsonString
                    )
                } catch (e: SerializationException) {
                    Log.e("ConfigLoader", "Ошибка парсинга JSON: ${e.message}", e)
                    GameConfig.DEFAULT
                }
            }
        }.parseConfig(this)
    }
}

// Companion object access helper
private val ConfigLoader.Companion.jsonFormat: Json
    get() = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = false
    }
