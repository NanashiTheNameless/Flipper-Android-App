package dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.impl

import android.content.Context
import android.hardware.usb.UsbManager
import dev.namelessnanashi.flipperzero.bridge.connection.feature.actionnotifier.api.FlipperActionNotifier
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FInternalTransportConnectionStatus
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FTransportConnectionStatusListener
import dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.api.FUSBApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.api.FUSBDeviceConnectionConfig
import dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.api.USBDeviceConnectionApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.impl.serial.AndroidCdcAcmPort
import dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.impl.serial.FAndroidUSBSerialDeviceApi
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

private val CLI_PROMPT = ">: ".toByteArray()
private val START_RPC_COMMAND = "start_rpc_session\r".toByteArray()
private const val PROMPT_TIMEOUT_MS = 3000L
private const val ECHO_TIMEOUT_MS = 2000L
private const val HANDSHAKE_READ_TIMEOUT_MS = 50
private const val HANDSHAKE_BUFFER_SIZE = 16 * 1024

/**
 * USB connection to a Flipper over the Android USB host API. [FUSBDeviceConnectionConfig.path]
 * is the [android.hardware.usb.UsbDevice.getDeviceName] of the attached Flipper.
 */
class AndroidUSBDeviceConnectionApiImpl(
    private val context: Context,
    private val actionNotifierFactory: FlipperActionNotifier.Factory
) : USBDeviceConnectionApi, LogTagProvider {
    override val TAG = "AndroidUSBDeviceConnectionApi"

    override suspend fun connect(
        scope: CoroutineScope,
        config: FUSBDeviceConnectionConfig,
        listener: FTransportConnectionStatusListener
    ): Result<FUSBApi> = runCatching {
        listener.onStatusUpdate(FInternalTransportConnectionStatus.Connecting)
        val usbManager = context.getSystemService(UsbManager::class.java)
        val device = usbManager.deviceList[config.path]
            ?: throw IOException("Flipper is not attached at ${config.path}")
        if (!usbManager.requestPermissionIfNeeded(context, device)) {
            throw IOException("USB permission denied for ${config.path}")
        }

        val port = withContext(Dispatchers.IO) { AndroidCdcAcmPort.open(usbManager, device) }
        info { "Opened ${device.deviceName}, waiting for the CLI prompt" }
        val initialRpcBytes = try {
            withContext(Dispatchers.IO) {
                // Wait for the CLI prompt so the command is not sent before the shell is ready;
                // continue anyway if it never shows (the prompt text differs between firmwares)
                if (port.skipUntil(CLI_PROMPT, PROMPT_TIMEOUT_MS) == null) {
                    info { "No CLI prompt seen, sending start_rpc_session anyway" }
                }
                port.write(START_RPC_COMMAND)
                // Skip the echoed command line; everything after it is RPC
                port.skipUntil("\n".toByteArray(), ECHO_TIMEOUT_MS)
                    ?: byteArrayOf().also { info { "No echo of start_rpc_session seen" } }
            }
        } catch (e: Exception) {
            port.close()
            throw e
        }
        info { "Flipper on ${config.path} is in RPC mode" }

        val deviceApi = FAndroidUSBSerialDeviceApi(
            scope = scope,
            port = port,
            actionNotifier = actionNotifierFactory(scope),
            initialBytes = initialRpcBytes
        )
        scope.launch {
            try {
                awaitCancellation()
            } finally {
                withContext(NonCancellable) { port.close() }
            }
        }
        listener.onStatusUpdate(FInternalTransportConnectionStatus.Connected(scope, deviceApi))
        deviceApi
    }

    /**
     * Reads until [marker] has been seen. USB bulk reads must take whole packets (a read smaller
     * than the endpoint's packet size overflows and loses the data), so this reads full buffers
     * and scans them.
     * @return bytes received after the marker, or null if it did not appear in time
     */
    private fun AndroidCdcAcmPort.skipUntil(marker: ByteArray, timeoutMs: Long): ByteArray? {
        val deadline = System.currentTimeMillis() + timeoutMs
        val buffer = ByteArray(HANDSHAKE_BUFFER_SIZE)
        var matched = 0
        while (System.currentTimeMillis() < deadline) {
            val read = read(buffer, HANDSHAKE_READ_TIMEOUT_MS)
            for (index in 0 until read) {
                val byte = buffer[index]
                matched = when (byte) {
                    marker[matched] -> matched + 1
                    marker[0] -> 1
                    else -> 0
                }
                if (matched == marker.size) {
                    return buffer.copyOfRange(index + 1, read)
                }
            }
        }
        return null
    }

}
