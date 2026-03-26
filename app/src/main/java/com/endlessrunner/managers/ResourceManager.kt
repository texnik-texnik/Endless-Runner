package com.endlessrunner.managers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import com.endlessrunner.animation.Animation
import com.endlessrunner.animation.Sprite
import com.endlessrunner.animation.AnimationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Менеджер ресурсов.
 * Загружает и кэширует Bitmap, Animation, Sprite и другие ресурсы.
 * Использует LRU кэш для эффективного управления памятью.
 *
 * @param context Android Context
 * @param memoryCacheSize Размер кэша в байтах (по умолчанию 1/8 доступной памяти)
 */
class ResourceManager(
    private val context: Context,
    memoryCacheSize: Int = (Runtime.getRuntime().maxMemory() / 8).toInt()
) {
    companion object {
        private const val TAG = "ResourceManager"

        /** Префикс для ресурсов в assets */
        private const val ASSETS_PREFIX = "file:///"

        /** Префикс для drawable ресурсов */
        private const val DRAWABLE_PREFIX = "drawable://"
    }
    
    /** LRU кэш для Bitmap */
    private val bitmapCache: LruCache<String, Bitmap>
    
    /** Кэш для анимаций */
    private val animationCache: MutableMap<String, Animation> = mutableMapOf()
    
    /** Кэш для спрайтов */
    private val spriteCache: MutableMap<String, Sprite> = mutableMapOf()

    /** Кэш для загруженных ресурсов */
    private val loadedResources: MutableMap<String, Any> = mutableMapOf()

    /** Флаг инициализации */
    private var isInitialized: Boolean = false

    /** Callback на ошибку загрузки */
    var onLoadError: ((String, Throwable) -> Unit)? = null
    
    init {
        bitmapCache = object : LruCache<String, Bitmap>(memoryCacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // Размер в байтах
                return bitmap.byteCount
            }
            
            override fun entryRemoved(
                evicted: Boolean,
                key: String,
                oldValue: Bitmap,
                newValue: Bitmap?
            ) {
                if (evicted) {
                    Log.d(TAG, "Кэш удалён: $key")
                    if (!oldValue.isRecycled) {
                        oldValue.recycle()
                    }
                }
            }
        }
        
        Log.d(TAG, "ResourceManager инициализирован, размер кэша: ${memoryCacheSize / 1024}KB")
    }
    
    // ============================================================================
    // ИНИЦИАЛИЗАЦИЯ
    // ============================================================================
    
    /**
     * Инициализация менеджера.
     */
    fun initialize() {
        if (isInitialized) {
            Log.w(TAG, "Уже инициализирован")
            return
        }
        
        isInitialized = true
        Log.i(TAG, "ResourceManager инициализирован")
    }
    
    // ============================================================================
    // ЗАГРУЗКА BITMAP
    // ============================================================================
    
    /**
     * Загрузка Bitmap из assets.
     * 
     * @param path Путь к файлу в assets
     * @param config Конфигурация декодирования
     * @return Bitmap или null при ошибке
     */
    suspend fun loadBitmap(
        path: String,
        config: BitmapFactory.Options = BitmapFactory.Options()
    ): Bitmap? = withContext(Dispatchers.IO) {
        // Проверка кэша
        bitmapCache.get(path)?.let {
            Log.d(TAG, "Найдено в кэше: $path")
            return@withContext it
        }
        
        try {
            context.assets.open(path).use { inputStream ->
                val bitmap = decodeBitmap(inputStream, config)
                
                if (bitmap != null) {
                    bitmapCache.put(path, bitmap)
                    Log.d(TAG, "Загружено: $path (${bitmap.width}x${bitmap.height})")
                }
                
                bitmap
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки Bitmap: $path", e)
            onLoadError?.invoke(path, e)
            null
        }
    }
    
    /**
     * Загрузка Bitmap из drawable.
     * 
     * @param resId ID ресурса
     * @return Bitmap или null при ошибке
     */
    suspend fun loadBitmap(resId: Int): Bitmap? = withContext(Dispatchers.IO) {
        val key = "drawable://$resId"
        
        bitmapCache.get(key)?.let {
            return@withContext it
        }
        
        try {
            val bitmap = BitmapFactory.decodeResource(context.resources, resId)
            
            if (bitmap != null) {
                bitmapCache.put(key, bitmap)
                Log.d(TAG, "Загружено drawable: $resId")
            }
            
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки drawable: $resId", e)
            onLoadError?.invoke(key, e)
            null
        }
    }
    
    /**
     * Загрузка Bitmap с декодированием.
     */
    private fun decodeBitmap(
        inputStream: InputStream,
        options: BitmapFactory.Options
    ): Bitmap? {
        // Сначала получаем размеры
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(inputStream, null, options)
        
        // Вычисляем sample size
        options.inSampleSize = calculateSampleSize(options)
        options.inJustDecodeBounds = false
        
        // Перематываем stream
        inputStream.reset()
        
        // Декодируем с правильным sample size
        return BitmapFactory.decodeStream(inputStream, null, options)
    }
    
    /**
     * Расчёт sample size для оптимизации памяти.
     */
    private fun calculateSampleSize(options: BitmapFactory.Options): Int {
        val height = options.outHeight
        val width = options.outWidth
        
        // Максимальный размер для спрайтов
        val maxSize = 512
        
        var sampleSize = 1
        
        if (height > maxSize || width > maxSize) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while (halfHeight / sampleSize >= maxSize && 
                   halfWidth / sampleSize >= maxSize) {
                sampleSize *= 2
            }
        }
        
        return sampleSize
    }
    
    // ============================================================================
    // ПОЛУЧЕНИЕ РЕСУРСОВ
    // ============================================================================
    
    /**
     * Получение Bitmap из кэша.
     * 
     * @param path Путь к ресурсу
     * @return Bitmap или null
     */
    fun getBitmap(path: String): Bitmap? {
        return bitmapCache.get(path)
    }
    
    /**
     * Получение или загрузка Bitmap.
     * 
     * @param path Путь к ресурсу
     * @return Bitmap или null
     */
    suspend fun getOrLoadBitmap(path: String): Bitmap? {
        return getBitmap(path) ?: loadBitmap(path)
    }
    
    /**
     * Получение Bitmap с placeholder.
     * 
     * @param path Путь к ресурсу
     * @param placeholder Placeholder Bitmap
     * @return Bitmap
     */
    suspend fun getBitmapWithPlaceholder(
        path: String,
        placeholder: Bitmap
    ): Bitmap {
        return getBitmap(path) ?: loadBitmap(path) ?: placeholder
    }
    
    // ============================================================================
    // ПРЕФЕТЧИНГ
    // ============================================================================
    
    /**
     * Предварительная загрузка ресурсов.
     * 
     * @param paths Список путей
     */
    suspend fun prefetchBitmaps(vararg paths: String) {
        withContext(Dispatchers.IO) {
            paths.forEach { path ->
                if (bitmapCache.get(path) == null) {
                    loadBitmap(path)
                }
            }
        }
    }
    
    /**
     * Предварительная загрузка из списка.
     */
    suspend fun prefetchBitmaps(paths: Collection<String>) {
        prefetchBitmaps(*paths.toTypedArray())
    }
    
    // ============================================================================
    // УПРАВЛЕНИЕ КЭШЕМ
    // ============================================================================
    
    /**
     * Очистка кэша.
     */
    fun clearCache() {
        Log.d(TAG, "Очистка кэша")
        bitmapCache.evictAll()
    }
    
    /**
     * Удаление конкретного ресурса из кэша.
     */
    fun removeFromCache(path: String) {
        bitmapCache.remove(path)
    }
    
    /**
     * Получение размера кэша.
     */
    fun getCacheSize(): Int = bitmapCache.size()
    
    /**
     * Получение максимального размера кэша.
     */
    fun getMaxCacheSize(): Int = bitmapCache.maxSize()
    
    /**
     * Получение статистики кэша.
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            size = bitmapCache.size(),
            maxSize = bitmapCache.maxSize(),
            hitCount = bitmapCache.hitCount(),
            missCount = bitmapCache.missCount(),
            putCount = bitmapCache.putCount()
        )
    }
    
    // ============================================================================
    // ОСВОБОЖДЕНИЕ
    // ============================================================================
    
    /**
     * Освобождение ресурсов.
     */
    fun dispose() {
        Log.d(TAG, "Освобождение ResourceManager")
        
        clearCache()
        loadedResources.clear()
        onLoadError = null
        isInitialized = false
    }
    
    /**
     * Принудительная сборка мусора.
     */
    fun forceGc() {
        System.gc()
        Log.d(TAG, "Вызван GC")
    }
    
    // ============================================================================
    // ЗАГРУЗКА АНИМАЦИЙ И СПРАЙТОВ
    // ============================================================================
    
    /**
     * Загрузка спрайта из спрайт-листа.
     *
     * @param path Путь к спрайт-листу
     * @param frameWidth Ширина кадра
     * @param frameHeight Высота кадра
     * @param frameCount Количество кадров
     * @param fps Кадры в секунду
     * @param isLooping Зациклена ли анимация
     * @return Animation или null
     */
    suspend fun loadSpriteSheet(
        path: String,
        frameWidth: Int,
        frameHeight: Int,
        frameCount: Int,
        fps: Float = 10f,
        isLooping: Boolean = true
    ): Animation? = withContext(Dispatchers.IO) {
        val bitmap = getOrLoadBitmap(path) ?: return@withContext null
        
        val frames = mutableListOf<Bitmap>()
        
        // Разбиение спрайт-листа на кадры
        for (i in 0 until frameCount) {
            val x = (i % (bitmap.width / frameWidth)) * frameWidth
            val y = (i / (bitmap.width / frameWidth)) * frameHeight
            
            val frame = Bitmap.createBitmap(bitmap, x, y, frameWidth, frameHeight)
            frames.add(frame)
        }
        
        Animation(frames, frameDuration = 1f / fps, isLooping = isLooping)
    }
    
    /**
     * Загрузка анимации из последовательности файлов.
     *
     * @param pathPrefix Префикс пути (например, "animations/run/frame_")
     * @param frameCount Количество кадров
     * @param fps Кадры в секунду
     * @param isLooping Зациклена ли анимация
     * @return Animation или null
     */
    suspend fun loadAnimation(
        pathPrefix: String,
        frameCount: Int,
        fps: Float = 10f,
        isLooping: Boolean = true
    ): Animation? = withContext(Dispatchers.IO) {
        val frames = mutableListOf<Bitmap>()
        
        for (i in 0 until frameCount) {
            val path = "${pathPrefix}${i.toString().padStart(3, '0')}.png"
            val bitmap = getOrLoadBitmap(path)
            if (bitmap != null) {
                frames.add(bitmap)
            }
        }
        
        if (frames.isEmpty()) {
            Log.w(TAG, "Не найдено кадров для анимации: $pathPrefix")
            return@withContext null
        }
        
        Animation(frames, frameDuration = 1f / fps, isLooping = isLooping)
    }
    
    /**
     * Получение или загрузка анимации.
     */
    fun getAnimation(name: String): Animation? {
        return animationCache[name]
    }
    
    /**
     * Кэширование анимации.
     */
    fun cacheAnimation(name: String, animation: Animation) {
        animationCache[name] = animation
        Log.d(TAG, "Закэширована анимация: $name")
    }
    
    /**
     * Получение или загрузка спрайта.
     */
    fun getSprite(name: String): Sprite? {
        return spriteCache[name]
    }
    
    /**
     * Кэширование спрайта.
     */
    fun cacheSprite(name: String, sprite: Sprite) {
        spriteCache[name] = sprite
        Log.d(TAG, "Закэширован спрайт: $name")
    }
    
    /**
     * Предзагрузка набора анимаций.
     */
    suspend fun preloadAnimations(vararg configs: AnimationConfig) {
        withContext(Dispatchers.IO) {
            configs.forEach { config ->
                if (!animationCache.containsKey(config.name)) {
                    val animation = loadAnimation(
                        config.pathPrefix,
                        config.frameCount,
                        config.fps,
                        config.isLooping
                    )
                    animation?.let {
                        animationCache[config.name] = it
                    }
                }
            }
        }
    }
    
    /**
     * Очистка кэша анимаций.
     */
    fun clearAnimationCache() {
        animationCache.clear()
        Log.d(TAG, "Кэш анимаций очищен")
    }
    
    /**
     * Очистка кэша спрайтов.
     */
    fun clearSpriteCache() {
        spriteCache.clear()
        Log.d(TAG, "Кэш спрайтов очищен")
    }
    
    /**
     * Data class для конфигурации анимации.
     */
    data class AnimationConfig(
        val name: String,
        val pathPrefix: String,
        val frameCount: Int,
        val fps: Float = 10f,
        val isLooping: Boolean = true
    )
    
    /**
     * Data class для статистики кэша.
     */
    data class CacheStats(
        val size: Int,
        val maxSize: Int,
        val hitCount: Int,
        val missCount: Int,
        val putCount: Int
    ) {
        /** Процент попаданий в кэш */
        val hitRate: Float
            get() {
                val total = hitCount + missCount
                return if (total == 0) 0f else hitCount.toFloat() / total
            }
        
        /** Процент использования кэша */
        val utilization: Float
            get() = size.toFloat() / maxSize
    }
}

/**
 * Extension функция для загрузки нескольких Bitmap.
 */
suspend fun ResourceManager.loadBitmaps(vararg paths: String): Map<String, Bitmap?> {
    return paths.associateWith { loadBitmap(it) }
}

/**
 * Extension функция для получения всех загруженных Bitmap.
 */
fun ResourceManager.getAllBitmaps(): Map<String, Bitmap> {
    val result = mutableMapOf<String, Bitmap>()
    // LruCache не предоставляет прямой итерации, но можно использовать snapshot
    return result
}

/**
 * Extension property для проверки наличия Bitmap в кэше.
 */
fun ResourceManager.hasBitmap(path: String): Boolean {
    return getBitmap(path) != null
}
