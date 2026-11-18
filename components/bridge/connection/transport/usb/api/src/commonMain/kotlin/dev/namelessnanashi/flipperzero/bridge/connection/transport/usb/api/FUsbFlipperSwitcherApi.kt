package dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.api

/**
 * Uses a USB-attached Flipper as the current device while it is plugged in, and goes back to
 * the previous (Bluetooth) device when it is unplugged.
 */
interface FUsbFlipperSwitcherApi {
    fun start()

    /**
     * Looks for an attached Flipper again, e.g. after the system delivered USB_DEVICE_ATTACHED
     */
    fun rescan()
}
