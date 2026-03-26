package com.endlessrunner.ui.screens.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.endlessrunner.ui.components.ComboDisplay
import com.endlessrunner.ui.components.DistanceProgressBar
import com.endlessrunner.ui.components.ResourceBar
import com.endlessrunner.ui.theme.GameColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause

/**
 * HUD overlay для игрового экрана.
 * Отображает счёт, здоровье, монеты и другие игровые показатели.
 *
 * @param state Состояние игры
 * @param onPauseClick Обработчик нажатия паузы
 * @param modifier Модификатор
 */
@Composable
fun HudOverlay(
    state: GameStateUi,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GameColors()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Верхняя панель
        TopHudBar(
            score = state.score,
            coins = state.coins,
            gameTime = state.gameTime,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // Кнопка паузы
        PauseButton(
            onClick = onPauseClick,
            modifier = Modifier.align(Alignment.TopEnd)
        )
        
        // Здоровье (левый верхний угол под счётом)
        HealthBar(
            currentHealth = state.health,
            maxHealth = state.maxHealth,
            damageTaken = state.damageTaken,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 80.dp)
        )
        
        // Прогресс дистанции (верхний центр)
        DistanceProgressBar(
            currentDistance = state.distance,
            totalDistance = state.totalDistance,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 140.dp)
        )
        
        // Комбо дисплей (правый верхний угол)
        ComboDisplay(
            combo = state.combo,
            multiplier = state.multiplier,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp)
        )
        
        // Анимация получения урона
        if (state.damageTaken > 0) {
            DamageOverlay(modifier = Modifier.fillMaxSize())
        }
        
        // Анимация получения монет
        // TODO: Анимация всплывающих монет
    }
}

/**
 * Верхняя панель HUD.
 */
@Composable
private fun TopHudBar(
    score: Int,
    coins: Int,
    gameTime: String,
    modifier: Modifier = Modifier
) {
    val colors = GameColors()
    
    Row(
        modifier = modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface.copy(alpha = 0.8f))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Счёт слева
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "СЧЁТ",
                color = colors.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = score.toString(),
                color = colors.score,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        
        Spacer(modifier = Modifier.width(32.dp))
        
        // Время в центре
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ВРЕМЯ",
                color = colors.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = gameTime,
                color = colors.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(32.dp))
        
        // Монеты справа
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка монеты
            androidx.compose.ui.graphics.drawscope.Stroke
            androidx.compose.foundation.Canvas(
                modifier = Modifier.size(28.dp)
            ) {
                drawCircle(
                    color = colors.coin,
                    radius = size.minDimension / 2
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = size.minDimension / 4,
                    center = androidx.compose.ui.geometry.Offset(
                        size.minDimension / 4,
                        size.minDimension / 4
                    )
                )
            }
            
            Spacer(modifier = Modifier.width(6.dp))
            
            Text(
                text = coins.toString(),
                color = colors.coin,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Кнопка паузы.
 */
@Composable
private fun PauseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GameColors()
    
    // Анимация нажатия
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "pauseButtonScale"
    )
    
    Box(
        modifier = modifier
            .size(56.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Pause,
            contentDescription = "Пауза",
            tint = colors.onSurface,
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * Полоса здоровья.
 */
@Composable
private fun HealthBar(
    currentHealth: Float,
    maxHealth: Float,
    damageTaken: Int,
    modifier: Modifier = Modifier
) {
    val colors = GameColors()
    
    Column(
        modifier = modifier
            .width(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface.copy(alpha = 0.8f))
            .padding(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ЗДОРОВЬЕ",
                color = colors.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            
            Text(
                text = "${currentHealth.toInt()}/${maxHealth.toInt()}",
                color = colors.health,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        ResourceBar(
            currentValue = currentHealth,
            maxValue = maxHealth,
            color = if (currentHealth / maxHealth > 0.5f) {
                colors.health
            } else if (currentHealth / maxHealth > 0.25f) {
                colors.warning
            } else {
                colors.error
            },
            showText = false,
            height = 16.dp
        )
    }
}

/**
 * Оверлей получения урона.
 */
@Composable
private fun DamageOverlay(
    modifier: Modifier = Modifier
) {
    val colors = GameColors()
    val infiniteTransition = rememberInfiniteTransition(label = "damage")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "damageAlpha"
    )
    
    Box(
        modifier = modifier
            .background(colors.error.copy(alpha = alpha))
    )
}

/**
 * Анимированный счётчик с эффектом получения очков.
 */
@Composable
private fun AnimatedScoreCounter(
    score: Int,
    modifier: Modifier = Modifier
) {
    val colors = GameColors()
    
    // Анимация увеличения при изменении счёта
    var previousScore by remember { mutableStateOf(score) }
    val isScoreIncreased = score > previousScore
    
    LaunchedEffect(score) {
        if (isScoreIncreased) {
            previousScore = score
        }
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isScoreIncreased) 1.2f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "scoreScale"
    )
    
    Text(
        text = score.toString(),
        color = colors.score,
        fontSize = (28 * scale).sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = modifier.scale(scale)
    )
}
