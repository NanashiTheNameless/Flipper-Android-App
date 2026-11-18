package dev.namelessnanashi.flipperzero.faphub.target.api

import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import kotlinx.coroutines.flow.StateFlow

interface FlipperTargetProviderApi {
    fun getFlipperTarget(): StateFlow<FlipperTarget?>
}
