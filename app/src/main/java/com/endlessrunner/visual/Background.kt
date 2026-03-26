package com.endlessrunner.visual

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import kotlin.math.abs

/**
 * Многослойный параллакс фон.
 * Поддерживает несколько слоёв с разной скоростью прокрутки.
 */
class Background {
    
    /** Слои фона */
    private val layers: MutableList<BackgroundLayer> = mutableListOf()
    
    /** Paint для отрисовки */
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    /** Градиент неба */
    private var skyGradient: LinearGradient? = null
    
    /** Цвет неба (если нет градиента) */
    var skyColor: Int = Color.parseColor("#87CEEB")
    
    /** Цвет горизонта */
    var horizonColor: Int = Color.parseColor("#E0F0FF")
    
    /** Ширина viewport */
    var viewportWidth: Float = 1920f
    
    /** Высота viewport */
    var viewportHeight: Float = 1080f
    
    /**
     * Добавление слоя фона.
     *
     * @param bitmap Bitmap слоя
     * @param speed Скорость параллакса (0-1)
     * @param repeat Повторять ли слой
     * @param zIndex Порядок отрисовки
     */
    fun addLayer(
        bitmap: Bitmap? = null,
        speed: Float = 0.5f,
        repeat: Boolean = true,
        zIndex: Int = 0,
        color: Int? = null
    ): BackgroundLayer {
        val layer = BackgroundLayer(bitmap, speed, repeat, color)
        layer.zIndex = zIndex
        layers.add(layer)
        layers.sortBy { it.zIndex }
        return layer
    }
    
    /**
     * Создание стандартных слоёв для раннера.
     */
    fun setupDefaultLayers() {
        // Небо (градиент)
        skyGradient = LinearGradient(
            0f, 0f, 0f, viewportHeight * 0.6f,
            Color.parseColor("#1A1A2E"),
            Color.parseColor("#16213E"),
            Shader.TileMode.CLAMP
        )
        
        // Дальний слой (горы) - медленный
        addLayer(speed = 0.1f, repeat = true, zIndex = 0)
        
        // Средний слой (здания/деревья)
        addLayer(speed = 0.3f, repeat = true, zIndex = 1)
        
        // Ближний слой (кусты) - быстрый
        addLayer(speed = 0.6f, repeat = true, zIndex = 2)
    }
    
    /**
     * Обновление фона.
     *
     * @param cameraX Позиция камеры X
     * @param deltaTime Время с последнего кадра
     */
    fun update(cameraX: Float, deltaTime: Float) {
        layers.forEach { it.update(cameraX, deltaTime) }
    }
    
    /**
     * Отрисовка фона.
     */
    fun render(canvas: Canvas) {
        // Отрисовка неба
        renderSky(canvas)
        
        // Отрисовка слоёв
        layers.forEach { layer ->
            layer.render(canvas, viewportWidth, viewportHeight, paint)
        }
    }
    
    /**
     * Отрисовка неба.
     */
    private fun renderSky(canvas: Canvas) {
        if (skyGradient != null) {
            paint.shader = skyGradient
            canvas.drawRect(0f, 0f, viewportWidth, viewportHeight * 0.7f, paint)
            paint.shader = null
        } else {
            paint.color = skyColor
            canvas.drawRect(0f, 0f, viewportWidth, viewportHeight * 0.7f, paint)
        }
    }
    
    /**
     * Очистка всех слоёв.
     */
    fun clear() {
        layers.forEach { it.dispose() }
        layers.clear()
    }
    
    /**
     * Удаление слоя.
     */
    fun removeLayer(layer: BackgroundLayer) {
        layers.remove(layer)
        layer.dispose()
    }
    
    /**
     * Получение слоя по индексу.
     */
    fun getLayer(index: Int): BackgroundLayer? = layers.getOrNull(index)
    
    /**
     * Количество слоёв.
     */
    fun getLayerCount(): Int = layers.size
}

/**
 * Слой фона.
 */
