package com.endlessrunner.bosses.arena

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.endlessrunner.bosses.BossType

/**
 * Арена для боя с боссом.
 * Определяет границы, опасности и платформы.
 *
 * @property width Ширина арены
 * @property height Высота арены
 * @property bossType Тип босса (для конфигурации)
 */
class BossArena(
    val width: Float,
    val height: Float,
    val bossType: BossType
) {
    /** Границы арены */
    val boundaries: RectF = RectF(0f, 0f, width, height)

    /** Список опасностей */
    private val hazards = mutableListOf<ArenaHazard>()

    /** Список платформ */
    private val platforms = mutableListOf<Platform>()

    /** Safe zone для игрока */
    private var safeZone: RectF = RectF(100f, 400f, width - 100f, height - 100f)

    /** Фон арены */
    private val backgroundPaint = Paint().apply {
        color = Color.rgb(30, 30, 40)
    }

    /** Границы арены (отрисовка) */
    private val boundaryPaint = Paint().apply {
        color = Color.rgb(100, 100, 120)
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    /**
     * Инициализация арены для конкретного босса.
     */
    fun initialize() {
        setupHazards()
        setupPlatforms()
        setupSafeZone()
    }

    /**
     * Настройка опасностей в зависимости от босса.
     */
    private fun setupHazards() {
        hazards.clear()

        when (bossType) {
            BossType.GIANT_SLIME -> {
                // Ядовитые лужи
                addHazard(PoisonHazard(200f, 700f, 100f))
                addHazard(PoisonHazard(width - 300f, 700f, 100f))
            }
            BossType.MECH_DRAGON -> {
                // Огненные зоны
                addHazard(FireHazard(300f, 750f, 150f))
                addHazard(FireHazard(width - 400f, 750f, 150f))
            }
            BossType.DARK_KNIGHT -> {
                // Шипы
                addHazard(SpikeHazard(100f, height - 50f, 200f))
                addHazard(SpikeHazard(width - 300f, height - 50f, 200f))
            }
            BossType.VOID_GUARDIAN -> {
                // Зоны гравитации
                addHazard(GravityHazard(width / 2f, height / 2f, 200f))
            }
            BossType.FINAL_BOSS -> {
                // Комбинация всех опасностей
                addHazard(PoisonHazard(150f, 700f, 80f))
                addHazard(FireHazard(400f, 750f, 100f))
                addHazard(SpikeHazard(width - 250f, height - 50f, 150f))
                addHazard(GravityHazard(width / 2f, 300f, 150f))
            }
        }
    }

    /**
     * Настройка платформ.
     */
    private fun setupPlatforms() {
        platforms.clear()

        // Базовые платформы
        addPlatform(Platform(200f, 600f, 400f, 50f))
        addPlatform(Platform(width - 600f, 600f, 400f, 50f))
        addPlatform(Platform(width / 2f - 200f, 400f, 400f, 30f))
    }

    /**
     * Настройка safe zone.
     */
    private fun setupSafeZone() {
        safeZone = RectF(
            100f,
            300f,
            width - 100f,
            height - 100f
        )
    }

    /**
     * Добавление опасности.
     */
    private fun addHazard(hazard: ArenaHazard) {
        hazards.add(hazard)
    }

    /**
     * Добавление платформы.
     */
    private fun addPlatform(platform: Platform) {
        platforms.add(platform)
    }

    /**
     * Обновление арены.
     */
    fun update(deltaTime: Float) {
        hazards.forEach { it.update(deltaTime) }
        platforms.forEach { it.update(deltaTime) }
    }

    /**
     * Отрисовка арены.
     */
    fun render(canvas: Canvas, cameraX: Float = 0f) {
        // Фон
        canvas.drawRect(boundaries, backgroundPaint)

        // Границы
        canvas.drawRect(boundaries, boundaryPaint)

        // Платформы
        platforms.forEach { it.render(canvas, cameraX) }

        // Опасности
        hazards.forEach { it.render(canvas, cameraX) }
    }

    /**
     * Проверка, находится ли игрок на арене.
     */
    fun isPlayerInArena(playerX: Float, playerY: Float): Boolean {
        return boundaries.contains(playerX, playerY)
    }

    /**
     * Проверка коллизии с опасностью.
     */
    fun checkHazardCollision(playerX: Float, playerY: Float, playerRadius: Float): ArenaHazard? {
        return hazards.find { it.checkCollision(playerX, playerY, playerRadius) }
    }

    /**
     * Получение safe zone.
     */
    fun getSafeZone(): RectF = RectF(safeZone)

    /**
     * Получение всех опасностей.
     */
    fun getHazards(): List<ArenaHazard> = hazards.toList()

    /**
     * Получение всех платформ.
     */
    fun getPlatforms(): List<Platform> = platforms.toList()

    /**
     * Очистка арены.
     */
    fun clear() {
        hazards.clear()
        platforms.clear()
    }

    /**
     * Активация всех опасностей.
     */
    fun activateAllHazards() {
        hazards.forEach { it.isActive = true }
    }

    /**
     * Деактивация всех опасностей.
     */
    fun deactivateAllHazards() {
        hazards.forEach { it.isActive = false }
    }
}

/**
 * Базовый класс опасности арены.
 */
abstract class ArenaHazard(
    val x: Float,
    val y: Float,
    val radius: Float,
    val damage: Int = 10,
    val damageInterval: Float = 0.5f
) {
    var isActive: Boolean = true
    protected var damageTimer: Float = 0f

    /**
     * Обновление опасности.
     */
    open fun update(deltaTime: Float) {
        if (isActive) {
            damageTimer += deltaTime
        }
    }

    /**
     * Проверка коллизии с игроком.
     */
    open fun checkCollision(playerX: Float, playerY: Float, playerRadius: Float): Boolean {
        if (!isActive) return false

        val dx = x - playerX
        val dy = y - playerY
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        return distance < radius + playerRadius
    }

    /**
     * Нанесение урона.
     */
    open fun onCollide(): Int {
        return damage
    }

    /**
     * Отрисовка опасности.
     */
    abstract fun render(canvas: Canvas, cameraX: Float)
}

/**
 * Ядовитая лужа.
 */
class PoisonHazard(
    x: Float,
    y: Float,
    radius: Float,
    damage: Int = 5
) : ArenaHazard(x, y, radius, damage) {

    private val paint = Paint().apply {
        color = Color.rgb(76, 175, 80)
        alpha = 100
    }

    override fun render(canvas: Canvas, cameraX: Float) {
        if (!isActive) return
        canvas.drawCircle(x - cameraX, y, radius, paint)
    }

    override fun onCollide(): Int {
        if (damageTimer >= damageInterval) {
            damageTimer = 0f
            return damage
        }
        return 0
    }
}

/**
 * Огненная зона.
 */
class FireHazard(
    x: Float,
    y: Float,
    radius: Float,
    damage: Int = 15
) : ArenaHazard(x, y, radius, damage) {

    private val paint = Paint().apply {
        color = Color.rgb(255, 87, 34)
        alpha = 120
    }

    private var flickerTimer: Float = 0f

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        flickerTimer += deltaTime
        paint.alpha = (100 + kotlin.math.sin(flickerTimer * 10f) * 20).toInt()
    }

    override fun render(canvas: Canvas, cameraX: Float) {
        if (!isActive) return
        canvas.drawCircle(x - cameraX, y, radius, paint)
    }

    override fun onCollide(): Int {
        if (damageTimer >= damageInterval) {
            damageTimer = 0f
            return damage
        }
        return 0
    }
}

