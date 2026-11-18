package dev.namelessnanashi.flipperzero.faphub.fapscreen.impl.model

import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItem

sealed class FapDetailedControlState {
    object Loading : FapDetailedControlState()

    data class Installed(
        val fapItem: FapItem
    ) : FapDetailedControlState()

    data class InProgressOrNotInstalled(
        val fapItem: FapItem
    ) : FapDetailedControlState()
}
