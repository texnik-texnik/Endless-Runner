package com.endlessrunner.bosses.attacks

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.endlessrunner.bosses.Boss
import com.endlessrunner.bosses.PatternType
import kotlin.math.abs

/**
 * Паттерн: Charge - рывок на игрока.
 */
class ChargePattern(
    damage: Int = 15,
    duration: Float = 2f,
    cooldown: Float = 4f
) : AttackPattern(PatternType.CHARGE, damage, duration, cooldown, difficulty = 1) {

    private var isCharging: Boolean = false
    private var chargeSpeed: Float = 500f
    private var chargeDirection: Float = 1f // 1 = вправо, -1 = влево
    private val chargeWarningTime: Float = 0.5f
    private var warningTimer: Float = 0f

    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    override fun onAttackStart() {
        isCharging = false
        warningTimer = chargeWarningTime
    }

    override fun onAttackUpdate(deltaTime: Float) {
        if (!isCharging) {
            warningTimer -= deltaTime
            if (warningTimer <= 0) {
                startCharge()
            }
        } else {
            updateCharge(deltaTime)
        }
    }

    private fun startCharge() {
        isCharging = true
        val playerPos = boss?.getPlayerPosition() ?: return
        val bossPos = boss?.positionComponent ?: return

        chargeDirection = if (playerPos.first > bossPos.x) 1f else -1f
        boss?.setVelocityX(chargeDirection * chargeSpeed)
    }

    private fun updateCharge(deltaTime: Float) {
        val bossPos = boss?.positionComponent ?: return

        // Проверка, достигли ли края арены или прошло достаточно времени
        if (timeElapsed > duration * 0.5f) {
            boss?.setVelocityX(0f)
            finish()
        }
    }

    override fun onAttackEnd() {
        boss?.setVelocityX(0f)
        isCharging = false
    }

    override fun render(canvas: Canvas) {
        val bossPos = boss?.positionComponent ?: return
        if (!isCharging && warningTimer > 0) {
            // Рисуем предупреждение
            val indicatorWidth = 300f
            val indicatorHeight = 20f
            val rect = if (chargeDirection > 0) {
                RectF(bossPos.x, bossPos.y + 50f, bossPos.x + indicatorWidth, bossPos.y + 50f + indicatorHeight)
            } else {
                RectF(bossPos.x - indicatorWidth, bossPos.y + 50f, bossPos.x, bossPos.y + 50f + indicatorHeight)
            }
            paint.alpha = (255 * (warningTimer / chargeWarningTime)).toInt()
            canvas.drawRect(rect, paint)
        }
    }
}

/**
 * Паттерн: Slam - удар по земле с волной.
 */
class SlamPattern(
    damage: Int = 20,
    duration: Float = 2f,
    cooldown: Float = 5f
) : AttackPattern(PatternType.SLAM, damage, duration, cooldown, difficulty = 2) {

    private var shockwaveRadius: Float = 0f
    private val shockwaveSpeed: Float = 400f
    private var hasSlammed: Boolean = false
    private val slamTime: Float = 0.5f

    private val paint = Paint().apply {
        color = Color.rgb(255, 152, 0)
        style = Paint.Style.STROKE
        strokeWidth = 5f
        alpha = 150
    }

    override fun onAttackStart() {
        shockwaveRadius = 0f
        hasSlammed = false
    }

    override fun onAttackUpdate(deltaTime: Float) {
        if (!hasSlammed) {
            if (timeElapsed >= slamTime) {
                hasSlammed = true
                boss?.triggerGroundSlam()
            }
        } else {
            shockwaveRadius += shockwaveSpeed * deltaTime
            if (shockwaveRadius > 500f) {
                finish()
            }
        }
    }

    override fun onAttackEnd() {
        shockwaveRadius = 0f
    }

    override fun render(canvas: Canvas) {
        val bossPos = boss?.positionComponent ?: return
        if (hasSlammed && shockwaveRadius > 0) {
            canvas.drawCircle(bossPos.x, bossPos.y + 50f, shockwaveRadius, paint)
        }
    }
}

/**
 * Паттерн: Swipe - удар лапой/мечом.
 */