class BackgroundLayer(
    /** Bitmap слоя */
    var bitmap: Bitmap?,
    
    /** Скорость параллакса */
    var speed: Float = 0.5f,
    
    /** Повторять ли */
    var repeat: Boolean = true,
    
    /** Цвет (если нет bitmap) */
    var color: Int? = null
) {
    /** Смещение слоя */
    var offsetX: Float = 0f
    
    /** Порядок отрисовки */
    var zIndex: Int = 0
    
    /** Альфа-канал */
    var alpha: Int = 255
    
    /** Масштаб */
    var scale: Float = 1f
    
    /** Смещение по Y */
    var offsetY: Float = 0f
    
    /**
     * Обновление слоя.
     */
    fun update(cameraX: Float, deltaTime: Float) {
        offsetX = cameraX * speed
    }
    
    /**
     * Отрисовка слоя.
     */
    fun render(canvas: Canvas, viewportWidth: Float, viewportHeight: Float, paint: Paint) {
        canvas.save()
        
        try {
            paint.alpha = alpha
            
            if (bitmap != null) {
                renderBitmap(canvas, viewportWidth, viewportHeight, paint)
            } else if (color != null) {
                renderColor(canvas, viewportWidth, viewportHeight, paint)
            }
        } finally {
            canvas.restore()
        }
    }
    
    /**
     * Отрисовка bitmap.
     */
    private fun renderBitmap(canvas: Canvas, viewportWidth: Float, viewportHeight: Float, paint: Paint) {
        val bmp = bitmap ?: return
        
        val drawX = -offsetX * viewportWidth
        
        if (repeat) {
            // Повторение фона
            var x = drawX % (bmp.width * scale)
            if (x > 0) x -= bmp.width * scale
            
            while (x < viewportWidth) {
                if (scale != 1f) {
                    canvas.save()
                    canvas.scale(scale, scale, x, offsetY)
                    canvas.drawBitmap(bmp, x, offsetY, paint)
                    canvas.restore()
                } else {
                    canvas.drawBitmap(bmp, x, offsetY, paint)
                }
                x += bmp.width * scale
            }
        } else {
            // Одиночное изображение
            if (scale != 1f) {
                canvas.save()
                canvas.scale(scale, scale, drawX, offsetY)
                canvas.drawBitmap(bmp, drawX, offsetY, paint)
                canvas.restore()
            } else {
                canvas.drawBitmap(bmp, drawX, offsetY, paint)
            }
        }
    }
    
    /**
     * Отрисовка цветом (процедурный фон).
     */
    private fun renderColor(canvas: Canvas, viewportWidth: Float, viewportHeight: Float, paint: Paint) {
        paint.color = color ?: return
        
        if (repeat) {
            val segmentWidth = viewportWidth / 5f
            var x = -offsetX * segmentWidth % segmentWidth
            if (x > 0) x -= segmentWidth
            
            while (x < viewportWidth) {
                canvas.drawRect(x, offsetY, x + segmentWidth, viewportHeight, paint)
                x += segmentWidth
            }
        } else {
            canvas.drawRect(0f, offsetY, viewportWidth, viewportHeight, paint)
        }
    }
    
    /**
     * Освобождение ресурсов.
     */
    fun dispose() {
        // Bitmap не освобождаем - он управляется ResourceManager
        bitmap = null
    }
}

/**
 * Декоративные элементы фона.
 */
class Decoration {
    
    /** Список декораций */
    private val decorations: MutableList<DecorationElement> = mutableListOf()
    
    /**
     * Добавление декорации.
     */
    fun addDecoration(
        type: DecorationType,
        x: Float,
        y: Float,
        scale: Float = 1f,
        rotation: Float = 0f
    ) {
        decorations.add(DecorationElement(type, x, y, scale, rotation))
    }
    
    /**
     * Отрисовка всех декораций.
     */
    fun render(canvas: Canvas, cameraX: Float) {
        // Сортировка по Y для псевдо-глубины
        decorations
            .filter { it.isVisible(cameraX) }
            .sortedBy { it.y }
            .forEach { it.render(canvas, cameraX) }
    }
    
    /**
     * Очистка декораций.
     */
    fun clear() {
        decorations.clear()
    }
    
    /**
     * Удаление декораций за пределами.
     */
    fun cullOutOfBounds(cameraX: Float, viewportWidth: Float) {
        decorations.removeAll { !it.isVisible(cameraX) }
    }
}

