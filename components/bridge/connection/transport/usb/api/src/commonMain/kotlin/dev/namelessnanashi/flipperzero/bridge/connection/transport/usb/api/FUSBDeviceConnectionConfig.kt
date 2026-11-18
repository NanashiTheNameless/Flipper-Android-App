package dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.api

import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FDeviceConnectionConfig

data class FUSBDeviceConnectionConfig(
    val path: String,
) : FDeviceConnectionConfig<FUSBApi>()
