package com.endlessrunner.domain.model

/**
 * Запись в таблице лидеров.
 *
 * @property entryId Уникальный идентификатор записи
 * @property playerName Имя игрока
 * @property score Набранные очки
 * @property coins Собранные монеты в этом забеге
 * @property distance Пройденная дистанция в метрах
 * @property timestamp Время записи (timestamp)
 * @property rank Позиция в таблице лидеров
 */
data class LeaderboardEntry(
    val entryId: String,
    val playerName: String,
    val score: Int,
    val coins: Int = 0,
    val distance: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val rank: Int = 0
) {
    /**
     * Время записи в читаемом формате.
     */
    val formattedTime: String
        get() {
            val date = java.util.Date(timestamp)
            val format = java.text.SimpleDateFormat("dd.MM HH:mm", java.util.Locale.getDefault())
            return format.format(date)
        }

    /**
     * Дистанция в километрах.
     */
    val distanceKm: Float
        get() = distance / 1000f

    /**
     * Очки за монеты.
     */
    val coinsBonus: Int
        get() = coins * 10

    /**
     * Общий счёт с бонусами.
     */
    val totalScore: Int
        get() = score + coinsBonus

    /**
     * Ранг в виде строки с суффиксом.
     */
    val rankString: String
        get() = when (rank) {
            1 -> "${rank}st"
            2 -> "${rank}nd"
            3 -> "${rank}rd"
            else -> "${rank}th"
        }

    companion object {
        /**
         * Генерация уникального ID для записи.
         */
        fun generateId(): String = "entry_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
}
