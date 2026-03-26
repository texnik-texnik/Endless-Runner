package com.endlessrunner.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.endlessrunner.ui.components.LoadingIndicator
import com.endlessrunner.ui.components.ParallaxBackground
import com.endlessrunner.ui.theme.GameColors

/**
 * Splash экран.
 * Показывается при запуске приложения для загрузки ресурсов.
 *
 * @param onNavigateToMenu Callback для перехода на главный экран
 * @param viewModel ViewModel (для тестов можно передать mock)
 */
@Composable
fun SplashScreen(
    onNavigateToMenu: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    
    // Наблюдение за состоянием загрузки
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            onNavigateToMenu()
        }
    }
    
    // Инициализация при первом запуске
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }
    
    // Параллакс фон
    ParallaxBackground()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GameColors().background.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Логотип игры с анимацией
            GameLogo()
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Индикатор загрузки
            if (state.isLoading) {
                LoadingIndicator(
                    size = 64.dp,
                    color = GameColors().primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Прогресс бар
                LinearProgressIndicator(
                    progress = state.progress,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = GameColors().primary,
                    trackColor = GameColors().surfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Статус загрузки
                Text(
                    text = state.statusMessage,
                    color = GameColors().onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            
            // Сообщение об ошибке
            state.error?.let { error ->
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = error,
                    color = GameColors().error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Кнопка повтора
                com.endlessrunner.ui.components.GameButton(
                    onClick = { viewModel.retry() },
                    text = "Повторить"
                )
            }
        }
    }
}

/**
 * Логотип игры с анимацией.
 */
@Composable
private fun GameLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    
    // Анимация масштабирования
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )
    
    // Анимация свечения
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoAlpha"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale)
    ) {
        // Название игры
        Text(
            text = "ENDLESS",
            color = GameColors().primary.copy(alpha = alpha),
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 4.sp
        )
        
        Text(
            text = "RUNNER",
            color = GameColors().secondary.copy(alpha = alpha),
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 4.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Подзаголовок
        Text(
            text = "Бесконечный раннер",
            color = GameColors().onSurfaceVariant.copy(alpha = alpha),
            fontSize = 16.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 2.sp
        )
    }
}

/**
 * Линейный индикатор прогресса.
 */
@Composable
private fun LinearProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    trackColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )
    
    Box(modifier = modifier) {
        // Фон
        androidx.compose.material3.LinearProgressIndicator(
            progress = 1f,
            modifier = Modifier.background(trackColor),
            color = trackColor
        )
        
        // Прогресс
        androidx.compose.material3.LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier,
            color = color
        )
    }
}
