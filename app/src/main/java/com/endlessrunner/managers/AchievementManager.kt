package com.endlessrunner.managers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Менеджер достижений.
 */
class AchievementManager {

    private val _unlockedAchievements = MutableStateFlow<Set<String>>(emptySet())
    val unlockedAchievements: StateFlow<Set<String>> = _unlockedAchievements.asStateFlow()

    fun unlockAchievement(id: String) {
        val current = _unlockedAchievements.value.toMutableSet()
        if (current.add(id)) {
            _unlockedAchievements.value = current
        }
    }

    fun isUnlocked(id: String): Boolean = _unlockedAchievements.value.contains(id)
}
