package com.endlessrunner.bosses.achievements

import com.endlessrunner.bosses.BossType
import com.endlessrunner.bosses.managers.BossProgress

/**
 * Достижения, связанные с боссами.
 */
sealed class BossAchievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val reward: AchievementReward
) {
    /**
     * Победа над первым боссом.
     */
    object FirstBoss : BossAchievement(
        id = "first_boss",
        name = "Первая Кровь",
        description = "Победить первого босса",
        icon = "trophy_bronze",
        reward = AchievementReward.Gold(100)
    )

    /**
     * Победа над всеми боссами.
     */
    object AllBosses : BossAchievement(
        id = "all_bosses",
        name = "Убийца Боссов",
        description = "Победить всех боссов",
        icon = "trophy_gold",
        reward = AchievementReward.Gold(500)
    )

    /**
     * Победа над финальным боссом.
     */
    object FinalBoss : BossAchievement(
        id = "final_boss",
        name = "Легенда",
        description = "Победить финального босса",
        icon = "trophy_diamond",
        reward = AchievementReward.Title("Убийца Богов")
    )

    /**
     * Спидран босса.
     */
    data class SpeedrunBoss(
        val bossType: BossType,
        val timeLimit: Long // миллисекунды
    ) : BossAchievement(
        id = "speedrun_${bossType.id}",
        name = "Спидраннер",
        description = "Победить ${bossType.id} за $timeLimit мс",
        icon = "timer",
        reward = AchievementReward.Gold(200)
    )

    /**
     * Победа без получения урона.
     */
    object NoDamageBoss : BossAchievement(
        id = "no_damage_boss",
        name = "Неуязвимый",
        description = "Победить босса без получения урона",
        icon = "shield",
        reward = AchievementReward.Gold(300)
    )

    /**
     * Победа без использования апгрейдов.
     */
    object SoloBoss : BossAchievement(
        id = "solo_boss",
        name = "Одиночка",
        description = "Победить босса без апгрейдов",
        icon = "fist",
        reward = AchievementReward.Gold(250)
    )

    /**
     * Победа над всеми боссами без смерти.
     */
    object FlawlessRun : BossAchievement(
        id = "flawless_run",
        name = "Безупречный",
        description = "Победить всех боссов без единой смерти",
        icon = "crown",
        reward = AchievementReward.Title("Непобедимый")
    )

    /**
     * Нанесение большого урона за бой.
     */
    data class HighDamage(
        val damageThreshold: Int
    ) : BossAchievement(
        id = "high_damage_$damageThreshold",
        name = "Разрушитель",
        description = "Нанести $damageThreshold урона за один бой",
        icon = "sword",
        reward = AchievementReward.Gold(150)
    )

    /**
     * Быстрая победа над боссом.
     */
    data class QuickVictory(
        val bossType: BossType,
        val timeLimit: Long
    ) : BossAchievement(
        id = "quick_${bossType.id}",
        name = "Быстрая Победа",
        description = "Победить ${bossType.id} за $timeLimit секунд",
        icon = "lightning",
        reward = AchievementReward.Gold(100)
    )

    /**
     * Победа над боссом в ярости.
     */
    object EnrageVictory : BossAchievement(
        id = "enrage_victory",
        name = "Против Ярости",
        description = "Победить босса в режиме ярости",
        icon = "fire",
        reward = AchievementReward.Gold(200)
    )

    /**
     * Убийство миньонов.
     */
    data class MinionSlayer(
        val count: Int
    ) : BossAchievement(
        id = "minion_slayer_$count",
        name = "Охотник на Миньонов",
        description = "Уничтожить $count миньонов",
        icon = "bug",
        reward = AchievementReward.Gold(50)
    )

    /**
     * Уклонение от атак.
     */
    data class DodgeMaster(
        val dodgesCount: Int
    ) : BossAchievement(
        id = "dodge_master_$dodgesCount",
        name = "Мастер Уклонения",
        description = "Уклониться от $dodgesCount атак подряд",
        icon = "wind",
        reward = AchievementReward.Gold(100)
    )

    /**
     * Коллекционное достижение.
     */
    object BossCollector : BossAchievement(
        id = "boss_collector",
        name = "Коллекционер",
        description = "Победить каждого босса хотя бы 5 раз",
        icon = "collection",
        reward = AchievementReward.Skin("boss_hunter")
    )

    companion object {
        /** Все достижения */
        val ALL_ACHIEVEMENTS: List<BossAchievement> = listOf(
            FirstBoss,
            AllBosses,
            FinalBoss,
            NoDamageBoss,
            SoloBoss,
            FlawlessRun,
            EnrageVictory,
            BossCollector
        )

        /** Получение достижения по ID */
        fun getById(id: String): BossAchievement? = ALL_ACHIEVEMENTS.find { it.id == id }
    }
}

