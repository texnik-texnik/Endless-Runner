package com.endlessrunner.bosses

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.endlessrunner.bosses.visuals.BossVisuals

/**
 * Гигантский слизень - первый босс.
 * Механика: делится на мелких слизней, ядовитые облака, прыжки.
 */
class GiantSlimeBoss : Boss(BossType.GIANT_SLIME) {

    private val slimePaint = Paint().apply {
        color = Color.rgb(76, 175, 80)
        alpha = 180
    }

    private var jumpCooldown: Float = 0f
    private var splitCooldown: Float = 0f
    private var isJumping: Boolean = false
    private var jumpTargetX: Float = 0f
    private val jumpDuration: Float = 1.5f
    private var jumpTimer: Float = 0f

    override fun onActivate() {
        super.onActivate()
        jumpCooldown = 2f
        splitCooldown = 10f
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        if (isDead) return

        // Обновление кулдаунов
        if (jumpCooldown > 0) jumpCooldown -= deltaTime
        if (splitCooldown > 0) splitCooldown -= deltaTime

        // Обновление прыжка
        if (isJumping) {
            updateJump(deltaTime)
        }
    }

    override fun render(canvas: Canvas) {
        if (!isActive || isDead) return

        // Рендеринг тела слизня
        val pos = positionComponent ?: return

        // Анимация сжатия/растяжения
        val stretchFactor = if (isJumping) 1.3f else 1f
        val squashFactor = if (isJumping) 0.8f else 1f

        val width = bossType.width * squashFactor
        val height = bossType.height * stretchFactor

        canvas.drawOval(
            pos.x - width / 2,
            pos.y - height / 2,
            pos.x + width / 2,
            pos.y + height / 2,
            slimePaint
        )

        // Глаза
        val eyePaint = Paint().apply { color = Color.WHITE }
        val pupilPaint = Paint().apply { color = Color.BLACK }

        canvas.drawCircle(pos.x - 30f, pos.y - 20f, 15f, eyePaint)
        canvas.drawCircle(pos.x + 30f, pos.y - 20f, 15f, eyePaint)
        canvas.drawCircle(pos.x - 30f, pos.y - 20f, 8f, pupilPaint)
        canvas.drawCircle(pos.x + 30f, pos.y - 20f, 8f, pupilPaint)

        super.render(canvas)
    }

    private fun updateJump(deltaTime: Float) {
        jumpTimer -= deltaTime

        if (jumpTimer <= 0) {
            // Приземление
            isJumping = false
            positionComponent?.vy = 0f
            triggerGroundSlam()
            dealDamageToArea(damage, 150f)
        }
    }

    override fun onPhaseTransition(oldPhase: BossPhase?, newPhase: BossPhase) {
        super.onPhaseTransition(oldPhase, newPhase)

        when (newPhase) {
            is BossPhase.Phase2 -> {
                // Увеличение частоты прыжков
                jumpCooldown *= 0.7f
            }
            is BossPhase.Phase3 -> {
                // Максимальная агрессия
                jumpCooldown *= 0.5f
                splitCooldown *= 0.7f
            }
            else -> {}
        }
    }

    override fun executeAttack(pattern: AttackPattern) {
        super.executeAttack(pattern)

        when (pattern.type) {
            PatternType.JUMP_ATTACK -> {
                performSlimeJump()
            }
            PatternType.SLIME_SPLIT -> {
                performSplit()
            }
            PatternType.POISON_CLOUD -> {
                spawnPoisonClouds()
            }
            else -> {}
        }
    }

    private fun performSlimeJump() {
        if (isJumping) return

        val playerPos = getPlayerPosition()
        jumpTargetX = playerPos.first

        isJumping = true
        jumpTimer = jumpDuration
        positionComponent?.vy = -600f

        visuals.onJump(this)
    }

    private fun performSplit() {
        if (splitCooldown > 0) return

        // Разделение на мелких слизней
        spawnMinions(2)
        splitCooldown = 10f
    }

    private fun spawnPoisonClouds() {
        // Спавн ядовитых облаков вокруг арены
        visuals.onPoisonCloudSpawn(this)
    }

    override fun onShieldBroken() {
        // У слизня нет щита
    }

    override fun onDeath() {
        super.onDeath()
        visuals.onSlimeDeath(this)
    }
}

