package dev.namelessnanashi.flipperzero.synchronization.stub

import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationApi
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationState
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.MutableStateFlow
import dev.zacsweers.metro.Inject

@ContributesBinding(AppGraph::class)
class SynchronizationApiStub @Inject constructor() : SynchronizationApi {
    override fun startSynchronization(force: Boolean) = Unit
    override fun getSynchronizationState() = MutableStateFlow(SynchronizationState.NotStarted)
    override fun isSynchronizationRunning() = false
    override suspend fun stop() = Unit
}
