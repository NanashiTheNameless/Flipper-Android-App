package dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.impl.utils

import dev.namelessnanashi.flipperzero.bridge.connection.config.api.FDeviceType
import dev.namelessnanashi.flipperzero.bridge.connection.device.common.api.FDeviceApi
import dev.namelessnanashi.flipperzero.bridge.connection.device.fzero.api.FZeroDeviceApi
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.FDeviceConnectStatus
import dev.zacsweers.metro.Inject

class FDeviceConnectStatusToDeviceApi @Inject constructor(
    private val fZeroDeviceApiFactory: FZeroDeviceApi.Factory
) {
    fun get(status: FDeviceConnectStatus.Connected): FDeviceApi {
        return when (status.device.type) {
            FDeviceType.FLIPPER_ZERO_USB,
            FDeviceType.FLIPPER_ZERO_BLE -> fZeroDeviceApiFactory(
                scope = status.scope,
                connectedDevice = status.deviceApi
            )
        }
    }
}
