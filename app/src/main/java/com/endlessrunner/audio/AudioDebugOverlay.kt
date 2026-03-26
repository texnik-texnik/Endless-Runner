package com.endlessrunner.audio

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface

/**
 * Debug overlay для отображения аудио информации.
 *
 * Визуализирует текущее состояние аудио системы:
 * - Текущий музыкальный трек
 * - Активные каналы
 * - Использование памяти
 * - Уровень громкости
 *
 * @param audioManager Менеджер аудио
 */
class AudioDebugOverlay(
    private val audioManager: AudioManager
) {
    /**
     * Флаг видимости overlay.
     */
    var isVisible: Boolean = false

    /**
     * Paint для текста.
     */
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 12f
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
    }

    /**
     * Paint для фона.
     */
    private val backgroundPaint = Paint().apply {
        color = Color.argb(180, 0, 0, 0)
        style = Paint.Style.FILL
    }

    /**
     * Paint для прогресс баров.
     */
    private val progressPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
    }

    /**
     * Paint для заголовков.
     */
    private val headerPaint = Paint().apply {
        color = Color.CYAN
        textSize = 14f
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
        isFakeBoldText = true
    }

    /**
     * Rect для фона.
     */
    private val backgroundRect = RectF()

    /**
     * Отступы.
     */
    private val padding = 8f
    private val lineHeight = 16f

    /**
     * Переключить видимость.
     */
    fun toggleVisibility() {
        isVisible = !isVisible
    }

    /**
     * Отрисовать debug overlay.
     *
     * @param canvas Canvas для отрисовки
     * @param width Ширина области
     * @param height Высота области
     */
    fun render(canvas: Canvas, width: Float, height: Float) {
        if (!isVisible) return

        val lines = buildDebugLines()
        val contentHeight = (lines.size * lineHeight) + padding * 2
        val contentWidth = width.coerceAtMost(300f)

        // Фон
        backgroundRect.set(0f, 0f, contentWidth, contentHeight)
        canvas.drawRect(backgroundRect, backgroundPaint)

        // Отрисовка строк
        var y = padding + lineHeight
        lines.forEach { line ->
            val (text, color, isHeader) = line
            textPaint.color = color
            canvas.drawText(text, padding, y, if (isHeader) headerPaint else textPaint)
            y += lineHeight
        }
    }

    /**
     * Построить список строк для отрисовки.
     */
    private fun buildDebugLines(): List<DebugLine> {
        val lines = mutableListOf<DebugLine>()

        // Заголовок
        lines.add(DebugLine("=== AUDIO DEBUG ===", Color.CYAN, isHeader = true))

        // Текущий трек
        lines.add(DebugLine("", Color.WHITE, isHeader = false))
        lines.add(DebugLine("CURRENT TRACK:", Color.CYAN, isHeader = true))
        val currentTrack = audioManager.getCurrentMusic()
        if (currentTrack != null) {
            lines.add(DebugLine("  Name: ${currentTrack.name}", Color.WHITE, isHeader = false))
            lines.add(DebugLine("  Playing: ${audioManager.isMusicPlaying()}", Color.WHITE, isHeader = false))
            lines.add(DebugLine("  Position: ${currentTrack.currentPosition.formattedDuration}", Color.WHITE, isHeader = false))
            lines.add(DebugLine("  Duration: ${currentTrack.formattedDuration}", Color.WHITE, isHeader = false))
        } else {
            lines.add(DebugLine("  No track", Color.GRAY, isHeader = false))
        }

        // Громкость
        lines.add(DebugLine("", Color.WHITE, isHeader = false))
        lines.add(DebugLine("VOLUMES:", Color.CYAN, isHeader = true))
        lines.add(DebugLine("  Master: ${(audioManager.masterVolumeFlow.value * 100).toInt()}%", Color.WHITE, isHeader = false))
        lines.add(DebugLine("  Music: ${(audioManager.musicVolumeFlow.value * 100).toInt()}%", Color.WHITE, isHeader = false))
        lines.add(DebugLine("  SFX: ${(audioManager.sfxVolumeFlow.value * 100).toInt()}%", Color.WHITE, isHeader = false))
        lines.add(DebugLine("  Muted: ${audioManager.isMutedFlow.value}", Color.WHITE, isHeader = false))

        // Активные каналы
        lines.add(DebugLine("", Color.WHITE, isHeader = false))
        lines.add(DebugLine("CHANNELS:", Color.CYAN, isHeader = true))
        val audioMixer = AudioMixer.getInstance()
        AudioChannel.ALL.forEach { channel ->
            val volume = audioMixer.getChannelVolume(channel)
            val isMuted = audioMixer.isChannelMuted(channel)
            val status = if (isMuted) " [MUTED]" else ""
            val color = if (isMuted) Color.GRAY else Color.WHITE
            lines.add(DebugLine("  ${channel.name}: ${(volume * 100).toInt()}%$status", color, isHeader = false))
        }

        // Использование памяти
        lines.add(DebugLine("", Color.WHITE, isHeader = false))
        lines.add(DebugLine("MEMORY:", Color.CYAN, isHeader = true))
        val cache = AudioCache.getInstance()
        val stats = cache.getStats()
        lines.add(DebugLine("  Music cached: ${stats.musicCount}", Color.WHITE, isHeader = false))
        lines.add(DebugLine("  SFX cached: ${stats.soundCount}", Color.WHITE, isHeader = false))
        lines.add(DebugLine("  Cache size: ${stats.formattedCacheSize}", Color.WHITE, isHeader = false))
        lines.add(DebugLine("  Cache fill: ${stats.fillPercent}%", Color.WHITE, isHeader = false))

        // SoundPool статистика
        lines.add(DebugLine("", Color.WHITE, isHeader = false))
        lines.add(DebugLine("SOUND POOL:", Color.CYAN, isHeader = true))
        val soundPool = SoundEffectPool.getInstance(audioManager as? android.content.Context ?: return lines)
        lines.add(DebugLine("  Active: ${soundPool.getActiveCount()}", Color.WHITE, isHeader = false))
        lines.add(DebugLine("  Free: ${soundPool.getFreeCount()}", Color.WHITE, isHeader = false))
        lines.add(DebugLine("  Created: ${soundPool.getCreatedCount()}", Color.WHITE, isHeader = false))

        return lines
    }

    /**
     * Показать информацию о текущем треке.
     */
    fun showCurrentTrack(): String {
        val track = audioManager.getCurrentMusic()
        return if (track != null) {
            """
            |Track: ${track.name}
            |ID: ${track.id}
            |Playing: ${audioManager.isMusicPlaying()}
            |Position: ${track.currentPosition.formattedDuration} / ${track.formattedDuration}
            |Volume: ${(track.volume * 100).toInt()}%
            |Looping: ${track.isLooping}
            """.trimMargin()
        } else {
            "No track playing"
        }
    }

    /**
     * Показать активные каналы.
     */
    fun showActiveChannels(): String {
        val audioMixer = AudioMixer.getInstance()
        return buildString {
            appendLine("Active Audio Channels:")
            AudioChannel.ALL.forEach { channel ->
                val volume = audioMixer.getChannelVolume(channel)
                val isMuted = audioMixer.isChannelMuted(channel)
                appendLine("  ${channel.name}: ${(volume * 100).toInt()}%${if (isMuted) " [MUTED]" else ""}")
            }
        }
    }

    /**
     * Показать использование памяти.
     */
    fun showMemoryUsage(): String {
        val cache = AudioCache.getInstance()
        val stats = cache.getStats()
        return """
        |Audio Cache Stats:
        |  Music tracks: ${stats.musicCount}
        |  Sound effects: ${stats.soundCount}
        |  Total objects: ${stats.totalCount}
        |  Cache size: ${stats.formattedCacheSize} / ${stats.formattedMaxSize}
        |  Fill percentage: ${stats.fillPercent}%
        """.trimMargin()
    }

    /**
     * Получить полную debug информацию.
     */
    fun getFullDebugInfo(): String {
        return buildString {
            appendLine(showCurrentTrack())
            appendLine()
            appendLine(showActiveChannels())
            appendLine()
            appendLine(showMemoryUsage())
        }
    }

    /**
     * Строка для отрисовки.
     */
    private data class DebugLine(
        val text: String,
        val color: Int,
        val isHeader: Boolean
    )

    companion object {
        @Volatile
        private var instance: AudioDebugOverlay? = null

        /**
         * Получить singleton экземпляр.
         */
        fun getInstance(audioManager: AudioManager): AudioDebugOverlay {
            return instance ?: synchronized(this) {
                instance ?: AudioDebugOverlay(audioManager).also { instance = it }
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