class SwipePattern(
    damage: Int = 12,
    duration: Float = 1f,
    cooldown: Float = 3f
) : AttackPattern(PatternType.SWIPE, damage, duration, cooldown, difficulty = 1) {

    private var hasSwiped: Boolean = false
    private val swipeTime: Float = 0.3f
    private var swipeAngle: Float = 0f
    private val swipeArc: Float = Math.toRadians(90.0).toFloat()

    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        alpha = 100
    }

    override fun onAttackStart() {
        hasSwiped = false
        swipeAngle = 0f
    }

    override fun onAttackUpdate(deltaTime: Float) {
        if (!hasSwiped) {
            if (timeElapsed >= swipeTime) {
                hasSwiped = true
                performSwipe()
            }
        } else {
            swipeAngle += 10f * deltaTime
            if (swipeAngle > swipeArc) {
                finish()
            }
        }
    }

    private fun performSwipe() {
        // Проверка коллизии с игроком
        val playerPos = boss?.getPlayerPosition() ?: return
        val bossPos = boss?.positionComponent ?: return

        val dx = playerPos.first - bossPos.x
        val dy = playerPos.second - bossPos.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        if (distance < 150f && abs(dy) < 100f) {
            boss?.dealDamageToPlayer(damage)
        }
    }

    override fun render(canvas: Canvas) {
        val bossPos = boss?.positionComponent ?: return
        if (hasSwiped) {
            canvas.drawArc(
                RectF(
                    bossPos.x - 150f,
                    bossPos.y - 100f,
                    bossPos.x + 150f,
                    bossPos.y + 100f
                ),
                -45f,
                Math.toDegrees(swipeAngle.toDouble()).toFloat(),
                true,
                paint
            )
        }
    }
}

/**
 * Паттерн: Tail Spin - вращение с хвостом.
 */
