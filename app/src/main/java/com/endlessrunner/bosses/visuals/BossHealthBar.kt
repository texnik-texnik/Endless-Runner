package com.endlessrunner.bosses.visuals

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.endlessrunner.bosses.BossPhase
import com.endlessrunner.bosses.BossType
import com.endlessrunner.bosses.phaseName

/**
 * Health bar для босса.
 * Отображает здоровье, фазы, имя босса.
 */
class BossHealthBar {

    // ============================================================================
    // КОНФИГУРАЦИЯ
    // ============================================================================

    /** Ширина health bar */
    private val barWidth: Float = 800f

    /** Высота health bar */
    private val barHeight: Float = 40f

    /** Позиция X (центр) */
    private val barX: Float = 560f // (1920 - 800) / 2

    /** Позиция Y */
    private val barY: Float = 50f

    /** Высота текста */
    private val textHeight: Float = 30f

    /** Отступ между текстом и bar */
    private val textMargin: Float = 10f

    // ============================================================================
    // ДАННЫЕ
    // ============================================================================

    /** Текущее здоровье */
    private var currentHealth: Int = 100

    /** Максимальное здоровье */
    private var maxHealth: Int = 100

    /** Предыдущее здоровье (для анимации) */
    private var previousHealth: Int = 100

    /** Тип босса */
    private var bossType: BossType? = null

    /** Текущая фаза */
    private var currentPhase: BossPhase? = null

    /** Таймер анимации здоровья */
    private var healthAnimTimer: Float = 0f

    /** Длительность анимации */
    private val animDuration: Float = 0.5f

    // ============================================================================
    // PAINTS
    // ============================================================================

    /** Фон health bar */
    private val backgroundPaint = Paint().apply {
        color = Color.rgb(40, 40, 50)
    }

    /** Основной health bar */
    private val healthPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    /** Градиент для health bar */
    private var healthShader: Shader? = null

    /** Задний health (для анимации) */
    private val delayedHealthPaint = Paint().apply {
        color = Color.rgb(100, 100, 100)
        style = Paint.Style.FILL
    }

    /** Обводка */
    private val borderPaint = Paint().apply {
        color = Color.rgb(80, 80, 100)
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    /** Текст имени */
    private val namePaint = Paint().apply {
        color = Color.WHITE
        textSize = 28f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        typeface = Typeface.DEFAULT_BOLD
    }

    /** Текст здоровья */
    private val healthTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }

    /** Текст фазы */
    private val phasePaint = Paint().apply {
        color = Color.rgb(255, 215, 0)
        textSize = 18f
        textAlign = Paint.Align.CENTER
    }

