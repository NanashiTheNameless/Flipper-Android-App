package dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api

import dev.namelessnanashi.flipperzero.bridge.connection.config.api.model.FDeviceBaseModel
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.FDeviceConnectStatus
import kotlinx.coroutines.flow.StateFlow

interface FDeviceOrchestrator {

    fun getState(): StateFlow<FDeviceConnectStatus>

    suspend fun connect(config: FDeviceBaseModel)

    suspend fun disconnectCurrent()
}
