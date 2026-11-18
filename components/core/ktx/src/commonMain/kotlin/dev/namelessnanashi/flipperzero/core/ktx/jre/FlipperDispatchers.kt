package dev.namelessnanashi.flipperzero.core.ktx.jre

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

/**
 * To be able to mock dispatchers
 */
object FlipperDispatchers {
    val workStealingDispatcher: CoroutineDispatcher by lazy {
        Executors.newWorkStealingPool().asCoroutineDispatcher()
    }
}
