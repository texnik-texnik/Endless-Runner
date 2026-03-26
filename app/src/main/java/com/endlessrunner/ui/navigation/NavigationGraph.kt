package com.endlessrunner.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.endlessrunner.ui.screens.game.GameScreen
import com.endlessrunner.ui.screens.gameover.GameOverScreen
import com.endlessrunner.ui.screens.leaderboard.LeaderboardScreen
import com.endlessrunner.ui.screens.mainmenu.MainMenuScreen
import com.endlessrunner.ui.screens.pause.PauseScreen
import com.endlessrunner.ui.screens.settings.SettingsScreen
import com.endlessrunner.ui.screens.shop.ShopScreen
import com.endlessrunner.ui.screens.splash.SplashScreen

/**
 * Граф навигации приложения.
 *
 * @param navController Контроллер навигации
 * @param onNavigateToGame Callback для перехода к игре (инициализация игры)
 * @param onNavigateFromGame Callback для выхода из игры (очистка игры)
 * @param startDestination Начальное назначение
 */
@Composable
fun GameNavigationGraph(
    navController: NavHostController,
    onNavigateToGame: () -> Unit = {},
    onNavigateFromGame: () -> Unit = {},
    startDestination: String = "splash"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInTransition() },
        exitTransition = { slideOutTransition() },
        popEnterTransition = { slideInPopTransition() },
        popExitTransition = { slideOutPopTransition() }
    ) {
        // Splash Screen
        composable<Route.Splash>(
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            SplashScreen(
                onNavigateToMenu = {
                    navController.navigate(Route.MainMenu) {
                        popUpTo(Route.Splash) { inclusive = true }
                    }
                }
            )
        }
        
        // Main Menu
        composable<Route.MainMenu>(
            enterTransition = { slideInHorizontally() },
            exitTransition = { slideOutHorizontally() }
        ) {
            MainMenuScreen(
                onPlayClick = {
                    onNavigateToGame()
                    navController.navigate(Route.Game())
                },
                onShopClick = { navController.navigate(Route.Shop) },
                onLeaderboardClick = { navController.navigate(Route.Leaderboard) },
                onSettingsClick = { navController.navigate(Route.Settings) }
            )
        }
        
        // Game Screen
        composable<Route.Game>(
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeIn(tween(300)) } // Fade для паузы
        ) { backStackEntry ->
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "normal"
            GameScreen(
                difficulty = difficulty,
                onPauseClick = { navController.navigate(Route.Pause()) },
                onGameOver = { score, isNewRecord ->
                    navController.navigate(Route.GameOver(score, isNewRecord)) {
                        popUpTo(Route.Game) { inclusive = true }
                    }
                }
            )
        }
        
        // Pause Screen
        composable<Route.Pause>(
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) }
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            PauseScreen(
                currentScore = score,
                onResumeClick = { navController.popBackStack() },
                onRestartClick = {
                    navController.popBackStack(Route.Game, inclusive = false)
                },
                onSettingsClick = { navController.navigate(Route.Settings) },
                onQuitClick = {
                    onNavigateFromGame()
                    navController.popBackStack(Route.MainMenu, inclusive = false)
                }
            )
        }
        
        // Game Over Screen
        composable<Route.GameOver>(
            enterTransition = { slideInVertically(initialOffsetY = { it }) },
            exitTransition = { slideOutVertically(targetOffsetY = { it }) }
        ) { backStackEntry ->
            val finalScore = backStackEntry.arguments?.getInt("finalScore") ?: 0
            val isNewRecord = backStackEntry.arguments?.getBoolean("isNewRecord") ?: false
            GameOverScreen(
                finalScore = finalScore,
                isNewRecord = isNewRecord,
                onPlayAgainClick = {
                    onNavigateFromGame()
                    navController.popBackStack(Route.Game, inclusive = false)
                },
                onShareClick = { /* Заглушка */ },
                onMenuClick = {
                    onNavigateFromGame()
                    navController.popBackStack(Route.MainMenu, inclusive = false)
                }
            )
        }
        
        // Shop Screen
        composable<Route.Shop>(
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
        ) {
            ShopScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // Settings Screen
        composable<Route.Settings>(
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
        ) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onResetClick = { /* Сброс настроек */ }
            )
        }
        
        // Leaderboard Screen
        composable<Route.Leaderboard>(
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
        ) {
            LeaderboardScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Анимации переходов.
 */
private fun slideInTransition(): ContentTransform {
    return slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(300))
}

private fun slideOutTransition(): ContentTransform {
    return slideOutHorizontally(targetOffsetX = { -it / 8 }) + fadeOut(tween(300))
}

private fun slideInPopTransition(): ContentTransform {
    return slideInHorizontally(initialOffsetX = { -it / 8 }) + fadeIn(tween(300))
}

private fun slideOutPopTransition(): ContentTransform {
    return slideOutHorizontally(targetOffsetX = { it }) + fadeOut(tween(300))
}

/**
 * Маршруты для типобезопасной навигации.
 * Использует kotlinx.serialization.
 */
sealed class Route {
    @kotlinx.serialization.Serializable
    data object Splash : Route()
    
    @kotlinx.serialization.Serializable
    data object MainMenu : Route()
    
    @kotlinx.serialization.Serializable
    data class Game(val difficulty: String = "normal") : Route()
    
    @kotlinx.serialization.Serializable
    data class Pause(val score: Int = 0) : Route()
    
    @kotlinx.serialization.Serializable
    data class GameOver(
        val finalScore: Int = 0,
        val isNewRecord: Boolean = false
    ) : Route()
    
    @kotlinx.serialization.Serializable
    data object Shop : Route()
    
    @kotlinx.serialization.Serializable
    data object Settings : Route()
    
    @kotlinx.serialization.Serializable
    data object Leaderboard : Route()
}
