package com.endlessrunner.bosses.attacks

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.endlessrunner.bosses.Boss
import com.endlessrunner.bosses.PatternType
import com.endlessrunner.bosses.minions.MinionSpawner
import com.endlessrunner.bosses.minions.MinionType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Паттерн: Summon Minions - призыв миньонов.
 */
class SummonMinionsPattern(
    damage: Int = 0,
    duration: Float = 3f,
    cooldown: Float = 15f
) : AttackPattern(PatternType.SUMMON_MINIONS, damage, duration, cooldown, difficulty = 2) {

    private var hasSummoned: Boolean = false
    private val summonTime: Float = 1.5f
    private var minionSpawner: MinionSpawner? = null

    override fun initialize(boss: Boss) {
        super.initialize(boss)
        minionSpawner = boss.minionSpawner
    }

    override fun onAttackStart() {
        hasSummoned = false
    }

    override fun onAttackUpdate(deltaTime: Float) {
        if (!hasSummoned) {
            if (timeElapsed >= summonTime) {
                hasSummoned = true
                summonMinions()
                finish()
            }
        }
    }

    private fun summonMinions() {
        val bossPos = boss?.positionComponent ?: return
        minionSpawner?.spawnWave(
            count = 3,
            positionX = bossPos.x,
            positionY = bossPos.y,
            type = MinionType.DARK_SPAWN
        )
    }

    override fun onAttackEnd() {}
}

/**
 * Паттерн: Laser Beam - луч через всю арену.
 */
class LaserBeamPattern(
    damage: Int = 30,
    duration: Float = 4f,
    cooldown: Float = 8f
) : AttackPattern(PatternType.LASER_BEAM, damage, duration, cooldown, difficulty = 4) {

    private var isFiring: Boolean = false
    private val chargeTime: Float = 1.5f
    private val fireDuration: Float = 2f
    private var beamAngle: Float = 0f
    private val beamWidth: Float = 40f

    private val chargePaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 3f
        alpha = 100
    }

    private val beamPaint = Paint().apply {
        color = Color.rgb(255, 0, 100)
        style = Paint.Style.FILL
        alpha = 200
    }

    override fun onAttackStart() {
        isFiring = false
        beamAngle = 0f
    }

    override fun onAttackUpdate(deltaTime: Float) {
        if (!isFiring) {
            if (timeElapsed >= chargeTime) {
                isFiring = true
                startLaser()
            }
        } else {
            beamAngle += 0.5f * deltaTime
            checkLaserCollision()

            if (timeElapsed >= chargeTime + fireDuration) {
                finish()
            }
        }
    }

    private fun startLaser() {
        boss?.triggerLaserAttack()
    }

    private fun checkLaserCollision() {
        val playerPos = boss?.getPlayerPosition() ?: return
        val bossPos = boss?.positionComponent ?: return

        // Упрощённая проверка - если игрок в зоне луча
        val dx = playerPos.first - bossPos.x
        val dy = playerPos.second - bossPos.y

        if (kotlin.math.abs(dy) < beamWidth && dx > 0) {
            boss?.dealDamageToPlayer(damage)
        }
    }

    override fun render(canvas: Canvas) {
        val bossPos = boss?.positionComponent ?: return

        if (!isFiring && timeElapsed < chargeTime) {
            // Индикатор зарядки
            val progress = timeElapsed / chargeTime
            canvas.drawLine(
                bossPos.x,
                bossPos.y,
                bossPos.x + 1000f * progress,
                bossPos.y,
                chargePaint
            )
        } else if (isFiring) {
            // Луч
            canvas.drawRect(
                RectF(
                    bossPos.x,
                    bossPos.y - beamWidth / 2,
                    bossPos.x + 1500f,
                    bossPos.y + beamWidth / 2
                ),
                beamPaint
            )
        }
    }

    override fun onAttackEnd() {
        isFiring = false
    }
}

/**
 * Паттерн: Teleport Strike - телепортация + удар.
 */
