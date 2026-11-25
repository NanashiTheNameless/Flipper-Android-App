package dev.namelessnanashi.flipperzero.firstpair.impl.viewmodels.guide

import dev.namelessnanashi.flipperzero.bridge.connection.service.api.FConnectionService
import dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.api.FUsbFlipperSwitcherApi
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.firstpair.impl.storage.FirstPairStorage
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConnectionGuideViewModel @Inject constructor(
    private val usbFlipperSwitcher: FUsbFlipperSwitcherApi,
    private val firstPairStorage: FirstPairStorage,
    private val connectionService: FConnectionService
) : DecomposeViewModel(), LogTagProvider {
    override val TAG = "ConnectionGuideViewModel"

    private val usbNotFound = MutableStateFlow(false)

    fun isUsbNotFound(): StateFlow<Boolean> = usbNotFound.asStateFlow()

    /**
     * Finishes first setup without Bluetooth pairing when a Flipper is plugged in.
     */
    fun useUsb(onConnected: () -> Unit) {
        if (!usbFlipperSwitcher.isFlipperAttached()) {
            usbNotFound.value = true
            return
        }
        info { "Finish first setup over USB" }
        usbNotFound.value = false
        usbFlipperSwitcher.rescan()
        connectionService.forceReconnect()
        firstPairStorage.markDeviceSelected(deviceId = null, deviceName = null, address = null)
        onConnected()
    }
}
