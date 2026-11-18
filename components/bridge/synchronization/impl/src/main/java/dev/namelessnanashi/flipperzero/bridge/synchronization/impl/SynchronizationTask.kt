package dev.namelessnanashi.flipperzero.bridge.synchronization.impl

import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.getSync
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileStorageMD5Api
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationState
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.di.TaskSynchronizationComponent
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.executor.DiffKeyExecutor
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.model.RestartSynchronizationException
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.FavoriteSynchronization
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.flipper.FlipperHashRepository
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.flipper.TimestampSynchronizationChecker
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.di.ComponentHolder
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.progress.DetailedProgressListener
import dev.namelessnanashi.flipperzero.core.progress.DetailedProgressWrapperTracker
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.FOneTimeExecutionBleTask
import dev.namelessnanashi.flipperzero.nfc.mfkey32.api.MfKey32Api
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

interface SynchronizationTask {

    data object SynchronizationStartedProgressDetail : DetailedProgressListener.Detail
    data object SynchronizationFinishedProgressDetail : DetailedProgressListener.Detail

    fun start(
        input: Unit,
        stateListener: suspend (SynchronizationState) -> Unit
    ): Job

    suspend fun onStop()

    interface Builder {
        fun build(): SynchronizationTask
    }
}

@ContributesBinding(AppGraph::class, binding<SynchronizationTask.Builder>())
class SynchronizationTaskBuilder @Inject constructor(
    private val featureProvider: FFeatureProvider,
    private val mfKey32Api: MfKey32Api
) : SynchronizationTask.Builder {
    override fun build(): SynchronizationTask {
        return SynchronizationTaskImpl(
            featureProvider,
            mfKey32Api
        )
    }
}

private const val START_SYNCHRONIZATION_PERCENT = 0.01f

class SynchronizationTaskImpl(
    private val featureProvider: FFeatureProvider,
    private val mfKey32Api: MfKey32Api
) : FOneTimeExecutionBleTask<Unit, SynchronizationState>(),
    SynchronizationTask,
    LogTagProvider {
    override val TAG = "SynchronizationTask"

    private fun mapState(
        detail: DetailedProgressListener.Detail?,
        progress: Float,
    ): SynchronizationState.InProgress {
        return when (detail) {
            is DiffKeyExecutor.DiffProgressDetail -> {
                SynchronizationState.InProgress.FileInProgress(
                    progress = progress,
                    fileName = detail.fileName,
                )
            }

            is TimestampSynchronizationChecker.TimestampsProgressDetail -> {
                SynchronizationState.InProgress.Prepare(
                    progress = progress,
                )
            }

            is FavoriteSynchronization.FavoritesProgressDetail -> {
                SynchronizationState.InProgress.Favorites(
                    progress = progress,
                )
            }

            is FlipperHashRepository.HashesProgressDetail -> {
                SynchronizationState.InProgress.PrepareHashes(
                    progress = progress,
                    keyType = detail.keyType
                )
            }

            else -> SynchronizationState.InProgress.Default(
                progress = progress,
            )
        }
    }

    override suspend fun startInternal(
        scope: CoroutineScope,
        input: Unit,
        stateListener: suspend (SynchronizationState) -> Unit
    ) {
        val storageFeatureApi = featureProvider
            .getSync<FStorageFeatureApi>()
            ?: error("Can't find storage feature api")
        startInternal(
            scope = scope,
            storageFeatureApi = storageFeatureApi,
            progressTracker = DetailedProgressWrapperTracker(
                min = 0f,
                max = 1f,
                progressListener = { progress, detail ->
                    val state = mapState(detail, progress)
                    stateListener(state)
                }
            )
        )
        stateListener(SynchronizationState.Finished)
    }

    override suspend fun onStopAsync(stateListener: suspend (SynchronizationState) -> Unit) {
        stateListener(SynchronizationState.Finished)
    }

    private suspend fun startInternal(
        scope: CoroutineScope,
        storageFeatureApi: FStorageFeatureApi,
        progressTracker: DetailedProgressWrapperTracker
    ) {
        info { "#startInternal" }
        val mfKey32check = scope.async { checkMfKey32Safe(storageFeatureApi.md5Api()) }
        try {
            progressTracker.onProgress(
                current = START_SYNCHRONIZATION_PERCENT,
                detail = SynchronizationTask.SynchronizationStartedProgressDetail
            )
            launch(
                storageFeatureApi = storageFeatureApi,
                progressTracker = DetailedProgressWrapperTracker(
                    min = START_SYNCHRONIZATION_PERCENT,
                    max = 1.0f,
                    progressListener = progressTracker
                )
            )
            progressTracker.onProgress(
                current = 1.0f,
                detail = SynchronizationTask.SynchronizationFinishedProgressDetail
            )
            mfKey32check.await()
        } catch (
            @Suppress("SwallowedException")
            restartException: RestartSynchronizationException
        ) {
            info { "Synchronization request restart" }
            startInternal(scope, storageFeatureApi, progressTracker)
        }
    }

    private suspend fun checkMfKey32Safe(mD5Api: FFileStorageMD5Api) = try {
        mfKey32Api.checkBruteforceFileExist(mD5Api)
    } catch (throwable: Throwable) {
        error(throwable) { "Failed check mfkey32" }
    }

    private suspend fun launch(
        storageFeatureApi: FStorageFeatureApi,
        progressTracker: DetailedProgressWrapperTracker
    ) = withContext(FlipperDispatchers.workStealingDispatcher) {
        val taskComponent = TaskSynchronizationComponent.ManualFactory
            .create(
                deps = ComponentHolder.component(),
                storageFeatureApi = storageFeatureApi
            )

        taskComponent.keysSynchronization.syncKeys(
            DetailedProgressWrapperTracker(
                min = 0f,
                max = 0.90f,
                progressListener = progressTracker
            )
        )
        taskComponent.favoriteSynchronization.syncFavorites(
            DetailedProgressWrapperTracker(
                min = 0.90f,
                max = 0.95f,
                progressListener = progressTracker
            )
        )
        progressTracker.onProgress(1.0f, SynchronizationTask.SynchronizationFinishedProgressDetail)
    }
}
