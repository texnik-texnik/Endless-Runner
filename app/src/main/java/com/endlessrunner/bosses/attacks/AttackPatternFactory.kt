package com.endlessrunner.bosses.attacks

import com.endlessrunner.bosses.Boss
import com.endlessrunner.bosses.PatternType

/**
 * Фабрика для создания паттернов атак.
 * Factory Method Pattern.
 */
object AttackPatternFactory {

    /**
     * Создание паттерна по типу.
     *
     * @param patternType Тип паттерна
     * @param boss Босс для инициализации
     * @return Созданный паттерн
     */
    fun createPattern(patternType: PatternType, boss: Boss): AttackPattern {
        val pattern = when (patternType) {
            // Projectile Attacks
            PatternType.BULLET_HELL -> BulletHellPattern()
            PatternType.AIMED_SHOT -> AimedShotPattern()
            PatternType.SPREAD_SHOT -> SpreadShotPattern()
            PatternType.RAIN_PROJECTILES -> RainProjectilesPattern()
            PatternType.ORBIT_PROJECTILES -> OrbitProjectilesPattern()
            PatternType.METEOR_STRIKE -> MeteorStrikePattern()
            PatternType.MISSILE_STORM -> MissileStormPattern()

            // Melee Attacks
            PatternType.CHARGE -> ChargePattern()
            PatternType.SLAM -> SlamPattern()
            PatternType.SWIPE -> SwipePattern()
            PatternType.TAIL_SPIN -> TailSpinPattern()
            PatternType.TAIL_SWIPE -> TailSwipePattern()
            PatternType.SWORD_COMBO -> SwordComboPattern()
            PatternType.SHIELD_BASH -> ShieldBashPattern()
            PatternType.GROUND_POUND -> GroundPoundPattern()
            PatternType.JUMP_ATTACK -> JumpAttackPattern()

            // Special Attacks
            PatternType.SUMMON_MINIONS -> SummonMinionsPattern()
            PatternType.LASER_BEAM -> LaserBeamPattern()
            PatternType.TELEPORT_STRIKE -> TeleportStrikePattern()
            PatternType.SHOCKWAVE -> ShockwavePattern()
            PatternType.BLACK_HOLE -> BlackHolePattern()
            PatternType.TIME_SLOW -> TimeSlowPattern()
            PatternType.FIRE_BREATH -> FireBreathPattern()
            PatternType.VOID_BEAM -> VoidBeamPattern()
            PatternType.DARK_ORBS -> DarkOrbsPattern()
            PatternType.POISON_CLOUD -> PoisonCloudPattern()
            PatternType.SLIME_SPLIT -> SlimeSplitPattern()
            PatternType.ALL_ATTACKS -> AllAttacksPattern()
        }

        pattern.initialize(boss)
        return pattern
    }

    /**
     * Создание паттерна с кастомными параметрами.
     */
    fun createPattern(
        patternType: PatternType,
        boss: Boss,
        damage: Int,
        duration: Float,
        cooldown: Float
    ): AttackPattern {
        val pattern = createPattern(patternType, boss)
        pattern.damage = damage
        pattern.duration = duration
        pattern.cooldown = cooldown
        return pattern
    }

    /**
     * Создание списка паттернов для фазы.
     */
    fun createPatternsForPhase(
        patternTypes: List<PatternType>,
        boss: Boss
    ): List<AttackPattern> {
        return patternTypes.map { createPattern(it, boss) }
    }

    /**
     * Получение паттерна по ID.
     */
    fun createPatternById(patternId: String, boss: Boss): AttackPattern? {
        val patternType = PatternType.fromId(patternId) ?: return null
        return createPattern(patternType, boss)
    }
}

/**
 * Паттерн для ракетного залпа (дополнение).
 */
class MissileStormPattern(
    damage: Int = 12,
    duration: Float = 4f,
    cooldown: Float = 7f
) : AttackPattern(PatternType.MISSILE_STORM, damage, duration, cooldown, difficulty = 3) {

    private val missiles = mutableListOf<BossMissile>()
    private var spawnTimer: Float = 0f
    private val spawnInterval: Float = 0.3f

    override fun onAttackStart() {
        missiles.clear()
        spawnTimer = 0f
    }

    override fun onAttackUpdate(deltaTime: Float) {
        spawnTimer -= deltaTime

        if (spawnTimer <= 0 && timeElapsed < duration - 1f) {
            spawnTimer = spawnInterval
            spawnMissile()
        }

        // Обновление ракет
        val iterator = missiles.iterator()
        while (iterator.hasNext()) {
            val missile = iterator.next()
            missile.update(deltaTime)
            if (missile.isExpired) {
                iterator.remove()
            }
        }

        if (missiles.isEmpty() && timeElapsed > duration - 1f) {
            finish()
        }
    }

    private fun spawnMissile() {
        val bossPos = boss?.positionComponent ?: return
        val playerPos = boss?.getPlayerPosition() ?: return

        val missile = BossMissile(
            startX = bossPos.x + 100f,
            startY = bossPos.y - 50f,
            targetX = playerPos.first,
            targetY = playerPos.second,
            damage = damage
        )
        missiles.add(missile)
    }

    override fun onAttackEnd() {
        missiles.clear()
    }
}

/**
 * Самонаводящаяся ракета.
 */
class BossMissile(
    var startX: Float,
    var startY: Float,
    var targetX: Float,
    var targetY: Float,
    var damage: Int
) {
    var x: Float = startX
    var y: Float = startY
    var velocityX: Float = 0f
    var velocityY: Float = 0f
    var timeAlive: Float = 0f
    var isExpired: Boolean = false

    private val speed: Float = 400f
    private val homingStrength: Float = 3f
    private val lifetime: Float = 5f

    init {
        // Начальное направление
        updateVelocity()
    }

    fun update(deltaTime: Float) {
        timeAlive += deltaTime

        // Самонаведение
        val dx = targetX - x
        val dy = targetY - y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        if (distance > 10f) {
            velocityX += (dx / distance) * homingStrength
            velocityY += (dy / distance) * homingStrength

            // Ограничение скорости
            val currentSpeed = kotlin.math.sqrt(velocityX * velocityX + velocityY * velocityY)
            if (currentSpeed > speed) {
                velocityX = (velocityX / currentSpeed) * speed
                velocityY = (velocityY / currentSpeed) * speed
            }
        }

        x += velocityX * deltaTime
        y += velocityY * deltaTime

        // Проверка попадания или истечения
        if (distance < 50f || timeAlive > lifetime || y > 1000f) {
            isExpired = true
        }
    }

    private fun updateVelocity() {
        val dx = targetX - startX
        val dy = targetY - startY
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        if (distance > 0) {
            velocityX = (dx / distance) * speed
            velocityY = (dy / distance) * speed
        }
    }
}
