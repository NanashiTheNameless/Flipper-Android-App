package dev.namelessnanashi.flipperzero.updater.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.FDeviceOrchestrator
import dev.namelessnanashi.flipperzero.core.FlipperStorageProvider
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.faphub.installedtab.api.FapNeedUpdatePopUpHelper
import dev.namelessnanashi.flipperzero.updater.api.UpdaterApi
import dev.namelessnanashi.flipperzero.updater.impl.UpdaterTask
import dev.namelessnanashi.flipperzero.updater.impl.tasks.FlipperUpdateImageHelper
import dev.namelessnanashi.flipperzero.updater.impl.tasks.UploadToFlipperHelper
import dev.namelessnanashi.flipperzero.updater.impl.tasks.downloader.UpdateContentDownloader
import dev.namelessnanashi.flipperzero.updater.model.FirmwareChannel
import dev.namelessnanashi.flipperzero.updater.model.FirmwareVersion
import dev.namelessnanashi.flipperzero.updater.model.UpdateRequest
import dev.namelessnanashi.flipperzero.updater.model.UpdatingState
import dev.namelessnanashi.flipperzero.updater.model.UpdatingStateWithRequest
import dev.namelessnanashi.flipperzero.updater.subghz.helpers.SubGhzProvisioningHelper
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@SingleIn(AppGraph::class)
@Suppress("LongParameterList")
@ContributesBinding(AppGraph::class, binding<UpdaterApi>())
class UpdaterApiImpl @Inject constructor(
    private val updateContentDownloader: Set<UpdateContentDownloader>,
    private val subGhzProvisioningHelper: SubGhzProvisioningHelper,
    private val uploadToFlipperHelper: UploadToFlipperHelper,
    private val fapNeedUpdatePopUpHelper: FapNeedUpdatePopUpHelper,
    private val storageProvider: FlipperStorageProvider,
    private val fFeatureProvider: FFeatureProvider,
    private val flipperUpdateImageHelper: FlipperUpdateImageHelper,
    private val orchestrator: FDeviceOrchestrator,
) : UpdaterApi, LogTagProvider {
    override val TAG = "UpdaterApi"

    private val updatingState = MutableStateFlow(
        UpdatingStateWithRequest(UpdatingState.NotStarted, request = null)
    )

    private var currentActiveTask: UpdaterTask? = null
    private val isLaunched = AtomicBoolean(false)

    override fun start(updateRequest: UpdateRequest) {
        info { "Request update with file $updateRequest" }
        if (!isLaunched.compareAndSet(false, true)) {
            info { "Update skipped, because we already in update" }
            return
        }
        /**
         * Yes, the same behaviour can be achieved if you make a Provider using Dagger and get a new Instance each time.
         * But at this point it is critical for us to have a new instance of UpdaterTask every time we use it.
         * If by chance it happens to be the same instance, it will be very hard to debug.
         * The main idea is to simplify the code in complex places
         */
        val localActiveTask = UpdaterTask(
            uploadToFlipperHelper = uploadToFlipperHelper,
            subGhzProvisioningHelper = subGhzProvisioningHelper,
            updateContentDownloader = updateContentDownloader,
            fapNeedUpdatePopUpHelper = fapNeedUpdatePopUpHelper,
            storageProvider = storageProvider,
            flipperUpdateImageHelper = flipperUpdateImageHelper,
            orchestrator = orchestrator,
            fFeatureProvider = fFeatureProvider
        )
        currentActiveTask = localActiveTask

        localActiveTask.start(updateRequest) {
            info { "Updater state update to $it" }
            withContext(NonCancellable) {
                updatingState.emit(UpdatingStateWithRequest(it, request = updateRequest))

                if (it.isFinalState) {
                    currentActiveTask?.onStop()
                    currentActiveTask = null
                    isLaunched.set(false)
                }
            }
        }
    }

    override suspend fun cancel(silent: Boolean) {
        currentActiveTask?.onStop()
    }

    override fun onDeviceConnected(versionName: FirmwareVersion) {
        updatingState.update {
            if (it.state == UpdatingState.Rebooting) {
                if (it.request?.updateTo?.version == versionName.version) {
                    UpdatingStateWithRequest(UpdatingState.Complete, request = it.request)
                } else if (it.request?.updateTo?.channel != FirmwareChannel.CUSTOM) {
                    UpdatingStateWithRequest(UpdatingState.Failed, request = it.request)
                } else {
                    it
                }
            } else {
                it
            }
        }
    }

    override fun getState(): StateFlow<UpdatingStateWithRequest> = updatingState
    override fun resetState() {
        isLaunched.compareAndSet(true, false)
        updatingState.update {
            if (it.state != UpdatingState.NotStarted && it.state.isFinalState) {
                UpdatingStateWithRequest(UpdatingState.NotStarted, request = null)
            } else {
                it
            }
        }
    }

    override fun isUpdateInProcess(): Boolean {
        return isLaunched.get()
    }
}
