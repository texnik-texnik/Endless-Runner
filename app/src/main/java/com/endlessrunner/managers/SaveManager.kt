package com.endlessrunner.managers

import android.content.Context
import com.endlessrunner.data.local.datastore.PlayerPreferencesDataStore
import com.endlessrunner.data.local.datastore.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Менеджер сохранений и прогресса игрока.
 */
class SaveManager(
    private val context: Context,
    private val settingsDataStore: SettingsDataStore,
    private val playerPreferencesDataStore: PlayerPreferencesDataStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _totalCoins = MutableStateFlow(0)
    val totalCoins: StateFlow<Int> = _totalCoins.asStateFlow()

    private val _bestScore = MutableStateFlow(0)
    val bestScore: StateFlow<Int> = _bestScore.asStateFlow()

    private val _currentSkin = MutableStateFlow("skin_default")
    val currentSkin: StateFlow<String> = _currentSkin.asStateFlow()

    init {
        loadProgress()
    }

    private fun loadProgress() {
        scope.launch {
            playerPreferencesDataStore.getTotalCoins().collect { _totalCoins.value = it }
        }
        scope.launch {
            playerPreferencesDataStore.getBestScore().collect { _bestScore.value = it }
        }
        scope.launch {
            playerPreferencesDataStore.getCurrentSkin().collect { _currentSkin.value = it }
        }
    }

    fun addCoins(amount: Int) {
        scope.launch {
            val current = _totalCoins.value
            playerPreferencesDataStore.setTotalCoins(current + amount)
        }
    }

    fun setBestScore(score: Int) {
        scope.launch {
            if (score > _bestScore.value) {
                playerPreferencesDataStore.setBestScore(score)
            }
        }
    }

    fun spendCoins(amount: Int): Boolean {
        if (_totalCoins.value >= amount) {
            scope.launch {
                playerPreferencesDataStore.setTotalCoins(_totalCoins.value - amount)
            }
            return true
        }
        return false
    }

    fun setCurrentSkin(skinId: String) {
        scope.launch {
            playerPreferencesDataStore.setCurrentSkin(skinId)
        }
    }

    fun isSfxEnabled(): Boolean = true
    fun isMusicEnabled(): Boolean = true
    fun isVibrationEnabled(): Boolean = true

    fun setSfxEnabled(enabled: Boolean) {
        scope.launch {
            settingsDataStore.setSfxVolume(if (enabled) 1.0f else 0.0f)
        }
    }

    fun setMusicEnabled(enabled: Boolean) {
        scope.launch {
            settingsDataStore.setMusicVolume(if (enabled) 1.0f else 0.0f)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        // Заглушка
    }

    fun loadPlayerData() {
        loadProgress()
    }

    fun savePlayerData() {
        // Автосохранение через DataStore
    }
}
