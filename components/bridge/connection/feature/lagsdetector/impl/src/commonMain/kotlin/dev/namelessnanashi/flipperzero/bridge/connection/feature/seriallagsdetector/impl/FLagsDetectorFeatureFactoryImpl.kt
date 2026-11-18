package dev.namelessnanashi.flipperzero.bridge.connection.feature.seriallagsdetector.impl

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeature
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureQualifier
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FUnsafeDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.restartrpc.api.FRestartRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FConnectedDeviceApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.serial.FSerialDeviceApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.zacsweers.metro.ContributesIntoMap
import kotlinx.coroutines.CoroutineScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@FDeviceFeatureQualifier(FDeviceFeature.SERIAL_LAGS_DETECTOR)
@ContributesIntoMap(AppGraph::class, binding<FDeviceFeatureApi.Factory>())
class FLagsDetectorFeatureFactoryImpl @Inject constructor(
    private val lagsDetectorFeatureFactory: FLagsDetectorFeatureImpl.InternalFactory
) : FDeviceFeatureApi.Factory {
    override suspend fun invoke(
        unsafeFeatureDeviceApi: FUnsafeDeviceFeatureApi,
        scope: CoroutineScope,
        connectedDevice: FConnectedDeviceApi
    ): FDeviceFeatureApi? {
        val restartRpcFeature =
            unsafeFeatureDeviceApi.getUnsafe(FRestartRpcFeatureApi::class)
        val flipperActionNotifier = (connectedDevice as? FSerialDeviceApi)
            ?.getActionNotifier()
            ?: return null
        return lagsDetectorFeatureFactory(
            scope = scope,
            restartRpcFeatureApi = restartRpcFeature,
            flipperActionNotifier = flipperActionNotifier
        )
    }
}
