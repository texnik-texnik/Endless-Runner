package com.endlessrunner.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.endlessrunner.data.local.datastore.PlayerPreferencesDataStore
import com.endlessrunner.data.local.datastore.SettingsDataStore
import com.endlessrunner.managers.SaveManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Koin модуль для Data слоя (DataStore only).
 */

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
private val Context.playerDataStore: DataStore<Preferences> by preferencesDataStore(name = "player")

val dataModule = module {

    // ============================================================================
    // DATASTORE
    // ============================================================================

    /**
     * Settings DataStore (Singleton).
     */
    single {
        SettingsDataStore(get())
    }

    /**
     * Player Preferences DataStore (Singleton).
     */
    single {
        PlayerPreferencesDataStore(get())
    }

    // ============================================================================
    // MANAGERS
    // ============================================================================

    /**
     * Save Manager (Singleton).
     */
    singleOf(::SaveManager)
}