/**
 * Награда за достижение.
 */
sealed class AchievementReward {
    /** Золото */
    data class Gold(val amount: Int) : AchievementReward()

    /** Опыт */
    data class XP(val amount: Int) : AchievementReward()

    /** Заголовок/звание */
    data class Title(val title: String) : AchievementReward()

    /** Скинь */
    data class Skin(val skinId: String) : AchievementReward()

    /** Предмет */
    data class Item(val itemId: String, val count: Int) : AchievementReward()
}

/**
 * Менеджер достижений боссов.
 * Отслеживает прогресс и выдаёт награды.
 */
class BossAchievementManager {

    /** Разблокированные достижения */
    private val unlockedAchievements = mutableSetOf<String>()

    /** Прогресс по достижениям */
    private val achievementProgress = mutableMapOf<String, Int>()

    /** Callback при разблокировке достижения */
    var onAchievementUnlocked: ((BossAchievement) -> Unit)? = null

    /**
     * Проверка достижений после победы над боссом.
     */
    fun checkOnBossVictory(
        bossType: BossType,
        fightTime: Long,
        damageTaken: Int,
        damageDealt: Int,
        minionsKilled: Int,
        hasUpgrades: Boolean,
        deathsCount: Int
    ) {
        // Первая победа
        if (bossType == BossType.GIANT_SLIME) {
            unlockAchievement(BossAchievement.FirstBoss)
        }

        // Спидран
        val speedrunAchievement = BossAchievement.SpeedrunBoss(bossType, 180000) // 3 минуты
        if (fightTime < speedrunAchievement.timeLimit) {
            unlockAchievement(speedrunAchievement)
        }

        // Без урона
        if (damageTaken == 0) {
            unlockAchievement(BossAchievement.NoDamageBoss)
        }

        // Без апгрейдов
        if (!hasUpgrades) {
            unlockAchievement(BossAchievement.SoloBoss)
        }

        // Высокий урон
        if (damageDealt >= 5000) {
            unlockAchievement(BossAchievement.HighDamage(5000))
        }

        // Убийца миньонов
        if (minionsKilled >= 50) {
            unlockAchievement(BossAchievement.MinionSlayer(50))
        }

        // Проверка всех боссов
        checkAllBossesDefeated()
    }

    /**
     * Проверка победы над всеми боссами.
     */
    private fun checkAllBossesDefeated() {
        val defeatedBosses = BossType.ALL_TYPES.filter { isBossDefeated(it) }
        if (defeatedBosses.size == BossType.ALL_TYPES.size) {
            unlockAchievement(BossAchievement.AllBosses)
        }
    }

    /**
     * Проверка победы над финальным боссом.
     */
    fun checkOnFinalBossVictory(fightTime: Long) {
        unlockAchievement(BossAchievement.FinalBoss)

        // Спидран финального босса
        if (fightTime < 300000) { // 5 минут
            unlockAchievement(BossAchievement.QuickVictory(BossType.FINAL_BOSS, 300))
        }
    }

