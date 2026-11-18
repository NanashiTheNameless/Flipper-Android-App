package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcstats.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcstats.api.FRpcStatsOnDeviceReadyFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.api.FStorageInfoFeatureApi
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope

class FRpcStatsOnDeviceReadyFeatureApiImpl @AssistedInject constructor(
    @Assisted private val scope: CoroutineScope,
    @Assisted private val storageFeatureApi: FStorageInfoFeatureApi
) : FRpcStatsOnDeviceReadyFeatureApi, LogTagProvider {
    override val TAG = "FlipperRpcStatsCollector"

    override suspend fun onReady() {
        storageFeatureApi.invalidate(
            scope,
            force = false
        )
    }

    @AssistedFactory
    fun interface InternalFactory {
        operator fun invoke(
            scope: CoroutineScope,
            storageFeatureApi: FStorageInfoFeatureApi
        ): FRpcStatsOnDeviceReadyFeatureApiImpl
    }
}
