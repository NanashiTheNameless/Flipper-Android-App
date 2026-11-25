package dev.namelessnanashi.flipperzero.bridge.synchronization.api

import kotlinx.coroutines.flow.StateFlow

interface SynchronizationApi {
    /**
     * @param force if true marks the synchronization dirty
     * and starts the second synchronization after the first synchronization is finished.
     * Requests during the five-second cooldown after completion are combined into one pending sync.
     */
    fun startSynchronization(force: Boolean = false)

    fun getSynchronizationState(): StateFlow<SynchronizationState>

    fun isSynchronizationRunning(): Boolean

    suspend fun stop()
}
