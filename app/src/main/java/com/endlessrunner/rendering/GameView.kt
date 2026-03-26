package com.endlessrunner.rendering

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.endlessrunner.config.FullConfig
import com.endlessrunner.entities.interfaces.IRenderable
import com.endlessrunner.entities.interfaces.Vector2
import com.endlessrunner.systems.RenderSystem

/**
 * GameView - основной View для отрисовки игры.
 *
 * Использует SurfaceView для эффективного рендеринга графики.
 * Поддерживает отрисовку игровых сущностей и UI элементов.
 */
class GameView(
    context: Context,
    private val config: FullConfig?
) : SurfaceView(context), SurfaceHolder.Callback {

    private val holder: SurfaceHolder = holder

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
        typeface = Typeface.DEFAULT_BOLD
    }

    /** Позиция камеры */
    var cameraPosition = Vector2(0f, 0f)

    /** Размер viewport в мировых координатах */
    var viewportSize = Vector2(0f, 0f)

    /** RenderSystem для отрисовки сущностей */
    var renderSystem: RenderSystem? = null

    /** Данные для отрисовки HUD */
    var hudData: HudData? = null

    /** Текущее состояние для отрисовки */
    var gameState: String = "Menu"

    /** Флаг активности рендеринга */
    private var isRendering = false

    /** Поток рендеринга */
    private var renderThread: Thread? = null

    init {
        holder.addCallback(this)
        setWillNotDraw(false)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        updateViewportSize()
        isRendering = true
        startRenderLoop()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        updateViewportSize()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRendering = false
        renderThread?.join(1000)
        renderThread = null
    }

    /**
     * Запуск цикла рендеринга.
     */
    private fun startRenderLoop() {
        renderThread = Thread {
            while (isRendering && !Thread.currentThread().isInterrupted) {
                try {
                    val canvas = holder.lockCanvas()
                    if (canvas != null) {
                        try {
                            synchronized(this) {
                                doDraw(canvas)
                            }
                        } finally {
                            holder.unlockCanvasAndPost(canvas)
                        }
                    }

                    // Ограничение FPS для рендеринга
                    Thread.sleep(16)  // ~60 FPS
                } catch (e: Exception) {
                    // Игнорируем ошибки рендеринга
                }
            }
        }.apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    /**
     * Основной метод отрисовки.
     */
    private fun doDraw(canvas: Canvas) {
        // Очистка экрана
        drawBackground(canvas)

        // Отрисовка игровых сущностей через RenderSystem
        renderSystem?.renderAll(canvas, cameraPosition, viewportSize)

        // Отрисовка UI
        drawUI(canvas)
        drawHud(canvas)
    }

    /**
     * Отрисовка фона с градиентом.
     */
    private fun drawBackground(canvas: Canvas) {
        // Градиентное небо
        val skyColors = intArrayOf(
            Color.parseColor("#1A1A2E"),
            Color.parseColor("#16213E"),
            Color.parseColor("#0F3460")
        )
        val skyGradient = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            skyColors[0],
            skyColors[2],
            Shader.TileMode.CLAMP
        )
        val skyPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            shader = skyGradient
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat() * 0.7f, skyPaint)

        // Земля внизу
        val groundPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.parseColor("#2D4A22")
        }
        canvas.drawRect(0f, height.toFloat() * 0.7f, width.toFloat(), height.toFloat(), groundPaint)
    }

    /**
     * Отрисовка UI элементов (меню, пауза, game over).
     */
    private fun drawUI(canvas: Canvas) {
        textPaint.textSize = 48f
        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER

        when (gameState) {
            "Menu" -> {
                // Заголовок
                canvas.drawText(
                    "ENDLESS RUNNER",
                    width / 2f,
                    height / 3f,
                    textPaint
                )

                // Инструкция
                textPaint.textSize = 24f
                textPaint.color = Color.LTGRAY
                canvas.drawText(
                    "Tap to Jump",
                    width / 2f,
                    height / 2f,
                    textPaint
                )

                // Кнопка старта
                textPaint.textSize = 32f
                textPaint.color = Color.parseColor("#4CAF50")
                canvas.drawText(
                    "TAP TO START",
                    width / 2f,
                    height * 2f / 3f,
                    textPaint
                )
            }

            "Paused" -> {
                textPaint.textSize = 48f
                textPaint.color = Color.YELLOW
                canvas.drawText(
                    "PAUSED",
                    width / 2f,
                    height / 2f,
                    textPaint
                )

                textPaint.textSize = 24f
                textPaint.color = Color.LTGRAY
                canvas.drawText(
                    "Tap to Resume",
                    width / 2f,
                    height / 2f + 50,
                    textPaint
                )
            }

            "GameOver" -> {
                textPaint.textSize = 48f
                textPaint.color = Color.RED
                canvas.drawText(
                    "GAME OVER",
                    width / 2f,
                    height / 3f,
                    textPaint
                )

                textPaint.textSize = 24f
                textPaint.color = Color.WHITE
                canvas.drawText(
                    "Tap to Restart",
                    width / 2f,
                    height / 2f,
                    textPaint
                )
            }
        }
    }

    /**
     * Отрисовка HUD (счёт, жизни, монеты).
     */
    private fun drawHud(canvas: Canvas) {
        hudData?.let { data ->
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.textSize = 28f
            textPaint.color = Color.WHITE

            // Счёт
            canvas.drawText(
                "Score: ${data.score}",
                20f,
                40f,
                textPaint
            )

            // Монеты
            textPaint.color = Color.parseColor("#FFD700")
            canvas.drawText(
                "Coins: ${data.coins}",
                20f,
                80f,
                textPaint
            )

            // Жизни
            textPaint.color = Color.parseColor("#FF4444")
            canvas.drawText(
                "Lives: ${data.lives}",
                20f,
                120f,
                textPaint
            )

            // Дистанция
            textPaint.color = Color.WHITE
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(
                "Distance: ${data.distance}m",
                width - 20f,
                40f,
                textPaint
            )

            // Скорость
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(
                "Speed: ${data.speed} km/h",
                width - 20f,
                80f,
                textPaint
            )
        }
    }

    /**
     * Обновление размера viewport.
     */
    private fun updateViewportSize() {
        // Конвертируем пиксели в мировые координаты
        // Предполагаем, что ширина экрана = 20 мировым единицам
        val worldWidth = 20f
        val aspectRatio = width.toFloat() / height.toFloat()
        viewportSize = Vector2(worldWidth, worldWidth / aspectRatio)
    }

    /**
     * Обновление позиции камеры.
     */
    fun setCameraPosition(x: Float, y: Float) {
        cameraPosition.x = x
        cameraPosition.y = y
    }

    /**
     * Освобождение ресурсов.
     */
    fun release() {
        isRendering = false
        renderThread?.join(1000)
        renderThread = null
        renderSystem = null
        hudData = null
    }

    /**
     * Данные для отображения в HUD.
     */
    data class HudData(
        val score: Int,
        val coins: Int,
        val lives: Int,
        val distance: Int,
        val speed: Float
    )
}
