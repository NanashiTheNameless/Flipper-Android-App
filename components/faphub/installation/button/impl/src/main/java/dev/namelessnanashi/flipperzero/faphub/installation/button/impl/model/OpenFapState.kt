package dev.namelessnanashi.flipperzero.faphub.installation.button.impl.model

import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapButtonConfig

sealed class OpenFapState {
    object Loading : OpenFapState()
    object NotSupported : OpenFapState()
    object Ready : OpenFapState()
    data class InProgress(val config: FapButtonConfig) : OpenFapState()
}
