package com.endlessrunner.bosses.attacks

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.endlessrunner.bosses.Boss
import com.endlessrunner.bosses.PatternType
import com.endlessrunner.bosses.projectiles.BossProjectile
import com.endlessrunner.bosses.projectiles.ProjectilePool
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Паттерн: Bullet Hell - множество снарядов по спирали.
 */
class BulletHellPattern(
    damage: Int = 5,
    duration: Float = 5f,
    cooldown: Float = 8f
) : AttackPattern(PatternType.BULLET_HELL, damage, duration, cooldown, difficulty = 3) {

    private val projectilePool = ProjectilePool.getInstance()
    private val projectiles = mutableListOf<BossProjectile>()
    private var spawnTimer: Float = 0f
    private val spawnInterval: Float = 0.1f
    private var angle: Float = 0f
    private val spiralSpeed: Float = 2f

    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    override fun onAttackStart() {
        spawnTimer = 0f
        angle = 0f
        projectiles.clear()
    }

    override fun onAttackUpdate(deltaTime: Float) {
        spawnTimer -= deltaTime
        angle += spiralSpeed * deltaTime

        if (spawnTimer <= 0) {
            spawnTimer = spawnInterval
            spawnSpiralProjectiles()
        }

        // Обновление снарядов
        val iterator = projectiles.iterator()
        while (iterator.hasNext()) {
            val projectile = iterator.next()
            projectile.update(deltaTime)
            if (projectile.isExpired) {
                projectilePool.release(projectile)
                iterator.remove()
            }
        }
    }

    private fun spawnSpiralProjectiles() {
        val bossPos = boss?.positionComponent ?: return
        val numProjectiles = 8

        for (i in 0 until numProjectiles) {
            val projectileAngle = angle + (i * 2 * PI / numProjectiles)
            val velocityX = cos(projectileAngle) * 400f
            val velocityY = sin(projectileAngle) * 200f

            val projectile = projectilePool.acquire(
                startX = bossPos.x,
                startY = bossPos.y,
                velocityX = velocityX,
                velocityY = velocityY,
                damage = damage,
                lifetime = 5f
            )
            projectiles.add(projectile)
        }
    }

    override fun onAttackEnd() {
        projectiles.forEach { projectilePool.release(it) }
        projectiles.clear()
    }

    override fun render(canvas: Canvas) {
        projectiles.forEach { it.render(canvas, paint) }
    }
}

/**
 * Паттерн: Aimed Shot - снаряд в игрока.
 */
class AimedShotPattern(
    damage: Int = 10,
    duration: Float = 1f,
    cooldown: Float = 2f
) : AttackPattern(PatternType.AIMED_SHOT, damage, duration, cooldown, difficulty = 1) {

    private val projectilePool = ProjectilePool.getInstance()
    private var hasFired: Boolean = false
    private var projectile: BossProjectile? = null

    override fun onAttackStart() {
        hasFired = false
        projectile = null
    }

    override fun onAttackUpdate(deltaTime: Float) {
        if (!hasFired) {
            fireAtPlayer()
            hasFired = true
        }

        projectile?.update(deltaTime)
        if (projectile?.isExpired == true) {
            projectilePool.release(projectile)
            projectile = null
            finish()
        }
    }

    private fun fireAtPlayer() {
        val bossPos = boss?.positionComponent ?: return
        val playerPos = boss?.getPlayerPosition() ?: return

        val dx = playerPos.first - bossPos.x
        val dy = playerPos.second - bossPos.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        if (distance > 0) {
            val speed = 600f
            val velocityX = (dx / distance) * speed
            val velocityY = (dy / distance) * speed

            projectile = projectilePool.acquire(
                startX = bossPos.x,
                startY = bossPos.y,
                velocityX = velocityX,
                velocityY = velocityY,
                damage = damage,
                lifetime = 3f
            )
        }
    }

    override fun onAttackEnd() {
        projectile?.let { projectilePool.release(it) }
        projectile = null
    }

    override fun render(canvas: Canvas) {
        projectile?.render(canvas, Paint().apply { color = Color.RED })
    }
}

/**
 * Паттерн: Spread Shot - веер снарядов.
 */
