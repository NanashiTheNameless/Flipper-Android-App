package dev.namelessnanashi.flipperzero.bridge.connection.feature.seriallagsdetector.impl

import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.FlipperRequest
import dev.namelessnanashi.flipperzero.core.buildkonfig.BuildKonfig
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.verbose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.updateAndGet
import java.util.concurrent.ConcurrentHashMap

internal class PendingResponseCounter(
    private val onAction: () -> Unit
) {
    private val counter = MutableStateFlow(0)
    private val pendingCommands = ConcurrentHashMap<FlipperRequest, Unit>()

    fun pendingCount(): StateFlow<Int> = counter.asStateFlow()

    fun hasPendingRequests(): Boolean {
        return if (counter.value > 0) {
            true
        } else if (counter.value < 0) {
            counter.value = 0
            if (BuildKonfig.CRASH_APP_ON_FAILED_CHECKS) {
                error("Pending response counter less than zero")
            }
            false
        } else {
            false
        }
    }

    fun rememberAction(request: FlipperRequest?) {
        if (BuildKonfig.LOGGING_PENDING_COMMANDS && request != null) {
            pendingCommands[request] = Unit
        }
        val tag = request?.javaClass?.simpleName.orEmpty()
        onAction.invoke()
        val pendingCount = counter.getAndUpdate { it + 1 }
        verbose { "Increase pending response command $tag, current size is ${pendingCount + 1}" }
    }

    fun forgetAction(request: FlipperRequest?) {
        if (BuildKonfig.LOGGING_PENDING_COMMANDS && request != null) {
            pendingCommands.remove(request)
        }
        val pendingCount = counter.updateAndGet { it - 1 }
        verbose { "Decrease pending response command, current size is $pendingCount" }
    }

    fun logPendingCommands() {
        if (BuildKonfig.LOGGING_PENDING_COMMANDS) {
            error { "Pending commands is: ${pendingCommands.keys.joinToString { it.data.toString() }}" }
        }
    }

    companion object {
        internal const val LAGS_FLIPPER_DETECT_TIMEOUT_MS = 30 * 1000L // 30 seconds
    }
}
