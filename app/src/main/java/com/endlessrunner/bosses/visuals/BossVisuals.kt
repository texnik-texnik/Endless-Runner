package com.endlessrunner.bosses.visuals

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Shader.TileMode
import com.endlessrunner.bosses.Boss
import com.endlessrunner.bosses.BossPhase
import com.endlessrunner.bosses.BossType
import com.endlessrunner.bosses.ConcreteBosses.FinalBossBoss
import kotlin.math.cos
import kotlin.math.sin

/**
 * Визуальные эффекты для босса.
 * Управляет всеми визуальными эффектами: аура, удары, переходы фаз, смерть.
 */
class BossVisuals {

    // ============================================================================
    // ЭФФЕКТЫ
    // ============================================================================

    /** Аура вокруг босса */
    private var auraEnabled: Boolean = false
    private var auraRadius: Float = 0f
    private var auraColor: Int = Color.WHITE
    private var auraPulseSpeed: Float = 2f
    private var auraTimer: Float = 0f

    /** Эффект получения урона */
    private var hitEffectEnabled: Boolean = false
    private var hitEffectTimer: Float = 0f
    private var hitEffectColor: Int = Color.WHITE
    private val hitEffectDuration: Float = 0.3f

    /** Эффект зарядки атаки */
    private var chargeEffectEnabled: Boolean = false
    private var chargeEffectTimer: Float = 0f
    private var chargeEffectIntensity: Float = 0f
    private val chargeEffectDuration: Float = 1.5f

    /** Эффект перехода фазы */
    private var phaseTransitionEnabled: Boolean = false
    private var phaseTransitionTimer: Float = 0f
    private val phaseTransitionDuration: Float = 2f

    /** Эффект смерти */
    private var deathEffectEnabled: Boolean = false
    private var deathEffectTimer: Float = 0f
    private val deathEffectDuration: Float = 3f

    /** Эффект щита */
    private var shieldEffectEnabled: Boolean = false
    private var shieldHealth: Float = 1f

    // ============================================================================
    // PAINTS
    // ============================================================================

