package dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.executor

import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.progress.ProgressListener
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.api.FapManifestApi
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapActionRequest
import dev.zacsweers.metro.Inject

class FapActionExecutor @Inject constructor(
    private val installationExecutor: InstallationActionExecutor,
    private val updateActionExecutor: UpdateActionExecutor,
    private val fapManifestApi: FapManifestApi,
    private val artifactExecutor: ArtifactInstallationExecutor
) : LogTagProvider {
    override val TAG = "FapActionExecutor"
    suspend fun execute(
        fapAction: FapActionRequest,
        progressListener: ProgressListener
    ) {
        info { "Received fap action request $fapAction" }
        when (fapAction) {
            is FapActionRequest.InstallArtifact -> artifactExecutor.install(fapAction, progressListener)
            is FapActionRequest.Install -> installationExecutor.install(fapAction, progressListener)
            is FapActionRequest.Update -> updateActionExecutor.update(fapAction, progressListener)
            is FapActionRequest.Cancel -> error("Cancel shouldn't be here")
            is FapActionRequest.Delete -> fapManifestApi.remove(fapAction.applicationUid)
        }
    }
}
