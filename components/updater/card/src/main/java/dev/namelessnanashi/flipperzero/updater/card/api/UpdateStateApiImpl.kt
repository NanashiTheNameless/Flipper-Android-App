package dev.namelessnanashi.flipperzero.updater.card.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.api.FVersionFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.model.FlipperSupportedState
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.get
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.FDeviceOrchestrator
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.FDeviceConnectStatus
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.updater.api.FlipperVersionProviderApi
import dev.namelessnanashi.flipperzero.updater.api.UpdateStateApi
import dev.namelessnanashi.flipperzero.updater.api.UpdaterApi
import dev.namelessnanashi.flipperzero.updater.model.FlipperUpdateState
import dev.namelessnanashi.flipperzero.updater.model.UpdatingState
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<UpdateStateApi>())
class UpdateStateApiImpl @Inject constructor(
    private val versionParser: FlipperVersionProviderApi,
    private val updaterApi: UpdaterApi,
    private val fFeatureProvider: FFeatureProvider,
    private val fDeviceOrchestrator: FDeviceOrchestrator
) : UpdateStateApi {
    override fun getFlipperUpdateState(
        scope: CoroutineScope
    ): Flow<FlipperUpdateState> {
        return combine(
            fDeviceOrchestrator.getState(),
            fFeatureProvider.get<FVersionFeatureApi>()
                .map { status -> status as? FFeatureStatus.Supported<FVersionFeatureApi> }
                .mapLatest { status -> status?.featureApi?.getSupportedStateFlow() },
            versionParser.getCurrentFlipperVersion(),
            updaterApi.getState()
        ) { deviceConnection, connectionState, flipperVersion, updaterState ->
            val isReady = connectionState?.value == FlipperSupportedState.READY &&
                deviceConnection is FDeviceConnectStatus.Connected

            return@combine if (isReady && flipperVersion != null) {
                when (updaterState.state) {
                    is UpdatingState.Rebooting -> {
                        updaterApi.onDeviceConnected(
                            flipperVersion
                        )
                        FlipperUpdateState.Ready
                    }

                    is UpdatingState.Complete ->
                        FlipperUpdateState.Complete(updaterState.request?.updateTo)

                    is UpdatingState.Failed ->
                        FlipperUpdateState.Failed(updaterState.request?.updateTo)

                    else -> FlipperUpdateState.Ready
                }
            } else if (updaterState.state is UpdatingState.Rebooting) {
                FlipperUpdateState.Updating
            } else if (deviceConnection is FDeviceConnectStatus.Disconnected) {
                FlipperUpdateState.NotConnected
            } else {
                FlipperUpdateState.ConnectingInProgress
            }
        }
    }
}