class TeleportStrikePattern(
    damage: Int = 20,
    duration: Float = 3f,
    cooldown: Float = 6f
) : AttackPattern(PatternType.TELEPORT_STRIKE, damage, duration, cooldown, difficulty = 3) {

    private var hasTeleported: Boolean = false
    private val teleportTime: Float = 0.5f
    private var hasStruck: Boolean = false
    private val strikeTime: Float = 1.5f

    private val teleportPaint = Paint().apply {
        color = Color.rgb(103, 58, 183)
        style = Paint.Style.FILL
        alpha = 150
    }

    override fun onAttackStart() {
        hasTeleported = false
        hasStruck = false
    }

    override fun onAttackUpdate(deltaTime: Float) {
        if (!hasTeleported) {
            if (timeElapsed >= teleportTime) {
                hasTeleported = true
                teleportToPlayer()
            }
        } else if (!hasStruck) {
            if (timeElapsed >= strikeTime) {
                hasStruck = true
                performStrike()
                finish()
            }
        }
    }

    private fun teleportToPlayer() {
        val playerPos = boss?.getPlayerPosition() ?: return
        val bossPos = boss?.positionComponent ?: return

        // Телепортация за спину игрока
        val teleportX = playerPos.first + 150f
        boss?.teleportTo(teleportX, bossPos.y)
    }

    private fun performStrike() {
        boss?.dealDamageToArea(damage, 150f)
    }

    override fun render(canvas: Canvas) {
        val bossPos = boss?.positionComponent ?: return
        if (hasTeleported && !hasStruck) {
            canvas.drawCircle(bossPos.x, bossPos.y, 100f, teleportPaint)
        }
    }

    override fun onAttackEnd() {}
}

/**
 * Паттерн: Shockwave - волна вокруг босса.
 */
class ShockwavePattern(
    damage: Int = 15,
    duration: Float = 2f,
    cooldown: Float = 5f
) : AttackPattern(PatternType.SHOCKWAVE, damage, duration, cooldown, difficulty = 2) {

    private var hasReleased: Boolean = false
    private val releaseTime: Float = 0.5f
    private var waveRadius: Float = 0f
    private val waveSpeed: Float = 500f

    private val wavePaint = Paint().apply {
        color = Color.rgb(0, 188, 212)
        style = Paint.Style.STROKE
        strokeWidth = 8f
        alpha = 180
    }

    override fun onAttackStart() {
        hasReleased = false
        waveRadius = 0f
    }

    override fun onAttackUpdate(deltaTime: Float) {
        if (!hasReleased) {
            if (timeElapsed >= releaseTime) {
                hasReleased = true
                boss?.triggerShockwave()
            }
        } else {
            waveRadius += waveSpeed * deltaTime
            checkWaveCollision()

            if (waveRadius > 400f) {
                finish()
            }
        }
    }

    private fun checkWaveCollision() {
        val playerPos = boss?.getPlayerPosition() ?: return
        val bossPos = boss?.positionComponent ?: return

        val dx = playerPos.first - bossPos.x
        val dy = playerPos.second - bossPos.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        // Проверка попадания волны
        if (kotlin.math.abs(distance - waveRadius) < 50f) {
            boss?.dealDamageToPlayer(damage)
            boss?.knockbackPlayer(
                (dx / distance) * 400f,
                (dy / distance) * 300f - 200f
            )
        }
    }

    override fun render(canvas: Canvas) {
        val bossPos = boss?.positionComponent ?: return
        if (hasReleased && waveRadius > 0) {
            canvas.drawCircle(bossPos.x, bossPos.y, waveRadius, wavePaint)
        }
    }

    override fun onAttackEnd() {
        waveRadius = 0f
    }
}

/**
 * Паттерн: Black Hole - притягивание игрока.
 */
