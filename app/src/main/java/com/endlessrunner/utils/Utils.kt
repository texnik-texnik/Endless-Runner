package com.endlessrunner.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore для хранения настроек игры.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_settings")

/**
 * Ключи для DataStore.
 */
object PreferencesKeys {
    val MUSIC_VOLUME = floatPreferencesKey("music_volume")
    val SFX_VOLUME = floatPreferencesKey("sfx_volume")
    val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    val HIGH_SCORE = intPreferencesKey("high_score")
    val TOTAL_COINS = intPreferencesKey("total_coins")
    val DIFFICULTY = stringPreferencesKey("difficulty")
}

/**
 * Менеджер настроек игры.
 * Использует DataStore для надёжного хранения.
 */
class SettingsManager(private val context: Context) {

    private val dataStore = context.dataStore

    /**
     * Громкость музыки (0.0 - 1.0).
     */
    val musicVolume: Flow<Float> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.MUSIC_VOLUME] ?: 0.6f
    }

    /**
     * Громкость звуковых эффектов (0.0 - 1.0).
     */
    val sfxVolume: Flow<Float> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SFX_VOLUME] ?: 0.7f
    }

    /**
     * Включена ли вибрация.
     */
    val vibrationEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true
    }

    /**
     * Рекорд.
     */
    val highScore: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HIGH_SCORE] ?: 0
    }

    /**
     * Общее количество монет.
     */
    val totalCoins: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TOTAL_COINS] ?: 0
    }

    /**
     * Уровень сложности.
     */
    val difficulty: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DIFFICULTY] ?: "normal"
    }

    /**
     * Установка громкости музыки.
     */
    suspend fun setMusicVolume(volume: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MUSIC_VOLUME] = volume.coerceIn(0f, 1f)
        }
    }

    /**
     * Установка громкости SFX.
     */
    suspend fun setSfxVolume(volume: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SFX_VOLUME] = volume.coerceIn(0f, 1f)
        }
    }

    /**
     * Установка состояния вибрации.
     */
    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    /**
     * Установка нового рекорда.
     */
    suspend fun setHighScore(score: Int) {
        dataStore.edit { preferences ->
            val currentHigh = preferences[PreferencesKeys.HIGH_SCORE] ?: 0
            if (score > currentHigh) {
                preferences[PreferencesKeys.HIGH_SCORE] = score
            }
        }
    }

    /**
     * Добавление монет.
     */
    suspend fun addCoins(amount: Int) {
        dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.TOTAL_COINS] ?: 0
            preferences[PreferencesKeys.TOTAL_COINS] = current + amount
        }
    }

    /**
     * Установка сложности.
     */
    suspend fun setDifficulty(difficulty: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DIFFICULTY] = difficulty
        }
    }
}

/**
 * Утилиты для работы с математикой.
 */
object MathUtils {
    /**
     * Линейная интерполяция.
     */
    fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t.coerceIn(0f, 1f)
    }

    /**
     * Плавная интерполяция (smoothstep).
     */
    fun smoothstep(start: Float, end: Float, t: Float): Float {
        val x = t.coerceIn(0f, 1f)
        return start + (end - start) * x * x * (3f - 2f * x)
    }

    /**
     * Ограничение значения в диапазоне.
     */
    fun clamp(value: Float, min: Float, max: Float): Float {
        return value.coerceIn(min, max)
    }

    /**
     * Ограничение значения в диапазоне (Int).
     */
    fun clamp(value: Int, min: Int, max: Int): Int {
        return value.coerceIn(min, max)
    }

    /**
     * Проверка, находится ли значение в диапазоне.
     */
    fun inRange(value: Float, min: Float, max: Float): Boolean {
        return value >= min && value <= max
    }

    /**
     * Случайное число в диапазоне.
     */
    fun randomRange(min: Float, max: Float): Float {
        return min + (max - min) * kotlin.random.Random.nextFloat()
    }

    /**
     * Случайное число в диапазоне (Int).
     */
    fun randomRange(min: Int, max: Int): Int {
        return kotlin.random.Random.nextInt(min, max + 1)
    }

    /**
     * Случайный булево значение с вероятностью.
     */
    fun randomChance(probability: Float): Boolean {
        return kotlin.random.Random.nextFloat() < probability
    }

    /**
     * Конвертация градусов в радианы.
     */
    fun degreesToRadians(degrees: Float): Float {
        return kotlin.math.toRadians(degrees.toDouble()).toFloat()
    }

    /**
     * Конвертация радиан в градусы.
     */
    fun radiansToDegrees(radians: Float): Float {
        return kotlin.math.toDegrees(radians.toDouble()).toFloat()
    }

    /**
     * Расстояние между двумя точками.
     */
    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return kotlin.math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
    }

    /**
     * Квадрат расстояния (быстрее для сравнений).
     */
    fun distanceSquared(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)
    }
}

/**
 * Утилиты для логирования.
 */
object Logger {
    private const val TAG = "EndlessRunner"
    private var debugMode: Boolean = true

    fun setDebugMode(enabled: Boolean) {
        debugMode = enabled
    }

    fun d(message: String) {
        if (debugMode) android.util.Log.d(TAG, message)
    }

    fun i(message: String) {
        android.util.Log.i(TAG, message)
    }

    fun w(message: String) {
        android.util.Log.w(TAG, message)
    }

    fun e(message: String) {
        android.util.Log.e(TAG, message)
    }

    fun e(message: String, throwable: Throwable) {
        android.util.Log.e(TAG, message, throwable)
    }
}
