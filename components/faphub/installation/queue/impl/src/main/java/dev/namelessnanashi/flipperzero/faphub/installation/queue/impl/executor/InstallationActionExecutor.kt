package dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.executor

import androidx.datastore.core.DataStore
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import dev.namelessnanashi.flipperzero.core.progress.ProgressListener
import dev.namelessnanashi.flipperzero.faphub.dao.api.FapDownloadApi
import dev.namelessnanashi.flipperzero.faphub.dao.api.FapNetworkApi
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.api.FapManifestApi
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestItem
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapActionRequest
import dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.executor.actions.FapActionUpload
import dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.executor.actions.FapIconDownloader
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import dev.namelessnanashi.flipperzero.faphub.utils.FapHubConstants.FLIPPER_APPS_FOLDER
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import java.io.File
import dev.zacsweers.metro.Inject

class InstallationActionExecutor @Inject constructor(
    fapDownloadApi: FapDownloadApi,
    fapUploadAction: FapActionUpload,
    fapNetworkApi: FapNetworkApi,
    private val dataStoreSettings: DataStore<Settings>,
    private val fapManifestApi: FapManifestApi,
    private val fapIconDownloader: FapIconDownloader
) : PrepareFapActionExecutor(fapDownloadApi, fapUploadAction, fapNetworkApi), LogTagProvider {
    override val TAG = "InstallationActionExecutor"

    suspend fun install(
        request: FapActionRequest.Install,
        progressListener: ProgressListener
    ) {
        val target = request.toVersion.target as? FlipperTarget.Received
            ?: error("Failed download fap for $request")
        // The icon only goes into the manifest: fetch it while the app uploads
        val (path, iconBase64Request) = coroutineScope {
            val icon = async {
                fapIconDownloader.downloadToBase64(request.iconUrl).onFailure {
                    error(it) { "Failed download ${request.iconUrl}" }
                }
            }
            uploadAndDownloadFap(request.toVersion.id, target, progressListener) to icon.await()
        }
        var finalFapPath = File(FLIPPER_APPS_FOLDER, request.categoryAlias).absolutePath
        finalFapPath = File(finalFapPath, "${request.applicationAlias}.fap").absolutePath

        fapManifestApi.add(
            pathToFap = path,
            fapManifestItem = FapManifestItem(
                applicationAlias = request.applicationAlias,
                uid = request.applicationUid,
                versionUid = request.toVersion.id,
                path = finalFapPath,
                fullName = request.applicationName,
                iconBase64 = iconBase64Request.getOrNull(),
                sdkApi = getSdkApi(request.applicationUid, request.toVersion),
                isDevCatalog = dataStoreSettings.data.first().use_dev_catalog
            )
        )
        info { "Fap manifest added by request $request" }
    }
}
