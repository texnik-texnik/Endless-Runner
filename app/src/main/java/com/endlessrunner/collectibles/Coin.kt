package com.endlessrunner.collectibles

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.endlessrunner.audio.AudioManager
import com.endlessrunner.audio.SoundLibrary
import com.endlessrunner.components.RenderComponent
import com.endlessrunner.config.CoinConfig
import com.endlessrunner.core.GameConstants
import com.endlessrunner.entities.Entity
import com.endlessrunner.player.Player

/**
 * Монета - собираемый предмет.
 * Наследуется от Collectible.
 *
 * @param value Стоимость монеты
 * @param config Конфигурация монеты
 */
class Coin(
    override val value: Int = GameConstants.BASE_COIN_SCORE,
    private val config: CoinConfig = CoinConfig(),
    private val context: Context? = null
) : Collectible(
    value = value,
    width = config.width,
    height = config.height
) {

    /**
     * Аудио менеджер для воспроизведения звуков.
     */
    private val audioManager: AudioManager? by lazy {
        context?.let { AudioManager.getInstance(it) }
    }
    
    companion object {
        /** Paint для отрисовки монеты (кэшируется) */
        private val coinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = config.color
        }
        
        /** Paint для обводки */
        private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#DAA520")
            strokeWidth = 3f
        }
        
        /** Paint для блеска */
        private val shinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#FFFFE0")
        }
    }
    
    // ============================================================================
    // СВОЙСТВА
    // ============================================================================
    
    /** Текущий масштаб для анимации появления */
    private var spawnScale: Float = 0f
    
    /** Фаза анимации появления */
    private var isSpawning: Boolean = true
    
    /** Длительность анимации появления */
    private val spawnDuration: Float = 0.3f
    
    // ============================================================================
    // ИНИЦИАЛИЗАЦИЯ
    // ============================================================================
    
    init {
        setupCoin()
    }
    
    /**
     * Настройка монеты.
     */
    private fun setupCoin() {
        // Настройка рендеринга
        renderComponent?.apply {
            color = config.color
            setSize(config.width, config.height)
        }
        
        // Настройка анимаций
        setRotationAnimation(config.rotationSpeed)
        setBobAnimation(config.bobAmplitude, config.bobFrequency)
        
        // Начальное состояние
        spawnScale = 0f
        isSpawning = true
    }
    
    /**
     * Установка спрайта монеты.
     * 
     * @param bitmap Bitmap спрайта
     */
    fun setSprite(bitmap: Bitmap?) {
        renderComponent?.setSprite(bitmap)
    }
    
    // ============================================================================
    // ОБНОВЛЕНИЕ
    // ============================================================================
    
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
        
        super.update(deltaTime)
    }
    
    override fun render(canvas: Canvas) {
        if (isCollected) return
        
        if (renderComponent?.sprite == null) {
            // Отрисовка процедурной монеты
            renderProceduralCoin(canvas)
        } else {
            super.render(canvas)
        }
    }
    
    /**
     * Отрисовка процедурной монеты (без спрайта).
     */
    private fun renderProceduralCoin(canvas: Canvas) {
        val position = positionComponent ?: return
        val render = renderComponent ?: return
        
        canvas.save()
        
        try {
            // Трансформация
            canvas.translate(position.x, position.y)
            
            // Вращение (симуляция 3D через масштаб по X)
            val rotationRad = Math.toRadians(position.rotation.toDouble()).toFloat()
            val scaleX = kotlin.math.abs(kotlin.math.cos(rotationRad))
            
            canvas.scale(scaleX * render.scaleX, render.scaleY)
            
            // Радиус монеты
            val radiusX = render.width / 2f
            val radiusY = render.height / 2f
            
            // Основной круг
            coinPaint.color = config.color
            coinPaint.alpha = render.alpha
            val bounds = RectF(-radiusX, -radiusY, radiusX, radiusY)
            canvas.drawOval(bounds, coinPaint)
            
            // Обводка
            borderPaint.alpha = render.alpha
            canvas.drawOval(bounds, borderPaint)
            
            // Блеск
            if (scaleX > 0.5f) {
                shinePaint.alpha = (render.alpha * 0.6f).toInt()
                val shineBounds = RectF(-radiusX * 0.3f, -radiusY * 0.5f, radiusX * 0.2f, -radiusY * 0.2f)
                canvas.drawOval(shineBounds, shinePaint)
            }
            
        } finally {
            canvas.restore()
        }
    }
    
    // ============================================================================
    // СБОР
    // ============================================================================
    
    override fun onCollect(collector: Entity?) {
        // Добавление очков игроку
        if (collector is Player) {
            collector.collectCoin(value)
        }

        // Воспроизведение звука сбора монеты
        audioManager?.playSfx(SoundLibrary.COIN_COLLECT)
        
        // TODO: Добавить частицы
    }
    
    override fun onCollectAnimation() {
        // Анимация сбора - уменьшение и исчезновение
        // TODO: Реализовать анимацию
    }
    
    override fun checkOutOfBounds() {
        val position = positionComponent
        // Монеты удаляются когда уходят далеко за левую границу
        if (position.x < -300f) {
            markForDestroy()
        }
    }
    
    override fun reset() {
        super.reset()
        spawnScale = 0f
        isSpawning = true
        renderComponent?.setScale(1f)
        renderComponent?.isVisible = true
    }
}