/**
 * Механический дракон - второй босс.
 * Механика: летает, периодически приземляется, ракетный залп, огненное дыхание.
 */
class MechDragonBoss : Boss(BossType.MECH_DRAGON) {

    private val dragonPaint = Paint().apply {
        color = Color.rgb(255, 87, 34)
        style = Paint.Style.FILL
    }

    private val wingPaint = Paint().apply {
        color = Color.rgb(255, 152, 0)
        alpha = 200
    }

    private var isFlying: Boolean = true
    private var flightHeight: Float = 200f
    private var wingAngle: Float = 0f
    private val wingSpeed: Float = 5f

    private var diveCooldown: Float = 0f
    private var breathCooldown: Float = 0f
    private var missileCooldown: Float = 0f

    override fun onActivate() {
        super.onActivate()
        diveCooldown = 5f
        breathCooldown = 7f
        missileCooldown = 10f
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        if (isDead) return

        // Обновление кулдаунов
        if (diveCooldown > 0) diveCooldown -= deltaTime
        if (breathCooldown > 0) breathCooldown -= deltaTime
        if (missileCooldown > 0) missileCooldown -= deltaTime

        // Обновление полёта
        updateFlight(deltaTime)

        // Обновление крыльев
        wingAngle += wingSpeed * deltaTime
    }

    override fun render(canvas: Canvas) {
        if (!isActive || isDead) return

        val pos = positionComponent ?: return

        // Тело дракона
        canvas.drawOval(
            pos.x - 100f,
            pos.y - flightHeight - 50f,
            pos.x + 100f,
            pos.y - flightHeight + 50f,
            dragonPaint
        )

        // Голова
        canvas.drawCircle(pos.x + 120f, pos.y - flightHeight - 30f, 60f, dragonPaint)

        // Крылья (анимация)
        val wingOffset = kotlin.math.sin(wingAngle) * 50f
        canvas.drawOval(
            pos.x - 80f,
            pos.y - flightHeight - 150f + wingOffset,
            pos.x - 20f,
            pos.y - flightHeight - 50f + wingOffset,
            wingPaint
        )
        canvas.drawOval(
            pos.x + 20f,
            pos.y - flightHeight - 150f - wingOffset,
            pos.x + 80f,
            pos.y - flightHeight - 50f - wingOffset,
            wingPaint
        )

        // Хвост
        canvas.drawOval(
            pos.x - 180f,
            pos.y - flightHeight - 30f,
            pos.x - 80f,
            pos.y - flightHeight + 30f,
            dragonPaint
        )

        super.render(canvas)
    }

    private fun updateFlight(deltaTime: Float) {
        if (isFlying) {
            // Полёт над ареной
            positionComponent?.y = 400f
        }
    }

    override fun executeAttack(pattern: AttackPattern) {
        super.executeAttack(pattern)

        when (pattern.type) {
            PatternType.FIRE_BREATH -> {
                performFireBreath()
            }
            PatternType.TAIL_SWIPE -> {
                performTailSwipe()
            }
            PatternType.MISSILE_STORM -> {
                launchMissiles()
            }
            PatternType.LASER_BEAM -> {
                chargeLaser()
            }
            else -> {}
        }
    }

    private fun performFireBreath() {
        if (breathCooldown > 0) return

        // Огненное дыхание в направлении игрока
        breathCooldown = 7f
    }

    private fun performTailSwipe() {
        // Удар хвостом при приземлении
        visuals.onTailSwipe(this)
    }

    private fun launchMissiles() {
        if (missileCooldown > 0) return

        // Ракетный залп
        missileCooldown = 10f
    }

    private fun chargeLaser() {
        // Зарядка лазерного луча
        visuals.onLaserCharge(this)
    }

    override fun onPhaseTransition(oldPhase: BossPhase?, newPhase: BossPhase) {
        super.onPhaseTransition(oldPhase, newPhase)

        when (newPhase) {
            is BossPhase.Phase2 -> {
                // Больше ракет
                missileCooldown *= 0.8f
            }
            is BossPhase.Phase3 -> {
                // Лазерный луч доступен
                diveCooldown *= 0.6f
            }
            else -> {}
        }
    }

    override fun onDeath() {
        super.onDeath()
        visuals.onMechExplosion(this)
    }
}

/**
 * Тёмный рыцарь - третий босс.
 * Механика: щит (нужно ломать), телепортация, комбо атаки мечом.
 */
