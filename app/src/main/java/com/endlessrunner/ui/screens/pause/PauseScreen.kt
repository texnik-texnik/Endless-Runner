package com.endlessrunner.ui.screens.pause

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.endlessrunner.ui.components.GameButton
import com.endlessrunner.ui.theme.GameColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

/**
 * Экран паузы.
 * Показывается поверх игрового экрана.
 *
 * @param currentScore Текущий счёт
 * @param onResumeClick Callback для возобновления игры
 * @param onRestartClick Callback для перезапуска игры
 * @param onSettingsClick Callback для перехода в настройки
 * @param onQuitClick Callback для выхода в меню
 * @param viewModel ViewModel (для тестов можно передать mock)
 */
@Composable
fun PauseScreen(
    currentScore: Int = 0,
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onQuitClick: () -> Unit,
    viewModel: PauseViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = GameColors()
    
    // Обновление состояния при изменении счёта
    LaunchedEffect(currentScore) {
        // ViewModel обновится автоматически через StateFlow
    }
    
    // Полупрозрачный фон с размытием
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background.copy(alpha = 0.85f))
            .blur(4.dp)
    )
    
    // Контент паузы
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Заголовок
                Text(
                    text = "ПАУЗА",
                    color = colors.onSurface,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Текущий счёт
                ScoreDisplay(
                    currentScore = state.currentScore,
                    bestScore = state.bestScore
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Время игры
                Text(
                    text = "Время: ${state.gameTime}",
                    color = colors.onSurfaceVariant,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Кнопки
                PauseButtons(
                    onResumeClick = {
                        viewModel.resume()
                        onResumeClick()
                    },
                    onRestartClick = {
                        viewModel.restart()
                        onRestartClick()
                    },
                    onSettingsClick = onSettingsClick,
                    onQuitClick = {
                        viewModel.quit()
                        onQuitClick()
                    }
                )
            }
        }
    }
}

/**
 * Отображение счёта.
 */
@Composable
private fun ScoreDisplay(
    currentScore: Int,
    bestScore: Int
) {
    val colors = GameColors()
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Текущий счёт",
            color = colors.onSurfaceVariant,
            fontSize = 14.sp
        )
        
        Text(
            text = currentScore.toString(),
            color = colors.score,
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Лучший счёт
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = colors.tertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Рекорд: $bestScore",
                color = colors.tertiary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Кнопки меню паузы.
 */
@Composable
private fun PauseButtons(
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onQuitClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Продолжить
        GameButton(
            onClick = onResumeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            primary = true,
            text = "ПРОДОЛЖИТЬ",
            icon = Icons.Default.PlayArrow
        )
        
        // Перезапуск
        GameButton(
            onClick = onRestartClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            primary = false,
            text = "ЗАНОВО",
            icon = Icons.Default.Refresh
        )
        
        // Настройки
        GameButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            primary = false,
            text = "НАСТРОЙКИ",
            icon = Icons.Default.Settings
        )
        
        // Выход в меню
        GameButton(
            onClick = onQuitClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            primary = false,
            text = "В МЕНЮ",
            icon = Icons.Default.Home
        )
    }
}
