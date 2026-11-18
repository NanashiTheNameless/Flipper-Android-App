package dev.namelessnanashi.flipperzero.faphub.target.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.api.FSdkVersionFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.get
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.FDeviceOrchestrator
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.FDeviceConnectStatus
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.faphub.target.api.FlipperTargetProviderApi
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@SingleIn(AppGraph::class)
@ContributesBinding(AppGraph::class, binding<FlipperTargetProviderApi>())
class FlipperTargetProviderApiImpl @Inject constructor(
    private val fFeatureProviderApi: FFeatureProvider,
    private val fDeviceOrchestrator: FDeviceOrchestrator
) : FlipperTargetProviderApi, LogTagProvider {
    override val TAG = "FlipperTargetProviderApi"

    private val scope = CoroutineScope(SupervisorJob() + FlipperDispatchers.workStealingDispatcher)
    private val targetFlow = MutableStateFlow<FlipperTarget?>(null)

    init {
        scope.launch(FlipperDispatchers.workStealingDispatcher) {
            subscribe()
        }
    }

    override fun getFlipperTarget() = targetFlow.asStateFlow()

    private suspend fun subscribe() {
        info { "Start subscribe" }

        combine(
            fDeviceOrchestrator.getState(),
            fFeatureProviderApi.get<FSdkVersionFeatureApi>()
        ) { connectionState, status ->
            when (connectionState) {
                is FDeviceConnectStatus.Disconnecting,
                is FDeviceConnectStatus.Disconnected,
                is FDeviceConnectStatus.Connecting -> {
                    targetFlow.emit(FlipperTarget.NotConnected)
                    return@combine
                }

                is FDeviceConnectStatus.Connected -> Unit
            }

            targetFlow.emit(null)
            val semVer = when (status) {
                is FFeatureStatus.Supported -> status.featureApi.getSdkVersion().getOrNull()

                FFeatureStatus.Retrieving -> {
                    targetFlow.emit(FlipperTarget.NotConnected)
                    return@combine
                }

                FFeatureStatus.NotFound,
                FFeatureStatus.Unsupported -> {
                    targetFlow.emit(FlipperTarget.Unsupported)
                    return@combine
                }
            }
            info { "Sdk version is $semVer" }

            val targetState = when (semVer) {
                null -> null
                else -> FlipperTarget.Received(
                    target = "f7",
                    sdk = semVer
                )
            }
            targetFlow.emit(targetState)
        }.collect()
    }
}