class DarkKnightBoss : Boss(BossType.DARK_KNIGHT) {

    private val armorPaint = Paint().apply {
        color = Color.rgb(156, 39, 176)
        style = Paint.Style.FILL
    }

    private val shieldPaint = Paint().apply {
        color = Color.rgb(96, 96, 96)
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val swordPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private var hasActiveShield: Boolean = false
    private var teleportCooldown: Float = 0f
    private var comboStep: Int = 0

    override fun onActivate() {
        super.onActivate()
        teleportCooldown = 6f
        hasActiveShield = phase.hasShield
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        if (isDead) return

        if (teleportCooldown > 0) teleportCooldown -= deltaTime

        // Обновление состояния щита
        hasActiveShield = shieldHealth > 0
    }

    override fun render(canvas: Canvas) {
        if (!isActive || isDead) return

        val pos = positionComponent ?: return

        // Тело рыцаря
        canvas.drawRect(
            pos.x - 60f,
            pos.y - 100f,
            pos.x + 60f,
            pos.y + 100f,
            armorPaint
        )

        // Голова (шлем)
        canvas.drawCircle(pos.x, pos.y - 120f, 40f, armorPaint)

        // Щит
        if (hasActiveShield) {
            val shieldPercent = getShieldPercent()
            shieldPaint.alpha = (150 + 105 * shieldPercent).toInt()

            canvas.drawOval(
                pos.x + 40f,
                pos.y - 80f,
                pos.x + 120f,
                pos.y + 80f,
                shieldPaint
            )
        }

        // Меч
        canvas.drawLine(
            pos.x - 40f,
            pos.y + 50f,
            pos.x - 40f,
            pos.y - 80f,
            swordPaint
        )

        super.render(canvas)
    }

    override fun executeAttack(pattern: AttackPattern) {
        super.executeAttack(pattern)

        when (pattern.type) {
            PatternType.SWORD_COMBO -> {
                performSwordCombo()
            }
            PatternType.SHIELD_BASH -> {
                performShieldBash()
            }
            PatternType.DARK_ORBS -> {
                summonDarkOrbs()
            }
            PatternType.TELEPORT_STRIKE -> {
                performTeleportStrike()
            }
            else -> {}
        }
    }

    private fun performSwordCombo() {
        // Комбо атака мечом (3 удара)
        comboStep = 0
    }

    private fun performShieldBash() {
        if (!hasActiveShield) return

        // Удар щитом с отталкиванием
        visuals.onShieldBash(this)
    }

    private fun summonDarkOrbs() {
        // Призыв тёмных сфер
        visuals.onDarkOrbsSpawn(this)
    }

    private fun performTeleportStrike() {
        if (teleportCooldown > 0) return

        // Телепортация за спину игрока и удар
        val playerPos = getPlayerPosition()
        teleportTo(playerPos.first + 200f, positionComponent?.y ?: 600f)
        teleportCooldown = 6f

        dealDamageToArea(damage, 150f)
        visuals.onTeleport(this, positionComponent?.x ?: 0f, positionComponent?.y ?: 0f)
    }

    override fun onShieldBroken() {
        super.onShieldBroken()
        hasActiveShield = false
        // Рыцарь становится агрессивнее без щита
        phaseSpeedMultiplier *= 1.3f
    }

    override fun onPhaseTransition(oldPhase: BossPhase?, newPhase: BossPhase) {
        super.onPhaseTransition(oldPhase, newPhase)

        when (newPhase) {
            is BossPhase.Phase3 -> {
                // Потеря щита, увеличение скорости
                hasActiveShield = false
                shieldHealth = 0
                phaseSpeedMultiplier *= 1.5f
            }
            else -> {}
        }
    }

    override fun onDeath() {
        super.onDeath()
        visuals.onKnightDeath(this)
    }
}

/**
 * Страж пустоты - четвертый босс.
 * Механика: порталы, телепортация, гравитация, чёрные дыры, метеоры.
 */
class VoidGuardianBoss : Boss(BossType.VOID_GUARDIAN) {

    private val voidPaint = Paint().apply {
        color = Color.rgb(103, 58, 183)
        style = Paint.Style.FILL
        alpha = 200
    }

    private val portalPaint = Paint().apply {
        color = Color.rgb(171, 71, 188)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        alpha = 150
    }

