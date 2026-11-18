package dev.namelessnanashi.flipperzero.bridge.connection.device.fzero.api

import dev.namelessnanashi.flipperzero.bridge.connection.device.common.api.FDeviceApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FConnectedDeviceApi
import kotlinx.coroutines.CoroutineScope

interface FZeroDeviceApi : FDeviceApi {

    fun interface Factory {
        operator fun invoke(
            scope: CoroutineScope,
            connectedDevice: FConnectedDeviceApi
        ): FZeroDeviceApi
    }
}
