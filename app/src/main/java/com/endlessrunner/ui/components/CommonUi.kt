package com.endlessrunner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.endlessrunner.ui.theme.GameColors
import kotlinx.coroutines.delay

/**
 * Стилизованная кнопка для игры.
 *
 * @param onClick Обработчик клика
 * @param modifier Модификатор
 * @param enabled Состояние доступности
 * @param primary Является ли кнопка основной (акцентной)
 * @param text Текст кнопки
 * @param icon Иконка (опционально)
 * @param showRipple Показывать ли ripple эффект
 */
@Composable
fun GameButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = true,
    text: String,
    icon: ImageVector? = null,
    showRipple: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Анимация нажатия
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "buttonScale"
    )
    
    // Цвета
    val colors = GameColors()
    val backgroundColor = if (primary) {
        colors.primary
    } else {
        colors.surfaceVariant
    }
    val contentColor = if (primary) {
        colors.onPrimary
    } else {
        colors.onSurface
    }
    
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = if (enabled) backgroundColor else Color.Gray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = if (primary) colors.tertiary.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = if (showRipple) rememberRipple() else null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Текстовое поле для ввода (например, имени игрока).
 *
 * @param value Текущее значение
 * @param onValueChange Обработчик изменения
 * @param modifier Модификатор
 * @param placeholder Текст подсказки
 * @param label Лейбл
 * @param singleLine Однострочный ввод
 */
@Composable
fun GameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String = "",
    singleLine: Boolean = true,
    maxLength: Int = Int.MAX_VALUE
) {
    val colors = GameColors()
    
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= maxLength) onValueChange(it) },
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        placeholder = {
            if (placeholder.isNotEmpty()) {
                Text(
                    text = placeholder,
                    color = colors.onSurfaceVariant
                )
            }
        },
        label = {
            if (label.isNotEmpty()) {
                Text(text = label)
            }
        },
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.surfaceVariant,
            focusedTextColor = colors.onSurface,
            unfocusedTextColor = colors.onSurface,
            cursorColor = colors.primary
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Полоса ресурса (здоровье, энергия и т.д.).
 *
 * @param currentValue Текущее значение
 * @param maxValue Максимальное значение
 * @param modifier Модификатор
 * @param color Цвет полосы
 * @param showText Показывать ли текст
 * @param height Высота полосы
 */
