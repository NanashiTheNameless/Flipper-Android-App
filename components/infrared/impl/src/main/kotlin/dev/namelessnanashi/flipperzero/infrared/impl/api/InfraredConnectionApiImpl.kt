package dev.namelessnanashi.flipperzero.infrared.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.emulate.api.FEmulateFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.api.FVersionFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.model.FlipperSupportedState
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.get
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.FDeviceOrchestrator
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.FDeviceConnectStatus
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationApi
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationState
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.infrared.api.InfraredConnectionApi
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<InfraredConnectionApi>())
class InfraredConnectionApiImpl @Inject constructor(
    private val synchronizationApi: SynchronizationApi,
    private val fDeviceOrchestrator: FDeviceOrchestrator,
    private val fFeatureProvider: FFeatureProvider
) : InfraredConnectionApi {
    override fun getState(): Flow<InfraredConnectionApi.InfraredEmulateState> {
        return combine(
            fFeatureProvider.get<FVersionFeatureApi>()
                .map { status -> status as? FFeatureStatus.Supported<FVersionFeatureApi> }
                .map { status -> status?.featureApi }
                .flatMapLatest { feature -> feature?.getVersionInformationFlow() ?: flowOf(null) },
            fFeatureProvider.get<FVersionFeatureApi>()
                .map { status -> status as? FFeatureStatus.Supported<FVersionFeatureApi> }
                .map { status -> status?.featureApi }
                .flatMapLatest { feature -> feature?.getSupportedStateFlow() ?: flowOf(null) },
            fDeviceOrchestrator.getState(),
            synchronizationApi.getSynchronizationState(),
            fFeatureProvider.get<FEmulateFeatureApi>()
                .map { status -> status as? FFeatureStatus.Supported<FEmulateFeatureApi> }
                .map { status -> status?.featureApi }
                .flatMapLatest { feature ->
                    feature?.isInfraredEmulationSupported ?: flowOf(false)
                },
        ) { versionInformation,
            supportedState,
            connectionState,
            synchronizationState,
            isInfraredEmulationSupported ->
            val infraredEmulateState = when {
                connectionState is FDeviceConnectStatus.Disconnected -> {
                    InfraredConnectionApi.InfraredEmulateState.NOT_CONNECTED
                }

                connectionState !is FDeviceConnectStatus.Connected ||
                    supportedState != FlipperSupportedState.READY -> {
                    InfraredConnectionApi.InfraredEmulateState.CONNECTING
                }

                synchronizationState is SynchronizationState.InProgress -> {
                    InfraredConnectionApi.InfraredEmulateState.SYNCING
                }

                versionInformation == null || !isInfraredEmulationSupported -> {
                    InfraredConnectionApi.InfraredEmulateState.UPDATE_FLIPPER
                }

                else -> {
                    InfraredConnectionApi.InfraredEmulateState.ALL_GOOD
                }
            }
            info { "#onServiceApiReady $infraredEmulateState" }
            infraredEmulateState
        }
    }
}
