package dev.namelessnanashi.flipperzero.bridge.synchronization.ui.viewmodel

import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.SimpleKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationApi
import dev.namelessnanashi.flipperzero.bridge.synchronization.ui.model.ItemSynchronizationState
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import dev.zacsweers.metro.Inject

class SynchronizationStateViewModel @Inject constructor(
    private val synchronizationApi: SynchronizationApi,
    private val keysApi: SimpleKeyApi
) : DecomposeViewModel() {
    internal fun getSynchronizationState(
        keyPath: FlipperKeyPath
    ): Flow<ItemSynchronizationState> = combine(
        keysApi.getKeyAsFlow(keyPath),
        synchronizationApi.getSynchronizationState()
    ) { flipperKey, synchronizationState ->
        if (flipperKey == null) {
            return@combine ItemSynchronizationState.NOT_SYNCHRONIZED
        }

        return@combine ItemSynchronizationStateMapper.getItemSynchronizedState(
            flipperKey.synchronized,
            synchronizationState
        )
    }
}
