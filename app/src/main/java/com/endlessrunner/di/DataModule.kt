package com.endlessrunner.di

import android.content.Context
import com.endlessrunner.data.local.AppDatabase
import com.endlessrunner.data.local.dao.AchievementDao
import com.endlessrunner.data.local.dao.GameSaveDao
import com.endlessrunner.data.local.dao.LeaderboardDao
import com.endlessrunner.data.local.dao.PlayerProgressDao
import com.endlessrunner.data.local.datastore.PlayerPreferencesDataStore
import com.endlessrunner.data.local.datastore.SettingsDataStore
import com.endlessrunner.data.repository.GameRepository
import com.endlessrunner.data.repository.GameRepositoryImpl
import com.endlessrunner.managers.AchievementManager
import com.endlessrunner.managers.LeaderboardManager
import com.endlessrunner.managers.ProgressManager
import com.endlessrunner.managers.SaveManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin модуль для Data слоя.
 * Предоставляет зависимости для работы с базой данных, DataStore и менеджерами.
 */
val dataModule = module {

    // ============================================================================
    // DATABASE
    // ============================================================================

    /**
     * Room Database (Singleton).
     */
    single {
        AppDatabase.getInstance(androidContext())
    }

    /**
     * DAO интерфейсы.
     */
    single { get<AppDatabase>().playerProgressDao() }
    single { get<AppDatabase>().gameSaveDao() }
    single { get<AppDatabase>().achievementDao() }
    single { get<AppDatabase>().leaderboardDao() }

    // ============================================================================
    // DATASTORE
    // ============================================================================

    /**
     * Settings DataStore (Singleton).
     */
    singleOf(::SettingsDataStore)

    /**
     * Player Preferences DataStore (Singleton).
     */
    singleOf(::PlayerPreferencesDataStore)

    // ============================================================================
    // REPOSITORY
    // ============================================================================

    /**
     * Game Repository (Singleton).
     */
    single<GameRepository> {
        GameRepositoryImpl(
            playerProgressDao = get(),
            gameSaveDao = get(),
            achievementDao = get(),
            leaderboardDao = get(),
            settingsDataStore = get(),
            playerPreferencesDataStore = get()
        )
    }

    // ============================================================================
    // MANAGERS
    // ============================================================================

    /**
     * Save Manager (Singleton).
     * Интервал автосохранения: 30 секунд.
     */
    single {
        SaveManager(
            repository = get(),
            autoSaveInterval = 30_000L
        )
    }

    /**
     * Progress Manager (Singleton).
     */
    singleOf(::ProgressManager)

    /**
     * Achievement Manager (Singleton).
     */
    singleOf(::AchievementManager)

    /**
     * Leaderboard Manager (Singleton).
     */
    singleOf(::LeaderboardManager)
}

/**
 * Модуль для тестов с in-memory базой данных.
 */
val testDataModule = module {

    /**
     * In-memory Room Database для тестов.
     */
    single {
        AppDatabase.builder(androidContext()).apply {
            inMemoryDatabase()
        }.build()
    }

    single { get<AppDatabase>().playerProgressDao() }
    single { get<AppDatabase>().gameSaveDao() }
    single { get<AppDatabase>().achievementDao() }
    single { get<AppDatabase>().leaderboardDao() }

    singleOf(::SettingsDataStore)
    singleOf(::PlayerPreferencesDataStore)

    single<GameRepository> {
        GameRepositoryImpl(
            playerProgressDao = get(),
            gameSaveDao = get(),
            achievementDao = get(),
            leaderboardDao = get(),
            settingsDataStore = get(),
            playerPreferencesDataStore = get()
        )
    }

    single { SaveManager(repository = get(), autoSaveInterval = 5_000L) }
    singleOf(::ProgressManager)
    singleOf(::AchievementManager)
    singleOf(::LeaderboardManager)
}
