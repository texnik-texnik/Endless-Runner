package com.endlessrunner.domain.model

/**
 * Сохранение текущего забега.
 * Используется для системы слотов сохранений.
 *
 * @property saveId Уникальный идентификатор сохранения
 * @property score Текущий счёт в забеге
 * @property coins Монеты, собранные в этом забеге
 * @property distance Пройденная дистанция в метрах
 * @property timestamp Время сохранения (timestamp)
 * @property isCompleted Флаг завершённости забега
 */
data class GameSave(
    val saveId: String,
    val score: Int = 0,
    val coins: Int = 0,
    val distance: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
) {
    /**
     * Время сохранения в читаемом формате.
     */
    val formattedTime: String
        get() {
            val date = java.util.Date(timestamp)
            val format = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
            return format.format(date)
        }

    /**
     * Дистанция в километрах.
     */
    val distanceKm: Float
        get() = distance / 1000f

    /**
     * Проверка, является ли сохранение активным (незавершённым).
     */
    val isActive: Boolean
        get() = !isCompleted
}
