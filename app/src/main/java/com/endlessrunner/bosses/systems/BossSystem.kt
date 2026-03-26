package com.endlessrunner.bosses.systems

import android.graphics.Canvas
import android.util.Log
import com.endlessrunner.bosses.Boss
import com.endlessrunner.bosses.managers.BossManager
import com.endlessrunner.bosses.minions.Minion
import com.endlessrunner.config.GameConfig
import com.endlessrunner.core.GameConstants
import com.endlessrunner.entities.EntityManager
import com.endlessrunner.entities.Entity
import com.endlessrunner.player.Player
import com.endlessrunner.systems.BaseSystem

/**
 * Система управления боссами.
 * Интегрируется с GameManager для обработки боёв.
 *
 * @param entityManager Менеджер сущностей
 * @param config Конфигурация игры
 */
class BossSystem(
    entityManager: EntityManager,
    config: GameConfig = GameConfig.DEFAULT
) : BaseSystem(entityManager, config) {

    companion object {
        private const val TAG = "BossSystem"
    }

    /** Менеджер боссов */
    private val bossManager = BossManager.getInstance()

    /** Флаг активности системы */
    private var isEnabled: Boolean = true

    /** Триггер спавна босса */
    private var bossSpawnTriggered: Boolean = false

    /** Дистанция до триггера спавна */
    private var bossSpawnDistance: Float = 5000f

    init {
        updatePriority = 10
        renderPriority = 10

        // Подписка на события менеджера боссов
        bossManager.onBossStart = { boss ->
            onBossFightStart(boss)
        }

        bossManager.onBossDefeated = { boss ->
            onBossFightEnd(boss)
        }

        bossManager.onBossPhaseChange = { boss, phase ->
            onBossPhaseChange(boss, phase)
        }

        bossManager.onPlayerDeathDuringBossFight = {
            onPlayerDeathDuringBossFight()
        }
    }

    override fun init() {
        super.init()
        Log.d(TAG, "BossSystem инициализирована")
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        if (!isEnabled) return

        // Обновление менеджера боссов
        bossManager.update(deltaTime)

        // Проверка триггера спавна
        if (!bossSpawnTriggered && !bossManager.isBossFight) {
            checkBossSpawnTrigger()
        }

        // Обновление коллизий с боссом
        updateBossCollisions()
    }

    override fun render(canvas: Canvas) {
        super.render(canvas)

        if (!isEnabled) return

        // Отрисовка через менеджер
        val cameraX = getCameraX()
        bossManager.render(canvas, cameraX)
    }

    override fun reset() {
        super.reset()
        bossSpawnTriggered = false
        // Не сбрасываем менеджера - босс остаётся
    }

    // ============================================================================
    // УПРАВЛЕНИЕ БОССОМ
    // ============================================================================

    /**
     * Спавн босса.
     */
    fun spawnBoss(bossType: com.endlessrunner.bosses.BossType, x: Float, y: Float) {
        if (bossManager.isBossFight) {
            Log.w(TAG, "Бой с боссом уже идёт!")
            return
        }

        val boss = bossManager.spawnBoss(bossType, x, y, entityManager)
        if (boss != null) {
            Log.d(TAG, "Босс заспавнен: ${bossType.id}")
        }
    }

    /**
     * Начало боя с боссом.
     */
    fun startBossFight() {
        bossManager.startBossFight()
    }

    /**
     * Завершение боя с боссом.
     */
    fun endBossFight() {
        bossManager.endBossFight()
    }

    /**
     * Проверка триггера спавна босса.
     */
    private fun checkBossSpawnTrigger() {
        val player = entityManager.getFirstByTag(GameConstants.TAG_PLAYER) ?: return
        val playerPos = player.getComponent<com.endlessrunner.components.PositionComponent>() ?: return

        // Проверка дистанции
        if (playerPos.x >= bossSpawnDistance) {
            bossSpawnTriggered = true
            triggerBossSpawn()
        }
    }

    /**
     * Триггер спавна босса.
     */
    private fun triggerBossSpawn() {
        // Определение типа босса на основе прогресса
        val nextBoss = bossManager.getNextBossType() ?: return

        // Спавн на фиксированной позиции
        spawnBoss(nextBoss, bossSpawnDistance + 500f, 600f)
    }

    /**
     * Обновление коллизий с боссом.
     */
    private fun updateBossCollisions() {
        val boss = bossManager.currentBoss ?: return
        val player = entityManager.getFirstByTag(GameConstants.TAG_PLAYER) as? Player ?: return

        if (!boss.isActive || boss.isDead) return

        // Проверка коллизии с боссом
        if (checkBossPlayerCollision(boss, player)) {
            boss.dealDamageToPlayer(boss.damage)
        }

        // Проверка коллизий с миньонами
        checkMinionCollisions(player)
    }

    /**
     * Проверка коллизии босса с игроком.
     */
    private fun checkBossPlayerCollision(boss: Boss, player: Player): Boolean {
        val bossPos = boss.positionComponent ?: return false
        val playerPos = player.positionComponent ?: return false

        val dx = bossPos.x - playerPos.x
        val dy = bossPos.y - playerPos.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        val bossRadius = boss.bossType.width / 2f
        val playerRadius = 50f // Примерный радиус игрока

        return distance < bossRadius + playerRadius
    }

    /**
     * Проверка коллизий с миньонами.
     */
    private fun checkMinionCollisions(player: Player) {
        val minions = entityManager.getAllEntities()
            .filter { it.tag == "minion" }
            .filter { it.isActive }

        minions.forEach { entity ->
            // Получение миньона
            val minion = Minion(com.endlessrunner.bosses.minions.MinionType.DARK_SPAWN, null)
            // TODO: Правильное получение миньона

            if (checkMinionPlayerCollision(minion, player)) {
                // Нанесение урона
            }
        }
    }

    /**
     * Проверка коллизии миньона с игроком.
     */
    private fun checkMinionPlayerCollision(minion: Minion, player: Player): Boolean {
        val minionPos = minion.positionComponent ?: return false
        val playerPos = player.positionComponent ?: return false

        val dx = minionPos.x - playerPos.x
        val dy = minionPos.y - playerPos.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        return distance < 80f
    }

    // ============================================================================
    // СОБЫТИЯ
    // ============================================================================

    /**
     * Вызывается при начале боя с боссом.
     */
    private fun onBossFightStart(boss: Boss) {
        Log.i(TAG, "Начало боя с боссом: ${boss.bossType.id}")

        // Блокировка обычного спавна
        // TODO: spawnSystem.enableEnemySpawn = false

        // Включение музыки босса
        // TODO: audioManager.playMusic(MusicLibrary.BOSS_BATTLE)

        // Тряска камеры
        // TODO: cameraSystem.shake(1f, 20f)

        // Zoom на босса
        // TODO: cameraSystem.zoom(1.2f)
    }

    /**
     * Вызывается при победе над боссом.
     */
    private fun onBossFightEnd(boss: Boss) {
        Log.i(TAG, "Босс побеждён: ${boss.bossType.id}")

        // Награды
        val reward = calculateVictoryReward(boss)
        // TODO: player.addGold(reward.gold)
        // TODO: player.addXP(reward.xp)

        // Разблокировка обычного спавна
        // TODO: spawnSystem.enableEnemySpawn = true

        // Сброс камеры
        // TODO: cameraSystem.resetZoom()

        // Завершение боя через задержку
        // TODO: delayedEndBossFight()
    }

    /**
     * Вызывается при смене фазы.
     */
    private fun onBossPhaseChange(boss: Boss, phase: com.endlessrunner.bosses.BossPhase) {
        Log.d(TAG, "Смена фазы: ${phase.phaseName}")

        // Тряска камеры
        // TODO: cameraSystem.shake(0.5f, 10f)

        // Звуковой эффект
        // TODO: audioManager.playSfx(SoundLibrary.BOSS_PHASE_CHANGE)
    }

    /**
     * Вызывается при смерти игрока во время боя.
     */
    private fun onPlayerDeathDuringBossFight() {
        Log.d(TAG, "Игрок умер во время боя с боссом")

        // Босс не сбрасывается
        // Игрок может попробовать снова
    }

    /**
     * Расчёт награды за победу.
     */
    private fun calculateVictoryReward(boss: Boss): BossVictoryReward {
        val baseGold = com.endlessrunner.bosses.config.BossGlobalConfig.BASE_GOLD_REWARD
        val baseXP = com.endlessrunner.bosses.config.BossGlobalConfig.BASE_XP_REWARD
        val multiplier = com.endlessrunner.bosses.config.BossGlobalConfig.VICTORY_REWARD_MULTIPLIER

        val difficultyMultiplier = when (boss.bossType.difficulty) {
            1 -> 1f
            2 -> 1.5f
            3 -> 2f
            4 -> 3f
            5 -> 5f
            else -> 1f
        }

        return BossVictoryReward(
            gold = (baseGold * multiplier * difficultyMultiplier).toInt(),
            xp = (baseXP * multiplier * difficultyMultiplier).toInt()
        )
    }

    /**
     * Получение позиции камеры.
     */
    private fun getCameraX(): Float {
        // TODO: Получить из CameraSystem
        return 0f
    }

    // ============================================================================
    // УТИЛИТЫ
    // ============================================================================

    /**
     * Проверка, идёт ли бой с боссом.
     */
    fun isBossFightActive(): Boolean = bossManager.isBossFight

    /**
     * Получение текущего босса.
     */
    fun getCurrentBoss(): Boss? = bossManager.currentBoss

    /**
     * Включение/выключение системы.
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    /**
     * Установка дистанции спавна босса.
     */
    fun setBossSpawnDistance(distance: Float) {
        bossSpawnDistance = distance
    }

    /**
     * Data class для награды.
     */
    data class BossVictoryReward(
        val gold: Int,
        val xp: Int
    )
}

/**
 * Extension функция для создания BossSystem.
 */
fun createBossSystem(
    entityManager: EntityManager,
    config: GameConfig
): BossSystem {
    return BossSystem(entityManager, config)
}