class SpreadShotPattern(
    damage: Int = 8,
    duration: Float = 2f,
    cooldown: Float = 4f
) : AttackPattern(PatternType.SPREAD_SHOT, damage, duration, cooldown, difficulty = 2) {

    private val projectilePool = ProjectilePool.getInstance()
    private val projectiles = mutableListOf<BossProjectile>()
    private var hasFired: Boolean = false
    private val numProjectiles = 5
    private val spreadAngle: Float = 45f // градусов

    override fun onAttackStart() {
        hasFired = false
        projectiles.clear()
    }

    override fun onAttackUpdate(deltaTime: Float) {
        if (!hasFired) {
            fireSpread()
            hasFired = true
        }

        val iterator = projectiles.iterator()
        while (iterator.hasNext()) {
            val projectile = iterator.next()
            projectile.update(deltaTime)
            if (projectile.isExpired) {
                projectilePool.release(projectile)
                iterator.remove()
            }
        }
    }

    private fun fireSpread() {
        val bossPos = boss?.positionComponent ?: return
        val playerPos = boss?.getPlayerPosition() ?: return

        val dx = playerPos.first - bossPos.x
        val dy = playerPos.second - bossPos.y
        val baseAngle = kotlin.math.atan2(dy, dx)
        val angleStep = Math.toRadians(spreadAngle.toDouble()) / (numProjectiles - 1)

        for (i in 0 until numProjectiles) {
            val angle = baseAngle - angleStep / 2 + (i * angleStep)
            val speed = 500f
            val velocityX = kotlin.math.cos(angle) * speed
            val velocityY = kotlin.math.sin(angle) * speed

            val projectile = projectilePool.acquire(
                startX = bossPos.x,
                startY = bossPos.y,
                velocityX = velocityX,
                velocityY = velocityY,
                damage = damage,
                lifetime = 4f
            )
            projectiles.add(projectile)
        }
    }

    override fun onAttackEnd() {
        projectiles.forEach { projectilePool.release(it) }
        projectiles.clear()
    }
}

/**
 * Паттерн: Rain Projectiles - снаряды сверху.
 */
class RainProjectilesPattern(
    damage: Int = 7,
    duration: Float = 4f,
    cooldown: Float = 6f
) : AttackPattern(PatternType.RAIN_PROJECTILES, damage, duration, cooldown, difficulty = 2) {

    private val projectilePool = ProjectilePool.getInstance()
    private val projectiles = mutableListOf<BossProjectile>()
    private var spawnTimer: Float = 0f
    private val spawnInterval: Float = 0.2f

    override fun onAttackStart() {
        spawnTimer = 0f
        projectiles.clear()
    }

    override fun onAttackUpdate(deltaTime: Float) {
        spawnTimer -= deltaTime

        if (spawnTimer <= 0) {
            spawnTimer = spawnInterval
            spawnRainProjectile()
        }

        val iterator = projectiles.iterator()
        while (iterator.hasNext()) {
            val projectile = iterator.next()
            projectile.update(deltaTime)
            if (projectile.isExpired) {
                projectilePool.release(projectile)
                iterator.remove()
            }
        }
    }

    private fun spawnRainProjectile() {
        val bossPos = boss?.positionComponent ?: return
        val randomOffset = (Math.random().toFloat() * 400 - 200)

        val projectile = projectilePool.acquire(
            startX = bossPos.x + randomOffset,
            startY = bossPos.y - 300,
            velocityX = 0f,
            velocityY = 400f,
            damage = damage,
            lifetime = 3f
        )
        projectiles.add(projectile)
    }

    override fun onAttackEnd() {
        projectiles.forEach { projectilePool.release(it) }
        projectiles.clear()
    }
}

/**
 * Паттерн: Orbit Projectiles - снаряды по орбите.
 */
class OrbitProjectilesPattern(
    damage: Int = 6,
    duration: Float = 6f,
    cooldown: Float = 8f
) : AttackPattern(PatternType.ORBIT_PROJECTILES, damage, duration, cooldown, difficulty = 2) {

    private val projectilePool = ProjectilePool.getInstance()
    private val projectiles = mutableListOf<BossProjectile>()
    private var angle: Float = 0f
    private val orbitSpeed: Float = 1.5f
    private val orbitRadius: Float = 150f
    private val numProjectiles = 4

    override fun onAttackStart() {
        angle = 0f
        projectiles.clear()

        // Создаём снаряды на орбите
        val bossPos = boss?.positionComponent ?: return
        for (i in 0 until numProjectiles) {
            val projectileAngle = (i * 2 * PI / numProjectiles).toFloat()
            val projectile = projectilePool.acquire(
                startX = bossPos.x + kotlin.math.cos(projectileAngle) * orbitRadius,
                startY = bossPos.y + kotlin.math.sin(projectileAngle) * orbitRadius,
                velocityX = 0f,
                velocityY = 0f,
                damage = damage,
                lifetime = duration
            )
            projectile.orbitOffset = projectileAngle
            projectiles.add(projectile)
        }
    }

    override fun onAttackUpdate(deltaTime: Float) {
        angle += orbitSpeed * deltaTime
        val bossPos = boss?.positionComponent ?: return

        projectiles.forEachIndexed { index, projectile ->
            val projectileAngle = angle + (index * 2 * PI / numProjectiles)
            projectile.position.x = bossPos.x + kotlin.math.cos(projectileAngle) * orbitRadius
            projectile.position.y = bossPos.y + kotlin.math.sin(projectileAngle) * orbitRadius
        }
    }

    override fun onAttackEnd() {
        projectiles.forEach { projectilePool.release(it) }
        projectiles.clear()
    }
}