/**
 * Шипы.
 */
class SpikeHazard(
    x: Float,
    y: Float,
    width: Float,
    damage: Int = 20
) : ArenaHazard(x, y, width / 2f, damage) {

    private val paint = Paint().apply {
        color = Color.rgb(100, 100, 100)
        style = Paint.Style.FILL
    }

    override fun render(canvas: Canvas, cameraX: Float) {
        if (!isActive) return

        val spikeWidth = 30f
        val spikeHeight = 50f
        val numSpikes = (width / spikeWidth).toInt()

        for (i in 0 until numSpikes) {
            val spikeX = x - width / 2f + i * spikeWidth - cameraX
            val path = android.graphics.Path()
            path.moveTo(spikeX, y)
            path.lineTo(spikeX + spikeWidth / 2f, y - spikeHeight)
            path.lineTo(spikeX + spikeWidth, y)
            path.close()
            canvas.drawPath(path, paint)
        }
    }

    override fun onCollide(): Int {
        if (damageTimer >= damageInterval) {
            damageTimer = 0f
            return damage
        }
        return 0
    }
}

/**
 * Зона гравитации.
 */
class GravityHazard(
    x: Float,
    y: Float,
    radius: Float,
    val pullStrength: Float = 200f
) : ArenaHazard(x, y, radius, damage = 5) {

    private val paint = Paint().apply {
        color = Color.rgb(103, 58, 183)
        style = Paint.Style.STROKE
        strokeWidth = 3f
        alpha = 100
    }

    private var rotationAngle: Float = 0f

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        rotationAngle += deltaTime
    }

    override fun render(canvas: Canvas, cameraX: Float) {
        if (!isActive) return

        // Концентрические круги
        for (i in 1..3) {
            val alpha = (150 - i * 30).coerceAtLeast(50)
            paint.alpha = alpha
            canvas.drawCircle(
                x - cameraX,
                y,
                radius * i / 3f,
                paint
            )
        }

        // Спираль
        paint.alpha = 150
        for (i in 0..360 step 30) {
            val angle = Math.toRadians((i + rotationAngle * 50).toDouble()).toFloat()
            val r = radius * (i / 360f)
            val px = x - cameraX + kotlin.math.cos(angle) * r
            val py = y + kotlin.math.sin(angle) * r
            canvas.drawCircle(px, py, 5f, paint)
        }
    }

    /**
     * Получение силы притяжения в точке.
     */
    fun getPullForce(playerX: Float, playerY: Float): Pair<Float, Float> {
        val dx = x - playerX
        val dy = y - playerY
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        if (distance > 0) {
            val force = pullStrength / (distance / 50f)
            return Pair((dx / distance) * force, (dy / distance) * force)
        }
        return Pair(0f, 0f)
    }
}

