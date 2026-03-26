package com.endlessrunner.entities.interfaces

import android.graphics.Canvas

/**
 * Интерфейс для объектов, которые могут отрисовываться.
 * 
 * Реализуется сущностями, которые имеют визуальное представление
 * и должны рендериться на экране.
 */
interface IRenderable {
    /**
     * Отрисовка объекта.
     * 
     * @param canvas Canvas для отрисовки
     * @param cameraPosition Позиция камеры для расчёта видимости
     */
    fun render(canvas: Canvas, cameraPosition: Vector2)

    /**
     * Проверка видимости объекта в камере.
     * 
     * @param cameraPosition Позиция камеры
     * @param viewportSize Размер видимой области
     * @return true если объект виден в камере
     */
    fun isVisible(cameraPosition: Vector2, viewportSize: Vector2): Boolean

    /**
     * Приоритет отрисовки.
     * Объекты с большим priority рисуются поверх объектов с меньшим.
     */
    val renderPriority: Int
        get() = 0

    /**
     * Альфа-канал (прозрачность) от 0.0 до 1.0.
     */
    var alpha: Float
        get() = 1.0f
        set(_) {}

    /**
     * Проверка, должен ли объект отрисовываться.
     */
    val shouldRender: Boolean
        get() = alpha > 0f
}

/**
 * Расширения для удобной работы с IRenderable.
 */

/**
 * Отрисовка списка рендерящихся объектов.
 * Сортирует объекты по приоритету перед отрисовкой.
 */
fun List<IRenderable>.renderAll(canvas: Canvas, cameraPosition: Vector2) {
    sortedBy { it.renderPriority }
        .filter { it.shouldRender }
        .forEach { it.render(canvas, cameraPosition) }
}

/**
 * Фильтрация видимых объектов в камере.
 */
fun List<IRenderable>.getVisibleObjects(
    cameraPosition: Vector2,
    viewportSize: Vector2
): List<IRenderable> = filter { it.isVisible(cameraPosition, viewportSize) }

/**
 * Базовая реализация IRenderable для простых случаев.
 */
abstract class BaseRenderable : IRenderable {
    override var alpha: Float = 1.0f
    override val renderPriority: Int = 0
    override val shouldRender: Boolean get() = alpha > 0f

    override fun isVisible(cameraPosition: Vector2, viewportSize: Vector2): Boolean {
        // По умолчанию считаем все объекты видимыми
        // Переопределите в наследниках для оптимизации
        return true
    }
}
