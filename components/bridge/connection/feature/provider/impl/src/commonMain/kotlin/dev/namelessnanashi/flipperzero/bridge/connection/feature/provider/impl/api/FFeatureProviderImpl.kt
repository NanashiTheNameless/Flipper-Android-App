package dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.device.common.api.FDeviceApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.impl.utils.FDeviceConnectStatusToDeviceApi
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.FDeviceOrchestrator
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.FDeviceConnectStatus
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.reflect.KClass
import dev.zacsweers.metro.binding

@SingleIn(AppGraph::class)
@ContributesBinding(AppGraph::class, binding<FFeatureProvider>())
class FFeatureProviderImpl @Inject constructor(
    private val orchestrator: FDeviceOrchestrator,
    private val deviceApiMapper: FDeviceConnectStatusToDeviceApi
) : FFeatureProvider {
    private val scope = CoroutineScope(FlipperDispatchers.workStealingDispatcher)

    private val deviceStateFlow = MutableStateFlow<FDeviceApi?>(null)

    init {
        orchestrator.getState().map { status ->
            when (status) {
                is FDeviceConnectStatus.Connected -> deviceApiMapper.get(status)
                is FDeviceConnectStatus.Connecting,
                is FDeviceConnectStatus.Disconnecting,
                is FDeviceConnectStatus.Disconnected -> null
            }
        }.onEach {
            deviceStateFlow.emit(it)
        }.launchIn(scope)
    }

    override fun <T : FDeviceFeatureApi> get(clazz: KClass<T>): Flow<FFeatureStatus<T>> {
        return deviceStateFlow.mapLatest { deviceApi ->
            return@mapLatest if (deviceApi == null) {
                FFeatureStatus.Retrieving
            } else {
                val feature = deviceApi.get(clazz)
                    ?: return@mapLatest FFeatureStatus.Unsupported
                return@mapLatest FFeatureStatus.Supported<T>(feature)
            }
        }
    }

    override suspend fun <T : FDeviceFeatureApi> getSync(clazz: KClass<T>): T? {
        return get(clazz)
            .filter { it !is FFeatureStatus.Retrieving }
            .map { featureStatus ->
                when (featureStatus) {
                    is FFeatureStatus.Supported -> featureStatus.featureApi
                    FFeatureStatus.NotFound,
                    FFeatureStatus.Unsupported -> null

                    FFeatureStatus.Retrieving -> error("Impossible situation")
                }
            }.first()
    }
}
