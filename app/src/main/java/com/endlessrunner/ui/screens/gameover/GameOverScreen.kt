package com.endlessrunner.ui.screens.gameover

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.endlessrunner.ui.components.*
import com.endlessrunner.ui.theme.GameColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

/**
 * Экран конца игры.
 * Показывает результаты и статистику забега.
 *
 * @param finalScore Финальный счёт
 * @param isNewRecord Новый рекорд
 * @param onPlayAgainClick Callback для повторной игры
 * @param onShareClick Callback для partage счёта
 * @param onMenuClick Callback для выхода в меню
 * @param viewModel ViewModel (для тестов можно передать mock)
 */
@Composable
fun GameOverScreen(
    finalScore: Int = 0,
    isNewRecord: Boolean = false,
    onPlayAgainClick: () -> Unit,
    onShareClick: () -> Unit,
    onMenuClick: () -> Unit,
    viewModel: GameOverViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = GameColors()
    
    // Инициализация при первом запуске
    LaunchedEffect(Unit) {
        viewModel.initialize(finalScore, isNewRecord)
    }
    
    // Анимация появления
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 500),
        label = "fadeIn"
    )
    
    val offsetY by animateDpAsState(
        targetValue = 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "slideIn"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Бейдж нового рекорда
            if (state.isNewRecord) {
                NewRecordBadge(
                    modifier = Modifier.offset(y = offsetY)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Заголовок
            Text(
                text = "ИГРА ОКОНЧЕНА",
                color = colors.error,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Финальный счёт
            FinalScoreCard(
                score = state.finalScore,
                bestScore = state.bestScore
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Собранные монеты
            CoinsCollectedCard(
                coins = state.coinsCollected
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Статистика забега
            StatisticsCard(
                statistics = state.statistics
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Кнопки действий
            ActionButtons(
                onPlayAgainClick = onPlayAgainClick,
                onShareClick = onShareClick,
                onMenuClick = onMenuClick
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Карточка финального счёта.
 */
@Composable
private fun FinalScoreCard(
    score: Int,
    bestScore: Int
) {
    val colors = GameColors()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Финальный счёт",
                color = colors.onSurfaceVariant,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = score.toString(),
                color = colors.score,
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Лучший счёт
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = colors.tertiary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Лучший счёт",
                        color = colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Text(
                        text = bestScore.toString(),
                        color = colors.tertiary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Карточка собранных монет.
 */
@Composable
private fun CoinsCollectedCard(
    coins: Int
) {
    val colors = GameColors()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Иконка монеты
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.size(40.dp)
                ) {
                    drawCircle(
                        color = colors.coin,
                        radius = size.minDimension / 2
                    )
                    drawCircle(
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.4f),
                        radius = size.minDimension / 4,
                        center = androidx.compose.ui.geometry.Offset(
                            size.minDimension / 4,
                            size.minDimension / 4
                        )
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "Собрано монет",
                        color = colors.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "+$coins",
                        color = colors.coin,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Карточка статистики.
 */
@Composable
private fun StatisticsCard(
    statistics: RunStatistics
) {
    val colors = GameColors()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Статистика забега",
                color = colors.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Сетка статистики
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    icon = Icons.Default.DirectionsRun,
                    label = "Дистанция",
                    value = "${(statistics.distance / 100).toInt()}м",
                    modifier = Modifier.weight(1f)
                )
                
                StatItem(
                    icon = Icons.Default.Timer,
                    label = "Время",
                    value = statistics.timePlayed,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    icon = Icons.Default.AttachMoney,
                    label = "Монеты",
                    value = statistics.coinsCollected.toString(),
                    modifier = Modifier.weight(1f)
                )
                
                StatItem(
                    icon = Icons.Default.LocalFireDepartment,
                    label = "Комбо",
                    value = "x${statistics.maxCombo}",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    icon = Icons.Default.Shield,
                    label = "Врагов",
                    value = statistics.enemiesDefeated.toString(),
                    modifier = Modifier.weight(1f)
                )
                
                StatItem(
                    icon = Icons.Default.Favorite,
                    label = "Урон",
                    value = statistics.damageTaken.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Элемент статистики.
 */
@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = GameColors()
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = colors.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = colors.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

/**
 * Кнопки действий.
 */
@Composable
private fun ActionButtons(
    onPlayAgainClick: () -> Unit,
    onShareClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Играть снова
        GameButton(
            onClick = onPlayAgainClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            primary = true,
            text = "ИГРАТЬ СНОВА",
            icon = Icons.Default.Refresh
        )
        
        // Поделиться
        GameButton(
            onClick = onShareClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            primary = false,
            text = "ПОДЕЛИТЬСЯ",
            icon = Icons.Default.Share
        )
        
        // В меню
        GameButton(
            onClick = onMenuClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            primary = false,
            text = "В МЕНЮ",
            icon = Icons.Default.Home
        )
    }
}
