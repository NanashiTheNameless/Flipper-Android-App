package dev.namelessnanashi.flipperzero.updater.card.model

import dev.namelessnanashi.flipperzero.updater.model.UpdateRequest

sealed class UpdatePendingState {
    object LowBattery : UpdatePendingState()
    object FileExtension : UpdatePendingState()
    object FileBig : UpdatePendingState()
    data class Ready(
        val request: UpdateRequest,
        val syncingState: SyncingState
    ) : UpdatePendingState()
}

enum class SyncingState {
    IN_PROGRESS,
    STOP,
    COMPLETE
}
