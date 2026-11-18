package dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.api

import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.DeviceConnectionApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FTransportConnectionStatusListener
import kotlinx.coroutines.CoroutineScope

interface USBDeviceConnectionApi : DeviceConnectionApi<FUSBApi, FUSBDeviceConnectionConfig> {
    override suspend fun connect(
        scope: CoroutineScope,
        config: FUSBDeviceConnectionConfig,
        listener: FTransportConnectionStatusListener
    ): Result<FUSBApi>
}
