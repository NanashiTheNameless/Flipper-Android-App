package dev.namelessnanashi.flipperzero.bridge.connection.feature.screenstreaming.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeature
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureQualifier
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FUnsafeDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.api.FVersionFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.FRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FConnectedDeviceApi
import dev.namelessnanashi.flipperzero.core.data.SemVer
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.zacsweers.metro.ContributesIntoMap
import kotlinx.coroutines.CoroutineScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

private val API_SUPPORTED_UNLOCK = SemVer(
    majorVersion = 0,
    minorVersion = 16
)

@FDeviceFeatureQualifier(FDeviceFeature.SCREEN_UNLOCK)
@ContributesIntoMap(AppGraph::class, binding<FDeviceFeatureApi.Factory>())
class FScreenUnlockFactoryImpl @Inject constructor(
    private val factory: FScreenUnlockFeatureApiImpl.InternalFactory
) : FDeviceFeatureApi.Factory, LogTagProvider {
    override val TAG: String = "FScreenUnlockFactoryImpl"

    override suspend fun invoke(
        unsafeFeatureDeviceApi: FUnsafeDeviceFeatureApi,
        scope: CoroutineScope,
        connectedDevice: FConnectedDeviceApi
    ): FDeviceFeatureApi? {
        val versionApi = unsafeFeatureDeviceApi.getUnsafe(FVersionFeatureApi::class) ?: return null
        info { "Start request supported state for api level $API_SUPPORTED_UNLOCK" }
        val isSupported = versionApi.isSupported(API_SUPPORTED_UNLOCK)
        if (!isSupported) {
            error { "Failed init FScreenUnlockFeatureApi, because isSupported=false" }
            return null
        }
        info { "Version $API_SUPPORTED_UNLOCK supported, so continue building FScreenUnlockFeatureApi" }

        val rpcApi = unsafeFeatureDeviceApi.getUnsafe(FRpcFeatureApi::class) ?: return null
        return factory(
            rpcFeatureApi = rpcApi,
        )
    }
}
