package dev.namelessnanashi.flipperzero.faphub.target.model

import dev.namelessnanashi.flipperzero.core.data.SemVer

sealed class FlipperTarget {
    data object Unsupported : FlipperTarget()

    data object NotConnected : FlipperTarget()

    data class Received(
        val target: String,
        val sdk: SemVer
    ) : FlipperTarget()
}
