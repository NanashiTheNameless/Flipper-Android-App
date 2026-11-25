package dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.api

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.FapInstallationQueueApi
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapActionRequest
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FAP_DOWNLOAD_PROGRESS_SHARE
import dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.model.bundleKey
import dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.model.FapInternalQueueState
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapQueueState
import dev.zacsweers.metro.ContributesBinding
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@SingleIn(AppGraph::class)
@ContributesBinding(AppGraph::class, binding<FapInstallationQueueApi>())
class FapInstallationQueueApiImpl @Inject constructor(
    private val queueRunner: FapQueueRunner
) : FapInstallationQueueApi, LogTagProvider {
    override val TAG = "FapInstallationQueueApi"

    override fun getFlowById(
        applicationUid: String
    ): Flow<FapQueueState> {
        return combine(
            queueRunner.currentTaskFlow(),
            queueRunner.pendingTasksFlow(),
            queueRunner.prefetchProgressFlow()
        ) { currentTask, pendingTasks, downloads ->
            val state = if (currentTask != null &&
                currentTask.request.applicationUid == applicationUid
            ) {
                currentTask.withDownloadProgress(downloads)
            } else {
                val task = pendingTasks.find { it.applicationUid == applicationUid }
                if (task != null) {
                    FapQueueState.Pending(task, downloads[task.bundleKey()] ?: 0f)
                } else {
                    FapQueueState.NotFound
                }
            }
            return@combine state
        }
    }

    override fun getAllTasks(): Flow<ImmutableList<FapQueueState>> {
        return combine(
            queueRunner.currentTaskFlow(),
            queueRunner.pendingTasksFlow(),
            queueRunner.prefetchProgressFlow()
        ) { currentTask, pendingTasks, downloads ->
            val toReturn = mutableListOf<FapQueueState>()
            if (currentTask != null) {
                toReturn.add(currentTask.withDownloadProgress(downloads))
            }
            toReturn.addAll(pendingTasks.map { FapQueueState.Pending(it, downloads[it.bundleKey()] ?: 0f) })

            return@combine toReturn.toPersistentList()
        }
    }

    override fun enqueue(actionRequest: FapActionRequest) {
        info { "Enqueue $actionRequest" }
        queueRunner.enqueue(actionRequest)
    }

    override suspend fun enqueueSync(actionRequest: FapActionRequest) {
        info { "Enqueue $actionRequest" }
        queueRunner.enqueueSync(actionRequest)
    }
    private fun FapInternalQueueState.withDownloadProgress(downloads: Map<String, Float>): FapQueueState {
        val state = toFapQueueState()
        val downloaded = downloads[request.bundleKey()] ?: return state
        return if (state is FapQueueState.InProgress && state.float < FAP_DOWNLOAD_PROGRESS_SHARE) {
            state.copy(float = maxOf(state.float, downloaded * FAP_DOWNLOAD_PROGRESS_SHARE))
        } else {
            state
        }
    }

}
