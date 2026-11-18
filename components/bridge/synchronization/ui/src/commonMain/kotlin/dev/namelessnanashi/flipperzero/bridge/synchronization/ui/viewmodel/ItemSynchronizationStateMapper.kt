package dev.namelessnanashi.flipperzero.bridge.synchronization.ui.viewmodel

import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationState
import dev.namelessnanashi.flipperzero.bridge.synchronization.ui.model.ItemSynchronizationState

object ItemSynchronizationStateMapper {
    internal fun getItemSynchronizedState(
        synchronized: Boolean,
        synchronizationState: SynchronizationState
    ): ItemSynchronizationState {
        if (synchronized) {
            return ItemSynchronizationState.SYNCHRONIZED
        }

        if (synchronizationState is SynchronizationState.InProgress) {
            return ItemSynchronizationState.IN_PROGRESS
        }

        return ItemSynchronizationState.NOT_SYNCHRONIZED
    }
}