class TailSpinPattern(
    damage: Int = 18,
    duration: Float = 3f,
    cooldown: Float = 5f
) : AttackPattern(PatternType.TAIL_SPIN, damage, duration, cooldown, difficulty = 2) {

    private var tailAngle: Float = 0f
    private val tailSpeed: Float = 5f
    private val tailRadius: Float = 120f

    private val paint = Paint().apply {
        color = Color.rgb(156, 39, 176)
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    override fun onAttackStart() {
        tailAngle = 0f
    }

    override fun onAttackUpdate(deltaTime: Float) {
        tailAngle += tailSpeed * deltaTime
        if (tailAngle > 2 * PI) {
            tailAngle -= 2 * PI.toFloat()
        }

        // Проверка коллизии с игроком
        checkTailCollision()
    }

    private fun checkTailCollision() {
        val playerPos = boss?.getPlayerPosition() ?: return
        val bossPos = boss?.positionComponent ?: return

        val tailX = bossPos.x + kotlin.math.cos(tailAngle) * tailRadius
        val tailY = bossPos.y + kotlin.math.sin(tailAngle) * tailRadius

        val dx = playerPos.first - tailX
        val dy = playerPos.second - tailY
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        if (distance < 80f) {
            boss?.dealDamageToPlayer(damage)
        }
    }

    override fun render(canvas: Canvas) {
        val bossPos = boss?.positionComponent ?: return
        val tailX = bossPos.x + kotlin.math.cos(tailAngle) * tailRadius
        val tailY = bossPos.y + kotlin.math.sin(tailAngle) * tailRadius

        canvas.drawLine(bossPos.x, bossPos.y, tailX, tailY, paint)
    }
}

/**
 * Паттерн: Tail Swipe - удар хвостом.
 */
class TailSwipePattern(
    damage: Int = 16,
    duration: Float = 2f,
    cooldown: Float = 4f
) : AttackPattern(PatternType.TAIL_SWIPE, damage, duration, cooldown, difficulty = 2) {

    private var hasSwiped: Boolean = false
    private val swipeTime: Float = 0.5f
    private var swipeProgress: Float = 0f

    private val paint = Paint().apply {
        color = Color.rgb(255, 87, 34)
        style = Paint.Style.FILL
        alpha = 120
    }

    override fun onAttackStart() {
        hasSwiped = false
        swipeProgress = 0f
    }

    override fun onAttackUpdate(deltaTime: Float) {
        if (!hasSwiped) {
            if (timeElapsed >= swipeTime) {
                hasSwiped = true
                performTailSwipe()
            }
        } else {
            swipeProgress += 2f * deltaTime
            if (swipeProgress > 1f) {
                finish()
            }
        }
    }

    private fun performTailSwipe() {
        val playerPos = boss?.getPlayerPosition() ?: return
        val bossPos = boss?.positionComponent ?: return

        val dx = playerPos.first - bossPos.x
        val distance = kotlin.math.sqrt(dx * dx + (playerPos.second - bossPos.y) * (playerPos.second - bossPos.y))

        if (distance < 200f) {
            boss?.dealDamageToPlayer(damage)
        }
    }

    override fun render(canvas: Canvas) {
        val bossPos = boss?.positionComponent ?: return
        if (hasSwiped) {
            canvas.drawCircle(bossPos.x, bossPos.y, 150f * swipeProgress, paint)
        }
    }
}

/**
 * Паттерн: Sword Combo - комбо атака мечом.
 */
class SwordComboPattern(
    damage: Int = 25,
    duration: Float = 3f,
    cooldown: Float = 5f
) : AttackPattern(PatternType.SWORD_COMBO, damage, duration, cooldown, difficulty = 3) {

    private var comboStep: Int = 0
    private val comboTimes = listOf(0.5f, 1.2f, 2.0f)
    private val comboDamages = listOf(damage, damage, damage * 2)

    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    override fun onAttackStart() {
        comboStep = 0
    }

    override fun onAttackUpdate(deltaTime: Float) {
        if (comboStep < comboTimes.size) {
            if (timeElapsed >= comboTimes[comboStep]) {
                performComboStrike(comboStep)
                comboStep++
            }
        } else {
            finish()
        }
    }

    private fun performComboStrike(step: Int) {
        boss?.dealDamageToPlayer(comboDamages[step])
    }

    override fun render(canvas: Canvas) {
        val bossPos = boss?.positionComponent ?: return
        paint.alpha = 200
        canvas.drawLine(
            bossPos.x - 100f,
            bossPos.y,
            bossPos.x + 100f,
            bossPos.y,
            paint
        )
    }
}

/**
 * Паттерн: Shield Bash - удар щитом.
 */
class ShieldBashPattern(
    damage: Int = 20,
    duration: Float = 2f,
    cooldown: Float = 6f
) : AttackPattern(PatternType.SHIELD_BASH, damage, duration, cooldown, difficulty = 2) {

    private var hasBashed: Boolean = false
    private val bashTime: Float = 0.8f

    private val paint = Paint().apply {
        color = Color.rgb(96, 96, 96)
        style = Paint.Style.FILL
        alpha = 180
    }

    override fun onAttackStart() {
        hasBashed = false
    }

    override fun onAttackUpdate(deltaTime: Float) {
        if (!hasBashed) {
            if (timeElapsed >= bashTime) {
                hasBashed = true
                performShieldBash()
            }
        } else {
            if (timeElapsed > duration * 0.8f) {
                finish()
            }
        }
    }

    private fun performShieldBash() {
        val playerPos = boss?.getPlayerPosition() ?: return
        val bossPos = boss?.positionComponent ?: return

        val dx = playerPos.first - bossPos.x
        val distance = kotlin.math.sqrt(dx * dx + (playerPos.second - bossPos.y) * (playerPos.second - bossPos.y))

        if (distance < 120f && dx > 0) {
            boss?.dealDamageToPlayer(damage)
            // Отталкивание игрока
            boss?.knockbackPlayer(300f, -200f)
        }
    }

    override fun render(canvas: Canvas) {
        val bossPos = boss?.positionComponent ?: return
        if (hasBashed) {
            canvas.drawCircle(bossPos.x + 80f, bossPos.y, 100f, paint)
        }
    }
}

/**
 * Паттерн: Ground Pound - удар по земле.
 */
class GroundPoundPattern(
    damage: Int = 18,
    duration: Float = 2f,
    cooldown: Float = 5f
) : AttackPattern(PatternType.GROUND_POUND, damage, duration, cooldown, difficulty = 2) {

    private var hasPounded: Boolean = false
    private val poundTime: Float = 0.6f
    private var shockwaveRadius: Float = 0f
    private val shockwaveSpeed: Float = 350f

    private val paint = Paint().apply {
        color = Color.rgb(121, 85, 72)
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    override fun onAttackStart() {
        hasPounded = false
        shockwaveRadius = 0f
    }

    override fun onAttackUpdate(deltaTime: Float) {
        if (!hasPounded) {
            if (timeElapsed >= poundTime) {
                hasPounded = true
                boss?.triggerGroundSlam()
            }
        } else {
            shockwaveRadius += shockwaveSpeed * deltaTime
            if (shockwaveRadius > 400f) {
                finish()
            }
        }
    }

    override fun onAttackEnd() {
        shockwaveRadius = 0f
    }

    override fun render(canvas: Canvas) {
        val bossPos = boss?.positionComponent ?: return
        if (hasPounded && shockwaveRadius > 0) {
            paint.alpha = (200 * (1 - shockwaveRadius / 400f)).toInt()
            canvas.drawCircle(bossPos.x, bossPos.y + 80f, shockwaveRadius, paint)
        }
    }
}

/**
 * Паттерн: Jump Attack - прыжок с атакой.
 */
class JumpAttackPattern(
    damage: Int = 15,
    duration: Float = 3f,
    cooldown: Float = 4f
) : AttackPattern(PatternType.JUMP_ATTACK, damage, duration, cooldown, difficulty = 1) {

    private var hasJumped: Boolean = false
    private val jumpTime: Float = 0.5f
    private val landingTime: Float = 2f
    private var isLanding: Boolean = false

    override fun onAttackStart() {
        hasJumped = false
        isLanding = false
    }

    override fun onAttackUpdate(deltaTime: Float) {
        if (!hasJumped) {
            if (timeElapsed >= jumpTime) {
                hasJumped = true
                boss?.performJump()
            }
        } else if (!isLanding) {
            if (timeElapsed >= landingTime) {
                isLanding = true
                performLanding()
            }
        } else {
            if (timeElapsed > duration * 0.8f) {
                finish()
            }
        }
    }

    private fun performLanding() {
        boss?.triggerGroundSlam()
        boss?.dealDamageToArea(damage, 200f)
    }

    override fun onAttackEnd() {
        boss?.setVelocityY(0f)
    }
}
