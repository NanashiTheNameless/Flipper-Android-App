package dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.api

import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.DeviceConnectionApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FTransportConnectionStatusListener
import kotlinx.coroutines.CoroutineScope

interface BleDeviceConnectionApi : DeviceConnectionApi<FBleApi, FBleDeviceConnectionConfig> {
    override suspend fun connect(
        scope: CoroutineScope,
        config: FBleDeviceConnectionConfig,
        listener: FTransportConnectionStatusListener
    ): Result<FBleApi>
}