    private var portalCooldown: Float = 0f
    private var blackHoleCooldown: Float = 0f
    private var activePortals: MutableList<Pair<Float, Float>> = mutableListOf()

    override fun onActivate() {
        super.onActivate()
        portalCooldown = 8f
        blackHoleCooldown = 10f
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        if (isDead) return

        if (portalCooldown > 0) portalCooldown -= deltaTime
        if (blackHoleCooldown > 0) blackHoleCooldown -= deltaTime
    }

    override fun render(canvas: Canvas) {
        if (!isActive || isDead) return

        val pos = positionComponent ?: return

        // Тело стража (призрачная форма)
        canvas.drawOval(
            pos.x - 80f,
            pos.y - 120f,
            pos.x + 80f,
            pos.y + 120f,
            voidPaint
        )

        // Глаза
        val eyePaint = Paint().apply { color = Color.rgb(224, 64, 251) }
        canvas.drawCircle(pos.x - 30f, pos.y - 40f, 15f, eyePaint)
        canvas.drawCircle(pos.x + 30f, pos.y - 40f, 15f, eyePaint)

        // Аура
        val auraPaint = Paint().apply {
            color = Color.rgb(103, 58, 183)
            alpha = 50
        }
        canvas.drawCircle(pos.x, pos.y, 150f, auraPaint)

        // Порталы
        activePortals.forEach { (px, py) ->
            canvas.drawCircle(px, py, 60f, portalPaint)
        }

        super.render(canvas)
    }

    override fun executeAttack(pattern: AttackPattern) {
        super.executeAttack(pattern)

        when (pattern.type) {
            PatternType.VOID_BEAM -> {
                fireVoidBeam()
            }
            PatternType.TELEPORT_STRIKE -> {
                performVoidTeleport()
            }
            PatternType.BLACK_HOLE -> {
                createVoidBlackHole()
            }
            PatternType.METEOR_STRIKE -> {
                summonVoidMeteors()
            }
            else -> {}
        }
    }

    private fun fireVoidBeam() {
        // Луч пустоты через всю арену
        visuals.onVoidBeam(this)
    }

    private fun performVoidTeleport() {
        // Телепортация через порталы
        val playerPos = getPlayerPosition()
        if (activePortals.isNotEmpty()) {
            val portal = activePortals.random()
            teleportTo(portal.first, portal.second)
        } else {
            teleportTo(playerPos.first - 200f, positionComponent?.y ?: 600f)
        }
    }

    private fun createVoidBlackHole() {
        if (blackHoleCooldown > 0) return

        createBlackHole()
        blackHoleCooldown = 10f
    }

    private fun summonVoidMeteors() {
        // Метеоры из пустоты
        visuals.onVoidMeteors(this)
    }

    override fun onPhaseTransition(oldPhase: BossPhase?, newPhase: BossPhase) {
        super.onPhaseTransition(oldPhase, newPhase)

        when (newPhase) {
            is BossPhase.Phase2 -> {
                // Больше порталов
                spawnPortal()
            }
            is BossPhase.Phase3 -> {
                // Метеоры доступны
                activePortals.clear()
                spawnPortal()
                spawnPortal()
            }
            else -> {}
        }
    }

    private fun spawnPortal() {
        val pos = positionComponent ?: return
        val portalX = pos.x + (Math.random().toFloat() * 800 - 400)
        val portalY = pos.y + (Math.random().toFloat() * 400 - 200)
        activePortals.add(Pair(portalX, portalY))
    }

    override fun onDeath() {
        super.onDeath()
        visuals.onVoidCollapse(this)
        activePortals.clear()
    }
}

/**
 * Финальный босс.
 * Механика: меняет форму (слим → дракон → рыцарь → истинная форма), комбинирует все атаки.
 */
class FinalBossBoss : Boss(BossType.FINAL_BOSS) {

    private val formPaint = Paint().apply {
        color = Color.rgb(216, 27, 96)
        style = Paint.Style.FILL
    }

    private var currentForm: BossForm = BossForm.SLIME
    private var formChangeCooldown: Float = 0f
    private var trueFormUnlocked: Boolean = false

    enum class BossForm {
        SLIME,
        DRAGON,
        KNIGHT,
        TRUE_FORM
    }

    override fun onActivate() {
        super.onActivate()
        formChangeCooldown = 0f
        trueFormUnlocked = false
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        if (isDead) return

        if (formChangeCooldown > 0) formChangeCooldown -= deltaTime
    }

