package dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.executor

import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.progress.ProgressListener
import dev.namelessnanashi.flipperzero.faphub.dao.api.FapDownloadApi
import dev.namelessnanashi.flipperzero.faphub.dao.api.FapNetworkApi
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.api.FapManifestApi
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapActionRequest
import dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.executor.actions.FapActionUpload
import dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.executor.actions.FapIconDownloader
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class UpdateActionExecutor @Inject constructor(
    fapDownloadApi: FapDownloadApi,
    fapUploadAction: FapActionUpload,
    fapNetworkApi: FapNetworkApi,
    private val fapManifestApi: FapManifestApi,
    private val fapIconDownloader: FapIconDownloader
) : PrepareFapActionExecutor(fapDownloadApi, fapUploadAction, fapNetworkApi) {
    override val TAG = "UpdateActionExecutor"

    suspend fun update(
        request: FapActionRequest.Update,
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

        fapManifestApi.add(
            pathToFap = path,
            fapManifestItem = request.from.copy(
                fullName = request.applicationName,
                versionUid = request.toVersion.id,
                iconBase64 = iconBase64Request.getOrNull() ?: request.from.iconBase64,
                sdkApi = getSdkApi(request.applicationUid, request.toVersion)
            )
        )
        info { "Fap manifest added by request $request" }
    }
}
