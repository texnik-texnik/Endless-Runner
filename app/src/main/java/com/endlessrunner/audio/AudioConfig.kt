package com.endlessrunner.audio

/**
 * Data class конфигурации аудио системы.
 *
 * Определяет настройки аудио для игры.
 * Может быть сохранена и загружена из настроек.
 *
 * @param enableMusic Включена ли музыка
 * @param enableSfx Включены ли звуковые эффекты
 * @param enableAmbience Включены ли фоновые звуки
 * @param maxSimultaneousSfx Максимум одновременных SFX
 * @param musicFadeDuration Длительность fade эффекта музыки
 * @param sfxPriority Приоритеты для звуков
 * @param defaultVolumes Громкости по умолчанию
 */
data class AudioConfig(
    val enableMusic: Boolean = true,
    val enableSfx: Boolean = true,
    val enableAmbience: Boolean = true,
    val maxSimultaneousSfx: Int = AudioConstants.MAX_SIMULTANEOUS_SFX,
    val musicFadeDuration: Long = AudioConstants.MUSIC_FADE_DURATION,
    val sfxPriority: Map<String, Int> = emptyMap(),
    val defaultVolumes: DefaultVolumes = DefaultVolumes()
) {
    /**
     * Проверка, включено ли аудио полностью.
     */
    val isAudioEnabled: Boolean
        get() = enableMusic || enableSfx || enableAmbience

    /**
     * Проверка, включена ли только музыка.
     */
    val isMusicOnly: Boolean
        get() = enableMusic && !enableSfx && !enableAmbience

    /**
     * Проверка, включены ли только звуки.
     */
    val isSfxOnly: Boolean
        get() = !enableMusic && enableSfx && !enableAmbience

    /**
     * Проверка, включены ли все аудио компоненты.
     */
    val isAllEnabled: Boolean
        get() = enableMusic && enableSfx && enableAmbience

    /**
     * Получить приоритет для звука.
     *
     * @param soundId ID звука
     * @return Приоритет или значение по умолчанию
     */
    fun getPriorityForSound(soundId: String): Int {
        return sfxPriority[soundId] ?: AudioConstants.DEFAULT_PRIORITY
    }

    /**
     * Создать копию конфигурации с изменёнными параметрами.
     */
    fun copy(
        enableMusic: Boolean = this.enableMusic,
        enableSfx: Boolean = this.enableSfx,
        enableAmbience: Boolean = this.enableAmbience,
        maxSimultaneousSfx: Int = this.maxSimultaneousSfx,
        musicFadeDuration: Long = this.musicFadeDuration,
        sfxPriority: Map<String, Int> = this.sfxPriority,
        defaultVolumes: DefaultVolumes = this.defaultVolumes
    ): AudioConfig = AudioConfig(
        enableMusic = enableMusic,
        enableSfx = enableSfx,
        enableAmbience = enableAmbience,
        maxSimultaneousSfx = maxSimultaneousSfx,
        musicFadeDuration = musicFadeDuration,
        sfxPriority = sfxPriority,
        defaultVolumes = defaultVolumes
    )

    /**
     * Конфигурация для низкого качества (экономия ресурсов).
     */
    fun toLowQuality(): AudioConfig = copy(
        maxSimultaneousSfx = 4,
        enableAmbience = false,
        musicFadeDuration = 500L
    )

    /**
     * Конфигурация для высокого качества.
     */
    fun toHighQuality(): AudioConfig = copy(
        maxSimultaneousSfx = 16,
        enableAmbience = true,
        musicFadeDuration = 1500L
    )

    /**
     * Конфигурация по умолчанию.
     */
    companion object {
        /**
         * Конфигурация по умолчанию.
         */
        val DEFAULT = AudioConfig()

        /**
         * Конфигурация для экономии батареи.
         */
        val BATTERY_SAVER = AudioConfig(
            enableMusic = true,
            enableSfx = true,
            enableAmbience = false,
            maxSimultaneousSfx = 4,
            musicFadeDuration = 500L
        )

        /**
         * Конфигурация только музыка.
         */
        val MUSIC_ONLY = AudioConfig(
            enableMusic = true,
            enableSfx = false,
            enableAmbience = false
        )

        /**
         * Конфигурация только звуки.
         */
        val SFX_ONLY = AudioConfig(
            enableMusic = false,
            enableSfx = true,
            enableAmbience = false
        )

        /**
         * Полностью выключенное аудио.
         */
        val SILENT = AudioConfig(
            enableMusic = false,
            enableSfx = false,
            enableAmbience = false
        )

        /**
         * Приоритеты по умолчанию для звуков.
         */
        val DEFAULT_PRIORITIES: Map<String, Int> = mapOf(
            SoundLibrary.PLAYER_DEATH.id to 10,
            SoundLibrary.PLAYER_HIT.id to 9,
            SoundLibrary.BOSS_ROAR.id to 10,
            SoundLibrary.ACHIEVEMENT_UNLOCK.id to 9,
            SoundLibrary.COIN_COLLECT.id to 7,
            SoundLibrary.PLAYER_JUMP.id to 7,
            SoundLibrary.BUTTON_CLICK.id to 6
        )

        /**
         * Создать конфигурацию с приоритетами по умолчанию.
         */
        fun withDefaultPriorities(): AudioConfig = AudioConfig(
            sfxPriority = DEFAULT_PRIORITIES
        )
    }
}
