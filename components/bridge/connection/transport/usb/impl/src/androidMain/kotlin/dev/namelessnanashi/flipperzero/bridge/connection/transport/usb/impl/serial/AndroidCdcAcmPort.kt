package dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.impl.serial

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import java.io.Closeable
import java.io.IOException

private const val USB_CLASS_CDC_CONTROL = 0x02
private const val REQUEST_TYPE_CLASS_INTERFACE_OUT = 0x21
private const val SET_LINE_CODING = 0x20
private const val SET_CONTROL_LINE_STATE = 0x22
private const val CONTROL_LINE_DTR_RTS = 0x03
private const val CONTROL_TIMEOUT_MS = 1000
private const val WRITE_TIMEOUT_MS = 2000
private const val BAUD_RATE = 230400
private const val DATA_BITS = 8.toByte()

/**
 * Minimal CDC-ACM serial port over the Android USB host API (the Flipper's USB VCP).
 * Not thread safe for concurrent writes; reads and writes may run on different threads.
 */
class AndroidCdcAcmPort private constructor(
    private val connection: UsbDeviceConnection,
    private val controlInterface: UsbInterface?,
    private val dataInterface: UsbInterface,
    private val inEndpoint: UsbEndpoint,
    private val outEndpoint: UsbEndpoint
) : Closeable {

    /**
     * @return bytes read into [buffer]; 0 on timeout or error (Android reports both as -1, so a
     * removed device is detected through the detach broadcast instead)
     */
    fun read(buffer: ByteArray, timeoutMs: Int): Int {
        val read = connection.bulkTransfer(inEndpoint, buffer, buffer.size, timeoutMs)
        return if (read < 0) 0 else read
    }

    fun write(data: ByteArray) {
        var offset = 0
        while (offset < data.size) {
            val written = connection.bulkTransfer(
                outEndpoint,
                data,
                offset,
                data.size - offset,
                WRITE_TIMEOUT_MS
            )
            if (written <= 0) {
                throw IOException("USB write failed after $offset of ${data.size} bytes")
            }
            offset += written
        }
    }

    override fun close() {
        runCatching { setControlLines(0) }
        runCatching { connection.releaseInterface(dataInterface) }
        controlInterface?.let { runCatching { connection.releaseInterface(it) } }
        connection.close()
    }

    private fun setControlLines(value: Int) {
        val index = controlInterface?.id ?: dataInterface.id
        connection.controlTransfer(
            REQUEST_TYPE_CLASS_INTERFACE_OUT,
            SET_CONTROL_LINE_STATE,
            value,
            index,
            null,
            0,
            CONTROL_TIMEOUT_MS
        )
    }

    private fun configure() {
        val index = controlInterface?.id ?: dataInterface.id
        val lineCoding = byteArrayOf(
            (BAUD_RATE and 0xFF).toByte(),
            (BAUD_RATE shr 8 and 0xFF).toByte(),
            (BAUD_RATE shr 16 and 0xFF).toByte(),
            (BAUD_RATE shr 24 and 0xFF).toByte(),
            0, // 1 stop bit
            0, // no parity
            DATA_BITS
        )
        connection.controlTransfer(
            REQUEST_TYPE_CLASS_INTERFACE_OUT,
            SET_LINE_CODING,
            0,
            index,
            lineCoding,
            lineCoding.size,
            CONTROL_TIMEOUT_MS
        )
        // The Flipper starts its CLI session when DTR goes high
        setControlLines(CONTROL_LINE_DTR_RTS)
    }

    companion object {
        fun open(usbManager: UsbManager, device: UsbDevice): AndroidCdcAcmPort {
            val interfaces = (0 until device.interfaceCount).map { device.getInterface(it) }
            val dataInterface = interfaces.firstOrNull { usbInterface ->
                usbInterface.interfaceClass == UsbConstants.USB_CLASS_CDC_DATA &&
                    usbInterface.bulkEndpoint(UsbConstants.USB_DIR_IN) != null &&
                    usbInterface.bulkEndpoint(UsbConstants.USB_DIR_OUT) != null
            } ?: throw IOException("No CDC data interface on ${device.deviceName}")
            val controlInterface = interfaces.firstOrNull {
                it.interfaceClass == USB_CLASS_CDC_CONTROL
            }

            val connection = usbManager.openDevice(device)
                ?: throw IOException("Could not open ${device.deviceName} (no permission?)")
            try {
                if (controlInterface != null && !connection.claimInterface(controlInterface, true)) {
                    throw IOException("Could not claim CDC control interface")
                }
                if (!connection.claimInterface(dataInterface, true)) {
                    throw IOException("Could not claim CDC data interface")
                }
                return AndroidCdcAcmPort(
                    connection = connection,
                    controlInterface = controlInterface,
                    dataInterface = dataInterface,
                    inEndpoint = requireNotNull(dataInterface.bulkEndpoint(UsbConstants.USB_DIR_IN)),
                    outEndpoint = requireNotNull(dataInterface.bulkEndpoint(UsbConstants.USB_DIR_OUT))
                ).also { it.configure() }
            } catch (e: Exception) {
                connection.close()
                throw e
            }
        }

        private fun UsbInterface.bulkEndpoint(direction: Int): UsbEndpoint? =
            (0 until endpointCount).map { getEndpoint(it) }.firstOrNull {
                it.type == UsbConstants.USB_ENDPOINT_XFER_BULK && it.direction == direction
            }
    }
}