    /** Маркеры фаз */
    private val phaseMarkerPaint = Paint().apply {
        color = Color.rgb(255, 255, 255)
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    // ============================================================================
    // УПРАВЛЕНИЕ
    // ============================================================================

    /**
     * Установка здоровья.
     */
    fun setHealth(current: Int, max: Int) {
        previousHealth = currentHealth
        currentHealth = current
        maxHealth = max
        healthAnimTimer = animDuration
    }

    /**
     * Установка типа босса.
     */
    fun setBossType(type: BossType) {
        bossType = type
        updateHealthColor()
    }

    /**
     * Установка фазы.
     */
    fun setPhase(phase: BossPhase) {
        currentPhase = phase
    }

    /**
     * Обновление цвета здоровья в зависимости от процента.
     */
    private fun updateHealthColor() {
        val healthPercent = currentHealth.toFloat() / maxHealth

        healthShader = when {
            healthPercent > 0.6f -> {
                // Зелёный
                LinearGradient(
                    barX, barY, barX + barWidth, barY,
                    intArrayOf(
                        Color.rgb(76, 175, 80),
                        Color.rgb(129, 199, 132)
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            healthPercent > 0.3f -> {
                // Жёлтый/Оранжевый
                LinearGradient(
                    barX, barY, barX + barWidth, barY,
                    intArrayOf(
                        Color.rgb(255, 152, 0),
                        Color.rgb(255, 193, 7)
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            else -> {
                // Красный
                LinearGradient(
                    barX, barY, barX + barWidth, barY,
                    intArrayOf(
                        Color.rgb(244, 67, 54),
                        Color.rgb(255, 87, 34)
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
        }
    }

    /**
     * Отрисовка health bar.
     */
    fun render(canvas: Canvas) {
        // Фон
        canvas.drawRoundRect(
            RectF(barX, barY, barX + barWidth, barY + barHeight),
            10f, 10f, backgroundPaint
        )

        // Анимация здоровья
        healthAnimTimer -= 1f / 60f
        val animProgress = (healthAnimTimer / animDuration).coerceIn(0f, 1f)
        val delayedHealth = previousHealth + (currentHealth - previousHealth) * (1f - animProgress)

        // Задний health (delayed)
        val delayedWidth = (delayedHealth.toFloat() / maxHealth) * barWidth
        canvas.drawRoundRect(
            RectF(barX, barY, barX + delayedWidth, barY + barHeight),
            10f, 10f, delayedHealthPaint
        )

        // Основной health
        val healthWidth = (currentHealth.toFloat() / maxHealth) * barWidth
        healthPaint.shader = healthShader
        canvas.drawRoundRect(
            RectF(barX, barY, barX + healthWidth, barY + barHeight),
            10f, 10f, healthPaint
        )
        healthPaint.shader = null

        // Обводка
        canvas.drawRoundRect(
            RectF(barX, barY, barX + barWidth, barY + barHeight),
            10f, 10f, borderPaint
        )

        // Маркеры фаз
        renderPhaseMarkers(canvas)

        // Имя босса
        renderBossName(canvas)

        // Здоровье (число)
        renderHealthText(canvas)

        // Фаза
        renderPhaseText(canvas)
    }

    /**
     * Отрисовка маркеров фаз.
     */
    private fun renderPhaseMarkers(canvas: Canvas) {
        bossType?.phases?.forEach { phase ->
            if (phase.healthThreshold < 1f) {
                val x = barX + barWidth * phase.healthThreshold
                canvas.drawLine(x, barY - 5f, x, barY + barHeight + 5f, phaseMarkerPaint)
            }
        }
    }

    /**
     * Отрисовка имени босса.
     */
    private fun renderBossName(canvas: Canvas) {
        val name = bossType?.id?.replace("_", " ")?.uppercase() ?: "BOSS"
        canvas.drawText(name, barX + barWidth / 2f, barY - textMargin, namePaint)
    }

    /**
     * Отрисовка текста здоровья.
     */
    private fun renderHealthText(canvas: Canvas) {
        val healthText = "$currentHealth / $maxHealth"
        canvas.drawText(
            healthText,
            barX + barWidth / 2f,
            barY + barHeight / 2f + 7f,
            healthTextPaint
        )
    }

    /**
     * Отрисовка текста фазы.
     */
    private fun renderPhaseText(canvas: Canvas) {
        currentPhase?.let { phase ->
            val phaseText = phase.phaseName
            canvas.drawText(
                phaseText,
                barX + barWidth / 2f,
                barY + barHeight + textMargin + 15f,
                phasePaint
            )
        }
    }

    /**
     * Сброс health bar.
     */
    fun reset() {
        currentHealth = 100
        maxHealth = 100
        previousHealth = 100
        bossType = null
        currentPhase = null
        healthAnimTimer = 0f
    }
}

/**
 * Warning overlay для появления босса.
 * Затемнение экрана, имя босса, тряска.
 */
class BossWarningOverlay {

    // ============================================================================
    // КОНФИГУРАЦИЯ
    // ============================================================================

    /** Длительность появления */
    private val showDuration: Float = 3f

    /** Длительность задержки перед исчезновением */
    private val holdDuration: Float = 1f

    /** Длительность исчезновения */
    private val fadeDuration: Float = 1f

    // ============================================================================
    // СОСТОЯНИЕ
    // ============================================================================

    /** Показан ли overlay */
    private var isVisible: Boolean = false

    /** Текущее состояние */
    private var state: WarningState = WarningState.Hidden

    /** Таймер */
    private var timer: Float = 0f

    /** Тип босса */
    private var bossType: BossType? = null

    // ============================================================================
    // PAINTS
    // ============================================================================

    /** Затемнение фона */
    private val backgroundPaint = Paint().apply {
        color = Color.BLACK
    }

    /** Текст имени босса */
    private val bossNamePaint = Paint().apply {
        color = Color.rgb(255, 87, 34)
        textSize = 80f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        typeface = Typeface.DEFAULT_BOLD
    }

    /** Подзаголовок */
    private val subtitlePaint = Paint().apply {
        color = Color.rgb(200, 200, 200)
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }

    // ============================================================================
    // УПРАВЛЕНИЕ
    // ============================================================================

    /**
     * Показ предупреждения.
     */
    fun show(type: BossType) {
        bossType = type
        isVisible = true
        state = WarningState.FadingIn
        timer = 0f
    }

    /**
     * Обновление overlay.
     */
    fun update(deltaTime: Float) {
        if (!isVisible) return

        timer += deltaTime

        when (state) {
            WarningState.FadingIn -> {
                if (timer >= showDuration) {
                    state = WarningState.Holding
                    timer = 0f
                }
            }
            WarningState.Holding -> {
                if (timer >= holdDuration) {
                    state = WarningState.FadingOut
                    timer = 0f
                }
            }
            WarningState.FadingOut -> {
                if (timer >= fadeDuration) {
                    isVisible = false
                    state = WarningState.Hidden
                }
            }
            WarningState.Hidden -> {}
        }
    }

    /**
     * Отрисовка overlay.
     */
    fun render(canvas: Canvas) {
        if (!isVisible) return

        val progress = getProgress()
        val alpha = getAlpha()

        // Затемнение фона
        backgroundPaint.alpha = (180 * alpha).toInt()
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), backgroundPaint)

        // Имя босса
        bossNamePaint.alpha = (255 * alpha).toInt()
        val bossName = bossType?.id?.replace("_", " ")?.uppercase() ?: "BOSS"
        canvas.drawText(
            bossName,
            canvas.width / 2f,
            canvas.height / 2f - 50f,
            bossNamePaint
        )

        // Подзаголовок
        subtitlePaint.alpha = (200 * alpha).toInt()
        canvas.drawText(
            "APPROACHING",
            canvas.width / 2f,
            canvas.height / 2f + 50f,
            subtitlePaint
        )

        // Эффект пульсации текста
        if (state == WarningState.Holding) {
            val scale = 1f + kotlin.math.sin(timer * 5f) * 0.05f
            // TODO: Применить масштабирование к тексту
        }
    }

    /**
     * Получение прогресса текущего состояния.
     */
    private fun getProgress(): Float {
        return when (state) {
            WarningState.FadingIn -> (timer / showDuration).coerceIn(0f, 1f)
            WarningState.Holding -> 1f
            WarningState.FadingOut -> 1f - (timer / fadeDuration).coerceIn(0f, 1f)
            WarningState.Hidden -> 0f
        }
    }

    /**
     * Получение альфа-канала.
     */
    private fun getAlpha(): Float {
        return when (state) {
            WarningState.FadingIn -> getProgress()
            WarningState.Holding -> 1f
            WarningState.FadingOut -> getProgress()
            WarningState.Hidden -> 0f
        }
    }

    /**
     * Сброс overlay.
     */
    fun reset() {
        isVisible = false
        state = WarningState.Hidden
        timer = 0f
        bossType = null
    }

    /**
     * Проверка, виден ли overlay.
     */
    fun isVisible(): Boolean = isVisible && state != WarningState.Hidden

    /**
     * Состояния overlay.
     */
    private enum class WarningState {
        Hidden,
        FadingIn,
        Holding,
        FadingOut
    }
}
