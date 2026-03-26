package com.endlessrunner.bosses.managers

import android.graphics.Canvas
import android.util.Log
import com.endlessrunner.bosses.Boss
import com.endlessrunner.bosses.BossType
import com.endlessrunner.bosses.arena.BossArena
import com.endlessrunner.bosses.visuals.BossHealthBar
import com.endlessrunner.bosses.visuals.BossWarningOverlay
import com.endlessrunner.entities.EntityManager
import com.endlessrunner.managers.GameManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Менеджер боссов.
 * Singleton для управления всеми аспектами боёв с боссами.
 */
class BossManager private constructor() {

    companion object {
        private const val TAG = "BossManager"

        @Volatile
        private var instance: BossManager? = null

        /**
         * Получение экземпляра BossManager.
         */
        fun getInstance(): BossManager {
            return instance ?: synchronized(this) {
                instance ?: BossManager().also { instance = it }
            }
        }

        /**
         * Сброс экземпляра (для тестов).
         */
        fun resetInstance() {
            instance?.dispose()
            instance = null
        }
    }

    // ============================================================================
    // СОСТОЯНИЕ
    // ============================================================================

    /** Текущий активный босс */
    var currentBoss: Boss? = null
        private set

    /** Идёт ли бой с боссом */
    val isBossFight: Boolean
        get() = currentBoss?.isActive == true && !currentBoss!!.isDead

    /** Текущая арена босса */
    var currentArena: BossArena? = null
        private set

    /** Health bar босса */
    private val bossHealthBar = BossHealthBar()

    /** Warning overlay для появления босса */
    private val warningOverlay = BossWarningOverlay()

    // ============================================================================
    // СТАТИСТИКА
    // ============================================================================

    /** Количество побеждённых боссов */
    var bossesDefeated: Int = 0
        private set

    /** Урон, нанесённый боссам */
    var bossDamageDealt: Int = 0
        private set

    /** Время, проведённое в боях с боссами */
    var totalBossFightTime: Float = 0f
        private set

    /** Прогресс по каждому боссу */
    private val bossProgressMap = mutableMapOf<BossType, BossProgress>()

    // ============================================================================
    // CALLBACKS
    // ============================================================================

    /** Вызывается при начале боя с боссом */
    var onBossStart: ((Boss) -> Unit)? = null

    /** Вызывается при победе над боссом */
    var onBossDefeated: ((Boss) -> Unit)? = null

    /** Вызывается при смене фазы босса */
    var onBossPhaseChange: ((Boss, BossPhase) -> Unit)? = null

    /** Вызывается при смерти игрока во время боя */
    var onPlayerDeathDuringBossFight: (() -> Unit)? = null

    // ============================================================================
    // СОСТОЯНИЕ (StateFlow)
    // ============================================================================

    private val _bossState = MutableStateFlow<BossState>(BossState.None)
    val bossState: StateFlow<BossState> = _bossState.asStateFlow()

    // ============================================================================
    // УПРАВЛЕНИЕ БОССАМИ
    // ============================================================================

    /**
     * Спавн босса.
     *
     * @param type Тип босса
     * @param positionX Позиция X
     * @param positionY Позиция Y
     * @param entityManager EntityManager для создания сущности
     */
    fun spawnBoss(
        type: BossType,
        positionX: Float,
        positionY: Float,
        entityManager: EntityManager
    ): Boss? {
        if (isBossFight) {
            Log.w(TAG, "Бой с боссом уже идёт!")
            return null
        }

        Log.d(TAG, "Спавн босса: ${type.id}")

        // Создание босса
        val boss = createBoss(type)
        boss.positionComponent?.setPosition(positionX, positionY)
        boss.entityManager = entityManager

        // Создание арены
        currentArena = BossArena(type.arenaWidth, 1080f, type)
        currentArena?.initialize()

        // Добавление босса в EntityManager
        entityManager.create(tag = "boss") { entity ->
            entity.addComponent(boss.positionComponent!!)
            entity.addComponent(boss.renderComponent!!)
            entity.addComponent(boss.physicsComponent!!)
        }

        currentBoss = boss

        // Обновление состояния
        _bossState.value = BossState.Spawning(type)

        // Показ предупреждения
        warningOverlay.show(type)

        return boss
    }

