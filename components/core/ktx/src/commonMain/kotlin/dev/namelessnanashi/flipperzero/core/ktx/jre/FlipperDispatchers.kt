package dev.namelessnanashi.flipperzero.core.ktx.jre

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

/**
 * To be able to mock dispatchers
 */
object FlipperDispatchers {
    val workStealingDispatcher: CoroutineDispatcher by lazy {
        Executors.newWorkStealingPool().asCoroutineDispatcher()
    }

    /**
     * Fork: for loops that block a thread (the RPC send/receive/parse loops). The work-stealing
     * pool has one thread per core, and app work blocking all of them stalled the connection:
     * received bytes were never parsed and USB reads stopped.
     */
    val blockingDispatcher: CoroutineDispatcher
        get() = Dispatchers.IO
}
