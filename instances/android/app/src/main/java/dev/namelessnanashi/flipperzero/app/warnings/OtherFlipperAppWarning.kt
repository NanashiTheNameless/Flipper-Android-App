package dev.namelessnanashi.flipperzero.app.warnings

import dev.namelessnanashi.flipperzero.app.R
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.get
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.FRpcFeatureApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.inappnotification.api.InAppNotificationStorage
import dev.namelessnanashi.flipperzero.inappnotification.api.model.InAppNotification
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.atomic.AtomicBoolean

private const val WARNING_DURATION_MS = 10_000L

/**
 * Warns when another app (usually the official Flipper app) shares the connection: both apps'
 * storage commands interleave and corrupt files on the Flipper.
 */
@SingleIn(AppGraph::class)
class OtherFlipperAppWarning @Inject constructor(
    private val featureProvider: FFeatureProvider,
    private val inAppNotificationStorage: InAppNotificationStorage
) {
    private val scope = CoroutineScope(SupervisorJob() + FlipperDispatchers.workStealingDispatcher)
    private val isStarted = AtomicBoolean(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
        if (!isStarted.compareAndSet(false, true)) return
        featureProvider.get<FRpcFeatureApi>()
            .flatMapLatest { status ->
                (status as? FFeatureStatus.Supported<FRpcFeatureApi>)?.featureApi?.otherClientDetected()
                    ?: emptyFlow()
            }
            .onEach {
                inAppNotificationStorage.addNotification(
                    InAppNotification.Error(
                        titleId = R.string.other_flipper_app_title,
                        descId = R.string.other_flipper_app_desc,
                        actionTextId = null,
                        action = null,
                        durationMs = WARNING_DURATION_MS
                    )
                )
            }
            .launchIn(scope)
    }
}
