package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcstats.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FOnDeviceReadyFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FUnsafeDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.getUnsafe
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.api.FStorageInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FConnectedDeviceApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.zacsweers.metro.ContributesIntoSet
import kotlinx.coroutines.CoroutineScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesIntoSet(AppGraph::class, binding<FOnDeviceReadyFeatureApi.Factory>())
class FRpcStatsOnDeviceReadyFeatureFactoryImpl @Inject constructor(
    private val factory: FRpcStatsOnDeviceReadyFeatureApiImpl.InternalFactory
) : FOnDeviceReadyFeatureApi.Factory {
    override suspend fun invoke(
        unsafeFeatureDeviceApi: FUnsafeDeviceFeatureApi,
        scope: CoroutineScope,
        connectedDevice: FConnectedDeviceApi
    ): FOnDeviceReadyFeatureApi? {
        val storageApi =
            unsafeFeatureDeviceApi.getUnsafe(FStorageInfoFeatureApi::class) ?: return null

        return factory(
            scope = scope,
            storageFeatureApi = storageApi
        )
    }
}
