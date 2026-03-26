package com.endlessrunner.ui.screens.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
 * Экран таблицы лидеров.
 *
 * @param onBackClick Callback для возврата назад
 * @param viewModel ViewModel (для тестов можно передать mock)
 */
@Composable
fun LeaderboardScreen(
    onBackClick: () -> Unit,
    viewModel: LeaderboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = GameColors()
    
    // Параллакс фон
    ParallaxBackground()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Верхняя панель
            LeaderboardHeader(
                onBackClick = onBackClick
            )
            
            // Вкладки типов
            LeaderboardTypeTabs(
                selectedType = state.selectedType,
                onTypeSelected = { viewModel.selectType(it) }
            )
            
            // Индикатор загрузки
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(
                        size = 48.dp,
                        color = colors.primary
                    )
                }
            } else {
                // Контент таблицы лидеров
                LeaderboardContent(
                    state = state,
                    viewModel = viewModel
                )
            }
        }
    }
}

/**
 * Верхняя панель.
 */
@Composable
private fun LeaderboardHeader(
    onBackClick: () -> Unit
) {
    val colors = GameColors()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка назад
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Назад",
                tint = colors.onSurface
            )
        }
        
        // Заголовок
        Text(
            text = "ТАБЛИЦА ЛИДЕРОВ",
            color = colors.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
        
        // Кнопка обновления
        IconButton(onClick = { /* Refresh */ }) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Обновить",
                tint = colors.onSurface
            )
        }
    }
}

/**
 * Вкладки типов таблицы лидеров.
 */
@Composable
private fun LeaderboardTypeTabs(
    selectedType: LeaderboardType,
    onTypeSelected: (LeaderboardType) -> Unit
) {
    val colors = GameColors()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceVariant)
    ) {
        LeaderboardType.entries.forEach { type ->
            val isSelected = selectedType == type
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTypeSelected(type) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (type) {
                            LeaderboardType.LOCAL -> "📱 Локальные"
                            LeaderboardType.GLOBAL -> "🌍 Глобальные"
                        },
                        color = if (isSelected) colors.primary else colors.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    
                    // Индикатор выбранной вкладки
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(3.dp)
                                .background(colors.primary)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Контент таблицы лидеров.
 */
@Composable
private fun LeaderboardContent(
    state: LeaderboardState,
    viewModel: LeaderboardViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Информация о рекорде игрока
        PlayerRecordCard(
            rank = state.playerRank,
            score = state.playerScore
        )
        
        // Заголовки списка
        LeaderboardListHeader()
        
        // Список записей
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(state.entries) { index, entry ->
                LeaderboardEntryRow(
                    entry = entry,
                    isLast = index == state.entries.lastIndex
                )
            }
            
            // Сообщение если список пуст
            if (state.entries.isEmpty()) {
                item {
                    EmptyLeaderboardMessage()
                }
            }
        }
    }
}

/**
 * Карточка рекорда игрока.
 */
@Composable
private fun PlayerRecordCard(
    rank: Int,
    score: Int
) {
    val colors = GameColors()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
            Column {
                Text(
                    text = "Ваш рекорд",
                    color = colors.onSurface,
                    fontSize = 14.sp
                )
                Text(
                    text = score.toString(),
                    color = colors.score,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Ранг",
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Text(
                    text = if (rank > 0) "#$rank" else "-",
                    color = colors.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Заголовок списка.
 */
@Composable
private fun LeaderboardListHeader() {
    val colors = GameColors()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#",
            color = colors.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )
        
        Text(
            text = "Игрок",
            color = colors.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "Счёт",
            color = colors.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(80.dp)
        )
        
        Text(
            text = "Дата",
            color = colors.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(100.dp)
        )
    }
}

/**
 * Строка записи в таблице лидеров.
 */
@Composable
private fun LeaderboardEntryRow(
    entry: LeaderboardEntry,
    isLast: Boolean
) {
    val colors = GameColors()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (entry.isCurrentPlayer) colors.primary.copy(alpha = 0.15f) else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ранг
        Box(
            modifier = Modifier.width(40.dp),
            contentAlignment = Alignment.Center
        ) {
            when (entry.rank) {
                1 -> {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "1 место",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(28.dp)
                    )
                }
                2 -> {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "2 место",
                        tint = Color(0xFFC0C0C0),
                        modifier = Modifier.size(28.dp)
                    )
                }
                3 -> {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "3 место",
                        tint = Color(0xFFCD7F32),
                        modifier = Modifier.size(28.dp)
                    )
                }
                else -> {
                    Text(
                        text = entry.rank.toString(),
                        color = colors.onSurfaceVariant,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Имя игрока
        Text(
            text = entry.playerName,
            color = if (entry.isCurrentPlayer) colors.primary else colors.onSurface,
            fontSize = 16.sp,
            fontWeight = if (entry.isCurrentPlayer) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        
        // Счёт
        Text(
            text = entry.score.toString(),
            color = colors.score,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(80.dp)
        )
        
        // Дата
        Text(
            text = entry.formattedDate,
            color = colors.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.width(100.dp)
        )
    }
    
    // Разделитель
    if (!isLast) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = colors.surfaceVariant
        )
    }
}

/**
 * Сообщение о пустой таблице.
 */
@Composable
private fun EmptyLeaderboardMessage() {
    val colors = GameColors()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Leaderboard,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Таблица лидеров пуста",
                color = colors.onSurfaceVariant,
                fontSize = 16.sp
            )
            Text(
                text = "Начните играть чтобы попасть в топ!",
                color = colors.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}
