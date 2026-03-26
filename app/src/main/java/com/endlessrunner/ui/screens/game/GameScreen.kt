package com.endlessrunner.ui.screens.game

import android.view.SurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.endlessrunner.config.ConfigManager
import com.endlessrunner.core.GameLoop
import com.endlessrunner.core.TimeProvider
import com.endlessrunner.di.getGameManager
import com.endlessrunner.di.getScoreManager
import com.endlessrunner.entities.EntityManager
import com.endlessrunner.managers.GameManager
import com.endlessrunner.managers.ScoreManager
import com.endlessrunner.player.PlayerInputHandler
import com.endlessrunner.systems.*
import com.endlessrunner.ui.theme.GameColors

/**
 * Игровой экран.
 * Содержит SurfaceView для рендеринга игры и HUD overlay.
 *
 * @param difficulty Сложность игры
 * @param onPauseClick Callback для паузы
 * @param onGameOver Callback для конца игры
 * @param viewModel ViewModel (для тестов можно передать mock)
 */
@Composable
fun GameScreen(
    difficulty: String = "normal",
    onPauseClick: () -> Unit,
    onGameOver: (Int, Boolean) -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colors = GameColors()
    
    // Ссылки на игровые компоненты
    var gameView by remember { mutableStateOf<SurfaceView?>(null) }
    var gameLoop by remember { mutableStateOf<GameLoop?>(null) }
    var inputHandler by remember { mutableStateOf<PlayerInputHandler?>(null) }
    
    // Инициализация игры
    LaunchedEffect(Unit) {
        val gameManager = context.getGameManager()
        val scoreManager = context.getScoreManager()
        val entityManager = EntityManager.getInstance()
        val configManager = ConfigManager.getInstance(context)
        
        // Создание SurfaceView
        gameView = SurfaceView(context)
        
        // Инициализация игрового цикла
        val timeProvider = TimeProvider()
        gameLoop = GameLoop(gameView!!, timeProvider).also { loop ->
            loop.gameUpdateListener = object : GameLoop.GameUpdateListener {
                override fun onUpdate(deltaTime: Float) {
                    gameManager.updateSystems(deltaTime)
                    entityManager.update(deltaTime)
                }
            }
            loop.gameRenderListener = object : GameLoop.GameRenderListener {
                override fun onRender(canvas: android.graphics.Canvas) {
                    canvas.drawColor(android.graphics.Color.BLACK)
                    gameManager.renderSystems(canvas)
                    entityManager.render(canvas)
                }
                
                override fun onSurfaceCreated() {}
                override fun onSurfaceChanged(width: Int, height: Int) {
                    // Настройка viewport
                }
                override fun onSurfaceDestroyed() {}
                override fun onRenderStop() {}
            }
        }
        
        // Создание обработчика ввода
        inputHandler = PlayerInputHandler(gameView!!)
        
        // Создание систем
        val config = configManager.getConfig()
        
        val inputSystem = InputSystem(entityManager, config, inputHandler!!)
        val movementSystem = MovementSystem(entityManager, config)
        val collisionSystem = CollisionSystem(entityManager, config)
        val spawnSystem = SpawnSystem(entityManager, config)
        val cameraSystem = CameraSystem(entityManager, config)
        
        // Добавление систем
        gameManager.addSystem(inputSystem)
        gameManager.addSystem(movementSystem)
        gameManager.addSystem(collisionSystem)
        gameManager.addSystem(spawnSystem)
        gameManager.addSystem(cameraSystem)
        
        // Запуск игры
        viewModel.startGame()
        gameLoop.start()
    }
    
    // Наблюдение за состоянием игры
    LaunchedEffect(state.isGameOver) {
        if (state.isGameOver) {
            val finalScore = viewModel.getFinalScore()
            val isNewRecord = viewModel.isNewRecord()
            onGameOver(finalScore, isNewRecord)
        }
    }
    
    // Очистка при выходе
    DisposableEffect(Unit) {
        onDispose {
            gameLoop?.stop()
            inputHandler?.dispose()
            val gameManager = context.getGameManager()
            gameManager.stopGame()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // SurfaceView для рендеринга игры
        gameView?.let { view ->
            AndroidView(
                factory = { view },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // HUD overlay поверх игры
        HudOverlay(
            state = state,
            onPauseClick = {
                viewModel.pauseGame()
                onPauseClick()
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Extension функции для получения менеджеров из Context.
 */
fun android.content.Context.getGameManager(): GameManager = 
    com.endlessrunner.di.getGameManager()

fun android.content.Context.getScoreManager(): ScoreManager = 
    com.endlessrunner.di.getScoreManager()
