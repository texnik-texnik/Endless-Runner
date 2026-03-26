package com.endlessrunner.managers

/**
 * Менеджер прогресса игрока.
 */
class ProgressManager(
    private val saveManager: SaveManager
) {
    fun recordCoin(value: Int) {
        saveManager.addCoins(value)
    }

    fun recordScore(points: Int) {
        saveManager.setBestScore(points)
    }

    fun recordDistance(amount: Float) {
        // Заглушка
    }

    fun recordEnemyDefeated() {
        // Заглушка
    }

    fun recordGameCompleted() {
        // Заглушка
    }

    fun unlockSkin(skinId: String) {
        // Заглушка
    }

    fun setCurrentSkin(skinId: String) {
        saveManager.setCurrentSkin(skinId)
    }

    fun getTotalCoins() = saveManager.totalCoins
    fun getBestScore() = saveManager.bestScore
}
