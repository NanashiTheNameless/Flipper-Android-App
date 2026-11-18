package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.impl

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeature
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureQualifier
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FUnsafeDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.getUnsafe
import dev.namelessnanashi.flipperzero.bridge.connection.feature.restartrpc.api.FRestartRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.seriallagsdetector.api.FLagsDetectorFeature
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FConnectedDeviceApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.serial.FSerialDeviceApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.zacsweers.metro.ContributesIntoMap
import kotlinx.coroutines.CoroutineScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@FDeviceFeatureQualifier(FDeviceFeature.RPC)
@ContributesIntoMap(AppGraph::class, binding<FDeviceFeatureApi.Factory>())
class FRpcFeatureFactoryImpl @Inject constructor(
    private val rpcFeatureFactory: FRpcFeatureApiImpl.InternalFactory
) : FDeviceFeatureApi.Factory {
    override suspend fun invoke(
        unsafeFeatureDeviceApi: FUnsafeDeviceFeatureApi,
        scope: CoroutineScope,
        connectedDevice: FConnectedDeviceApi
    ): FDeviceFeatureApi? {
        val serialDeviceApi = connectedDevice as? FSerialDeviceApi ?: return null
        val lagsDetector =
            unsafeFeatureDeviceApi.getUnsafe(FLagsDetectorFeature::class) ?: return null
        val restartFeatureApi =
            unsafeFeatureDeviceApi.getUnsafe(FRestartRpcFeatureApi::class)
        return rpcFeatureFactory(
            scope = scope,
            serialApi = serialDeviceApi,
            lagsDetector = lagsDetector,
            restartApiFeature = restartFeatureApi
        )
    }
}