    /**
     * Создание босса нужного типа.
     */
    private fun createBoss(type: BossType): Boss {
        return when (type) {
            BossType.GIANT_SLIME -> GiantSlimeBoss()
            BossType.MECH_DRAGON -> MechDragonBoss()
            BossType.DARK_KNIGHT -> DarkKnightBoss()
            BossType.VOID_GUARDIAN -> VoidGuardianBoss()
            BossType.FINAL_BOSS -> FinalBossBoss()
        }
    }

    /**
     * Начало боя с боссом.
     */
    fun startBossFight() {
        val boss = currentBoss ?: return

        Log.d(TAG, "Начало боя с боссом: ${boss.bossType.id}")

        boss.startFight()
        _bossState.value = BossState.Fighting(boss.bossType)

        // Инициализация health bar
        bossHealthBar.setHealth(boss.health, boss.maxHealth)
        bossHealthBar.setBossType(boss.bossType)
        bossHealthBar.setPhase(boss.phase)

        // Callback
        onBossStart?.invoke(boss)
    }

    /**
     * Обновление менеджера боссов.
     */
    fun update(deltaTime: Float) {
        // Обновление warning overlay
        warningOverlay.update(deltaTime)

        if (!isBossFight) return

        val boss = currentBoss ?: return

        // Обновление времени боя
        totalBossFightTime += deltaTime

        // Обновление арены
        currentArena?.update(deltaTime)

        // Обновление health bar
        bossHealthBar.setHealth(boss.health, boss.maxHealth)
        bossHealthBar.setPhase(boss.phase)

        // Проверка смерти босса
        if (boss.isDead) {
            onBossKilled()
        }

        // Проверка смены фазы
        checkPhaseChange(boss)
    }

    /**
     * Проверка смены фазы.
     */
    private fun checkPhaseChange(boss: Boss) {
        val currentPhase = boss.phase
        val expectedPhase = boss.bossType.getPhaseForHealth(boss.getHealthPercent())

        if (currentPhase != expectedPhase) {
            onBossPhaseChange?.invoke(boss, expectedPhase)
        }
    }

    /**
     * Отрисовка менеджера боссов.
     */
    fun render(canvas: Canvas, cameraX: Float = 0f) {
        // Отрисовка арены
        currentArena?.render(canvas, cameraX)

        // Отрисовка warning overlay
        warningOverlay.render(canvas)

        // Отрисовка health bar (поверх всего)
        if (isBossFight) {
            bossHealthBar.render(canvas)
        }
    }

    /**
     * Вызывается при смерти босса.
     */
    private fun onBossKilled() {
        val boss = currentBoss ?: return

        Log.i(TAG, "Босс побеждён: ${boss.bossType.id}")

        bossesDefeated++

        // Обновление прогресса
        updateBossProgress(boss.bossType, isVictory = true)

        // Callback
        onBossDefeated?.invoke(boss)

        // Обновление состояния
        _bossState.value = BossState.Victory(boss.bossType)
    }

    /**
     * Вызывается при смерти игрока.
     */
    fun onPlayerDeath() {
        if (!isBossFight) return

        Log.d(TAG, "Игрок умер во время боя с боссом")

        onPlayerDeathDuringBossFight?.invoke()

        // Босс не сбрасывается, игрок может попробовать снова
        _bossState.value = BossState.Defeat(currentBoss?.bossType ?: BossType.GIANT_SLIME)
    }

    /**
     * Завершение боя с боссом.
     */
    fun endBossFight() {
        val boss = currentBoss ?: return

        Log.d(TAG, "Завершение боя с боссом")

        // Удаление босса
        boss.markForDestroy()
        currentBoss = null

        // Очистка арены
        currentArena = null

        // Сброс состояния
        _bossState.value = BossState.None
    }

