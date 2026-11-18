package dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeature
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureQualifier
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FUnsafeDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.api.FGetInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FConnectedDeviceApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.zacsweers.metro.ContributesIntoMap
import kotlinx.coroutines.CoroutineScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@FDeviceFeatureQualifier(FDeviceFeature.SDK_VERSION)
@ContributesIntoMap(AppGraph::class, binding<FDeviceFeatureApi.Factory>())
class FSdkVersionFeatureFactoryImpl @Inject constructor(
    private val factory: FSdkVersionFeatureApiImpl.InternalFactory
) : FDeviceFeatureApi.Factory {
    override suspend fun invoke(
        unsafeFeatureDeviceApi: FUnsafeDeviceFeatureApi,
        scope: CoroutineScope,
        connectedDevice: FConnectedDeviceApi
    ): FDeviceFeatureApi? {
        val fGetInfoFeatureApi = unsafeFeatureDeviceApi
            .getUnsafe(FGetInfoFeatureApi::class)
            ?: return null
        return factory(
            fGetInfoFeatureApi = fGetInfoFeatureApi,
        )
    }
}
