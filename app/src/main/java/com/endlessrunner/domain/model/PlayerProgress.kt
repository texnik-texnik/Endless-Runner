package com.endlessrunner.domain.model

/**
 * Domain модель прогресса игрока.
 * Содержит всю статистику и достижения игрока.
 *
 * @property playerId Уникальный идентификатор игрока
 * @property totalCoins Общее количество собранных монет
 * @property bestScore Лучший результат (рекорд)
 * @property totalDistance Общая пройденная дистанция в метрах
 * @property totalGamesPlayed Всего сыграно игр
 * @property totalGamesWon Всего выиграно игр
 * @property enemiesDefeated Всего уничтожено врагов
 * @property coinsCollected Всего собрано монет (за все игры)
 * @property playTimeSeconds Общее время игры в секундах
 * @property currentSkin ID текущего скина
 * @property unlockedSkins Набор разблокированных скинов
 * @property createdAt Время создания прогресса (timestamp)
 * @property lastPlayedAt Время последней игры (timestamp)
 */
data class PlayerProgress(
    val playerId: String,
    val totalCoins: Int = 0,
    val bestScore: Int = 0,
    val totalDistance: Float = 0f,
    val totalGamesPlayed: Int = 0,
    val totalGamesWon: Int = 0,
    val enemiesDefeated: Int = 0,
    val coinsCollected: Int = 0,
    val playTimeSeconds: Long = 0L,
    val currentSkin: String = "skin_default",
    val unlockedSkins: Set<String> = emptySet(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastPlayedAt: Long = System.currentTimeMillis()
) {
    /**
     * Проверка, разблокирован ли скин.
     */
    fun isSkinUnlocked(skinId: String): Boolean = unlockedSkins.contains(skinId)

    /**
     * Общее количество игр.
     */
    val winRate: Float
        get() = if (totalGamesPlayed > 0) {
            totalGamesWon.toFloat() / totalGamesPlayed * 100f
        } else 0f

    /**
     * Среднее количество монет за игру.
     */
    val averageCoinsPerGame: Float
        get() = if (totalGamesPlayed > 0) {
            coinsCollected.toFloat() / totalGamesPlayed
        } else 0f

    /**
     * Время игры в формате ЧЧ:ММ:СС.
     */
    val playTimeFormatted: String
        get() {
            val hours = playTimeSeconds / 3600
            val minutes = (playTimeSeconds % 3600) / 60
            val seconds = playTimeSeconds % 60
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
}