    private val auraPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val hitPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    private val chargePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val phaseTransitionPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    private val deathPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    private val shieldPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        alpha = 150
    }

    // ============================================================================
    // ИНИЦИАЛИЗАЦИЯ
    // ============================================================================

    /**
     * Вызывается при активации босса.
     */
    fun onBossActivated(boss: Boss) {
        auraEnabled = true
        auraRadius = boss.bossType.width
        auraColor = boss.bossType.debugColor
        auraTimer = 0f
    }

    /**
     * Обновление эффектов.
     */
    fun update(deltaTime: Float) {
        // Обновление ауры
        if (auraEnabled) {
            auraTimer += deltaTime
        }

        // Обновление эффекта удара
        if (hitEffectEnabled) {
            hitEffectTimer -= deltaTime
            if (hitEffectTimer <= 0) {
                hitEffectEnabled = false
            }
        }

        // Обновление эффекта зарядки
        if (chargeEffectEnabled) {
            chargeEffectTimer -= deltaTime
            chargeEffectIntensity = 1f - (chargeEffectTimer / chargeEffectDuration)
            if (chargeEffectTimer <= 0) {
                chargeEffectEnabled = false
            }
        }

        // Обновление перехода фазы
        if (phaseTransitionEnabled) {
            phaseTransitionTimer -= deltaTime
            if (phaseTransitionTimer <= 0) {
                phaseTransitionEnabled = false
            }
        }

        // Обновление эффекта смерти
        if (deathEffectEnabled) {
            deathEffectTimer -= deltaTime
            if (deathEffectTimer <= 0) {
                deathEffectEnabled = false
            }
        }
    }

    /**
     * Отрисовка эффектов.
     */
    fun render(canvas: Canvas, boss: Boss) {
        val pos = boss.positionComponent ?: return

        // Аура
        if (auraEnabled) {
            renderAura(canvas, pos)
        }

        // Эффект удара
        if (hitEffectEnabled) {
            renderHitEffect(canvas, pos)
        }

        // Эффект зарядки
        if (chargeEffectEnabled) {
            renderChargeEffect(canvas, pos)
        }

        // Эффект перехода фазы
        if (phaseTransitionEnabled) {
            renderPhaseTransition(canvas, pos)
        }

        // Эффект смерти
        if (deathEffectEnabled) {
            renderDeathEffect(canvas, pos)
        }

        // Эффект щита
        if (shieldEffectEnabled) {
            renderShieldEffect(canvas, pos)
        }
    }

    // ============================================================================
    // ОТРИСОВКА ЭФФЕКТОВ
    // ============================================================================

    private fun renderAura(canvas: Canvas, pos: com.endlessrunner.components.PositionComponent) {
        val pulseRadius = auraRadius + kotlin.math.sin(auraTimer * auraPulseSpeed) * 20f

        auraPaint.color = auraColor
        auraPaint.alpha = (100 + kotlin.math.sin(auraTimer * auraPulseSpeed) * 50).toInt()
        auraPaint.setShader(
            RadialGradient(
                pos.x,
                pos.y,
                pulseRadius,
                intArrayOf(auraColor, Color.TRANSPARENT),
                floatArrayOf(0.5f, 1f),
                TileMode.CLAMP
            )
        )

        canvas.drawCircle(pos.x, pos.y, pulseRadius, auraPaint)
        auraPaint.setShader(null)
    }

    private fun renderHitEffect(canvas: Canvas, pos: com.endlessrunner.components.PositionComponent) {
        val alpha = (255 * (hitEffectTimer / hitEffectDuration)).toInt()
        hitPaint.color = hitEffectColor
        hitPaint.alpha = alpha

        canvas.drawCircle(pos.x, pos.y, boss.bossType.width * 0.6f, hitPaint)
    }

    private fun renderChargeEffect(canvas: Canvas, pos: com.endlessrunner.components.PositionComponent) {
        val intensity = chargeEffectIntensity
        val radius = boss.bossType.width * (0.5f + intensity * 0.5f)

        chargePaint.color = Color.RED
        chargePaint.alpha = (200 * intensity).toInt()

        // Спиральный эффект
        for (i in 0..8) {
            val angle = (i / 8f) * 360 + intensity * 180
            val x = pos.x + kotlin.math.cos(Math.toRadians(angle)) * radius
            val y = pos.y + kotlin.math.sin(Math.toRadians(angle)) * radius
            canvas.drawCircle(x, y, 10f * intensity, chargePaint)
        }
    }

    private fun renderPhaseTransition(canvas: Canvas, pos: com.endlessrunner.components.PositionComponent) {
        val progress = 1f - (phaseTransitionTimer / phaseTransitionDuration)
        val alpha = (255 * kotlin.math.sin(progress * Math.PI)).toInt()

        phaseTransitionPaint.color = Color.rgb(255, 215, 0)
        phaseTransitionPaint.alpha = alpha

        val radius = boss.bossType.width * (0.5f + progress * 2f)
        canvas.drawCircle(pos.x, pos.y, radius, phaseTransitionPaint)
    }

    private fun renderDeathEffect(canvas: Canvas, pos: com.endlessrunner.components.PositionComponent) {
        val progress = 1f - (deathEffectTimer / deathEffectDuration)

        // Исчезновение
        deathPaint.color = boss.bossType.debugColor
        deathPaint.alpha = (255 * (1f - progress)).toInt()

        // Частицы (упрощённо)
        for (i in 0..12) {
            val angle = (i / 12f) * 360 + progress * 360
            val distance = progress * 300f
            val x = pos.x + kotlin.math.cos(Math.toRadians(angle)) * distance
            val y = pos.y + kotlin.math.sin(Math.toRadians(angle)) * distance
            canvas.drawCircle(x, y, 20f * (1f - progress), deathPaint)
        }
    }

    private fun renderShieldEffect(canvas: Canvas, pos: com.endlessrunner.components.PositionComponent) {
        val alpha = (150 * shieldHealth).toInt()
        shieldPaint.color = Color.rgb(100, 150, 255)
        shieldPaint.alpha = alpha

        canvas.drawOval(
            pos.x - boss.bossType.width * 0.7f,
            pos.y - boss.bossType.height * 0.7f,
            pos.x + boss.bossType.width * 0.7f,
            pos.y + boss.bossType.height * 0.7f,
            shieldPaint
        )
    }

    // ============================================================================
    // СОБЫТИЯ
    // ============================================================================

    /**
     * Вызывается при смене фазы.
     */
    fun onPhaseChange(boss: Boss, newPhase: BossPhase) {
        // Изменение цвета ауры
        auraColor = when (newPhase) {
            is BossPhase.Phase1 -> boss.bossType.debugColor
            is BossPhase.Phase2 -> Color.rgb(255, 152, 0)
            is BossPhase.Phase3 -> Color.rgb(255, 0, 0)
            is BossPhase.Phase4 -> Color.rgb(255, 215, 0)
            is BossPhase.Enraged -> Color.rgb(150, 0, 0)
        }

        // Увеличение пульсации
        auraPulseSpeed = when (newPhase) {
            is BossPhase.Phase1 -> 2f
            is BossPhase.Phase2 -> 3f
            is BossPhase.Phase3 -> 4f
            is BossPhase.Phase4 -> 5f
            is BossPhase.Enraged -> 8f
        }
    }

    /**
     * Вызывается при начале перехода фазы.
     */
    fun onPhaseTransitionStart(boss: Boss, oldPhase: BossPhase, newPhase: BossPhase) {
        phaseTransitionEnabled = true
        phaseTransitionTimer = phaseTransitionDuration
    }

    /**
     * Вызывается при завершении перехода фазы.
     */
    fun onPhaseTransitionEnd(boss: Boss, newPhase: BossPhase) {
        phaseTransitionEnabled = false
    }

    /**
     * Вызывается при получении урона.
     */
    fun onDamageTaken(boss: Boss, amount: Int) {
        hitEffectEnabled = true
        hitEffectTimer = hitEffectDuration
        hitEffectColor = Color.WHITE
    }

    /**
     * Вызывается при ударе по щиту.
     */
    fun onShieldHit(boss: Boss, remainingShield: Int) {
        hitEffectEnabled = true
        hitEffectTimer = hitEffectDuration
        hitEffectColor = Color.rgb(100, 150, 255)
    }

    /**
     * Вызывается при разрушении щита.
     */
    fun onShieldBroken(boss: Boss) {
        shieldEffectEnabled = false
        // Эффект разрушения щита
        phaseTransitionEnabled = true
        phaseTransitionTimer = 1f
    }

    /**
     * Вызывается при начале ярости.
     */
    fun onEnrageStart(boss: Boss) {
        auraColor = Color.rgb(150, 0, 0)
        auraPulseSpeed = 8f
        auraRadius = boss.bossType.width * 1.5f
    }

    /**
     * Вызывается при начале зарядки атаки.
     */
    fun onAttackCharge(boss: Boss) {
        chargeEffectEnabled = true
        chargeEffectTimer = chargeEffectDuration
        chargeEffectIntensity = 0f
    }

    /**
     * Вызывается при прыжке.
     */
    fun onJump(boss: Boss) {
        // Эффект пыли при прыжке
    }

    /**
     * Вызывается при ударе по земле.
     */
    fun onGroundSlam(boss: Boss) {
        // Эффект ударной волны
    }

    /**
     * Вызывается при телепортации.
     */
    fun onTeleport(boss: Boss, x: Float, y: Float) {
        // Эффект исчезновения/появления
    }

    /**
     * Вызывается при начале лазерной атаки.
     */
    fun onLaserCharge(boss: Boss) {
        chargeEffectEnabled = true
        chargeEffectTimer = 2f
    }

    /**
     * Вызывается при начале атаки ударной волной.
     */
    fun onShockwave(boss: Boss) {
        // Эффект ударной волны
    }

    /**
     * Вызывается при начале чёрной дыры.
     */
    fun onBlackHoleStart(boss: Boss) {
        auraColor = Color.BLACK
        auraPulseSpeed = 1f
    }

    /**
     * Вызывается при завершении чёрной дыры.
     */
    fun onBlackHoleEnd(boss: Boss) {
        auraColor = boss.bossType.debugColor
        auraPulseSpeed = 2f
    }

    /**
     * Вызывается при начале замедления времени.
     */
    fun onTimeSlowStart(boss: Boss, factor: Float) {
        // Эффект искажения
    }

    /**
     * Вызывается при завершении замедления времени.
     */
    fun onTimeSlowEnd(boss: Boss) {
        // Нормализация
    }

    /**
     * Вызывается при смерти босса.
     */
    fun onDeath(boss: Boss) {
        deathEffectEnabled = true
        deathEffectTimer = deathEffectDuration
        auraEnabled = false
    }

    /**
     * Вызывается при смене формы (финальный босс).
     */
    fun onFormChange(boss: Boss, form: FinalBossBoss.BossForm) {
        phaseTransitionEnabled = true
        phaseTransitionTimer = phaseTransitionDuration

        auraColor = when (form) {
            FinalBossBoss.BossForm.SLIME -> Color.rgb(76, 175, 80)
            FinalBossBoss.BossForm.DRAGON -> Color.rgb(255, 87, 34)
            FinalBossBoss.BossForm.KNIGHT -> Color.rgb(156, 39, 176)
            FinalBossBoss.BossForm.TRUE_FORM -> Color.rgb(255, 215, 0)
        }
    }

    /**
     * Вызывается при появлении ядовитых облаков.
     */
    fun onPoisonCloudSpawn(boss: Boss) {
        // Зелёное свечение
    }

    /**
     * Вызывается при ударе хвостом.
     */
    fun onTailSwipe(boss: Boss) {
        // Эффект удара хвостом
    }

    /**
     * Вызывается при ударе щитом.
     */
    fun onShieldBash(boss: Boss) {
        // Эффект удара щитом
    }

    /**
     * Вызывается при появлении тёмных сфер.
     */
    fun onDarkOrbsSpawn(boss: Boss) {
        // Тёмное свечение
    }

    /**
     * Вызывается при смерти слизня.
     */
    fun onSlimeDeath(boss: Boss) {
        deathEffectEnabled = true
        deathEffectTimer = deathEffectDuration
        // Брызги
    }

    /**
     * Вызывается при взрыве меха.
     */
    fun onMechExplosion(boss: Boss) {
        deathEffectEnabled = true
        deathEffectTimer = deathEffectDuration * 1.5f
        // Большой взрыв
    }

    /**
     * Вызывается при смерти рыцаря.
     */
    fun onKnightDeath(boss: Boss) {
        deathEffectEnabled = true
        deathEffectTimer = deathEffectDuration
        // Падение на колени
    }

    /**
     * Вызывается при схлопывании пустоты.
     */
    fun onVoidCollapse(boss: Boss) {
        deathEffectEnabled = true
        deathEffectTimer = deathEffectDuration * 2f
        // Схлопывание в чёрную дыру
    }

    /**
     * Вызывается при смерти финального босса.
     */
    fun onFinalBossDeath(boss: Boss) {
        deathEffectEnabled = true
        deathEffectTimer = deathEffectDuration * 3f
        // Эпичная смерть
    }

    /**
     * Вызывается при начале боя.
     */
    fun onFightStart(boss: Boss) {
        auraEnabled = true
    }

    /**
     * Сброс эффектов.
     */
    fun reset() {
        auraEnabled = false
        hitEffectEnabled = false
        chargeEffectEnabled = false
        phaseTransitionEnabled = false
        deathEffectEnabled = false
        shieldEffectEnabled = false
    }

    // ============================================================================
    // УТИЛИТЫ
    // ============================================================================

    /**
     * Проверка, активен ли эффект смерти.
     */
    fun isDeathActive(): Boolean = deathEffectEnabled

    /**
     * Проверка, активен ли эффект перехода.
     */
    fun isTransitionActive(): Boolean = phaseTransitionEnabled
}