class BlackHolePattern(
    damage: Int = 10,
    duration: Float = 5f,
    cooldown: Float = 10f
) : AttackPattern(PatternType.BLACK_HOLE, damage, duration, cooldown, difficulty = 4) {

    private var isActive: Boolean = false
    private val activateTime: Float = 1f
    private var pullStrength: Float = 200f
    private var blackHoleRadius: Float = 50f

    private val blackHolePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        alpha = 200
    }

    private val auraPaint = Paint().apply {
        color = Color.rgb(103, 58, 183)
        style = Paint.Style.STROKE
        strokeWidth = 3f
        alpha = 100
    }

    override fun onAttackStart() {
        isActive = false
        blackHoleRadius = 50f
    }

    override fun onAttackUpdate(deltaTime: Float) {
        if (!isActive) {
            if (timeElapsed >= activateTime) {
                isActive = true
                boss?.createBlackHole()
            }
        } else {
            blackHoleRadius += 10f * deltaTime
            applyPullForce(deltaTime)

            if (timeElapsed >= duration * 0.8f) {
                finish()
            }
        }
    }

    private fun applyPullForce(deltaTime: Float) {
        val playerPos = boss?.getPlayerPosition() ?: return
        val bossPos = boss?.positionComponent ?: return

        val dx = bossPos.x - playerPos.first
        val dy = bossPos.y - playerPos.second
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        if (distance > 50f) {
            val pullForce = pullStrength / (distance / 100f)
            boss?.pullPlayer(
                (dx / distance) * pullForce,
                (dy / distance) * pullForce
            )
        } else {
            // Игрок в центре - наносим урон
            boss?.dealDamageToPlayer(damage)
        }
    }

    override fun render(canvas: Canvas) {
        val bossPos = boss?.positionComponent ?: return
        if (isActive) {
            // Чёрная дыра
            canvas.drawCircle(bossPos.x, bossPos.y, blackHoleRadius, blackHolePaint)
            // Аура
            canvas.drawCircle(bossPos.x, bossPos.y, blackHoleRadius + 20f, auraPaint)
        }
    }

    override fun onAttackEnd() {
        isActive = false
        boss?.removeBlackHole()
    }
}

/**
 * Паттерн: Meteor Strike - метеоры с неба.
 */
