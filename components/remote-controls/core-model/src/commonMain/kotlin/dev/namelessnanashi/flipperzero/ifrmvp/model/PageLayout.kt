package dev.namelessnanashi.flipperzero.ifrmvp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PageLayout(
    @SerialName("buttons")
    val buttons: List<IfrButton>
)
