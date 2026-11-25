package dev.namelessnanashi.flipperzero.bridge.synchronization.impl.api

import androidx.datastore.core.DataStore
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationApi
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationState
import dev.namelessnanashi.flipperzero.core.buildkonfig.BuildKonfig
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.SynchronizationTask
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.runBlockingWithLog
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@SingleIn(AppGraph::class)
@ContributesBinding(AppGraph::class, binding<SynchronizationApi>())
@Suppress("LongParameterList")
class SynchronizationApiImpl internal constructor(
    private val synchronizationTaskBuilder: SynchronizationTask.Builder,
    private val dataStore: DataStore<Settings>,
    private val scope: CoroutineScope
) : SynchronizationApi, LogTagProvider {
    @Inject
    constructor(
        synchronizationTaskBuilder: SynchronizationTask.Builder,
        dataStore: DataStore<Settings>
    ) : this(
        synchronizationTaskBuilder,
        dataStore,
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    )

    override val TAG = "SynchronizationApi-${hashCode()}"

    private val isLaunched = AtomicBoolean(false)
    private val synchronizationState = MutableStateFlow<SynchronizationState>(
        SynchronizationState.NotStarted
    )
    private var synchronizationTask: SynchronizationTask? = null
    private var markDirty = false
    private var cooldownJob: Job? = null

    @Synchronized
    override fun startSynchronization(force: Boolean) {
        info { "Request synchronization..." }
        if (!isLaunched.compareAndSet(false, true)) {
            if (force) {
                markDirty = true
            }
            info { "Synchronization skipped, because we already in synchronization" }
            return
        }
        if (BuildKonfig.CRASH_APP_ON_FAILED_CHECKS && !force) { // Option only for debug build
            if (runBlockingWithLog { dataStore.data.first().skip_auto_sync_in_debug }) {
                isLaunched.set(false)
                return
            }
        }
        if (cooldownJob != null) {
            isLaunched.set(false)
            markDirty = true
            info { "Synchronization queued until cooldown ends" }
            return
        }
        markDirty = false
        val localSynchronizationTask = synchronizationTaskBuilder.build()
        synchronizationTask = localSynchronizationTask
        info { "Create synchronization task ${localSynchronizationTask.hashCode()}" }

        localSynchronizationTask.start(input = Unit) { taskState ->
            if (onTaskState(localSynchronizationTask, taskState)) {
                localSynchronizationTask.onStop()
            }
        }
    }

    @Synchronized
    private fun onTaskState(task: SynchronizationTask, state: SynchronizationState): Boolean {
        if (synchronizationTask !== task) {
            return false
        }
        synchronizationState.update { state }
        if (state != SynchronizationState.Finished) {
            return false
        }
        synchronizationTask = null
        isLaunched.set(false)
        cooldownJob = scope.launch {
            delay(SYNC_COOLDOWN_MS)
            synchronized(this@SynchronizationApiImpl) {
                cooldownJob = null
                if (markDirty) {
                    startSynchronization(force = true)
                }
            }
        }
        return true
    }

    override suspend fun stop() {
        val task = synchronized(this) {
            markDirty = false
            // The UI shows it stopped at once; the task's Finished starts the cooldown.
            synchronizationState.value = SynchronizationState.Finished
            synchronizationTask
        }
        task?.onStop()
    }

    override fun isSynchronizationRunning() = isLaunched.get()

    override fun getSynchronizationState(): StateFlow<SynchronizationState> {
        return synchronizationState
    }

    private companion object {
        const val SYNC_COOLDOWN_MS = 5_000L
    }
}