@Composable
fun ResourceBar(
    currentValue: Float,
    maxValue: Float,
    modifier: Modifier = Modifier,
    color: Color = GameColors().health,
    showText: Boolean = true,
    height: Dp = 20.dp
) {
    val colors = GameColors()
    val progress = (currentValue / maxValue).coerceIn(0f, 1f)
    
    // Анимация изменения
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "resourceBarProgress"
    )
    
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.surfaceVariant)
        ) {
            // Прогресс
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(height)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(color, color.copy(alpha = 0.7f))
                        )
                    )
            )
            
            // Блик
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(RoundedCornerShape(10.dp))
                    .drawBehind {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            size = Size(size.width, size.height / 2)
                        )
                    }
            )
        }
        
        if (showText) {
            Text(
                text = "${currentValue.toInt()}/${maxValue.toInt()}",
                color = colors.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Отображение монет.
 *
 * @param coins Количество монет
 * @param modifier Модификатор
 * @param showLabel Показывать ли лейбл
 */
@Composable
fun CoinDisplay(
    coins: Int,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val colors = GameColors()
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Иконка монеты
        Canvas(
            modifier = Modifier.size(32.dp)
        ) {
            // Внешний круг
            drawCircle(
                color = colors.coin,
                radius = size.minDimension / 2
            )
            // Внутренний круг (блик)
            drawCircle(
                color = colors.coin.copy(alpha = 0.7f),
                radius = size.minDimension / 3
            )
            // Блеск
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = size.minDimension / 6,
                center = Offset(size.minDimension / 4, size.minDimension / 4)
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Количество
        Text(
            text = coins.toString(),
            color = colors.coin,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        if (showLabel) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "монет",
                color = colors.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Анимированный счётчик очков.
 *
 * @param score Текущий счёт
 * @param modifier Модификатор
 * @param label Лейбл
 */
@Composable
fun ScoreCounter(
    score: Int,
    modifier: Modifier = Modifier,
    label: String = "Счёт"
) {
    val colors = GameColors()
    
    // Анимация появления
    var animatedScore by remember { mutableStateOf(0) }
    
    LaunchedEffect(score) {
        val diff = score - animatedScore
        if (diff > 0) {
            val steps = 10
            val stepValue = diff / steps
            for (i in 1..steps) {
                animatedScore = (animatedScore + stepValue).coerceAtMost(score)
                delay(50)
            }
            animatedScore = score
        } else {
            animatedScore = score
        }
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = colors.onSurfaceVariant,
            fontSize = 12.sp
        )
        Text(
            text = animatedScore.toString(),
            color = colors.score,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Индикатор загрузки.
 *
 * @param modifier Модификатор
 * @param size Размер
 * @param color Цвет
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = GameColors().primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
                .rotate(rotation)
        ) {
            val strokeWidth = 4.dp.toPx()
            val radius = (size.toPx() - strokeWidth) / 2
            
            // Основной круг
            drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )
            
            // Градиентный конец
            drawArc(
                color = color.copy(alpha = 0.5f),
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = false,
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )
        }
    }
}

/**
 * Параллакс фон (декоративный).
 *
 * @param modifier Модификатор
 * @param speed Скорость движения
 * @param colors Цвета для градиента
 */
@Composable
fun ParallaxBackground(
    modifier: Modifier = Modifier,
    speed: Float = 0.5f,
    colors: List<Color> = listOf(
        GameColors().gradientStart,
        GameColors().gradientMiddle,
        GameColors().gradientEnd
    )
) {
    val infiniteTransition = rememberInfiniteTransition(label = "parallax")
    
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (10000 / speed).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(colors = colors)
            )
    ) {
        // Декоративные элементы
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            // Горизонтальные линии
            for (i in 0..10) {
                val y = (i * height / 10 + offset) % height
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Вертикальные линии
            for (i in 0..5) {
                val x = i * width / 5
                drawLine(
                    color = Color.White.copy(alpha = 0.03f),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}

/**
 * Комбо дисплей (множитель).
 *
 * @param combo Текущее комбо
 * @param multiplier Множитель
 * @param modifier Модификатор
 */
@Composable
fun ComboDisplay(
    combo: Int,
    multiplier: Float,
    modifier: Modifier = Modifier
) {
    val colors = GameColors()
    
    if (combo > 0) {
        // Анимация пульсации
        val infiniteTransition = rememberInfiniteTransition(label = "combo")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "comboScale"
        )
        
        Box(
            modifier = modifier
                .scale(scale)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "COMBO",
                    color = colors.combo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "x$combo",
                        color = colors.combo,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = " ${multiplier}x",
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
 * Прогресс бар дистанции.
 *
 * @param currentDistance Текущая дистанция
 * @param totalDistance Общая дистанция уровня
 * @param modifier Модификатор
 */
@Composable
fun DistanceProgressBar(
    currentDistance: Float,
    totalDistance: Float,
    modifier: Modifier = Modifier
) {
    val colors = GameColors()
    val progress = (currentDistance / totalDistance).coerceIn(0f, 1f)
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "distanceProgress"
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Дистанция",
            color = colors.onSurfaceVariant,
            fontSize = 12.sp
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.secondary)
            )
        }
        
        Text(
            text = "${(currentDistance / 100).toInt()}м / ${(totalDistance / 100).toInt()}м",
            color = colors.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

/**
 * Карточка товара для магазина.
 *
 * @param title Название
 * @param description Описание
 * @param price Цена
 * @param level Уровень (для улучшений)
 * @param isPurchased Куплено ли
 * @param isEquipped Экипировано ли
 * @param onPurchase Обработчик покупки
 * @param onEquip Обработчик экипировки
 * @param modifier Модификатор
 */
@Composable
fun ShopItemCard(
    title: String,
    description: String,
    price: Int,
    level: Int = 0,
    isPurchased: Boolean = false,
    isEquipped: Boolean = false,
    onPurchase: () -> Unit = {},
    onEquip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = GameColors()
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = colors.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (level > 0) {
                        Text(
                            text = "Уровень $level",
                            color = colors.secondary,
                            fontSize = 12.sp
                        )
                    }
                }
                
                if (isEquipped) {
                    Badge(
                        containerColor = colors.success
                    ) {
                        Text("Экипировано")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                color = colors.onSurfaceVariant,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Star,
                        contentDescription = null,
                        tint = colors.coin,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = price.toString(),
                        color = colors.coin,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (isPurchased) {
                    GameButton(
                        onClick = onEquip,
                        primary = !isEquipped,
                        text = if (isEquipped) "Выбрано" else "Выбрать",
                        enabled = !isEquipped
                    )
                } else {
                    GameButton(
                        onClick = onPurchase,
                        primary = true,
                        text = "Купить"
                    )
                }
            }
        }
    }
}

/**
 * Запись в таблице лидеров.
 *
 * @param rank Позиция
 * @param playerName Имя игрока
 * @param score Счёт
 * @param date Дата
 * @param isCurrentPlayer Это текущий игрок
 * @param modifier Модификатор
 */
@Composable
fun LeaderboardEntry(
    rank: Int,
    playerName: String,
    score: Int,
    date: String,
    isCurrentPlayer: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = GameColors()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isCurrentPlayer) colors.primary.copy(alpha = 0.2f) else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ранг
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            when (rank) {
                1 -> {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.EmojiEvents,
                        contentDescription = "1 место",
                        tint = Color(0xFFFFD700), // Золото
                        modifier = Modifier.size(32.dp)
                    )
                }
                2 -> {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.EmojiEvents,
                        contentDescription = "2 место",
                        tint = Color(0xFFC0C0C0), // Серебро
                        modifier = Modifier.size(32.dp)
                    )
                }
                3 -> {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.EmojiEvents,
                        contentDescription = "3 место",
                        tint = Color(0xFFCD7F32), // Бронза
                        modifier = Modifier.size(32.dp)
                    )
                }
                else -> {
                    Text(
                        text = rank.toString(),
                        color = colors.onSurfaceVariant,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Имя и счёт
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playerName,
                color = if (isCurrentPlayer) colors.primary else colors.onSurface,
                fontSize = 16.sp,
                fontWeight = if (isCurrentPlayer) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = date,
                color = colors.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        
        // Счёт
        Text(
            text = score.toString(),
            color = colors.score,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Слайдер для настроек.
 *
 * @param value Текущее значение
 * @param onValueChange Обработчик изменения
 * @param modifier Модификатор
 * @param valueRange Диапазон значений
 * @param label Лейбл
 */
@Composable
fun GameSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    label: String = ""
) {
    val colors = GameColors()
    
    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    color = colors.onSurface,
                    fontSize = 14.sp
                )
                Text(
                    text = "${(value * 100).toInt()}%",
                    color = colors.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = colors.primary,
                activeTrackColor = colors.primary,
                inactiveTrackColor = colors.surfaceVariant
            )
        )
    }
}

/**
 * Переключатель (toggle) для настроек.
 *
 * @param checked Состояние
 * @param onCheckedChange Обработчик изменения
 * @param modifier Модификатор
 * @param label Лейбл
 */
@Composable
fun GameToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String = ""
) {
    val colors = GameColors()
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = colors.onSurface,
            fontSize = 14.sp
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.primary,
                checkedTrackColor = colors.primary.copy(alpha = 0.5f),
                uncheckedThumbColor = colors.onSurfaceVariant,
                uncheckedTrackColor = colors.surfaceVariant
            )
        )
    }
}

/**
 * Бейдж "Новый рекорд".
 *
 * @param modifier Модификатор
 */
@Composable
fun NewRecordBadge(
    modifier: Modifier = Modifier
) {
    val colors = GameColors()
    val infiniteTransition = rememberInfiniteTransition(label = "badge")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badgeScale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(colors.tertiary, colors.primary)
                )
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🏆 НОВЫЙ РЕКОРД!",
            color = colors.onPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
