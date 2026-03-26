package com.endlessrunner.entities.interfaces

/**
 * Интерфейс для объектов, которые могут обновляться.
 * 
 * Реализуется сущностями, которые изменяют своё состояние
 * во времени (движутся, анимируются, изменяют свойства).
 */
interface IUpdatable {
    /**
     * Обновление состояния объекта.
     * 
     * @param deltaTime Время, прошедшее с последнего обновления (в секундах)
     */
    fun update(deltaTime: Float)

    /**
     * Фиксированное обновление состояния.
     * Вызывается с фиксированным шагом времени для физической логики.
     * 
     * @param fixedDeltaTime Фиксированный шаг времени (в секундах)
     */
    fun fixedUpdate(fixedDeltaTime: Float) {}

    /**
     * Позднее обновление.
     * Вызывается после всех обновлений, для синхронизации состояний.
     * 
     * @param deltaTime Время, прошедшее с последнего обновления (в секундах)
     */
    fun lateUpdate(deltaTime: Float) {}
}

/**
 * Расширения для удобной работы с IUpdatable.
 */

/**
 * Обновление списка обновляемых объектов.
 */
fun List<IUpdatable>.updateAll(deltaTime: Float) {
    forEach { it.update(deltaTime) }
}

/**
 * Фиксированное обновление списка объектов.
 */
fun List<IUpdatable>.fixedUpdateAll(fixedDeltaTime: Float) {
    forEach { it.fixedUpdate(fixedDeltaTime) }
}

/**
 * Позднее обновление списка объектов.
 */
fun List<IUpdatable>.lateUpdateAll(deltaTime: Float) {
    forEach { it.lateUpdate(deltaTime) }
}
