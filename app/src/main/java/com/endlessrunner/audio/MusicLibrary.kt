package com.endlessrunner.audio

/**
 * Библиотека музыкальных треков.
 *
 * Содержит пресеты музыкальных треков для различных игровых ситуаций.
 * Все пути указаны относительно папки assets/music/.
 */
object MusicLibrary {

    /**
     * Главная тема меню.
     * Спокойная, ненавязчивая музыка для главного меню.
     */
    val MAIN_THEME = MusicTrack(
        id = "main_theme",
        name = "Main Theme",
        assetPath = "music/main_theme.ogg",
        duration = 180_000L, // 3 минуты
        isLooping = true,
        volume = 0.7f
    )

    /**
     * Основная музыка геймплея.
     * Динамичная музыка для обычного игрового процесса.
     */
    val GAMEPLAY_1 = MusicTrack(
        id = "gameplay_1",
        name = "Gameplay Theme 1",
        assetPath = "music/gameplay_1.ogg",
        duration = 150_000L,
        isLooping = true,
        volume = 0.8f
    )

    /**
     * Интенсивная музыка геймплея.
     * Более энергичная версия для сложных участков.
     */
    val GAMEPLAY_2 = MusicTrack(
        id = "gameplay_2",
        name = "Gameplay Theme 2",
        assetPath = "music/gameplay_2.ogg",
        duration = 140_000L,
        isLooping = true,
        volume = 0.85f
    )

    /**
     * Музыка битвы с боссом.
     * Напряженная, эпичная музыка.
     */
    val BOSS_BATTLE = MusicTrack(
        id = "boss_battle",
        name = "Boss Battle",
        assetPath = "music/boss_battle.ogg",
        duration = 200_000L,
        isLooping = true,
        volume = 0.9f
    )

    /**
     * Музыка проигрыша.
     * Меланхоличная, спокойная композиция.
     */
    val GAME_OVER = MusicTrack(
        id = "game_over",
        name = "Game Over",
        assetPath = "music/game_over.ogg",
        duration = 60_000L,
        isLooping = false,
        volume = 0.6f
    )

    /**
     * Музыка победы.
     * Торжественная, победная мелодия.
     */
    val VICTORY = MusicTrack(
        id = "victory",
        name = "Victory",
        assetPath = "music/victory.ogg",
        duration = 45_000L,
        isLooping = false,
        volume = 0.8f
    )

    /**
     * Музыка магазина.
     * Лёгкая, ненавязчивая музыка для магазина.
     */
    val SHOP = MusicTrack(
        id = "shop",
        name = "Shop Theme",
        assetPath = "music/shop.ogg",
        duration = 120_000L,
        isLooping = true,
        volume = 0.5f
    )

    /**
     * Музыка паузы.
     * Тихая, фоновая музыка для меню паузы.
     */
    val PAUSE = MusicTrack(
        id = "pause",
        name = "Pause Menu",
        assetPath = "music/pause.ogg",
        duration = 90_000L,
        isLooping = true,
        volume = 0.4f
    )

    /**
     * Музыка уровня достижений.
     * Праздничная музыка при получении достижения.
     */
    val ACHIEVEMENT = MusicTrack(
        id = "achievement",
        name = "Achievement Unlocked",
        assetPath = "music/achievement.ogg",
        duration = 10_000L,
        isLooping = false,
        volume = 0.7f
    )

    /**
     * Музыка обучающего уровня.
     * Спокойная музыка для туториала.
     */
    val TUTORIAL = MusicTrack(
        id = "tutorial",
        name = "Tutorial",
        assetPath = "music/tutorial.ogg",
        duration = 180_000L,
        isLooping = true,
        volume = 0.5f
    )

    /**
     * Все доступные треки.
     */
    val ALL_TRACKS: List<MusicTrack> = listOf(
        MAIN_THEME,
        GAMEPLAY_1,
        GAMEPLAY_2,
        BOSS_BATTLE,
        GAME_OVER,
        VICTORY,
        SHOP,
        PAUSE,
        ACHIEVEMENT,
        TUTORIAL
    )

    /**
     * Треки для обычного геймплея.
     */
    val GAMEPLAY_TRACKS: List<MusicTrack> = listOf(GAMEPLAY_1, GAMEPLAY_2)

    /**
     * Треки для меню.
     */
    val MENU_TRACKS: List<MusicTrack> = listOf(MAIN_THEME, SHOP, PAUSE)

    /**
     * Получить трек по ID.
     *
     * @param id Уникальный идентификатор трека
     * @return MusicTrack или null если не найден
     */
    fun getTrackById(id: String): MusicTrack? = ALL_TRACKS.find { it.id == id }

    /**
     * Получить случайный трек из списка геймплея.
     */
    fun getRandomGameplayTrack(): MusicTrack = GAMEPLAY_TRACKS.random()

    /**
     * Проверка, является ли трек треком меню.
     */
    fun isMenuTrack(track: MusicTrack): Boolean = track in MENU_TRACKS

    /**
     * Проверка, является ли трек треком геймплея.
     */
    fun isGameplayTrack(track: MusicTrack): Boolean = track in GAMEPLAY_TRACKS
}
