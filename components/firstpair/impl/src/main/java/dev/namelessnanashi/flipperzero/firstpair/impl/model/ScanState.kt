package dev.namelessnanashi.flipperzero.firstpair.impl.model

import dev.namelessnanashi.flipperzero.bridge.api.scanner.DiscoveredBluetoothDevice

sealed class ScanState {
    open class Stopped : ScanState()

    object Searching : ScanState()

    class Founded(val devices: List<DiscoveredBluetoothDevice>) : ScanState()

    object Timeout : Stopped()
}
