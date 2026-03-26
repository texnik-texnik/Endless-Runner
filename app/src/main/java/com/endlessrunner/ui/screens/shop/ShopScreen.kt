package com.endlessrunner.ui.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
 * Экран магазина.
 *
 * @param onBackClick Callback для возврата назад
 * @param viewModel ViewModel (для тестов можно передать mock)
 */
@Composable
fun ShopScreen(
    onBackClick: () -> Unit,
    viewModel: ShopViewModel = viewModel()
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
            ShopHeader(
                playerCurrency = state.playerCurrency,
                onBackClick = onBackClick
            )
            
            // Вкладки категорий
            ShopCategoryTabs(
                selectedCategory = state.selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )
            
            // Контент вкладки
            ShopContent(
                state = state,
                viewModel = viewModel
            )
        }
        
        // Уведомление об успешной покупке
        if (state.showPurchaseSuccess) {
            PurchaseSuccessNotification(
                onDismiss = { viewModel.clearPurchaseSuccess() }
            )
        }
        
        // Уведомление об ошибке покупки
        if (state.showPurchaseError) {
            PurchaseErrorNotification(
                onDismiss = { viewModel.clearPurchaseError() }
            )
        }
    }
}

/**
 * Верхняя панель магазина.
 */
@Composable
private fun ShopHeader(
    playerCurrency: Int,
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
            text = "МАГАЗИН",
            color = colors.onSurface,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
        
        // Баланс
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка монеты
            androidx.compose.foundation.Canvas(
                modifier = Modifier.size(28.dp)
            ) {
                drawCircle(
                    color = colors.coin,
                    radius = size.minDimension / 2
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = playerCurrency.toString(),
                color = colors.coin,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Вкладки категорий.
 */
@Composable
private fun ShopCategoryTabs(
    selectedCategory: ShopCategory,
    onCategorySelected: (ShopCategory) -> Unit
) {
    val colors = GameColors()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceVariant),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ShopCategory.entries.forEach { category ->
            val isSelected = selectedCategory == category
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onCategorySelected(category) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (category) {
                            ShopCategory.UPGRADES -> "⬆️"
                            ShopCategory.SKINS -> "🎨"
                            ShopCategory.POWERUPS -> "⚡"
                        },
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (category) {
                            ShopCategory.UPGRADES -> "Улучшения"
                            ShopCategory.SKINS -> "Скины"
                            ShopCategory.POWERUPS -> "Бонусы"
                        },
                        color = if (isSelected) colors.primary else colors.onSurfaceVariant,
                        fontSize = 12.sp,
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
 * Контент магазина в зависимости от категории.
 */
@Composable
private fun ShopContent(
    state: ShopState,
    viewModel: ShopViewModel
) {
    when (state.selectedCategory) {
        ShopCategory.UPGRADES -> UpgradesList(
            upgrades = state.upgrades,
            playerCurrency = state.playerCurrency,
            onPurchase = { viewModel.purchaseUpgrade(it) }
        )
        ShopCategory.SKINS -> SkinsList(
            skins = state.skins,
            playerCurrency = state.playerCurrency,
            onPurchase = { viewModel.purchaseSkin(it) },
            onEquip = { viewModel.equipSkin(it) }
        )
        ShopCategory.POWERUPS -> PowerUpsList(
            powerUps = state.powerUps,
            playerCurrency = state.playerCurrency,
            onPurchase = { viewModel.purchasePowerUp(it) },
            onUse = { viewModel.usePowerUp(it) }
        )
    }
}

/**
 * Список улучшений.
 */
@Composable
private fun UpgradesList(
    upgrades: List<UpgradeItem>,
    playerCurrency: Int,
    onPurchase: (UpgradeItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(upgrades) { upgrade ->
            UpgradeItemCard(
                upgrade = upgrade,
                playerCurrency = playerCurrency,
                onPurchase = { onPurchase(upgrade) }
            )
        }
    }
}

/**
 * Карточка улучшения.
 */
@Composable
private fun UpgradeItemCard(
    upgrade: UpgradeItem,
    playerCurrency: Int,
    onPurchase: () -> Unit
) {
    val colors = GameColors()
    val canAfford = playerCurrency >= upgrade.getNextLevelPrice()
    val isMaxLevel = !upgrade.canUpgrade()
    
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Text(
                        text = upgrade.icon,
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = upgrade.title,
                            color = colors.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Уровень ${upgrade.level}/${upgrade.maxLevel}",
                            color = colors.primary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = upgrade.description,
                color = colors.onSurfaceVariant,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Прогресс бар уровня
            LinearProgressIndicator(
                progress = upgrade.getProgressPercent(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = colors.primary,
                trackColor = colors.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💰",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isMaxLevel) "MAX" else upgrade.getNextLevelPrice().toString(),
                        color = if (canAfford || isMaxLevel) colors.coin else colors.error,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                GameButton(
                    onClick = onPurchase,
                    primary = true,
                    text = if (isMaxLevel) "МАКС" else "УЛУЧШИТЬ",
                    enabled = canAfford && !isMaxLevel
                )
            }
        }
    }
}

/**
 * Список скинов.
 */
@Composable
private fun SkinsList(
    skins: List<SkinItem>,
    playerCurrency: Int,
    onPurchase: (SkinItem) -> Unit,
    onEquip: (SkinItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(skins) { skin ->
            SkinItemCard(
                skin = skin,
                playerCurrency = playerCurrency,
                onPurchase = { onPurchase(skin) },
                onEquip = { onEquip(skin) }
            )
        }
    }
}

/**
 * Карточка скина.
 */
@Composable
private fun SkinItemCard(
    skin: SkinItem,
    playerCurrency: Int,
    onPurchase: () -> Unit,
    onEquip: () -> Unit
) {
    val colors = GameColors()
    val canAfford = playerCurrency >= skin.price
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
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
                Text(
                    text = skin.icon,
                    fontSize = 40.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = skin.title,
                            color = colors.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Бейдж редкости
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(skin.rarity.color.copy(alpha = 0.3f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = skin.rarity.name,
                                color = skin.rarity.color,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = skin.description,
                        color = colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (skin.isPurchased) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💰",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = skin.price.toString(),
                        color = colors.coin,
                        fontSize = 16.sp
                    )
                }
                
                GameButton(
                    onClick = onEquip,
                    primary = !skin.isEquipped,
                    text = if (skin.isEquipped) "ВЫБРАНО" else "ВЫБРАТЬ",
                    enabled = !skin.isEquipped
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💰",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = skin.price.toString(),
                        color = if (canAfford) colors.coin else colors.error,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                GameButton(
                    onClick = onPurchase,
                    primary = true,
                    text = "КУПИТЬ",
                    enabled = canAfford
                )
            }
        }
    }
}

/**
 * Список бонусов.
 */
@Composable
private fun PowerUpsList(
    powerUps: List<PowerUpItem>,
    playerCurrency: Int,
    onPurchase: (PowerUpItem) -> Unit,
    onUse: (PowerUpItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(powerUps) { powerUp ->
            PowerUpItemCard(
                powerUp = powerUp,
                playerCurrency = playerCurrency,
                onPurchase = { onPurchase(powerUp) },
                onUse = { onUse(powerUp) }
            )
        }
    }
}

/**
 * Карточка бонуса.
 */
@Composable
private fun PowerUpItemCard(
    powerUp: PowerUpItem,
    playerCurrency: Int,
    onPurchase: () -> Unit,
    onUse: () -> Unit
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Text(
                        text = powerUp.icon,
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = powerUp.title,
                            color = colors.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Длительность: ${powerUp.duration}с",
                            color = colors.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
                
                // Количество
                if (powerUp.quantity > 0) {
                    Badge(
                        containerColor = colors.primary
                    ) {
                        Text(
                            text = "x${powerUp.quantity}",
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = powerUp.description,
                color = colors.onSurfaceVariant,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💰",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = powerUp.price.toString(),
                        color = colors.coin,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (powerUp.quantity > 0) {
                        GameButton(
                            onClick = onUse,
                            primary = false,
                            text = "ИСПОЛЬЗОВАТЬ"
                        )
                    }
                    GameButton(
                        onClick = onPurchase,
                        primary = true,
                        text = "КУПИТЬ"
                    )
                }
            }
        }
    }
}

/**
 * Уведомление об успешной покупке.
 */
@Composable
private fun PurchaseSuccessNotification(
    onDismiss: () -> Unit
) {
    val colors = GameColors()
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        onDismiss()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = colors.success
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colors.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Покупка успешна!",
                    color = colors.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Уведомление об ошибке покупки.
 */
@Composable
private fun PurchaseErrorNotification(
    onDismiss: () -> Unit
) {
    val colors = GameColors()
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        onDismiss()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = colors.error
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = colors.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Недостаточно монет!",
                    color = colors.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