    /**
     * Сброс менеджера.
     */
    fun reset() {
        currentBoss?.markForDestroy()
        currentBoss = null
        currentArena = null
        bossHealthBar.reset()
        warningOverlay.reset()
        _bossState.value = BossState.None
    }

    // ============================================================================
    // ПРОГРЕСС
    // ============================================================================

    /**
     * Обновление прогресса босса.
     */
    private fun updateBossProgress(type: BossType, isVictory: Boolean) {
        val progress = bossProgressMap.getOrPut(type) {
            BossProgress(type)
        }

        if (isVictory) {
            progress.isDefeated = true
            progress.attempts++
        } else {
            progress.attempts++
        }

        // TODO: Обновить bestTime, maxDamageDealt
    }

    /**
     * Получение прогресса босса.
     */
    fun getBossProgress(type: BossType): BossProgress {
        return bossProgressMap.getOrPut(type) {
            BossProgress(type)
        }
    }

    /**
     * Проверка, побеждён ли босс.
     */
    fun hasDefeatedBoss(type: BossType): Boolean {
        return bossProgressMap[type]?.isDefeated == true
    }

    /**
     * Получение всех прогрессов.
     */
    fun getAllProgress(): List<BossProgress> {
        return bossProgressMap.values.toList()
    }

    /**
     * Запись урона по боссу.
     */
    fun recordDamageToBoss(amount: Int) {
        bossDamageDealt += amount
        currentBoss?.let { boss ->
            bossProgressMap[boss.bossType]?.maxDamageDealt =
                maxOf(bossProgressMap[boss.bossType]?.maxDamageDealt ?: 0, bossDamageDealt)
        }
    }

    // ============================================================================
    // УТИЛИТЫ
    // ============================================================================

    /**
     * Получение следующего босса.
     */
    fun getNextBossType(): BossType? {
        return BossType.ALL_TYPES.find { !hasDefeatedBoss(it) }
    }

    /**
     * Проверка, все ли боссы побеждены.
     */
    fun allBossesDefeated(): Boolean {
        return BossType.ALL_TYPES.all { hasDefeatedBoss(it) }
    }

    /**
     * Получение статистики.
     */
    fun getStats(): BossStats {
        return BossStats(
            bossesDefeated = bossesDefeated,
            bossDamageDealt = bossDamageDealt,
            totalBossFightTime = totalBossFightTime,
            bossesProgress = bossProgressMap.values.toList()
        )
    }

    /**
     * Освобождение ресурсов.
     */
    fun dispose() {
        reset()
        onBossStart = null
        onBossDefeated = null
        onBossPhaseChange = null
        onPlayerDeathDuringBossFight = null
    }

    // ============================================================================
    // DATA CLASSES
    // ============================================================================

    /**
     * Состояние менеджера боссов.
     */
    sealed class BossState {
        /** Нет активного босса */
        object None : BossState()

        /** Босс появляется */
        data class Spawning(val bossType: BossType) : BossState()

        /** Идёт бой */
        data class Fighting(val bossType: BossType) : BossState()

        /** Победа */
        data class Victory(val bossType: BossType) : BossState()

        /** Поражение */
        data class Defeat(val bossType: BossType) : BossState()
    }

    /**
     * Статистика боссов.
     */
    data class BossStats(
        val bossesDefeated: Int,
        val bossDamageDealt: Int,
        val totalBossFightTime: Float,
        val bossesProgress: List<BossProgress>
    )
}

/**
 * Прогресс босса.
 * Сохраняет информацию о попытках и достижениях.
 */
data class BossProgress(
    val bossType: BossType,
    var isDefeated: Boolean = false,
    var bestTime: Long = 0L, // в миллисекундах
    var attempts: Int = 0,
    var maxDamageDealt: Int = 0
) {
    /** Количество побед */
    var victories: Int = 0
        private set

    /** Увеличение счётчика побед */
    fun incrementVictories() {
        victories++
    }
}

/**
 * Extension функция для получения BossManager из GameManager.
 */
fun GameManager.getBossManager(): BossManager {
    return BossManager.getInstance()
}
