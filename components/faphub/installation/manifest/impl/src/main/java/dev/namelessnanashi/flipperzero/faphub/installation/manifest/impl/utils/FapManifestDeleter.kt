package dev.namelessnanashi.flipperzero.faphub.installation.manifest.impl.utils

import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.getSync
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestItem
import java.io.File
import dev.zacsweers.metro.Inject

class FapManifestDeleter @Inject constructor(
    private val fFeatureProvider: FFeatureProvider
) : LogTagProvider {
    override val TAG: String = "FapManifestDeleter"

    suspend fun delete(fapManifestItem: FapManifestItem) {
        val manifestPath = File(
            FapManifestConstants.FAP_MANIFESTS_FOLDER_ON_FLIPPER,
            "${fapManifestItem.applicationAlias}.${FapManifestConstants.FAP_MANIFEST_EXTENSION}"
        ).path
        val deleteApi = fFeatureProvider.getSync<FStorageFeatureApi>()?.deleteApi()
        if (deleteApi == null) {
            error { "#delete could not getSync deleteApi" }
            return
        }
        deleteApi.delete(manifestPath)
            .onFailure { error(it) { "#delete could not delete $manifestPath" } }
        deleteApi.delete(fapManifestItem.path)
            .onFailure { error(it) { "#delete could not delete ${fapManifestItem.path}" } }
    }
}
