package dev.namelessnanashi.flipperzero.share.receive.models

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.keyparser.api.model.FlipperKeyParsed

sealed class ReceiveState {
    data object NotStarted : ReceiveState()

    data class Pending(
        val flipperKey: FlipperKey,
        val parsed: FlipperKeyParsed,
        val isSaving: Boolean = false
    ) : ReceiveState()

    data object Finished : ReceiveState()
    data class Error(val type: ReceiverError) : ReceiveState()
}
