package com.endlessrunner.audio

import android.content.Context
import java.io.IOException

/**
 * Менеджер аудио ассетов.
 *
 * Управляет путями к аудио файлам в assets.
 * Предоставляет методы для получения путей и проверки доступности файлов.
 *
 * @param context Контекст приложения
 */
class AudioAssetManager private constructor(
    private val context: Context
) {
    /**
     * Базовая папка для музыки.
     */
    private val musicFolder = "music"

    /**
     * Базовая папка для звуковых эффектов.
     */
    private val sfxFolder = "sfx"

    /**
     * Базовая папка для фоновых звуков.
     */
    private val ambienceFolder = "ambience"

    /**
     * Расширение файлов по умолчанию.
     */
    private val defaultExtension = "ogg"

    /**
     * Получить путь к музыкальному треку.
     *
     * @param trackId ID трека
     * @return Полный путь к файлу
     */
    fun getMusicPath(trackId: String): String {
        return "$musicFolder/$trackId.$defaultExtension"
    }

    /**
     * Получить путь к музыкальному треку с указанием расширения.
     */
    fun getMusicPath(trackId: String, extension: String): String {
        return "$musicFolder/$trackId.$extension"
    }

    /**
     * Получить путь к звуковому эффекту.
     *
     * @param soundId ID звука
     * @return Полный путь к файлу
     */
    fun getSfxPath(soundId: String): String {
        // Определяем категорию по префиксу ID
        val category = getCategoryFromId(soundId)
        return "$sfxFolder/$category/$soundId.$defaultExtension"
    }

    /**
     * Получить путь к звуковому эффекту с указанием расширения.
     */
    fun getSfxPath(soundId: String, extension: String): String {
        val category = getCategoryFromId(soundId)
        return "$sfxFolder/$category/$soundId.$extension"
    }

    /**
     * Получить путь к фоновому звуку.
     *
     * @param ambienceId ID звука окружения
     * @return Полный путь к файлу
     */
    fun getAmbiencePath(ambienceId: String): String {
        return "$ambienceFolder/$ambienceId.$defaultExtension"
    }

    /**
     * Получить список доступных музыкальных треков.
     *
     * @return Список ID доступных треков
     */
    fun listAvailableMusic(): List<String> {
        return listAssetsInFolder(musicFolder)
            .filter { it.endsWith(".$defaultExtension") }
            .map { it.substringBeforeLast('.') }
    }

    /**
     * Получить список доступных звуковых эффектов.
     *
     * @return Список ID доступных звуков
     */
    fun listAvailableSfx(): List<String> {
        val allSounds = mutableListOf<String>()

        // Проверяем подпапки sfx
        val sfxCategories = listAssetsInFolder(sfxFolder)
            .filter { isFolder(sfxFolder, it) }

        sfxCategories.forEach { category ->
            val sounds = listAssetsInFolder("$sfxFolder/$category")
                .filter { it.endsWith(".$defaultExtension") }
                .map { it.substringBeforeLast('.') }
            allSounds.addAll(sounds)
        }

        return allSounds
    }

    /**
     * Получить список доступных фоновых звуков.
     */
    fun listAvailableAmbience(): List<String> {
        return listAssetsInFolder(ambienceFolder)
            .filter { it.endsWith(".$defaultExtension") }
            .map { it.substringBeforeLast('.') }
    }

    /**
     * Проверка, существует ли музыкальный файл.
     *
     * @param trackId ID трека
     * @return true если файл существует
     */
    fun musicExists(trackId: String): Boolean {
        return assetExists(getMusicPath(trackId))
    }

    /**
     * Проверка, существует ли файл звукового эффекта.
     *
     * @param soundId ID звука
     * @return true если файл существует
     */
    fun sfxExists(soundId: String): Boolean {
        return assetExists(getSfxPath(soundId))
    }

    /**
     * Проверка, существует ли файл фонового звука.
     */
    fun ambienceExists(ambienceId: String): Boolean {
        return assetExists(getAmbiencePath(ambienceId))
    }

    /**
     * Получить размер файла в байтах.
     *
     * @param path Путь к файлу
     * @return Размер файла или -1 если ошибка
     */
    fun getFileSize(path: String): Long {
        return try {
            context.assets.openFd(path).use { fd ->
                fd.length
            }
        } catch (e: IOException) {
            -1L
        }
    }

    /**
     * Получить общий размер всех музыкальных файлов.
     */
    fun getTotalMusicSize(): Long {
        return listAvailableMusic()
            .map { getFileSize(getMusicPath(it)) }
            .filter { it > 0 }
            .sum()
    }

    /**
     * Получить общий размер всех файлов звуковых эффектов.
     */
    fun getTotalSfxSize(): Long {
        return listAvailableSfx()
            .map { getFileSize(getSfxPath(it)) }
            .filter { it > 0 }
            .sum()
    }

    /**
     * Получить список подпапок в папке.
     */
    private fun listAssetsInFolder(folder: String): List<String> {
        return try {
            context.assets.list(folder)?.toList() ?: emptyList()
        } catch (e: IOException) {
            emptyList()
        }
    }

    /**
     * Проверка, является ли элемент папкой.
     */
    private fun isFolder(parentPath: String, name: String): Boolean {
        return try {
            val files = context.assets.list("$parentPath/$name")
            files != null && files.isNotEmpty()
        } catch (e: IOException) {
            false
        }
    }

    /**
     * Проверка, существует ли ассет.
     */
    private fun assetExists(path: String): Boolean {
        return try {
            context.assets.openFd(path).use { fd ->
                fd.close()
            }
            true
        } catch (e: IOException) {
            false
        }
    }

    /**
     * Определить категорию по ID звука.
     */
    private fun getCategoryFromId(soundId: String): String {
        return when {
            soundId.startsWith("ui_") -> "ui"
            soundId.startsWith("player_") -> "player"
            soundId.startsWith("coin_") || soundId.startsWith("gem_") || soundId.startsWith("treasure_") -> "coins"
            soundId.startsWith("enemy_") || soundId.startsWith("boss_") -> "enemies"
            soundId.startsWith("obstacle_") || soundId.startsWith("platform_") ||
                    soundId.startsWith("door_") || soundId.startsWith("spike_") -> "environment"
            soundId.startsWith("powerup_") || soundId.startsWith("shield_") ||
                    soundId.startsWith("magnet_") || soundId.startsWith("speed_") ||
                    soundId.startsWith("invincibility_") -> "powerups"
            else -> "common"
        }
    }

    companion object {
        @Volatile
        private var instance: AudioAssetManager? = null

        /**
         * Получить singleton экземпляр.
         */
        fun getInstance(context: Context): AudioAssetManager {
            return instance ?: synchronized(this) {
                instance ?: AudioAssetManager(context.applicationContext).also { instance = it }
            }
        }

        /**
         * Очистить singleton (для тестов).
         */
        fun clearInstance() {
            instance = null
        }
    }
}
