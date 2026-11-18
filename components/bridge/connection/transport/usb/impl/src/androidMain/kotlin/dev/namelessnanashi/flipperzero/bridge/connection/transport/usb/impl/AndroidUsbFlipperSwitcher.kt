package dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.impl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.core.content.ContextCompat
import dev.namelessnanashi.flipperzero.bridge.connection.config.api.FDevicePersistedStorage
import dev.namelessnanashi.flipperzero.bridge.connection.config.api.model.FDeviceFlipperZeroUsbModel
import dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.api.FUsbFlipperSwitcherApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean

// STMicroelectronics virtual COM port, used by the Flipper Zero
internal const val FLIPPER_USB_VENDOR_ID = 0x0483
internal const val FLIPPER_USB_PRODUCT_ID = 0x5740
private const val USB_DEVICE_ID = "flipper-usb"

internal fun UsbDevice.isFlipper() =
    vendorId == FLIPPER_USB_VENDOR_ID && productId == FLIPPER_USB_PRODUCT_ID

@SingleIn(AppGraph::class)
@ContributesBinding(AppGraph::class, binding<FUsbFlipperSwitcherApi>())
class AndroidUsbFlipperSwitcher @Inject constructor(
    private val context: Context,
    private val devicePersistedStorage: FDevicePersistedStorage
) : FUsbFlipperSwitcherApi, LogTagProvider {
    override val TAG = "AndroidUsbFlipperSwitcher"

    private val scope = CoroutineScope(SupervisorJob() + FlipperDispatchers.workStealingDispatcher)
    private val mutex = Mutex()
    private val isStarted = AtomicBoolean(false)
    private var previousDeviceId: String? = null
    private val usbManager by lazy { context.getSystemService(UsbManager::class.java) }

    private val detachReceiver = object : BroadcastReceiver() {
        override fun onReceive(receiverContext: Context, intent: Intent) {
            if (intent.action == UsbManager.ACTION_USB_DEVICE_DETACHED) {
                scope.launch { mutex.withLock { onDetachedLocked() } }
            }
        }
    }

    override fun start() {
        if (!isStarted.compareAndSet(false, true)) return
        // USB_DEVICE_ATTACHED only reaches activities (manifest filter -> rescan());
        // DETACHED is a regular broadcast
        ContextCompat.registerReceiver(
            context,
            detachReceiver,
            IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        rescan()
    }

    override fun rescan() {
        scope.launch {
            mutex.withLock {
                runCatching {
                    val flipper = usbManager.deviceList.values.firstOrNull { it.isFlipper() }
                    if (flipper != null) switchToUsbLocked(flipper)
                }.onFailure { error(it) { "Failed to switch to USB" } }
            }
        }
    }

    private suspend fun switchToUsbLocked(device: UsbDevice) {
        val current = devicePersistedStorage.getCurrentDevice().first()
        if (current is FDeviceFlipperZeroUsbModel && current.portPath == device.deviceName) return
        if (current !is FDeviceFlipperZeroUsbModel) {
            previousDeviceId = current?.uniqueId
        }
        info { "Flipper attached at ${device.deviceName}, switching to USB" }
        devicePersistedStorage.removeDevice(USB_DEVICE_ID)
        devicePersistedStorage.addDevice(
            FDeviceFlipperZeroUsbModel(
                name = "USB",
                portPath = device.deviceName,
                humanReadableName = "Flipper (USB)",
                uniqueId = USB_DEVICE_ID
            )
        )
        devicePersistedStorage.setCurrentDevice(USB_DEVICE_ID)
    }

    private suspend fun onDetachedLocked() {
        if (usbManager.deviceList.values.any { it.isFlipper() }) return
        val current = devicePersistedStorage.getCurrentDevice().first()
        if (current !is FDeviceFlipperZeroUsbModel) return
        val fallbackId = previousDeviceId
            ?: devicePersistedStorage.getAllDevices().first()
                .firstOrNull { it !is FDeviceFlipperZeroUsbModel }
                ?.uniqueId
        info { "Flipper unplugged, switching back to $fallbackId" }
        devicePersistedStorage.setCurrentDevice(fallbackId)
        devicePersistedStorage.removeDevice(USB_DEVICE_ID)
    }
}
