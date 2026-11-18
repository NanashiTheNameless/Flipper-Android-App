package dev.namelessnanashi.flipperzero.updater.card.model

import android.content.Context
import android.net.Uri
import dev.namelessnanashi.flipperzero.updater.model.FirmwareVersion
import dev.namelessnanashi.flipperzero.updater.model.UpdateRequest

sealed class UpdatePending {
    data class URI(
        val uri: Uri,
        val context: Context,
        val currentVersion: FirmwareVersion
    ) : UpdatePending()
    data class Request(val updateRequest: UpdateRequest) : UpdatePending()
}
