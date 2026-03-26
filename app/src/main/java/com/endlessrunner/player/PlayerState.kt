package com.endlessrunner.player

/**
 * Sealed class для состояний игрока.
 * Каждое состояние может иметь свои параметры.
 */
sealed class PlayerState {
    
    /**
     * Время в текущем состоянии.
     */
    var stateTime: Float = 0f
        internal set
    
    /**
     * Вызывается при входе в состояние.
     */
    open fun onEnter(player: Player) {}
    
    /**
     * Вызывается при выходе из состояния.
     */
    open fun onExit(player: Player) {}
    
    /**
     * Обновление состояния.
     */
    open fun onUpdate(player: Player, deltaTime: Float) {
        stateTime += deltaTime
    }
    
    /**
     * Проверка, можно ли перейти в другое состояние.
     */
    open fun canTransitionTo(newState: PlayerState): Boolean = true
    
    /**
     * Сброс состояния.
     */
    open fun reset() {
        stateTime = 0f
    }
    
    // ============================================================================
    // СОСТОЯНИЯ
    // ============================================================================
    
    /**
     * Игрок на земле, не движется.
     */
    object Idle : PlayerState() {
        override fun onEnter(player: Player) {
            player.movementComponent?.stop()
        }
        
        override fun canTransitionTo(newState: PlayerState): Boolean {
            return when (newState) {
                is Running, is Jumping, is Falling, is Dead, is Invincible -> true
                else -> false
            }
        }
    }
    
    /**
     * Игрок бежит.
     */
    object Running : PlayerState() {
        override fun canTransitionTo(newState: PlayerState): Boolean {
            return when (newState) {
                is Idle, is Jumping, is Falling, is Dead, is Invincible -> true
                else -> false
            }
        }
    }
    
    /**
     * Игрок в прыжке (движется вверх).
     */
    object Jumping : PlayerState() {
        override fun onEnter(player: Player) {
            player.movementComponent?.jump()
        }
        
        override fun onUpdate(player: Player, deltaTime: Float) {
            super.onUpdate(player, deltaTime)
            
            // Переход в падение если скорость стала положительной
            val vy = player.positionComponent?.vy ?: 0f
            if (vy >= 0) {
                player.changeState(Falling)
            }
        }
        
        override fun canTransitionTo(newState: PlayerState): Boolean {
            return when (newState) {
                is Falling, is Dead, is Invincible -> true
                else -> false
            }
        }
    }
    
    /**
     * Игрок падает.
     */
    object Falling : PlayerState() {
        override fun canTransitionTo(newState: PlayerState): Boolean {
            return when (newState) {
                is Idle, is Running, is Jumping, is Dead, is Invincible -> true
                else -> false
            }
        }
    }
    
    /**
     * Игрок мёртв.
     */
    object Dead : PlayerState() {
        override fun onEnter(player: Player) {
            player.movementComponent?.stop()
        }
        
        override fun canTransitionTo(newState: PlayerState): Boolean {
            // Из состояния смерти нельзя перейти в другое состояние
            return false
        }
    }
    
    /**
     * Игрок неуязвим (после получения урона).
     */
    data class Invincible(
        val duration: Float = 1.5f
    ) : PlayerState() {
        
        override fun onEnter(player: Player) {
            player.isInvincible = true
        }
        
        override fun onExit(player: Player) {
            player.isInvincible = false
        }
        
        override fun onUpdate(player: Player, deltaTime: Float) {
            super.onUpdate(player, deltaTime)
            
            // Проверка окончания неуязвимости
            if (stateTime >= duration) {
                // Возврат в предыдущее состояние
                val previousState = player.previousState ?: Idle
                player.changeState(previousState)
            }
        }
        
        override fun canTransitionTo(newState: PlayerState): Boolean {
            // Можно перейти только в обычное состояние после окончания таймера
            return newState is Idle || newState is Running || newState is Falling
        }
    }
    
    /**
     * Игрок атакует (для будущих расширений).
     */
    data class Attacking(
        val attackDuration: Float = 0.3f
    ) : PlayerState() {
        
        override fun onEnter(player: Player) {
            // TODO: Запуск анимации атаки
        }
        
        override fun onUpdate(player: Player, deltaTime: Float) {
            super.onUpdate(player, deltaTime)
            
            if (stateTime >= attackDuration) {
                // Возврат в предыдущее состояние
                val previousState = player.previousState ?: Idle
                player.changeState(previousState)
            }
        }
        
        override fun canTransitionTo(newState: PlayerState): Boolean {
            return when (newState) {
                is Idle, is Running, is Jumping, is Falling, is Dead -> true
                else -> false
            }
        }
    }
    
    /**
     * Игрок скользит (для будущих расширений).
     */
    object Sliding : PlayerState() {
        override fun canTransitionTo(newState: PlayerState): Boolean {
            return when (newState) {
                is Idle, is Running, is Dead -> true
                else -> false
            }
        }
    }
    
    /**
     * Игрок на платформе (движется вместе с ней).
     */
    data class OnPlatform(
        val platformVelocity: Pair<Float, Float> = Pair(0f, 0f)
    ) : PlayerState() {
        override fun onUpdate(player: Player, deltaTime: Float) {
            super.onUpdate(player, deltaTime)
            
            // Применение скорости платформы
            player.positionComponent?.apply {
                x += platformVelocity.first * deltaTime
                y += platformVelocity.second * deltaTime
            }
        }
        
        override fun canTransitionTo(newState: PlayerState): Boolean {
            return when (newState) {
                is Idle, is Running, is Jumping, is Falling, is Dead -> true
                else -> false
            }
        }
    }
}

/**
 * Extension property для проверки состояния.
 */
val PlayerState.isGrounded: Boolean
    get() = this is PlayerState.Idle || this is PlayerState.Running

/**
 * Extension property для проверки airborne состояния.
 */
val PlayerState.isAirborne: Boolean
    get() = this is PlayerState.Jumping || this is PlayerState.Falling

/**
 * Extension property для проверки мёртвого состояния.
 */
val PlayerState.isDead: Boolean
    get() = this is PlayerState.Dead

/**
 * Extension property для проверки неуязвимости.
 */
val PlayerState.isInvincible: Boolean
    get() = this is PlayerState.Invincible

/**
 * Extension property для проверки движения.
 */
val PlayerState.isMoving: Boolean
    get() = this is PlayerState.Running
