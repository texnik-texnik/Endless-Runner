package com.endlessrunner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Цветовая палитра игры.
 * Основные цвета для тёмной темы (игра использует тёмную тему по умолчанию).
 */
@Immutable
data class GameColors(
    // Основные цвета
    val primary: Color = Color(0xFFFF6B35),        // Оранжевый - основной акцент
    val secondary: Color = Color(0xFF00D9FF),      // Голубой - вторичный акцент
    val tertiary: Color = Color(0xFFFFD93D),       // Жёлтый - для монет и бонусов
    
    // Фоновые цвета
    val background: Color = Color(0xFF0D0D0D),     // Очень тёмный фон
    val surface: Color = Color(0xFF1A1A1A),        // Тёмная поверхность
    val surfaceVariant: Color = Color(0xFF2A2A2A), // Вариант поверхности
    
    // Цвета состояния
    val success: Color = Color(0xFF4CAF50),        // Зелёный - успех
    val error: Color = Color(0xFFFF5252),          // Красный - ошибка/урон
    val warning: Color = Color(0xFFFFC107),        // Янтарный - предупреждение
    val info: Color = Color(0xFF2196F3),           // Синий - информация
    
    // Цвета текста
    val onPrimary: Color = Color(0xFFFFFFFF),
    val onSecondary: Color = Color(0xFF000000),
    val onBackground: Color = Color(0xFFFFFFFF),
    val onSurface: Color = Color(0xFFFFFFFF),
    val onSurfaceVariant: Color = Color(0xFFB0B0B0),
    
    // Специальные цвета
    val health: Color = Color(0xFFE91E63),         // Розовый - здоровье
    val energy: Color = Color(0xFF9C27B0),         // Фиолетовый - энергия
    val coin: Color = Color(0xFFFFD700),           // Золотой - монеты
    val score: Color = Color(0xFF00E676),          // Ярко-зелёный - счёт
    val combo: Color = Color(0xFFFF4081),          // Розовый - комбо
    
    // Градиенты (для фонов)
    val gradientStart: Color = Color(0xFF1A237E),  // Тёмно-синий
    val gradientMiddle: Color = Color(0xFF311B92), // Фиолетовый
    val gradientEnd: Color = Color(0xFF4A148C)     // Тёмно-фиолетовый
)

/**
 * Цвета для светлой темы.
 */
val GameColorsLight = GameColors(
    primary = Color(0xFFE65100),
    secondary = Color(0xFF00B8D4),
    tertiary = Color(0xFFFFC107),
    background = Color(0xFFF5F5F5),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF0F0F0),
    onBackground = Color(0xFF000000),
    onSurface = Color(0xFF000000),
    onSurfaceVariant = Color(0xFF666666)
)

/**
 * Тёмная цветовая схема Material 3.
 */
private val DarkColorScheme = darkColorScheme(
    primary = GameColors().primary,
    secondary = GameColors().secondary,
    tertiary = GameColors().tertiary,
    background = GameColors().background,
    surface = GameColors().surface,
    surfaceVariant = GameColors().surfaceVariant,
    onPrimary = GameColors().onPrimary,
    onSecondary = GameColors().onSecondary,
    onBackground = GameColors().onBackground,
    onSurface = GameColors().onSurface,
    onSurfaceVariant = GameColors().onSurfaceVariant,
    error = GameColors().error
)

/**
 * Светлая цветовая схема Material 3.
 */
private val LightColorScheme = lightColorScheme(
    primary = GameColorsLight.primary,
    secondary = GameColorsLight.secondary,
    tertiary = GameColorsLight.tertiary,
    background = GameColorsLight.background,
    surface = GameColorsLight.surface,
    surfaceVariant = GameColorsLight.surfaceVariant,
    onPrimary = GameColorsLight.onPrimary,
    onSecondary = GameColorsLight.onSecondary,
    onBackground = GameColorsLight.onBackground,
    onSurface = GameColorsLight.onSurface,
    onSurfaceVariant = GameColorsLight.onSurfaceVariant,
    error = GameColorsLight.error
)

/**
 * Типографика игры.
 * Использует системные шрифты с разными начертаниями.
 */
val GameTypography = androidx.compose.material3.Typography(
    // Заголовки
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    
    // Заголовки экранов
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    
    // Заголовки секций
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    
    // Основной текст
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    
    // Кнопки и лейблы
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * Формы компонентов.
 */
val GameShapes = androidx.compose.material3.Shapes(
    extraSmall = RectangleShape,
    small = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
)

/**
 * Основная тема игры.
 *
 * @param darkTheme Использовать тёмную тему
 * @param dynamicColor Использовать динамические цвета (Material You)
 * @param content Контент приложения
 */
@Composable
fun GameTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GameTypography,
        shapes = GameShapes,
        content = content
    )
}

/**
 * Extension property для доступа к цветам игры.
 */
val GameColors.current: GameColors
    @Composable
    get() = GameColors()
