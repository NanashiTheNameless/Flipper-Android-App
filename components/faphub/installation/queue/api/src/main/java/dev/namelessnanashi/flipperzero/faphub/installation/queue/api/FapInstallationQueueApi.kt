package dev.namelessnanashi.flipperzero.faphub.installation.queue.api

import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapActionRequest
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapQueueState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface FapInstallationQueueApi {
    fun getFlowById(applicationUid: String): Flow<FapQueueState>

    fun getAllTasks(): Flow<ImmutableList<FapQueueState>>

    fun enqueue(actionRequest: FapActionRequest)
    suspend fun enqueueSync(actionRequest: FapActionRequest)
}