/**
 * Типы декораций.
 */
enum class DecorationType {
    CLOUD,      // Облако
    TREE,       // Дерево
    BUSH,       // Куст
    ROCK,       // Камень
    FLOWER,     // Цветок
    GRASS,      // Трава
    BUILDING,   // Здание
    FENCE,      // Забор
    LAMP        // Фонарь
}

/**
 * Элемент декорации.
 */
class DecorationElement(
    /** Тип декорации */
    val type: DecorationType,
    
    /** Позиция X */
    val x: Float,
    
    /** Позиция Y */
    val y: Float,
    
    /** Масштаб */
    val scale: Float = 1f,
    
    /** Вращение */
    val rotation: Float = 0f
) {
    /** Ширина элемента */
    val width: Float = when (type) {
        DecorationType.CLOUD -> 150f
        DecorationType.TREE -> 80f
        DecorationType.BUSH -> 60f
        DecorationType.ROCK -> 50f
        DecorationType.FLOWER -> 20f
        DecorationType.GRASS -> 15f
        DecorationType.BUILDING -> 200f
        DecorationType.FENCE -> 100f
        DecorationType.LAMP -> 30f
    } * scale
    
    /** Высота элемента */
    val height: Float = when (type) {
        DecorationType.CLOUD -> 60f
        DecorationType.TREE -> 150f
        DecorationType.BUSH -> 50f
        DecorationType.ROCK -> 40f
        DecorationType.FLOWER -> 30f
        DecorationType.GRASS -> 20f
        DecorationType.BUILDING -> 300f
        DecorationType.FENCE -> 80f
        DecorationType.LAMP -> 120f
    } * scale
    
    /**
     * Проверка видимости.
     */
    fun isVisible(cameraX: Float): Boolean {
        val screenX = x - cameraX
        return screenX > -width && screenX < 2000f // viewportWidth + запас
    }
    
    /**
     * Отрисовка элемента.
     */
    fun render(canvas: Canvas, cameraX: Float) {
        val screenX = x - cameraX
        
        canvas.save()
        
        try {
            canvas.translate(screenX, y)
            canvas.scale(scale, scale)
            canvas.rotate(rotation)
            
            when (type) {
                DecorationType.CLOUD -> renderCloud(canvas)
                DecorationType.TREE -> renderTree(canvas)
                DecorationType.BUSH -> renderBush(canvas)
                DecorationType.ROCK -> renderRock(canvas)
                DecorationType.FLOWER -> renderFlower(canvas)
                DecorationType.GRASS -> renderGrass(canvas)
                DecorationType.BUILDING -> renderBuilding(canvas)
                DecorationType.FENCE -> renderFence(canvas)
                DecorationType.LAMP -> renderLamp(canvas)
            }
            
        } finally {
            canvas.restore()
        }
    }
    
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private fun renderCloud(canvas: Canvas) {
        paint.color = Color.parseColor("#FFFFFF")
        paint.alpha = 200
        
        // Несколько кругов для облака
        canvas.drawCircle(0f, 0f, 30f, paint)
        canvas.drawCircle(-25f, 5f, 25f, paint)
        canvas.drawCircle(25f, 5f, 25f, paint)
        canvas.drawCircle(0f, 10f, 28f, paint)
    }
    
    private fun renderTree(canvas: Canvas) {
        // Ствол
        paint.color = Color.parseColor("#8B4513")
        canvas.drawRect(-10f, 50f, 10f, 150f, paint)
        
        // Крона (треугольник)
        paint.color = Color.parseColor("#228B22")
        val path = android.graphics.Path().apply {
            moveTo(0f, -50f)
            lineTo(-40f, 50f)
            lineTo(40f, 50f)
            close()
        }
        canvas.drawPath(path, paint)
    }
    
    private fun renderBush(canvas: Canvas) {
        paint.color = Color.parseColor("#2E8B2E")
        
        // Несколько кругов для куста
        canvas.drawCircle(0f, 0f, 25f, paint)
        canvas.drawCircle(-20f, 10f, 20f, paint)
        canvas.drawCircle(20f, 10f, 20f, paint)
    }
    
    private fun renderRock(canvas: Canvas) {
        paint.color = Color.parseColor("#696969")
        
        val path = android.graphics.Path().apply {
            moveTo(-25f, 20f)
            lineTo(-15f, -15f)
            lineTo(0f, -20f)
            lineTo(15f, -10f)
            lineTo(25f, 20f)
            close()
        }
        canvas.drawPath(path, paint)
    }
    
    private fun renderFlower(canvas: Canvas) {
        // Стебель
        paint.color = Color.parseColor("#228B22")
        paint.strokeWidth = 3f
        canvas.drawLine(0f, 0f, 0f, 30f, paint)
        
        // Лепестки
        paint.color = Color.parseColor("#FF69B4")
        paint.strokeWidth = 1f
        canvas.drawCircle(0f, 0f, 8f, paint)
        
        // Центр
        paint.color = Color.parseColor("#FFFF00")
        canvas.drawCircle(0f, 0f, 4f, paint)
    }
    
    private fun renderGrass(canvas: Canvas) {
        paint.color = Color.parseColor("#32CD32")
        paint.strokeWidth = 2f
        
        // Несколько травинок
        canvas.drawLine(0f, 0f, -5f, -15f, paint)
        canvas.drawLine(0f, 0f, 0f, -18f, paint)
        canvas.drawLine(0f, 0f, 5f, -15f, paint)
    }
    
    private fun renderBuilding(canvas: Canvas) {
        // Основное здание
        paint.color = Color.parseColor("#4A4A4A")
        canvas.drawRect(-100f, -300f, 100f, 0f, paint)
        
        // Окна
        paint.color = Color.parseColor("#FFFF00")
        for (row in 0..5) {
            for (col in -2..2) {
                val wx = col * 40f
                val wy = -280f + row * 50f
                canvas.drawRect(wx - 15f, wy - 30f, wx + 15f, wy, paint)
            }
        }
    }
    
    private fun renderFence(canvas: Canvas) {
        paint.color = Color.parseColor("#8B4513")
        
        // Доски забора
        for (i in -2..2) {
            val x = i * 40f
            canvas.drawRect(x - 15f, -80f, x + 15f, 0f, paint)
            // Острие сверху
            val path = android.graphics.Path().apply {
                moveTo(x - 15f, -80f)
                lineTo(x, -95f)
                lineTo(x + 15f, -80f)
            }
            canvas.drawPath(path, paint)
        }
        
        // Горизонтальные планки
        canvas.drawRect(-100f, -50f, 100f, -40f, paint)
        canvas.drawRect(-100f, -20f, 100f, -10f, paint)
    }
    
    private fun renderLamp(canvas: Canvas) {
        // Столб
        paint.color = Color.parseColor("#2F2F2F")
        canvas.drawRect(-8f, -120f, 8f, 0f, paint)
        
        // Фонарь
        paint.color = Color.parseColor("#FFD700")
        canvas.drawCircle(0f, -130f, 20f, paint)
        
        // Свет
        paint.color = Color.parseColor("#40FFFF00")
        val path = android.graphics.Path().apply {
            moveTo(-15f, -125f)
            lineTo(-50f, -80f)
            lineTo(50f, -80f)
            lineTo(15f, -125f)
            close()
        }
        canvas.drawPath(path, paint)
    }
}

/**
 * Extension функция для создания случайных декораций.
 */
fun Decoration.addRandomDecorations(
    startX: Float,
    endX: Float,
    groundY: Float,
    count: Int
) {
    val random = kotlin.random.Random
    val types = DecorationType.values()
    
    repeat(count) {
        val x = startX + random.nextFloat() * (endX - startX)
        val type = types.random()
        
        val y = when (type) {
            DecorationType.CLOUD -> groundY - 400f - random.nextFloat() * 200f
            DecorationType.BUILDING -> groundY
            else -> groundY
        }
        
        val scale = 0.8f + random.nextFloat() * 0.4f
        val rotation = (random.nextFloat() - 0.5f) * 20f
        
        addDecoration(type, x, y, scale, rotation)
    }
}
