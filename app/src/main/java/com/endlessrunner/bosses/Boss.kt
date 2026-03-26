package com.endlessrunner.bosses

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.endlessrunner.bosses.attacks.AttackPattern
import com.endlessrunner.bosses.attacks.AttackPatternFactory
import com.endlessrunner.bosses.minions.MinionSpawner
import com.endlessrunner.bosses.minions.MinionType
import com.endlessrunner.bosses.projectiles.Position2D
import com.endlessrunner.bosses.visuals.BossVisuals
import com.endlessrunner.components.PhysicsComponent
import com.endlessrunner.components.PositionComponent
import com.endlessrunner.components.RenderComponent
import com.endlessrunner.core.GameConstants
import com.endlessrunner.entities.Entity
import com.endlessrunner.entities.EntityManager

/**
 * Базовый класс босса.
 * Наследуется от Entity и управляет всеми аспектами боя с боссом.
 *
 * @property bossType Тип босса
 * @property startHealth Начальное здоровье
 * @property startDamage Начальный урон
 */
open class Boss(
    val bossType: BossType,
    startHealth: Int = bossType.baseHealth,
    startDamage: Int = bossType.baseDamage
) : Entity(tag = "boss") {

    // ============================================================================
    // КОМПОНЕНТЫ
    // ============================================================================

    /** Компонент позиции */
    val positionComponent: PositionComponent?
        get() = getComponent()

    /** Компонент рендеринга */
    val renderComponent: RenderComponent?
        get() = getComponent()

    /** Компонент физики */
    val physicsComponent: PhysicsComponent?
        get() = getComponent()

    // ============================================================================
    // СВОЙСТВА БОССА
    // ============================================================================

    /** Текущее здоровье */
    var health: Int = startHealth
        private set

    /** Максимальное здоровье */
    val maxHealth: Int = startHealth

    /** Текущий урон */
    var damage: Int = startDamage
        protected set

    /** Текущая фаза босса */
    var phase: BossPhase = BossPhase.Phase1(emptyList())
        private set

    /** Предыдущая фаза */
    private var previousPhase: BossPhase? = null

    /** Время в текущей фазе */
    var timeInPhase: Float = 0f
        private set

    /** Таймер перехода между фазами */
    var transitionTimer: Float = 0f

    /** Флаг неуязвимости */
    var isInvulnerable: Boolean = false
        protected set

    /** Текущий паттерн атаки */
    var currentAttackPattern: AttackPattern? = null
        private set

    /** Доступные паттерны атак для текущей фазы */
    private val availablePatterns = mutableListOf<AttackPattern>()

    /** Таймер выбора следующей атаки */
    private var attackTimer: Float = 0f

    /** Спавнер миньонов */
    var minionSpawner: MinionSpawner? = null
        private set

    /** Визуальные эффекты */
    private val visuals: BossVisuals = BossVisuals()

    /** Ссылка на EntityManager для спавна миньонов */
    var entityManager: EntityManager? = null

    /** Позиция игрока (кэшируется) */
    private var cachedPlayerPosition: Pair<Float, Float> = Pair(0f, 0f)

    /** Флаг смерти босса */
    var isDead: Boolean = false
        private set

    /** Флаг начала боя */
    var isFightStarted: Boolean = false
        private set

    /** Время боя */
    var fightTime: Float = 0f
        private set

    // ============================================================================
    // КОНФИГУРАЦИЯ
    // ============================================================================

    /** Текущая скорость движения */
    var currentMoveSpeed: Float = bossType.moveSpeed
        private set

    /** Множитель урона от фазы */
    private var phaseDamageMultiplier: Float = 1f

    /** Множитель скорости от фазы */
    private var phaseSpeedMultiplier: Float = 1f

    /** Щит босса (если есть) */
    var shieldHealth: Int = 0
        private set

    /** Максимальный щит */
    var maxShieldHealth: Int = 0
        private set

    // ============================================================================
    // ИНИЦИАЛИЗАЦИЯ
    // ============================================================================

    init {
        setupComponents()
        setupMinionSpawner()
    }

    /**
     * Настройка компонентов босса.
     */
    private fun setupComponents() {
        // Позиция
        addComponent(
            PositionComponent(
                x = 1000f,
                y = 600f
            )
        )

        // Рендеринг
        addComponent(
            RenderComponent(
                color = bossType.debugColor,
                width = bossType.width,
                height = bossType.height
            )
        )

        // Физика
        addComponent(
            PhysicsComponent(
                width = bossType.width,
                height = bossType.height,
                collisionLayer = GameConstants.LAYER_OBSTACLE,
                isTrigger = false
            )
        )
    }

    /**
     * Настройка спавнера миньонов.
     */
    private fun setupMinionSpawner() {
        minionSpawner = MinionSpawner(this)
    }

    // ============================================================================
    // ЖИЗНЕННЫЙ ЦИКЛ
    // ============================================================================

    override fun onActivate() {
        super.onActivate()
        health = maxHealth
        isDead = false
        isInvulnerable = false
        isFightStarted = false
        fightTime = 0f
        timeInPhase = 0f
        transitionTimer = 0f
        shieldHealth = 0
        maxShieldHealth = 0

        // Инициализация фазы
        enterPhase(bossType.phases.first())

        // Инициализация визуальных эффектов
        visuals.onBossActivated(this)

        // Подключение спавнера миньонов
        minionSpawner?.entityManager = entityManager
    }

    override fun update(deltaTime: Float) {
        if (!isActive || isDead) return

        if (isFightStarted) {
            fightTime += deltaTime
        }

        // Обновление времени в фазе
        if (!isInvulnerable && transitionTimer <= 0) {
            timeInPhase += deltaTime
        }

        // Обновление перехода между фазами
        if (transitionTimer > 0) {
            transitionTimer -= deltaTime
            if (transitionTimer <= 0) {
                onTransitionComplete()
            }
        }

        // Обновление текущей атаки
        updateAttackPattern(deltaTime)

        // Обновление миньонов
        minionSpawner?.update(deltaTime)

        // Обновление визуальных эффектов
        visuals.update(deltaTime)

        // Проверка перехода фазы
        checkPhaseTransition()

        super.update(deltaTime)
    }

    override fun render(canvas: Canvas) {
        if (!isActive) return

        // Рендеринг базовых компонентов
        super.render(canvas)

        // Рендеринг визуальных эффектов
        visuals.render(canvas, this)

        // Рендеринг миньонов
        minionSpawner?.render(canvas)

        // Рендеринг текущей атаки
        currentAttackPattern?.render(canvas)

        // Отладочная информация
        if (true) { // config.showDebugInfo
            renderDebugInfo(canvas)
        }
    }

    override fun reset() {
        super.reset()
        health = maxHealth
        isDead = false
        isInvulnerable = false
        isFightStarted = false
        fightTime = 0f
        timeInPhase = 0f
        transitionTimer = 0f
        shieldHealth = 0
        maxShieldHealth = 0
        currentAttackPattern = null
        availablePatterns.clear()
        visuals.reset()
        minionSpawner?.clearMinions()
    }

    // ============================================================================
    // УПРАВЛЕНИЕ ФАЗАМИ
    // ============================================================================

    /**
     * Вход в новую фазу.
     */
    fun enterPhase(newPhase: BossPhase) {
        previousPhase = phase
        phase = newPhase
        timeInPhase = 0f
        transitionTimer = 0f

        // Обновление множителей
        phaseDamageMultiplier = newPhase.damageMultiplier
        phaseSpeedMultiplier = newPhase.moveSpeedMultiplier
        currentMoveSpeed = bossType.moveSpeed * phaseSpeedMultiplier
        damage = bossType.baseDamage * phaseDamageMultiplier.toInt()

        // Обновление щита
        if (newPhase.hasShield && newPhase.shieldHealth > 0) {
            shieldHealth = newPhase.shieldHealth
            maxShieldHealth = newPhase.shieldHealth
        } else {
            shieldHealth = 0
            maxShieldHealth = 0
        }

        // Инициализация паттернов атак для новой фазы
        initializeAttackPatterns(newPhase.attackPatterns)

        // Уведомление о переходе
        onPhaseTransition(previousPhase, newPhase)

        // Визуальный эффект перехода
        visuals.onPhaseChange(this, newPhase)
    }

    /**
     * Инициализация паттернов атак.
     */
    private fun initializeAttackPatterns(patternTypes: List<PatternType>) {
        availablePatterns.clear()
        patternTypes.forEach { type ->
            val pattern = AttackPatternFactory.createPattern(type, this)
            availablePatterns.add(pattern)
        }
    }

    /**
     * Проверка перехода в новую фазу.
     */
    private fun checkPhaseTransition() {
        if (isInvulnerable || transitionTimer > 0) return

        val healthPercent = health.toFloat() / maxHealth
        val expectedPhase = bossType.getPhaseForHealth(healthPercent)

        if (expectedPhase != phase) {
            startPhaseTransition(expectedPhase)
        }

        // Проверка ярости
        checkEnrage()
    }

    /**
     * Начало перехода фазы.
     */
    private fun startPhaseTransition(newPhase: BossPhase) {
        isInvulnerable = true
        transitionTimer = 3f // 3 секунды неуязвимости

        // Прерывание текущей атаки
        currentAttackPattern?.interrupt()
        currentAttackPattern = null

        // Визуальный эффект
        visuals.onPhaseTransitionStart(this, phase, newPhase)
    }

    /**
     * Завершение перехода фазы.
     */
    private fun onTransitionComplete() {
        isInvulnerable = false
        enterPhase(phase) // Перезаход в фазу с новыми паттернами
        visuals.onPhaseTransitionEnd(this, phase)
    }

    /**
     * Проверка режима ярости.
     */
    private fun checkEnrage() {
        val healthPercent = health.toFloat() / maxHealth

        if (healthPercent < 0.1f && phase !is BossPhase.Enraged) {
            // Вход в ярость
            val enragedPhase = BossPhase.Enraged(phase)
            enterPhase(enragedPhase)
            visuals.onEnrageStart(this)
        }

        // Обновление таймера ярости
        if (phase is BossPhase.Enraged) {
            (phase as BossPhase.Enraged).updateEnrageTimer(1f / 60f)
            if (!(phase as BossPhase.Enraged).isEnrageActive()) {
                // Выход из ярости - смерть босса
                takeDamage(health)
            }
        }
    }

    /**
     * Вызывается при переходе фазы.
     */
    protected open fun onPhaseTransition(oldPhase: BossPhase?, newPhase: BossPhase) {
        // Переопределяется в наследниках
    }

    // ============================================================================
    // УПРАВЛЕНИЕ АТАКАМИ
    // ============================================================================

    /**
     * Обновление текущей атаки.
     */
    private fun updateAttackPattern(deltaTime: Float) {
        // Обновление кулдауна текущей атаки
        currentAttackPattern?.updateCooldown(deltaTime)

        // Если нет активной атаки, выбираем новую
        if (currentAttackPattern == null || currentAttackPattern?.isReady() == true) {
            selectAndStartAttack()
        }

        // Обновление активной атаки
        if (currentAttackPattern?.isActive() == true) {
            currentAttackPattern?.update(deltaTime)
        }
    }

    /**
     * Выбор и запуск новой атаки.
     */
    private fun selectAndStartAttack() {
        if (availablePatterns.isEmpty() || isInvulnerable) return

        // Выбор случайного готового паттерна
        val readyPatterns = availablePatterns.filter { it.isReady() }
        if (readyPatterns.isEmpty()) return

        val selectedPattern = readyPatterns.random()
        executeAttack(selectedPattern)
    }

    /**
     * Выполнение атаки.
     */
    fun executeAttack(pattern: AttackPattern) {
        if (isInvulnerable || !pattern.isReady()) return

        currentAttackPattern = pattern
        pattern.onStart()

        // Аудио предупреждение
        // TODO: audioManager.playSfx(SoundLibrary.BOSS_ATTACK_WARNING)
    }

    // ============================================================================
    // ПОЛУЧЕНИЕ УРОНА
    // ============================================================================

    /**
     * Получение урона боссом.
     *
     * @param amount Количество урона
     * @return true если босс умер
     */
    fun takeDamage(amount: Int): Boolean {
        if (isDead || isInvulnerable) return false

        // Проверка щита
        if (shieldHealth > 0) {
            shieldHealth -= amount
            visuals.onShieldHit(this, shieldHealth)

            if (shieldHealth <= 0) {
                shieldHealth = 0
                onShieldBroken()
            }
            return false
        }

        health -= amount
        visuals.onDamageTaken(this, amount)

        if (health <= 0) {
            health = 0
            onDeath()
            return true
        }

        return false
    }

    /**
     * Вызывается при разрушении щита.
     */
    protected open fun onShieldBroken() {
        visuals.onShieldBroken(this)
        // Временная оглушенность
        isInvulnerable = true
        transitionTimer = 2f
    }

    /**
     * Вызывается при смерти босса.
     */
    protected open fun onDeath() {
        isDead = true
        isInvulnerable = true
        currentAttackPattern?.interrupt()
        currentAttackPattern = null
        minionSpawner?.onBossDeath()
        visuals.onDeath(this)

        // TODO: audioManager.playSfx(SoundLibrary.BOSS_DEFEATED)
    }

    // ============================================================================
    // БОЕВЫЕ ДЕЙСТВИЯ
    // ============================================================================

    /**
     * Начало боя с боссом.
     */
    fun startFight() {
        isFightStarted = true
        visuals.onFightStart(this)
        // TODO: audioManager.playMusic(MusicLibrary.BOSS_BATTLE)
    }

    /**
     * Нанесение урона игроку.
     */
    fun dealDamageToPlayer(amount: Int) {
        // Делегируется GameManager
        // TODO: gameManager.onDamageTaken(amount)
    }

    /**
     * Нанесение урона по области.
     */
    fun dealDamageToArea(amount: Int, radius: Float) {
        val playerPos = getPlayerPosition()
        val bossPos = positionComponent ?: return

        val dx = playerPos.first - bossPos.x
        val dy = playerPos.second - bossPos.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        if (distance <= radius) {
            dealDamageToPlayer(amount)
        }
    }

    /**
     * Отталкивание игрока.
     */
    fun knockbackPlayer(knockbackX: Float, knockbackY: Float) {
        // TODO: Реализовать через физику игрока
    }

    /**
     * Притягивание игрока.
     */
    fun pullPlayer(pullX: Float, pullY: Float) {
        // TODO: Реализовать через физику игрока
    }

    // ============================================================================
    // СПЕЦИАЛЬНЫЕ ДЕЙСТВИЯ
    // ============================================================================

    /**
     * Телепортация босса.
     */
    fun teleportTo(x: Float, y: Float) {
        positionComponent?.setPosition(x, y)
        visuals.onTeleport(this, x, y)
    }

    /**
     * Прыжок босса.
     */
    fun performJump() {
        positionComponent?.vy = -800f
        visuals.onJump(this)
    }

    /**
     * Установка скорости по X.
     */
    fun setVelocityX(velocity: Float) {
        positionComponent?.vx = velocity
    }

    /**
     * Установка скорости по Y.
     */
    fun setVelocityY(velocity: Float) {
        positionComponent?.vy = velocity
    }

    /**
     * Удар по земле.
     */
    fun triggerGroundSlam() {
        visuals.onGroundSlam(this)
        // TODO: cameraSystem.shake(0.5f, 10f)
    }

    /**
     * Лазерная атака.
     */
    fun triggerLaserAttack() {
        visuals.onLaserCharge(this)
    }

    /**
     * Ударная волна.
     */
    fun triggerShockwave() {
        visuals.onShockwave(this)
    }

    /**
     * Создание чёрной дыры.
     */
    fun createBlackHole() {
        visuals.onBlackHoleStart(this)
    }

    /**
     * Удаление чёрной дыры.
     */
    fun removeBlackHole() {
        visuals.onBlackHoleEnd(this)
    }

    /**
     * Активация замедления времени.
     */
    fun activateTimeSlow(factor: Float) {
        visuals.onTimeSlowStart(this, factor)
    }

    /**
     * Деактивация замедления времени.
     */
    fun deactivateTimeSlow() {
        visuals.onTimeSlowEnd(this)
    }

    /**
     * Призыв миньонов.
     */
    fun spawnMinions(count: Int) {
        val bossPos = positionComponent ?: return
        minionSpawner?.spawnWave(
            count = count,
            positionX = bossPos.x,
            positionY = bossPos.y,
            type = MinionType.SLIME_SPLIT
        )
    }

    // ============================================================================
    // УТИЛИТЫ
    // ============================================================================

    /**
     * Получение позиции игрока.
     */
    fun getPlayerPosition(): Pair<Float, Float> {
        // Получение позиции игрока из EntityManager
        val player = entityManager?.getFirstByTag(com.endlessrunner.core.GameConstants.TAG_PLAYER)
        val playerPos = player?.getComponent<PositionComponent>()

        return if (playerPos != null) {
            val newPos = Pair(playerPos.x, playerPos.y)
            cachedPlayerPosition = newPos
            newPos
        } else {
            cachedPlayerPosition
        }
    }

    /**
     * Получение процента здоровья.
     */
    fun getHealthPercent(): Float = health.toFloat() / maxHealth

    /**
     * Получение процента щита.
     */
    fun getShieldPercent(): Float {
        return if (maxShieldHealth > 0) shieldHealth.toFloat() / maxShieldHealth else 0f
    }

    /**
     * Проверка, активен ли щит.
     */
    fun hasShield(): Boolean = shieldHealth > 0

    /**
     * Отрисовка отладочной информации.
     */
    private fun renderDebugInfo(canvas: Canvas) {
        val pos = positionComponent ?: return
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 24f
        }

        canvas.drawText(
            "Boss: ${bossType.id} HP: $health/$maxHealth Phase: ${phase.phaseName}",
            pos.x - 100f,
            pos.y - bossType.height / 2 - 20f,
            paint
        )
    }

    override fun toString(): String {
        return "Boss(type=${bossType.id}, health=$health/$maxHealth, phase=${phase.phaseName})"
    }
}

/**
 * Extension функция для получения всех активных боссов.
 */
fun EntityManager.getAllBosses(): List<Boss> {
    return getAllEntities()
        .filterIsInstance<Boss>()
        .filter { it.isActive && !it.isDead }
}
