package com.endlessrunner.powerups

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.endlessrunner.audio.AudioManager
import com.endlessrunner.audio.SoundLibrary
import com.endlessrunner.collectibles.Collectible
import com.endlessrunner.components.PositionComponent
import com.endlessrunner.config.PowerUpConfig
import com.endlessrunner.core.GameConstants
import com.endlessrunner.entities.Entity
import com.endlessrunner.player.Player

/**
 * Бонус (Power-Up) - предмет дающий временное усиление игроку.
 * Наследуется от Collectible.
 *
 * @param type Тип бонуса
 * @param config Конфигурация бонуса
 * @param context Контекст для аудио
 */
class PowerUp(
    val type: PowerUpType,
    private val config: PowerUpConfig = PowerUpConfig(),
    private val context: Context? = null
) : Collectible(
    value = type.scoreValue,
    width = config.width,
    height = config.height
) {

    /**
     * Аудио менеджер для воспроизведения звуков.
     */
    private val audioManager: AudioManager? by lazy {
        context?.let { AudioManager.getInstance(it) }
    }

    /**
     * Длительность эффекта в секундах.
     */
    val duration: Float = type.durationSeconds

    /**
     * Флаг активации бонуса.
     */
    var isActivated: Boolean = false
        private set

    /**
     * Время до истечения эффекта.
     */
    var timeRemaining: Float = 0f
        private set

    /**
     * Paint для отрисовки бонуса.
     */
    private val powerUpPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = type.rarity.color
    }

    /**
     * Paint для обводки.
     */
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = 2f
    }

    /**
     * Paint для иконки.
     */
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }

    /**
     * Спрайт бонуса.
     */
    private var spriteBitmap: Bitmap? = null

    /**
     * Анимация вращения.
     */
    private var rotationAngle: Float = 0f

    /**
     * Анимация парения.
     */
    private var bobOffset: Float = 0f
    private var bobTime: Float = 0f

    init {
        setupPowerUp()
    }

    /**
     * Настройка бонуса.
     */
    private fun setupPowerUp() {
        renderComponent?.apply {
            color = type.rarity.color
            setSize(config.width, config.height)
        }

        // Начальная анимация появления
        spawnScale = 0f
        isSpawning = true
    }

    /**
     * Установка спрайта бонуса.
     */
    fun setSprite(bitmap: Bitmap?) {
        spriteBitmap = bitmap
        renderComponent?.setSprite(bitmap)
    }

    override fun update(deltaTime: Float) {
        if (isCollected) return

        // Анимация появления
        if (isSpawning) {
            spawnScale += deltaTime / spawnDuration
            if (spawnScale >= 1f) {
                spawnScale = 1f
                isSpawning = false
            }
            renderComponent?.setScale(spawnScale)
        }

        // Анимация вращения
        rotationAngle += config.rotationSpeed * deltaTime
        if (rotationAngle > 360f) {
            rotationAngle -= 360f
        }

        // Анимация парения
        bobTime += deltaTime
        bobOffset = kotlin.math.sin(bobTime * config.bobFrequency) * config.bobAmplitude

        // Обновление позиции с учётом парения
        positionComponent?.let { pos ->
            pos.y = baseY + bobOffset
        }

        super.update(deltaTime)
    }

    override fun render(canvas: Canvas) {
        if (isCollected) return

        if (spriteBitmap == null) {
            renderProceduralPowerUp(canvas)
        } else {
            super.render(canvas)
        }
    }

    /**
     * Отрисовка процедурного бонуса.
     */
    private fun renderProceduralPowerUp(canvas: Canvas) {
        val position = positionComponent ?: return
        val render = renderComponent ?: return

        canvas.save()

        try {
            canvas.translate(position.x, position.y + bobOffset)

            // Вращение (симуляция 3D)
            val rotationRad = Math.toRadians(rotationAngle.toDouble()).toFloat()
            val scaleX = kotlin.math.abs(kotlin.math.cos(rotationRad))

            canvas.scale(scaleX * render.scaleX, render.scaleY)

            val radiusX = render.width / 2f
            val radiusY = render.height / 2f

            // Основной фон (ромб)
            powerUpPaint.color = type.rarity.color
            powerUpPaint.alpha = render.alpha

            val diamondPath = android.graphics.Path()
            diamondPath.moveTo(0f, -radiusY)
            diamondPath.moveTo(radiusX, 0f)
            diamondPath.moveTo(0f, radiusY)
            diamondPath.moveTo(-radiusX, 0f)
            diamondPath.close()

            canvas.drawPath(diamondPath, powerUpPaint)

            // Обводка
            borderPaint.alpha = render.alpha
            canvas.drawPath(diamondPath, borderPaint)

            // Иконка (первая буква названия)
            iconPaint.alpha = render.alpha
            val iconChar = type.displayName.first().toString()
            canvas.drawText(iconChar, 0f, 5f, iconPaint)

        } finally {
            canvas.restore()
        }
    }

    /**
     * Базовая Y позиция (без учёта парения).
     */
    private var baseY: Float = 0f

    init {
        positionComponent?.let { pos ->
            baseY = pos.y
        }
    }

    override fun onCollect(collector: Entity?) {
        if (collector is Player) {
            activate(collector)
        }
    }

    /**
     * Активировать бонус на игроке.
     */
    fun activate(player: Player) {
        if (isActivated) return

        isActivated = true
        timeRemaining = duration

        // Воспроизведение звука активации
        when (type) {
            PowerUpType.SHIELD -> audioManager?.playSfx(SoundLibrary.SHIELD_ACTIVATE)
            PowerUpType.MAGNET -> audioManager?.playSfx(SoundLibrary.MAGNET_ACTIVATE)
            PowerUpType.SPEED -> audioManager?.playSfx(SoundLibrary.SPEED_BOOST)
            PowerUpType.INVINCIBILITY -> audioManager?.playSfx(SoundLibrary.INVINCIBILITY)
            else -> audioManager?.playSfx(SoundLibrary.POWERUP_ACTIVATE)
        }

        // Применение эффекта
        applyEffect(player)

        onActivate()
    }

    /**
     * Применить эффект бонуса.
     */
    private fun applyEffect(player: Player) {
        when (type) {
            PowerUpType.SHIELD -> {
                // Мгновенный эффект - добавляем щит
                player.isInvincible = true
            }
            PowerUpType.MAGNET -> {
                // Включить магнит для монет
                // TODO: Реализовать магнит
            }
            PowerUpType.SPEED -> {
                // Увеличить скорость игрока
                player.movementComponent?.speed = config.speed * 1.5f
            }
            PowerUpType.INVINCIBILITY -> {
                player.isInvincible = true
            }
            PowerUpType.DOUBLE_SCORES -> {
                // Удвоить очки
                // TODO: Реализовать множитель очков
            }
            PowerUpType.SLOW_TIME -> {
                // Замедлить время
                // TODO: Реализовать замедление времени
            }
            PowerUpType.TELEPORT -> {
                // Телепортировать вперёд
                player.positionComponent?.x = (player.positionComponent?.x ?: 0f) + 500f
            }
            PowerUpType.BOMB -> {
                // Уничтожить nearby врагов
                // TODO: Реализовать бомбу
            }
            PowerUpType.SUPER_JUMP -> {
                // Увеличить силу прыжка
                player.movementComponent?.jumpForce = config.jumpForce * 1.5f
            }
            PowerUpType.COIN_RAIN -> {
                // Спавнить монеты
                // TODO: Реализовать монетный дождь
            }
        }
    }

    /**
     * Вызывается при активации.
     */
    protected open fun onActivate() {
        // Переопределяется в наследниках
    }

    /**
     * Обновление таймера бонуса.
     */
    fun updateTimer(deltaTime: Float) {
        if (!isActivated || PowerUpType.isInstant(type)) return

        timeRemaining -= deltaTime
        if (timeRemaining <= 0f) {
            timeRemaining = 0f
            onDeactivate()
        }
    }

    /**
     * Вызывается при окончании эффекта.
     */
    protected open fun onDeactivate() {
        // Сброс эффектов
        when (type) {
            PowerUpType.SHIELD -> {
                // Щит одноразовый
            }
            PowerUpType.SPEED -> {
                // Восстановить нормальную скорость
            }
            PowerUpType.INVINCIBILITY -> {
                // Выключить неуязвимость если это был последний источник
            }
            else -> {
                // Сброс по умолчанию
            }
        }
    }

    /**
     * Получить процент оставшегося времени.
     */
    fun getTimePercent(): Float {
        return if (duration > 0f) timeRemaining / duration else 0f
    }

    /**
     * Проверка, активен ли ещё эффект.
     */
    fun isActiveEffect(): Boolean = isActivated && (PowerUpType.isInstant(type) || timeRemaining > 0f)

    companion object {
        /** Длительность анимации появления */
        private const val spawnDuration: Float = 0.3f

        /** Текущий масштаб появления */
        private var spawnScale: Float = 0f

        /** Флаг анимации появления */
        private var isSpawning: Boolean = true
    }

    override fun reset() {
        super.reset()
        isActivated = false
        timeRemaining = 0f
        spawnScale = 0f
        isSpawning = true
        rotationAngle = 0f
        bobTime = 0f
        bobOffset = 0f
        renderComponent?.setScale(1f)
        renderComponent?.isVisible = true
    }

    override fun checkOutOfBounds() {
        val position = positionComponent
        if (position.x < -300f) {
            markForDestroy()
        }
    }
}

/**
 * Конфигурация бонуса.
 */
data class PowerUpConfig(
    val width: Float = 40f,
    val height: Float = 40f,
    val rotationSpeed: Float = 90f,    // Градусов в секунду
    val bobAmplitude: Float = 5f,       // Амплитуда парения
    val bobFrequency: Float = 3f,       // Частота парения (Гц)
    val speed: Float = 300f,            // Базовая скорость
    val jumpForce: Float = 500f         // Базовая сила прыжка
)

/**
 * Extension функция для создания бонуса.
 */
fun createPowerUp(
    x: Float,
    y: Float,
    type: PowerUpType,
    config: PowerUpConfig = PowerUpConfig(),
    context: Context? = null
): PowerUp {
    return PowerUp(type, config, context).apply {
        positionComponent?.setPosition(x, y)
    }
}

/**
 * Extension функция для создания случайного бонуса.
 */
fun createRandomPowerUp(
    x: Float,
    y: Float,
    config: PowerUpConfig = PowerUpConfig(),
    context: Context? = null
): PowerUp {
    val type = PowerUpType.getRandomWeighted()
    return createPowerUp(x, y, type, config, context)
}