    /**
     * Проверка ярости.
     */
    fun checkEnrageVictory(bossType: BossType, wasEnraged: Boolean) {
        if (wasEnraged) {
            unlockAchievement(BossAchievement.EnrageVictory)
        }
    }

    /**
     * Обновление прогресса миньонов.
     */
    fun updateMinionKills(count: Int) {
        updateProgress("minion_slayer", count)
        if (count >= 50) {
            unlockAchievement(BossAchievement.MinionSlayer(50))
        }
        if (count >= 100) {
            unlockAchievement(BossAchievement.MinionSlayer(100))
        }
    }

    /**
     * Обновление прогресса уклонений.
     */
    fun updateDodges(count: Int) {
        updateProgress("dodge_master", count)
        if (count >= 20) {
            unlockAchievement(BossAchievement.DodgeMaster(20))
        }
    }

    /**
     * Проверка коллекционера.
     */
    fun checkCollector(bossProgresses: List<BossProgress>) {
        val allFiveTimes = bossProgresses.all { it.victories >= 5 }
        if (allFiveTimes) {
            unlockAchievement(BossAchievement.BossCollector)
        }
    }

    /**
     * Проверка безупречного забега.
     */
    fun checkFlawlessRun(totalDeaths: Int, allBossesDefeated: Boolean) {
        if (totalDeaths == 0 && allBossesDefeated) {
            unlockAchievement(BossAchievement.FlawlessRun)
        }
    }

    // ============================================================================
    // ВНУТРЕННИЕ МЕТОДЫ
    // ============================================================================

    /**
     * Разблокировка достижения.
     */
    private fun unlockAchievement(achievement: BossAchievement) {
        if (unlockedAchievements.contains(achievement.id)) return

        unlockedAchievements.add(achievement.id)
        onAchievementUnlocked?.invoke(achievement)

        // Выдача награды
        giveReward(achievement.reward)
    }

    /**
     * Выдача награды.
     */
    private fun giveReward(reward: AchievementReward) {
        when (reward) {
            is AchievementReward.Gold -> {
                // TODO: Добавить золото игроку
            }
            is AchievementReward.XP -> {
                // TODO: Добавить опыт игроку
            }
            is AchievementReward.Title -> {
                // TODO: Установить заголовок
            }
            is AchievementReward.Skin -> {
                // TODO: Разблокировать скин
            }
            is AchievementReward.Item -> {
                // TODO: Добавить предмет
            }
        }
    }

    /**
     * Обновление прогресса.
     */
    private fun updateProgress(achievementId: String, value: Int) {
        val current = achievementProgress[achievementId] ?: 0
        achievementProgress[achievementId] = maxOf(current, value)
    }

    /**
     * Проверка, побеждён ли босс.
     */
    private fun isBossDefeated(bossType: BossType): Boolean {
        // TODO: Проверить из BossManager
        return unlockedAchievements.contains("defeated_${bossType.id}")
    }

    /**
     * Получение всех разблокированных достижений.
     */
    fun getUnlockedAchievements(): List<BossAchievement> {
        return BossAchievement.ALL_ACHIEVEMENTS.filter { unlockedAchievements.contains(it.id) }
    }

    /**
     * Получение заблокированных достижений.
     */
    fun getLockedAchievements(): List<BossAchievement> {
        return BossAchievement.ALL_ACHIEVEMENTS.filter { !unlockedAchievements.contains(it.id) }
    }

    /**
     * Проверка, разблокировано ли достижение.
     */
    fun isUnlocked(achievementId: String): Boolean = unlockedAchievements.contains(achievementId)

    /**
     * Получение прогресса достижения.
     */
    fun getProgress(achievementId: String): Int = achievementProgress[achievementId] ?: 0

    /**
     * Сброс менеджера.
     */
    fun reset() {
        unlockedAchievements.clear()
        achievementProgress.clear()
    }
}
