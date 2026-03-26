package com.endlessrunner.bosses.minions

import android.graphics.Canvas
import com.endlessrunner.bosses.Boss
import com.endlessrunner.entities.EntityManager
import kotlin.math.cos
import kotlin.math.sin

/**
 * Спавнер миньонов для босса.
 * Управляет созданием, обновлением и удалением миньонов.
 *
 * @property masterBoss Босс-хозяин
 */
class MinionSpawner(
    val masterBoss: Boss
) {
    /** Список активных миньонов */
    private val activeMinions = mutableListOf<Minion>()

    /** EntityManager для создания сущностей */
    var entityManager: EntityManager? = null

    /** Пул миньонов */
    private val minionPool = MinionPool.getInstance()

    /** Таймер спавна */
    private var spawnTimer: Float = 0f

    /** Максимальное количество миньонов одновременно */
    var maxActiveMinions: Int = 10

    /** Флаг активности спавна */
    var spawnEnabled: Boolean = true

    /**
     * Обновление миньонов.
     */
    fun update(deltaTime: Float) {
        // Обновление всех миньонов
        val iterator = activeMinions.iterator()
        while (iterator.hasNext()) {
            val minion = iterator.next()

            if (minion.isDestroyed || !minion.isActive) {
                minionPool.release(minion)
                iterator.remove()
            } else {
                minion.update(deltaTime)
            }
        }

        // Проверка смерти босса
        if (masterBoss.isDead) {
            onBossDeath()
        }
    }

    /**
     * Отрисовка миньонов.
     */
    fun render(canvas: Canvas) {
        activeMinions.forEach { minion ->
            minion.render(canvas)
        }
    }

    /**
     * Спавн одного миньона.
     *
     * @param type Тип миньона
     * @param positionX Позиция X
     * @param positionY Позиция Y
     */
    fun spawnMinion(
        type: MinionType,
        positionX: Float,
        positionY: Float
    ): Minion? {
        if (!spawnEnabled) return null
        if (activeMinions.size >= maxActiveMinions) return null

        val minion = minionPool.acquire(type, masterBoss)
        minion.positionComponent?.setPosition(positionX, positionY)

        // Добавление в EntityManager
        entityManager?.create(tag = "minion") { entity ->
            entity.addComponent(minion.positionComponent!!)
            entity.addComponent(minion.renderComponent!!)
            entity.addComponent(minion.physicsComponent!!)
        }

        activeMinions.add(minion)

        return minion
    }

    /**
     * Спавн волны миньонов.
     *
     * @param count Количество миньонов
     * @param positionX Позиция X центра
     * @param positionY Позиция Y центра
     * @param type Тип миньонов
     * @param pattern Паттерн спавна
     */
    fun spawnWave(
        count: Int,
        positionX: Float,
        positionY: Float,
        type: MinionType,
        pattern: SpawnPattern = SpawnPattern.CIRCLE
    ) {
        if (!spawnEnabled) return

        when (pattern) {
            SpawnPattern.CIRCLE -> spawnCircleWave(count, positionX, positionY, type)
            SpawnPattern.LINE -> spawnLineWave(count, positionX, positionY, type)
            SpawnPattern.RANDOM -> spawnRandomWave(count, positionX, positionY, type)
            SpawnPattern.SPIRAL -> spawnSpiralWave(count, positionX, positionY, type)
        }
    }

    /**
     * Спавн по кругу.
     */
    private fun spawnCircleWave(count: Int, centerX: Float, centerY: Float, type: MinionType) {
        val radius = 150f
        val angleStep = 360f / count

        for (i in 0 until count) {
            val angle = Math.toRadians((i * angleStep).toDouble()).toFloat()
            val x = centerX + cos(angle) * radius
            val y = centerY + sin(angle) * radius

            spawnMinion(type, x, y)
        }
    }

    /**
     * Спавн по линии.
     */
    private fun spawnLineWave(count: Int, centerX: Float, centerY: Float, type: MinionType) {
        val spacing = 80f
        val startX = centerX - (count - 1) * spacing / 2f

        for (i in 0 until count) {
            val x = startX + i * spacing
            spawnMinion(type, x, centerY)
        }
    }

    /**
     * Спавн в случайных позициях.
     */
    private fun spawnRandomWave(count: Int, centerX: Float, centerY: Float, type: MinionType) {
        for (i in 0 until count) {
            val offsetX = (Math.random().toFloat() * 400 - 200)
            val offsetY = (Math.random().toFloat() * 200 - 100)

            spawnMinion(type, centerX + offsetX, centerY + offsetY)
        }
    }

    /**
     * Спавн по спирали.
     */
    private fun spawnSpiralWave(count: Int, centerX: Float, centerY: Float, type: MinionType) {
        val startRadius = 100f
        val radiusStep = 30f
        val angleStep = 45f

        for (i in 0 until count) {
            val radius = startRadius + i * radiusStep
            val angle = Math.toRadians((i * angleStep).toDouble()).toFloat()
            val x = centerX + cos(angle) * radius
            val y = centerY + sin(angle) * radius

            spawnMinion(type, x, y)
        }
    }

    /**
     * Удаление всех миньонов.
     */
    fun clearMinions() {
        activeMinions.forEach { minion ->
            minionPool.release(minion)
        }
        activeMinions.clear()
    }

    /**
     * Вызывается при смерти босса.
     */
    fun onBossDeath() {
        // Все миньоны становятся агрессивными или исчезают
        activeMinions.forEach { minion ->
            minion.onMasterDeath()
        }
    }

    /**
     * Получение количества активных миньонов.
     */
    fun getActiveMinionCount(): Int = activeMinions.size

    /**
     * Получение всех активных миньонов.
     */
    fun getActiveMinions(): List<Minion> = activeMinions.toList()

    /**
     * Удаление миньонов по типу.
     */
    fun removeMinionsByType(type: MinionType) {
        val toRemove = activeMinions.filter { it.minionType == type }
        toRemove.forEach { minion ->
            minionPool.release(minion)
            activeMinions.remove(minion)
        }
    }

    /**
     * Лечебная аура для миньонов.
     */
    fun healMinions(amount: Int) {
        activeMinions.forEach { minion ->
            // TODO: Реализовать лечение
        }
    }

    /**
     * Усиление миньонов.
     */
    fun buffMinions(damageMultiplier: Float, speedMultiplier: Float) {
        activeMinions.forEach { minion ->
            minion.damage = (minion.damage * damageMultiplier).toInt()
            // TODO: Обновить скорость
        }
    }
}

/**
 * Паттерн спавна миньонов.
 */
enum class SpawnPattern {
    /** По кругу вокруг точки */
    CIRCLE,

    /** По линии */
    LINE,

    /** Случайные позиции */
    RANDOM,

    /** По спирали */
    SPIRAL
}