class MeteorStrikePattern(
    damage: Int = 15,
    duration: Float = 5f,
    cooldown: Float = 10f
) : AttackPattern(PatternType.METEOR_STRIKE, damage, duration, cooldown, difficulty = 4) {

    private data class Meteor(
        var x: Float,
        var y: Float,
        var velocityY: Float,
        val damage: Int,
        var radius: Float
    )

    private val meteors = mutableListOf<Meteor>()
    private var spawnTimer: Float = 0f
    private val spawnInterval: Float = 0.5f
    private val meteorSpeed: Float = 400f

    private val meteorPaint = Paint().apply {
        color = Color.rgb(255, 87, 34)
        style = Paint.Style.FILL
    }

    private val trailPaint = Paint().apply {
        color = Color.rgb(255, 152, 0)
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    override fun onAttackStart() {
        meteors.clear()
        spawnTimer = 0f
    }

    override fun onAttackUpdate(deltaTime: Float) {
        spawnTimer -= deltaTime
        if (spawnTimer <= 0 && timeElapsed < duration - 1f) {
            spawnTimer = spawnInterval
            spawnMeteor()
        }

        // Обновление метеоров
        val iterator = meteors.iterator()
        while (iterator.hasNext()) {
            val meteor = iterator.next()
            meteor.y += meteor.velocityY * deltaTime

            if (meteor.y > 800f) {
                // Удар о землю
                boss?.dealDamageToArea(meteor.damage, meteor.radius * 2)
                boss?.triggerGroundSlam()
                iterator.remove()
            }
        }

        if (meteors.isEmpty() && timeElapsed > duration - 1f) {
            finish()
        }
    }

    private fun spawnMeteor() {
        val bossPos = boss?.positionComponent ?: return
        val playerPos = boss?.getPlayerPosition() ?: return

        // Спавн в направлении игрока
        val offsetX = (Math.random().toFloat() * 400 - 200)
        meteors.add(
            Meteor(
                x = playerPos.first + offsetX,
                y = -100f,
                velocityY = meteorSpeed,
                damage = damage,
                radius = 30f
            )
        )
    }

    override fun render(canvas: Canvas) {
        meteors.forEach { meteor ->
            // След
            canvas.drawLine(
                meteor.x,
                meteor.y - 50f,
                meteor.x,
                meteor.y,
                trailPaint
            )
            // Метеор
            canvas.drawCircle(meteor.x, meteor.y, meteor.radius, meteorPaint)
        }
    }

    override fun onAttackEnd() {
        meteors.clear()
    }
}

/**
 * Паттерн: Time Slow - замедление времени.
 */
class TimeSlowPattern(
    damage: Int = 0,
    duration: Float = 5f,
    cooldown: Float = 20f
) : AttackPattern(PatternType.TIME_SLOW, damage, duration, cooldown, difficulty = 5) {

    private var isActive: Boolean = false
    private val activateTime: Float = 0.5f
    private val slowFactor: Float = 0.3f

    override fun onAttackStart() {
        isActive = false
    }

    override fun onAttackUpdate(deltaTime: Float) {
        if (!isActive) {
            if (timeElapsed >= activateTime) {
                isActive = true
                boss?.activateTimeSlow(slowFactor)
            }
        } else {
            if (timeElapsed >= duration * 0.8f) {
                finish()
            }
        }
    }

    override fun onAttackEnd() {
        isActive = false
        boss?.deactivateTimeSlow()
    }
}

/**
 * Паттерн: Fire Breath - огненное дыхание.
 */
class FireBreathPattern(
    damage: Int = 25,
    duration: Float = 4f,
    cooldown: Float = 7f
) : AttackPattern(PatternType.FIRE_BREATH, damage, duration, cooldown, difficulty = 3) {

    private var isBreathing: Boolean = false
    private val chargeTime: Float = 1f
    private var fireAngle: Float = 0f
    private val fireSpread: Float = 30f
    private val fireRange: Float = 400f

    private val firePaint = Paint().apply {
        color = Color.rgb(255, 87, 34)
        style = Paint.Style.FILL
        alpha = 150
    }

    override fun onAttackStart() {
        isBreathing = false
        fireAngle = 0f
    }

    override fun onAttackUpdate(deltaTime: Float) {
        if (!isBreathing) {
            if (timeElapsed >= chargeTime) {
                isBreathing = true
            }
        } else {
            fireAngle += 1f * deltaTime
            checkFireCollision()

            if (timeElapsed >= duration * 0.8f) {
                finish()
            }
        }
    }

    private fun checkFireCollision() {
        val playerPos = boss?.getPlayerPosition() ?: return
        val bossPos = boss?.positionComponent ?: return

        val dx = playerPos.first - bossPos.x
        val dy = playerPos.second - bossPos.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        if (distance < fireRange && dx > 0 && kotlin.math.abs(dy) < 150f) {
            boss?.dealDamageToPlayer(damage)
        }
    }

    override fun render(canvas: Canvas) {
        val bossPos = boss?.positionComponent ?: return
        if (isBreathing) {
            val sweepAngle = kotlin.math.sin(fireAngle) * fireSpread
            canvas.drawArc(
                RectF(
                    bossPos.x - fireRange,
                    bossPos.y - fireRange,
                    bossPos.x + fireRange,
                    bossPos.y + fireRange
                ),
                -45f + sweepAngle,
                90f,
                true,
                firePaint
            )
        }
    }

    override fun onAttackEnd() {
        isBreathing = false
    }
}

/**
 * Паттерн: Void Beam - луч пустоты.
 */
class VoidBeamPattern(
    damage: Int = 28,
    duration: Float = 5f,
    cooldown: Float = 8f
) : AttackPattern(PatternType.VOID_BEAM, damage, duration, cooldown, difficulty = 4) {

    private var isFiring: Boolean = false
    private val chargeTime: Float = 2f
    private val fireDuration: Float = 2f
    private var beamWidth: Float = 60f

    private val beamPaint = Paint().apply {
        color = Color.rgb(103, 58, 183)
        style = Paint.Style.FILL
        alpha = 180
    }

    override fun onAttackStart() {
        isFiring = false
    }

    override fun onAttackUpdate(deltaTime: Float) {
        if (!isFiring) {
            if (timeElapsed >= chargeTime) {
                isFiring = true
            }
        } else {
            checkBeamCollision()
            if (timeElapsed >= chargeTime + fireDuration) {
                finish()
            }
        }
    }

    private fun checkBeamCollision() {
        val playerPos = boss?.getPlayerPosition() ?: return
        val bossPos = boss?.positionComponent ?: return

        val dx = playerPos.first - bossPos.x
        val dy = playerPos.second - bossPos.y

        if (kotlin.math.abs(dy) < beamWidth && dx > 0) {
            boss?.dealDamageToPlayer(damage)
        }
    }

    override fun render(canvas: Canvas) {
        val bossPos = boss?.positionComponent ?: return
        if (isFiring) {
            canvas.drawRect(
                RectF(
                    bossPos.x,
                    bossPos.y - beamWidth / 2,
                    bossPos.x + 2000f,
                    bossPos.y + beamWidth / 2
                ),
                beamPaint
            )
        }
    }

    override fun onAttackEnd() {
        isFiring = false
    }
}

/**
 * Паттерн: Dark Orbs - тёмные сферы.
 */
class DarkOrbsPattern(
    damage: Int = 18,
    duration: Float = 4f,
    cooldown: Float = 6f
) : AttackPattern(PatternType.DARK_ORBS, damage, duration, cooldown, difficulty = 3) {

    private data class DarkOrb(
        var x: Float,
        var y: Float,
        var angle: Float,
        val radius: Float,
        val speed: Float
    )

    private val orbs = mutableListOf<DarkOrb>()
    private val numOrbs = 6
    private val orbitRadius: Float = 150f

    private val orbPaint = Paint().apply {
        color = Color.rgb(156, 39, 176)
        style = Paint.Style.FILL
        alpha = 200
    }

    override fun onAttackStart() {
        orbs.clear()
        for (i in 0 until numOrbs) {
            val angle = (i * 2 * PI / numOrbs).toFloat()
            val bossPos = boss?.positionComponent ?: return
            orbs.add(
                DarkOrb(
                    x = bossPos.x + cos(angle) * orbitRadius,
                    y = bossPos.y + sin(angle) * orbitRadius,
                    angle = angle,
                    radius = 25f,
                    speed = 2f
                )
            )
        }
    }

    override fun onAttackUpdate(deltaTime: Float) {
        val bossPos = boss?.positionComponent ?: return

        orbs.forEach { orb ->
            orb.angle += orb.speed * deltaTime
            orb.x = bossPos.x + cos(orb.angle) * orbitRadius
            orb.y = bossPos.y + sin(orb.angle) * orbitRadius

            checkOrbCollision(orb)
        }
    }

    private fun checkOrbCollision(orb: DarkOrb) {
        val playerPos = boss?.getPlayerPosition() ?: return
        val dx = playerPos.first - orb.x
        val dy = playerPos.second - orb.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        if (distance < orb.radius + 50f) {
            boss?.dealDamageToPlayer(damage)
        }
    }

    override fun render(canvas: Canvas) {
        orbs.forEach { orb ->
            canvas.drawCircle(orb.x, orb.y, orb.radius, orbPaint)
        }
    }

    override fun onAttackEnd() {
        orbs.clear()
    }
}

/**
 * Паттерн: Poison Cloud - ядовитое облако.
 */
class PoisonCloudPattern(
    damage: Int = 8,
    duration: Float = 6f,
    cooldown: Float = 8f
) : AttackPattern(PatternType.POISON_CLOUD, damage, duration, cooldown, difficulty = 2) {

    private data class PoisonCloud(
        var x: Float,
        var y: Float,
        var radius: Float,
        val maxRadius: Float
    )

    private val clouds = mutableListOf<PoisonCloud>()
    private val cloudPaint = Paint().apply {
        color = Color.rgb(76, 175, 80)
        style = Paint.Style.FILL
        alpha = 100
    }

    override fun onAttackStart() {
        clouds.clear()
    }

    override fun onAttackUpdate(deltaTime: Float) {
        // Спавн облаков
        if (clouds.size < 3 && timeElapsed < duration - 2f) {
            val bossPos = boss?.positionComponent ?: return
            val playerPos = boss?.getPlayerPosition() ?: return

            val offsetX = (Math.random().toFloat() * 300 - 150)
            clouds.add(
                PoisonCloud(
                    x = playerPos.first + offsetX,
                    y = bossPos.y + 100f,
                    radius = 10f,
                    maxRadius = 100f
                )
            )
        }

        // Обновление облаков
        clouds.forEach { cloud ->
            if (cloud.radius < cloud.maxRadius) {
                cloud.radius += 30f * deltaTime
            }
            checkCloudCollision(cloud)
        }
    }

    private fun checkCloudCollision(cloud: PoisonCloud) {
        val playerPos = boss?.getPlayerPosition() ?: return
        val dx = playerPos.first - cloud.x
        val dy = playerPos.second - cloud.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        if (distance < cloud.radius) {
            boss?.dealDamageToPlayer(damage)
        }
    }

    override fun render(canvas: Canvas) {
        clouds.forEach { cloud ->
            canvas.drawCircle(cloud.x, cloud.y, cloud.radius, cloudPaint)
        }
    }

    override fun onAttackEnd() {
        clouds.clear()
    }
}

/**
 * Паттерн: Slime Split - разделение слизня.
 */
class SlimeSplitPattern(
    damage: Int = 5,
    duration: Float = 2f,
    cooldown: Float = 10f
) : AttackPattern(PatternType.SLIME_SPLIT, damage, duration, cooldown, difficulty = 2) {

    private var hasSplit: Boolean = false
    private val splitTime: Float = 1f

    override fun onAttackStart() {
        hasSplit = false
    }

    override fun onAttackUpdate(deltaTime: Float) {
        if (!hasSplit) {
            if (timeElapsed >= splitTime) {
                hasSplit = true
                performSplit()
                finish()
            }
        }
    }

    private fun performSplit() {
        boss?.spawnMinions(2)
    }

    override fun onAttackEnd() {}
}

/**
 * Паттерн: All Attacks - все атаки одновременно (финальный босс).
 */
class AllAttacksPattern(
    damage: Int = 0,
    duration: Float = 10f,
    cooldown: Float = 30f
) : AttackPattern(PatternType.ALL_ATTACKS, damage, duration, cooldown, difficulty = 5) {

    private val subPatterns = mutableListOf<AttackPattern>()
    private var activePatternIndex: Int = 0
    private var patternTimer: Float = 0f
    private val patternDuration: Float = 2f

    override fun initialize(boss: Boss) {
        super.initialize(boss)
        subPatterns.clear()
        subPatterns.add(BulletHellPattern())
        subPatterns.add(MeteorStrikePattern())
        subPatterns.add(BlackHolePattern())
        subPatterns.add(LaserBeamPattern())

        subPatterns.forEach { it.initialize(boss) }
    }

    override fun onAttackStart() {
        activePatternIndex = 0
        patternTimer = 0f
        subPatterns[activePatternIndex].onStart()
    }

    override fun onAttackUpdate(deltaTime: Float) {
        patternTimer += deltaTime

        // Обновление текущего паттерна
        if (!subPatterns[activePatternIndex].update(deltaTime)) {
            // Переход к следующему паттерну
            activePatternIndex = (activePatternIndex + 1) % subPatterns.size
            subPatterns[activePatternIndex].onStart()
        }
    }

    override fun onAttackEnd() {
        subPatterns.forEach { it.onEnd() }
    }

    override fun render(canvas: Canvas) {
        subPatterns.forEach { it.render(canvas) }
    }
}
