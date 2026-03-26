package com.endlessrunner.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Аудио микшер для управления каналами.
 *
 * Управляет громкостью, панорамированием и высотой тона для каждого канала.
 * Поддерживает независимое заглушение каналов.
 */
class AudioMixer private constructor() {

    /**
     * Состояния каналов.
     */
    private val channelStates = mutableMapOf<AudioChannel, ChannelState>()

    /**
     * StateFlow для изменений громкости каналов.
     */
    private val channelVolumeFlows = mutableMapOf<AudioChannel, MutableStateFlow<Float>>()

    init {
        // Инициализация состояний каналов
        AudioChannel.ALL.forEach { channel ->
            channelStates[channel] = ChannelState(volume = channel.defaultVolume)
            channelVolumeFlows[channel] = MutableStateFlow(channel.defaultVolume)
        }
    }

    /**
     * Установить громкость канала.
     *
     * @param channel Канал
     * @param volume Громкость от 0.0 до 1.0
     */
    fun setChannelVolume(channel: AudioChannel, volume: Float) {
        val clampedVolume = volume.coerceIn(AudioConstants.MIN_VOLUME, AudioConstants.MAX_VOLUME)
        channelStates[channel] = channelStates[channel]?.copy(volume = clampedVolume)
        channelVolumeFlows[channel]?.value = clampedVolume
    }

    /**
     * Установить панорамирование канала.
     *
     * @param channel Канал
     * @param pan Панорамирование от -1.0 (лево) до 1.0 (право)
     */
    fun setChannelPan(channel: AudioChannel, pan: Float) {
        val clampedPan = pan.coerceIn(-1.0f, 1.0f)
        channelStates[channel] = channelStates[channel]?.copy(pan = clampedPan)
    }

    /**
     * Установить высоту тона канала.
     *
     * @param channel Канал
     * @param pitch Высота тона (0.5 = октава вниз, 1.0 = оригинал, 2.0 = октава вверх)
     */
    fun setChannelPitch(channel: AudioChannel, pitch: Float) {
        val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
        channelStates[channel] = channelStates[channel]?.copy(pitch = clampedPitch)
    }

    /**
     * Заглушить канал.
     *
     * @param channel Канал
     */
    fun muteChannel(channel: AudioChannel) {
        channelStates[channel] = channelStates[channel]?.copy(isMuted = true)
    }

    /**
     * Включить канал после mute.
     *
     * @param channel Канал
     */
    fun unmuteChannel(channel: AudioChannel) {
        channelStates[channel] = channelStates[channel]?.copy(isMuted = false)
    }

    /**
     * Переключить mute канала.
     *
     * @param channel Канал
     */
    fun toggleChannelMute(channel: AudioChannel) {
        val currentState = channelStates[channel] ?: return
        channelStates[channel] = currentState.copy(isMuted = !currentState.isMuted)
    }

    /**
     * Получить громкость канала.
     *
     * @param channel Канал
     * @return Громкость от 0.0 до 1.0
     */
    fun getChannelVolume(channel: AudioChannel): Float {
        return channelStates[channel]?.volume ?: channel.defaultVolume
    }

    /**
     * Получить панорамирование канала.
     *
     * @param channel Канал
     * @return Панорамирование от -1.0 до 1.0
     */
    fun getChannelPan(channel: AudioChannel): Float {
        return channelStates[channel]?.pan ?: 0.0f
    }

    /**
     * Получить высоту тона канала.
     *
     * @param channel Канал
     * @return Высота тона
     */
    fun getChannelPitch(channel: AudioChannel): Float {
        return channelStates[channel]?.pitch ?: 1.0f
    }

    /**
     * Проверка, заглушен ли канал.
     *
     * @param channel Канал
     * @return true если канал заглушен
     */
    fun isChannelMuted(channel: AudioChannel): Boolean {
        return channelStates[channel]?.isMuted ?: false
    }

    /**
     * Получить эффективную громкость канала (с учётом mute).
     *
     * @param channel Канал
     * @return Эффективная громкость
     */
    fun getEffectiveVolume(channel: AudioChannel): Float {
        return channelStates[channel]?.effectiveVolume ?: channel.defaultVolume
    }

    /**
     * Получить StateFlow громкости канала.
     *
     * @param channel Канал
     * @return StateFlow<Float>
     */
    fun getChannelVolumeFlow(channel: AudioChannel): StateFlow<Float> {
        return channelVolumeFlows[channel]?.asStateFlow()
            ?: MutableStateFlow(channel.defaultVolume)
    }

    /**
     * Получить состояние канала.
     *
     * @param channel Канал
     * @return ChannelState
     */
    fun getChannelState(channel: AudioChannel): ChannelState {
        return channelStates[channel] ?: ChannelState.default()
    }

    /**
     * Сбросить все каналы к значениям по умолчанию.
     */
    fun resetAllChannels() {
        AudioChannel.ALL.forEach { channel ->
            channelStates[channel] = ChannelState(volume = channel.defaultVolume)
            channelVolumeFlows[channel]?.value = channel.defaultVolume
        }
    }

    /**
     * Заглушить все каналы кроме указанного.
     *
     * @param except Канал который не нужно заглушать
     */
    fun muteAllExcept(except: AudioChannel) {
        AudioChannel.ALL.forEach { channel ->
            if (channel != except) {
                muteChannel(channel)
            }
        }
    }

    /**
     * Включить все каналы.
     */
    fun unmuteAll() {
        AudioChannel.ALL.forEach { channel ->
            unmuteChannel(channel)
        }
    }

    /**
     * Получить микс громкости для канала (с учётом master).
     *
     * @param channel Канал
     * @return Итоговая громкость
     */
    fun getMixedVolume(channel: AudioChannel): Float {
        val masterVolume = getEffectiveVolume(AudioChannel.MASTER)
        val channelVolume = getEffectiveVolume(channel)
        return masterVolume * channelVolume
    }

    companion object {
        @Volatile
        private var instance: AudioMixer? = null

        /**
         * Получить singleton экземпляр.
         */
        fun getInstance(): AudioMixer {
            return instance ?: synchronized(this) {
                instance ?: AudioMixer().also { instance = it }
            }
        }

        /**
         * Очистить singleton (для тестов).
         */
        fun clearInstance() {
            instance = null
        }
    }
}
