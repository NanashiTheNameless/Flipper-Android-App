package dev.namelessnanashi.flipperzero.updater.impl

import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.api.FGetInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.getSync
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcStorageExistException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileUploadApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FListingStorageApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.update.api.FUpdateFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.FDeviceOrchestrator
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.FDeviceConnectStatus
import dev.namelessnanashi.flipperzero.core.FlipperStorageProvider
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.FOneTimeExecutionBleTask
import dev.namelessnanashi.flipperzero.faphub.installedtab.api.FapNeedUpdatePopUpHelper
import dev.namelessnanashi.flipperzero.updater.impl.model.IntFlashFullException
import dev.namelessnanashi.flipperzero.updater.impl.model.UpdateContentException
import dev.namelessnanashi.flipperzero.updater.impl.tasks.FlipperUpdateImageHelper
import dev.namelessnanashi.flipperzero.updater.impl.tasks.UploadToFlipperHelper
import dev.namelessnanashi.flipperzero.updater.impl.tasks.downloader.UpdateContentDownloader
import dev.namelessnanashi.flipperzero.updater.model.OfficialFirmware
import dev.namelessnanashi.flipperzero.updater.model.SubGhzProvisioningException
import dev.namelessnanashi.flipperzero.updater.model.UpdateContent
import dev.namelessnanashi.flipperzero.updater.model.UpdateRequest
import dev.namelessnanashi.flipperzero.updater.model.UpdatingState
import dev.namelessnanashi.flipperzero.updater.subghz.helpers.SubGhzProvisioningHelper
import dev.namelessnanashi.flipperzero.updater.subghz.model.FailedUploadSubGhzException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.net.UnknownHostException
import dev.zacsweers.metro.Inject

private const val DISCONNECT_WAIT_TIMEOUT_MS = 30 * 1000L

