package dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeature
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureQualifier
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FUnsafeDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.api.FGetInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FConnectedDeviceApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.meta.FTransportMetaInfoApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.zacsweers.metro.ContributesIntoMap
import kotlinx.coroutines.CoroutineScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@FDeviceFeatureQualifier(FDeviceFeature.GATT_INFO)
@ContributesIntoMap(AppGraph::class, binding<FDeviceFeatureApi.Factory>())
class FGattInfoFeatureFactoryImpl @Inject constructor(
    private val factory: FGattInfoFeatureApiImpl.InternalFactory
) : FDeviceFeatureApi.Factory, LogTagProvider {
    override val TAG = "FGattInfoFeatureFactory"

    override suspend fun invoke(
        unsafeFeatureDeviceApi: FUnsafeDeviceFeatureApi,
        scope: CoroutineScope,
        connectedDevice: FConnectedDeviceApi
    ): FDeviceFeatureApi? {
        // USB has no GATT services: read the same information over RPC
        val metaInfoApi = connectedDevice as? FTransportMetaInfoApi
            ?: unsafeFeatureDeviceApi.getUnsafe(FGetInfoFeatureApi::class)
                ?.let { RpcGattMetaInfoApi(it, scope) }
            ?: return null

        return factory(
            metaInfoApi = metaInfoApi,
            scope = scope
        )
    }
}
