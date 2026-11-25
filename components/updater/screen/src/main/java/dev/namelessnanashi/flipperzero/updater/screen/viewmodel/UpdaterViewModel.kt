package dev.namelessnanashi.flipperzero.updater.screen.viewmodel

import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.FDeviceOrchestrator
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.FDeviceConnectStatus
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationApi
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationState
import dev.namelessnanashi.flipperzero.core.ktx.jre.combine
import dev.namelessnanashi.flipperzero.core.ktx.jre.launchWithLock
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.log.verbose
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.updater.api.UpdaterApi
import dev.namelessnanashi.flipperzero.updater.model.UpdateRequest
import dev.namelessnanashi.flipperzero.updater.model.UpdatingState
import dev.namelessnanashi.flipperzero.updater.screen.model.FailedReason
import dev.namelessnanashi.flipperzero.updater.screen.model.UpdaterScreenState
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import dev.zacsweers.metro.Inject

private const val CHECK_CANCEL_DELAY = 100L

class UpdaterViewModel @Inject constructor(
    private val updaterApi: UpdaterApi,
    private val synchronizationApi: SynchronizationApi,
    private val orchestrator: FDeviceOrchestrator
) : DecomposeViewModel(), LogTagProvider {
    override val TAG = "UpdaterViewModel"

    private val updaterScreenStateFlow = MutableStateFlow<UpdaterScreenState>(
        UpdaterScreenState.NotStarted
    )
    private val connectionState = orchestrator.getState()
    private val mutex = Mutex()
    private var updaterJob: Job? = null

    init {
        updaterJob = subscribeOnUpdaterFlow()
    }

    fun getState(): StateFlow<UpdaterScreenState> = updaterScreenStateFlow

    fun start(
        updateRequest: UpdateRequest?
    ) = launchWithLock(mutex, viewModelScope, "start") {
        startUnsafe(updateRequest)
    }

    private suspend fun startUnsafe(
        updateRequest: UpdateRequest?
    ) {
        if (updateRequest == null) {
            if (!updaterApi.isUpdateInProcess()) {
                updaterScreenStateFlow.emit(UpdaterScreenState.Finish)
            }
            return
        }

        updaterScreenStateFlow.emit(UpdaterScreenState.CancelingSynchronization(updateRequest))
        synchronizationApi.stop()

        info {
            "Wait until synchronization end. " +
                "Current state is ${synchronizationApi.getSynchronizationState().value}"
        }

        // Wait until synchronization is really canceled
        synchronizationApi.getSynchronizationState()
            .filter { it == SynchronizationState.NotStarted || it == SynchronizationState.Finished }
            .first()

        info { "Start updating" }

        updaterApi.start(updateRequest.copy(requestId = System.currentTimeMillis()))
    }

    fun retry(
        updateRequest: UpdateRequest?
    ) = launchWithLock(mutex, viewModelScope, "retry") {
        updaterJob?.cancelAndJoin()
        updaterApi.cancel(silent = true)

        info { "Wait until updating end" }

        while (updaterApi.isUpdateInProcess()) {
            delay(CHECK_CANCEL_DELAY)
        }
        updaterApi.resetState()
        updaterJob = subscribeOnUpdaterFlow()

        startUnsafe(updateRequest)
    }

    fun cancelUpdate() = launchWithLock(mutex, viewModelScope, "cancel") {
        updaterJob?.cancelAndJoin()
        updaterJob = null
        updaterApi.cancel()
        // Fork: leave the screen at once; the updater stops in the background and a new update
        // waits for it (see the start check above)
        updaterScreenStateFlow.emit(UpdaterScreenState.Finish)
    }

    private fun subscribeOnUpdaterFlow(): Job = updaterApi.getState()
        .combine(connectionState).onEach { (updatingState, connectionState) ->
            val updateRequest = updatingState.request
            val updaterScreenState = when (val state = updatingState.state) {
                UpdatingState.NotStarted -> UpdaterScreenState.NotStarted
                is UpdatingState.DownloadingFromNetwork ->
                    UpdaterScreenState.DownloadingFromNetwork(
                        percent = state.percent,
                        updateRequest = updateRequest
                    )

                is UpdatingState.UploadOnFlipper ->
                    UpdaterScreenState.UploadOnFlipper(
                        percent = state.percent,
                        updateRequest = updateRequest
                    )

                UpdatingState.FailedUpload,
                UpdatingState.FailedPrepare ->
                    UpdaterScreenState.Failed(FailedReason.UPLOAD_ON_FLIPPER)

                UpdatingState.FailedDownload ->
                    UpdaterScreenState.Failed(FailedReason.DOWNLOAD_FROM_NETWORK)

                UpdatingState.Complete,
                UpdatingState.Failed -> UpdaterScreenState.Finish

                UpdatingState.Rebooting ->
                    if (connectionState !is FDeviceConnectStatus.Connected) {
                        UpdaterScreenState.Finish
                    } else {
                        UpdaterScreenState.Rebooting
                    }

                UpdatingState.FailedOutdatedApp -> UpdaterScreenState.Failed(
                    FailedReason.OUTDATED_APP
                )

                UpdatingState.FailedSubGhzProvisioning -> UpdaterScreenState.Failed(
                    FailedReason.FAILED_SUB_GHZ_PROVISIONING
                )

                UpdatingState.FailedInternalStorage -> UpdaterScreenState.Failed(
                    FailedReason.FAILED_INT_STORAGE
                )

                UpdatingState.FailedCustomUpdate -> UpdaterScreenState.Failed(
                    FailedReason.FAILED_INTERNAL_UPDATE
                )

                UpdatingState.SubGhzProvisioning -> UpdaterScreenState.SubGhzProvisioning(
                    updateRequest = updateRequest
                )
            }
            verbose {
                "From updating state ${updatingState.state} " +
                    "and connection state $connectionState " +
                    "produce $updaterScreenState"
            }
            updaterScreenStateFlow.emit(updaterScreenState)
        }.launchIn(viewModelScope)
}
