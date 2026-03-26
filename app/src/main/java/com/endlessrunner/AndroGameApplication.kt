package com.endlessrunner

import android.app.Application
import android.util.Log
import com.endlessrunner.audio.AudioLoader
import com.endlessrunner.audio.AudioManager
import com.endlessrunner.config.ConfigManager
import com.endlessrunner.data.local.AppDatabase
import com.endlessrunner.di.initKoin
import com.endlessrunner.managers.AchievementManager
import com.endlessrunner.managers.GameManager
import com.endlessrunner.managers.ProgressManager
import com.endlessrunner.managers.ResourceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Application class для игры.
 * Инициализирует DI и глобальные менеджеры.
 */
class AndroGameApplication : Application() {

    companion object {
        private const val TAG = "AndroGameApplication"

        /** Singleton instance для доступа из любого места */
        lateinit var instance: AndroGameApplication
            private set
    }

    /** GameManager (ленивая инициализация) */
    val gameManager: GameManager by lazy { GameManager.getInstance(this) }

    /** ConfigManager */
    val configManager: ConfigManager by lazy { ConfigManager.getInstance(this) }

    /** ResourceManager */
    val resourceManager: ResourceManager by lazy { ResourceManager(this) }

    /** AudioManager */
    val audioManager: AudioManager by lazy { AudioManager.getInstance(this) }

    /** AudioLoader */
    val audioLoader: AudioLoader by lazy { AudioLoader.getInstance(this, audioManager) }

    /** ProgressManager */
    val progressManager: ProgressManager by lazy {
        org.koin.core.context.GlobalContext.get().get()
    }

    /** AchievementManager */
    val achievementManager: AchievementManager by lazy {
        org.koin.core.context.GlobalContext.get().get()
    }

    /** CoroutineScope для операций приложения */
    private val appScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreate() {
        super.onCreate()

        instance = this

        Log.i(TAG, "AndroGameApplication onCreate")

        // Инициализация Koin DI
        initKoin(this)

        // Инициализация ConfigManager
        configManager.load()

        // Инициализация AudioManager
        audioManager.initialize()

        // Инициализация ResourceManager
        resourceManager.initialize()

        // Инициализация прогресса и достижений
        initializeGameData()

        // Предзагрузка аудио (асинхронно)
        preloadAudio()

        Log.i(TAG, "AndroGameApplication инициализирован")
    }

    /**
     * Предзагрузка аудио ресурсов.
     */
    private fun preloadAudio() {
        appScope.launch {
            try {
                // Предзагрузка звуков игрока и UI
                audioLoader.preloadCategory(
                    com.endlessrunner.audio.SoundLibrary.SoundCategory.PLAYER
                )
                audioLoader.preloadCategory(
                    com.endlessrunner.audio.SoundLibrary.SoundCategory.UI
                )
                audioLoader.preloadCategory(
                    com.endlessrunner.audio.SoundLibrary.SoundCategory.COINS
                )

                Log.i(TAG, "Аудио ресурсы предзагружены")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при предзагрузке аудио", e)
            }
        }
    }

    /**
     * Инициализация игровых данных (прогресс, достижения).
     */
    private fun initializeGameData() {
        appScope.launch {
            try {
                // Инициализация прогресса игрока
                progressManager.initializeProgress()

                // Инициализация достижений
                achievementManager.initialize()

                Log.i(TAG, "Игровые данные инициализированы")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при инициализации игровых данных", e)
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        Log.i(TAG, "AndroGameApplication onTerminate")

        // Освобождение ресурсов
        gameManager.dispose()
        resourceManager.dispose()
        progressManager.dispose()
        achievementManager.dispose()
        audioManager.release()
        appScope.cancel()

        // Закрытие базы данных
        AppDatabase.closeDatabase()
    }

    override fun onLowMemory() {
        super.onLowMemory()

        Log.w(TAG, "Low memory warning")

        // Принудительная сборка мусора
        resourceManager.forceGc()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        Log.w(TAG, "Trim memory: $level")

        when (level) {
            TRIM_MEMORY_RUNNING_CRITICAL,
            TRIM_MEMORY_COMPLETE -> {
                // Критический уровень памяти
                resourceManager.clearCache()
            }
        }
    }
}

/**
 * Extension property для доступа к Application.
 */
val android.content.Context.gameApplication: AndroGameApplication
    get() = applicationContext as AndroGameApplication

/**
 * Extension property для доступа к GameManager.
 */
val android.content.Context.gameManager: GameManager
    get() = gameApplication.gameManager

/**
 * Extension property для доступа к ConfigManager.
 */
val android.content.Context.configManager: ConfigManager
    get() = gameApplication.configManager

/**
 * Extension property для доступа к ResourceManager.
 */
val android.content.Context.resourceManager: ResourceManager
    get() = gameApplication.resourceManager

/**
 * Extension property для доступа к ProgressManager.
 */
val android.content.Context.progressManager: ProgressManager
    get() = gameApplication.progressManager

/**
 * Extension property для доступа к AchievementManager.
 */
val android.content.Context.achievementManager: AchievementManager
    get() = gameApplication.achievementManager

/**
 * Extension property для доступа к AudioManager.
 */
val android.content.Context.audioManager: AudioManager
    get() = gameApplication.audioManager

/**
 * Extension property для доступа к AudioLoader.
 */
val android.content.Context.audioLoader: AudioLoader
    get() = gameApplication.audioLoader
