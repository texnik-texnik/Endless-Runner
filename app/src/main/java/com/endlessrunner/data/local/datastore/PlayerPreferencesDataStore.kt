package com.endlessrunner.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * DataStore для хранения предпочтений игрока.
 */
private val Context.playerPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "player_preferences"
)

/**
 * Ключи Preferences для предпочтений игрока.
 */
object PlayerPreferencesKeys {
    val CURRENT_SKIN = stringPreferencesKey("current_skin")
    val PLAYER_NAME = stringPreferencesKey("player_name")
    val TUTORIAL_COMPLETED = booleanPreferencesKey("tutorial_completed")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
    val AUTO_SAVE_ENABLED = booleanPreferencesKey("auto_save_enabled")
}

/**
 * Менеджер предпочтений игрока на основе DataStore.
 * Хранит быстрые настройки и персонализацию.
 *
 * @param context Context приложения
 */
class PlayerPreferencesDataStore(private val context: Context) {

    /**
     * Поток текущего скина.
     */
    val currentSkinFlow: Flow<String> = context.playerPreferencesDataStore.data
        .map { preferences ->
            preferences[PlayerPreferencesKeys.CURRENT_SKIN] ?: "skin_default"
        }

    /**
     * Поток имени игрока.
     */
    val playerNameFlow: Flow<String> = context.playerPreferencesDataStore.data
        .map { preferences ->
            preferences[PlayerPreferencesKeys.PLAYER_NAME] ?: "Игрок"
        }

    /**
     * Поток статуса прохождения туториала.
     */
    val tutorialCompletedFlow: Flow<Boolean> = context.playerPreferencesDataStore.data
        .map { preferences ->
            preferences[PlayerPreferencesKeys.TUTORIAL_COMPLETED] ?: false
        }

    /**
     * Установка текущего скина.
     */
    suspend fun setCurrentSkin(skinId: String) {
        context.playerPreferencesDataStore.edit { preferences ->
            preferences[PlayerPreferencesKeys.CURRENT_SKIN] = skinId
        }
    }

    /**
     * Получение текущего скина.
     */
    suspend fun getCurrentSkin(): String {
        return context.playerPreferencesDataStore.data
            .map { it[PlayerPreferencesKeys.CURRENT_SKIN] ?: "skin_default" }
            .first()
    }

    /**
     * Установка имени игрока.
     */
    suspend fun setPlayerName(name: String) {
        require(name.isNotBlank()) { "Имя игрока не может быть пустым" }
        require(name.length <= 20) { "Имя игрока не может быть длиннее 20 символов" }
        context.playerPreferencesDataStore.edit { preferences ->
            preferences[PlayerPreferencesKeys.PLAYER_NAME] = name.trim()
        }
    }

    /**
     * Получение имени игрока.
     */
    suspend fun getPlayerName(): String {
        return context.playerPreferencesDataStore.data
            .map { it[PlayerPreferencesKeys.PLAYER_NAME] ?: "Игрок" }
            .first()
    }

    /**
     * Установка статуса прохождения туториала.
     */
    suspend fun setTutorialCompleted(completed: Boolean) {
        context.playerPreferencesDataStore.edit { preferences ->
            preferences[PlayerPreferencesKeys.TUTORIAL_COMPLETED] = completed
        }
    }

    /**
     * Проверка, пройден ли туториал.
     */
    suspend fun isTutorialCompleted(): Boolean {
        return context.playerPreferencesDataStore.data
            .map { it[PlayerPreferencesKeys.TUTORIAL_COMPLETED] ?: false }
            .first()
    }

    /**
     * Переключение уведомлений.
     */
    suspend fun toggleNotifications() {
        context.playerPreferencesDataStore.edit { preferences ->
            val current = preferences[PlayerPreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
            preferences[PlayerPreferencesKeys.NOTIFICATIONS_ENABLED] = !current
        }
    }

    /**
     * Проверка, включены ли уведомления.
     */
    suspend fun isNotificationsEnabled(): Boolean {
        return context.playerPreferencesDataStore.data
            .map { it[PlayerPreferencesKeys.NOTIFICATIONS_ENABLED] ?: true }
            .first()
    }

    /**
     * Переключение тактильной отдачи.
     */
    suspend fun toggleHapticFeedback() {
        context.playerPreferencesDataStore.edit { preferences ->
            val current = preferences[PlayerPreferencesKeys.HAPTIC_FEEDBACK] ?: true
            preferences[PlayerPreferencesKeys.HAPTIC_FEEDBACK] = !current
        }
    }

    /**
     * Проверка, включена ли тактильная отдача.
     */
    suspend fun isHapticFeedbackEnabled(): Boolean {
        return context.playerPreferencesDataStore.data
            .map { it[PlayerPreferencesKeys.HAPTIC_FEEDBACK] ?: true }
            .first()
    }

    /**
     * Переключение автосохранения.
     */
    suspend fun toggleAutoSave() {
        context.playerPreferencesDataStore.edit { preferences ->
            val current = preferences[PlayerPreferencesKeys.AUTO_SAVE_ENABLED] ?: true
            preferences[PlayerPreferencesKeys.AUTO_SAVE_ENABLED] = !current
        }
    }

    /**
     * Проверка, включено ли автосохранение.
     */
    suspend fun isAutoSaveEnabled(): Boolean {
        return context.playerPreferencesDataStore.data
            .map { it[PlayerPreferencesKeys.AUTO_SAVE_ENABLED] ?: true }
            .first()
    }

    /**
     * Получение всех предпочтений.
     */
    suspend fun getAllPreferences(): PlayerPreferences {
        val data = context.playerPreferencesDataStore.data.first()
        return PlayerPreferences(
            currentSkin = data[PlayerPreferencesKeys.CURRENT_SKIN] ?: "skin_default",
            playerName = data[PlayerPreferencesKeys.PLAYER_NAME] ?: "Игрок",
            tutorialCompleted = data[PlayerPreferencesKeys.TUTORIAL_COMPLETED] ?: false,
            notificationsEnabled = data[PlayerPreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
            hapticFeedback = data[PlayerPreferencesKeys.HAPTIC_FEEDBACK] ?: true,
            autoSaveEnabled = data[PlayerPreferencesKeys.AUTO_SAVE_ENABLED] ?: true
        )
    }

    /**
     * Сброс всех предпочтений.
     */
    suspend fun resetPreferences() {
        context.playerPreferencesDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Data class для всех предпочтений.
     */
    data class PlayerPreferences(
        val currentSkin: String,
        val playerName: String,
        val tutorialCompleted: Boolean,
        val notificationsEnabled: Boolean,
        val hapticFeedback: Boolean,
        val autoSaveEnabled: Boolean
    )
}
