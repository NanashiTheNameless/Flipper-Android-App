package dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.api

import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.ktx.jre.withLock
import dev.namelessnanashi.flipperzero.core.ktx.jre.withLockResult
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.faphub.dao.api.FapDownloadApi
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapActionRequest
import dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.executor.FapActionExecutor
import dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.model.bundleKey
import dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.model.FapInternalQueueState
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import dev.zacsweers.metro.Inject

// Bundles downloaded at once while the queue uploads other apps
private const val PREFETCH_PARALLELISM = 8

class FapQueueRunner @Inject constructor(
    private val fapActionExecutor: FapActionExecutor,
    private val fapDownloadApi: FapDownloadApi,
) : LogTagProvider {
    override val TAG = "FapQueueRunner"

    private val scope = CoroutineScope(SupervisorJob() + FlipperDispatchers.workStealingDispatcher)

    private val mutex = Mutex()
    private val pendingTaskFlow =
        MutableStateFlow<ImmutableList<FapActionRequest>>(persistentListOf())
    private var currentTaskJob: Job? = null
    private val currentTaskFlow = MutableStateFlow<FapInternalQueueState?>(null)

    private val prefetchProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    fun prefetchProgressFlow() = prefetchProgress.asStateFlow()

    private val prefetchSemaphore = Semaphore(PREFETCH_PARALLELISM)
    private val prefetchStarted = mutableSetOf<String>()

    init {
        scope.launch {
            pendingTaskFlow.collect { loop() }
        }
        scope.launch {
            pendingTaskFlow.collect { prefetch(it) }
        }
    }

    /**
     * Uploading to the Flipper is the slow part of an install, so the bundles of queued apps are
     * downloaded into the cache meanwhile; the executor then finds them there. A failed prefetch
     * is only logged: the install downloads the bundle again and reports the error.
     */
    private fun prefetch(pending: List<FapActionRequest>) {
        pending.forEach { request ->
            val version = when (request) {
                is FapActionRequest.Install -> request.toVersion
                is FapActionRequest.Update -> request.toVersion
                else -> return@forEach
            }
            val target = version.target as? FlipperTarget.Received ?: return@forEach
            val key = request.bundleKey() ?: return@forEach
            if (!synchronized(prefetchStarted) { prefetchStarted.add(key) }) return@forEach
            scope.launch {
                try {
                    prefetchSemaphore.withPermit {
                        // Skip apps that were cancelled while waiting
                        if (pendingTaskFlow.value.any { it.applicationUid == request.applicationUid }) {
                            fapDownloadApi.downloadBundle(target, version.id) { progress ->
                                prefetchProgress.update { it + (key to progress.coerceIn(0f, 1f)) }
                            }
                            prefetchProgress.update { it + (key to 1f) }
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (throwable: Throwable) {
                    prefetchProgress.update { it - key }
                    error(throwable) { "Prefetch of ${version.id} failed" }
                } finally {
                    synchronized(prefetchStarted) { prefetchStarted.remove(key) }
                }
            }
        }
    }

    fun pendingTasksFlow() = pendingTaskFlow.asStateFlow()
    fun currentTaskFlow() = currentTaskFlow.asStateFlow()

    fun enqueue(actionRequest: FapActionRequest) = scope.launch {
        enqueueSync(actionRequest)
    }

    suspend fun enqueueSync(actionRequest: FapActionRequest) {
        if (actionRequest is FapActionRequest.Cancel) {
            cancelTasksForApplicationUid(actionRequest)
        } else {
            withLock(mutex, "enqueue") {
                pendingTaskFlow.update { list ->
                    list.filterNot { it.applicationUid == actionRequest.applicationUid }
                        .plus(actionRequest).toImmutableList()
                }
            }
        }
    }

    private suspend fun loop() {
        info { "Start loop" }
        val currentJob = takeJobForLoop() ?: return
        info { "Current job is: $currentJob" }
        currentJob.join()
        info { "Complete current job" }
    }

    private suspend fun takeJobForLoop() = withLockResult(mutex, "create_job") {
        val mutableList = pendingTaskFlow.value.toMutableList()
        val currentTask = mutableList.removeFirstOrNull() ?: return@withLockResult null
        info { "Start execute $currentTask" }
        pendingTaskFlow.emit(mutableList.toImmutableList())
        currentTaskFlow.emit(FapInternalQueueState.Scheduled(currentTask))
        return@withLockResult scope.launch {
            try {
                currentTaskFlow.emit(FapInternalQueueState.InProgress(currentTask, 0f))
                fapActionExecutor.execute(currentTask) { progress ->
                    currentTaskFlow.emit(FapInternalQueueState.InProgress(currentTask, progress))
                }
            } catch (cancellation: CancellationException) {
                // Cancelled by the user: not a failure
                throw cancellation
            } catch (throwable: Throwable) {
                error(throwable) { "While executing $currentTask" }
                currentTaskFlow.emit(FapInternalQueueState.Failed(currentTask, throwable))
            } finally {
                withContext(NonCancellable) {
                    withLock(mutex, "finally_clean") {
                        currentTaskJob = null
                        currentTaskFlow.emit(null)
                    }
                }
            }
        }.also { currentTaskJob = it }
    }

    private suspend fun cancelTasksForApplicationUid(
        actionRequest: FapActionRequest.Cancel
    ) = withLock(mutex, "cancel") {
        info { "Cancel tasks with application uid: $actionRequest" }
        if (currentTaskFlow.value?.request?.applicationUid == actionRequest.applicationUid) {
            info { "Current job is job for canceling, so cancel job" }
            currentTaskJob?.cancel()
            // Fork: show it as cancelled at once instead of after the upload unwinds
            currentTaskFlow.emit(null)
        }
        pendingTaskFlow.update { list ->
            list.filterNot { it.applicationUid == actionRequest.applicationUid }.toImmutableList()
        }
    }
}
