package dev.namelessnanashi.flipperzero.faphub.installation.stateprovider.impl.api

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapBuildState
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemVersion
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.api.FapManifestApi
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.error.NoSdCardException
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestState
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.FapInstallationQueueApi
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapActionRequest
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FAP_DOWNLOAD_PROGRESS_SHARE
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapQueueState
import dev.namelessnanashi.flipperzero.faphub.installation.stateprovider.api.api.FapInstallationStateManager
import dev.namelessnanashi.flipperzero.faphub.installation.stateprovider.api.model.FapState
import dev.namelessnanashi.flipperzero.faphub.installation.stateprovider.api.model.NotAvailableReason
import dev.namelessnanashi.flipperzero.faphub.target.api.FlipperTargetProviderApi
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.combine
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<FapInstallationStateManager>())
class FapInstallationStateManagerImpl @Inject constructor(
    private val fapManifestApi: FapManifestApi,
    private val queueApi: FapInstallationQueueApi,
    private val flipperTargetProviderApi: FlipperTargetProviderApi
) : FapInstallationStateManager, LogTagProvider {
    override val TAG = "FapInstallationStateManager"

    override fun getFapStateFlow(
        applicationUid: String,
        currentVersion: FapItemVersion
    ) = combine(
        fapManifestApi.getManifestFlow(),
        queueApi.getFlowById(applicationUid),
        flipperTargetProviderApi.getFlipperTarget()
    ) { manifests, queueState, target ->
        return@combine getState(manifests, applicationUid, queueState, currentVersion, target)
    }

    @Suppress("UnusedPrivateFunction")
    private fun getState(
        manifest: FapManifestState,
        applicationUid: String,
        queueState: FapQueueState,
        currentVersion: FapItemVersion,
        target: FlipperTarget?
    ): FapState {
        val stateFromQueue = queueStateToFapState(queueState)
        if (stateFromQueue != null) {
            return stateFromQueue
        }

        if (target == null) {
            return FapState.RetrievingManifest
        }

        when (target) {
            FlipperTarget.NotConnected -> return FapState.NotAvailableForInstall(
                NotAvailableReason.FLIPPER_NOT_CONNECTED
            )

            FlipperTarget.Unsupported -> return FapState.NotAvailableForInstall(
                NotAvailableReason.FLIPPER_OUTDATED
            )

            is FlipperTarget.Received -> {}
        }

        val stateFromManifest = manifestStateToFapState(
            manifest,
            applicationUid,
            currentVersion,
            target
        )

        if (stateFromManifest != null) {
            return stateFromManifest
        }

        when (currentVersion.buildState) {
            FapBuildState.READY, FapBuildState.READY_ON_RELEASE -> {
            }

            FapBuildState.BUILD_RUNNING -> return FapState.NotAvailableForInstall(NotAvailableReason.BUILD_RUNNING)

            FapBuildState.UNSUPPORTED_APP -> return FapState.NotAvailableForInstall(
                NotAvailableReason.UNSUPPORTED_APP
            )

            FapBuildState.FLIPPER_OUTDATED -> return FapState.NotAvailableForInstall(
                NotAvailableReason.FLIPPER_OUTDATED
            )

            FapBuildState.UNSUPPORTED_SDK -> return FapState.NotAvailableForInstall(
                NotAvailableReason.UNSUPPORTED_SDK
            )
        }

        return FapState.ReadyToInstall
    }

    private fun manifestStateToFapState(
        manifest: FapManifestState,
        applicationUid: String,
        currentVersion: FapItemVersion,
        flipperTarget: FlipperTarget.Received
    ) = when (manifest) {
        is FapManifestState.Loaded -> {
            val fapManifestItem = manifest.items.find { it.uid == applicationUid }
            if (fapManifestItem == null) {
                if (manifest.inProgress) {
                    FapState.RetrievingManifest
                } else {
                    null
                }
            } else {
                val sdkApi = fapManifestItem.sdkApi
                if (currentVersion.buildState != FapBuildState.READY) {
                    FapState.Installed
                } else if (fapManifestItem.versionUid != currentVersion.id) {
                    FapState.ReadyToUpdate(fapManifestItem)
                } else if (sdkApi == null ||
                    sdkApi.majorVersion != flipperTarget.sdk.majorVersion ||
                    flipperTarget.sdk.minorVersion < sdkApi.minorVersion
                ) {
                    FapState.ReadyToUpdate(fapManifestItem)
                } else {
                    FapState.Installed
                }
            }
        }

        is FapManifestState.NotLoaded -> when (manifest.throwable) {
            is NoSdCardException -> FapState.NotAvailableForInstall(NotAvailableReason.NO_SD_CARD)
            else -> FapState.NotAvailableForInstall(NotAvailableReason.FLIPPER_OUTDATED)
        }
    }

    private fun queueStateToFapState(queueState: FapQueueState) = when (queueState) {
        is FapQueueState.InProgress -> when (queueState.request) {
            is FapActionRequest.Cancel -> FapState.Canceling
            is FapActionRequest.Install -> FapState.InstallationInProgress(
                active = true,
                progress = queueState.float
            )

            is FapActionRequest.Update -> FapState.UpdatingInProgress(
                active = true,
                progress = queueState.float
            )

            is FapActionRequest.Delete -> FapState.Deleting
        }

        is FapQueueState.Pending -> when (queueState.request) {
            is FapActionRequest.Cancel -> FapState.Canceling
            is FapActionRequest.Install -> FapState.InstallationInProgress(
                active = false,
                progress = queueState.downloadProgress * FAP_DOWNLOAD_PROGRESS_SHARE
            )

            is FapActionRequest.Update -> FapState.UpdatingInProgress(
                active = false,
                progress = queueState.downloadProgress * FAP_DOWNLOAD_PROGRESS_SHARE
            )
            is FapActionRequest.Delete -> FapState.Deleting
        }

        FapQueueState.NotFound, is FapQueueState.Failed -> null
    }
}
