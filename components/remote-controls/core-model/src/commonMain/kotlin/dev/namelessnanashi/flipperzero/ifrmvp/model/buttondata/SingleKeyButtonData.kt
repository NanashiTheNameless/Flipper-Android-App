package dev.namelessnanashi.flipperzero.ifrmvp.model.buttondata

import dev.namelessnanashi.flipperzero.ifrmvp.model.IfrKeyIdentifier
import kotlinx.serialization.Serializable

@Serializable
sealed interface SingleKeyButtonData : ButtonData {
    val keyIdentifier: IfrKeyIdentifier
}
