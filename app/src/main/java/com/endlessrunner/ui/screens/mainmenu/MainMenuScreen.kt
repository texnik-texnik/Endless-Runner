package com.endlessrunner.ui.screens.mainmenu

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.endlessrunner.ui.components.*
import com.endlessrunner.ui.theme.GameColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

/**
 * Главное меню игры.
 *
 * @param onPlayClick Callback для начала игры
 * @param onShopClick Callback для перехода в магазин
 * @param onLeaderboardClick Callback для перехода в таблицу лидеров
 * @param onSettingsClick Callback для перехода в настройки
 * @param viewModel ViewModel (для тестов можно передать mock)
 */
@Composable
fun MainMenuScreen(
    onPlayClick: () -> Unit,
    onShopClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: MainMenuViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = GameColors()
    
    // Параллакс фон
    ParallaxBackground()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Логотип игры
            GameLogoLarge()
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Информация об игроке
            PlayerInfoCard(
                playerName = state.playerName,
                highScore = state.highScore,
                coins = state.coins
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Ежедневная награда
            if (state.dailyRewardAvailable) {
                DailyRewardCard(
                    onCollect = { viewModel.collectDailyReward() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Основные кнопки меню
            MainMenuButtons(
                onPlayClick = {
                    viewModel.startGame()
                    onPlayClick()
                },
                onShopClick = onShopClick,
                onLeaderboardClick = onLeaderboardClick,
                onSettingsClick = onSettingsClick
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Статистика
            GameStatsCard(
                totalGamesPlayed = state.totalGamesPlayed,
                highScore = state.highScore
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Версия игры
            Text(
                text = "Версия 1.0.0",
                color = colors.onSurfaceVariant.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Крупный логотип игры с анимацией.
 */
@Composable
private fun GameLogoLarge() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )
    
    val colors = GameColors()
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale)
    ) {
        Text(
            text = "ENDLESS",
            color = colors.primary,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 6.sp
        )
        
        Text(
            text = "RUNNER",
            color = colors.secondary,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 6.sp
        )
    }
}

/**
 * Карточка с информацией об игроке.
 */
@Composable
private fun PlayerInfoCard(
    playerName: String,
    highScore: Int,
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
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Имя игрока
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = playerName,
                        color = colors.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Рекорд и монеты
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Рекорд
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = colors.tertiary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = highScore.toString(),
                        color = colors.score,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Рекорд",
                        color = colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                
                // Монеты
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = colors.coin,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = coins.toString(),
                        color = colors.coin,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Монеты",
                        color = colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * Карточка ежедневной награды.
 */
@Composable
private fun DailyRewardCard(
    onCollect: () -> Unit
) {
    val colors = GameColors()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.primary.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = colors.tertiary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "🎁 Ежедневная награда",
                        color = colors.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+100 монет",
                        color = colors.coin,
                        fontSize = 14.sp
                    )
                }
            }
            
            GameButton(
                onClick = onCollect,
                primary = true,
                text = "Забрать"
            )
        }
    }
}

/**
 * Кнопки главного меню.
 */
@Composable
private fun MainMenuButtons(
    onPlayClick: () -> Unit,
    onShopClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Кнопка Play - самая большая
        GameButton(
            onClick = onPlayClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            primary = true,
            text = "ИГРАТЬ",
            icon = Icons.Default.PlayArrow
        )
        
        // Кнопка Магазин
        GameButton(
            onClick = onShopClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            primary = false,
            text = "МАГАЗИН",
            icon = Icons.Default.ShoppingCart
        )
        
        // Кнопка Таблица лидеров
        GameButton(
            onClick = onLeaderboardClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            primary = false,
            text = "ЛИДЕРЫ",
            icon = Icons.Default.Leaderboard
        )
        
        // Кнопка Настройки
        GameButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            primary = false,
            text = "НАСТРОЙКИ",
            icon = Icons.Default.Settings
        )
    }
}

/**
 * Карточка статистики игры.
 */
@Composable
private fun GameStatsCard(
    totalGamesPlayed: Int,
    highScore: Int
) {
    val colors = GameColors()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = totalGamesPlayed.toString(),
                label = "Игр сыграно",
                icon = Icons.Default.SportsEsports
            )
            
            HorizontalDivider(
                modifier = Modifier
                    .height(40.dp)
                    .padding(vertical = 8.dp),
                color = colors.surfaceVariant
            )
            
            StatItem(
                value = highScore.toString(),
                label = "Лучший счёт",
                icon = Icons.Default.Trophy
            )
        }
    }
}

/**
 * Элемент статистики.
 */
@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val colors = GameColors()
    
    Column(
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
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = colors.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}
