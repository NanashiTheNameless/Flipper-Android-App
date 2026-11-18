package dev.namelessnanashi.flipperzero.faphub.installation.button.impl.model

sealed interface OpenFapResult {
    object AllGood : OpenFapResult
    object FlipperIsBusy : OpenFapResult
    object Error : OpenFapResult
}
