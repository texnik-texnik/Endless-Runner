package com.endlessrunner.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "player")

class PlayerPreferencesDataStore(private val context: Context) {

    companion object {
        private val TOTAL_COINS = intPreferencesKey("total_coins")
        private val BEST_SCORE = intPreferencesKey("best_score")
        private val CURRENT_SKIN = stringPreferencesKey("current_skin")
        private val PLAYER_NAME = stringPreferencesKey("player_name")
    }

    val totalCoins: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[TOTAL_COINS] ?: 0
    }

    val bestScore: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[BEST_SCORE] ?: 0
    }

    val currentSkin: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CURRENT_SKIN] ?: "skin_default"
    }

    suspend fun setTotalCoins(coins: Int) {
        context.dataStore.edit { it[TOTAL_COINS] = coins }
    }

    suspend fun setBestScore(score: Int) {
        context.dataStore.edit { it[BEST_SCORE] = score }
    }

    suspend fun setCurrentSkin(skin: String) {
        context.dataStore.edit { it[CURRENT_SKIN] = skin }
    }

    suspend fun setPlayerName(name: String) {
        context.dataStore.edit { it[PLAYER_NAME] = name }
    }

    suspend fun getTotalCoins(): Flow<Int> = totalCoins
    suspend fun getBestScore(): Flow<Int> = bestScore
    suspend fun getCurrentSkin(): Flow<String> = currentSkin
}
