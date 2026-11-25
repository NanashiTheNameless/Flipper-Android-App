package dev.namelessnanashi.flipperzero.updater.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class UpdateCardState {
    data object InProgress : UpdateCardState()

    /**
     * @param reinstall the latest build of the channel, offered on long press as
     * "Update anyway" (fork); null when the channel has none
     */
    data class NoUpdate(
        val flipperVersion: FirmwareVersion,
        val reinstall: UpdateRequest? = null
    ) : UpdateCardState()

    data class UpdateAvailable(
        val update: UpdateRequest,
        val isOtherChannel: Boolean
    ) : UpdateCardState()

    data class UpdateFromFile(
        val flipperVersion: FirmwareVersion,
        val updateVersion: FirmwareVersion
    ) : UpdateCardState()

    data class Error(val type: UpdateErrorType) : UpdateCardState()
}
