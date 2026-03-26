package com.endlessrunner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class Player(private val context: Context) {
    
    var x = 100f
    var y = 0f
    private val width = 80f
    private val height = 120f
    
    private var velocityY = 0f
    private var isJumping = false
    
    private val gravity = 1.5f
    private val jumpForce = -35f
    
    private val bounds = RectF()
    
    private val paint = Paint().apply {
        color = Color.parseColor("#4fc3f7")
    }
    
    init {
        // Начальная позиция на "земле"
        y = 600f - height
    }
    
    fun update() {
        velocityY += gravity
        y += velocityY
        
        // Проверка земли
        val groundLevel = 600f - height
        if (y >= groundLevel) {
            y = groundLevel
            velocityY = 0f
            isJumping = false
        }
        
        // Обновление хитбокса
        bounds.set(x, y, x + width, y + height)
    }
    
    fun jump() {
        if (!isJumping) {
            velocityY = jumpForce
            isJumping = true
        }
    }
    
    fun render(canvas: Canvas) {
        canvas.drawRect(bounds, paint)
    }
    
    fun collects(coin: Coin): Boolean {
        return bounds.intersect(coin.bounds)
    }
}
