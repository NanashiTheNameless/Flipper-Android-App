package dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.impl.serial

import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

private const val DELAY_MS = 1000L

/**
 * Bytes per second over the last second.
 *
 * Fork: ticks only while bytes flow (upstream woke up every second for the whole connection,
 * twice per link). After a second without bytes the speed drops to 0 and the ticker stops
 * until the next bytes arrive.
 */
class SpeedMeter(private val scope: CoroutineScope) {
    private val bytesPerSecond = MutableStateFlow(0L)
    private val bytesCollected = AtomicLong(0)
    private val isTicking = AtomicBoolean(false)

    fun getSpeed(): StateFlow<Long> = bytesPerSecond

    fun onReceiveBytes(bytesCount: Int) {
        bytesCollected.addAndGet(bytesCount.toLong())
        if (isTicking.compareAndSet(false, true)) {
            scope.launch(FlipperDispatchers.workStealingDispatcher) { tickWhileBytesFlow() }
        }
    }

    private suspend fun tickWhileBytesFlow() {
        try {
            while (true) {
                delay(DELAY_MS)
                val totalBytes = bytesCollected.getAndSet(0)
                bytesPerSecond.value = totalBytes
                if (totalBytes == 0L) break
            }
        } finally {
            isTicking.set(false)
        }
        // Bytes that arrived between the last read and the flag reset start a new ticker
        if (bytesCollected.get() > 0 && isTicking.compareAndSet(false, true)) {
            scope.launch(FlipperDispatchers.workStealingDispatcher) { tickWhileBytesFlow() }
        }
    }
}
