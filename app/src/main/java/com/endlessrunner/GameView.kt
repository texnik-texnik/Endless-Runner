package com.endlessrunner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

    private val holder: SurfaceHolder = holder
    private val thread: Thread = Thread(this)
    private var running = false
    
    // Игровые объекты
    private val player = Player(context)
    private val coins = mutableListOf<Coin>()
    private var score = 0
    private var coinsCollected = 0
    
    // Краски для отрисовки
    private val bgPaint = Paint().apply { color = Color.parseColor("#1a237e") }
    private val textPaint = Paint().apply { 
        color = Color.WHITE 
        textSize = 48f
        isFakeBoldText = true
    }
    
    // Скорость игры
    private var gameSpeed = 5f
    
    override fun surfaceCreated(holder: SurfaceHolder) {
        running = true
        thread.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        try {
            thread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    fun resume() {
        if (!running) {
            running = true
            thread.start()
        }
    }

    fun pause() {
        running = false
    }

    override fun run() {
        while (running) {
            update()
            render()
            sleep(16) // ~60 FPS
        }
    }

    private fun update() {
        // Обновление игрока
        player.update()
        
        // Спавн монет
        if (coins.size < 3 && Math.random() < 0.02) {
            coins.add(Coin(width.toFloat(), height.toFloat()))
        }
        
        // Обновление монет
        coins.removeAll { coin ->
            coin.update(gameSpeed)
            // Проверка сбора
            if (player.collects(coin)) {
                coinsCollected++
                score += 10
                true
            } else {
                coin.isOffScreen(width.toFloat())
            }
        }
        
        // Увеличение скорости
        gameSpeed += 0.001f
    }

    private fun render() {
        if (!holder.surface.isValid) return
        
        val canvas: Canvas = holder.lockCanvas() ?: return
        
        // Очистка
        canvas.drawColor(Color.parseColor("#1a237e"))
        
        // Отрисовка игрока
        player.render(canvas)
        
        // Отрисовка монет
        coins.forEach { it.render(canvas) }
        
        // HUD
        canvas.drawText("Счёт: $score", 20f, 60f, textPaint)
        canvas.drawText("Монеты: $coinsCollected", 20f, 120f, textPaint)
        
        holder.unlockCanvasAndPost(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            player.jump()
            return true
        }
        return super.onTouchEvent(event)
    }
}
