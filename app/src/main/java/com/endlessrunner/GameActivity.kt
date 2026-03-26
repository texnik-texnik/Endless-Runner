package com.endlessrunner

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.endlessrunner.audio.AudioManager
import com.endlessrunner.config.ConfigManager
import com.endlessrunner.managers.SaveManager
import com.endlessrunner.di.getGameManager
import com.endlessrunner.di.getScoreManager
import com.endlessrunner.entities.EntityManager
import com.endlessrunner.managers.GameManager
import com.endlessrunner.managers.ScoreManager
import com.endlessrunner.ui.navigation.GameNavigationGraph
import com.endlessrunner.ui.theme.GameTheme

/**
 * Главная Activity игры.
 * Интегрирует Compose UI с игровым движком.
 */
class GameActivity : ComponentActivity() {

    companion object {
        private const val TAG = "GameActivity"
    }

    // ============================================================================
    // MANAGERS
    // ============================================================================

    /** GameManager */
    private lateinit var gameManager: GameManager

    /** ScoreManager */
    private lateinit var scoreManager: ScoreManager

    /** ConfigManager */
    private lateinit var configManager: ConfigManager

    /** SaveManager */
    private lateinit var saveManager: SaveManager

    /** EntityManager */
    private lateinit var entityManager: EntityManager

    /** AudioManager */
    private lateinit var audioManager: AudioManager

    // ============================================================================
    // LIFECYCLE
    // ============================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate")

        // Настройка окна
        setupWindow()

        // Инициализация менеджеров
        initManagers()

        // Включение Edge-to-Edge
        enableEdgeToEdge()

        // Настройка Compose UI
        setContent {
            GameTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameAppContent()
                }
            }
        }

        Log.i(TAG, "GameActivity создан")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")

        // Возобновление аудио
        audioManager.onResume()

        // Возобновление игры если была на паузе
        if (gameManager.isPaused) {
            gameManager.resumeGame()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")

        // Пауза аудио
        audioManager.onPause()

        // Пауза игры если активна
        if (gameManager.isPlaying && !gameManager.isPaused) {
            gameManager.pauseGame()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")

        // Остановка игры
        if (gameManager.isPlaying) {
            gameManager.stopGame()
        }

        // Освобождение ресурсов
        cleanup()
    }

    // ============================================================================
    // ИНИЦИАЛИЗАЦИЯ
    // ============================================================================

    /**
     * Настройка окна.
     */
    private fun setupWindow() {
        // Полноэкранный режим
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Не выключать экран
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * Инициализация менеджеров.
     */
    private fun initManagers() {
        gameManager = getGameManager()
        scoreManager = getScoreManager()
        configManager = ConfigManager.getInstance(this)
        saveManager = SaveManager(applicationContext)
        entityManager = EntityManager.getInstance()
        audioManager = AudioManager.getInstance(this)

        // Загрузка конфигурации
        configManager.load()

        // Инициализация аудио
        audioManager.initialize()
    }

    // ============================================================================
    // COMPOSE UI
    // ============================================================================

    /**
     * Основной контент приложения.
     */
    @Composable
    private fun GameAppContent() {
        val navController = rememberNavController()
        
        // Состояния для управления игрой
        var isGameRunning by remember { mutableStateOf(false) }

        GameNavigationGraph(
            navController = navController,
            onNavigateToGame = {
                Log.d(TAG, "Переход к игре")
                isGameRunning = true
                // Игра инициализируется в GameScreen
            },
            onNavigateFromGame = {
                Log.d(TAG, "Выход из игры")
                isGameRunning = false
                gameManager.stopGame()
                entityManager.clear()
            },
            startDestination = "splash"
        )
    }

    // ============================================================================
    // УПРАВЛЕНИЕ ИГРОЙ
    // ============================================================================

    /**
     * Старт игры.
     */
    fun startGame() {
        Log.d(TAG, "Старт игры")
        gameManager.startGame()
    }

    /**
     * Пауза игры.
     */
    fun pauseGame() {
        gameManager.pauseGame()
    }

    /**
     * Возобновление игры.
     */
    fun resumeGame() {
        gameManager.resumeGame()
    }

    /**
     * Перезапуск игры.
     */
    fun restartGame() {
        gameManager.restartGame()
    }

    // ============================================================================
    // ОЧИСТКА
    // ============================================================================

    /**
     * Освобождение ресурсов.
     */
    private fun cleanup() {
        entityManager.dispose()
        gameManager.dispose()
        audioManager.release()
        Log.d(TAG, "Ресурсы освобождены")
    }
}