    override fun render(canvas: Canvas) {
        if (!isActive || isDead) return

        val pos = positionComponent ?: return

        // Отрисовка в зависимости от формы
        when (currentForm) {
            BossForm.SLIME -> renderSlimeForm(canvas, pos)
            BossForm.DRAGON -> renderDragonForm(canvas, pos)
            BossForm.KNIGHT -> renderKnightForm(canvas, pos)
            BossForm.TRUE_FORM -> renderTrueForm(canvas, pos)
        }

        super.render(canvas)
    }

    private fun renderSlimeForm(canvas: Canvas, pos: PositionComponent) {
        canvas.drawOval(
            pos.x - 80f,
            pos.y - 60f,
            pos.x + 80f,
            pos.y + 60f,
            formPaint
        )
    }

    private fun renderDragonForm(canvas: Canvas, pos: PositionComponent) {
        canvas.drawOval(
            pos.x - 100f,
            pos.y - 50f,
            pos.x + 100f,
            pos.y + 50f,
            formPaint
        )
    }

    private fun renderKnightForm(canvas: Canvas, pos: PositionComponent) {
        canvas.drawRect(
            pos.x - 50f,
            pos.y - 100f,
            pos.x + 50f,
            pos.y + 100f,
            formPaint
        )
    }

    private fun renderTrueForm(canvas: Canvas, pos: PositionComponent) {
        // Истинная форма - сияющая аура
        val auraPaint = Paint().apply {
            color = Color.rgb(255, 215, 0)
            alpha = 100
        }
        canvas.drawCircle(pos.x, pos.y, 200f, auraPaint)
        canvas.drawOval(
            pos.x - 100f,
            pos.y - 120f,
            pos.x + 100f,
            pos.y + 120f,
            formPaint
        )
    }

    override fun executeAttack(pattern: AttackPattern) {
        super.executeAttack(pattern)

        // Атаки зависят от текущей формы
        when (currentForm) {
            BossForm.SLIME -> executeSlimeAttacks(pattern)
            BossForm.DRAGON -> executeDragonAttacks(pattern)
            BossForm.KNIGHT -> executeKnightAttacks(pattern)
            BossForm.TRUE_FORM -> executeTrueFormAttacks(pattern)
        }
    }

    private fun executeSlimeAttacks(pattern: AttackPattern) {
        when (pattern.type) {
            PatternType.JUMP_ATTACK, PatternType.SLIME_SPLIT, PatternType.POISON_CLOUD -> {
                super.executeAttack(pattern)
            }
            else -> {}
        }
    }

    private fun executeDragonAttacks(pattern: AttackPattern) {
        when (pattern.type) {
            PatternType.FIRE_BREATH, PatternType.TAIL_SWIPE, PatternType.MISSILE_STORM -> {
                super.executeAttack(pattern)
            }
            else -> {}
        }
    }

    private fun executeKnightAttacks(pattern: AttackPattern) {
        when (pattern.type) {
            PatternType.SWORD_COMBO, PatternType.SHIELD_BASH, PatternType.DARK_ORBS -> {
                super.executeAttack(pattern)
            }
            else -> {}
        }
    }

    private fun executeTrueFormAttacks(pattern: AttackPattern) {
        // Все атаки доступны
        super.executeAttack(pattern)
    }

    override fun onPhaseTransition(oldPhase: BossPhase?, newPhase: BossPhase) {
        super.onPhaseTransition(oldPhase, newPhase)

        when (newPhase) {
            is BossPhase.Phase2 -> {
                // Смена формы на дракона
                changeForm(BossForm.DRAGON)
            }
            is BossPhase.Phase3 -> {
                // Смена формы на рыцаря
                changeForm(BossForm.KNIGHT)
            }
            is BossPhase.Phase4 -> {
                // Истинная форма
                changeForm(BossForm.TRUE_FORM)
                trueFormUnlocked = true
            }
            else -> {}
        }
    }

    private fun changeForm(newForm: BossForm) {
        currentForm = newForm
        formChangeCooldown = 2f
        visuals.onFormChange(this, newForm)

        // Неуязвимость во время трансформации
        isInvulnerable = true
        transitionTimer = formChangeCooldown
    }

    override fun onDeath() {
        super.onDeath()
        visuals.onFinalBossDeath(this)
    }
}
