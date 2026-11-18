package dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeature
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureQualifier
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FUnsafeDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.getUnsafe
import dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.api.FVersionFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.FRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl.fm.FFileStorageMD5ApiImpl
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl.fm.delete.FFileDeleteApiImpl
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl.fm.download.FFileDownloadApiImpl
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl.fm.listing.FListingStorageApiImpl
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl.fm.listing.FlipperListingDelegateDeprecated
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl.fm.listing.FlipperListingDelegateNew
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl.fm.timestamp.FFileTimestampApiImpl
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl.fm.timestamp.FFileTimestampApiNoop
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl.fm.upload.FFileUploadApiImpl
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FConnectedDeviceApi
import dev.namelessnanashi.flipperzero.core.data.SemVer
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.zacsweers.metro.ContributesIntoMap
import kotlinx.coroutines.CoroutineScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

private val API_SUPPORTED_MD5_LISTING = SemVer(
    majorVersion = 0,
    minorVersion = 20
)
private val API_SUPPORTED_TIMESTAMP = SemVer(majorVersion = 0, minorVersion = 13)

@FDeviceFeatureQualifier(FDeviceFeature.STORAGE)
@ContributesIntoMap(AppGraph::class, binding<FDeviceFeatureApi.Factory>())
class FFileStorageApiFactoryImpl @Inject constructor() : FDeviceFeatureApi.Factory {
    override suspend fun invoke(
        unsafeFeatureDeviceApi: FUnsafeDeviceFeatureApi,
        scope: CoroutineScope,
        connectedDevice: FConnectedDeviceApi
    ): FDeviceFeatureApi? {
        val rpcApi = unsafeFeatureDeviceApi.getUnsafe<FRpcFeatureApi>() ?: return null
        val versionApi = unsafeFeatureDeviceApi.getUnsafe<FVersionFeatureApi>()
            ?: return null
        val listingDelegate = if (versionApi.isSupported(API_SUPPORTED_MD5_LISTING)) {
            FlipperListingDelegateNew(rpcApi)
        } else {
            FlipperListingDelegateDeprecated(rpcApi)
        }

        return FStorageFeatureApiImpl(
            md5Api = FFileStorageMD5ApiImpl(rpcApi),
            fListingStorageApi = FListingStorageApiImpl(listingDelegate),
            fileUploadApi = FFileUploadApiImpl(rpcApi, scope = scope),
            fileDownloadApi = FFileDownloadApiImpl(rpcApi),
            deleteApi = FFileDeleteApiImpl(rpcApi),
            timestampApi = if (versionApi.isSupported(API_SUPPORTED_TIMESTAMP)) {
                FFileTimestampApiImpl(rpcApi)
            } else {
                FFileTimestampApiNoop()
            }
        )
    }
}
