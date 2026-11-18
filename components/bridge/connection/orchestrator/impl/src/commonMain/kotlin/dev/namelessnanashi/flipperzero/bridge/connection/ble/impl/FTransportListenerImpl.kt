package dev.namelessnanashi.flipperzero.bridge.connection.ble.impl

import dev.namelessnanashi.flipperzero.bridge.connection.config.api.model.FDeviceBaseModel
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.ConnectingStatus
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.DisconnectStatus
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.FDeviceConnectStatus
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FInternalTransportConnectionStatus
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FTransportListenerImpl : LogTagProvider {
    override val TAG = "FTransportListener"

    private val state = MutableStateFlow<FDeviceConnectStatus>(
        FDeviceConnectStatus.Disconnected(
            device = null,
            reason = DisconnectStatus.NOT_INITIALIZED
        )
    )

    fun getState() = state.asStateFlow()

    fun onErrorDuringConnect(device: FDeviceBaseModel, throwable: Throwable) {
        @Suppress("UNUSED_EXPRESSION")
        when (throwable) {
            else -> {
                error(throwable) { "Unknown error from transport layer" }
                state.update {
                    FDeviceConnectStatus.Disconnected(
                        device = device,
                        reason = DisconnectStatus.ERROR_UNKNOWN
                    )
                }
            }
        }
    }

    fun onStatusUpdate(device: FDeviceBaseModel, status: FInternalTransportConnectionStatus) {
        state.update { currentStatus ->
            when (status) {
                is FInternalTransportConnectionStatus.Connected -> FDeviceConnectStatus.Connected(
                    device = device,
                    deviceApi = status.deviceApi,
                    scope = status.scope
                )

                FInternalTransportConnectionStatus.Connecting ->
                    FDeviceConnectStatus.Connecting(
                        device = device,
                        status = ConnectingStatus.CONNECTING
                    )

                FInternalTransportConnectionStatus.Disconnected ->
                    if (currentStatus is FDeviceConnectStatus.Disconnected) {
                        currentStatus
                    } else {
                        FDeviceConnectStatus.Disconnected(
                            device = device,
                            reason = DisconnectStatus.REPORTED_BY_TRANSPORT
                        )
                    }

                FInternalTransportConnectionStatus.Disconnecting -> FDeviceConnectStatus.Disconnecting(
                    device
                )

                FInternalTransportConnectionStatus.Pairing ->
                    FDeviceConnectStatus.Connecting(
                        device = device,
                        status = ConnectingStatus.INITIALIZING
                    )
            }
        }
    }
}
