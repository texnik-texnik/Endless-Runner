package com.endlessrunner.ui.screens.settings

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
 * Экран настроек.
 *
 * @param onBackClick Callback для возврата назад
 * @param onResetClick Callback для сброса настроек
 * @param viewModel ViewModel (для тестов можно передать mock)
 */
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onResetClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
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
            SettingsHeader(
                hasUnsavedChanges = state.hasUnsavedChanges,
                onBackClick = onBackClick,
                onSaveClick = { viewModel.saveSettings() }
            )
            
            // Контент настроек
            SettingsContent(
                state = state,
                viewModel = viewModel
            )
        }
        
        // Кнопки действий внизу
        if (state.hasUnsavedChanges) {
            UnsavedChangesBar(
                onDiscardClick = { viewModel.discardChanges() },
                onSaveClick = { viewModel.saveSettings() }
            )
        }
    }
}

/**
 * Верхняя панель настроек.
 */
@Composable
private fun SettingsHeader(
    hasUnsavedChanges: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
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
            text = "НАСТРОЙКИ",
            color = colors.onSurface,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
        
        // Кнопка сохранения
        IconButton(
            onClick = onSaveClick,
            enabled = hasUnsavedChanges
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Сохранить",
                tint = if (hasUnsavedChanges) colors.primary else colors.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * Контент настроек.
 */
@Composable
private fun SettingsContent(
    state: SettingsState,
    viewModel: SettingsViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Настройки звука
        SoundSettingsSection(
            sound = state.settings.sound,
            onSoundSettingsChanged = { viewModel.updateSoundSettings(it) }
        )
        
        // Настройки графики
        GraphicsSettingsSection(
            graphics = state.settings.graphics,
            onGraphicsSettingsChanged = { viewModel.updateGraphicsSettings(it) }
        )
        
        // Настройки геймплея
        GameplaySettingsSection(
            gameplay = state.settings.gameplay,
            onGameplaySettingsChanged = { viewModel.updateGameplaySettings(it) }
        )
        
        // Кнопка сброса
        ResetButton(
            onClick = { viewModel.resetToDefault() }
        )
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * Секция настроек звука.
 */
@Composable
private fun SoundSettingsSection(
    sound: SoundSettings,
    onSoundSettingsChanged: (SoundSettings) -> Unit
) {
    val colors = GameColors()
    
    SettingsCard(
        title = "Звук",
        icon = Icons.Default.VolumeUp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Общая громкость
            GameSlider(
                value = sound.masterVolume,
                onValueChange = {
                    onSoundSettingsChanged(sound.copy(masterVolume = it))
                },
                label = "Общая громкость",
                valueRange = 0f..1f
            )
            
            // Громкость музыки
            GameSlider(
                value = sound.musicVolume,
                onValueChange = {
                    onSoundSettingsChanged(sound.copy(musicVolume = it))
                },
                label = "Музыка",
                valueRange = 0f..1f
            )
            
            // Громкость эффектов
            GameSlider(
                value = sound.sfxVolume,
                onValueChange = {
                    onSoundSettingsChanged(sound.copy(sfxVolume = it))
                },
                label = "Звуковые эффекты",
                valueRange = 0f..1f
            )
            
            // Отключение звука
            GameToggle(
                checked = sound.isMuted,
                onCheckedChange = {
                    onSoundSettingsChanged(sound.copy(isMuted = it))
                },
                label = "Отключить звук"
            )
        }
    }
}

/**
 * Секция настроек графики.
 */
@Composable
private fun GraphicsSettingsSection(
    graphics: GraphicsSettings,
    onGraphicsSettingsChanged: (GraphicsSettings) -> Unit
) {
    val colors = GameColors()
    
    SettingsCard(
        title = "Графика",
        icon = Icons.Default.GraphicEq
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Качество графики
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Качество",
                    color = colors.onSurface,
                    fontSize = 14.sp
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GraphicsQuality.entries.forEach { quality ->
                        QualityButton(
                            quality = quality,
                            isSelected = graphics.quality == quality,
                            onClick = {
                                onGraphicsSettingsChanged(graphics.copy(quality = quality))
                            }
                        )
                    }
                }
            }
            
            // Показать FPS
            GameToggle(
                checked = graphics.showFps,
                onCheckedChange = {
                    onGraphicsSettingsChanged(graphics.copy(showFps = it))
                },
                label = "Показать FPS"
            )
            
            // Эффекты частиц
            GameToggle(
                checked = graphics.particleEffects,
                onCheckedChange = {
                    onGraphicsSettingsChanged(graphics.copy(particleEffects = it))
                },
                label = "Эффекты частиц"
            )
            
            // Тени
            GameToggle(
                checked = graphics.shadows,
                onCheckedChange = {
                    onGraphicsSettingsChanged(graphics.copy(shadows = it))
                },
                label = "Тени"
            )
        }
    }
}

