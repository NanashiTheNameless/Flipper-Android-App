package dev.namelessnanashi.flipperzero.bridge.connection.feature.seriallagsdetector.impl

import dev.namelessnanashi.flipperzero.bridge.connection.feature.actionnotifier.api.FlipperActionNotifier
import dev.namelessnanashi.flipperzero.bridge.connection.feature.restartrpc.api.FRestartRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.FlipperRequest
import dev.namelessnanashi.flipperzero.bridge.connection.feature.seriallagsdetector.api.FLagsDetectorFeature
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class FLagsDetectorFeatureImpl @AssistedInject constructor(
    @Assisted private val scope: CoroutineScope,
    @Assisted restartRpcFeatureApi: FRestartRpcFeatureApi?,
    @Assisted private val flipperActionNotifier: FlipperActionNotifier
) : FLagsDetectorFeature, LogTagProvider {
    override val TAG = "FlipperLagsDetector-${hashCode()}"

    private val pendingResponseCounter = PendingResponseCounter(
        onAction = flipperActionNotifier::notifyAboutAction
    )

    init {
        scope.launch(FlipperDispatchers.workStealingDispatcher) {
            flipperActionNotifier.getActionFlow().collectLatest { _ ->
                delay(PendingResponseCounter.LAGS_FLIPPER_DETECT_TIMEOUT_MS)
                if (pendingResponseCounter.hasPendingRequests()) {
                    error {
                        "We have pending commands, but flipper not respond " +
                            "${PendingResponseCounter.LAGS_FLIPPER_DETECT_TIMEOUT_MS}ms"
                    }
                    pendingResponseCounter.logPendingCommands()
                    if (restartRpcFeatureApi == null) {
                        error { "Fail to restart RPC because restartRpcFeatureApi is null" }
                    } else {
                        info { "Start restart RPC" }

                        restartRpcFeatureApi.restartRpc()
                    }
                }
            }
        }
    }

    override fun hasPendingRequests(): Flow<Boolean> = pendingResponseCounter.pendingCount()
        .map { it > 0 }
        .distinctUntilChanged()

    override fun notifyAboutAction() {
        flipperActionNotifier.notifyAboutAction()
    }

    override suspend fun <T> wrapPendingAction(
        request: FlipperRequest?,
        block: suspend () -> T
    ): T {
        pendingResponseCounter.rememberAction(request)
        val result = try {
            block()
        } finally {
            pendingResponseCounter.forgetAction(request)
        }
        return result
    }

    override fun <T> wrapPendingAction(request: FlipperRequest?, flow: Flow<T>): Flow<T> {
        return flow.onStart {
            pendingResponseCounter.rememberAction(request)
        }.onEach {
            flipperActionNotifier.notifyAboutAction()
        }.onCompletion {
            pendingResponseCounter.forgetAction(request)
        }
    }

    @AssistedFactory
    fun interface InternalFactory {
        operator fun invoke(
            scope: CoroutineScope,
            restartRpcFeatureApi: FRestartRpcFeatureApi?,
            flipperActionNotifier: FlipperActionNotifier
        ): FLagsDetectorFeatureImpl
    }
}
