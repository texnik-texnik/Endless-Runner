package com.endlessrunner.domain.model

/**
 * Достижение игрока.
 *
 * @property id Уникальный идентификатор достижения
 * @property title Заголовок достижения
 * @property description Описание достижения
 * @property iconResId ID ресурса иконки
 * @property isUnlocked Флаг разблокировки
 * @property unlockedAt Время разблокировки (timestamp) или null
 * @property progress Текущий прогресс
 * @property maxProgress Максимальный прогресс для разблокировки
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconResId: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val progress: Int = 0,
    val maxProgress: Int = 1
) {
    /**
     * Прогресс в процентах.
     */
    val progressPercent: Int
        get() = if (maxProgress > 0) (progress * 100 / maxProgress) else 0

    /**
     * Проверка, завершено ли достижение.
     */
    val isCompleted: Boolean
        get() = progress >= maxProgress

    /**
     * Форматированная строка прогресса.
     */
    val progressString: String
        get() = "$progress / $maxProgress"

    companion object {
        /**
         * Список всех достижений в игре.
         */
        val ALL_ACHIEVEMENTS = listOf(
            // Монеты
            Achievement(
                id = "FIRST_COIN",
                title = "Первая монета",
                description = "Соберите свою первую монету",
                iconResId = android.R.drawable.ic_menu_agenda,
                maxProgress = 1
            ),
            Achievement(
                id = "COIN_COLLECTOR",
                title = "Коллекционер",
                description = "Соберите 100 монет",
                iconResId = android.R.drawable.ic_menu_agenda,
                maxProgress = 100
            ),
            Achievement(
                id = "RICH",
                title = "Богач",
                description = "Соберите 1000 монет",
                iconResId = android.R.drawable.ic_menu_agenda,
                maxProgress = 1000
            ),

            // Игры
            Achievement(
                id = "FIRST_GAME",
                title = "Новичок",
                description = "Сыграйте свою первую игру",
                iconResId = android.R.drawable.ic_menu_play_clip,
                maxProgress = 1
            ),
            Achievement(
                id = "VETERAN",
                title = "Ветеран",
                description = "Сыграйте 100 игр",
                iconResId = android.R.drawable.ic_menu_play_clip,
                maxProgress = 100
            ),

            // Очки
            Achievement(
                id = "HIGH_SCORE",
                title = "Рекордсмен",
                description = "Наберите 1000 очков",
                iconResId = android.R.drawable.ic_menu_star,
                maxProgress = 1000
            ),
            Achievement(
                id = "LEGENDARY_SCORE",
                title = "Легенда",
                description = "Наберите 10000 очков",
                iconResId = android.R.drawable.ic_menu_star,
                maxProgress = 10000
            ),

            // Дистанция
            Achievement(
                id = "MARATHON",
                title = "Марафонец",
                description = "Пройдите 10000 метров",
                iconResId = android.R.drawable.ic_menu_mapmode,
                maxProgress = 10000
            ),
            Achievement(
                id = "ULTRA_MARATHON",
                title = "Ультра-марафон",
                description = "Пройдите 50000 метров",
                iconResId = android.R.drawable.ic_menu_mapmode,
                maxProgress = 50000
            ),

            // Враги
            Achievement(
                id = "ENEMY_SLAYER",
                title = "Убийца врагов",
                description = "Уничтожьте 100 врагов",
                iconResId = android.R.drawable.ic_menu_delete,
                maxProgress = 100
            ),
            Achievement(
                id = "ENEMY_HUNTER",
                title = "Охотник на врагов",
                description = "Уничтожьте 500 врагов",
                iconResId = android.R.drawable.ic_menu_delete,
                maxProgress = 500
            ),

            // Специальные
            Achievement(
                id = "PERFECT",
                title = "Идеальная игра",
                description = "Завершите игру без получения урона",
                iconResId = android.R.drawable.ic_menu_gallery,
                maxProgress = 1
            ),
            Achievement(
                id = "SPEEDRUNER",
                title = "Спидраннер",
                description = "Пройдите 1000 метров за 1 минуту",
                iconResId = android.R.drawable.ic_menu_recent_history,
                maxProgress = 1
            ),

            // Комбо
            Achievement(
                id = "COMBO_MASTER",
                title = "Мастер комбо",
                description = "Достигните комбо x5",
                iconResId = android.R.drawable.ic_menu_share,
                maxProgress = 50
            )
        )

        /**
         * Получение достижения по ID.
         */
        fun getById(id: String): Achievement? = ALL_ACHIEVEMENTS.find { it.id == id }
    }
}