/**
 * Платформа на арене.
 */
class Platform(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val isMoving: Boolean = false,
    val moveRange: Float = 0f,
    val moveSpeed: Float = 0f
) {
    private var moveOffset: Float = 0f
    private var moveDirection: Float = 1f

    private val paint = Paint().apply {
        color = Color.rgb(80, 80, 100)
        style = Paint.Style.FILL
    }

    private val edgePaint = Paint().apply {
        color = Color.rgb(120, 120, 140)
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    /**
     * Обновление платформы.
     */
    fun update(deltaTime: Float) {
        if (isMoving && moveRange > 0) {
            moveOffset += moveDirection * moveSpeed * deltaTime

            if (kotlin.math.abs(moveOffset) > moveRange) {
                moveDirection *= -1
            }
        }
    }

    /**
     * Отрисовка платформы.
     */
    fun render(canvas: Canvas, cameraX: Float) {
        val renderX = x + moveOffset - cameraX

        // Тело платформы
        canvas.drawRect(
            RectF(renderX - width / 2f, y - height / 2f, renderX + width / 2f, y + height / 2f),
            paint
        )

        // Края
        canvas.drawRect(
            RectF(renderX - width / 2f, y - height / 2f, renderX + width / 2f, y + height / 2f),
            edgePaint
        )
    }

    /**
     * Проверка коллизии с платформой.
     */
    fun checkCollision(playerX: Float, playerY: Float, playerWidth: Float, playerHeight: Float): Boolean {
        val renderX = x + moveOffset
        val playerRect = RectF(
            playerX - playerWidth / 2f,
            playerY - playerHeight / 2f,
            playerX + playerWidth / 2f,
            playerY + playerHeight / 2f
        )
        val platformRect = RectF(
            renderX - width / 2f,
            y - height / 2f,
            renderX + width / 2f,
            y + height / 2f
        )

        return android.graphics.RectF.intersects(playerRect, platformRect)
    }

    /**
     * Получение текущей позиции.
     */
    fun getCurrentX(): Float = x + moveOffset

    /**
     * Получение bounds платформы.
     */
    fun getBounds(): RectF {
        return RectF(
            getCurrentX() - width / 2f,
            y - height / 2f,
            getCurrentX() + width / 2f,
            y + height / 2f
        )
    }
}

/**
 * Тип опасности.
 */
enum class HazardType {
    SPIKES,
    LAVA,
    PIT,
    MOVING_PLATFORM,
    POISON,
    FIRE,
    GRAVITY
}