/**
 * Кнопка выбора качества.
 */
@Composable
private fun QualityButton(
    quality: GraphicsQuality,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = GameColors()
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) colors.primary else colors.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (quality) {
                GraphicsQuality.LOW -> "LOW"
                GraphicsQuality.MEDIUM -> "MED"
                GraphicsQuality.HIGH -> "HIGH"
                GraphicsQuality.ULTRA -> "ULTRA"
            },
            color = if (isSelected) colors.onPrimary else colors.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Секция настроек геймплея.
 */
@Composable
private fun GameplaySettingsSection(
    gameplay: GameplaySettings,
    onGameplaySettingsChanged: (GameplaySettings) -> Unit
) {
    val colors = GameColors()
    
    SettingsCard(
        title = "Геймплей",
        icon = Icons.Default.SportsEsports
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ориентация экрана
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ориентация",
                    color = colors.onSurface,
                    fontSize = 14.sp
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScreenOrientation.entries.forEach { orientation ->
                        OrientationButton(
                            orientation = orientation,
                            isSelected = gameplay.orientation == orientation,
                            onClick = {
                                onGameplaySettingsChanged(gameplay.copy(orientation = orientation))
                            }
                        )
                    }
                }
            }
            
            // Чувствительность касания
            GameSlider(
                value = gameplay.touchSensitivity,
                onValueChange = {
                    onGameplaySettingsChanged(gameplay.copy(touchSensitivity = it))
                },
                label = "Чувствительность",
                valueRange = 0.1f..1f
            )
            
            // Вибрация
            GameToggle(
                checked = gameplay.vibrationEnabled,
                onCheckedChange = {
                    onGameplaySettingsChanged(gameplay.copy(vibrationEnabled = it))
                },
                label = "Вибрация"
            )
            
            // Показывать обучение
            GameToggle(
                checked = gameplay.showTutorial,
                onCheckedChange = {
                    onGameplaySettingsChanged(gameplay.copy(showTutorial = it))
                },
                label = "Обучение"
            )
            
            // Автопрыжок
            GameToggle(
                checked = gameplay.autoJump,
                onCheckedChange = {
                    onGameplaySettingsChanged(gameplay.copy(autoJump = it))
                },
                label = "Автопрыжок"
            )
        }
    }
}

/**
 * Кнопка выбора ориентации.
 */
@Composable
private fun OrientationButton(
    orientation: ScreenOrientation,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = GameColors()
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) colors.primary else colors.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (orientation) {
                ScreenOrientation.PORTRAIT -> "📱"
                ScreenOrientation.LANDSCAPE -> "📱"
                ScreenOrientation.AUTO -> "🔄"
            },
            fontSize = 16.sp
        )
    }
}

/**
 * Карточка настроек.
 */
@Composable
private fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    color = colors.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            content()
        }
    }
}

/**
 * Кнопка сброса настроек.
 */
@Composable
private fun ResetButton(
    onClick: () -> Unit
) {
    val colors = GameColors()
    
    GameButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        primary = false,
        text = "СБРОСИТЬ К НАСТРОЙКАМ ПО УМОЛЧАНИЮ",
        icon = Icons.Default.Refresh
    )
}

/**
 * Панель несохранённых изменений.
 */
@Composable
private fun UnsavedChangesBar(
    onDiscardClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val colors = GameColors()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .background(colors.surfaceVariant)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Есть несохранённые изменения",
                color = colors.warning,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onDiscardClick) {
                    Text("Отменить")
                }
                GameButton(
                    onClick = onSaveClick,
                    primary = true,
                    text = "Сохранить"
                )
            }
        }
    }
}
