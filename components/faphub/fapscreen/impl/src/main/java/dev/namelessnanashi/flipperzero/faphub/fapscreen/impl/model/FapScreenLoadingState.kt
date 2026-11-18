package dev.namelessnanashi.flipperzero.faphub.fapscreen.impl.model

import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItem

sealed class FapScreenLoadingState {
    object Loading : FapScreenLoadingState()

    data class Loaded(
        val fapItem: FapItem,
        val shareUrl: String,
        val isHidden: Boolean
    ) : FapScreenLoadingState()
    data class Error(
        val throwable: Throwable
    ) : FapScreenLoadingState()
}