@Suppress("LongParameterList")
class UpdaterTask @Inject constructor(
    private val uploadToFlipperHelper: UploadToFlipperHelper,
    private val subGhzProvisioningHelper: SubGhzProvisioningHelper,
    private val updateContentDownloader: Set<UpdateContentDownloader>,
    private val fapNeedUpdatePopUpHelper: FapNeedUpdatePopUpHelper,
    private val storageProvider: FlipperStorageProvider,
    private val flipperUpdateImageHelper: FlipperUpdateImageHelper,
    private val orchestrator: FDeviceOrchestrator,
    private val fFeatureProvider: FFeatureProvider
) : FOneTimeExecutionBleTask<UpdateRequest, UpdatingState>(),
    LogTagProvider {
    override val TAG = "UpdaterTask"

    private var isStoppedManually = false

    override suspend fun onStopAsync(stateListener: suspend (UpdatingState) -> Unit) {
        if (!isStoppedManually) {
            stateListener(UpdatingState.NotStarted)
        }
    }

    override suspend fun startInternal(
        scope: CoroutineScope,
        input: UpdateRequest,
        stateListener: suspend (UpdatingState) -> Unit
    ) {
        val fUpdateFeatureApi = fFeatureProvider.getSync<FUpdateFeatureApi>() ?: run {
            error { "#startInternal could not get FUpdateFeatureApi" }
            return
        }
        val fGetInfoFeatureApi = fFeatureProvider.getSync<FGetInfoFeatureApi>() ?: run {
            error { "#startInternal could not get FGetInfoFeatureApi" }
            return
        }
        val fStorageFeatureApi = fFeatureProvider.getSync<FStorageFeatureApi>() ?: run {
            error { "#startInternal could not get FStorageFeatureApi" }
            return
        }
        val fFileUploadApi = fStorageFeatureApi.uploadApi()
        val fListingStorageApi: FListingStorageApi = fStorageFeatureApi.listingApi()

        try {
            startInternalUnwrapped(
                input = input,
                fFileUploadApi = fFileUploadApi,
                fGetInfoFeatureApi = fGetInfoFeatureApi,
                fListingStorageApi = fListingStorageApi,
                fUpdateFeatureApi = fUpdateFeatureApi,
                stateListener = {
                    if (it.isFinalState) {
                        isStoppedManually = true
                    }

                    stateListener(it)
                }
            )
        } finally {
            withContext(NonCancellable) {
                flipperUpdateImageHelper.stopImageOnFlipperSafe(fUpdateFeatureApi.displayApi())
            }
        }
    }

    @Suppress("LongMethod", "ComplexMethod", "CyclomaticComplexMethod")
    private suspend fun startInternalUnwrapped(
        input: UpdateRequest,
        fGetInfoFeatureApi: FGetInfoFeatureApi,
        fFileUploadApi: FFileUploadApi,
        fUpdateFeatureApi: FUpdateFeatureApi,
        fListingStorageApi: FListingStorageApi,
        stateListener: suspend (UpdatingState) -> Unit
    ) = storageProvider.useTemporaryFolder { tempFolder ->
        info { "Start update with folder: $tempFolder" }
        val updateContent = input.content

        val updaterFolder = File(tempFolder.toFile(), updateContent.folderName())
        try {
            downloadFirmwareLocal(input.content, updaterFolder, stateListener)
        } catch (e: Throwable) {
            error(e) { "Failed when download from network" }
            when (e) {
                is UpdateContentException -> stateListener(UpdatingState.FailedCustomUpdate)
                is CancellationException -> {}
                else -> if (input.content is OfficialFirmware) {
                    stateListener(UpdatingState.FailedDownload)
                } else {
                    stateListener(UpdatingState.FailedCustomUpdate)
                }
            }
            return@useTemporaryFolder
        }
        try {
            stateListener(UpdatingState.SubGhzProvisioning)
            subGhzProvisioningHelper.provideAndUploadSubGhz(
                fGetInfoFeatureApi = fGetInfoFeatureApi,
                fFileUploadApi = fFileUploadApi
            )
        } catch (e: SubGhzProvisioningException) {
            error(e) { "Failed receive subghz region" }
            stateListener(UpdatingState.FailedOutdatedApp)
            return@useTemporaryFolder
        } catch (e: FailedUploadSubGhzException) {
            error(e) { "Failed upload subghz provisioning" }
            stateListener(UpdatingState.FailedSubGhzProvisioning)
            return@useTemporaryFolder
        } catch (e: UnknownHostException) {
            error(e) { "Failed download subghz information" }
            stateListener(UpdatingState.FailedDownload)
            return@useTemporaryFolder
        } catch (e: CancellationException) {
            error(e) { "Cancel update" }
            return@useTemporaryFolder
        } catch (e: Throwable) {
            error(e) { "Failed when provide subghz provisioning" }
            stateListener(UpdatingState.FailedPrepare)
            return@useTemporaryFolder
        }

        stateListener(
            UpdatingState.UploadOnFlipper(0f)
        )
        val flipperPath = try {
            prepareToUpload(updaterFolder, fFileUploadApi, fUpdateFeatureApi)
        } catch (e: CancellationException) {
            error(e) { "Cancel prepare to upload" }
            return@useTemporaryFolder
        } catch (e: Throwable) {
            error(e) { "Failed when prepare upload to flipper" }
            stateListener(UpdatingState.FailedPrepare)
            return@useTemporaryFolder
        }

        try {
            uploadToFlipperHelper.uploadToFlipper(
                flipperPath = flipperPath,
                updaterFolder = updaterFolder,
                bootApi = fUpdateFeatureApi.bootApi(),
                fListingStorageApi = fListingStorageApi,
                fFileUploadApi = fFileUploadApi,
                stateListener = stateListener
            )
        } catch (e: CancellationException) {
            info { "Cancellation exception: ${e.message}" }
        } catch (e: Throwable) {
            error(e) { "Failed when upload to flipper" }
            when (e) {
                is IntFlashFullException -> stateListener(UpdatingState.FailedInternalStorage)
                else -> stateListener(UpdatingState.FailedUpload)
            }
            return@useTemporaryFolder
        }

        withTimeoutOrNull(DISCONNECT_WAIT_TIMEOUT_MS) {
            orchestrator.getState()
                .filterIsInstance<FDeviceConnectStatus.Disconnected>()
                .first()
        }

        fapNeedUpdatePopUpHelper.notifyIfUpdateAvailable()

        stateListener(UpdatingState.Rebooting)
    }

    private suspend fun downloadFirmwareLocal(
        content: UpdateContent,
        updaterFolder: File,
        stateListener: suspend (UpdatingState) -> Unit
    ) {
        val downloadHelper = updateContentDownloader
            .firstOrNull { it.isSupport(content) }
            ?: throw IllegalArgumentException("No one helper for upload fw to local")
        downloadHelper.downloadFirmwareLocal(content, updaterFolder, stateListener)
    }

    private suspend fun prepareToUpload(
        updaterFolder: File,
        fFileUploadApi: FFileUploadApi,
        fUpdateFeatureApi: FUpdateFeatureApi
    ): String {
        flipperUpdateImageHelper.loadImageOnFlipper(fUpdateFeatureApi.displayApi())

        val updateName: String = updaterFolder
            .listFiles()
            ?.first { it.isDirectory }
            ?.name
            ?: updaterFolder.name

        val flipperPath = "/ext/update/$updateName"
        try {
            fFileUploadApi.mkdir(flipperPath)
        } catch (e: FRpcStorageExistException) {
            info { "Storage already exists: ${e.message}" }
        } catch (e: Throwable) {
            error(e) { "#prepareToUpload could not mkdir" }
        }
        return flipperPath
    }
}
