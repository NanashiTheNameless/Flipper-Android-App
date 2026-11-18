package dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.impl.serial

import dev.namelessnanashi.flipperzero.bridge.connection.feature.actionnotifier.api.FlipperActionNotifier
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.meta.FTransportMetaInfoApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.serial.FSerialDeviceApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.serial.FSerialRestartApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.serial.FlipperSerialSpeed
import dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.api.FUSBApi
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference

private const val READ_BUFFER_SIZE = 16 * 1024
private const val READ_TIMEOUT_MS = 100

class FAndroidUSBSerialDeviceApi(
    scope: CoroutineScope,
    private val port: AndroidCdcAcmPort,
    private val actionNotifier: FlipperActionNotifier,
    // RPC bytes that arrived in the same USB packet as the end of the handshake
    initialBytes: ByteArray = byteArrayOf()
) : FUSBApi,
    FSerialDeviceApi,
    FSerialRestartApi by NoopRestartApi(),
    FTransportMetaInfoApi by FakeTransportMetaInfoApi(),
    LogTagProvider {
    override val TAG = "FAndroidUSBSerialDeviceApi"

    private val receiverByteFlow = MutableSharedFlow<ByteArray>()
    private val pendingInitialBytes = AtomicReference(initialBytes)
    private val txSpeed = SpeedMeter(scope)
    private val rxSpeed = SpeedMeter(scope)
    private val speedFlowState = MutableStateFlow(FlipperSerialSpeed())
    private val writeMutex = Mutex()

    init {
        scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(READ_BUFFER_SIZE)
            while (isActive) {
                val read = port.read(buffer, READ_TIMEOUT_MS)
                if (read > 0) {
                    receiverByteFlow.emit(buffer.copyOf(read))
                    rxSpeed.onReceiveBytes(read)
                }
            }
        }
        combine(
            rxSpeed.getSpeed(),
            txSpeed.getSpeed()
        ) { rxBPS, txBPS ->
            actionNotifier.notifyAboutAction()
            speedFlowState.emit(
                FlipperSerialSpeed(receiveBytesInSec = rxBPS, transmitBytesInSec = txBPS)
            )
        }.launchIn(scope)
    }

    override suspend fun getSpeed() = speedFlowState.asStateFlow()

    override suspend fun getReceiveBytesFlow(): Flow<ByteArray> = flow {
        val initial = pendingInitialBytes.getAndSet(null)
        if (initial != null && initial.isNotEmpty()) emit(initial)
        emitAll(receiverByteFlow)
    }

    override fun getActionNotifier() = actionNotifier

    override suspend fun sendBytes(data: ByteArray) = withContext(Dispatchers.IO) {
        writeMutex.withLock { port.write(data) }
        txSpeed.onReceiveBytes(data.size)
    }

    override suspend fun disconnect() {
        info { "Close USB port" }
        port.close()
    }
}