/**
 * Special монета с бонусом.
 */
class BonusCoin(
    value: Int = 50,
    private val bonusType: BonusType = BonusType.EXTRA_POINTS
) : Coin(value) {
    
    enum class BonusType {
        EXTRA_POINTS,     // Дополнительные очки
        SHIELD,           // Щит
        MAGNET,           // Магнит для монет
        SPEED_BOOST,      // Ускорение
        INVINCIBILITY     // Неуязвимость
    }
    
    override fun onCollect(collector: Entity?) {
        super.onCollect(collector)
        
        // Применение бонуса
        when (bonusType) {
            BonusType.EXTRA_POINTS -> {
                // Уже обработано в super.onCollect
            }
            BonusType.SHIELD -> {
                // TODO: Добавить щит
            }
            BonusType.MAGNET -> {
                // TODO: Добавить магнит
            }
            BonusType.SPEED_BOOST -> {
                // TODO: Добавить ускорение
            }
            BonusType.INVINCIBILITY -> {
                // TODO: Добавить неуязвимость
            }
        }
    }
}

/**
 * Extension функция для создания монеты с конфигурацией.
 */
fun createCoin(
    x: Float,
    y: Float,
    value: Int = GameConstants.BASE_COIN_SCORE,
    config: CoinConfig = CoinConfig()
): Coin {
    return Coin(value, config).apply {
        positionComponent?.setPosition(x, y)
    }
}

/**
 * Extension функция для создания группы монет в линию.
 */
fun createCoinLine(
    startX: Float,
    startY: Float,
    count: Int,
    spacing: Float = 80f,
    value: Int = GameConstants.BASE_COIN_SCORE
): List<Coin> {
    return List(count) { index ->
        Coin(value).apply {
            positionComponent?.setPosition(
                startX + index * spacing,
                startY
            )
        }
    }
}

/**
 * Extension функция для создания группы монет по дуге.
 */
fun createCoinArc(
    centerX: Float,
    centerY: Float,
    radius: Float,
    startAngle: Float,
    endAngle: Float,
    count: Int,
    value: Int = GameConstants.BASE_COIN_SCORE
): List<Coin> {
    return List(count) { index ->
        val progress = index.toFloat() / (count - 1)
        val angle = startAngle + (endAngle - startAngle) * progress
        val radian = Math.toRadians(angle.toDouble()).toFloat()
        
        Coin(value).apply {
            positionComponent?.setPosition(
                centerX + kotlin.math.cos(radian) * radius,
                centerY + kotlin.math.sin(radian) * radius
            )
        }
    }
}
