package com.endlessrunner.systems

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Тесты для GameStateSystem.
 */
class GameStateSystemTest {

    private lateinit var gameStateSystem: GameStateSystem

    @Before
    fun setup() {
        gameStateSystem = GameStateSystem()
    }

    @Test
    fun testInitialState() {
        assertEquals(0, gameStateSystem.score)
        assertEquals(0, gameStateSystem.coins)
        assertEquals(1, gameStateSystem.level)
        assertEquals(0, gameStateSystem.highScore)
    }

    @Test
    fun testAddScore() {
        gameStateSystem.addScore(100)
        assertEquals(100, gameStateSystem.score)
    }

    @Test
    fun testAddCoins() {
        gameStateSystem.addCoins(5)
        assertEquals(5, gameStateSystem.coins)
        assertEquals(50, gameStateSystem.score)  // 5 * 10 = 50 очков
    }

    @Test
    fun testHighScoreUpdates() {
        gameStateSystem.addScore(100)
        assertEquals(100, gameStateSystem.highScore)

        gameStateSystem.addScore(50)
        assertEquals(150, gameStateSystem.highScore)  // 100 + 50 = 150 > 100
    }

    @Test
    fun testHighScoreDoesNotDecrease() {
        gameStateSystem.addScore(200)
        assertEquals(200, gameStateSystem.highScore)

        gameStateSystem.reset()
        assertEquals(0, gameStateSystem.score)
        assertEquals(200, gameStateSystem.highScore)  // Рекорд сохраняется
    }

    @Test
    fun testNextLevel() {
        assertEquals(1, gameStateSystem.level)
        gameStateSystem.nextLevel()
        assertEquals(2, gameStateSystem.level)
        gameStateSystem.nextLevel()
        assertEquals(3, gameStateSystem.level)
    }

    @Test
    fun testReset() {
        gameStateSystem.addScore(100)
        gameStateSystem.addCoins(10)
        gameStateSystem.nextLevel()
        gameStateSystem.nextLevel()

        gameStateSystem.reset()

        assertEquals(0, gameStateSystem.score)
        assertEquals(0, gameStateSystem.coins)
        assertEquals(1, gameStateSystem.level)
    }

    @Test
    fun testLoadHighScore() {
        gameStateSystem.loadHighScore(500)
        assertEquals(500, gameStateSystem.highScore)
    }
}
