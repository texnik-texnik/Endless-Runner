package com.endlessrunner

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class Coin(screenWidth: Float, screenHeight: Float) {
    
    var x = screenWidth
    var y = (400..600).random().toFloat()
    private val size = 60f
    
    val bounds = RectF()
    
    private val paint = Paint().apply {
        color = Color.parseColor("#ffd700")
    }
    
    init {
        bounds.set(x, y, x + size, y + size)
    }
    
    fun update(speed: Float) {
        x -= speed
        bounds.offsetTo(x, y)
    }
    
    fun render(canvas: Canvas) {
        canvas.drawCircle(x + size/2, y + size/2, size/2, paint)
    }
    
    fun isOffScreen(screenWidth: Float): Boolean {
        return x + size < 0
    }
}
