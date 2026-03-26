package com.endlessrunner.ui.screens.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.endlessrunner.data.local.datastore.PlayerPreferencesDataStore
import com.endlessrunner.managers.ProgressManager
import com.endlessrunner.managers.SaveManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Состояние магазина.
 */
data class ShopState(
    val playerCurrency: Int = 0,
    val selectedCategory: ShopCategory = ShopCategory.UPGRADES,
    val upgrades: List<UpgradeItem> = DefaultUpgrades.ALL,
    val skins: List<SkinItem> = DefaultSkins.ALL,
    val powerUps: List<PowerUpItem> = DefaultPowerUps.ALL,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showPurchaseSuccess: Boolean = false,
    val showPurchaseError: Boolean = false,
    val currentSkin: String = "skin_default",
    val unlockedSkins: Set<String> = emptySet()
)

/**
 * ViewModel для магазина.
 * Управляет покупками и экипировкой предметов.
 * Интегрирована с ProgressManager и PlayerPreferencesDataStore.
 */
class ShopViewModel(
    private val saveManager: SaveManager,
    private val progressManager: ProgressManager,
    private val playerPreferencesDataStore: PlayerPreferencesDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(ShopState())
    val state: StateFlow<ShopState> = _state.asStateFlow()

    init {
        loadShopData()
        observePlayerProgress()
    }

    /**
     * Наблюдение за прогрессом игрока.
     */
    private fun observePlayerProgress() {
        viewModelScope.launch {
            progressManager.getPlayerProgress().collect { progress ->
                progress?.let {
                    _state.value = _state.value.copy(
                        playerCurrency = it.totalCoins,
                        unlockedSkins = it.unlockedSkins,
                        currentSkin = it.currentSkin
                    )
                }
            }
        }
    }

    /**
     * Загрузка данных магазина.
     */
    private fun loadShopData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                // Загрузка прогресса из ProgressManager
                val progress = progressManager.getCurrentProgress()
                val totalCoins = progress?.totalCoins ?: 0
                val unlockedSkins = progress?.unlockedSkins ?: emptySet()
                val currentSkin = progress?.currentSkin ?: "skin_default"

                // Загрузка улучшений с сохранёнными уровнями
                val loadedUpgrades = DefaultUpgrades.ALL.map { default ->
                    default.copy(level = 0) // Уровни пока не сохраняются в новой системе
                }

                // Загрузка скинов
                val loadedSkins = DefaultSkins.ALL.map { default ->
                    val isPurchased = unlockedSkins.contains(default.id) || default.id == "skin_default"
                    val isEquipped = currentSkin == default.id
                    default.copy(
                        isPurchased = isPurchased,
                        isEquipped = isEquipped
                    )
                }

                // Загрузка бонусов
                val loadedPowerUps = DefaultPowerUps.ALL.map { default ->
                    default.copy(quantity = 0) // Бонусы пока не сохраняются в новой системе
                }

                _state.value = _state.value.copy(
                    playerCurrency = totalCoins,
                    upgrades = loadedUpgrades,
                    skins = loadedSkins,
                    powerUps = loadedPowerUps,
                    unlockedSkins = unlockedSkins,
                    currentSkin = currentSkin,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Ошибка загрузки: ${e.message}"
                )
            }
        }
    }

    /**
     * Выбор категории.
     */
    fun selectCategory(category: ShopCategory) {
        _state.value = _state.value.copy(selectedCategory = category)
    }

    /**
     * Покупка улучшения.
     */
    fun purchaseUpgrade(upgrade: UpgradeItem) {
        if (!upgrade.canUpgrade()) return

        val price = upgrade.getNextLevelPrice()
        if (_state.value.playerCurrency < price) {
            _state.value = _state.value.copy(showPurchaseError = true)
            return
        }

        viewModelScope.launch {
            try {
                val newLevel = upgrade.level + 1

                // Обновление состояния (уровни пока не сохраняются в БД)
                _state.value = _state.value.copy(
                    playerCurrency = _state.value.playerCurrency - price,
                    upgrades = _state.value.upgrades.map {
                        if (it.id == upgrade.id) it.copy(level = newLevel) else it
                    },
                    showPurchaseSuccess = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(showPurchaseError = true)
            }
        }
    }

    /**
     * Покупка скина.
     */
    fun purchaseSkin(skin: SkinItem) {
        if (skin.isPurchased) return
        if (_state.value.playerCurrency < skin.price) {
            _state.value = _state.value.copy(showPurchaseError = true)
            return
        }

        viewModelScope.launch {
            try {
                // Разблокировка скина через ProgressManager
                progressManager.unlockSkin(skin.id)

                // Обновление состояния
                _state.value = _state.value.copy(
                    playerCurrency = _state.value.playerCurrency - skin.price,
                    skins = _state.value.skins.map {
                        if (it.id == skin.id) it.copy(isPurchased = true) else it
                    },
                    unlockedSkins = _state.value.unlockedSkins + skin.id,
                    showPurchaseSuccess = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(showPurchaseError = true)
            }
        }
    }

    /**
     * Экипировка скина.
     */
    fun equipSkin(skin: SkinItem) {
        if (!skin.isPurchased) return

        viewModelScope.launch {
            try {
                // Установка скина через ProgressManager
                val success = progressManager.setCurrentSkin(skin.id)

                if (success) {
                    // Обновление состояния
                    _state.value = _state.value.copy(
                        skins = _state.value.skins.map {
                            it.copy(isEquipped = it.id == skin.id)
                        },
                        currentSkin = skin.id
                    )
                }
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }

    /**
     * Покупка бонуса.
     */
    fun purchasePowerUp(powerUp: PowerUpItem) {
        viewModelScope.launch {
            try {
                // Обновление состояния (бонусы пока не сохраняются в БД)
                _state.value = _state.value.copy(
                    playerCurrency = _state.value.playerCurrency - powerUp.price,
                    powerUps = _state.value.powerUps.map {
                        if (it.id == powerUp.id) it.copy(quantity = it.quantity + 1) else it
                    },
                    showPurchaseSuccess = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(showPurchaseError = true)
            }
        }
    }

    /**
     * Использование бонуса.
     */
    fun usePowerUp(powerUp: PowerUpItem) {
        if (powerUp.quantity <= 0) return

        viewModelScope.launch {
            try {
                // Обновление состояния
                _state.value = _state.value.copy(
                    powerUps = _state.value.powerUps.map {
                        if (it.id == powerUp.id) it.copy(quantity = it.quantity - 1) else it
                    }
                )

                // TODO: Активация бонуса в игре
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }

    /**
     * Получение эффекта улучшения.
     */
    fun getUpgradeEffect(upgradeType: UpgradeItem.UpgradeType, level: Int): Float {
        return when (upgradeType) {
            UpgradeItem.UpgradeType.SPEED -> 0.1f * level
            UpgradeItem.UpgradeType.JUMP -> 0.05f * level
            UpgradeItem.UpgradeType.MAGNET -> 0.2f * level
            UpgradeItem.UpgradeType.COIN_MULTIPLIER -> 0.1f * level
            UpgradeItem.UpgradeType.HEALTH -> 10f * level
            UpgradeItem.UpgradeType.SHIELD -> 5f * level
            UpgradeItem.UpgradeType.DOUBLE_JUMP -> if (level >= 1) 1f else 0f
        }
    }

    /**
     * Сброс флага успеха.
     */
    fun clearPurchaseSuccess() {
        _state.value = _state.value.copy(showPurchaseSuccess = false)
    }

    /**
     * Сброс флага ошибки.
     */
    fun clearPurchaseError() {
        _state.value = _state.value.copy(showPurchaseError = false)
    }

    /**
     * Обновление данных.
     */
    fun refresh() {
        loadShopData()
    }
}
