package dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.impl.serial

import dev.namelessnanashi.flipperzero.bridge.connection.feature.actionnotifier.api.FlipperActionNotifier
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.serial.FSerialDeviceApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.serial.FSerialRestartApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.serial.FlipperSerialSpeed
import dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.api.FUSBApi
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import java.io.ByteArrayOutputStream
import java.io.IOException
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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.atomic.AtomicReference

private const val READ_BUFFER_SIZE = 16 * 1024
private const val NANOS_PER_MS = 1_000_000L

// One-packet (64 byte) reads kept queued on the IN endpoint at once
private const val PARALLEL_READS = 32

// Fork: RPC messages are small (a storage write carries at most 512 bytes); sending each in
// its own USB transfer wasted most of the link. Queued messages are joined into one transfer
// of up to this size.
private const val MAX_WRITE_BATCH_BYTES = 16 * 1024
private const val WRITE_QUEUE_CAPACITY = 64

class FAndroidUSBSerialDeviceApi(
    scope: CoroutineScope,
    private val port: AndroidCdcAcmPort,
    private val actionNotifier: FlipperActionNotifier,
    // RPC bytes that arrived in the same USB packet as the end of the handshake
    initialBytes: ByteArray = byteArrayOf(),
    // Called when the read loop ends while the connection is still active (cable pulled)
    onLinkLost: () -> Unit = {}
) : FUSBApi,
    FSerialDeviceApi,
    FSerialRestartApi by NoopRestartApi(),
    LogTagProvider {
    override val TAG = "FAndroidUSBSerialDeviceApi"

    private val receiverByteFlow = MutableSharedFlow<ByteArray>()
    private val pendingInitialBytes = AtomicReference(initialBytes)
    private val txSpeed = SpeedMeter(scope)
    private val rxSpeed = SpeedMeter(scope)
    private val speedFlowState = MutableStateFlow(FlipperSerialSpeed())
    private val writeQueue = Channel<ByteArray>(capacity = WRITE_QUEUE_CAPACITY)

    init {
        scope.launch(Dispatchers.IO) {
            runCatching {
                port.readLoop(
                    parallelReads = PARALLEL_READS,
                    flushBytes = READ_BUFFER_SIZE,
                    isActive = { isActive }
                ) { bytes ->
                    info { "USB rx ${bytes.size} B" }
                    // The loop runs on its own thread; hand bytes over in order
                    runBlocking { receiverByteFlow.emit(bytes) }
                    rxSpeed.onReceiveBytes(bytes.size)
                }
            }.onFailure { error(it) { "USB read loop stopped" } }
            // The loop only ends on its own when the port is gone
            if (isActive) onLinkLost()
        }
        scope.launch(Dispatchers.IO) { writeLoop() }
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

    override suspend fun sendBytes(data: ByteArray) {
        writeQueue.send(data)
    }

    private suspend fun writeLoop() {
        val batch = ByteArrayOutputStream(MAX_WRITE_BATCH_BYTES)
        for (first in writeQueue) {
            batch.reset()
            batch.write(first)
            while (batch.size() < MAX_WRITE_BATCH_BYTES) {
                val next = writeQueue.tryReceive().getOrNull() ?: break
                batch.write(next)
            }
            val bytes = batch.toByteArray()
            try {
                val started = System.nanoTime()
                port.write(bytes)
                txSpeed.onReceiveBytes(bytes.size)
                info { "USB tx ${bytes.size} B in ${(System.nanoTime() - started) / NANOS_PER_MS} ms" }
            } catch (e: IOException) {
                // Unplugging is handled by the detach broadcast; keep draining meanwhile
                error(e) { "USB write of ${bytes.size} bytes failed" }
            }
        }
    }

    override suspend fun disconnect() {
        info { "Close USB port" }
        writeQueue.close()
        port.close()
    }
}
