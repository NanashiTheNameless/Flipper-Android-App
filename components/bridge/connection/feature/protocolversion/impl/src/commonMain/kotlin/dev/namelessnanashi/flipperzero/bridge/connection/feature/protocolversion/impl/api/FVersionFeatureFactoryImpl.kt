package dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeature
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureQualifier
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FUnsafeDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.FRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FConnectedDeviceApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.meta.FTransportMetaInfoApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.zacsweers.metro.ContributesIntoMap
import kotlinx.coroutines.CoroutineScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@FDeviceFeatureQualifier(FDeviceFeature.VERSION)
@ContributesIntoMap(AppGraph::class, binding<FDeviceFeatureApi.Factory>())
class FVersionFeatureFactoryImpl @Inject constructor(
    private val factory: FVersionFeatureApiImpl.InternalFactory
) : FDeviceFeatureApi.Factory {
    override suspend fun invoke(
        unsafeFeatureDeviceApi: FUnsafeDeviceFeatureApi,
        scope: CoroutineScope,
        connectedDevice: FConnectedDeviceApi
    ): FDeviceFeatureApi? {
        // USB has no GATT metadata: ask the Flipper over RPC instead
        val metaInfoApi = connectedDevice as? FTransportMetaInfoApi
            ?: unsafeFeatureDeviceApi.getUnsafe(FRpcFeatureApi::class)?.let(::RpcApiVersionMetaInfoApi)
            ?: return null
        return factory(
            scope = scope,
            metaInfoApi = metaInfoApi
        )
    }
}
