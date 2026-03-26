package com.endlessrunner.di

import com.endlessrunner.config.ConfigManager
import com.endlessrunner.config.GameConfig
import com.endlessrunner.core.TimeProvider
import com.endlessrunner.entities.EntityManager
import com.endlessrunner.managers.GameManager
import com.endlessrunner.managers.GameStateManager
import com.endlessrunner.managers.ResourceManager
import com.endlessrunner.managers.ScoreManager
import com.endlessrunner.systems.CameraSystem
import com.endlessrunner.systems.CollisionSystem
import com.endlessrunner.systems.InputSystem
import com.endlessrunner.systems.MovementSystem
import com.endlessrunner.systems.SpawnSystem
import com.endlessrunner.ui.screens.game.GameViewModel
import com.endlessrunner.ui.screens.gameover.GameOverViewModel
import com.endlessrunner.ui.screens.leaderboard.LeaderboardViewModel
import com.endlessrunner.ui.screens.mainmenu.MainMenuViewModel
import com.endlessrunner.ui.screens.pause.PauseViewModel
import com.endlessrunner.ui.screens.settings.SettingsViewModel
import com.endlessrunner.ui.screens.shop.ShopViewModel
import com.endlessrunner.ui.screens.splash.SplashViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * Koin модуль для dependency injection.
 * Включает dataModule для работы с базой данных и сохранениями.
 */
val appModule = module {
    // ============================================================================
    // CORE
    // ============================================================================

    single { TimeProvider() }

    single { GameConfig.DEFAULT }

    /**
     * ConfigManager с интеграцией SettingsDataStore.
     */
    single { ConfigManager.getInstance(androidContext(), get()) }

    // ============================================================================
    // MANAGERS
    // ============================================================================

    single { ResourceManager(androidContext()) }

    single { EntityManager.getInstance() }

    /**
     * ScoreManager с интеграцией ProgressManager.
     */
    single { ScoreManager(get()) }

    single { GameStateManager() }

    /**
     * GameManager с интеграцией всех менеджеров.
     */
    single {
        GameManager(
            config = get(),
            entityManager = get(),
            saveManager = get(),
            progressManager = get(),
            achievementManager = get(),
            leaderboardManager = get(),
            playerName = "Игрок"
        )
    }

    // ============================================================================
    // SYSTEMS
    // ============================================================================

    // InputSystem требует PlayerInputHandler, который создаётся с View
    // Поэтому создаётся вручную в GameActivity

    single { MovementSystem(get(), get()) }

    single { CollisionSystem(get(), get()) }

    single { SpawnSystem(get(), get()) }

    single { CameraSystem(get(), get()) }

    // ============================================================================
    // VIEWMODELS
    // ============================================================================

    factory { SplashViewModel(get(), get(), get(), get()) }

    factory { MainMenuViewModel(get(), get(), get()) }

    factory { GameViewModel(get(), get()) }

    factory { PauseViewModel(get(), get(), get()) }

    factory { GameOverViewModel(get(), get(), get(), get()) }

    factory { ShopViewModel(get(), get(), get()) }

    factory { SettingsViewModel(get(), get()) }

    factory { LeaderboardViewModel(get(), get()) }
}

/**
 * Полный список модулей для инициализации Koin.
 */
val allModules = listOf(
    appModule,
    dataModule
)

/**
 * Инициализация Koin.
 * Вызывается из Application.onCreate().
 */
fun initKoin(androidContext: android.content.Context) {
    startKoin {
        androidContext(androidContext)
        modules(allModules)
    }
}

/**
 * Extension функция для получения GameManager.
 */
fun getGameManager(): GameManager = org.koin.core.context.GlobalContext.get().get()

/**
 * Extension функция для получения ScoreManager.
 */
fun getScoreManager(): ScoreManager = org.koin.core.context.GlobalContext.get().get()

/**
 * Extension функция для получения GameStateManager.
 */
fun getGameStateManager(): GameStateManager = org.koin.core.context.GlobalContext.get().get()

/**
 * Extension функция для получения ResourceManager.
 */
fun getResourceManager(): ResourceManager = org.koin.core.context.GlobalContext.get().get()

/**
 * Extension функция для получения ConfigManager.
 */
fun getConfigManager(): ConfigManager = org.koin.core.context.GlobalContext.get().get()

/**
 * Extension функция для получения SaveManager.
 */
fun getSaveManager(): SaveManager = org.koin.core.context.GlobalContext.get().get()

/**
 * Extension функция для получения ProgressManager.
 */
fun getProgressManager(): com.endlessrunner.managers.ProgressManager =
    org.koin.core.context.GlobalContext.get().get()

/**
 * Extension функция для получения AchievementManager.
 */
fun getAchievementManager(): com.endlessrunner.managers.AchievementManager =
    org.koin.core.context.GlobalContext.get().get()

/**
 * Extension функция для получения LeaderboardManager.
 */
fun getLeaderboardManager(): com.endlessrunner.managers.LeaderboardManager =
    org.koin.core.context.GlobalContext.get().get()
