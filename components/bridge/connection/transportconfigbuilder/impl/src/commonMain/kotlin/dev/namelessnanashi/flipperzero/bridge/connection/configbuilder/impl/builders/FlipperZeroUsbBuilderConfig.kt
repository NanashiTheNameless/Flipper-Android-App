package dev.namelessnanashi.flipperzero.bridge.connection.configbuilder.impl.builders

import dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.api.FUSBDeviceConnectionConfig
import dev.zacsweers.metro.Inject

class FlipperZeroUsbBuilderConfig @Inject constructor() {
    fun build(
        portDescription: String
    ) = FUSBDeviceConnectionConfig(portDescription)
}
