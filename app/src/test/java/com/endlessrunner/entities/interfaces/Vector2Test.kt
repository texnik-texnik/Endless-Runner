package com.endlessrunner.entities.interfaces

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Тесты для Vector2.
 */
class Vector2Test {

    @Test
    fun testConstructor() {
        val v = Vector2(3f, 4f)
        assertEquals(3f, v.x, 0.001f)
        assertEquals(4f, v.y, 0.001f)
    }

    @Test
    fun testDefaultConstructor() {
        val v = Vector2()
        assertEquals(0f, v.x, 0.001f)
        assertEquals(0f, v.y, 0.001f)
    }

    @Test
    fun testLength() {
        val v = Vector2(3f, 4f)
        assertEquals(5f, v.length, 0.001f)
    }

    @Test
    fun testLengthSquared() {
        val v = Vector2(3f, 4f)
        assertEquals(25f, v.lengthSquared, 0.001f)
    }

    @Test
    fun testNormalize() {
        val v = Vector2(3f, 4f)
        val normalized = v.normalize()
        assertEquals(0.6f, normalized.x, 0.001f)
        assertEquals(0.8f, normalized.y, 0.001f)
        assertEquals(1f, normalized.length, 0.001f)
    }

    @Test
    fun testAdd() {
        val v1 = Vector2(1f, 2f)
        val v2 = Vector2(3f, 4f)
        val result = v1 + v2
        assertEquals(4f, result.x, 0.001f)
        assertEquals(6f, result.y, 0.001f)
    }

    @Test
    fun testSubtract() {
        val v1 = Vector2(5f, 7f)
        val v2 = Vector2(2f, 3f)
        val result = v1 - v2
        assertEquals(3f, result.x, 0.001f)
        assertEquals(4f, result.y, 0.001f)
    }

    @Test
    fun testMultiply() {
        val v = Vector2(2f, 3f)
        val result = v * 2f
        assertEquals(4f, result.x, 0.001f)
        assertEquals(6f, result.y, 0.001f)
    }

    @Test
    fun testDivide() {
        val v = Vector2(6f, 8f)
        val result = v / 2f
        assertEquals(3f, result.x, 0.001f)
        assertEquals(4f, result.y, 0.001f)
    }

    @Test
    fun testDistanceTo() {
        val v1 = Vector2(0f, 0f)
        val v2 = Vector2(3f, 4f)
        assertEquals(5f, v1.distanceTo(v2), 0.001f)
    }

    @Test
    fun testDistanceSquaredTo() {
        val v1 = Vector2(0f, 0f)
        val v2 = Vector2(3f, 4f)
        assertEquals(25f, v1.distanceSquaredTo(v2), 0.001f)
    }

    @Test
    fun testCompanions() {
        assertEquals(0f, Vector2.Zero.x, 0.001f)
        assertEquals(0f, Vector2.Zero.y, 0.001f)

        assertEquals(0f, Vector2.Up.x, 0.001f)
        assertEquals(1f, Vector2.Up.y, 0.001f)

        assertEquals(0f, Vector2.Down.x, 0.001f)
        assertEquals(-1f, Vector2.Down.y, 0.001f)

        assertEquals(-1f, Vector2.Left.x, 0.001f)
        assertEquals(0f, Vector2.Left.y, 0.001f)

        assertEquals(1f, Vector2.Right.x, 0.001f)
        assertEquals(0f, Vector2.Right.y, 0.001f)
    }
}
